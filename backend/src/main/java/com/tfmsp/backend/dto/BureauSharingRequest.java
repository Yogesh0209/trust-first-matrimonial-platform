package com.tfmsp.backend.dto;

import jakarta.validation.constraints.NotNull;

public class BureauSharingRequest {

    @NotNull(message = "shareMembers is required")
    private Boolean shareMembers;

    @NotNull(message = "searchOtherBureaus is required")
    private Boolean searchOtherBureaus;

    private Boolean showBureauName;

    public Boolean getShareMembers() { return shareMembers; }
    public void setShareMembers(Boolean shareMembers) { this.shareMembers = shareMembers; }

    public Boolean getSearchOtherBureaus() { return searchOtherBureaus; }
    public void setSearchOtherBureaus(Boolean searchOtherBureaus) { this.searchOtherBureaus = searchOtherBureaus; }

    public Boolean getShowBureauName() { return showBureauName; }
    public void setShowBureauName(Boolean showBureauName) { this.showBureauName = showBureauName; }
    
}