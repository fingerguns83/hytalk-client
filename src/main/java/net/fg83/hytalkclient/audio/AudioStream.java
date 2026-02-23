package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.util.AppConstants;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base class for audio streams providing common audio processing functionality.
 * Handles volume control, muting, level monitoring, and sample manipulation.
 */
public abstract class AudioStream {

    // Thread-safe reference to the current audio level (0.0 to 1.0)
    protected final AtomicReference<Float> currentLevel = new AtomicReference<>(0.0f);

    // Thread-safe reference to the gain/volume multiplier (0.0 to 1.25)
    protected final AtomicReference<Float> gain = new AtomicReference<>(1.0f);

    // Thread-safe reference to the attenuation multiplier (0.0 to 1.0)
    protected final AtomicReference<Float> attenuation = new AtomicReference<>(0.0f);

    // Thread-safe flag indicating whether the stream is muted
    protected final AtomicBoolean muted = new AtomicBoolean(false);

    // Thread-safe flag indicating whether the stream is currently running
    protected final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Gets the current audio level.
     *
     * @return The current level (0.0 to 1.0), or 0.0 if muted
     */
    public float getCurrentLevel() {
        return muted.get() ? 0.0f : currentLevel.get();
    }

    /**
     * Sets the gain/volume multiplier.
     *
     * @param gain The desired gain value, clamped between 0.0 and 1.25
     */
    public void setGain(float gain) {
        this.gain.set(Math.max(0.0f, Math.min(1.25f, gain)));
    }

    /**
     * Gets the current gain/volume multiplier.
     *
     * @return The current gain value
     */
    public float getGain() {
        return gain.get();
    }

    /**
     * Sets the attenuation multiplier.
     *
     * @param attenuation The desired attenuation value, clamped between 0.0 and 1.0
     */
    public void setAttenuation(float attenuation) {
        this.attenuation.set(Math.max(0.0f, Math.min(1.0f, attenuation)));
    }

    /**
     * Gets the current attenuation multiplier.
     *
     * @return The current attenuation value
     */
    public float getAttenuation() {
        return attenuation.get();
    }

    /**
     * Sets the muted state of the stream.
     *
     * @param muted True to mute the stream, false to unmute
     */
    public void setMuted(boolean muted) {
        this.muted.set(muted);
    }

    /**
     * Checks if the stream is currently muted.
     *
     * @return True if muted, false otherwise
     */
    public boolean isMuted() {
        return muted.get();
    }

    /**
     * Checks if the stream is currently running.
     *
     * @return True if running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Updates the current level with decay applied to the previous level.
     * The new level is the maximum of the new value or 95% of the previous level.
     *
     * @param newLevel The new audio level to consider
     */
    protected void updateLevel(float newLevel) {
        // Apply 95% decay to the previous level
        float decayed = currentLevel.get() * 0.95f;
        // Set the level to the greater of the new level or decayed level
        currentLevel.set(Math.max(newLevel, decayed));
    }

    /**
     * Calculates the peak audio level from a byte array of audio samples.
     *
     * @param samples The audio sample data as bytes (16-bit little-endian)
     * @return The peak level normalized to 0.0 to 1.0 range
     */
    protected float calculatePeakLevel(byte[] samples) {
        float maxLevel = 0.0f;

        // Iterate through samples in pairs of bytes (16-bit samples)
        for (int i = 0; i < samples.length; i += AppConstants.Audio.BYTES_PER_SAMPLE) {
            // Reconstruct 16-bit signed sample from two bytes (little-endian)
            short sample = (short) ((samples[i + 1] << 8) | (samples[i] & 0xFF));
            // Normalize to 0.0-1.0 range and track the maximum
            maxLevel = Math.max(maxLevel, Math.abs(sample) / 32768.0f);
        }

        return maxLevel;
    }

    /**
     * Applies gain to a 16-bit audio sample, respecting mute state.
     *
     * @param sample The input sample as a signed 16-bit integer
     * @param gain   The gain multiplier to apply
     * @return The processed sample, clamped to 16-bit range
     */
    protected short applyGainToSample(short sample, float gain) {
        // Apply gain, and zero out if muted
        float gained = sample * gain * (isMuted() ? 0.0f : 1.0f);
        // Clamp to 16-bit signed integer range
        gained = Math.max(-32768, Math.min(32767, gained));
        return (short) gained;
    }

    /**
     * Applies gain and attenuation to a floating-point audio sample, respecting mute state.
     *
     * @param sample The input sample as a float
     * @param gain   The gain multiplier to apply
     * @return The processed sample, clamped to 16-bit range
     */
    protected float applyGainToSample(float sample, float gain) {
        // Apply gain, attenuation, and zero out if muted
        float gained = sample * gain * attenuation.get() * (isMuted() ? 0.0f : 1.0f);
        // Clamp to 16-bit signed integer range
        gained = Math.max(-32768, Math.min(32767, gained));
        return gained;
    }
}