package com.shutterflow.core.calendar;

import com.shutterflow.core.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @Data
    public static class CreateCalendarEntryRequest {
        @NotBlank
        private String photographerId;
        private String bookingId;
        @NotNull
        private CalendarEntryType entryType;
        @NotBlank
        private String title;
        @NotNull
        private LocalDateTime startDatetime;
        @NotNull
        private LocalDateTime endDatetime;
        private boolean allDay;
        private String location;
        private String notes;
    }

    @PostMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<CalendarEntry>> createEntry(
            @PathVariable String studioId,
            @Valid @RequestBody CreateCalendarEntryRequest request) {

        CalendarEntry entry = CalendarEntry.builder()
                .studioId(studioId)
                .photographerId(request.getPhotographerId())
                .bookingId(request.getBookingId())
                .entryType(request.getEntryType())
                .title(request.getTitle())
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .allDay(request.isAllDay())
                .location(request.getLocation())
                .notes(request.getNotes())
                .build();

        CalendarEntry created = calendarService.createEntry(entry);
        return ResponseEntity.ok(ApiResponse.success(created, "Calendar entry created"));
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<CalendarEntry>>> getCalendar(
            @PathVariable String studioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String photographerId) {

        List<CalendarEntry> entries;
        if (photographerId != null) {
            entries = calendarService.getPhotographerCalendar(photographerId, start, end);
        } else {
            entries = calendarService.getStudioCalendar(studioId, start, end);
        }

        return ResponseEntity.ok(ApiResponse.success(entries, "Fetched calendar entries"));
    }

    @DeleteMapping("/{entryId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Void>> deleteEntry(
            @PathVariable String studioId,
            @PathVariable String entryId) {
        calendarService.deleteEntry(entryId);
        return ResponseEntity.ok(ApiResponse.success(null, "Calendar entry deleted"));
    }
}
