package ru.krastti.weather.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import ru.krastti.weather.service.WeatherService;

@Component
public class WeatherTools {

    private static final Logger log = LoggerFactory.getLogger(WeatherTools.class);

    private final WeatherService weatherService;

    public WeatherTools(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Tool(description = "Get today's current weather and daily forecast for a city. If the user's city is unknown, ask them which city before calling this tool.")
    public String getTodayWeather(
            @ToolParam(description = "City name, preferably in the user's language") String city
    ) {
        long startedAt = System.nanoTime();
        log.info("Weather tool started: getTodayWeather(city={})", city);
        try {
            String result = weatherService.getTodayWeather(city);
            log.info("Weather tool completed in {} ms: city={}, result={}",
                    (System.nanoTime() - startedAt) / 1_000_000, city, result);
            return result;
        } catch (RuntimeException exception) {
            log.warn("Weather tool failed in {} ms: {}",
                    (System.nanoTime() - startedAt) / 1_000_000,
                    exception.getClass().getSimpleName());
            throw exception;
        }
    }
}
