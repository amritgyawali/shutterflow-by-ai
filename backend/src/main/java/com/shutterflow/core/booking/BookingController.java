package com.shutterflow.core.booking;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/studios/{studioId}/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Data
    public static class CreateBookingRequest {
        @NotBlank
        private String clientId;
        @NotBlank
        private String photographerId;
        private String secondShooterId;
        private String packageId;
        @NotBlank
        private String eventType;
        @NotNull
        private LocalDate eventDate;
        private LocalTime eventStartTime;
        private LocalTime eventEndTime;
        private String eventLocation;
        private BigDecimal eventLatitude;
        private BigDecimal eventLongitude;
        private String notesInternal;
        private String notesClient;
        private BigDecimal travelFee;
        private BigDecimal overtimeFee;
    }

    @Data
    public static class UpdateStatusRequest {
        @NotNull
        private BookingStatus status;
        private String note;
    }

    @PostMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Booking>> createBooking(
            @PathVariable String studioId,
            @Valid @RequestBody CreateBookingRequest request) {

        Booking booking = Booking.builder()
                .studioId(studioId)
                .clientId(request.getClientId())
                .photographerId(request.getPhotographerId())
                .secondShooterId(request.getSecondShooterId())
                .packageId(request.getPackageId())
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .eventStartTime(request.getEventStartTime())
                .eventEndTime(request.getEventEndTime())
                .eventLocation(request.getEventLocation())
                .eventLatitude(request.getEventLatitude())
                .eventLongitude(request.getEventLongitude())
                .notesInternal(request.getNotesInternal())
                .notesClient(request.getNotesClient())
                .travelFee(request.getTravelFee() != null ? request.getTravelFee() : BigDecimal.ZERO)
                .overtimeFee(request.getOvertimeFee() != null ? request.getOvertimeFee() : BigDecimal.ZERO)
                .build();

        Booking created = bookingService.createBooking(booking);
        return ResponseEntity.ok(ApiResponse.success(created, "Booking created successfully"));
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookings(
            @PathVariable String studioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Booking> bookings;
        if (startDate != null && endDate != null) {
            bookings = bookingService.getBookingsByDateRange(studioId, startDate, endDate);
        } else {
            bookings = bookingService.getStudioBookings(studioId);
        }

        return ResponseEntity.ok(ApiResponse.success(bookings, "Fetched bookings"));
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Booking>> getBooking(
            @PathVariable String studioId,
            @PathVariable String bookingId) {

        Booking booking = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(booking, "Fetched booking"));
    }

    @PatchMapping("/{bookingId}/status")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<Booking>> updateStatus(
            @PathVariable String studioId,
            @PathVariable String bookingId,
            @Valid @RequestBody UpdateStatusRequest request) {

        Booking updated = bookingService.updateStatus(bookingId, request.getStatus(), null, request.getNote());
        return ResponseEntity.ok(ApiResponse.success(updated, "Booking status updated"));
    }

    @GetMapping("/{bookingId}/timeline")
    @PreAuthorize("@tenantSecurity.hasAccessToStudio(authentication, #studioId)")
    public ResponseEntity<ApiResponse<List<BookingTimeline>>> getTimeline(
            @PathVariable String studioId,
            @PathVariable String bookingId) {

        List<BookingTimeline> timeline = bookingService.getTimeline(bookingId);
        return ResponseEntity.ok(ApiResponse.success(timeline, "Fetched booking timeline"));
    }
}
