package com.tfmsp.backend.dto;

import java.time.LocalDateTime;

public class BureauResponse {

    private Long id;
    private String name;
    private String bureauCode;
    private String city;
    private String status;
    private boolean shareMembers;
    private boolean searchOtherBureaus;
    private boolean showBureauName;
    private long memberCount;
    private String activePlan;
    private LocalDateTime createdAt;

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

    public long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(long memberCount) {
        this.memberCount = memberCount;
    }

    public String getActivePlan() {
        return activePlan;
    }

    public void setActivePlan(String activePlan) {
        this.activePlan = activePlan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
