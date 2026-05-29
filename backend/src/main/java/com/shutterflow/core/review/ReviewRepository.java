package com.shutterflow.core.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByStudioId(String studioId);

    List<Review> findByPhotographerIdAndStatus(String photographerId, String status);

    Optional<Review> findByBookingId(String bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.photographerId = :photographerId AND r.status = 'APPROVED'")
    Double getAverageRatingForPhotographer(String photographerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.photographerId = :photographerId AND r.status = 'APPROVED'")
    long getReviewCountForPhotographer(String photographerId);

    List<Review> findByPhotographerIdAndStatusAndIsFeaturedTrue(String photographerId, String status);
}
