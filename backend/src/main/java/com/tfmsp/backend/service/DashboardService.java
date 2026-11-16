package com.tfmsp.backend.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.tfmsp.backend.dto.AdminDashboardResponse;
import com.tfmsp.backend.dto.AuditLogResponse;
import com.tfmsp.backend.dto.HostDashboardResponse;
import com.tfmsp.backend.entity.*;
import com.tfmsp.backend.enums.InterestStatus;
import com.tfmsp.backend.enums.ReportStatus;
import com.tfmsp.backend.enums.ReviewStatus;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.SubscriptionStatus;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.repository.*;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final BureauRepository bureauRepository;
    private final ProfileRepository profileRepository;
    private final VerificationRepository verificationRepository;
    private final InterestRepository interestRepository;
    private final ReportRepository reportRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuditLogRepository auditLogRepository;

    public DashboardService(
            UserRepository userRepository,
            BureauRepository bureauRepository,
            ProfileRepository profileRepository,
            VerificationRepository verificationRepository,
            InterestRepository interestRepository,
            ReportRepository reportRepository,
            PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.bureauRepository = bureauRepository;
        this.profileRepository = profileRepository;
        this.verificationRepository = verificationRepository;
        this.interestRepository = interestRepository;
        this.reportRepository = reportRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public HostDashboardResponse getHostDashboard(Long bureauId) {
        List<User> bureauMembers = userRepository.findByBureauIdAndRole(bureauId, Role.END_USER);
        long totalMembers = bureauMembers.size();
        long activeMembers = bureauMembers.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();

        Set<Long> userIds = bureauMembers.stream().map(User::getId).collect(Collectors.toSet());
        List<Profile> profiles = userIds.isEmpty()
                ? List.of()
                : profileRepository.findByUserIdIn(new ArrayList<>(userIds));
        Set<Long> profileIds = profiles.stream().map(Profile::getId).collect(Collectors.toSet());
        List<Long> profileIdList = new ArrayList<>(profileIds);

        // Pending verifications (only this bureau's members)
        long pendingVerifications = profileIdList.isEmpty() ? 0
                : verificationRepository
                        .findByStatusAndProfileIdInOrderBySubmittedAtAsc(ReviewStatus.PENDING, profileIdList).size();

        // Accepted matches
        long acceptedInterests = profileIdList.isEmpty() ? 0
                : interestRepository.countByStatusForProfiles(InterestStatus.ACCEPTED, profileIdList);
        
        // Open reports
        long openReports = reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.OPEN)
                .stream()
                .filter(r -> profileIds.contains(r.getReportedProfileId()))
                .count();

        // Subscription info
        Subscription activeSub = subscriptionRepository
                .findFirstByBureauIdAndStatusOrderByEndDateDesc(bureauId, SubscriptionStatus.ACTIVE)
                .orElse(null);

        String planName = "Basic";
        Integer limit = 100;
        if (activeSub != null) {
            Plan plan = planRepository.findById(activeSub.getPlanId()).orElse(null);
            if (plan != null) {
                planName = plan.getName();
                limit = plan.getMemberLimit();
            }
        }

        int usagePercentage = (limit != null && limit > 0)
                ? (int) Math.min(100, Math.round(((double) totalMembers / limit) * 100))
                : 0;

        HostDashboardResponse res = new HostDashboardResponse();
        res.setTotalMembers(totalMembers);
        res.setActiveMembers(activeMembers);
        res.setPendingVerifications(pendingVerifications);
        res.setAcceptedInterests(acceptedInterests);
        res.setOpenReports(openReports);
        res.setSubscriptionPlan(planName);
        res.setMemberLimit(limit);
        res.setQuotaUsagePercentage(usagePercentage);

        return res;
    }

    public AdminDashboardResponse getAdminDashboard() {
        List<Bureau> bureaus = bureauRepository.findAll();
        long totalBureaus = bureaus.size();
        long activeBureaus = bureaus.stream().filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus())).count();
        long pendingBureaus = bureaus.stream().filter(b -> "PENDING".equalsIgnoreCase(b.getStatus())).count();

        long totalUsers = userRepository.count();

        long reviewedVerifications = verificationRepository.countByStatusNot(ReviewStatus.PENDING);

        long openReports = reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.OPEN).size();

        // Plan distribution
        Map<Long, String> planNameMap = planRepository.findAll().stream()
                .collect(Collectors.toMap(Plan::getId, Plan::getName));

        Map<String, Long> distribution = new HashMap<>();
        List<Subscription> activeSubs = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);

        for (Subscription sub : activeSubs) {
            String pName = planNameMap.getOrDefault(sub.getPlanId(), "Other");
            distribution.put(pName, distribution.getOrDefault(pName, 0L) + 1);
        }

        AdminDashboardResponse res = new AdminDashboardResponse();
        res.setTotalBureaus(totalBureaus);
        res.setActiveBureaus(activeBureaus);
        res.setPendingBureaus(pendingBureaus);
        res.setTotalPlatformUsers(totalUsers);
        res.setTotalVerificationsReviewed(reviewedVerifications);
        res.setOpenReports(openReports);
        res.setPlanDistribution(distribution);

        return res;
    }

    public List<AuditLogResponse> getAuditLogs(int limit) {
        int size = Math.max(1, Math.min(limit, 200));
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, size)).stream()
                .map(this::toAuditResponse)
                .collect(Collectors.toList());
    }
    
    public List<AuditLogResponse> getHostAuditLogs(Long bureauId, int limit) {
        int size = Math.max(1, Math.min(limit, 200));
        return auditLogRepository.findByBureau(bureauId, PageRequest.of(0, size)).stream()
                .map(this::toAuditResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse toAuditResponse(AuditLog log) {
        AuditLogResponse res = new AuditLogResponse();
        res.setId(log.getId());
        res.setActorUserId(log.getActorUserId());
        res.setAction(log.getAction());
        res.setEntityType(log.getEntityType());
        res.setEntityId(log.getEntityId());
        res.setDetails(log.getDetails());
        res.setCreatedAt(log.getCreatedAt());
        return res;
    }
}
