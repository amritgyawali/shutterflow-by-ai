package com.shutterflow.core.portfolio;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "public_portfolios")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicPortfolio {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(name = "photographer_id", nullable = false, length = 36)
    private String photographerId;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "custom_domain", length = 255)
    private String customDomain;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "seo_keywords", length = 500)
    private String seoKeywords;

    @Column(name = "hero_image_s3_key", length = 500)
    private String heroImageS3Key;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean isPublished = false;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String theme = "MODERN";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
