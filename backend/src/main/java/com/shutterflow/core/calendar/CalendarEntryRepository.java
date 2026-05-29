package com.shutterflow.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarEntryRepository extends JpaRepository<CalendarEntry, String> {

    List<CalendarEntry> findByStudioId(String studioId);

    List<CalendarEntry> findByPhotographerId(String photographerId);

    @Query("SELECT c FROM CalendarEntry c WHERE c.photographerId = :photographerId AND c.startDatetime >= :start AND c.endDatetime <= :end")
    List<CalendarEntry> findByPhotographerAndDateRange(String photographerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM CalendarEntry c WHERE c.studioId = :studioId AND c.startDatetime >= :start AND c.endDatetime <= :end")
    List<CalendarEntry> findByStudioAndDateRange(String studioId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM CalendarEntry c WHERE c.photographerId = :photographerId AND c.startDatetime < :end AND c.endDatetime > :start")
    List<CalendarEntry> findOverlappingEntries(String photographerId, LocalDateTime start, LocalDateTime end);
}
