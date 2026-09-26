package ru.krastti.voice;

import java.io.IOException;

@FunctionalInterface
public interface AudioPlayer {

    void playPcm16KhzMono(byte[] pcmAudio) throws IOException;
}
