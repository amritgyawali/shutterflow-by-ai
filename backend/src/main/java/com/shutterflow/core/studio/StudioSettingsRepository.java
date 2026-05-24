package com.shutterflow.core.studio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudioSettingsRepository extends JpaRepository<StudioSettings, String> {
    Optional<StudioSettings> findByStudioId(String studioId);
}
