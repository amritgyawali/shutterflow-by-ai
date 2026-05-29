package com.shutterflow.core.booking;

import com.shutterflow.core.common.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingTimelineRepository timelineRepository;

    @Transactional
    public Booking createBooking(Booking booking) {
        booking.setId(UUID.randomUUID().toString());
        booking.setStatus(BookingStatus.INQUIRY);

        // Conflict detection: check if photographer already has a booking on this date
        List<Booking> conflicts = bookingRepository.findActiveBookingsByPhotographerAndDate(
                booking.getPhotographerId(), booking.getEventDate());

        if (!conflicts.isEmpty()) {
            throw new AppException(
                    "Photographer already has a booking on " + booking.getEventDate() + ". Conflict detected.",
                    HttpStatus.CONFLICT);
        }

        Booking saved = bookingRepository.save(booking);

        // Record timeline entry
        recordTimeline(saved.getId(), null, BookingStatus.INQUIRY.name(), null, "Booking created");

        log.info("Created booking {} for studio {} on {}", saved.getId(), saved.getStudioId(), saved.getEventDate());
        return saved;
    }

    @Transactional
    public Booking updateStatus(String bookingId, BookingStatus newStatus, String changedBy, String note) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));

        String previousStatus = booking.getStatus().name();
        booking.setStatus(newStatus);
        Booking saved = bookingRepository.save(booking);

        recordTimeline(bookingId, previousStatus, newStatus.name(), changedBy, note);

        log.info("Booking {} status changed from {} to {}", bookingId, previousStatus, newStatus);
        return saved;
    }

    public Booking getBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));
    }

    public List<Booking> getStudioBookings(String studioId) {
        return bookingRepository.findByStudioId(studioId);
    }

    public List<Booking> getBookingsByDateRange(String studioId, LocalDate start, LocalDate end) {
        return bookingRepository.findByStudioIdAndEventDateBetween(studioId, start, end);
    }

    public List<BookingTimeline> getTimeline(String bookingId) {
        return timelineRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
    }

    private void recordTimeline(String bookingId, String previousStatus, String newStatus, String changedBy, String note) {
        BookingTimeline timeline = BookingTimeline.builder()
                .id(UUID.randomUUID().toString())
                .bookingId(bookingId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .note(note)
                .build();
        timelineRepository.save(timeline);
    }
}
