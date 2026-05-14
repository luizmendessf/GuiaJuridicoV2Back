package org.guiajuridico.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.guiajuridico.config.BrevoProperties;
import org.guiajuridico.service.NewsletterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/webhooks")
public class BrevoWebhookController {

    private static final Logger log = LoggerFactory.getLogger(BrevoWebhookController.class);

    private final NewsletterService newsletterService;
    private final BrevoProperties brevoProperties;

    public BrevoWebhookController(NewsletterService newsletterService, BrevoProperties brevoProperties) {
        this.newsletterService = newsletterService;
        this.brevoProperties = brevoProperties;
    }

    @PostMapping("/brevo")
    public ResponseEntity<Map<String, String>> handle(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody JsonNode body) {
        if (!brevoProperties.hasWebhookSecret()) {
            log.warn("BREVO_WEBHOOK_SECRET não configurado: webhook rejeitado.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Webhook não configurado no servidor."));
        }
        if (!isValidBearer(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autorizado."));
        }
        try {
            if (body != null && body.isArray()) {
                for (JsonNode node : body) {
                    newsletterService.handleBrevoMarketingWebhook(node);
                }
            } else {
                newsletterService.handleBrevoMarketingWebhook(body);
            }
        } catch (Exception e) {
            log.error("Erro ao processar webhook Brevo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Falha ao processar evento."));
        }
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private boolean isValidBearer(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return false;
        }
        String token = authorization.substring(7).trim();
        return brevoProperties.getWebhookSecret().equals(token);
    }
}
