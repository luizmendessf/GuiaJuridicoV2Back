package org.guiajuridico.controller;

import org.guiajuridico.dto.NewsletterSubscribeRequest;
import org.guiajuridico.dto.NewsletterUnsubscribeRequest;
import org.guiajuridico.service.NewsletterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/newsletter")
public class PublicNewsletterController {

    private final NewsletterService newsletterService;

    public PublicNewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody NewsletterSubscribeRequest request) {
        try {
            NewsletterService.SubscribeOutcome outcome = newsletterService.subscribe(
                    request.getEmail(),
                    Boolean.TRUE.equals(request.getAcceptConsent()),
                    request.getConsentTextVersion());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("message", outcome.message());
            body.put("alreadySubscribed", outcome.alreadySubscribed());
            body.put("subscribed", outcome.subscribed());
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribePost(@RequestBody NewsletterUnsubscribeRequest request) {
        String token = request != null ? request.getToken() : null;
        return buildUnsubscribeResponse(token);
    }

    @GetMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribeGet(@RequestParam("token") String token) {
        return buildUnsubscribeResponse(token);
    }

    private ResponseEntity<Map<String, String>> buildUnsubscribeResponse(String token) {
        boolean ok = newsletterService.unsubscribeByToken(token);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Token inválido ou inscrição não encontrada."));
        }
        return ResponseEntity.ok(Map.of("message", "Inscrição cancelada com sucesso."));
    }
}
