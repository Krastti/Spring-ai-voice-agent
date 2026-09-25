# Jarvis

Minimal stateless text assistant built with Java 21, Spring Boot, Spring AI, and OpenRouter.

## Run locally

Put the OpenRouter API key in the ignored `.env` file:

```dotenv
OPENROUTER_API_KEY=your-openrouter-api-key
```

Then start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

The application imports `.env` as optional local configuration. A system `OPENROUTER_API_KEY` environment variable can override it. Do not commit or share the key. Requests go to OpenRouter's OpenAI-compatible endpoint and use the fixed free model `inclusionai/ling-3.0-flash-fin:free`.

## Chat API

Send one message per request. The MVP does not keep conversation history.

```powershell
curl.exe -X POST http://localhost:8080/api/chat `
  -H "Content-Type: application/json" `
  -d '{"message":"Привет"}'
```

Successful response:

```json
{"reply":"Здравствуйте! Чем могу помочь?"}
```

Blank messages return `400 Bad Request`. Upstream failures return `502 Bad Gateway` without exposing provider details. For weather, include a city (for example, `Какая сегодня погода в Москве?`); if the city is missing, Jarvis asks you to specify it.

Open-Meteo geocoding and forecast APIs provide city coordinates and today's weather without an API key. The free API is intended for non-commercial use; provide attribution to Open-Meteo when sharing weather data.

## Verify

```powershell
.\mvnw.cmd clean verify
```
