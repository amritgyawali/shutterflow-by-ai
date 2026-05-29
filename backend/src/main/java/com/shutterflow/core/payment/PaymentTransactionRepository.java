package com.shutterflow.core.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {

    List<PaymentTransaction> findByInvoiceId(String invoiceId);

    List<PaymentTransaction> findByClientId(String clientId);

    Optional<PaymentTransaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<PaymentTransaction> findByStudioId(String studioId);
}
