package com.tfmsp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PhotoUploadRequest {

    @NotBlank(message = "Photo URL cannot be blank")
    @Size(max = 500, message = "Photo URL cannot exceed 500 characters")
    private String photoUrl;

    private boolean primary = false;

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
