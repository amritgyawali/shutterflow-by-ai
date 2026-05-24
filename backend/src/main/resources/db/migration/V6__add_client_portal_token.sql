-- V6__add_client_portal_token.sql
-- Add portal_token and tags columns to clients table for password-less portal login and tagging

ALTER TABLE clients
ADD COLUMN portal_token VARCHAR(36) NULL UNIQUE,
ADD COLUMN tags VARCHAR(255) NULL;
