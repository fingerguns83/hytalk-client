
package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.util.AppConstants;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base class for all audio streams in the application.
 *
 * Provides common functionality:
 * - Audio level tracking for metering
 * - Gain control
 * - Mute control
 * - Running state management
 */
public abstract class AudioStream {

    // Audio level monitoring (0.0 - 1.0)
    protected final AtomicReference<Float> currentLevel = new AtomicReference<>(0.0f);

    // Gain control (0.0 - 1.25, where 1.0 = unity)
    protected final AtomicReference<Float> gain = new AtomicReference<>(1.0f);

    // Proximity attenuation trim (0.0 - 1.0, where 1.0 = full volume, independent of fader gain)
    protected final AtomicReference<Float> attenuation = new AtomicReference<>(0.0f);

    // Mute state
    protected final AtomicBoolean muted = new AtomicBoolean(false);

    // Running state (for streams with lifecycle)
    protected final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Get the current audio level for metering.
     * Thread-safe.
     *
     * @return Current audio level (0.0 - 1.0)
     */
    public float getCurrentLevel() {
        return muted.get() ? 0.0f : currentLevel.get();
    }

    /**
     * Set the gain multiplier.
     *
     * @param gain Gain multiplier (0.0 - 2.0, where 1.0 = unity)
     */
    public void setGain(float gain) {
        this.gain.set(Math.max(0.0f, Math.min(2.0f, gain)));
    }

    /**
     * Get the current gain multiplier.
     *
     * @return Current gain (0.0 - 2.0)
     */
    public float getGain() {
        return gain.get();
    }

    public void setAttenuation(float attenuation) {
        this.attenuation.set(Math.max(0.0f, Math.min(1.0f, attenuation)));
    }
    public float getAttenuation() {
        return attenuation.get();
    }


    /**
     * Set the mute state.
     *
     * @param muted true to mute, false to unmute
     */
    public void setMuted(boolean muted) {
        this.muted.set(muted);
    }

    /**
     * Get the mute state.
     *
     * @return true if muted, false otherwise
     */
    public boolean isMuted() {
        return muted.get();
    }

    /**
     * Check if the stream is currently running/active.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Update the current audio level with decay for smooth metering.
     * Should be called by subclasses when processing audio.
     *
     * @param newLevel The new peak level from current frame (0.0 - 1.0)
     */
    protected void updateLevel(float newLevel) {
        if (muted.get()) {
            currentLevel.set(0.0f);
        } else {
            // Smooth decay for better visualization
            float decayed = currentLevel.get() * 0.95f;
            currentLevel.set(Math.max(newLevel, decayed));
        }
    }

    /**
     * Calculate peak level from 16-bit PCM samples.
     * Utility method for subclasses.
     *
     * @param samples PCM sample bytes (little-endian 16-bit)
     * @return Peak level (0.0 - 1.0)
     */
    protected float calculatePeakLevel(byte[] samples) {
        float maxLevel = 0.0f;

        for (int i = 0; i < samples.length; i += AppConstants.Audio.BYTES_PER_SAMPLE) {
            short sample = (short) ((samples[i + 1] << 8) | (samples[i] & 0xFF));
            maxLevel = Math.max(maxLevel, Math.abs(sample) / 32768.0f);
        }

        return maxLevel;
    }

    /**
     * Apply gain to a single sample value and clamp to prevent clipping.
     * Utility method for subclasses.
     *
     * @param sample Input sample value
     * @param gain Gain multiplier
     * @return Gained and clamped sample
     */
    protected short applyGainToSample(short sample, float gain) {
        float gained = sample * gain * attenuation.get();
        gained = Math.max(-32768, Math.min(32767, gained));
        return (short) gained;
    }
    protected float applyGainToSample(float sample, float gain) {
        float gained = sample * gain * attenuation.get();
        gained = Math.max(-32768, Math.min(32767, gained));
        return (float) gained;
    }
}