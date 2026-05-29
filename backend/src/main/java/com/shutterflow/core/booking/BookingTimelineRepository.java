package com.shutterflow.core.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingTimelineRepository extends JpaRepository<BookingTimeline, String> {
    List<BookingTimeline> findByBookingIdOrderByCreatedAtDesc(String bookingId);
}
