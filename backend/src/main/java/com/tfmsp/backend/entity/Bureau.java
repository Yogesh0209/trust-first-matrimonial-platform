package com.tfmsp.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "bureaus")
public class Bureau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "bureau_code", unique = true, length = 20)
    private String bureauCode;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, ACTIVE, SUSPENDED

    @Column(name = "share_members", nullable = false)
    private boolean shareMembers = false;

    @Column(name = "search_other_bureaus", nullable = false)
    private boolean searchOtherBureaus = false;

    @Column(name = "show_bureau_name", nullable = false)
    private boolean showBureauName = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBureauCode() {
        return bureauCode;
    }

    public void setBureauCode(String bureauCode) {
        this.bureauCode = bureauCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isShareMembers() {
        return shareMembers;
    }

    public void setShareMembers(boolean shareMembers) {
        this.shareMembers = shareMembers;
    }

    public boolean isSearchOtherBureaus() {
        return searchOtherBureaus;
    }

    public void setSearchOtherBureaus(boolean searchOtherBureaus) {
        this.searchOtherBureaus = searchOtherBureaus;
    }

    public boolean isShowBureauName() {
        return showBureauName;
    }

    public void setShowBureauName(boolean showBureauName) {
        this.showBureauName = showBureauName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
