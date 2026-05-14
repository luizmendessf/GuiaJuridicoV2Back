-- Inscrições na newsletter (LGPD + integração Brevo).
-- Rode uma vez no MySQL do ambiente (local/produção).

-- status: ACTIVE | UNSUBSCRIBED | COMPLAINT (spam) | BOUNCED (hard bounce)

CREATE TABLE IF NOT EXISTS newsletter_subscriptions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  unsubscribe_token VARCHAR(128) NOT NULL,
  consent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  consent_text_version VARCHAR(64) NOT NULL,
  unsubscribed_at TIMESTAMP NULL DEFAULT NULL,
  provider_contact_id BIGINT UNSIGNED NULL DEFAULT NULL,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_newsletter_subscriptions_email (email),
  UNIQUE KEY uk_newsletter_subscriptions_unsubscribe_token (unsubscribe_token),
  KEY idx_newsletter_subscriptions_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
