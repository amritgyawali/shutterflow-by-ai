package com.shutterflow.core.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublicPortfolioRepository extends JpaRepository<PublicPortfolio, String> {
    Optional<PublicPortfolio> findBySlug(String slug);
    Optional<PublicPortfolio> findByCustomDomain(String customDomain);
    List<PublicPortfolio> findByStudioId(String studioId);
    Optional<PublicPortfolio> findByPhotographerId(String photographerId);
}
