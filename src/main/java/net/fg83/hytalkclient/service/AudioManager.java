package net.fg83.hytalkclient.service;

import java.util.UUID;

public class AudioManager {


    public void onAudioPacket(UUID uuid, int sequence, byte[] opusFrame) {
        /*playerAudioStreams
                .computeIfAbsent(uuid, u -> new PlayerAudioStream())
                .pushPacket(sequence, opusFrame);*/
    }
}
