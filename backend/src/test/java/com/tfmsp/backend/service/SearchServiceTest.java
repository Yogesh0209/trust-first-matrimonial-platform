package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.SearchFilterCriteria;
import com.tfmsp.backend.dto.SearchResultResponse;
import com.tfmsp.backend.entity.*;
import com.tfmsp.backend.enums.Gender;
import com.tfmsp.backend.enums.MaritalStatus;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfilePhotoRepository profilePhotoRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BureauRepository bureauRepository;
    @Mock
    private ShortlistRepository shortlistRepository;
    @Mock
    private ProfileService profileService;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(
                profileRepository,
                profilePhotoRepository,
                userRepository,
                bureauRepository,
                shortlistRepository,
                profileService
        );
    }

    @Test
    void searchHomeBureau_FiltersAndReturnsCandidates() {
        User caller = new User();
        caller.setId(1L);
        caller.setBureauId(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(caller));

        Bureau bureau = new Bureau();
        bureau.setId(10L);
        bureau.setName("Delhi Rishtey");
        bureau.setShowBureauName(true);
        when(bureauRepository.findById(10L)).thenReturn(Optional.of(bureau));

        Profile callerProfile = new Profile();
        callerProfile.setId(11L);
        callerProfile.setGender(Gender.MALE);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(callerProfile));

        User candidate = new User();
        candidate.setId(2L);
        candidate.setFullName("Priya Patel");
        candidate.setBureauId(10L);
        candidate.setRole(Role.END_USER);
        candidate.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByBureauIdAndRole(10L, Role.END_USER)).thenReturn(List.of(candidate));

        Profile candProfile = new Profile();
        candProfile.setId(22L);
        candProfile.setUserId(2L);
        candProfile.setGender(Gender.FEMALE);
        candProfile.setDateOfBirth(LocalDate.now().minusYears(25));
        candProfile.setMaritalStatus(MaritalStatus.NEVER_MARRIED);
        candProfile.setCity("Delhi");
        candProfile.setVisibility(Visibility.VISIBLE);
        candProfile.setTrustLevel((short) 2);

        when(profileRepository.findByUserIdInAndVisibility(List.of(2L), Visibility.VISIBLE))
                .thenReturn(List.of(candProfile));
        when(profileService.calculateCompleteness(candProfile)).thenReturn(90);
        when(profileService.calculateTrustScore(candidate, candProfile, 90)).thenReturn(75);
        when(profileService.getTrustBand(75)).thenReturn("HIGH");

        SearchFilterCriteria criteria = new SearchFilterCriteria();
        criteria.setMinAge(22);
        criteria.setMaxAge(30);

        List<SearchResultResponse> results = searchService.searchHomeBureau(1L, criteria);

        assertEquals(1, results.size());
        assertEquals("Priya Patel", results.get(0).getFullName());
        assertEquals("HIGH", results.get(0).getTrustBand());
        assertEquals(75, results.get(0).getTrustScore());
        assertEquals(25, results.get(0).getAge());
    }

    @Test
    void searchShared_CrossBureauDisabled_ThrowsForbidden() {
        User caller = new User();
        caller.setId(1L);
        caller.setBureauId(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(caller));

        Bureau bureau = new Bureau();
        bureau.setId(10L);
        bureau.setSearchOtherBureaus(false); // disabled!
        when(bureauRepository.findById(10L)).thenReturn(Optional.of(bureau));

        SearchFilterCriteria criteria = new SearchFilterCriteria();
        assertThrows(ResponseStatusException.class, () -> searchService.searchShared(1L, criteria));
    }

    @Test
    void addToShortlist_Success() {
        Profile callerProfile = new Profile();
        callerProfile.setId(10L);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(callerProfile));

        Profile targetProfile = new Profile();
        targetProfile.setId(20L);
        targetProfile.setVisibility(Visibility.VISIBLE);
        when(profileRepository.findById(20L)).thenReturn(Optional.of(targetProfile));
        when(shortlistRepository.existsById(any())).thenReturn(false);

        searchService.addToShortlist(1L, 20L);

        verify(shortlistRepository).save(any(Shortlist.class));
    }

    @Test
    void addToShortlist_SelfShortlist_ThrowsBadRequest() {
        Profile callerProfile = new Profile();
        callerProfile.setId(10L);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(callerProfile));

        assertThrows(ResponseStatusException.class, () -> searchService.addToShortlist(1L, 10L));
    }
}
