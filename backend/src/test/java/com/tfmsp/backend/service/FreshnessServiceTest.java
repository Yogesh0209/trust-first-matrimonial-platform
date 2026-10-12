package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tfmsp.backend.entity.AuditLog;
import com.tfmsp.backend.entity.Profile;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.AuditLogRepository;
import com.tfmsp.backend.repository.ProfileRepository;
import com.tfmsp.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FreshnessServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private AuditLogRepository auditLogRepository;

    private FreshnessService freshnessService;

    @BeforeEach
    void setUp() {
        freshnessService = new FreshnessService(
                userRepository,
                profileRepository,
                auditLogRepository
        );
    }

    @Test
    void confirmActive_RenewsFreshnessAndRestoresVisibility() {
        User user = new User();
        user.setId(10L);
        user.setStatus(UserStatus.ACTIVE);
        user.setLastActiveAt(LocalDateTime.now().minusDays(100));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        Profile profile = new Profile();
        profile.setId(20L);
        profile.setVisibility(Visibility.HIDDEN);
        when(profileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));

        freshnessService.confirmActive(10L);

        assertNotNull(user.getLastActiveAt());
        assertTrue(user.getLastActiveAt().isAfter(LocalDateTime.now().minusSeconds(5)));
        assertEquals(Visibility.VISIBLE, profile.getVisibility());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void sweepInactiveProfiles_HidesProfilesOlderThan90Days() {
        User inactiveUser = new User();
        inactiveUser.setId(1L);
        inactiveUser.setStatus(UserStatus.ACTIVE);
        inactiveUser.setLastActiveAt(LocalDateTime.now().minusDays(95)); // older than 90 days!

        User activeUser = new User();
        activeUser.setId(2L);
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setLastActiveAt(LocalDateTime.now().minusDays(10)); // fresh!

        when(userRepository.findAll()).thenReturn(List.of(inactiveUser, activeUser));

        Profile inactiveProfile = new Profile();
        inactiveProfile.setId(101L);
        inactiveProfile.setVisibility(Visibility.VISIBLE);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(inactiveProfile));

        int hiddenCount = freshnessService.sweepInactiveProfiles();

        assertEquals(1, hiddenCount);
        assertEquals(Visibility.HIDDEN, inactiveProfile.getVisibility());
        verify(profileRepository).save(inactiveProfile);
    }
}
