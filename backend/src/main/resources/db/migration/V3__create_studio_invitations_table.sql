-- V3__create_studio_invitations_table.sql
-- Create studio_invitations table for secure team onboarding

CREATE TABLE IF NOT EXISTS studio_invitations (
    token VARCHAR(128) PRIMARY KEY,
    studio_id VARCHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'PHOTOGRAPHER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    redeemed BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
