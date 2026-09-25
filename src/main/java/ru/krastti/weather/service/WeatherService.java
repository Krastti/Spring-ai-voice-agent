package ru.krastti.weather.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.krastti.weather.client.OpenMeteoClient;
import ru.krastti.weather.dto.GeocodingResponse;
import ru.krastti.weather.dto.WeatherForecastResponse;
import ru.krastti.weather.exception.WeatherLocationNotFoundException;
import ru.krastti.weather.exception.WeatherProviderException;

import java.util.List;
import java.util.Locale;

@Service
public class WeatherService {

    private final OpenMeteoClient openMeteoClient;

    public WeatherService(OpenMeteoClient openMeteoClient) {
        this.openMeteoClient = openMeteoClient;
    }

    public String getTodayWeather(String city) {
        String normalizedCity = city == null ? "" : city.trim();
        if (normalizedCity.isBlank()) {
            throw new WeatherLocationNotFoundException(city);
        }

        try {
            GeocodingResponse geocoding = openMeteoClient.geocode(normalizedCity);
            List<GeocodingResponse.Location> locations = geocoding == null ? null : geocoding.results();
            if (locations == null || locations.isEmpty()) {
                throw new WeatherLocationNotFoundException(normalizedCity);
            }

            GeocodingResponse.Location location = locations.getFirst();
            WeatherForecastResponse forecast = openMeteoClient.forecast(location.latitude(), location.longitude());
            if (forecast == null || forecast.current() == null || forecast.daily() == null) {
                throw new WeatherProviderException(new IllegalStateException("Open-Meteo response is incomplete"));
            }

            return formatWeather(location, forecast);
        } catch (WeatherLocationNotFoundException | WeatherProviderException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new WeatherProviderException(exception);
        }
    }

    private String formatWeather(GeocodingResponse.Location location, WeatherForecastResponse forecast) {
        WeatherForecastResponse.Current current = forecast.current();
        WeatherForecastResponse.Daily daily = forecast.daily();
        String place = location.country() == null || location.country().isBlank()
                ? location.name()
                : location.name() + ", " + location.country();
        String dayDescription = describeCode(first(daily.weather_code()));
        String currentDescription = describeCode(current.weather_code());

        return "Погода в " + place + " сегодня (" + first(daily.time()) + "): "
                + dayDescription + "; сейчас " + format(current.temperature_2m()) + " °C, "
                + currentDescription + ", ощущается как " + format(current.apparent_temperature()) + " °C, "
                + "влажность " + format(current.relative_humidity_2m()) + " %, "
                + "ветер " + format(current.wind_speed_10m()) + " м/с, "
                + "осадки сейчас " + format(current.precipitation()) + " мм. "
                + "Сегодня температура от " + format(first(daily.temperature_2m_min())) + " до "
                + format(first(daily.temperature_2m_max())) + " °C; вероятность осадков до "
                + first(daily.precipitation_probability_max()) + " % (данные на " + current.time() + ").";
    }

    private <T> T first(List<T> values) {
        if (values == null || values.isEmpty()) {
            throw new WeatherProviderException(new IllegalStateException("Open-Meteo response is incomplete"));
        }
        return values.getFirst();
    }

    private String format(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private String describeCode(Integer code) {
        if (code == null) {
            return "неизвестные условия";
        }
        return switch (code) {
            case 0 -> "ясно";
            case 1 -> "преимущественно ясно";
            case 2 -> "переменная облачность";
            case 3 -> "пасмурно";
            case 45, 48 -> "туман";
            case 51, 53, 55 -> "морось";
            case 56, 57 -> "ледяная морось";
            case 61, 63, 65 -> "дождь";
            case 66, 67 -> "ледяной дождь";
            case 71, 73, 75, 77 -> "снег";
            case 80, 81, 82 -> "ливневый дождь";
            case 85, 86 -> "снегопад";
            case 95 -> "гроза";
            case 96, 99 -> "гроза с градом";
            default -> "неизвестные условия (код " + code + ")";
        };
    }
}
