package com.shutterflow.core.studio;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "studios")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Studio {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String subdomain;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_tier", nullable = false)
    @Builder.Default
    private PlanTier planTier = PlanTier.STARTER;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "studio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudioSettings settings;
}
