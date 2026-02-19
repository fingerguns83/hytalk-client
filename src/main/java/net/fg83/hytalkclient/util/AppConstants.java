package net.fg83.hytalkclient.util;

import javax.sound.sampled.AudioFormat;

public class AppConstants {
    public static final String VERSION = "1.0.0-alpha";
    public static final int DEFAULT_SERVER_PORT = 5222;

    public static class Audio {
        public static final int SAMPLE_RATE = 48000;
        public static final int CHANNELS = 1;
        public static final int BIT_DEPTH = 16;
        public static final int FRAME_SIZE = 960; // 20ms
        public static final int MAX_BUFFER_SIZE = 50;
        public static final boolean SIGNED = true;
        public static final boolean BIG_ENDIAN = false;

        public static final int BYTES_PER_SAMPLE = BIT_DEPTH / 8;
        public static final int BYTES_PER_FRAME = FRAME_SIZE * BYTES_PER_SAMPLE;

        public static final AudioFormat INPUT_AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, BIT_DEPTH, 1, SIGNED, BIG_ENDIAN);
        public static final AudioFormat OUTPUT_AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, BIT_DEPTH, 2, SIGNED, BIG_ENDIAN);

    }

    public static class UI {

        public static class ChannelStrip {
            public static final double FADER_MIN_Y = 0;
            public static final double FADER_MAX_Y = 182;
            public static final double VU_LEVEL_MAX = 260;
        }
    }

}