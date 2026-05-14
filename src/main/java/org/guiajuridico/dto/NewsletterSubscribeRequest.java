package org.guiajuridico.dto;

public class NewsletterSubscribeRequest {

    private String email;
    private Boolean acceptConsent;
    private String consentTextVersion;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getAcceptConsent() {
        return acceptConsent;
    }

    public void setAcceptConsent(Boolean acceptConsent) {
        this.acceptConsent = acceptConsent;
    }

    public String getConsentTextVersion() {
        return consentTextVersion;
    }

    public void setConsentTextVersion(String consentTextVersion) {
        this.consentTextVersion = consentTextVersion;
    }
}
