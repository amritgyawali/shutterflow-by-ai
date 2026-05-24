package com.shutterflow.core.studio;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "studio_settings")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudioSettings {

    @Id
    @Column(name = "studio_id", length = 36)
    private String studioId;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "AUD";

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = new BigDecimal("10.00");

    @Column(name = "bank_details", columnDefinition = "TEXT")
    private String bankDetails;

    @Column(name = "primary_color", nullable = false, length = 20)
    @Builder.Default
    private String primaryColor = "#1f2937";

    @Column(name = "secondary_color", nullable = false, length = 20)
    @Builder.Default
    private String secondaryColor = "#10b981";

    @Column(name = "commission_split_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal commissionSplitPercentage = new BigDecimal("70.00");

    @Column(name = "commission_flat_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal commissionFlatFee = new BigDecimal("0.00");

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private Studio studio;
}
