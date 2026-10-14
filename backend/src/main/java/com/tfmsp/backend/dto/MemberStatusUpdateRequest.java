package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.UserStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MemberStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;

    @Size(max = 500, message = "Reason must be at most 500 characters")
    private String reason;

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}