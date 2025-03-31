package model;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;

public class AudioRecorder {
    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;
    private boolean isRecording;
    private Instant startTime;
    private int recordingDuration; // in seconds
    private File tempFile;
    private byte[] audioData;

    public AudioRecorder() {
        // Định dạng âm thanh: 44100Hz, 16 bit, mono, signed, little endian
        audioFormat = new AudioFormat(44100, 16, 1, true, false);
    }

    public void startRecording(String outputPath) {
        try {
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            if (!AudioSystem.isLineSupported(dataLineInfo)) {
                throw new IllegalStateException("Line not supported");
            }

            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            isRecording = true;
            startTime = Instant.now();
            tempFile = new File(outputPath);

            Thread recordingThread = new Thread(() -> {
                AudioInputStream audioInputStream = new AudioInputStream(targetDataLine);

                try {
                    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, tempFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            recordingThread.start();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        isRecording = false;
        Instant endTime = Instant.now();
        recordingDuration = (int) Duration.between(startTime, endTime).getSeconds();

        targetDataLine.stop();
        targetDataLine.close();

        // Convert recording to byte array
        try {
            audioData = Files.readAllBytes(tempFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public int getRecordingDuration() {
        return recordingDuration;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public String getFilePath() {
        return tempFile.getAbsolutePath();
    }

    public void playRecording() {
        try {
            File soundFile = tempFile;
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);

            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int bytesRead = 0;

            while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                line.write(buffer, 0, bytesRead);
            }

            line.drain();
            line.close();
            audioInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}