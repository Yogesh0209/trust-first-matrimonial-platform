package com.tfmsp.backend.entity;

import java.time.LocalDateTime;

import com.tfmsp.backend.enums.InterestStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "interests")
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_profile_id", nullable = false)
    private Long senderProfileId;

    @Column(name = "receiver_profile_id", nullable = false)
    private Long receiverProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestStatus status = InterestStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public Interest() {
    }

    public Interest(Long senderProfileId, Long receiverProfileId) {
        this.senderProfileId = senderProfileId;
        this.receiverProfileId = receiverProfileId;
        this.status = InterestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

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
}
