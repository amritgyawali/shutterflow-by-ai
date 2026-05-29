-- V12__create_public_portfolio_tables.sql
-- Sprint 19: Public Website Builder & Landing Page

-- Public portfolios
CREATE TABLE IF NOT EXISTS public_portfolios (
    id VARCHAR(36) PRIMARY KEY,
    studio_id VARCHAR(36) NOT NULL,
    photographer_id VARCHAR(36) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    custom_domain VARCHAR(255) NULL,
    title VARCHAR(255) NOT NULL,
    tagline VARCHAR(500) NULL,
    bio TEXT NULL,
    seo_title VARCHAR(255) NULL,
    seo_description VARCHAR(500) NULL,
    seo_keywords VARCHAR(500) NULL,
    hero_image_s3_key VARCHAR(500) NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    theme VARCHAR(50) NOT NULL DEFAULT 'MODERN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE,
    FOREIGN KEY (photographer_id) REFERENCES users(id),
    UNIQUE INDEX idx_portfolio_slug (slug),
    UNIQUE INDEX idx_portfolio_domain (custom_domain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Portfolio images
CREATE TABLE IF NOT EXISTS portfolio_images (
    id VARCHAR(36) PRIMARY KEY,
    portfolio_id VARCHAR(36) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    thumbnail_s3_key VARCHAR(500) NULL,
    caption VARCHAR(500) NULL,
    category VARCHAR(100) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES public_portfolios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Lead capture (from public contact forms)
CREATE TABLE IF NOT EXISTS leads (
    id VARCHAR(36) PRIMARY KEY,
    studio_id VARCHAR(36) NOT NULL,
    photographer_id VARCHAR(36) NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NULL,
    event_type VARCHAR(100) NULL,
    event_date DATE NULL,
    message TEXT NULL,
    source VARCHAR(100) NOT NULL DEFAULT 'WEBSITE',
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    converted_client_id VARCHAR(36) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE,
    INDEX idx_lead_studio_status (studio_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Page analytics
CREATE TABLE IF NOT EXISTS page_analytics (
    id VARCHAR(36) PRIMARY KEY,
    portfolio_id VARCHAR(36) NOT NULL,
    visitor_ip VARCHAR(45) NULL,
    user_agent TEXT NULL,
    page_path VARCHAR(500) NULL,
    referrer VARCHAR(500) NULL,
    event_type VARCHAR(50) NOT NULL DEFAULT 'PAGE_VIEW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES public_portfolios(id) ON DELETE CASCADE,
    INDEX idx_analytics_portfolio_date (portfolio_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
