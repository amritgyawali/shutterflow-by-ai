package com.shutterflow.core.calendar;

import com.shutterflow.core.common.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final CalendarEntryRepository calendarEntryRepository;

    @Transactional
    public CalendarEntry createEntry(CalendarEntry entry) {
        entry.setId(UUID.randomUUID().toString());

        // Conflict detection: check for overlapping entries
        List<CalendarEntry> conflicts = calendarEntryRepository.findOverlappingEntries(
                entry.getPhotographerId(), entry.getStartDatetime(), entry.getEndDatetime());

        if (!conflicts.isEmpty() && entry.getEntryType() == CalendarEntryType.BOOKING) {
            throw new AppException("Time conflict detected with existing calendar entry", HttpStatus.CONFLICT);
        }

        return calendarEntryRepository.save(entry);
    }

    public List<CalendarEntry> getPhotographerCalendar(String photographerId, LocalDateTime start, LocalDateTime end) {
        return calendarEntryRepository.findByPhotographerAndDateRange(photographerId, start, end);
    }

    public List<CalendarEntry> getStudioCalendar(String studioId, LocalDateTime start, LocalDateTime end) {
        return calendarEntryRepository.findByStudioAndDateRange(studioId, start, end);
    }

    /**
     * Get public availability (free/busy only - no details).
     */
    public List<AvailabilitySlot> getPublicAvailability(String photographerId, LocalDateTime start, LocalDateTime end) {
        List<CalendarEntry> entries = calendarEntryRepository.findByPhotographerAndDateRange(photographerId, start, end);
        return entries.stream()
                .map(e -> new AvailabilitySlot(e.getStartDatetime(), e.getEndDatetime(), false))
                .toList();
    }

    @Transactional
    public void deleteEntry(String entryId) {
        calendarEntryRepository.deleteById(entryId);
    }

    @Transactional
    public CalendarEntry updateEntry(String entryId, CalendarEntry updated) {
        CalendarEntry existing = calendarEntryRepository.findById(entryId)
                .orElseThrow(() -> new AppException("Calendar entry not found", HttpStatus.NOT_FOUND));

        existing.setTitle(updated.getTitle());
        existing.setStartDatetime(updated.getStartDatetime());
        existing.setEndDatetime(updated.getEndDatetime());
        existing.setLocation(updated.getLocation());
        existing.setNotes(updated.getNotes());
        existing.setAllDay(updated.isAllDay());

        return calendarEntryRepository.save(existing);
    }

    public record AvailabilitySlot(LocalDateTime start, LocalDateTime end, boolean available) {}
}
