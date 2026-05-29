package com.shutterflow.core.portfolio;

import com.shutterflow.core.common.ApiResponse;
import com.shutterflow.core.common.AppException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Authenticated endpoints for studio owners to manage their portfolio and leads.
 */
@RestController
@RequestMapping("/api/v1/studios/{studioId}/portfolio")
@RequiredArgsConstructor
public class PortfolioManagementController {

    private final PublicPortfolioRepository portfolioRepository;
    private final LeadRepository leadRepository;

    @Data
    public static class CreatePortfolioRequest {
        @NotBlank
        private String slug;
        @NotBlank
        private String title;
        @NotBlank
        private String photographerId;
        private String tagline;
        private String bio;
        private String seoTitle;
        private String seoDescription;
        private String theme;
    }

    @Data
    public static class UpdatePortfolioRequest {
        private String title;
        private String tagline;
        private String bio;
        private String seoTitle;
        private String seoDescription;
        private String seoKeywords;
        private String theme;
        private String heroImageS3Key;
        private String customDomain;
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<PublicPortfolio>>> listPortfolios(@PathVariable String studioId) {
        return ResponseEntity.ok(ApiResponse.success(
                portfolioRepository.findByStudioId(studioId), "Fetched portfolios"));
    }

    @PostMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<PublicPortfolio>> createPortfolio(
            @PathVariable String studioId,
            @Valid @RequestBody CreatePortfolioRequest request) {

        if (portfolioRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new AppException("Slug already taken", HttpStatus.CONFLICT);
        }

        PublicPortfolio portfolio = PublicPortfolio.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .photographerId(request.getPhotographerId())
                .slug(request.getSlug())
                .title(request.getTitle())
                .tagline(request.getTagline())
                .bio(request.getBio())
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .theme(request.getTheme() != null ? request.getTheme() : "MODERN")
                .build();

        return ResponseEntity.ok(ApiResponse.success(portfolioRepository.save(portfolio), "Portfolio created"));
    }

    @PutMapping("/{portfolioId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<PublicPortfolio>> updatePortfolio(
            @PathVariable String studioId,
            @PathVariable String portfolioId,
            @RequestBody UpdatePortfolioRequest request) {

        PublicPortfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new AppException("Portfolio not found", HttpStatus.NOT_FOUND));

        if (request.getTitle() != null) portfolio.setTitle(request.getTitle());
        if (request.getTagline() != null) portfolio.setTagline(request.getTagline());
        if (request.getBio() != null) portfolio.setBio(request.getBio());
        if (request.getSeoTitle() != null) portfolio.setSeoTitle(request.getSeoTitle());
        if (request.getSeoDescription() != null) portfolio.setSeoDescription(request.getSeoDescription());
        if (request.getSeoKeywords() != null) portfolio.setSeoKeywords(request.getSeoKeywords());
        if (request.getTheme() != null) portfolio.setTheme(request.getTheme());
        if (request.getHeroImageS3Key() != null) portfolio.setHeroImageS3Key(request.getHeroImageS3Key());
        if (request.getCustomDomain() != null) portfolio.setCustomDomain(request.getCustomDomain());

        return ResponseEntity.ok(ApiResponse.success(portfolioRepository.save(portfolio), "Portfolio updated"));
    }

    @PostMapping("/{portfolioId}/publish")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<PublicPortfolio>> publishPortfolio(
            @PathVariable String studioId,
            @PathVariable String portfolioId) {

        PublicPortfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new AppException("Portfolio not found", HttpStatus.NOT_FOUND));

        portfolio.setPublished(true);
        return ResponseEntity.ok(ApiResponse.success(portfolioRepository.save(portfolio), "Portfolio published"));
    }

    @GetMapping("/leads")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Lead>>> listLeads(@PathVariable String studioId) {
        return ResponseEntity.ok(ApiResponse.success(
                leadRepository.findByStudioIdOrderByCreatedAtDesc(studioId), "Fetched leads"));
    }

    @PutMapping("/leads/{leadId}/status")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Lead>> updateLeadStatus(
            @PathVariable String studioId,
            @PathVariable String leadId,
            @RequestParam String status) {

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new AppException("Lead not found", HttpStatus.NOT_FOUND));

        lead.setStatus(status);
        return ResponseEntity.ok(ApiResponse.success(leadRepository.save(lead), "Lead status updated"));
    }
}
