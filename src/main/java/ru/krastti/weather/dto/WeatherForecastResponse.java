package ru.krastti.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherForecastResponse(Current current, Daily daily) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            String time,
            double temperature_2m,
            double relative_humidity_2m,
            double apparent_temperature,
            double precipitation,
            int weather_code,
            double wind_speed_10m
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(
            List<String> time,
            List<Integer> weather_code,
            List<Double> temperature_2m_max,
            List<Double> temperature_2m_min,
            List<Integer> precipitation_probability_max
    ) {
    }
}
