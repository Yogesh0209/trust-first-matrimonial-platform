package com.tfmsp.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "blocks")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_profile_id", nullable = false)
    private Long blockerProfileId;

    @Column(name = "blocked_profile_id", nullable = false)
    private Long blockedProfileId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Block() {
    }

    public Block(Long blockerProfileId, Long blockedProfileId) {
        this.blockerProfileId = blockerProfileId;
        this.blockedProfileId = blockedProfileId;
    }

    public Long getId() { return id; }
    public Long getBlockerProfileId() { return blockerProfileId; }
    public Long getBlockedProfileId() { return blockedProfileId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}