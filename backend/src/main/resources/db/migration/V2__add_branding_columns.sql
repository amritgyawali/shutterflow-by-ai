-- V2__add_branding_columns.sql
-- Add branding details (logo_s3_key, primary_color, secondary_color, custom_font) to studios table

ALTER TABLE studios
ADD COLUMN logo_s3_key VARCHAR(255) NULL,
ADD COLUMN primary_color VARCHAR(20) NULL DEFAULT '#1f2937',
ADD COLUMN secondary_color VARCHAR(20) NULL DEFAULT '#10b981',
ADD COLUMN custom_font VARCHAR(100) NULL DEFAULT 'Outfit';
