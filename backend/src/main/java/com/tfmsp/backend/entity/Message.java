package com.tfmsp.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interest_id", nullable = false)
    private Long interestId;

    @Column(name = "sender_profile_id", nullable = false)
    private Long senderProfileId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_flagged", nullable = false)
    private boolean flagged = false;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public Message() {
    }

    public Message(Long interestId, Long senderProfileId, String body, boolean flagged) {
        this.interestId = interestId;
        this.senderProfileId = senderProfileId;
        this.body = body;
        this.flagged = flagged;
        this.sentAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInterestId() {
        return interestId;
    }

    public void setInterestId(Long interestId) {
        this.interestId = interestId;
    }

    public Long getSenderProfileId() {
        return senderProfileId;
    }

    public void setSenderProfileId(Long senderProfileId) {
        this.senderProfileId = senderProfileId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
