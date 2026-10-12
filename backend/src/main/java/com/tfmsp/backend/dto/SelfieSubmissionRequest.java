package com.tfmsp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SelfieSubmissionRequest {

    @NotBlank(message = "Selfie file URL is required")
    @Size(max = 500, message = "File URL cannot exceed 500 characters")
    private String fileUrl;

    private String instructionPrompt;

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getInstructionPrompt() {
        return instructionPrompt;
    }

    public void setInstructionPrompt(String instructionPrompt) {
        this.instructionPrompt = instructionPrompt;
    }
}
