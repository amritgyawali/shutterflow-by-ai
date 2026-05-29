package com.shutterflow.core.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripeWebhookLogRepository extends JpaRepository<StripeWebhookLog, String> {
    Optional<StripeWebhookLog> findByEventId(String eventId);
}
