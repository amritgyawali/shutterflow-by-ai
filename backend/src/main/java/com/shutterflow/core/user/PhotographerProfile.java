package com.shutterflow.core.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "photographer_profiles")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotographerProfile {

    @Id
    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 255)
    private String specializations;

    @Column(name = "availability_hours", columnDefinition = "TEXT")
    private String availabilityHours; // JSON format

    @Column(name = "portfolio_s3_keys", columnDefinition = "TEXT")
    private String portfolioS3Keys; // Comma separated or JSON format

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, ON_LEAVE, INACTIVE

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private User user;
}
