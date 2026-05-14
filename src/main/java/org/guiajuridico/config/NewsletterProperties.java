package org.guiajuridico.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "newsletter")
public class NewsletterProperties {

    /** Versão do texto de consentimento (LGPD) aceite no formulário. */
    private String consentVersion = "v1";

    public String getConsentVersion() {
        return consentVersion;
    }

    public void setConsentVersion(String consentVersion) {
        this.consentVersion = consentVersion != null ? consentVersion : "v1";
    }
}
