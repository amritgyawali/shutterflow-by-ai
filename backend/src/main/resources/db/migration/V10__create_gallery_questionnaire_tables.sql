-- V10__create_gallery_questionnaire_tables.sql
-- Sprint 11-12: Gallery & Photo Delivery + Questionnaires & Forms

-- Galleries
CREATE TABLE IF NOT EXISTS galleries (
    id VARCHAR(36) PRIMARY KEY,
    studio_id VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36) NULL,
    client_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    cover_photo_s3_key VARCHAR(500) NULL,
    password VARCHAR(255) NULL,
    expires_at TIMESTAMP NULL,
    watermark_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    watermark_text VARCHAR(255) NULL,
    watermark_s3_key VARCHAR(500) NULL,
    download_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    download_limit INT NULL,
    share_token VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_gallery_share_token (share_token),
    INDEX idx_gallery_studio (studio_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Gallery photos
CREATE TABLE IF NOT EXISTS gallery_photos (
    id VARCHAR(36) PRIMARY KEY,
    gallery_id VARCHAR(36) NOT NULL,
    original_s3_key VARCHAR(500) NOT NULL,
    thumbnail_s3_key VARCHAR(500) NULL,
    watermarked_s3_key VARCHAR(500) NULL,
    filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    width INT NULL,
    height INT NULL,
    content_type VARCHAR(100) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    download_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gallery_id) REFERENCES galleries(id) ON DELETE CASCADE,
    INDEX idx_photo_gallery (gallery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Photo comments
CREATE TABLE IF NOT EXISTS photo_comments (
    id VARCHAR(36) PRIMARY KEY,
    photo_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NULL,
    user_id VARCHAR(36) NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (photo_id) REFERENCES gallery_photos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Questionnaire templates
CREATE TABLE IF NOT EXISTS questionnaire_templates (
    id VARCHAR(36) PRIMARY KEY,
    studio_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    event_type VARCHAR(100) NULL,
    schema_json JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Questionnaire instances (sent to clients)
CREATE TABLE IF NOT EXISTS questionnaires (
    id VARCHAR(36) PRIMARY KEY,
    studio_id VARCHAR(36) NOT NULL,
    template_id VARCHAR(36) NULL,
    booking_id VARCHAR(36) NULL,
    client_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    responses_json JSON NULL,
    submitted_at TIMESTAMP NULL,
    due_date DATE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES questionnaire_templates(id) ON DELETE SET NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    INDEX idx_questionnaire_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
