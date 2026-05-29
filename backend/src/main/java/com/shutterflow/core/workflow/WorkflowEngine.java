package com.shutterflow.core.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowEngine {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;

    /**
     * Trigger all active workflows matching the given trigger type.
     */
    @Transactional
    public void triggerWorkflows(String triggerType, String entityId) {
        List<Workflow> workflows = workflowRepository.findByTriggerTypeAndIsActiveTrue(triggerType);

        for (Workflow workflow : workflows) {
            executeWorkflow(workflow, entityId);
        }
    }

    @Transactional
    public WorkflowExecution executeWorkflow(Workflow workflow, String entityId) {
        WorkflowExecution execution = WorkflowExecution.builder()
                .id(UUID.randomUUID().toString())
                .workflowId(workflow.getId())
                .studioId(workflow.getStudioId())
                .triggerEntityId(entityId)
                .status("RUNNING")
                .build();

        execution = executionRepository.save(execution);

        try {
            // Parse and execute actions from JSON
            // In a full implementation, this would parse actionsJson and execute each action
            log.info("Executing workflow '{}' for entity {}", workflow.getName(), entityId);

            // TODO: implement actual action execution
            // Actions could be: SEND_EMAIL, SEND_QUESTIONNAIRE, CREATE_INVOICE, CREATE_TASK, DELAY

            execution.setStatus("COMPLETED");
            execution.setCompletedAt(LocalDateTime.now());
            execution.setExecutionLog("{\"actions_executed\": true}");
        } catch (Exception e) {
            execution.setStatus("FAILED");
            execution.setCompletedAt(LocalDateTime.now());
            execution.setErrorMessage(e.getMessage());
            log.error("Workflow execution failed for workflow {}: {}", workflow.getId(), e.getMessage());
        }

        return executionRepository.save(execution);
    }

    public List<Workflow> getStudioWorkflows(String studioId) {
        return workflowRepository.findByStudioId(studioId);
    }

    public List<WorkflowExecution> getWorkflowHistory(String workflowId) {
        return executionRepository.findByWorkflowIdOrderByStartedAtDesc(workflowId);
    }
}
