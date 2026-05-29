package com.shutterflow.core.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, String> {
    List<WorkflowExecution> findByWorkflowIdOrderByStartedAtDesc(String workflowId);
    List<WorkflowExecution> findByStudioIdOrderByStartedAtDesc(String studioId);
}
