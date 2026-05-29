package com.shutterflow.core.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, String> {
    List<Workflow> findByStudioId(String studioId);
    List<Workflow> findByTriggerTypeAndIsActiveTrue(String triggerType);
    List<Workflow> findByStudioIdAndIsActiveTrue(String studioId);
}
