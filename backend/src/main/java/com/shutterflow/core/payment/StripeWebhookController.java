package com.shutterflow.core.payment;

import com.shutterflow.core.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public webhook endpoint for Stripe event notifications.
 * This endpoint does NOT require authentication (Stripe sends webhooks externally).
 */
@RestController
@RequestMapping("/api/v1/public/payments")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/stripe-webhook")
    public ResponseEntity<ApiResponse<Void>> handleStripeWebhook(
            @RequestHeader(value = "Stripe-Signature", required = false) String stripeSignature,
            @RequestBody String payload) {

        // In production: verify Stripe-Signature header against webhook secret
        // For now, log the event and process it
        log.info("Received Stripe webhook event");

        try {
            // Parse the event type from payload (simplified - in production use Stripe SDK)
            // This is a placeholder for proper Stripe event deserialization
            String eventId = extractFieldFromJson(payload, "id");
            String eventType = extractFieldFromJson(payload, "type");

            if (eventId == null || eventType == null) {
                log.warn("Invalid webhook payload - missing id or type");
                return ResponseEntity.badRequest().body(ApiResponse.success(null, "Invalid payload"));
            }

            // Log the event for audit
            paymentService.logWebhookEvent(eventId, eventType, payload);

            // Process based on event type
            switch (eventType) {
                case "payment_intent.succeeded":
                    String intentId = extractNestedField(payload, "payment_intent");
                    String chargeId = extractNestedField(payload, "charge");
                    if (intentId != null) {
                        paymentService.processSuccessfulPayment(intentId, chargeId);
                    }
                    break;
                case "charge.refunded":
                    log.info("Charge refunded event received");
                    break;
                default:
                    log.info("Unhandled webhook event type: {}", eventType);
            }

            return ResponseEntity.ok(ApiResponse.success(null, "Webhook processed"));
        } catch (Exception e) {
            log.error("Error processing Stripe webhook", e);
            return ResponseEntity.ok(ApiResponse.success(null, "Webhook acknowledged with error"));
        }
    }

    private String extractFieldFromJson(String json, String field) {
        // Simple JSON field extraction - in production use Jackson ObjectMapper
        String searchKey = "\"" + field + "\":\"";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1) return null;
        startIdx += searchKey.length();
        int endIdx = json.indexOf("\"", startIdx);
        if (endIdx == -1) return null;
        return json.substring(startIdx, endIdx);
    }

    private String extractNestedField(String json, String field) {
        return extractFieldFromJson(json, field);
    }
}
