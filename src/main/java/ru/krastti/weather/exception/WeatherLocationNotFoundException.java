package ru.krastti.weather.exception;

public class WeatherLocationNotFoundException extends RuntimeException {

    public WeatherLocationNotFoundException(String city) {
        super("No location found for city: " + city);
    }
}
