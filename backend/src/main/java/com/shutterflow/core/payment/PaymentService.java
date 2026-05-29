package com.shutterflow.core.payment;

import com.shutterflow.core.client.event.PaymentCompletedEvent;
import com.shutterflow.core.common.AppException;
import com.shutterflow.core.invoice.Invoice;
import com.shutterflow.core.invoice.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final StripeWebhookLogRepository webhookLogRepository;
    private final InvoiceService invoiceService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Create a payment intent placeholder record before Stripe processing.
     */
    @Transactional
    public PaymentTransaction createPaymentIntent(String studioId, String invoiceId, String clientId, BigDecimal amount, String currency) {
        Invoice invoice = invoiceService.getInvoice(invoiceId);

        if (amount.compareTo(invoice.getAmountDue()) > 0) {
            throw new AppException("Payment amount exceeds invoice balance due", HttpStatus.BAD_REQUEST);
        }

        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .invoiceId(invoiceId)
                .clientId(clientId)
                .amount(amount)
                .currency(currency != null ? currency : "AUD")
                .status("PENDING")
                .build();

        return transactionRepository.save(transaction);
    }

    /**
     * Process a successful payment (called from webhook handler).
     */
    @Transactional
    public PaymentTransaction processSuccessfulPayment(String stripePaymentIntentId, String stripeChargeId) {
        PaymentTransaction transaction = transactionRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new AppException("Payment transaction not found for intent: " + stripePaymentIntentId, HttpStatus.NOT_FOUND));

        transaction.setStatus("SUCCEEDED");
        transaction.setStripeChargeId(stripeChargeId);
        transactionRepository.save(transaction);

        // Update invoice balance
        invoiceService.recordPayment(transaction.getInvoiceId(), transaction.getAmount());

        // Publish event for spend aggregation
        eventPublisher.publishEvent(new PaymentCompletedEvent(
                this, transaction.getClientId(), transaction.getAmount()));

        log.info("Payment {} processed successfully for invoice {}", transaction.getId(), transaction.getInvoiceId());
        return transaction;
    }

    /**
     * Process a refund.
     */
    @Transactional
    public PaymentTransaction processRefund(String transactionId, BigDecimal refundAmount) {
        PaymentTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException("Payment transaction not found", HttpStatus.NOT_FOUND));

        if (!"SUCCEEDED".equals(transaction.getStatus())) {
            throw new AppException("Can only refund successful payments", HttpStatus.BAD_REQUEST);
        }

        if (refundAmount.compareTo(transaction.getAmount()) > 0) {
            throw new AppException("Refund amount exceeds original payment", HttpStatus.BAD_REQUEST);
        }

        transaction.setRefundAmount(refundAmount);
        transaction.setStatus("REFUNDED");
        return transactionRepository.save(transaction);
    }

    /**
     * Log a webhook event for auditing.
     */
    public void logWebhookEvent(String eventId, String eventType, String payload) {
        if (webhookLogRepository.findByEventId(eventId).isPresent()) {
            log.warn("Duplicate webhook event received: {}", eventId);
            return;
        }

        StripeWebhookLog webhookLog = StripeWebhookLog.builder()
                .id(UUID.randomUUID().toString())
                .eventId(eventId)
                .eventType(eventType)
                .payload(payload)
                .processed(false)
                .build();
        webhookLogRepository.save(webhookLog);
    }

    public List<PaymentTransaction> getInvoicePayments(String invoiceId) {
        return transactionRepository.findByInvoiceId(invoiceId);
    }

    public List<PaymentTransaction> getStudioPayments(String studioId) {
        return transactionRepository.findByStudioId(studioId);
    }
}
