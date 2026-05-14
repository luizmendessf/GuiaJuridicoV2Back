package org.guiajuridico.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "brevo")
public class BrevoProperties {

    private String apiKey = "";
    private long newsletterListId = 5L;
    private String webhookSecret = "";

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
}
