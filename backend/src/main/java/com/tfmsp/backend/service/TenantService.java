package com.tfmsp.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.*;
import com.tfmsp.backend.entity.*;
import com.tfmsp.backend.enums.*;
import com.tfmsp.backend.repository.*;

@Service
public class TenantService {

    private final BureauRepository bureauRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository profilePhotoRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;

    public TenantService(
            BureauRepository bureauRepository,
            UserRepository userRepository,
            ProfileRepository profileRepository,
            ProfilePhotoRepository profilePhotoRepository,
            PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder,
            ProfileService profileService) {
        this.bureauRepository = bureauRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.profilePhotoRepository = profilePhotoRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileService = profileService;
    }

    @Transactional
    public BureauResponse registerBureau(BureauRegisterRequest req) {
        String normalizedCode = req.getBureauCode().trim().toUpperCase();

        if (bureauRepository.existsByBureauCode(normalizedCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bureau code '" + normalizedCode + "' is already registered");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        if (userRepository.existsByPhone(req.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone is already registered");
        }

        Bureau bureau = new Bureau();
        bureau.setName(req.getBureauName().trim());
        bureau.setBureauCode(normalizedCode);
        bureau.setCity(req.getCity().trim());
        bureau.setStatus(BureauStatus.PENDING.name());
        bureau.setShareMembers(false);
        bureau.setSearchOtherBureaus(false);
        bureau.setShowBureauName(false);
        Bureau savedBureau = bureauRepository.save(bureau);

        User hostUser = new User();
        hostUser.setBureauId(savedBureau.getId());
        hostUser.setRole(Role.HOST);
        hostUser.setFullName(req.getHostFullName().trim());
        hostUser.setEmail(req.getEmail().trim().toLowerCase());
        hostUser.setPhone(req.getPhone().trim());
        hostUser.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        hostUser.setEmailVerified(false);
        hostUser.setPhoneVerified(false);
        hostUser.setStatus(UserStatus.PENDING);
        userRepository.save(hostUser);

        // Assign default or chosen subscription plan
        String planName = (req.getPlanName() != null && !req.getPlanName().isBlank()) ? req.getPlanName() : "Basic";
        Plan plan = planRepository.findByNameIgnoreCase(planName)
                .orElseGet(() -> planRepository.findAll().stream().findFirst()
                        .orElseGet(() -> planRepository.save(new Plan("Basic", java.math.BigDecimal.ZERO, 100, false))));

        Subscription subscription = new Subscription(
                savedBureau.getId(),
                plan.getId(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                SubscriptionStatus.ACTIVE
        );
        subscriptionRepository.save(subscription);

        auditLogRepository.save(new AuditLog(
                hostUser.getId(),
                "REGISTER_BUREAU",
                "BUREAU",
                savedBureau.getId(),
                "New bureau registered with code: " + normalizedCode
        ));

        return toBureauResponse(savedBureau, 0L, plan.getName());
    }

    public List<BureauResponse> getAllBureaus(String statusFilter) {
        List<Bureau> bureaus = bureauRepository.findAll();
        List<BureauResponse> responses = new ArrayList<>();

        for (Bureau b : bureaus) {
            if (statusFilter != null && !statusFilter.isBlank()) {
                if (!b.getStatus().equalsIgnoreCase(statusFilter.trim())) {
                    continue;
                }
            }
            long memberCount = userRepository.countByBureauId(b.getId());
            String planName = subscriptionRepository
                    .findFirstByBureauIdAndStatusOrderByEndDateDesc(b.getId(), SubscriptionStatus.ACTIVE)
                    .flatMap(s -> planRepository.findById(s.getPlanId()))
                    .map(Plan::getName)
                    .orElse("None");

            responses.add(toBureauResponse(b, memberCount, planName));
        }

        return responses;
    }

    @Transactional
    public BureauResponse updateBureauStatus(Long bureauId, BureauStatus newStatus,
            String reason, Long actorAdminId) {

        if (newStatus == BureauStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A bureau cannot be moved back to PENDING");
        }
        if (newStatus == BureauStatus.SUSPENDED && (reason == null || reason.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A reason is required to suspend a bureau");
        }

        Bureau bureau = bureauRepository.findById(bureauId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bureau not found"));

        bureau.setStatus(newStatus.name());
        Bureau saved = bureauRepository.save(bureau);

        // Approving a bureau also activates its Host account(s)
        if (newStatus == BureauStatus.ACTIVE) {
            for (User host : userRepository.findByBureauIdAndRole(bureauId, Role.HOST)) {
                host.setStatus(UserStatus.ACTIVE);
                userRepository.save(host);
            }
        }

        auditLogRepository.save(new AuditLog(
                actorAdminId,
                "UPDATE_BUREAU_STATUS",
                "BUREAU",
                bureauId,
                "Status updated to: " + newStatus.name()
                        + (reason != null && !reason.isBlank() ? " | Reason: " + reason.trim() : "")
        ));

        long memberCount = userRepository.countByBureauId(bureau.getId());
        String planName = subscriptionRepository
                .findFirstByBureauIdAndStatusOrderByEndDateDesc(bureau.getId(), SubscriptionStatus.ACTIVE)
                .flatMap(s -> planRepository.findById(s.getPlanId()))
                .map(Plan::getName)
                .orElse("None");

        return toBureauResponse(saved, memberCount, planName);
    }

    // Kept so older callers and tests that pass no reason still work
    public BureauResponse updateBureauStatus(Long bureauId, BureauStatus newStatus, Long actorAdminId) {
        return updateBureauStatus(bureauId, newStatus, null, actorAdminId);
    }

    public List<MemberSummaryResponse> getHostMembers(Long bureauId) {
        List<User> members = userRepository.findByBureauIdAndRole(bureauId, Role.END_USER);
        List<MemberSummaryResponse> responses = new ArrayList<>();

        for (User u : members) {
            MemberSummaryResponse res = new MemberSummaryResponse();
            res.setUserId(u.getId());
            res.setFullName(u.getFullName());
            res.setEmail(u.getEmail());
            res.setPhone(u.getPhone());
            res.setStatus(u.getStatus());
            res.setCreatedAt(u.getCreatedAt());
            res.setLastActiveAt(u.getLastActiveAt());

            profileRepository.findByUserId(u.getId()).ifPresent(p -> {
                res.setProfileId(p.getId());
                res.setTrustLevel((short) p.getTrustLevel());
                res.setGender(p.getGender());
                res.setMaritalStatus(p.getMaritalStatus());
                res.setCity(p.getCity());

                int completeness = profileService.calculateCompleteness(p);
                res.setCompletenessPercentage(completeness);
                res.setTrustScore(profileService.calculateTrustScore(u, p, completeness));
            });

            responses.add(res);
        }

        return responses;
    }

    @Transactional
    public MemberSummaryResponse updateMemberStatus(Long bureauId, Long memberUserId, UserStatus newStatus, Long actorHostId) {
        User user = userRepository.findById(memberUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        if (!bureauId.equals(user.getBureauId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: member does not belong to this bureau");
        }

        user.setStatus(newStatus);
        if (newStatus == UserStatus.SUSPENDED || newStatus == UserStatus.DELETED) {
            user.setBlocked(true);
        } else if (newStatus == UserStatus.ACTIVE) {
            user.setBlocked(false);
        }
        User saved = userRepository.save(user);

        auditLogRepository.save(new AuditLog(
                actorHostId,
                "UPDATE_MEMBER_STATUS",
                "USER",
                memberUserId,
                "Member status updated to: " + newStatus.name()
        ));

        MemberSummaryResponse res = new MemberSummaryResponse();
        res.setUserId(saved.getId());
        res.setFullName(saved.getFullName());
        res.setEmail(saved.getEmail());
        res.setPhone(saved.getPhone());
        res.setStatus(saved.getStatus());
        res.setCreatedAt(saved.getCreatedAt());
        res.setLastActiveAt(saved.getLastActiveAt());

        profileRepository.findByUserId(saved.getId()).ifPresent(p -> {
            res.setProfileId(p.getId());
            res.setTrustLevel((short) p.getTrustLevel());
            res.setGender(p.getGender());
            res.setMaritalStatus(p.getMaritalStatus());
            res.setCity(p.getCity());

            int completeness = profileService.calculateCompleteness(p);
            res.setCompletenessPercentage(completeness);
            res.setTrustScore(profileService.calculateTrustScore(saved, p, completeness));
        });

        return res;
    }

    public SubscriptionResponse getBureauSubscription(Long bureauId) {
        Subscription sub = subscriptionRepository
                .findFirstByBureauIdAndStatusOrderByEndDateDesc(bureauId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active subscription found for this bureau"));

        Plan plan = planRepository.findById(sub.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Associated plan not found"));

        long currentCount = userRepository.countByBureauId(bureauId);

        SubscriptionResponse res = new SubscriptionResponse();
        res.setSubscriptionId(sub.getId());
        res.setBureauId(bureauId);
        res.setPlanId(plan.getId());
        res.setPlanName(plan.getName());
        res.setMemberLimit(plan.getMemberLimit());
        res.setCurrentMemberCount(currentCount);
        res.setStartDate(sub.getStartDate());
        res.setEndDate(sub.getEndDate());
        res.setStatus(sub.getStatus());
        res.setPricePerMonth(plan.getPricePerMonth());
        res.setGovIdVerificationEnabled(plan.isGovIdVerification());
        return res;
    }

    @Transactional
    public SubscriptionResponse upgradeSubscription(Long bureauId, Long newPlanId, Long actorUserId) {
        Plan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));

        // Cancel previous active subscriptions
        subscriptionRepository.findFirstByBureauIdAndStatusOrderByEndDateDesc(bureauId, SubscriptionStatus.ACTIVE)
                .ifPresent(s -> {
                    s.setStatus(SubscriptionStatus.CANCELLED);
                    subscriptionRepository.save(s);
                });

        Subscription newSub = new Subscription(
                bureauId,
                newPlan.getId(),
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                SubscriptionStatus.ACTIVE
        );
        Subscription savedSub = subscriptionRepository.save(newSub);

        auditLogRepository.save(new AuditLog(
                actorUserId,
                "UPGRADE_SUBSCRIPTION",
                "SUBSCRIPTION",
                savedSub.getId(),
                "Upgraded to plan: " + newPlan.getName()
        ));

        long currentCount = userRepository.countByBureauId(bureauId);

        SubscriptionResponse res = new SubscriptionResponse();
        res.setSubscriptionId(savedSub.getId());
        res.setBureauId(bureauId);
        res.setPlanId(newPlan.getId());
        res.setPlanName(newPlan.getName());
        res.setMemberLimit(newPlan.getMemberLimit());
        res.setCurrentMemberCount(currentCount);
        res.setStartDate(savedSub.getStartDate());
        res.setEndDate(savedSub.getEndDate());
        res.setStatus(savedSub.getStatus());
        res.setPricePerMonth(newPlan.getPricePerMonth());
        res.setGovIdVerificationEnabled(newPlan.isGovIdVerification());
        return res;
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public void checkMemberQuota(Long bureauId) {
        subscriptionRepository.findFirstByBureauIdAndStatusOrderByEndDateDesc(bureauId, SubscriptionStatus.ACTIVE)
                .flatMap(s -> planRepository.findById(s.getPlanId()))
                .ifPresent(plan -> {
                    if (plan.getMemberLimit() != null) {
                        long currentCount = userRepository.countByBureauId(bureauId);
                        if (currentCount >= plan.getMemberLimit()) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                    "Bureau member limit (" + plan.getMemberLimit() + ") reached for " + plan.getName() + " plan. Please upgrade.");
                        }
                    }
                });
    }

    private BureauResponse toBureauResponse(Bureau bureau, long memberCount, String planName) {
        BureauResponse res = new BureauResponse();
        res.setId(bureau.getId());
        res.setName(bureau.getName());
        res.setBureauCode(bureau.getBureauCode());
        res.setCity(bureau.getCity());
        res.setStatus(bureau.getStatus());
        res.setShareMembers(bureau.isShareMembers());
        res.setSearchOtherBureaus(bureau.isSearchOtherBureaus());
        res.setShowBureauName(bureau.isShowBureauName());
        res.setMemberCount(memberCount);
        res.setActivePlan(planName);
        res.setCreatedAt(bureau.getCreatedAt());
        return res;
    }
}
