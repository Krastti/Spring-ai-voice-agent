package ru.krastti.calendar.service;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.krastti.calendar.client.GoogleCalendarClient;
import ru.krastti.calendar.dto.CalendarEventDraft;
import ru.krastti.calendar.dto.CreatedCalendarEvent;

import java.time.Instant;

@Service
public class GoogleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);

    private final GoogleCalendarClient calendarClient;

    public GoogleCalendarService(GoogleCalendarClient calendarClient) {
        this.calendarClient = calendarClient;
    }

    public CreatedCalendarEvent createAndVerify(CalendarEventDraft draft) {
        log.info("Google Calendar operation started: name=createAndVerify,title={},start={},end={},timeZone={},description={},location={}",
                draft.title(), draft.start(), draft.end(), draft.timeZone(), draft.description(), draft.location());
        Event created = calendarClient.create(draft);
        if (created == null || created.getId() == null || created.getId().isBlank()) {
            throw new IllegalStateException("Google Calendar did not return an event ID");
        }

        Event verified = calendarClient.get(created.getId());
        if (verified == null || !created.getId().equals(verified.getId())
                || !draft.title().equals(verified.getSummary())
                || !matches(verified.getStart(), draft.start().atZone(draft.timeZone()).toInstant())
                || !matches(verified.getEnd(), draft.end().atZone(draft.timeZone()).toInstant())) {
            throw new IllegalStateException("Google Calendar event verification did not match the created event");
        }

        log.info("Google Calendar event verified: eventId={}", verified.getId());
        return new CreatedCalendarEvent(verified.getId(), verified.getSummary(), verified.getHtmlLink());
    }

    private boolean matches(EventDateTime actual, Instant expected) {
        return actual != null && actual.getDateTime() != null
                && actual.getDateTime().getValue() == expected.toEpochMilli();
    }
}
