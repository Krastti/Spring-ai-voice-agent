package ru.krastti.chat.exception;

public class ChatProviderException extends RuntimeException {

    public ChatProviderException(Throwable cause) {
        super("AI provider request failed", cause);
    }

    public ChatProviderException(String message) {
        super(message);
    }
}
