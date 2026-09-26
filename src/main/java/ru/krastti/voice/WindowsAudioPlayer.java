package ru.krastti.voice;

import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;

@Component
public class WindowsAudioPlayer implements AudioPlayer {

    private static final AudioFormat PCM_FORMAT = new AudioFormat(16_000, 16, 1, true, false);

    @Override
    public void playPcm16KhzMono(byte[] pcmAudio) throws IOException {
        // Проверка, что переданный массив рабочий
        if (pcmAudio == null || pcmAudio.length == 0 || pcmAudio.length % PCM_FORMAT.getFrameSize() != 0) {
            throw new IOException("Yandex SpeechKit returned empty or invalid PCM audio");
        }

        DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, PCM_FORMAT);

        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(lineInfo)) {
            line.open(PCM_FORMAT);
            line.start();
            int offset = 0;
            while (offset < pcmAudio.length) {
                offset += line.write(pcmAudio, offset, Math.min(4_096, pcmAudio.length - offset));
            }
            line.drain();
        } catch (Exception exception) {
            throw new IOException("Could not play TTS audio on the default Windows device", exception);
        }
    }
}
