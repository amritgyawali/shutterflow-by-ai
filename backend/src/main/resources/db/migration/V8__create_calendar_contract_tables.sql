-- V8__create_calendar_contract_tables.sql
-- Sprint 7-8: Calendar & Scheduling + Contracts & E-Signature

-- Calendar entries
CREATE TABLE IF NOT EXISTS calendar_entries (
    id VARCHAR(36) PRIMARY KEY,
    studio_id VARCHAR(36) NOT NULL,
    photographer_id VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36) NULL,
    entry_type VARCHAR(50) NOT NULL DEFAULT 'BOOKING',
    title VARCHAR(255) NOT NULL,
    start_datetime DATETIME NOT NULL,
    end_datetime DATETIME NOT NULL,
    all_day BOOLEAN NOT NULL DEFAULT FALSE,
    location VARCHAR(500) NULL,
    notes TEXT NULL,
    google_event_id VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE,
    FOREIGN KEY (photographer_id) REFERENCES users(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    INDEX idx_calendar_photographer_dates (photographer_id, start_datetime, end_datetime),
    INDEX idx_calendar_studio (studio_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Contract templates
CREATE TABLE IF NOT EXISTS contract_templates (
    id VARCHAR(36) PRIMARY KEY,
    studio_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    body_html TEXT NOT NULL,
    event_type VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Contracts (instances sent to clients)
CREATE TABLE IF NOT EXISTS contracts (
    id VARCHAR(36) PRIMARY KEY,
    studio_id VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36) NULL,
    template_id VARCHAR(36) NULL,
    client_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    compiled_html TEXT NOT NULL,
    client_signature_data TEXT NULL,
    client_signed_at TIMESTAMP NULL,
    client_signed_ip VARCHAR(45) NULL,
    photographer_signature_data TEXT NULL,
    photographer_signed_at TIMESTAMP NULL,
    photographer_signed_ip VARCHAR(45) NULL,
    pdf_s3_key VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    FOREIGN KEY (template_id) REFERENCES contract_templates(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    INDEX idx_contract_status (status),
    INDEX idx_contract_booking (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
