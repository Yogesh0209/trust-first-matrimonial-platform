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
    
    @jakarta.validation.constraints.Size(max = 200, message = "Note must be at most 200 characters") 
    private String note;

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
