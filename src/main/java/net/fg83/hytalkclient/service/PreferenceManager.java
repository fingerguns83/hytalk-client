package net.fg83.hytalkclient.service;

import java.util.prefs.Preferences;

/**
 * Manages application preferences and settings persistence
 */
public class PreferenceManager {

    private static final String PREF_INPUT_DEVICE_NAME = "audio.input.device.name";
    private static final String PREF_OUTPUT_DEVICE_NAME = "audio.output.device.name";
    private static final String PREF_INPUT_GAIN = "audio.input.gain";
    private static final String PREF_OUTPUT_GAIN = "audio.output.gain";

    private final Preferences prefs;

    public PreferenceManager() {
        // Store in user node so it persists per-user
        this.prefs = Preferences.userNodeForPackage(PreferenceManager.class);
    }

    // === Audio Device Preferences ===

    /**
     * Save the selected input device name
     */
    public void saveInputDevice(String deviceName) {
        if (deviceName != null) {
            prefs.put(PREF_INPUT_DEVICE_NAME, deviceName);
            System.out.println("Saved input device preference: " + deviceName);
        }
    }

    /**
     * Get the last selected input device name, or null if none saved
     */
    public String getInputDevice() {
        return prefs.get(PREF_INPUT_DEVICE_NAME, null);
    }

    /**
     * Save the selected output device name
     */
    public void saveOutputDevice(String deviceName) {
        if (deviceName != null) {
            prefs.put(PREF_OUTPUT_DEVICE_NAME, deviceName);
            System.out.println("Saved output device preference: " + deviceName);
        }
    }

    /**
     * Get the last selected output device name, or null if none saved
     */
    public String getOutputDevice() {
        return prefs.get(PREF_OUTPUT_DEVICE_NAME, null);
    }

    // === Gain Preferences ===

    /**
     * Save input gain (0.0 - 2.0)
     */
    public void saveInputGain(float gain) {
        prefs.putFloat(PREF_INPUT_GAIN, gain);
    }

    /**
     * Get saved input gain, defaults to 1.0 (unity)
     */
    public float getInputGain() {
        return prefs.getFloat(PREF_INPUT_GAIN, 1.0f);
    }

    /**
     * Save output gain (0.0 - 2.0)
     */
    public void saveOutputGain(float gain) {
        prefs.putFloat(PREF_OUTPUT_GAIN, gain);
    }

    /**
     * Get saved output gain, defaults to 1.0 (unity)
     */
    public float getOutputGain() {
        return prefs.getFloat(PREF_OUTPUT_GAIN, 1.0f);
    }

    // === Utility ===

    /**
     * Clear all preferences (useful for debugging/reset)
     */
    public void clearAll() {
        try {
            prefs.clear();
            System.out.println("Cleared all preferences");
        } catch (Exception e) {
            System.err.println("Failed to clear preferences: " + e.getMessage());
        }
    }
}