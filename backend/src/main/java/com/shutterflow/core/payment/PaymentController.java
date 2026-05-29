package com.shutterflow.core.payment;

import com.shutterflow.core.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Data
    public static class CreatePaymentIntentRequest {
        @NotBlank
        private String invoiceId;
        @NotBlank
        private String clientId;
        @NotNull
        private BigDecimal amount;
        private String currency;
    }

    @Data
    public static class RefundRequest {
        @NotNull
        private BigDecimal amount;
    }

    @PostMapping("/create-intent")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<PaymentTransaction>> createPaymentIntent(
            @PathVariable String studioId,
            @Valid @RequestBody CreatePaymentIntentRequest request) {

        PaymentTransaction transaction = paymentService.createPaymentIntent(
                studioId, request.getInvoiceId(), request.getClientId(), request.getAmount(), request.getCurrency());

        return ResponseEntity.ok(ApiResponse.success(transaction, "Payment intent created"));
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<PaymentTransaction>>> getPayments(@PathVariable String studioId) {
        List<PaymentTransaction> payments = paymentService.getStudioPayments(studioId);
        return ResponseEntity.ok(ApiResponse.success(payments, "Fetched payments"));
    }

    @PostMapping("/{transactionId}/refund")
    @PreAuthorize("hasRole('STUDIO_OWNER') and @tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<PaymentTransaction>> refundPayment(
            @PathVariable String studioId,
            @PathVariable String transactionId,
            @Valid @RequestBody RefundRequest request) {

        PaymentTransaction refunded = paymentService.processRefund(transactionId, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(refunded, "Refund processed successfully"));
    }
}
