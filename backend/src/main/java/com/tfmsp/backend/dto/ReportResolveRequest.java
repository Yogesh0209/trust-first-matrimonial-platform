package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.ReportStatus;

import jakarta.validation.constraints.NotNull;

public class ReportResolveRequest {

    @NotNull(message = "Resolution status is required")
    private ReportStatus status;

    private String notes;

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
