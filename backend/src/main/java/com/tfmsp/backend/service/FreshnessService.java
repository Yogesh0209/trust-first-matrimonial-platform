package com.tfmsp.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.entity.AuditLog;
import com.tfmsp.backend.entity.Profile;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.AuditLogRepository;
import com.tfmsp.backend.repository.ProfileRepository;
import com.tfmsp.backend.repository.UserRepository;

@Service
public class FreshnessService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AuditLogRepository auditLogRepository;

    public FreshnessService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void confirmActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setLastActiveAt(LocalDateTime.now());
        userRepository.save(user);

        // If profile was hidden due to inactivity, restore visibility
        profileRepository.findByUserId(userId).ifPresent(profile -> {
            if (profile.getVisibility() == Visibility.HIDDEN && user.getStatus() == UserStatus.ACTIVE) {
                profile.setVisibility(Visibility.VISIBLE);
                profileRepository.save(profile);
            }
        });

        auditLogRepository.save(new AuditLog(
                userId,
                "CONFIRM_ACTIVE",
                "USER",
                userId,
                "User confirmed active status; freshness renewed"
        ));
    }

    @Transactional
    public int sweepInactiveProfiles() {
        LocalDateTime cutoff90Days = LocalDateTime.now().minusDays(90);
        List<User> users = userRepository.findAll();
        int hiddenCount = 0;

        for (User u : users) {
            if (u.getStatus() == UserStatus.ACTIVE && u.getLastActiveAt() != null && u.getLastActiveAt().isBefore(cutoff90Days)) {
                Profile profile = profileRepository.findByUserId(u.getId()).orElse(null);
                if (profile != null && profile.getVisibility() == Visibility.VISIBLE) {
                    profile.setVisibility(Visibility.HIDDEN);
                    profileRepository.save(profile);
                    hiddenCount++;

                    auditLogRepository.save(new AuditLog(
                            null,
                            "AUTO_HIDE_INACTIVITY_90D",
                            "PROFILE",
                            profile.getId(),
                            "Profile auto-hidden due to >90 days of inactivity (last active: " + u.getLastActiveAt() + ")"
                    ));
                }
            }
        }

        return hiddenCount;
    }
}
