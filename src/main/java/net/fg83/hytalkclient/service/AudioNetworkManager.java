// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

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

/**
 * Manages audio encoding and network transmission for voice communication.
 * This class handles Opus encoding of PCM audio frames and sends them via UDP.
 */
public class AudioNetworkManager {
    // UDP client for sending audio packets to the server
    private UdpAudioClient udpClient;

    // Opus encoder instance for compressing audio data
    private OpusEncoder opusEncoder;
    // Buffer for storing PCM samples before encoding (16-bit samples)
    private final short[] encoderBuffer = new short[AppConstants.Audio.FRAME_SIZE];
    // Buffer for storing encoded Opus packets (up to 1024 bytes)
    private final byte[] opusPacket = new byte[1024];

    // Sequence number for audio packets to detect packet loss and reordering
    private final AtomicInteger sequenceNumber = new AtomicInteger(0);

    /**
     * Constructs a new AudioNetworkManager and initializes the Opus encoder.
     */
    public AudioNetworkManager() {
        initializeOpusEncoder();
    }

    /**
     * Initializes the Opus encoder with predefined settings for voice communication.
     * Configures the encoder for VOIP application with 64kbps bitrate and maximum complexity.
     */
    private void initializeOpusEncoder() {
        try {
            // Create Opus encoder with sample rate, channel count, and application type
            this.opusEncoder = new OpusEncoder(
                    AppConstants.Audio.SAMPLE_RATE,
                    AppConstants.Audio.CHANNELS,
                    OpusApplication.OPUS_APPLICATION_VOIP
            );
            // Set bitrate to 64kbps for good quality voice
            this.opusEncoder.setBitrate(64000); // 64kbps
            // Set maximum complexity (10) for best quality at the cost of CPU usage
            this.opusEncoder.setComplexity(10);

            System.out.println("AudioNetworkManager: Opus encoder initialized");
        } catch (OpusException e) {
            System.err.println("Failed to create Opus encoder: " + e.getMessage());
        }
    }

    /**
     * Sets up the UDP client for audio transmission to the specified server.
     * Shuts down any existing client before creating a new one.
     *
     * @param serverHost       the hostname or IP address of the server
     * @param serverPort       the port number to connect to
     * @param applicationState the application state for error handling and navigation
     */
    public void setupUdpClient(String serverHost, int serverPort, ApplicationState applicationState) {
        // Shutdown existing client if present
        if (udpClient != null) {
            udpClient.shutdown();
        }
        try {
            // Create new UDP client with server details
            this.udpClient = new UdpAudioClient(serverHost, serverPort, applicationState);
            // Reset sequence number for new connection
            this.sequenceNumber.set(0);

            System.out.println("AudioNetworkManager: UDP client initialized for player ");
        } catch (Exception e) {
            handleUdpSetupFailure(e, applicationState);
        }

    }

    /**
     * Processes a captured audio frame by encoding it with Opus and sending it over UDP.
     * Converts PCM bytes to 16-bit samples, encodes with Opus, and transmits the packet.
     *
     * @param pcmFrame the raw PCM audio data captured from the microphone
     */
    public void onCapturedFrame(byte[] pcmFrame) {
        // Check if UDP client is ready and encoder is available
        if (udpClient == null || !udpClient.readyForSend() || opusEncoder == null) {
            return;
        }


        try {
            // Convert PCM byte array to 16-bit short array (little-endian)
            for (int i = 0; i < AppConstants.Audio.FRAME_SIZE; i++) {
                int byteIndex = i * AppConstants.Audio.BYTES_PER_SAMPLE;
                // Combine two bytes into a 16-bit signed short (little-endian)
                encoderBuffer[i] = (short) ((pcmFrame[byteIndex + 1] << 8) | (pcmFrame[byteIndex] & 0xFF));
            }

            // Encode the PCM samples to Opus format
            int encodedBytes = opusEncoder.encode(
                    encoderBuffer,
                    0,
                    AppConstants.Audio.FRAME_SIZE,
                    opusPacket,
                    0,
                    opusPacket.length
            );

            // Send the encoded packet if encoding was successful
            if (encodedBytes > 0) {
                // Send via UDP with sequence number
                sequenceNumber.compareAndSet(Integer.MAX_VALUE, 0);
                int seq = sequenceNumber.getAndIncrement();

                udpClient.sendAudio(seq, opusPacket, encodedBytes);
            }

        } catch (OpusException | IOException e) {
            System.err.println("Error encoding/sending audio: " + e.getMessage());
        }
    }

    /**
     * Gets the current sequence number for audio packets.
     *
     * @return the current sequence number
     */
    public int getCurrentSequence() {
        return sequenceNumber.get();
    }

    /**
     * Handles UDP client setup failures by displaying an error dialog and
     * navigating back to the connection view.
     *
     * @param e                the exception that occurred during setup
     * @param applicationState the application state for error handling and navigation
     */
    private void handleUdpSetupFailure(Exception e, ApplicationState applicationState) {
        System.err.println("Failed to setup udp client: " + e.getMessage());
        // Show error dialog to user
        applicationState.getErrorDialogManager().showError("Failed to setup UDP client", e.getMessage());

        try {
            // Navigate back to connection view
            applicationState.getViewNavigationManager().navigateToView(
                    getView("subviews/ConnectionView.fxml"),
                    null,
                    WindowDimensions.CONNECTION_WIDTH,
                    WindowDimensions.CONNECTION_HEIGHT
            );
        } catch (IOException e1) {
            // Show error if navigation fails
            applicationState.getErrorDialogManager().showError("View Error", "Failed to load view: " + e.getMessage());
        }
    }

    /**
     * Shuts down the audio network manager by closing the UDP client
     * and resetting the sequence number.
     */
    public void shutdown() {
        // Shutdown and clean up UDP client
        if (udpClient != null) {
            udpClient.shutdown();
            udpClient = null;
        }
        // Reset sequence number
        sequenceNumber.set(0);

        System.out.println("AudioNetworkManager: Shutdown complete");
    }
}