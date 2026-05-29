-- V7__create_booking_tables.sql
-- Sprint 5-6: Packages add-ons + Complete Booking System

-- Package Add-ons table
CREATE TABLE IF NOT EXISTS package_addons (
    id VARCHAR(36) PRIMARY KEY,
    package_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL,
    addon_type VARCHAR(50) NOT NULL DEFAULT 'FIXED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seasonal pricing rules
CREATE TABLE IF NOT EXISTS seasonal_pricing (
    id VARCHAR(36) PRIMARY KEY,
    package_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    multiplier DECIMAL(4, 2) NOT NULL DEFAULT 1.00,
    FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add hours and deliverables columns to packages
ALTER TABLE packages ADD COLUMN hours DECIMAL(5, 2) NULL DEFAULT 0;
ALTER TABLE packages ADD COLUMN deliverables TEXT NULL;
ALTER TABLE packages ADD COLUMN currency VARCHAR(10) NOT NULL DEFAULT 'AUD';

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id VARCHAR(36) PRIMARY KEY,
    studio_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    photographer_id VARCHAR(36) NOT NULL,
    second_shooter_id VARCHAR(36) NULL,
    package_id VARCHAR(36) NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'INQUIRY',
    event_type VARCHAR(100) NULL,
    event_date DATE NOT NULL,
    event_start_time TIME NULL,
    event_end_time TIME NULL,
    event_location VARCHAR(500) NULL,
    event_latitude DECIMAL(10, 8) NULL,
    event_longitude DECIMAL(11, 8) NULL,
    notes_internal TEXT NULL,
    notes_client TEXT NULL,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    travel_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    overtime_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (photographer_id) REFERENCES users(id),
    FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE SET NULL,
    INDEX idx_booking_studio_date (studio_id, event_date),
    INDEX idx_booking_photographer_date (photographer_id, event_date),
    INDEX idx_booking_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Booking timeline/audit log
CREATE TABLE IF NOT EXISTS booking_timeline (
    id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL,
    previous_status VARCHAR(50) NULL,
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(36) NULL,
    note TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
