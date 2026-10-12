package com.tfmsp.backend.dto;

import java.time.LocalDateTime;

import com.tfmsp.backend.enums.ReviewStatus;
import com.tfmsp.backend.enums.VerificationType;

public class VerificationResponse {

    private Long id;
    private Long profileId;
    private VerificationType type;
    private String fileUrl;
    private String fileUrlBack;
    private ReviewStatus status;
    private String rejectReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    public VerificationResponse() {
    }

    public VerificationResponse(Long id, Long profileId, VerificationType type, String fileUrl,
                                String fileUrlBack, ReviewStatus status, String rejectReason,
                                LocalDateTime submittedAt, LocalDateTime reviewedAt) {
        this.id = id;
        this.profileId = profileId;
        this.type = type;
        this.fileUrl = fileUrl;
        this.fileUrlBack = fileUrlBack;
        this.status = status;
        this.rejectReason = rejectReason;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public VerificationType getType() {
        return type;
    }

    public void setType(VerificationType type) {
        this.type = type;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileUrlBack() {
        return fileUrlBack;
    }

    public void setFileUrlBack(String fileUrlBack) {
        this.fileUrlBack = fileUrlBack;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
