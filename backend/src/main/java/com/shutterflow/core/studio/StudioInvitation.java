package com.shutterflow.core.studio;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "studio_invitations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudioInvitation {

    @Id
    @Column(length = 128)
    private String token;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean redeemed = false;
}
