// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.service;

import net.fg83.hytalkclient.audio.InputAudioStream;
import net.fg83.hytalkclient.audio.OutputAudioStream;
import net.fg83.hytalkclient.audio.PlayerAudioStream;
import net.fg83.hytalkclient.model.VoiceChatPlayer;
import org.concentus.OpusException;

import javax.sound.sampled.LineUnavailableException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages audio streams for voice chat functionality, including input (microphone),
 * output (speakers), and per-player audio streams.
 */
public class AudioStreamManager {

    // Manages audio device selection and gain settings
    private final AudioIOManager audioIOManager;

    // Stream for capturing microphone input
    private InputAudioStream inputStream;
    // Stream for playing audio through speakers
    private OutputAudioStream outputStream;
    // Map of player UUIDs to their individual audio streams
    private final Map<UUID, PlayerAudioStream> playerStreams = new ConcurrentHashMap<>();

    // Distance in blocks at which audio starts to attenuate
    private int attenuationDistance = 48;

    // Consumer that processes captured audio data from the input stream
    Consumer<byte[]> inputConsumer;

    /**
     * Constructs a new AudioStreamManager with the given preference manager.
     *
     * @param preferenceManager The preference manager for audio device settings
     */
    public AudioStreamManager(PreferenceManager preferenceManager) {
        this.audioIOManager = new AudioIOManager(preferenceManager);
    }

    /**
     * Gets the audio I/O manager for device configuration.
     *
     * @return The AudioIOManager instance
     */
    public AudioIOManager getAudioIOManager() {
        return audioIOManager;
    }


    /**
     * Starts capturing audio from the selected input device.
     *
     * @param inputConsumer Consumer that receives captured audio data
     * @throws LineUnavailableException If no input device is selected or available
     */
    public void startInput(Consumer<byte[]> inputConsumer) throws LineUnavailableException {
        // Prevent starting if already running
        if (inputStream != null && inputStream.isRunning()) {
            return;
        }

        // Get the selected input device
        AudioIOManager.AudioDevice device = audioIOManager.getSelectedInputDevice();
        if (device == null) {
            throw new LineUnavailableException("No input device selected");
        }

        System.out.println("AudioManager: Starting input with device: " + device.name());

        // Get the current input gain setting
        float gain = audioIOManager.getInputGain();

        // Store the consumer for potential restarts
        this.inputConsumer = inputConsumer;

        // Create and start the input stream
        inputStream = new InputAudioStream(device, gain, inputConsumer);

        inputStream.start();
        System.out.println("AudioManager: Started input stream");
    }

    /**
     * Stops the input audio stream and releases resources.
     */
    public void stopInput() {
        if (inputStream != null) {
            inputStream.stop();
            inputStream = null;
            System.out.println("AudioManager: Stopped input stream");
        }
    }

    /**
     * Restarts the input stream with the current settings.
     *
     * @throws LineUnavailableException If the input device is unavailable
     */
    public void restartInput() throws LineUnavailableException {
        System.out.println("AudioManager: Restarting input stream");
        stopInput();
        startInput(inputConsumer);
    }

    /**
     * Sets the gain (volume) for the input stream.
     *
     * @param gain The gain value to apply
     */
    public void setInputGain(float gain) {
        audioIOManager.setInputGain(gain);
        if (inputStream != null) {
            inputStream.setGain(gain);
        }
    }

    /**
     * Mutes or unmutes the input stream.
     *
     * @param muted True to mute, false to unmute
     */
    public void setInputMuted(boolean muted) {
        if (inputStream != null) {
            inputStream.setMuted(muted);
        }
    }

    /**
     * Gets the current input audio stream.
     *
     * @return The InputAudioStream instance, or null if not started
     */
    public InputAudioStream getInputStream() {
        return inputStream;
    }


    /**
     * Starts the output audio stream for playing audio through speakers.
     *
     * @throws LineUnavailableException If no output device is selected or available
     */
    public void startOutput() throws LineUnavailableException {
        // Prevent starting if already running
        if (outputStream != null && outputStream.isRunning()) {
            return;
        }

        // Get the selected output device
        AudioIOManager.AudioDevice device = audioIOManager.getSelectedOutputDevice();
        if (device == null) {
            throw new LineUnavailableException("No output device selected");
        }

        // Create, configure, and start the output stream
        outputStream = new OutputAudioStream(device);
        outputStream.setGain(audioIOManager.getOutputGain());
        outputStream.start();

        System.out.println("AudioManager: Started output stream");
    }

    /**
     * Stops the output audio stream and releases resources.
     */
    public void stopOutput() {
        if (outputStream != null) {
            outputStream.stop();
            outputStream = null;
            System.out.println("AudioManager: Stopped output stream");
        }
    }

    /**
     * Restarts the output stream with the current settings.
     *
     * @throws LineUnavailableException If the output device is unavailable
     */
    public void restartOutput() throws LineUnavailableException {
        System.out.println("AudioManager: Restarting output stream");
        stopOutput();
        startOutput();
    }

    /**
     * Gets the current output audio stream.
     *
     * @return The OutputAudioStream instance, or null if not started
     */
    public OutputAudioStream getOutputStream() {
        return outputStream;
    }

    /**
     * Sets the gain (volume) for the output stream.
     *
     * @param gain The gain value to apply
     */
    public void setOutputGain(float gain) {
        audioIOManager.setOutputGain(gain);
        if (outputStream != null) {
            outputStream.setGain(gain);
        }
    }

    /**
     * Mutes or unmutes the output stream.
     *
     * @param muted True to mute, false to unmute
     */
    public void setOutputMuted(boolean muted) {
        if (outputStream != null) {
            outputStream.setMuted(muted);
        }
    }


    /**
     * Adds a new player audio stream for the specified player.
     *
     * @param playerId The UUID of the player
     * @return The created PlayerAudioStream, or null if creation fails
     */
    public PlayerAudioStream addPlayer(UUID playerId) {
        try {
            // Create a new player stream
            PlayerAudioStream stream = new PlayerAudioStream();
            playerStreams.put(playerId, stream);

            // Register with output mixer
            if (outputStream != null) {
                if (!outputStream.getPlayerStreams().containsKey(playerId)) {
                    outputStream.addPlayerStream(playerId, stream);
                    System.out.println("AudioManager: Added player stream: " + playerId);
                }
            }
            return stream;
        } catch (OpusException e) {
            System.err.println("Failed to create player stream: " + e.getMessage());

            return null;
        }
    }

    /**
     * Removes a player's audio stream.
     *
     * @param playerId The UUID of the player to remove
     */
    public void removePlayer(UUID playerId) {
        // Remove from the map
        PlayerAudioStream stream = playerStreams.remove(playerId);

        // Remove from output mixer if present
        if (stream != null && outputStream != null) {
            outputStream.removePlayerStream(playerId);
            System.out.println("AudioManager: Removed player stream: " + playerId);
        }
    }

    /**
     * Gets the audio stream for a specific player.
     *
     * @param playerId The UUID of the player
     * @return The PlayerAudioStream, or null if not found
     */
    public PlayerAudioStream getPlayerStream(UUID playerId) {
        return playerStreams.get(playerId);
    }

    /**
     * Gets all player audio streams.
     *
     * @return Map of player UUIDs to their audio streams
     */
    public Map<UUID, PlayerAudioStream> getAllPlayerStreams() {
        return playerStreams;
    }

    /**
     * Sets the gain (volume) for a specific player's audio stream.
     *
     * @param playerId The UUID of the player
     * @param gain     The gain value to apply
     */
    public void setPlayerGain(UUID playerId, float gain) {
        PlayerAudioStream stream = playerStreams.get(playerId);
        if (stream != null) {
            stream.setGain(gain);
        }
    }

    /**
     * Mutes or unmutes a specific player's audio stream.
     *
     * @param playerId The UUID of the player
     * @param muted    True to mute, false to unmute
     */
    public void setPlayerMuted(UUID playerId, boolean muted) {
        PlayerAudioStream stream = playerStreams.get(playerId);
        if (stream != null) {
            stream.setMuted(muted);
        }
    }


    /**
     * Handles incoming audio packets from a player.
     * Creates a new player stream if one doesn't exist.
     *
     * @param playerId  The UUID of the player
     * @param sequence  The packet sequence number
     * @param opusFrame The Opus-encoded audio data
     */
    public void onIncomingAudioPacket(UUID playerId, int sequence, byte[] opusFrame) {
        PlayerAudioStream stream = playerStreams.get(playerId);
        // Create stream if it doesn't exist
        if (stream == null) {
            stream = addPlayer(playerId);
        }
        // Push the packet to the stream
        if (stream != null) {
            stream.pushPacket(sequence, opusFrame);
        }
    }


    /**
     * Shuts down all audio streams and clears player data.
     */
    public void shutdown() {
        stopInput();
        stopOutput();
        playerStreams.clear();
        System.out.println("AudioManager: Shutdown complete");
    }

    /**
     * Sets the distance at which audio starts to attenuate.
     *
     * @param attenuationDistance The distance in blocks
     */
    public void setAttenuationDistance(int attenuationDistance) {
        this.attenuationDistance = attenuationDistance;
    }

    /**
     * Updates audio attenuation for all players based on their positions.
     *
     * @param voiceChatPlayers Map of player UUIDs to VoiceChatPlayer objects
     */
    public void updatePlayerAttenuation(Map<UUID, VoiceChatPlayer> voiceChatPlayers) {
        for (Map.Entry<UUID, VoiceChatPlayer> entry : voiceChatPlayers.entrySet()) {
            UUID playerId = entry.getKey();
            VoiceChatPlayer player = entry.getValue();
            PlayerAudioStream stream = playerStreams.get(playerId);
            // Calculate attenuation based on distance
            float attenuation = player.calculateAttenuation(attenuationDistance);
            // Apply attenuation if stream exists
            if (stream != null) {
                stream.setAttenuation(player.calculateAttenuation(attenuationDistance));
            }
        }
    }
}