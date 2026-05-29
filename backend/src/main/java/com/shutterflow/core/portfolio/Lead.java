package com.shutterflow.core.portfolio;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Lead {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(name = "photographer_id", length = 36)
    private String photographerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String source = "WEBSITE";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "NEW";

    @Column(name = "converted_client_id", length = 36)
    private String convertedClientId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
