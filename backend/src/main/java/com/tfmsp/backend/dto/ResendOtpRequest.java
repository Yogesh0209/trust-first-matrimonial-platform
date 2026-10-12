package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.OtpPurpose;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResendOtpRequest {

    @NotBlank(message = "Identifier is required")
    private String identifier;

    @NotNull(message = "Purpose is required")
    private OtpPurpose purpose;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public OtpPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(OtpPurpose purpose) {
        this.purpose = purpose;
    }
}
