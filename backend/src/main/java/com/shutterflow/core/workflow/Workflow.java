package com.shutterflow.core.workflow;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflows")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "studio_id = :studioId")
public class Workflow {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "trigger_type", nullable = false, length = 100)
    private String triggerType; // BOOKING_CREATED, CONTRACT_SIGNED, PAYMENT_RECEIVED, DATE_BASED

    @Column(name = "trigger_config", columnDefinition = "JSON")
    private String triggerConfig;

    @Column(name = "conditions_json", columnDefinition = "JSON")
    private String conditionsJson;

    @Column(name = "actions_json", nullable = false, columnDefinition = "JSON")
    private String actionsJson; // Array of actions: SEND_EMAIL, SEND_QUESTIONNAIRE, CREATE_INVOICE, CREATE_TASK

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
