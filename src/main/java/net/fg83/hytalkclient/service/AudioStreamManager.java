
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
 * AudioManager handles all audio processing and routing.
 * Responsibilities:
 * - Manage audio I/O devices
 * - Own and lifecycle all audio streams
 * - Route incoming audio packets to player streams
 * - Coordinate input → network and network → output flow
 */
public class AudioStreamManager {

    private final AudioIOManager audioIOManager;

    private InputAudioStream inputStream;
    private OutputAudioStream outputStream;
    private final Map<UUID, PlayerAudioStream> playerStreams = new ConcurrentHashMap<>();

    private int attenuationDistance = 48;

    Consumer<byte[]> inputConsumer;

    public AudioStreamManager(PreferenceManager preferenceManager) {
        this.audioIOManager = new AudioIOManager(preferenceManager);
    }

    public AudioIOManager getAudioIOManager() {
        return audioIOManager;
    }



    /* INPUT STREAM METHODS */

    public void startInput(Consumer<byte[]> inputConsumer) throws LineUnavailableException {
        if (inputStream != null && inputStream.isRunning()) {
            return;
        }

        AudioIOManager.AudioDevice device = audioIOManager.getSelectedInputDevice();
        if (device == null) {
            throw new LineUnavailableException("No input device selected");
        }

        System.out.println("AudioManager: Starting input with device: " + device.name());

        float gain = audioIOManager.getInputGain();

        this.inputConsumer = inputConsumer;

        // Callback for captured PCM frames (will be Opus encoder later)
        inputStream = new InputAudioStream(device, gain, inputConsumer);

        inputStream.start();
        System.out.println("AudioManager: Started input stream");
    }

    public void stopInput() {
        if (inputStream != null) {
            inputStream.stop();
            inputStream = null;
            System.out.println("AudioManager: Stopped input stream");
        }
    }

    public void restartInput() throws LineUnavailableException {
        System.out.println("AudioManager: Restarting input stream");
        stopInput();
        startInput(inputConsumer);
    }

    public void setInputGain(float gain) {
        audioIOManager.setInputGain(gain);
        if (inputStream != null) {
            inputStream.setGain(gain);
        }
    }

    public void setInputMuted(boolean muted) {
        if (inputStream != null) {
            inputStream.setMuted(muted);
        }
    }

    public InputAudioStream getInputStream() {
        return inputStream;
    }



    /* OUTPUT STREAM METHODS */

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

    public void stopOutput() {
        if (outputStream != null) {
            outputStream.stop();
            outputStream = null;
            System.out.println("AudioManager: Stopped output stream");
        }
    }

    public void restartOutput() throws LineUnavailableException {
        System.out.println("AudioManager: Restarting output stream");
        stopOutput();
        startOutput();
    }

    public OutputAudioStream getOutputStream() {
        return outputStream;
    }

    public void setOutputGain(float gain) {
        audioIOManager.setOutputGain(gain);
        if (outputStream != null) {
            outputStream.setGain(gain);
        }
    }

    public void setOutputMuted(boolean muted) {
        if (outputStream != null) {
            outputStream.setMuted(muted);
        }
    }



    /* PLAYER STREAM METHODS */

    public PlayerAudioStream addPlayer(UUID playerId) {
        try {
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

    public void removePlayer(UUID playerId) {
        PlayerAudioStream stream = playerStreams.remove(playerId);

        if (stream != null && outputStream != null) {
            outputStream.removePlayerStream(playerId);
            System.out.println("AudioManager: Removed player stream: " + playerId);
        }
    }

    public PlayerAudioStream getPlayerStream(UUID playerId) {
        return playerStreams.get(playerId);
    }

    public Map<UUID, PlayerAudioStream> getAllPlayerStreams() {
        return playerStreams;
    }

    public void setPlayerGain(UUID playerId, float gain) {
        PlayerAudioStream stream = playerStreams.get(playerId);
        if (stream != null) {
            stream.setGain(gain);
        }
    }

    public void setPlayerMuted(UUID playerId, boolean muted) {
        PlayerAudioStream stream = playerStreams.get(playerId);
        if (stream != null) {
            stream.setMuted(muted);
        }
    }

    /* UDP AUDIO METHODS */


    public void onIncomingAudioPacket(UUID playerId, int sequence, byte[] opusFrame) {
        PlayerAudioStream stream = playerStreams.get(playerId);
        if (stream == null) {
            stream = addPlayer(playerId);
        }
        if (stream != null) {
            stream.pushPacket(sequence, opusFrame);
        }
    }



    /* UTILITY METHODS */

    public void shutdown() {
        stopInput();
        stopOutput();
        playerStreams.clear();
        System.out.println("AudioManager: Shutdown complete");
    }

    public void setAttenuationDistance(int attenuationDistance) {
        this.attenuationDistance = attenuationDistance;
    }

    public void updatePlayerAttenuation(Map<UUID, VoiceChatPlayer> voiceChatPlayers) {
        for (Map.Entry<UUID, VoiceChatPlayer> entry : voiceChatPlayers.entrySet()) {
            UUID playerId = entry.getKey();
            VoiceChatPlayer player = entry.getValue();
            PlayerAudioStream stream = playerStreams.get(playerId);
            float attenuation = player.calculateAttenuation(attenuationDistance);
            if (stream != null) {
                stream.setAttenuation(player.calculateAttenuation(attenuationDistance));
                System.out.println("Set attenuation to " + attenuation + " for " + player.getPlayerName());
            }
        }
    }
}