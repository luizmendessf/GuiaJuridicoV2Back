package org.guiajuridico.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** URL pública do site (links em e-mails transacionais). */
    private String frontendUrl = "https://guiajuridico.org";

    public String getFrontendUrl() {
        return frontendUrl == null ? "" : frontendUrl.trim().replaceAll("/+$", "");
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }
}
