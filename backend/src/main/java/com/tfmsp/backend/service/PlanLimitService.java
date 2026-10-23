package com.tfmsp.backend.service;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.entity.Plan;
import com.tfmsp.backend.entity.Subscription;
import com.tfmsp.backend.enums.SubscriptionStatus;
import com.tfmsp.backend.repository.PlanRepository;
import com.tfmsp.backend.repository.SubscriptionRepository;

@Service
public class PlanLimitService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    public PlanLimitService(SubscriptionRepository subscriptionRepository,
            PlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }

    /** Returns the member limit of the bureau's active plan; null means unlimited. */
    public Plan getActivePlan(Long bureauId) {
        Subscription sub = subscriptionRepository
                .findFirstByBureauIdAndStatusOrderByEndDateDesc(bureauId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "PLAN: no active subscription for this bureau"));
        if (sub.getEndDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "PLAN: subscription has expired");
        }
        return planRepository.findById(sub.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "PLAN: plan not found"));
    }

    public Integer getMemberLimit(Long bureauId) {
        return getActivePlan(bureauId).getMemberLimit();
    }
    
    public void requireCrossBureau(Long bureauId) {
        if (bureauId == null) return;
        if (!getActivePlan(bureauId).isCrossBureauSearch()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "PLAN: cross-bureau search needs the Pro or Enterprise plan");
        }
    }

    public void requireGovIdVerification(Long bureauId) {
        if (bureauId == null) return;
        if (!getActivePlan(bureauId).isGovIdVerification()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "PLAN: ID verification needs the Pro or Enterprise plan");
        }
    }
}