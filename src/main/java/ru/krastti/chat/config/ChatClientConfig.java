package ru.krastti.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("You are Jarvis, a concise personal AI assistant. Respond in the user's language. For weather questions, use the available weather tool and never invent weather data. If the city is not specified, ask which city the user means.")
                .build();
    }
}
