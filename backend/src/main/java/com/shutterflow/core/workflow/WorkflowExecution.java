package com.shutterflow.core.workflow;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_executions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowExecution {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "workflow_id", nullable = false, length = 36)
    private String workflowId;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(name = "trigger_entity_id", length = 36)
    private String triggerEntityId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "RUNNING";

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_log", columnDefinition = "JSON")
    private String executionLog;
}
