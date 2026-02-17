package net.fg83.hytalkclient.audio;

import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;
import org.concentus.OpusException;

import static net.fg83.hytalkclient.util.AppConstants.Audio.*;

public class ClientAudioStream {

    private final OpusEncoder encoder;

    public ClientAudioStream() throws OpusException {
        this.encoder = new OpusEncoder(SAMPLE_RATE, CHANNELS, OpusApplication.OPUS_APPLICATION_VOIP);
    }


}
