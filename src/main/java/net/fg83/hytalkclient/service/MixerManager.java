// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

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
 * Manages audio mixer UI controllers and updates their audio level meters.
 * Coordinates between audio streams and their corresponding UI channel strip controllers.
 */
public class MixerManager {

    // Audio stream manager that provides access to input, output, and player audio streams
    private final AudioStreamManager audioStreamManager;

    // Controller for the input channel strip UI
    private InputChannelStripController inputController;
    // Controller for the output channel strip UI
    private OutputChannelStripController outputController;
    // Map of player IDs to their corresponding channel strip controllers
    private final Map<UUID, ChannelStripController> playerControllers = new HashMap<>();

    // Timer that periodically updates all audio level meters
    private AnimationTimer meterTimer;
    // Flag indicating whether meter updates are currently running
    private boolean running = false;

    /**
     * Constructs a MixerManager with the specified audio stream manager.
     *
     * @param audioStreamManager The audio stream manager to retrieve stream data from
     */
    public MixerManager(AudioStreamManager audioStreamManager) {
        this.audioStreamManager = audioStreamManager;
    }


    /**
     * Sets the controller for the input channel strip.
     *
     * @param controller The input channel strip controller
     */
    public void setInputController(InputChannelStripController controller) {
        this.inputController = controller;
    }

    /**
     * Sets the controller for the output channel strip.
     *
     * @param controller The output channel strip controller
     */
    public void setOutputController(OutputChannelStripController controller) {
        this.outputController = controller;
    }

    /**
     * Adds a player's channel strip controller to the manager.
     *
     * @param playerId   The unique identifier of the player
     * @param controller The channel strip controller for this player
     */
    public void addPlayerController(UUID playerId, ChannelStripController controller) {
        playerControllers.put(playerId, controller);
    }

    /**
     * Removes a player's channel strip controller from the manager.
     *
     * @param playerId The unique identifier of the player to remove
     */
    public void removePlayerController(UUID playerId) {
        playerControllers.remove(playerId);
    }


    /**
     * Starts periodic meter updates for all audio channels.
     * Uses JavaFX AnimationTimer to update meters on each frame.
     * Does nothing if meter updates are already running.
     */
    public void startMeterUpdates() {
        // Prevent starting multiple timers
        if (running) {
            return;
        }

        running = true;
        // Create an animation timer that calls updateAllMeters on each frame
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
     * Stops periodic meter updates for all audio channels.
     * Cleans up the animation timer if it exists.
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
     * Updates the audio level meters for all channels (input, players, output).
     * Retrieves current audio levels from streams and updates corresponding UI controllers.
     */
    private void updateAllMeters() {
        // Update input channel meter
        InputAudioStream inputStream = audioStreamManager.getInputStream();
        if (inputController != null && inputStream != null && inputStream.isRunning()) {
            float level = inputStream.getCurrentLevel();
            inputController.updateMeter(level);
        }

        // Update all player channel meters
        playerControllers.forEach((playerId, controller) -> {
            PlayerAudioStream stream = audioStreamManager.getPlayerStream(playerId);
            if (stream != null && stream.isPlaying()) {
                float level = stream.getCurrentLevel();
                controller.updateMeter(level);
            }
        });

        // Update output channel meter
        OutputAudioStream outputStream = audioStreamManager.getOutputStream();
        if (outputController != null && outputStream != null && outputStream.isRunning()) {
            float level = outputStream.getCurrentLevel();
            outputController.updateMeter(level);
        }
    }


    /**
     * Shuts down the mixer manager, stopping all meter updates and clearing all controllers.
     * Should be called when the mixer is no longer needed.
     */
    public void shutdown() {
        stopMeterUpdates();
        playerControllers.clear();
        inputController = null;
        outputController = null;
    }
}