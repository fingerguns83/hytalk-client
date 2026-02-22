package net.fg83.hytalkclient.service;

import javafx.animation.AnimationTimer;
import net.fg83.hytalkclient.audio.InputAudioStream;
import net.fg83.hytalkclient.audio.OutputAudioStream;
import net.fg83.hytalkclient.audio.PlayerAudioStream;
import net.fg83.hytalkclient.ui.controller.channelstrip.ChannelStripController;
import net.fg83.hytalkclient.ui.controller.channelstrip.InputChannelStripController;
import net.fg83.hytalkclient.ui.controller.channelstrip.OutputChannelStripController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MixerManager coordinates UI updates for the mixer.
 *
 * Responsibilities:
 * - Link audio streams to channel strip controllers for metering
 * - Update VU meters at 60fps
 * - Pure UI coordination (no audio processing)
 */
public class MixerManager {

    private final AudioStreamManager audioStreamManager;

    // UI controllers
    private InputChannelStripController inputController;
    private OutputChannelStripController outputController;
    private final Map<UUID, ChannelStripController> playerControllers = new HashMap<>();

    // Meter update timer
    private AnimationTimer meterTimer;
    private boolean running = false;

    public MixerManager(AudioStreamManager audioStreamManager) {
        this.audioStreamManager = audioStreamManager;
    }

    // === UI Controller Registration ===

    public void setInputController(InputChannelStripController controller) {
        this.inputController = controller;
    }

    public void setOutputController(OutputChannelStripController controller) {
        this.outputController = controller;
    }

    public void addPlayerController(UUID playerId, ChannelStripController controller) {
        playerControllers.put(playerId, controller);
    }

    public void removePlayerController(UUID playerId) {
        playerControllers.remove(playerId);
    }

    // === Meter Updates ===

    /**
     * Start the meter update timer (60fps)
     */
    public void startMeterUpdates() {
        if (running) {
            return;
        }

        running = true;
        meterTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateAllMeters();
            }
        };
        meterTimer.start();

        System.out.println("MixerManager: Started meter updates");
    }

    /**
     * Stop the meter update timer
     */
    public void stopMeterUpdates() {
        running = false;
        if (meterTimer != null) {
            meterTimer.stop();
            meterTimer = null;
        }

        System.out.println("MixerManager: Stopped meter updates");
    }

    /**
     * Update all VU meters from their respective audio streams
     */
    private void updateAllMeters() {
        // Update input meter
        InputAudioStream inputStream = audioStreamManager.getInputStream();
        if (inputController != null && inputStream != null && inputStream.isRunning()) {
            float level = inputStream.getCurrentLevel();
            inputController.updateMeter(level);
        }

        // Update player meters
        playerControllers.forEach((playerId, controller) -> {
            PlayerAudioStream stream = audioStreamManager.getPlayerStream(playerId);
            if (stream != null && stream.isPlaying()) {
                float level = stream.getCurrentLevel();
                controller.updateMeter(level);
            }
        });

        // Update output meter
        OutputAudioStream outputStream = audioStreamManager.getOutputStream();
        if (outputController != null && outputStream != null && outputStream.isRunning()) {
            float level = outputStream.getCurrentLevel();
            outputController.updateMeter(level);
        }
    }


    public void shutdown() {
        stopMeterUpdates();
        playerControllers.clear();
        inputController = null;
        outputController = null;
    }
}