package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.PhotoUploadRequest;
import com.tfmsp.backend.dto.PrivacyUpdateRequest;
import com.tfmsp.backend.dto.ProfilePhotoResponse;
import com.tfmsp.backend.dto.ProfileRequest;
import com.tfmsp.backend.dto.ProfileResponse;
import com.tfmsp.backend.entity.Profile;
import com.tfmsp.backend.entity.ProfilePhoto;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Gender;
import com.tfmsp.backend.enums.MaritalStatus;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.ProfilePhotoRepository;
import com.tfmsp.backend.repository.ProfileRepository;
import com.tfmsp.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfilePhotoRepository profilePhotoRepository;

    @Mock
    private UserRepository userRepository;

    private ProfileService profileService;

    @Mock private ProfileAccessService profileAccessService;
    
    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileRepository, profilePhotoRepository, userRepository, profileAccessService);
    }

    private User createSampleUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setFullName("Priya Patel");
        user.setEmail("priya@example.com");
        user.setPhone("9876543210");
        user.setRole(Role.END_USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setPhoneVerified(true);
        user.setLastActiveAt(LocalDateTime.now());
        return user;
    }

    private Profile createSampleProfile(Long userId) {
        Profile profile = new Profile();
        profile.setId(100L);
        profile.setUserId(userId);
        profile.setGender(Gender.FEMALE);
        profile.setDateOfBirth(LocalDate.now().minusYears(24));
        profile.setMaritalStatus(MaritalStatus.NEVER_MARRIED);
        profile.setReligion("Hindu");
        profile.setMotherTongue("Gujarati");
        profile.setCity("Ahmedabad");
        profile.setHeightCm(165);
        profile.setEducation("B.Tech Computer Science");
        profile.setOccupation("Software Engineer");
        profile.setFamilyType("Nuclear");
        profile.setAbout("Looking for a compatible partner");
        profile.setTrustLevel(1);
        profile.setVisibility(Visibility.VISIBLE);
        return profile;
    }

    @Test
    void getOwnProfile_Success() {
        User user = createSampleUser(1L);
        Profile profile = createSampleProfile(1L);

        ProfilePhoto photo = new ProfilePhoto();
        photo.setId(10L);
        photo.setProfileId(100L);
        photo.setPhotoUrl("https://example.com/photo1.jpg");
        photo.setPrimary(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profilePhotoRepository.findByProfileIdOrderByIsPrimaryDescUploadedAtDesc(100L))
                .thenReturn(List.of(photo));

        ProfileResponse res = profileService.getOwnProfile(1L);

        assertNotNull(res);
        assertEquals("Priya Patel", res.getFullName());
        assertEquals(100, res.getCompleteness());
        assertNotNull(res.getTrustScore());
        assertEquals("https://example.com/photo1.jpg", res.getPrimaryPhotoUrl());
    }

    @Test
    void getOwnProfile_NotFound_ThrowsException() {
        User user = createSampleUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.getOwnProfile(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createOrUpdateOwnProfile_Success() {
        User user = createSampleUser(1L);
        Profile profile = createSampleProfile(1L);

        ProfileRequest req = new ProfileRequest();
        req.setMaritalStatus(MaritalStatus.NEVER_MARRIED);
        req.setMotherTongue("Hindi");
        req.setCity("Mumbai");
        req.setEducation("MBA");
        req.setOccupation("Product Manager");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse res = profileService.createOrUpdateOwnProfile(1L, req);

        assertNotNull(res);
        assertEquals("Mumbai", res.getCity());
        assertEquals("Hindi", res.getMotherTongue());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void createOrUpdateOwnProfile_PendingUser_ActivatesWhenComplete() {
        User user = createSampleUser(1L);
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(true);
        user.setPhoneVerified(true);

        Profile profile = createSampleProfile(1L);

        ProfileRequest req = new ProfileRequest();
        req.setMaritalStatus(MaritalStatus.NEVER_MARRIED);
        req.setMotherTongue("Hindi");
        req.setCity("Delhi");
        req.setEducation("B.Tech");
        req.setOccupation("Engineer");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        profileService.createOrUpdateOwnProfile(1L, req);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    void getPublicProfile_Visible_ShowsTrustBandOnly() {
        User owner = createSampleUser(2L);
        Profile profile = createSampleProfile(2L);

        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(profilePhotoRepository.findByProfileIdOrderByIsPrimaryDescUploadedAtDesc(100L))
                .thenReturn(Collections.emptyList());

        // Viewer is user 1 (not owner)
        ProfileResponse res = profileService.getPublicProfile(100L, 1L);

        assertNotNull(res);
        assertNull(res.getTrustScore(), "Raw trust score must not be visible to other viewers per FRD VER-06");
        assertNotNull(res.getTrustBand(), "Trust band must be visible");
    }

    @Test
    void getPublicProfile_Hidden_ThrowsNotFoundForOthers() {
        Profile profile = createSampleProfile(2L);
        profile.setVisibility(Visibility.HIDDEN);

        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));

        // Viewer is user 1 (not owner)
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.getPublicProfile(100L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void addPhoto_Success() {
        Profile profile = createSampleProfile(1L);
        PhotoUploadRequest req = new PhotoUploadRequest();
        req.setPhotoUrl("https://example.com/selfie.jpg");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profilePhotoRepository.countByProfileId(100L)).thenReturn(0L); // First photo
        when(profilePhotoRepository.save(any(ProfilePhoto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfilePhotoResponse res = profileService.addPhoto(1L, req);

        assertNotNull(res);
        assertTrue(res.isPrimary(), "First uploaded photo must automatically be primary per FRD PRO-02");
    }

    @Test
    void addPhoto_MaxLimitReached_ThrowsBadRequest() {
        Profile profile = createSampleProfile(1L);
        PhotoUploadRequest req = new PhotoUploadRequest();
        req.setPhotoUrl("https://example.com/photo7.jpg");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profilePhotoRepository.countByProfileId(100L)).thenReturn(6L); // Limit 6 reached

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.addPhoto(1L, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Maximum 6 photos allowed"));
    }

    @Test
    void deletePhoto_PromotesRemainingPhotoAsPrimary() {
        Profile profile = createSampleProfile(1L);

        ProfilePhoto photo1 = new ProfilePhoto();
        photo1.setId(10L);
        photo1.setProfileId(100L);
        photo1.setPrimary(true);

        ProfilePhoto photo2 = new ProfilePhoto();
        photo2.setId(11L);
        photo2.setProfileId(100L);
        photo2.setPrimary(false);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profilePhotoRepository.findByIdAndProfileId(10L, 100L)).thenReturn(Optional.of(photo1));
        when(profilePhotoRepository.findByProfileIdOrderByIsPrimaryDescUploadedAtDesc(100L))
                .thenReturn(new ArrayList<>(List.of(photo2)));

        profileService.deletePhoto(1L, 10L);

        verify(profilePhotoRepository).delete(photo1);
        assertTrue(photo2.isPrimary(), "Deleting primary photo must promote next remaining photo to primary");
        verify(profilePhotoRepository).save(photo2);
    }

    @Test
    void updatePrivacy_Success() {
        Profile profile = createSampleProfile(1L);
        PrivacyUpdateRequest req = new PrivacyUpdateRequest();
        req.setVisibility(Visibility.HIDDEN);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        profileService.updatePrivacy(1L, req);

        assertEquals(Visibility.HIDDEN, profile.getVisibility());
        verify(profileRepository).save(profile);
    }
}
