package com.shutterflow.core.contract;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "studio_id = :studioId")
public class Contract {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(name = "booking_id", length = 36)
    private String bookingId;

    @Column(name = "template_id", length = 36)
    private String templateId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "compiled_html", nullable = false, columnDefinition = "TEXT")
    private String compiledHtml;

    @Column(name = "client_signature_data", columnDefinition = "TEXT")
    private String clientSignatureData;

    @Column(name = "client_signed_at")
    private LocalDateTime clientSignedAt;

    @Column(name = "client_signed_ip", length = 45)
    private String clientSignedIp;

    @Column(name = "photographer_signature_data", columnDefinition = "TEXT")
    private String photographerSignatureData;

    @Column(name = "photographer_signed_at")
    private LocalDateTime photographerSignedAt;

    @Column(name = "photographer_signed_ip", length = 45)
    private String photographerSignedIp;

    @Column(name = "pdf_s3_key", length = 500)
    private String pdfS3Key;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
