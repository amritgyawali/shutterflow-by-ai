package com.shutterflow.core.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, String> {
    List<Lead> findByStudioIdOrderByCreatedAtDesc(String studioId);
    List<Lead> findByStudioIdAndStatus(String studioId, String status);
}
