-- V4__create_photographer_profiles_table.sql
-- Create photographer_profiles table for portfolio, bio, availability, and active status tracking

CREATE TABLE IF NOT EXISTS photographer_profiles (
    user_id VARCHAR(36) PRIMARY KEY,
    bio TEXT NULL,
    specializations VARCHAR(255) NULL,
    availability_hours TEXT NULL,
    portfolio_s3_keys TEXT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
