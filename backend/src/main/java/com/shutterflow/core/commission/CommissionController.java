package com.shutterflow.core.commission;

import com.shutterflow.core.common.ApiResponse;
import com.shutterflow.core.common.AppException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/commissions")
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionRepository commissionRepository;

    @Data
    public static class CreateCommissionRequest {
        @NotBlank
        private String photographerId;
        @NotBlank
        private String bookingId;
        private String invoiceId;
        @NotNull
        private BigDecimal bookingTotal;
        @NotNull
        private BigDecimal commissionRate; // e.g., 0.15 for 15%
        private String notes;
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Commission>>> listCommissions(@PathVariable String studioId) {
        return ResponseEntity.ok(ApiResponse.success(
                commissionRepository.findByStudioId(studioId), "Fetched commissions"));
    }

    @GetMapping("/photographer/{photographerId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPhotographerCommissions(
            @PathVariable String studioId,
            @PathVariable String photographerId) {

        List<Commission> commissions = commissionRepository.findByPhotographerId(photographerId);
        BigDecimal pending = commissionRepository.getTotalPendingCommission(photographerId);
        BigDecimal paid = commissionRepository.getTotalPaidCommission(photographerId);

        Map<String, Object> summary = Map.of(
                "commissions", commissions,
                "totalPending", pending != null ? pending : BigDecimal.ZERO,
                "totalPaid", paid != null ? paid : BigDecimal.ZERO
        );

        return ResponseEntity.ok(ApiResponse.success(summary, "Fetched photographer commissions"));
    }

    @PostMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Commission>> createCommission(
            @PathVariable String studioId,
            @Valid @RequestBody CreateCommissionRequest request) {

        BigDecimal commissionAmount = request.getBookingTotal()
                .multiply(request.getCommissionRate())
                .setScale(2, RoundingMode.HALF_UP);

        Commission commission = Commission.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .photographerId(request.getPhotographerId())
                .bookingId(request.getBookingId())
                .invoiceId(request.getInvoiceId())
                .bookingTotal(request.getBookingTotal())
                .commissionRate(request.getCommissionRate())
                .commissionAmount(commissionAmount)
                .status("PENDING")
                .notes(request.getNotes())
                .build();

        return ResponseEntity.ok(ApiResponse.success(
                commissionRepository.save(commission), "Commission created"));
    }

    @PostMapping("/{commissionId}/pay")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Commission>> markAsPaid(
            @PathVariable String studioId,
            @PathVariable String commissionId) {

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new AppException("Commission not found", HttpStatus.NOT_FOUND));

        commission.setStatus("PAID");
        commission.setPaidAt(LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(
                commissionRepository.save(commission), "Commission marked as paid"));
    }
}
