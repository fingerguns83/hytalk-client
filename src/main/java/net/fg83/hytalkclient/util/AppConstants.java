// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.util;

import javax.sound.sampled.AudioFormat;

/**
 * Application-wide constants for the HyTalk client.
 * Contains configuration values for versioning, networking, audio, and UI components.
 */
public class AppConstants {
    // Application version string
    public static final String VERSION = "1.0.3-alpha";
    // Protocol version for client-server communication
    public static final String PROTO_VERSION = "1.1";
    // Default port for server connections
    public static final int DEFAULT_SERVER_PORT = 5222;

    /**
     * Audio-related constants for configuring audio capture, processing, and playback.
     */
    public static class Audio {
        // Audio sample rate in Hz (48kHz for high-quality audio)
        public static final int SAMPLE_RATE = 48000;
        // Number of audio channels (1 for mono)
        public static final int CHANNELS = 1;
        // Bit depth for audio samples (16-bit audio)
        public static final int BIT_DEPTH = 16;
        // Number of samples per audio frame (960 samples = 20ms at 48kHz)
        public static final int FRAME_SIZE = 960; // 20ms
        // Maximum number of audio frames to buffer
        public static final int MAX_BUFFER_SIZE = 50;
        // Whether audio samples are signed values
        public static final boolean SIGNED = true;
        // Whether to use big-endian byte order (false for little-endian)
        public static final boolean BIG_ENDIAN = false;

        // Number of bytes per audio sample (2 bytes for 16-bit)
        public static final int BYTES_PER_SAMPLE = BIT_DEPTH / 8;
        // Total bytes per audio frame (frame size * bytes per sample)
        public static final int BYTES_PER_FRAME = FRAME_SIZE * BYTES_PER_SAMPLE;

        // Audio format for input (microphone) - mono channel
        public static final AudioFormat INPUT_AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, BIT_DEPTH, 1, SIGNED, BIG_ENDIAN);
        // Audio format for output (speakers) - stereo (2 channels)
        public static final AudioFormat OUTPUT_AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, BIT_DEPTH, 2, SIGNED, BIG_ENDIAN);

    }

    /**
     * UI-related constants for configuring user interface components.
     */
    public static class UI {

        /**
         * Constants for the channel strip component in the mixer view.
         */
        public static class ChannelStrip {
            // Minimum Y-coordinate for the fader control (top position)
            public static final double FADER_MIN_Y = 0;
            // Maximum Y-coordinate for the fader control (bottom position)
            public static final double FADER_MAX_Y = 182;
            // Maximum value for the VU (volume unit) meter display
            public static final double VU_LEVEL_MAX = 260;
        }
    }

}