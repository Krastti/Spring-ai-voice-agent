package ru.krastti.calendar.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record CalendarEventDraft(
        String title,
        LocalDateTime start,
        LocalDateTime end,
        ZoneId timeZone,
        String description,
        String location
) {
}
