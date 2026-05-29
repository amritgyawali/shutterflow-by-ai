package com.shutterflow.core.invoice;

import com.shutterflow.core.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Data
    public static class LineItemRequest {
        @NotBlank
        private String description;
        @NotNull
        private Integer quantity;
        @NotNull
        private BigDecimal unitPrice;
    }

    @Data
    public static class CreateInvoiceRequest {
        @NotBlank
        private String clientId;
        private String bookingId;
        @NotNull
        private List<LineItemRequest> lineItems;
        private BigDecimal taxRate;
        private String currency;
    }

    @Data
    public static class IssueInvoiceRequest {
        @NotNull
        private LocalDate dueDate;
    }

    @PostMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Invoice>> createInvoice(
            @PathVariable String studioId,
            @Valid @RequestBody CreateInvoiceRequest request) {

        List<InvoiceLineItem> lineItems = request.getLineItems().stream()
                .map(li -> InvoiceLineItem.builder()
                        .id(UUID.randomUUID().toString())
                        .description(li.getDescription())
                        .quantity(li.getQuantity())
                        .unitPrice(li.getUnitPrice())
                        .total(li.getUnitPrice().multiply(BigDecimal.valueOf(li.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        BigDecimal taxRate = request.getTaxRate() != null ? request.getTaxRate() : new BigDecimal("10.00");

        Invoice invoice = invoiceService.createInvoice(
                studioId, request.getClientId(), request.getBookingId(), lineItems, taxRate, request.getCurrency());

        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice created successfully"));
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Invoice>>> getInvoices(@PathVariable String studioId) {
        List<Invoice> invoices = invoiceService.getStudioInvoices(studioId);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Fetched invoices"));
    }

    @GetMapping("/{invoiceId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Invoice>> getInvoice(
            @PathVariable String studioId,
            @PathVariable String invoiceId) {
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Fetched invoice"));
    }

    @PostMapping("/{invoiceId}/issue")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Invoice>> issueInvoice(
            @PathVariable String studioId,
            @PathVariable String invoiceId,
            @Valid @RequestBody IssueInvoiceRequest request) {
        Invoice issued = invoiceService.issueInvoice(invoiceId, request.getDueDate());
        return ResponseEntity.ok(ApiResponse.success(issued, "Invoice issued successfully"));
    }
}
