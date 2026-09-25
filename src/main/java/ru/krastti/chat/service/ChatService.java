package ru.krastti.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.krastti.chat.client.AiChatClient;
import ru.krastti.chat.exception.ChatProviderException;

import java.util.concurrent.TimeUnit;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final AiChatClient chatClient;

    public ChatService(AiChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @SuppressWarnings("LoggingSimilarMessage")
    public String reply(String message) {
        long startedAt = System.nanoTime();
        log.info("Processing chat request");

        try {
            String reply = chatClient.call(message);
            if (reply == null || reply.isBlank()) {
                throw new ChatProviderException("AI provider returned an empty response");
            }

            log.info("Chat request completed in {} ms", elapsedMilliseconds(startedAt));
            return reply;
        } catch (ChatProviderException exception) {
            log.warn("Chat request failed in {} ms: {}", elapsedMilliseconds(startedAt), exception.getClass().getSimpleName());
            throw exception;
        } catch (RuntimeException exception) {
            log.warn("Chat request failed in {} ms: {}", elapsedMilliseconds(startedAt), exception.getClass().getSimpleName());
            throw new ChatProviderException(exception);
        }
    }

    private long elapsedMilliseconds(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
