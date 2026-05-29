-- V13__add_user_auth_columns.sql
-- Sprint 2 Day 1: Add authentication-related columns to users table

ALTER TABLE users
    ADD COLUMN full_name VARCHAR(200) NULL AFTER email,
    ADD COLUMN phone VARCHAR(50) NULL AFTER full_name,
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE AFTER studio_id,
    ADD COLUMN account_non_locked BOOLEAN NOT NULL DEFAULT TRUE AFTER enabled,
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE AFTER account_non_locked,
    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0 AFTER email_verified,
    ADD COLUMN lock_expires_at TIMESTAMP NULL AFTER failed_login_attempts,
    ADD COLUMN last_login_at TIMESTAMP NULL AFTER lock_expires_at;

-- Index for efficient auth lookups
CREATE INDEX idx_user_studio_role ON users (studio_id, role);
CREATE INDEX idx_user_enabled ON users (enabled);
