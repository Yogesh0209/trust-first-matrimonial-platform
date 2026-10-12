package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.UserStatus;

import jakarta.validation.constraints.NotNull;

public class MemberStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
