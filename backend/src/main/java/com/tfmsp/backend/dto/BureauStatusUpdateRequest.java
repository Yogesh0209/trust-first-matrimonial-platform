package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.BureauStatus;

import jakarta.validation.constraints.NotNull;

public class BureauStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private BureauStatus status;

    private String reason;

    public BureauStatus getStatus() {
        return status;
    }

    public void setStatus(BureauStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
