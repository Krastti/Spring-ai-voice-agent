package ru.krastti.chat.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import ru.krastti.calendar.dto.CalendarEventDraft;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Хранит историю и незавершённый черновик диалога только в памяти процесса. */
@Component
public class ConversationStore {

    private final ConcurrentMap<UUID, Conversation> conversations = new ConcurrentHashMap<>();

    public Conversation getOrCreate(UUID conversationId) {
        return conversations.computeIfAbsent(conversationId, ignored -> new Conversation());
    }

    public static final class Conversation {

        private final List<Message> messages = new ArrayList<>();
        private CalendarEventDraft pendingCalendarEvent;
        private boolean calendarFlowActive;

        public List<Message> messages() {
            return messages;
        }

        public CalendarEventDraft pendingCalendarEvent() {
            return pendingCalendarEvent;
        }

        public void setPendingCalendarEvent(CalendarEventDraft pendingCalendarEvent) {
            this.pendingCalendarEvent = pendingCalendarEvent;
        }

        public CalendarEventDraft takePendingCalendarEvent() {
            CalendarEventDraft result = pendingCalendarEvent;
            pendingCalendarEvent = null;
            return result;
        }

        public void clearPendingCalendarEvent() {
            pendingCalendarEvent = null;
        }

        public boolean isCalendarFlowActive() {
            return calendarFlowActive;
        }

        public void setCalendarFlowActive(boolean calendarFlowActive) {
            this.calendarFlowActive = calendarFlowActive;
        }
    }
}
