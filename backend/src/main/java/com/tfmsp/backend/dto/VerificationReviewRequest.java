package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.ReviewStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VerificationReviewRequest {

    @NotNull(message = "Review decision is required")
    private ReviewStatus decision; // APPROVED or REJECTED

    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;

    public ReviewStatus getDecision() {
        return decision;
    }

    public void setDecision(ReviewStatus decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
