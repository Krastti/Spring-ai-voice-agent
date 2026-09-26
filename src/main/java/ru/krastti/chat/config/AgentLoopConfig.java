package ru.krastti.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import ru.krastti.weather.exception.WeatherLocationNotFoundException;

@Configuration
public class AgentLoopConfig {

    public static final String SYSTEM_PROMPT = "You are Jarvis, a concise personal AI assistant. Respond in the user's language. For weather questions, use the available weather tool and never invent weather data. If the city is not specified, ask which city the user means.";

    @Bean
    public ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
        return AgentLoopConfig::safeToolError;
    }

    public static String safeToolError(ToolExecutionException exception) {
        if (hasCause(exception, WeatherLocationNotFoundException.class)) {
            return "No matching city was found. Ask the user to clarify the city; do not claim that weather was retrieved.";
        }

        return "The weather lookup failed. Do not claim it succeeded; explain that weather is temporarily unavailable.";
    }

    private static boolean hasCause(Throwable failure, Class<? extends Throwable> type) {
        Throwable cause = failure;
        while (cause != null) {
            if (type.isInstance(cause)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
