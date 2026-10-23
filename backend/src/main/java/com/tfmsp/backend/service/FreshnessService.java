package com.tfmsp.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.PageRequest;
import java.util.stream.Collectors;

import com.tfmsp.backend.dto.AuditLogResponse;
import com.tfmsp.backend.dto.StillLookingRequest;
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

    @Scheduled(cron = "${app.freshness.cron:0 0 2 * * *}", zone = "Asia/Kolkata")
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

        System.out.println("Freshness sweep finished. Profiles hidden: " + hiddenCount);
        auditLogRepository.save(new AuditLog(
                null,
                "FRESHNESS_SWEEP",
                "SYSTEM",
                null,
                "Freshness sweep finished: " + hiddenCount + " profile(s) hidden"
        ));
        return hiddenCount;
    }
    
    @Transactional
    public String respondStillLooking(Long userId, StillLookingRequest.Choice choice) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // only normal members can change their own status; pending/suspended/etc. cannot
        if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.PAUSED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your account status does not allow this action");
        }

        String action;
        switch (choice) {
            case ACTIVE -> {
                user.setStatus(UserStatus.ACTIVE);
                user.setLastActiveAt(LocalDateTime.now());
                profileRepository.findByUserId(userId).ifPresent(p -> {
                    p.setVisibility(Visibility.VISIBLE);
                    profileRepository.save(p);
                });
                action = "STILL_LOOKING";
            }
            case PAUSED -> {
                user.setStatus(UserStatus.PAUSED);
                action = "PROFILE_PAUSED";
            }
            default -> {
                user.setStatus(UserStatus.ARCHIVED);
                action = "ENGAGED_OR_MARRIED";
            }
        }
        userRepository.save(user);

        auditLogRepository.save(new AuditLog(userId, action, "USER", userId,
                "Member answered 'Still looking?' with " + choice));

        return user.getStatus().name();
    }
    
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentRuns(int limit) {
        int size = Math.max(1, Math.min(limit, 100));
        return auditLogRepository.findByActionOrderByCreatedAtDesc("FRESHNESS_SWEEP", PageRequest.of(0, size))
                .stream()
                .map(log -> {
                    AuditLogResponse res = new AuditLogResponse();
                    res.setId(log.getId());
                    res.setActorUserId(log.getActorUserId());
                    res.setAction(log.getAction());
                    res.setEntityType(log.getEntityType());
                    res.setEntityId(log.getEntityId());
                    res.setDetails(log.getDetails());
                    res.setCreatedAt(log.getCreatedAt());
                    return res;
                })
                .collect(Collectors.toList());
    }
}
