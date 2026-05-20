package org.guiajuridico.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "brevo")
public class BrevoProperties {

    private String apiKey = "";
    private long newsletterListId = 5L;
    private String webhookSecret = "";
    /** Remetente transacional (deve estar verificado em Brevo → Senders). */
    private String senderEmail = "";
    private String senderName = "Guia Jurídico";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey != null ? apiKey : "";
    }

    public long getNewsletterListId() {
        return newsletterListId;
    }

    public void setNewsletterListId(long newsletterListId) {
        this.newsletterListId = newsletterListId;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret != null ? webhookSecret : "";
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    public boolean hasWebhookSecret() {
        return webhookSecret != null && !webhookSecret.isBlank();
    }

    public String getSenderEmail() {
        return senderEmail == null ? "" : senderEmail.trim();
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderName() {
        return senderName == null || senderName.isBlank() ? "Guia Jurídico" : senderName.trim();
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public boolean canSendTransactionalEmail() {
        return hasApiKey() && !getSenderEmail().isBlank();
    }
}
