package com.tfmsp.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SubscriptionAssignRequest {

    @NotNull(message = "Plan id is required")
    private Long planId;

    @Min(value = 1, message = "Months must be at least 1")
    @Max(value = 36, message = "Months cannot be more than 36")
    private Integer months;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Integer getMonths() {
        return months;
    }

    public void setMonths(Integer months) {
        this.months = months;
    }
}