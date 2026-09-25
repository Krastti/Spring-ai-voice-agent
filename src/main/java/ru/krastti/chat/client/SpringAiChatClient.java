package ru.krastti.chat.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import ru.krastti.weather.tool.WeatherTools;

@Component
public class SpringAiChatClient implements AiChatClient {

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;

    public SpringAiChatClient(ChatClient chatClient, WeatherTools weatherTools) {
        this.chatClient = chatClient;
        this.weatherTools = weatherTools;
    }

    @Override
    public String call(String message) {
        return chatClient.prompt()
                .user(message)
                .tools(weatherTools)
                .call()
                .content();
    }
}
