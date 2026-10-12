package com.tfmsp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class GovIdSubmissionRequest {

    @NotBlank(message = "Document file URL is required")
    @Size(max = 500, message = "File URL cannot exceed 500 characters")
    private String fileUrl;

    @Size(max = 500, message = "Back file URL cannot exceed 500 characters")
    private String fileUrlBack;

    @NotBlank(message = "Document type is required")
    private String documentType;

    /**
     * FRD VER-03: Do not persist full ID. Maximum four visible characters.
     */
    @NotBlank(message = "Masked reference is required")
    @Pattern(regexp = "^[a-zA-Z0-9]{1,4}$", message = "Masked reference must be at most 4 alphanumeric characters")
    private String maskedReference;

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileUrlBack() {
        return fileUrlBack;
    }

    public void setFileUrlBack(String fileUrlBack) {
        this.fileUrlBack = fileUrlBack;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getMaskedReference() {
        return maskedReference;
    }

    public void setMaskedReference(String maskedReference) {
        this.maskedReference = maskedReference;
    }
}
