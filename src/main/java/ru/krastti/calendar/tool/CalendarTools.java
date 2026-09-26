package ru.krastti.calendar.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.krastti.calendar.dto.CalendarEventDraft;
import ru.krastti.chat.service.ConversationStore;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/** Инструмент сохраняет черновик; запись в календарь выполняется только после подтверждения кода приложения. */
public class CalendarTools {

    private static final Logger log = LoggerFactory.getLogger(CalendarTools.class);

    private final ConversationStore.Conversation conversation;
    private final ZoneId timeZone;

    public CalendarTools(ConversationStore.Conversation conversation, String timeZone) {
        this.conversation = conversation;
        this.timeZone = ZoneId.of(timeZone);
    }

    @Tool(description = "Prepare a Google Calendar event draft only. Never creates an event. Ask the user for missing title, start or end date and time before calling. Use ISO local date-time values such as 2026-10-01T15:00:00. The application will ask the user to confirm the complete draft before writing it.")
    public String prepareCalendarEvent(
            @ToolParam(description = "Short event title") String title,
            @ToolParam(description = "Local event start in ISO format yyyy-MM-dd'T'HH:mm:ss") String startDateTime,
            @ToolParam(description = "Local event end in ISO format yyyy-MM-dd'T'HH:mm:ss") String endDateTime,
            @ToolParam(description = "Optional event description", required = false) String description,
            @ToolParam(description = "Optional event location", required = false) String location
    ) {
        log.info("Calendar tool started: name=prepareCalendarEvent,title={},start={},end={},description={},location={}",
                title, startDateTime, endDateTime, description, location);
        if (title == null || title.isBlank() || startDateTime == null || startDateTime.isBlank()
                || endDateTime == null || endDateTime.isBlank()) {
            log.info("Calendar tool completed: name=prepareCalendarEvent,result=missing required fields");
            return "The title, start and end are required. Ask the user to provide any missing values; do not guess.";
        }

        try {
            LocalDateTime start = LocalDateTime.parse(startDateTime);
            LocalDateTime end = LocalDateTime.parse(endDateTime);
            if (!end.isAfter(start)) {
                log.info("Calendar tool completed: name=prepareCalendarEvent,result=invalid time range");
                return "The end must be after the start. Ask the user to clarify the event time.";
            }
            CalendarEventDraft draft = new CalendarEventDraft(
                    title.trim(), start, end, timeZone, blankToNull(description), blankToNull(location)
            );
            conversation.setPendingCalendarEvent(draft);
            log.info("Calendar tool completed: name=prepareCalendarEvent,result=draft stored");
            return "Draft saved; no calendar event has been created. Show the title, date, start, end, time zone, and optional details to the user, then ask for explicit confirmation.";
        } catch (DateTimeParseException exception) {
            log.info("Calendar tool completed: name=prepareCalendarEvent,result=invalid date or time");
            return "The event date or time is invalid. Ask the user to clarify it; do not create an event.";
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
