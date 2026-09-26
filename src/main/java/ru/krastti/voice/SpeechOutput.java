package ru.krastti.voice;

@FunctionalInterface
public interface SpeechOutput {

    void enqueue(String text);
}
