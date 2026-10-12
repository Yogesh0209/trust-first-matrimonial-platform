package com.tfmsp.backend.dto;

import jakarta.validation.constraints.NotNull;

public class SubscriptionUpgradeRequest {

    @NotNull(message = "Plan ID is required")
    private Long planId;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }
}
