package com.tfmsp.backend.dto;

import jakarta.validation.constraints.NotNull;

public class SharingUpdateRequest {

    @NotNull(message = "Consent is required (true or false)")
    private Boolean consent;

    public Boolean getConsent() {
        return consent;
    }

    public void setConsent(Boolean consent) {
        this.consent = consent;
    }
}