package com.tfmsp.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "shortlists")
public class Shortlist {

    @EmbeddedId
    private ShortlistId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Shortlist() {
    }

    public Shortlist(Long profileId, Long shortlistedProfileId) {
        this.id = new ShortlistId(profileId, shortlistedProfileId);
        this.createdAt = LocalDateTime.now();
    }

    public ShortlistId getId() {
        return id;
    }

    public void setId(ShortlistId id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
