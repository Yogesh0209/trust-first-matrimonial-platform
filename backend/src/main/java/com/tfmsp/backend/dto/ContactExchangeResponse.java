package com.tfmsp.backend.dto;

public class ContactExchangeResponse {

    private boolean consentGranted;
    private boolean mutualConsent;
    private String contactPhone;
    private String contactEmail;
    private String message;

    public boolean isConsentGranted() {
        return consentGranted;
    }

    public void setConsentGranted(boolean consentGranted) {
        this.consentGranted = consentGranted;
    }

    public boolean isMutualConsent() {
        return mutualConsent;
    }

    public void setMutualConsent(boolean mutualConsent) {
        this.mutualConsent = mutualConsent;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
