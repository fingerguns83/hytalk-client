package net.fg83.hytalkclient.network;

import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.model.VoiceChatPlayer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class UdpAudioClient {
    private final ApplicationState applicationState;
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final int serverPort;

    private volatile boolean running = true;

    public UdpAudioClient(String host, int port, ApplicationState state) throws SocketException, UnknownHostException {
        this.serverAddress = InetAddress.getByName(host);
        this.serverPort = port;
        this.socket = new DatagramSocket(); // random local port
        this.applicationState = state;

        startReceiveThread();
    }

    public void sendAudio(int sequence, byte[] opusData, int length) throws IOException {

        VoiceChatPlayer player = applicationState.getPlayerManager().getClientPlayer();

        if (player == null){
            throw new IllegalStateException("ClientPlayer is null");
        }


        ByteBuffer buffer = ByteBuffer.allocate(23 + length);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.put((byte) 0); // packet type

        buffer.putLong(player.getPlayerId().getMostSignificantBits());
        buffer.putLong(player.getPlayerId().getLeastSignificantBits());

        buffer.putInt(sequence);
        buffer.putShort((short) length);

        buffer.put(opusData, 0, length);

        DatagramPacket packet = new DatagramPacket(
                buffer.array(),
                buffer.position(),
                serverAddress,
                serverPort
        );

        socket.send(packet);
    }

    private void startReceiveThread() {
        Thread thread = new Thread(() -> {
            byte[] buffer = new byte[1500]; // safe MTU
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    handlePacket(packet);

                } catch (Exception e) {
                    if (running) e.printStackTrace();
                }
            }
        }, "Voice-UDP-Receiver");

        thread.setDaemon(true);
        thread.start();
    }

    private void handlePacket(DatagramPacket packet) {

        ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
        buffer.order(ByteOrder.BIG_ENDIAN);

        byte type = buffer.get();
        if (type != 0) return; // only audio for now

        long msb = buffer.getLong();
        long lsb = buffer.getLong();
        UUID uuid = new UUID(msb, lsb);

        int sequence = buffer.getInt();
        int payloadLength = Short.toUnsignedInt(buffer.getShort());

        byte[] opusData = new byte[payloadLength];
        buffer.get(opusData);

        applicationState.getAudioStreamManager().onIncomingAudioPacket(uuid, sequence, opusData);
    }

    public boolean readyForSend(){
        return (applicationState != null && applicationState.getPlayerManager() != null && applicationState.getPlayerManager().getClientPlayer() != null);
    }

    public void shutdown() {
        running = false;
        socket.close();
    }
}

