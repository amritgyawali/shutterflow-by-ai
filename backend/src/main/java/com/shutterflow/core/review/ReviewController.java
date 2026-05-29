package com.shutterflow.core.review;

import com.shutterflow.core.common.ApiResponse;
import com.shutterflow.core.common.AppException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;

    @Data
    public static class RequestReviewRequest {
        @NotBlank
        private String bookingId;
        @NotBlank
        private String clientId;
        @NotBlank
        private String photographerId;
    }

    @Data
    public static class SubmitReviewRequest {
        @NotNull
        @Min(1)
        @Max(5)
        private Integer rating;
        private String reviewText;
    }

    @Data
    public static class RespondRequest {
        @NotBlank
        private String response;
    }

    @Data
    @Builder
    public static class ReviewStats {
        private Double averageRating;
        private long totalReviews;
    }

    @PostMapping("/request")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Review>> requestReview(
            @PathVariable String studioId,
            @Valid @RequestBody RequestReviewRequest request) {

        Review review = Review.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .bookingId(request.getBookingId())
                .clientId(request.getClientId())
                .photographerId(request.getPhotographerId())
                .status("REQUESTED")
                .requestedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(ApiResponse.success(reviewRepository.save(review), "Review requested"));
    }

    @PostMapping("/{reviewId}/submit")
    public ResponseEntity<ApiResponse<Review>> submitReview(
            @PathVariable String studioId,
            @PathVariable String reviewId,
            @Valid @RequestBody SubmitReviewRequest request) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException("Review not found", HttpStatus.NOT_FOUND));

        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review.setStatus("PENDING");
        review.setSubmittedAt(LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(reviewRepository.save(review), "Review submitted"));
    }

    @PostMapping("/{reviewId}/approve")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Review>> approveReview(
            @PathVariable String studioId,
            @PathVariable String reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException("Review not found", HttpStatus.NOT_FOUND));

        review.setStatus("APPROVED");
        review.setApprovedAt(LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(reviewRepository.save(review), "Review approved"));
    }

    @PostMapping("/{reviewId}/respond")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Review>> respondToReview(
            @PathVariable String studioId,
            @PathVariable String reviewId,
            @Valid @RequestBody RespondRequest request) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException("Review not found", HttpStatus.NOT_FOUND));

        review.setPhotographerResponse(request.getResponse());

        return ResponseEntity.ok(ApiResponse.success(reviewRepository.save(review), "Response posted"));
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Review>>> getReviews(@PathVariable String studioId) {
        return ResponseEntity.ok(ApiResponse.success(reviewRepository.findByStudioId(studioId), "Fetched reviews"));
    }

    @GetMapping("/photographer/{photographerId}/stats")
    public ResponseEntity<ApiResponse<ReviewStats>> getPhotographerStats(
            @PathVariable String studioId,
            @PathVariable String photographerId) {

        Double avgRating = reviewRepository.getAverageRatingForPhotographer(photographerId);
        long count = reviewRepository.getReviewCountForPhotographer(photographerId);

        ReviewStats stats = ReviewStats.builder()
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(count)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats, "Fetched review stats"));
    }

    @GetMapping("/photographer/{photographerId}/public")
    public ResponseEntity<ApiResponse<List<Review>>> getPublicReviews(
            @PathVariable String studioId,
            @PathVariable String photographerId) {
        List<Review> reviews = reviewRepository.findByPhotographerIdAndStatus(photographerId, "APPROVED");
        return ResponseEntity.ok(ApiResponse.success(reviews, "Fetched public reviews"));
    }
}
