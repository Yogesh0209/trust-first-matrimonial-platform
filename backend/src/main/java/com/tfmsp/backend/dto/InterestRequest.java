package com.tfmsp.backend.dto;

import jakarta.validation.constraints.NotNull;

public class InterestRequest {

    @NotNull(message = "Receiver profile ID is required")
    private Long receiverProfileId;

    public Long getReceiverProfileId() {
        return receiverProfileId;
    }

    public void setReceiverProfileId(Long receiverProfileId) {
        this.receiverProfileId = receiverProfileId;
    }
}
