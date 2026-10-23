package com.tfmsp.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tfmsp.backend.entity.AuditLog;
import com.tfmsp.backend.entity.Subscription;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.SubscriptionStatus;
import com.tfmsp.backend.repository.AuditLogRepository;
import com.tfmsp.backend.repository.SubscriptionRepository;
import com.tfmsp.backend.repository.UserRepository;

@Service
public class SubscriptionExpiryService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogRepository auditLogRepository;

    public SubscriptionExpiryService(SubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            AuditLogRepository auditLogRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogRepository = auditLogRepository;
    }

    // Runs every day at 1 AM India time
    @Scheduled(cron = "${app.subscription.cron:0 0 1 * * *}", zone = "Asia/Kolkata")
    @Transactional
    public Map<String, Integer> runDailyCheck() {
        LocalDate today = LocalDate.now();

        // 1. Mark finished subscriptions as EXPIRED and tell the Host
        List<Subscription> ended = subscriptionRepository
                .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, today);
        for (Subscription s : ended) {
            s.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(s);
            auditLogRepository.save(new AuditLog(null, "SUBSCRIPTION_EXPIRED", "SUBSCRIPTION",
                    s.getId(), "Subscription ended on " + s.getEndDate()));
            notifyHosts(s.getBureauId(), "SUBSCRIPTION_EXPIRED",
                    "Your subscription expired on " + s.getEndDate()
                            + ". Please renew to continue using member features.");
        }

        // 2. Warn the Host exactly 7 days before the end date
        List<Subscription> soon = subscriptionRepository
                .findByStatusAndEndDate(SubscriptionStatus.ACTIVE, today.plusDays(7));
        for (Subscription s : soon) {
            notifyHosts(s.getBureauId(), "SUBSCRIPTION_EXPIRING",
                    "Your subscription will expire on " + s.getEndDate() + " (in 7 days). Please renew.");
        }

        return Map.of("expired", ended.size(), "warned", soon.size());
    }

    private void notifyHosts(Long bureauId, String type, String message) {
        userRepository.findByBureauIdAndRole(bureauId, Role.HOST)
                .forEach(h -> notificationService.notify(h.getId(), type, message));
    }
}