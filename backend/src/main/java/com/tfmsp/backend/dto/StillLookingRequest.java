package com.tfmsp.backend.dto;

import jakarta.validation.constraints.NotNull;

public class StillLookingRequest {

    public enum Choice { ACTIVE, ENGAGED, PAUSED }

    @NotNull(message = "status is required")
    private Choice status;

    public Choice getStatus() { return status; }
    public void setStatus(Choice status) { this.status = status; }
}