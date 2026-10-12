package com.tfmsp.backend.dto;

public class HostDashboardResponse {

    private long totalMembers;
    private long activeMembers;
    private long pendingVerifications;
    private long acceptedInterests;
    private long openReports;
    private String subscriptionPlan;
    private Integer memberLimit;
    private int quotaUsagePercentage;

    public long getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(long totalMembers) {
        this.totalMembers = totalMembers;
    }

    public long getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(long activeMembers) {
        this.activeMembers = activeMembers;
    }

    public long getPendingVerifications() {
        return pendingVerifications;
    }

    public void setPendingVerifications(long pendingVerifications) {
        this.pendingVerifications = pendingVerifications;
    }

    public long getAcceptedInterests() {
        return acceptedInterests;
    }

    public void setAcceptedInterests(long acceptedInterests) {
        this.acceptedInterests = acceptedInterests;
    }

    public long getOpenReports() {
        return openReports;
    }

    public void setOpenReports(long openReports) {
        this.openReports = openReports;
    }

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public Integer getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(Integer memberLimit) {
        this.memberLimit = memberLimit;
    }

    public int getQuotaUsagePercentage() {
        return quotaUsagePercentage;
    }

    public void setQuotaUsagePercentage(int quotaUsagePercentage) {
        this.quotaUsagePercentage = quotaUsagePercentage;
    }
}
