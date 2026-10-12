package com.tfmsp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class InterestRespondRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(ACCEPT|DECLINE)$", message = "Action must be either ACCEPT or DECLINE")
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
