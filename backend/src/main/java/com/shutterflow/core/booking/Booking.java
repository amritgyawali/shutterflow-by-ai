package com.shutterflow.core.booking;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "studio_id = :studioId")
public class Booking {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "photographer_id", nullable = false, length = 36)
    private String photographerId;

    @Column(name = "second_shooter_id", length = 36)
    private String secondShooterId;

    @Column(name = "package_id", length = 36)
    private String packageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.INQUIRY;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_start_time")
    private LocalTime eventStartTime;

    @Column(name = "event_end_time")
    private LocalTime eventEndTime;

    @Column(name = "event_location", length = 500)
    private String eventLocation;

    @Column(name = "event_latitude", precision = 10, scale = 8)
    private BigDecimal eventLatitude;

    @Column(name = "event_longitude", precision = 11, scale = 8)
    private BigDecimal eventLongitude;

    @Column(name = "notes_internal", columnDefinition = "TEXT")
    private String notesInternal;

    @Column(name = "notes_client", columnDefinition = "TEXT")
    private String notesClient;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "travel_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal travelFee = BigDecimal.ZERO;

    @Column(name = "overtime_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal overtimeFee = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
