package ru.krastti.weather.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.krastti.weather.dto.GeocodingResponse;
import ru.krastti.weather.dto.WeatherForecastResponse;

@Component
public class OpenMeteoClient {

    private final RestClient geocodingClient;
    private final RestClient forecastClient;

    public OpenMeteoClient(
            @Qualifier("openMeteoGeocoding") RestClient geocodingClient,
            @Qualifier("openMeteoForecast") RestClient forecastClient
    ) {
        this.geocodingClient = geocodingClient;
        this.forecastClient = forecastClient;
    }

    public GeocodingResponse geocode(String city) {
        return geocodingClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("name", city)
                        .queryParam("count", 5)
                        .queryParam("language", "ru")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(GeocodingResponse.class);
    }

    public WeatherForecastResponse forecast(double latitude, double longitude) {
        return forecastClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m")
                        .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max")
                        .queryParam("temperature_unit", "celsius")
                        .queryParam("wind_speed_unit", "ms")
                        .queryParam("timezone", "auto")
                        .queryParam("forecast_days", 1)
                        .build())
                .retrieve()
                .body(WeatherForecastResponse.class);
    }
}
