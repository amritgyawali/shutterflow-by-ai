package com.shutterflow.core.gallery;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "galleries")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "studio_id = :studioId")
public class Gallery {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(name = "booking_id", length = 36)
    private String bookingId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_photo_s3_key", length = 500)
    private String coverPhotoS3Key;

    @Column(length = 255)
    private String password;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "watermark_enabled", nullable = false)
    @Builder.Default
    private boolean watermarkEnabled = true;

    @Column(name = "watermark_text", length = 255)
    private String watermarkText;

    @Column(name = "watermark_s3_key", length = 500)
    private String watermarkS3Key;

    @Column(name = "download_enabled", nullable = false)
    @Builder.Default
    private boolean downloadEnabled = true;

    @Column(name = "download_limit")
    private Integer downloadLimit;

    @Column(name = "share_token", nullable = false, unique = true, length = 100)
    private String shareToken;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "galleryId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<GalleryPhoto> photos = new ArrayList<>();
}
