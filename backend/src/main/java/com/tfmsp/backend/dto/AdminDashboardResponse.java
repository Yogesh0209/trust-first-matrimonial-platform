package com.tfmsp.backend.dto;

import java.util.Map;

public class AdminDashboardResponse {

    private long totalBureaus;
    private long activeBureaus;
    private long pendingBureaus;
    private long totalPlatformUsers;
    private long totalVerificationsReviewed;
    private long openReports;
    private Map<String, Long> planDistribution;

    public long getTotalBureaus() {
        return totalBureaus;
    }

    public void setTotalBureaus(long totalBureaus) {
        this.totalBureaus = totalBureaus;
    }

    public long getActiveBureaus() {
        return activeBureaus;
    }

    public void setActiveBureaus(long activeBureaus) {
        this.activeBureaus = activeBureaus;
    }

    public long getPendingBureaus() {
        return pendingBureaus;
    }

    public void setPendingBureaus(long pendingBureaus) {
        this.pendingBureaus = pendingBureaus;
    }

    public long getTotalPlatformUsers() {
        return totalPlatformUsers;
    }

    public void setTotalPlatformUsers(long totalPlatformUsers) {
        this.totalPlatformUsers = totalPlatformUsers;
    }

    public long getTotalVerificationsReviewed() {
        return totalVerificationsReviewed;
    }

    public void setTotalVerificationsReviewed(long totalVerificationsReviewed) {
        this.totalVerificationsReviewed = totalVerificationsReviewed;
    }

    public long getOpenReports() {
        return openReports;
    }

    public void setOpenReports(long openReports) {
        this.openReports = openReports;
    }

    public Map<String, Long> getPlanDistribution() {
        return planDistribution;
    }

    public void setPlanDistribution(Map<String, Long> planDistribution) {
        this.planDistribution = planDistribution;
    }
}
