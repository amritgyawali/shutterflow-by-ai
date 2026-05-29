package com.shutterflow.core.invoice;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invoice_sequences")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceSequence {

    @Id
    @Column(name = "studio_id", length = 36)
    private String studioId;

    @Column(name = "last_number", nullable = false)
    @Builder.Default
    private int lastNumber = 0;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String prefix = "INV";
}
