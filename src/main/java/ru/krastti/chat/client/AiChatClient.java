package ru.krastti.chat.client;

@FunctionalInterface
public interface AiChatClient {

    String call(String message);
}
