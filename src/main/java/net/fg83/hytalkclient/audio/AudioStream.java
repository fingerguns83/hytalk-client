
package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.util.AppConstants;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AudioStream {

    protected final AtomicReference<Float> currentLevel = new AtomicReference<>(0.0f);

    protected final AtomicReference<Float> gain = new AtomicReference<>(1.0f);

    protected final AtomicReference<Float> attenuation = new AtomicReference<>(0.0f);

    protected final AtomicBoolean muted = new AtomicBoolean(false);

    protected final AtomicBoolean running = new AtomicBoolean(false);

    public float getCurrentLevel() {
        return muted.get() ? 0.0f : currentLevel.get();
    }

    public void setGain(float gain) {
        this.gain.set(Math.max(0.0f, Math.min(1.25f, gain)));
    }
    public float getGain() { return gain.get(); }

    public void setAttenuation(float attenuation) {
        this.attenuation.set(Math.max(0.0f, Math.min(1.0f, attenuation)));
    }
    public float getAttenuation() {
        return attenuation.get();
    }

    public void setMuted(boolean muted) {
        this.muted.set(muted);
    }

    public boolean isMuted() {
        return muted.get();
    }

    public boolean isRunning() {
        return running.get();
    }

    protected void updateLevel(float newLevel) {
        float decayed = currentLevel.get() * 0.95f;
        currentLevel.set(Math.max(newLevel, decayed));
    }

    protected float calculatePeakLevel(byte[] samples) {
        float maxLevel = 0.0f;

        for (int i = 0; i < samples.length; i += AppConstants.Audio.BYTES_PER_SAMPLE) {
            short sample = (short) ((samples[i + 1] << 8) | (samples[i] & 0xFF));
            maxLevel = Math.max(maxLevel, Math.abs(sample) / 32768.0f);
        }

        return maxLevel;
    }

    protected short applyGainToSample(short sample, float gain) {
        float gained = sample * gain * (isMuted() ? 0.0f : 1.0f);
        gained = Math.max(-32768, Math.min(32767, gained));
        return (short) gained;
    }

    protected float applyGainToSample(float sample, float gain) {
        float gained = sample * gain * attenuation.get() * (isMuted() ? 0.0f : 1.0f);
        gained = Math.max(-32768, Math.min(32767, gained));
        return gained;
    }
}