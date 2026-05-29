package com.shutterflow.core.workflow;

import com.shutterflow.core.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowRepository workflowRepository;
    private final WorkflowEngine workflowEngine;

    @Data
    public static class CreateWorkflowRequest {
        @NotBlank
        private String name;
        private String description;
        @NotBlank
        private String triggerType;
        private String triggerConfig;
        private String conditionsJson;
        @NotBlank
        private String actionsJson;
    }

    @PostMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Workflow>> createWorkflow(
            @PathVariable String studioId,
            @Valid @RequestBody CreateWorkflowRequest request) {

        Workflow workflow = Workflow.builder()
                .id(UUID.randomUUID().toString())
                .studioId(studioId)
                .name(request.getName())
                .description(request.getDescription())
                .triggerType(request.getTriggerType())
                .triggerConfig(request.getTriggerConfig())
                .conditionsJson(request.getConditionsJson())
                .actionsJson(request.getActionsJson())
                .isActive(true)
                .build();

        return ResponseEntity.ok(ApiResponse.success(workflowRepository.save(workflow), "Workflow created"));
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Workflow>>> getWorkflows(@PathVariable String studioId) {
        return ResponseEntity.ok(ApiResponse.success(workflowEngine.getStudioWorkflows(studioId), "Fetched workflows"));
    }

    @PatchMapping("/{workflowId}/toggle")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Workflow>> toggleWorkflow(
            @PathVariable String studioId,
            @PathVariable String workflowId) {

        Workflow workflow = workflowRepository.findById(workflowId).orElse(null);
        if (workflow != null) {
            workflow.setActive(!workflow.isActive());
            workflowRepository.save(workflow);
        }
        return ResponseEntity.ok(ApiResponse.success(workflow, "Workflow toggled"));
    }

    @GetMapping("/{workflowId}/history")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<WorkflowExecution>>> getHistory(
            @PathVariable String studioId,
            @PathVariable String workflowId) {
        return ResponseEntity.ok(ApiResponse.success(workflowEngine.getWorkflowHistory(workflowId), "Fetched history"));
    }
}
