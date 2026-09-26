package ru.krastti.chat.service;

import java.util.UUID;

public record ChatResult(String reply, UUID conversationId) {
}
