
package net.fg83.hytalkclient.service;

import net.fg83.hytalkclient.audio.InputAudioStream;
import net.fg83.hytalkclient.audio.OutputAudioStream;
import net.fg83.hytalkclient.audio.PlayerAudioStream;
import org.concentus.OpusException;

import javax.sound.sampled.LineUnavailableException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AudioManager handles all audio processing and routing.
 *
 * Responsibilities:
 * - Manage audio I/O devices
 * - Own and lifecycle all audio streams
 * - Route incoming audio packets to player streams
 * - Coordinate input → network and network → output flow
 */
public class AudioManager {

    private final AudioIOManager audioIOManager;

    // Audio streams
    private InputAudioStream inputStream;
    private OutputAudioStream outputStream;
    private final Map<UUID, PlayerAudioStream> playerStreams = new ConcurrentHashMap<>();

    public AudioManager() {
        this.audioIOManager = new AudioIOManager();
    }

    // === Audio I/O Management ===

    public AudioIOManager getAudioIOManager() {
        return audioIOManager;
    }

    // === Input Stream Management ===

    /**
     * Start capturing audio from the input device
     */
    public void startInput() throws LineUnavailableException {
        if (inputStream != null && inputStream.isRunning()) {
            return;
        }

        AudioIOManager.AudioDevice device = audioIOManager.getSelectedInputDevice();
        if (device == null) {
            throw new LineUnavailableException("No input device selected");
        }

        float gain = audioIOManager.getInputGain();

        // Callback for captured PCM frames (will be Opus encoder later)
        inputStream = new InputAudioStream(device, gain, pcmFrame -> {
            // For now, just log periodically to avoid spam
            // In production, this will encode to Opus and send via UDP
        });

        inputStream.start();
        System.out.println("AudioManager: Started input stream");
    }

    /**
     * Stop capturing audio
     */
    public void stopInput() {
        if (inputStream != null) {
            inputStream.stop();
            inputStream = null;
            System.out.println("AudioManager: Stopped input stream");
        }
    }

    public InputAudioStream getInputStream() {
        return inputStream;
    }

    // === Output Stream Management ===

    /**
     * Start audio playback to the output device
     */
    public void startOutput() throws LineUnavailableException {
        if (outputStream != null && outputStream.isRunning()) {
            return;
        }

        AudioIOManager.AudioDevice device = audioIOManager.getSelectedOutputDevice();
        if (device == null) {
            throw new LineUnavailableException("No output device selected");
        }

        outputStream = new OutputAudioStream(device);
        outputStream.setGain(audioIOManager.getOutputGain());
        outputStream.start();

        System.out.println("AudioManager: Started output stream");
    }

    /**
     * Stop audio playback
     */
    public void stopOutput() {
        if (outputStream != null) {
            outputStream.stop();
            outputStream = null;
            System.out.println("AudioManager: Stopped output stream");
        }
    }

    public OutputAudioStream getOutputStream() {
        return outputStream;
    }

    // === Player Stream Management ===

    /**
     * Create a new player audio stream when a player joins
     */
    public void addPlayer(UUID playerId) {
        try {
            PlayerAudioStream stream = new PlayerAudioStream();
            playerStreams.put(playerId, stream);

            // Register with output mixer
            if (outputStream != null) {
                outputStream.addPlayerStream(playerId, stream);
            }

            System.out.println("AudioManager: Added player stream: " + playerId);
        } catch (OpusException e) {
            System.err.println("Failed to create player stream: " + e.getMessage());
        }
    }

    /**
     * Remove a player audio stream when a player leaves
     */
    public void removePlayer(UUID playerId) {
        PlayerAudioStream stream = playerStreams.remove(playerId);

        if (stream != null && outputStream != null) {
            outputStream.removePlayerStream(playerId);
            System.out.println("AudioManager: Removed player stream: " + playerId);
        }
    }

    /**
     * Called from UDP receiver when an audio packet arrives
     */
    public void onAudioPacket(UUID playerId, int sequence, byte[] opusFrame) {
        PlayerAudioStream stream = playerStreams.get(playerId);

        if (stream == null) {
            // Player stream doesn't exist yet - create it
            addPlayer(playerId);
            stream = playerStreams.get(playerId);
        }

        if (stream != null) {
            stream.pushPacket(sequence, opusFrame);
        }
    }

    public PlayerAudioStream getPlayerStream(UUID playerId) {
        return playerStreams.get(playerId);
    }

    public Map<UUID, PlayerAudioStream> getAllPlayerStreams() {
        return playerStreams;
    }

    // === Gain Control (forwarding to streams) ===

    public void setInputGain(float gain) {
        audioIOManager.setInputGain(gain);
        if (inputStream != null) {
            inputStream.setGain(gain);
        }
    }

    public void setOutputGain(float gain) {
        audioIOManager.setOutputGain(gain);
        if (outputStream != null) {
            outputStream.setGain(gain);
        }
    }

    public void setPlayerGain(UUID playerId, float gain) {
        PlayerAudioStream stream = playerStreams.get(playerId);
        if (stream != null) {
            stream.setGain(gain);
        }
    }

    // === Mute Control ===

    public void setInputMuted(boolean muted) {
        if (inputStream != null) {
            inputStream.setMuted(muted);
        }
    }

    public void setOutputMuted(boolean muted) {
        if (outputStream != null) {
            outputStream.setMuted(muted);
        }
    }

    public void setPlayerMuted(UUID playerId, boolean muted) {
        PlayerAudioStream stream = playerStreams.get(playerId);
        if (stream != null) {
            stream.setMuted(muted);
        }
    }

    // === Cleanup ===

    public void shutdown() {
        stopInput();
        stopOutput();
        playerStreams.clear();
        System.out.println("AudioManager: Shutdown complete");
    }
}