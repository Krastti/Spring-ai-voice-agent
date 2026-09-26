package ru.krastti.weather.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenMeteoClientConfig {

    @Bean
    @Qualifier("openMeteoGeocoding")
    RestClient openMeteoGeocoding(RestClient.Builder builder) {
        return builder.clone()
                .baseUrl("https://geocoding-api.open-meteo.com")
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    @Bean
    @Qualifier("openMeteoForecast")
    RestClient openMeteoForecast(RestClient.Builder builder) {
        return builder.clone()
                .baseUrl("https://api.open-meteo.com")
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }
}
