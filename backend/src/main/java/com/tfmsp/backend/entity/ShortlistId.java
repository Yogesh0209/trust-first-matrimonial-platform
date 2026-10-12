package com.tfmsp.backend.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ShortlistId implements Serializable {

    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "shortlisted_profile_id")
    private Long shortlistedProfileId;

    public ShortlistId() {
    }

    public ShortlistId(Long profileId, Long shortlistedProfileId) {
        this.profileId = profileId;
        this.shortlistedProfileId = shortlistedProfileId;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getShortlistedProfileId() {
        return shortlistedProfileId;
    }

    public void setShortlistedProfileId(Long shortlistedProfileId) {
        this.shortlistedProfileId = shortlistedProfileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortlistId that = (ShortlistId) o;
        return Objects.equals(profileId, that.profileId) &&
               Objects.equals(shortlistedProfileId, that.shortlistedProfileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, shortlistedProfileId);
    }
}
