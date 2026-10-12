package com.tfmsp.backend.dto;

import java.time.LocalDateTime;

import com.tfmsp.backend.enums.InterestStatus;

public class InterestResponse {

    private Long id;
    private Long senderProfileId;
    private Long receiverProfileId;
    private String senderName;
    private String receiverName;
    private InterestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private String photoUrl;
    private String city;
    private String trustBand;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderProfileId() {
        return senderProfileId;
    }

    public void setSenderProfileId(Long senderProfileId) {
        this.senderProfileId = senderProfileId;
    }

    public Long getReceiverProfileId() {
        return receiverProfileId;
    }

    public void setReceiverProfileId(Long receiverProfileId) {
        this.receiverProfileId = receiverProfileId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public InterestStatus getStatus() {
        return status;
    }

    public void setStatus(InterestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTrustBand() {
        return trustBand;
    }

    public void setTrustBand(String trustBand) {
        this.trustBand = trustBand;
    }
}
