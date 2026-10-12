package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.Visibility;

import jakarta.validation.constraints.NotNull;

public class PrivacyUpdateRequest {

    @NotNull(message = "Visibility setting is required")
    private Visibility visibility;

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}
