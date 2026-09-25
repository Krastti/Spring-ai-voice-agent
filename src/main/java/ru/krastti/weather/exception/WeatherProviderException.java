package ru.krastti.weather.exception;

public class WeatherProviderException extends RuntimeException {

    public WeatherProviderException(Throwable cause) {
        super("Weather provider request failed", cause);
    }
}
