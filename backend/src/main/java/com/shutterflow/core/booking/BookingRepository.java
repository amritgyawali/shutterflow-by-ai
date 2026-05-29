package com.shutterflow.core.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByStudioId(String studioId);

    List<Booking> findByPhotographerIdAndEventDate(String photographerId, LocalDate eventDate);

    List<Booking> findByClientId(String clientId);

    @Query("SELECT b FROM Booking b WHERE b.photographerId = :photographerId AND b.eventDate = :date AND b.status NOT IN ('CANCELLED', 'ARCHIVED')")
    List<Booking> findActiveBookingsByPhotographerAndDate(String photographerId, LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.studioId = :studioId AND b.status = :status")
    List<Booking> findByStudioIdAndStatus(String studioId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.studioId = :studioId AND b.eventDate BETWEEN :startDate AND :endDate")
    List<Booking> findByStudioIdAndEventDateBetween(String studioId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT b FROM Booking b WHERE b.photographerId = :photographerId AND b.eventDate BETWEEN :startDate AND :endDate AND b.status NOT IN ('CANCELLED', 'ARCHIVED')")
    List<Booking> findActiveByPhotographerAndDateRange(String photographerId, LocalDate startDate, LocalDate endDate);
}
