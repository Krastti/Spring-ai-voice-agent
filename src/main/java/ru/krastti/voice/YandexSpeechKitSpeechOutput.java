package ru.krastti.voice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.concurrent.RejectedExecutionException;

@Service
public class YandexSpeechKitSpeechOutput implements SpeechOutput {

    private static final Logger log = LoggerFactory.getLogger(YandexSpeechKitSpeechOutput.class);
    private final RestClient restClient;
    private final TaskExecutor speechExecutor;
    private final AudioPlayer audioPlayer;
    private final String apiUrl;
    private final String apiKey;
    private final String voice;
    private final String speed;

    public YandexSpeechKitSpeechOutput(RestClient.Builder restClientBuilder,
                                       TaskExecutor speechExecutor,
                                       AudioPlayer audioPlayer,
                                       @Value("${yandex.speechkit.api-url}") String apiUrl,
                                       @Value("${YANDEX_SPEECHKIT_API_KEY:}") String apiKey,
                                       @Value("${YANDEX_SPEECHKIT_VOICE:filipp}") String voice,
                                       @Value("${YANDEX_SPEECHKIT_SPEED:0.95}") String speed) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
        this.speechExecutor = speechExecutor;
        this.audioPlayer = audioPlayer;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.voice = voice;
        this.speed = speed;
    }

    @Override
    public void enqueue(String text) {
        if (apiKey.isBlank() || text == null || text.isBlank()) {
            if (apiKey.isBlank()) {
                log.debug("Yandex SpeechKit TTS is disabled: configure YANDEX_SPEECHKIT_API_KEY");
            }
            return;
        }

        try {
            speechExecutor.execute(() -> synthesizeAndPlay(text));
        } catch (RejectedExecutionException exception) {
            log.warn("TTS queue is full; dropping speech response");
        }
    }

    private void synthesizeAndPlay(String text) {
        long startedAt = System.nanoTime();
        try {
            MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
            request.add("text", text);
            request.add("lang", "ru-RU");
            request.add("voice", voice);
            request.add("speed", speed);
            request.add("format", "lpcm");
            request.add("sampleRateHertz", "16000");

            byte[] pcmAudio = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Api-Key " + apiKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(request)
                    .retrieve()
                    .body(byte[].class);
            audioPlayer.playPcm16KhzMono(pcmAudio);
            log.info("Yandex SpeechKit response played in {} ms", elapsedMilliseconds(startedAt));
        } catch (Exception exception) {
            log.warn("Yandex SpeechKit TTS request or playback failed: {}", exception.getClass().getSimpleName());
        }
    }

    private long elapsedMilliseconds(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
