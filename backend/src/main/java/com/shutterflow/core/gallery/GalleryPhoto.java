package com.shutterflow.core.gallery;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gallery_photos")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GalleryPhoto {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "gallery_id", nullable = false, length = 36)
    private String galleryId;

    @Column(name = "original_s3_key", nullable = false, length = 500)
    private String originalS3Key;

    @Column(name = "thumbnail_s3_key", length = 500)
    private String thumbnailS3Key;

    @Column(name = "watermarked_s3_key", length = 500)
    private String watermarkedS3Key;

    @Column(nullable = false)
    private String filename;

    @Column(name = "file_size", nullable = false)
    @Builder.Default
    private long fileSize = 0;

    private Integer width;
    private Integer height;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private boolean isFavorite = false;

    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private int downloadCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
