package org.guiajuridico.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "newsletter_subscriptions")
public class NewsletterSubscription {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_UNSUBSCRIBED = "UNSUBSCRIBED";
    public static final String STATUS_COMPLAINT = "COMPLAINT";
    public static final String STATUS_BOUNCED = "BOUNCED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 32)
    private String status = STATUS_ACTIVE;

    @Column(name = "unsubscribe_token", nullable = false, unique = true, length = 128)
    private String unsubscribeToken;

    @Column(name = "consent_at", nullable = false)
    private Timestamp consentAt;

    @Column(name = "consent_text_version", nullable = false, length = 64)
    private String consentTextVersion;

    @Column(name = "unsubscribed_at")
    private Timestamp unsubscribedAt;

    @Column(name = "provider_contact_id")
    private Long providerContactId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Timestamp updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUnsubscribeToken() {
        return unsubscribeToken;
    }

    public void setUnsubscribeToken(String unsubscribeToken) {
        this.unsubscribeToken = unsubscribeToken;
    }

    public Timestamp getConsentAt() {
        return consentAt;
    }

    public void setConsentAt(Timestamp consentAt) {
        this.consentAt = consentAt;
    }

    public String getConsentTextVersion() {
        return consentTextVersion;
    }

    public void setConsentTextVersion(String consentTextVersion) {
        this.consentTextVersion = consentTextVersion;
    }

    public Timestamp getUnsubscribedAt() {
        return unsubscribedAt;
    }

    public void setUnsubscribedAt(Timestamp unsubscribedAt) {
        this.unsubscribedAt = unsubscribedAt;
    }

    public Long getProviderContactId() {
        return providerContactId;
    }

    public void setProviderContactId(Long providerContactId) {
        this.providerContactId = providerContactId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
