package net.fg83.hytalkclient.service;

import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.network.UdpAudioClient;
import net.fg83.hytalkclient.util.AppConstants;
import net.fg83.hytalkclient.util.WindowDimensions;
import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;
import org.concentus.OpusException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static net.fg83.hytalkclient.HytalkClientApplication.getView;

public class AudioNetworkManager {
    private UdpAudioClient udpClient;

    private OpusEncoder opusEncoder;
    private final short[] encoderBuffer = new short[AppConstants.Audio.FRAME_SIZE];
    private final byte[] opusPacket = new byte[1024];

    private final AtomicInteger sequenceNumber = new AtomicInteger(0);

    public AudioNetworkManager(){
        initializeOpusEncoder();
    }

    private void initializeOpusEncoder() {
        try {
            this.opusEncoder = new OpusEncoder(
                    AppConstants.Audio.SAMPLE_RATE,
                    AppConstants.Audio.CHANNELS,
                    OpusApplication.OPUS_APPLICATION_VOIP
            );
            // Configure for low latency voice
            this.opusEncoder.setBitrate(64000); // 64kbps
            this.opusEncoder.setComplexity(10);

            System.out.println("AudioNetworkManager: Opus encoder initialized");
        } catch (OpusException e) {
            System.err.println("Failed to create Opus encoder: " + e.getMessage());
        }
    }

    public void setupUdpClient(String serverHost, int serverPort, ApplicationState applicationState) {
        if (udpClient != null) {
            udpClient.shutdown();
        }
        try {
            this.udpClient = new UdpAudioClient(serverHost, serverPort, applicationState);
            this.sequenceNumber.set(0); // Reset sequence on new connection

            System.out.println("AudioNetworkManager: UDP client initialized for player ");
        } catch (Exception e) {
            handleUdpSetupFailure(e, applicationState);
        }

    }

    public void onCapturedFrame(byte[] pcmFrame) {
        if (udpClient == null || !udpClient.readyForSend() || opusEncoder == null) {
            return; // Not ready to send
        }

        try {
            // Convert PCM bytes to shorts for Opus encoder
            for (int i = 0; i < AppConstants.Audio.FRAME_SIZE; i++) {
                int byteIndex = i * AppConstants.Audio.BYTES_PER_SAMPLE;
                encoderBuffer[i] = (short) ((pcmFrame[byteIndex + 1] << 8) | (pcmFrame[byteIndex] & 0xFF));
            }

            // Encode to Opus
            int encodedBytes = opusEncoder.encode(
                    encoderBuffer,
                    0,
                    AppConstants.Audio.FRAME_SIZE,
                    opusPacket,
                    0,
                    opusPacket.length
            );

            if (encodedBytes > 0) {
                // Send via UDP with sequence number
                int seq = sequenceNumber.getAndIncrement();
                udpClient.sendAudio(seq, opusPacket, encodedBytes);
            }

        } catch (OpusException | IOException e) {
            System.err.println("Error encoding/sending audio: " + e.getMessage());
        }
    }

    public int getCurrentSequence() {
        return sequenceNumber.get();
    }

    /* UTILITY METHODS */
    private void handleUdpSetupFailure(Exception e, ApplicationState applicationState) {
        System.err.println("Failed to setup udp client: " + e.getMessage());
        applicationState.getErrorDialogManager().showError("Failed to setup UDP client", e.getMessage());

        try {
            applicationState.getViewNavigationManager().navigateToView(
                    getView("subviews/ConnectionView.fxml"),
                    null,
                    WindowDimensions.CONNECTION_WIDTH,
                    WindowDimensions.CONNECTION_HEIGHT
            );
        } catch (IOException e1) {
            applicationState.getErrorDialogManager().showError("View Error", "Failed to load view: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (udpClient != null) {
            udpClient.shutdown();
            udpClient = null;
        }
        sequenceNumber.set(0);

        System.out.println("AudioNetworkManager: Shutdown complete");
    }
}
