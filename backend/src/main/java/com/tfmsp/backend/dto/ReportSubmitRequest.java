package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.ReportReason;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReportSubmitRequest {

    @NotNull(message = "Reported profile ID is required")
    private Long reportedProfileId;

    @NotNull(message = "Reason is required")
    private ReportReason reason;

    @Size(max = 500, message = "Details cannot exceed 500 characters")
    private String details;

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
}
