package com.tfmsp.backend.dto;

import java.time.LocalDateTime;

import com.tfmsp.backend.enums.ReportReason;
import com.tfmsp.backend.enums.ReportStatus;

public class ReportResponse {

    private Long id;
    private Long reporterProfileId;
    private String reporterName;
    private Long reportedProfileId;
    private String reportedName;
    private ReportReason reason;
    private String details;
    private ReportStatus status;
    private Long handledBy;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReporterProfileId() {
        return reporterProfileId;
    }

    public void setReporterProfileId(Long reporterProfileId) {
        this.reporterProfileId = reporterProfileId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public Long getReportedProfileId() {
        return reportedProfileId;
    }

    public void setReportedProfileId(Long reportedProfileId) {
        this.reportedProfileId = reportedProfileId;
    }

    public String getReportedName() {
        return reportedName;
    }

    public void setReportedName(String reportedName) {
        this.reportedName = reportedName;
    }

    public ReportReason getReason() {
        return reason;
    }

    public void setReason(ReportReason reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Long getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(Long handledBy) {
        this.handledBy = handledBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
