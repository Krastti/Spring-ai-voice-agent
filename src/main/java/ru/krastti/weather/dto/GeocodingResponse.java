package ru.krastti.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingResponse(List<Location> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(String name, String country, double latitude, double longitude) {
    }
}
