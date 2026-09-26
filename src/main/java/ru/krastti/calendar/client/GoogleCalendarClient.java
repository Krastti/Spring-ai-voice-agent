package ru.krastti.calendar.client;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.krastti.calendar.dto.CalendarEventDraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/** Минимальный OAuth-клиент Google Calendar для создания и чтения событий. */
@Component
public class GoogleCalendarClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarClient.class);
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/calendar.events.owned");

    private final String credentialsFile;
    private final String tokensDirectory;
    private final String calendarId;
    private volatile Calendar service;

    public GoogleCalendarClient(
            @Value("${jarvis.calendar.credentials-file:credentials.json}") String credentialsFile,
            @Value("${jarvis.calendar.tokens-directory:.tokens}") String tokensDirectory,
            @Value("${jarvis.calendar.calendar-id:}") String calendarId
    ) {
        this.credentialsFile = credentialsFile;
        this.tokensDirectory = tokensDirectory;
        this.calendarId = calendarId;
    }

    public Event create(CalendarEventDraft draft) {
        String configuredCalendarId = requiredCalendarId();
        Event event = new Event()
                .setId(UUID.randomUUID().toString().replace("-", ""))
                .setSummary(draft.title())
                .setDescription(draft.description())
                .setLocation(draft.location())
                .setStart(toEventDateTime(draft.start(), draft.timeZone()))
                .setEnd(toEventDateTime(draft.end(), draft.timeZone()));
        try {
            Event created = calendar().events().insert(configuredCalendarId, event).execute();
            log.info("Google Calendar event created: eventId={}", created.getId());
            return created;
        } catch (IOException | GeneralSecurityException exception) {
            log.warn("Google Calendar create failed: {}", exception.getClass().getSimpleName());
            throw new IllegalStateException("Google Calendar event could not be created");
        }
    }

    public Event get(String eventId) {
        String configuredCalendarId = requiredCalendarId();
        log.info("Google Calendar read started: eventId={}", eventId);
        try {
            Event event = calendar().events().get(configuredCalendarId, eventId).execute();
            log.info("Google Calendar read completed: eventId={}, found={}", eventId, event != null);
            return event;
        } catch (IOException | GeneralSecurityException exception) {
            log.warn("Google Calendar verification failed: {}", exception.getClass().getSimpleName());
            throw new IllegalStateException("Google Calendar event could not be verified");
        }
    }

    private synchronized Calendar calendar() throws IOException, GeneralSecurityException {
        if (service == null) {
            File credentials = new File(credentialsFile);
            if (!credentials.isFile()) {
                throw new IllegalStateException("Google OAuth credentials file is not configured");
            }
            NetHttpTransport transport = new NetHttpTransport.Builder().build();
            GoogleClientSecrets secrets;
            try (FileInputStream input = new FileInputStream(credentials)) {
                secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(input));
            }
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    transport,
                    JSON_FACTORY,
                    secrets,
                    SCOPES
            )
                    .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectory)))
                    .setAccessType("offline")
                    .build();
            Credential credential = new AuthorizationCodeInstalledApp(
                    flow,
                    new LocalServerReceiver.Builder().setPort(8888).build()
            ).authorize("jarvis-user");
            service = new Calendar.Builder(transport, JSON_FACTORY, credential)
                    .setApplicationName("Jarvis")
                    .build();
        }
        return service;
    }

    private String requiredCalendarId() {
        if (calendarId == null || calendarId.isBlank()) {
            throw new IllegalStateException("Google Calendar test calendar ID is not configured");
        }
        return calendarId;
    }

    private EventDateTime toEventDateTime(java.time.LocalDateTime value, ZoneId zoneId) {
        Date date = Date.from(value.atZone(zoneId).toInstant());
        return new EventDateTime()
                .setDateTime(new DateTime(date))
                .setTimeZone(zoneId.getId());
    }
}
