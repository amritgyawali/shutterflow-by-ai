package com.shutterflow.core.portfolio;

import com.shutterflow.core.common.ApiResponse;
import com.shutterflow.core.common.AppException;
import com.shutterflow.core.review.Review;
import com.shutterflow.core.review.ReviewRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Public endpoints - no authentication required.
 * These serve the photographer's public portfolio pages.
 */
@RestController
@RequestMapping("/api/v1/public/portfolio")
@RequiredArgsConstructor
public class PublicPortfolioController {

    private final PublicPortfolioRepository portfolioRepository;
    private final LeadRepository leadRepository;
    private final ReviewRepository reviewRepository;

    @Data
    public static class ContactFormRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String email;
        private String phone;
        private String eventType;
        private LocalDate eventDate;
        private String message;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PublicPortfolio>> getPortfolio(@PathVariable String slug) {
        PublicPortfolio portfolio = portfolioRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException("Portfolio not found", HttpStatus.NOT_FOUND));

        if (!portfolio.isPublished()) {
            throw new AppException("Portfolio is not published", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(ApiResponse.success(portfolio, "Fetched portfolio"));
    }

    @GetMapping("/{slug}/reviews")
    public ResponseEntity<ApiResponse<List<Review>>> getPortfolioReviews(@PathVariable String slug) {
        PublicPortfolio portfolio = portfolioRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException("Portfolio not found", HttpStatus.NOT_FOUND));

        List<Review> reviews = reviewRepository.findByPhotographerIdAndStatus(
                portfolio.getPhotographerId(), "APPROVED");

        return ResponseEntity.ok(ApiResponse.success(reviews, "Fetched reviews"));
    }

    @PostMapping("/{slug}/contact")
    public ResponseEntity<ApiResponse<Lead>> submitContactForm(
            @PathVariable String slug,
            @Valid @RequestBody ContactFormRequest request) {

        PublicPortfolio portfolio = portfolioRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException("Portfolio not found", HttpStatus.NOT_FOUND));

        Lead lead = Lead.builder()
                .id(UUID.randomUUID().toString())
                .studioId(portfolio.getStudioId())
                .photographerId(portfolio.getPhotographerId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .message(request.getMessage())
                .source("PORTFOLIO")
                .status("NEW")
                .build();

        return ResponseEntity.ok(ApiResponse.success(leadRepository.save(lead), "Contact form submitted"));
    }
}
