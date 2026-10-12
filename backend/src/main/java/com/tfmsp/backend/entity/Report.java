package com.tfmsp.backend.entity;

import java.time.LocalDateTime;

import com.tfmsp.backend.enums.ReportReason;
import com.tfmsp.backend.enums.ReportStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_profile_id", nullable = false)
    private Long reporterProfileId;

    @Column(name = "reported_profile_id", nullable = false)
    private Long reportedProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(length = 500)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.OPEN;

    @Column(name = "handled_by")
    private Long handledBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public Report() {
    }

    public Report(Long reporterProfileId, Long reportedProfileId, ReportReason reason, String details) {
        this.reporterProfileId = reporterProfileId;
        this.reportedProfileId = reportedProfileId;
        this.reason = reason;
        this.details = details;
        this.status = ReportStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

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

    public Long getReportedProfileId() {
        return reportedProfileId;
    }

    public void setReportedProfileId(Long reportedProfileId) {
        this.reportedProfileId = reportedProfileId;
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
