package org.guiajuridico.repository;

import org.guiajuridico.model.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {

    Optional<NewsletterSubscription> findByEmailIgnoreCase(String email);

    Optional<NewsletterSubscription> findByUnsubscribeToken(String unsubscribeToken);
}
