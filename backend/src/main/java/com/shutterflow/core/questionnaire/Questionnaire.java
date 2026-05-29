package com.shutterflow.core.questionnaire;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "questionnaires")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "studio_id = :studioId")
public class Questionnaire {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(name = "template_id", length = 36)
    private String templateId;

    @Column(name = "booking_id", length = 36)
    private String bookingId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "responses_json", columnDefinition = "JSON")
    private String responsesJson;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
