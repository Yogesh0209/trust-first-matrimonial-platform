package com.tfmsp.backend.dto;

import java.time.LocalDateTime;

public class ProfilePhotoResponse {

    private Long id;
    private String photoUrl;
    private boolean primary;
    private LocalDateTime uploadedAt;

    public ProfilePhotoResponse() {
    }

    public ProfilePhotoResponse(Long id, String photoUrl, boolean primary, LocalDateTime uploadedAt) {
        this.id = id;
        this.photoUrl = photoUrl;
        this.primary = primary;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
