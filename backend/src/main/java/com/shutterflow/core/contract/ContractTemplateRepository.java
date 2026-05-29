package com.shutterflow.core.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, String> {
    List<ContractTemplate> findByStudioId(String studioId);
    List<ContractTemplate> findByStudioIdAndEventType(String studioId, String eventType);
}
