-- V5__add_commission_settings.sql
-- Add commission splitting rules (split_percentage, flat_fee) to studio_settings table

ALTER TABLE studio_settings
ADD COLUMN commission_split_percentage DECIMAL(5, 2) NOT NULL DEFAULT 70.00,
ADD COLUMN commission_flat_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
