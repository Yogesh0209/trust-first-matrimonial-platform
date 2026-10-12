package com.tfmsp.backend.dto;

import java.time.LocalDateTime;

public class ConversationResponse {

    private Long interestId;
    private Long otherProfileId;
    private Long otherUserId;
    private String otherFullName;
    private String otherPhotoUrl;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private String trustBand;
    private int trustScore;

    public Long getInterestId() {
        return interestId;
    }

    public void setInterestId(Long interestId) {
        this.interestId = interestId;
    }

    public Long getOtherProfileId() {
        return otherProfileId;
    }

    public void setOtherProfileId(Long otherProfileId) {
        this.otherProfileId = otherProfileId;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherFullName() {
        return otherFullName;
    }

    public void setOtherFullName(String otherFullName) {
        this.otherFullName = otherFullName;
    }

    public String getOtherPhotoUrl() {
        return otherPhotoUrl;
    }

    public void setOtherPhotoUrl(String otherPhotoUrl) {
        this.otherPhotoUrl = otherPhotoUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getTrustBand() {
        return trustBand;
    }

    public void setTrustBand(String trustBand) {
        this.trustBand = trustBand;
    }

    public int getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(int trustScore) {
        this.trustScore = trustScore;
    }
}
