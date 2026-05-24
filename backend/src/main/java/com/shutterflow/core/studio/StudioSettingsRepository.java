package com.shutterflow.core.studio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudioSettingsRepository extends JpaRepository<StudioSettings, String> {
}
