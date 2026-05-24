package com.shutterflow.core.studio;

import com.shutterflow.core.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/settings")
@RequiredArgsConstructor
public class StudioSettingsController {

    private final StudioSettingsRepository settingsRepository;

    /**
     * Get settings for a studio.
     * Only users belonging to this studio OR an ADMIN can access it.
     */
    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<StudioSettings>> getSettings(@PathVariable String studioId) {
        StudioSettings settings = settingsRepository.findByStudioId(studioId)
                .orElseThrow(() -> new RuntimeException("Settings not found"));
        return ResponseEntity.ok(ApiResponse.success(settings, "Fetched settings successfully"));
    }

    /**
     * Update settings. Only STUDIO_OWNER of this specific studio can perform this.
     */
    @PutMapping
    @PreAuthorize("hasRole('STUDIO_OWNER') and @tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<StudioSettings>> updateSettings(
            @PathVariable String studioId,
            @RequestBody StudioSettings updateRequest) {
        
        // Validate currency
        String currency = updateRequest.getCurrency();
        if (currency != null) {
            java.util.List<String> allowedCurrencies = java.util.List.of("AUD", "USD", "EUR", "GBP", "NPR");
            if (!allowedCurrencies.contains(currency.toUpperCase())) {
                throw new com.shutterflow.core.common.AppException(
                        "Invalid currency code. Supported currencies: AUD, USD, EUR, GBP, NPR", 
                        org.springframework.http.HttpStatus.BAD_REQUEST
                );
            }
            updateRequest.setCurrency(currency.toUpperCase());
        }

        // Validate tax rate
        java.math.BigDecimal taxRate = updateRequest.getTaxRate();
        if (taxRate != null) {
            if (taxRate.compareTo(java.math.BigDecimal.ZERO) < 0 || taxRate.compareTo(new java.math.BigDecimal("100.00")) > 0) {
                throw new com.shutterflow.core.common.AppException(
                        "Tax rate must be between 0.00% and 100.00%", 
                        org.springframework.http.HttpStatus.BAD_REQUEST
                );
            }
        }

        StudioSettings settings = settingsRepository.findByStudioId(studioId)
                .orElseGet(() -> {
                    StudioSettings s = new StudioSettings();
                    s.setStudioId(studioId);
                    return s;
                });

        if (currency != null) {
            settings.setCurrency(updateRequest.getCurrency());
        }
        if (taxRate != null) {
            settings.setTaxRate(updateRequest.getTaxRate());
        }
        if (updateRequest.getBankDetails() != null) {
            settings.setBankDetails(updateRequest.getBankDetails());
        }
        if (updateRequest.getPrimaryColor() != null) {
            settings.setPrimaryColor(updateRequest.getPrimaryColor());
        }
        if (updateRequest.getSecondaryColor() != null) {
            settings.setSecondaryColor(updateRequest.getSecondaryColor());
        }

        // Validate splits config
        if (updateRequest.getCommissionSplitPercentage() != null) {
            java.math.BigDecimal splitPct = updateRequest.getCommissionSplitPercentage();
            if (splitPct.compareTo(java.math.BigDecimal.ZERO) < 0 || splitPct.compareTo(new java.math.BigDecimal("100.00")) > 0) {
                throw new com.shutterflow.core.common.AppException(
                        "Commission split percentage must be between 0.00% and 100.00%", 
                        org.springframework.http.HttpStatus.BAD_REQUEST
                );
            }
            settings.setCommissionSplitPercentage(splitPct);
        }

        if (updateRequest.getCommissionFlatFee() != null) {
            java.math.BigDecimal flatFee = updateRequest.getCommissionFlatFee();
            if (flatFee.compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new com.shutterflow.core.common.AppException(
                        "Commission flat fee must be non-negative", 
                        org.springframework.http.HttpStatus.BAD_REQUEST
                );
            }
            settings.setCommissionFlatFee(flatFee);
        }

        StudioSettings saved = settingsRepository.save(settings);
        return ResponseEntity.ok(ApiResponse.success(saved, "Settings updated successfully"));
    }
}
