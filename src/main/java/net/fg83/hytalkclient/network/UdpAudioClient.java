// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.network;

import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.model.VoiceChatPlayer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * UDP client for handling real-time audio transmission and reception.
 * Manages bidirectional audio communication with the voice chat server using UDP datagrams.
 * Audio packets are encoded/decoded with a custom protocol including player UUID, sequence numbers, and Opus data.
 */
public class UdpAudioClient {
    // Application state providing access to player and audio stream managers
    private final ApplicationState applicationState;
    // UDP socket for sending and receiving audio packets
    private final DatagramSocket socket;
    // Server's IP address for audio transmission
    private final InetAddress serverAddress;
    // Server's UDP port for audio transmission
    private final int serverPort;

    // Flag to control the receive thread lifecycle (volatile for thread visibility)
    private volatile boolean running = true;

    /**
     * Creates a new UDP audio client and establishes connection to the server.
     * Initializes the socket, resolves the server address, and starts the receive thread.
     *
     * @param host  the hostname or IP address of the voice chat server
     * @param port  the UDP port of the voice chat server
     * @param state the application state providing access to managers
     * @throws SocketException      if the socket could not be opened
     * @throws UnknownHostException if the host address could not be resolved
     */
    public UdpAudioClient(String host, int port, ApplicationState state) throws SocketException, UnknownHostException {
        this.serverAddress = InetAddress.getByName(host);
        this.serverPort = port;
        this.socket = new DatagramSocket(); // random local port
        this.applicationState = state;

        // Start background thread for receiving incoming audio packets
        startReceiveThread();
    }

    /**
     * Sends an audio packet to the server containing Opus-encoded audio data.
     * Packet format: [1 byte type][16 bytes UUID][4 bytes sequence][2 bytes length][N bytes opus data]
     *
     * @param sequence the sequence number for packet ordering
     * @param opusData the Opus-encoded audio data buffer
     * @param length   the number of valid bytes in the opusData buffer
     * @throws IOException           if the packet could not be sent
     * @throws IllegalStateException if the client player is not available
     */
    public void sendAudio(int sequence, byte[] opusData, int length) throws IOException {

        // Get the current client player to obtain their UUID
        VoiceChatPlayer player = applicationState.getPlayerManager().getClientPlayer();

        // Ensure the client player exists before sending
        if (player == null) {
            throw new IllegalStateException("ClientPlayer is null");
        }

        // Allocate buffer for packet: 1 (type) + 16 (UUID) + 4 (sequence) + 2 (length) + audio data
        ByteBuffer buffer = ByteBuffer.allocate(23 + length);
        buffer.order(ByteOrder.BIG_ENDIAN);

        // Write packet type (0 = audio packet)
        buffer.put((byte) 0); // packet type

        // Write player UUID as two 64-bit longs (most significant bits first)
        buffer.putLong(player.getPlayerId().getMostSignificantBits());
        buffer.putLong(player.getPlayerId().getLeastSignificantBits());

        // Write sequence number for packet ordering and loss detection
        buffer.putInt(sequence);
        // Write payload length as unsigned short
        buffer.putShort((short) length);

        // Write the actual Opus-encoded audio data
        buffer.put(opusData, 0, length);

        // Create and send the UDP datagram packet to the server
        DatagramPacket packet = new DatagramPacket(
                buffer.array(),
                buffer.position(),
                serverAddress,
                serverPort
        );

        socket.send(packet);
    }

    /**
     * Starts a daemon thread to continuously receive and process incoming audio packets.
     * The thread runs until the 'running' flag is set to false or an error occurs.
     */
    private void startReceiveThread() {
        Thread thread = new Thread(() -> {
            // Buffer sized for safe MTU (Maximum Transmission Unit)
            byte[] buffer = new byte[1500]; // safe MTU
            // Continue receiving packets while the client is running
            while (running) {
                try {
                    // Create a datagram packet to receive incoming data
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    // Block until a packet is received
                    socket.receive(packet);

                    // Process the received audio packet
                    handlePacket(packet);

                }
                catch (Exception e) {
                    // Only print errors if the client is still running (ignore shutdown errors)
                    if (running) e.printStackTrace();
                }
            }
        }, "Voice-UDP-Receiver");

        // Set as daemon thread so it doesn't prevent JVM shutdown
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Processes an incoming audio packet by parsing its contents and forwarding to the audio stream manager.
     * Packet format: [1 byte type][16 bytes UUID][4 bytes sequence][2 bytes length][N bytes opus data]
     *
     * @param packet the received UDP datagram packet
     */
    private void handlePacket(DatagramPacket packet) {

        // Wrap the packet data in a ByteBuffer for easy parsing
        ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
        buffer.order(ByteOrder.BIG_ENDIAN);

        // Read and validate packet type
        byte type = buffer.get();
        if (type != 0) return; // only audio for now

        // Read the sender's UUID (16 bytes as two longs)
        long msb = buffer.getLong();
        long lsb = buffer.getLong();
        UUID uuid = new UUID(msb, lsb);

        // Read sequence number for packet ordering
        int sequence = buffer.getInt();
        // Read payload length as unsigned short
        int payloadLength = Short.toUnsignedInt(buffer.getShort());

        // Extract the Opus-encoded audio data
        byte[] opusData = new byte[payloadLength];
        buffer.get(opusData);

        // Forward the audio packet to the audio stream manager for playback
        applicationState.getAudioStreamManager().onIncomingAudioPacket(uuid, sequence, opusData);
    }

    /**
     * Checks if the client is ready to send audio packets.
     * Verifies that all required components (application state, player manager, client player) are available.
     *
     * @return true if ready to send audio, false otherwise
     */
    public boolean readyForSend() {
        return (applicationState != null && applicationState.getPlayerManager() != null && applicationState.getPlayerManager().getClientPlayer() != null);
    }

    /**
     * Shuts down the UDP audio client by stopping the receive thread and closing the socket.
     * This method should be called when disconnecting from the voice chat server.
     */
    public void shutdown() {
        // Signal the receive thread to stop
        running = false;
        // Close the socket (this will also interrupt the receive() call)
        socket.close();
    }
}