package com.tfmsp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tfmsp.backend.enums.SubscriptionStatus;

public class SubscriptionResponse {

    private Long subscriptionId;
    private Long bureauId;
    private Long planId;
    private String planName;
    private Integer memberLimit;
    private long currentMemberCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private BigDecimal pricePerMonth;
    private boolean govIdVerificationEnabled;

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getBureauId() {
        return bureauId;
    }

    public void setBureauId(Long bureauId) {
        this.bureauId = bureauId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Integer getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(Integer memberLimit) {
        this.memberLimit = memberLimit;
    }

    public long getCurrentMemberCount() {
        return currentMemberCount;
    }

    public void setCurrentMemberCount(long currentMemberCount) {
        this.currentMemberCount = currentMemberCount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public BigDecimal getPricePerMonth() {
        return pricePerMonth;
    }

    public void setPricePerMonth(BigDecimal pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    public boolean isGovIdVerificationEnabled() {
        return govIdVerificationEnabled;
    }

    public void setGovIdVerificationEnabled(boolean govIdVerificationEnabled) {
        this.govIdVerificationEnabled = govIdVerificationEnabled;
    }
}
