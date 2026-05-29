package com.shutterflow.core.calendar;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_entries")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "studio_id = :studioId")
public class CalendarEntry {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "studio_id", nullable = false, length = 36)
    private String studioId;

    @Column(name = "photographer_id", nullable = false, length = 36)
    private String photographerId;

    @Column(name = "booking_id", length = 36)
    private String bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    @Builder.Default
    private CalendarEntryType entryType = CalendarEntryType.BOOKING;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    @Column(name = "all_day", nullable = false)
    @Builder.Default
    private boolean allDay = false;

    @Column(length = 500)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "google_event_id", length = 255)
    private String googleEventId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
