package com.shutterflow.core.pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, String> {
    List<Package> findByStudioId(String studioId);
    List<Package> findByStudioIdAndIsPublic(String studioId, boolean isPublic);
}
