package org.guiajuridico.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.guiajuridico.config.BrevoProperties;
import org.guiajuridico.config.NewsletterProperties;
import org.guiajuridico.model.NewsletterSubscription;
import org.guiajuridico.repository.NewsletterSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.security.SecureRandom;

@Service
public class NewsletterService {

    private static final Logger log = LoggerFactory.getLogger(NewsletterService.class);

    private final NewsletterSubscriptionRepository repository;
    private final BrevoContactsClient brevoContactsClient;
    private final BrevoProperties brevoProperties;
    private final NewsletterProperties newsletterProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public NewsletterService(
            NewsletterSubscriptionRepository repository,
            BrevoContactsClient brevoContactsClient,
            BrevoProperties brevoProperties,
            NewsletterProperties newsletterProperties) {
        this.repository = repository;
        this.brevoContactsClient = brevoContactsClient;
        this.brevoProperties = brevoProperties;
        this.newsletterProperties = newsletterProperties;
    }

    @Transactional
    public SubscribeOutcome subscribe(String emailRaw, boolean acceptConsent, String consentVersionOverride) {
        if (!acceptConsent) {
            throw new IllegalArgumentException("É necessário aceitar os termos para se inscrever na newsletter.");
        }
        String email = normalizeEmail(emailRaw);
        if (email == null) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        String consentVersion = consentVersionOverride != null && !consentVersionOverride.isBlank()
                ? consentVersionOverride.trim()
                : newsletterProperties.getConsentVersion();

        Optional<NewsletterSubscription> existingOpt = repository.findByEmailIgnoreCase(email);
        if (existingOpt.isPresent()) {
            NewsletterSubscription existing = existingOpt.get();
            if (NewsletterSubscription.STATUS_ACTIVE.equals(existing.getStatus())) {
                return new SubscribeOutcome(false, true, "Este e-mail já está inscrito na newsletter.");
            }
            return reactivateOrResubscribe(existing, email, consentVersion);
        }

        NewsletterSubscription row = new NewsletterSubscription();
        row.setEmail(email);
        row.setStatus(NewsletterSubscription.STATUS_ACTIVE);
        row.setUnsubscribeToken(newUnsubscribeToken());
        row.setConsentAt(Timestamp.from(Instant.now()));
        row.setConsentTextVersion(consentVersion);
        row.setUnsubscribedAt(null);

        Optional<Long> brevoId = brevoContactsClient.createOrUpdateContactOnNewsletterList(email);
        brevoId.ifPresent(row::setProviderContactId);

        repository.save(row);
        return new SubscribeOutcome(true, false, "Inscrição realizada com sucesso.");
    }

    private SubscribeOutcome reactivateOrResubscribe(NewsletterSubscription row, String email, String consentVersion) {
        row.setStatus(NewsletterSubscription.STATUS_ACTIVE);
        row.setUnsubscribeToken(newUnsubscribeToken());
        row.setConsentAt(Timestamp.from(Instant.now()));
        row.setConsentTextVersion(consentVersion);
        row.setUnsubscribedAt(null);

        Optional<Long> brevoId = brevoContactsClient.createOrUpdateContactOnNewsletterList(email);
        brevoId.ifPresent(row::setProviderContactId);

        repository.save(row);
        return new SubscribeOutcome(true, false, "Inscrição reativada com sucesso.");
    }

    @Transactional
    public boolean unsubscribeByToken(String tokenRaw) {
        if (tokenRaw == null || tokenRaw.isBlank()) {
            return false;
        }
        String token = tokenRaw.trim();
        Optional<NewsletterSubscription> opt = repository.findByUnsubscribeToken(token);
        if (opt.isEmpty()) {
            return false;
        }
        NewsletterSubscription row = opt.get();
        if (!NewsletterSubscription.STATUS_ACTIVE.equals(row.getStatus())) {
            return true;
        }
        row.setStatus(NewsletterSubscription.STATUS_UNSUBSCRIBED);
        row.setUnsubscribedAt(Timestamp.from(Instant.now()));
        repository.save(row);
        brevoContactsClient.removeEmailFromNewsletterList(row.getEmail());
        return true;
    }

    /**
     * Processa payload de webhook de marketing do Brevo (um objeto JSON por pedido).
     */
    @Transactional
    public void handleBrevoMarketingWebhook(JsonNode root) {
        if (root == null || root.isNull()) {
            return;
        }
        String event = root.path("event").asText("").trim().toLowerCase();
        String email = normalizeEmail(root.path("email").asText(""));
        if (email == null) {
            log.warn("Webhook Brevo sem e-mail válido: {}", root);
            return;
        }

        switch (event) {
            case "unsubscribe" -> handleUnsubscribeEvent(root, email);
            case "spam" -> applyNonDeliverableStatus(email, NewsletterSubscription.STATUS_COMPLAINT);
            case "hard_bounce" -> applyNonDeliverableStatus(email, NewsletterSubscription.STATUS_BOUNCED);
            default -> { /* ignorar opened, click, etc. */ }
        }
    }

    private void handleUnsubscribeEvent(JsonNode root, String email) {
        if (!affectsOurNewsletterList(root)) {
            log.debug("Ignorando unsubscribe Brevo (outra lista): {}", root);
            return;
        }
        applyNonDeliverableStatus(email, NewsletterSubscription.STATUS_UNSUBSCRIBED);
    }

    private boolean affectsOurNewsletterList(JsonNode root) {
        if (!root.has("list_id") || root.get("list_id").isNull()) {
            return true;
        }
        JsonNode listIdNode = root.get("list_id");
        if (!listIdNode.isArray()) {
            return true;
        }
        long our = brevoProperties.getNewsletterListId();
        for (JsonNode n : listIdNode) {
            if (n.canConvertToLong() && n.longValue() == our) {
                return true;
            }
        }
        return false;
    }

    private void applyNonDeliverableStatus(String email, String newStatus) {
        repository.findByEmailIgnoreCase(email).ifPresent(row -> {
            row.setStatus(newStatus);
            row.setUnsubscribedAt(Timestamp.from(Instant.now()));
            repository.save(row);
        });
    }

    private static String normalizeEmail(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim().toLowerCase();
        if (trimmed.isEmpty() || !trimmed.contains("@") || trimmed.length() > 254) {
            return null;
        }
        return trimmed;
    }

    private String newUnsubscribeToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record SubscribeOutcome(boolean subscribed, boolean alreadySubscribed, String message) {
    }
}
