package view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import javax.sound.sampled.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.VoiceMessage;

public class VoiceMessageComponent extends HBox {

    private Button playButton;
    private Label durationLabel;
    private VoiceMessage voiceMessage;
    private boolean isPlaying = false;
    private Thread playbackThread;
    private Label timeLabel;
    private final String playIconPath = "/resouces/play.png";
    private final String pauseIconPath = "/resouces/pause.png";
    private File tempFile;

    public VoiceMessageComponent(VoiceMessage message, boolean isFromCurrentUser) {
        this.voiceMessage = message;

        // Create temp file for playback
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            tempFile = new File(tempDir, UUID.randomUUID().toString() + ".wav");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(message.getAudioData());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create UI components
        playButton = new Button();
        ImageView playIcon = new ImageView(new Image(getClass().getResourceAsStream(playIconPath)));
        playIcon.setFitHeight(20);
        playIcon.setFitWidth(20);
        playButton.setGraphic(playIcon);
        playButton.getStyleClass().add("voice-play-button");
        playButton.setStyle(
                "-fx-background-color: #4b96ff; -fx-background-radius: 30; -fx-min-width: 40px; -fx-min-height: 40px; -fx-max-width: 40px; -fx-max-height: 40px;");

        // Create wave form visual representation
        HBox waveform = createWaveform();

        // Duration label
        durationLabel = new Label(formatDuration(message.getDuration()));
        durationLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");

        // Current time label
        timeLabel = new Label("0:00");
        timeLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");

        // Add components to a VBox
        VBox container = new VBox(5);
        container.getChildren().addAll(waveform, new HBox(10, timeLabel, durationLabel));

        // Set up the event handler for play button
        playButton.setOnAction(e -> togglePlayback());

        this.setPadding(new Insets(10, 15, 10, 15));
        this.setSpacing(10);

        // Align based on sender
        if (isFromCurrentUser) {
            this.setAlignment(Pos.CENTER_RIGHT);
            this.setStyle("-fx-background-color: #DCF8C6; -fx-background-radius: 15px;");
            this.getChildren().addAll(container, playButton);
        } else {
            this.setAlignment(Pos.CENTER_LEFT);
            this.setStyle(
                    "-fx-background-color: #FFFFFF; -fx-background-radius: 15px; -fx-border-color: #E0E0E0; -fx-border-radius: 15px; -fx-border-width: 1px;");
            this.getChildren().addAll(playButton, container);
        }
    }

    private HBox createWaveform() {
        HBox waveform = new HBox(2);
        waveform.setAlignment(Pos.CENTER_LEFT);

        // Create simple waveform visualization (10 bars with varying heights)
        for (int i = 0; i < 10; i++) {
            int barHeight = (int) (Math.random() * 20) + 10; // Random height between 10-30px
            VBox bar = new VBox();
            bar.setPrefWidth(4);
            bar.setPrefHeight(barHeight);
            bar.setStyle("-fx-background-color: #a3c6ff; -fx-background-radius: 2;");
            waveform.getChildren().add(bar);
        }

        return waveform;
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    private void togglePlayback() {
        if (isPlaying) {
            stopPlayback();
        } else {
            startPlayback();
        }
    }

    private void startPlayback() {
        try {
            isPlaying = true;
            // Update button to show pause icon
            Platform.runLater(() -> {
                ImageView pauseIcon = new ImageView(new Image(getClass().getResourceAsStream(pauseIconPath)));
                pauseIcon.setFitHeight(20);
                pauseIcon.setFitWidth(20);
                playButton.setGraphic(pauseIcon);
            });

            playbackThread = new Thread(() -> {
                try {
                    // Play the audio file
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(tempFile);
                    AudioFormat format = audioInputStream.getFormat();
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

                    line.open(format);
                    line.start();

                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int bytesRead = 0;

                    int totalBytesRead = 0;
                    long frameSize = format.getFrameSize();
                    float frameRate = format.getFrameRate();
                    long totalFrames = audioInputStream.getFrameLength();

                    while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1 && isPlaying) {
                        line.write(buffer, 0, bytesRead);

                        // Update time label
                        totalBytesRead += bytesRead;
                        long framesRead = totalBytesRead / frameSize;
                        float currentTimeInSeconds = framesRead / frameRate;
                        int seconds = (int) currentTimeInSeconds;
                        int minutes = seconds / 60;
                        seconds = seconds % 60;

                        String timeText = String.format("%d:%02d", minutes, seconds);
                        Platform.runLater(() -> timeLabel.setText(timeText));
                    }

                    line.drain();
                    line.close();
                    audioInputStream.close();

                    // Reset when playback completes
                    if (isPlaying) { // Only if we completed naturally, not stopped
                        Platform.runLater(() -> {
                            isPlaying = false;
                            ImageView playIcon = new ImageView(new Image(getClass().getResourceAsStream(playIconPath)));
                            playIcon.setFitHeight(20);
                            playIcon.setFitWidth(20);
                            playButton.setGraphic(playIcon);
                            timeLabel.setText("0:00");
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    isPlaying = false;
                    Platform.runLater(() -> {
                        ImageView playIcon = new ImageView(new Image(getClass().getResourceAsStream(playIconPath)));
                        playIcon.setFitHeight(20);
                        playIcon.setFitWidth(20);
                        playButton.setGraphic(playIcon);
                    });
                }
            });

            playbackThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlayback() {
        isPlaying = false;
        if (playbackThread != null) {
            playbackThread.interrupt();
        }

        Platform.runLater(() -> {
            ImageView playIcon = new ImageView(new Image(getClass().getResourceAsStream(playIconPath)));
            playIcon.setFitHeight(20);
            playIcon.setFitWidth(20);
            playButton.setGraphic(playIcon);
        });
    }

    public void cleanup() {
        // Stop playback if running
        if (isPlaying) {
            stopPlayback();
        }

        // Delete temp file
        if (tempFile != null && tempFile.exists()) {
            try {
                Files.deleteIfExists(tempFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}