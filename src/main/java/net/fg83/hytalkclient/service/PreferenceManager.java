package net.fg83.hytalkclient.service;

import java.util.prefs.Preferences;

/**
 * Manages user preferences for audio device settings and gain controls.
 * Uses Java Preferences API to persist settings across application sessions.
 */
public class PreferenceManager {

    // Preference key for the input audio device name
    private static final String PREF_INPUT_DEVICE_NAME = "audio.input.device.name";
    // Preference key for the output audio device name
    private static final String PREF_OUTPUT_DEVICE_NAME = "audio.output.device.name";
    // Preference key for the input audio gain level
    private static final String PREF_INPUT_GAIN = "audio.input.gain";
    // Preference key for the output audio gain level
    private static final String PREF_OUTPUT_GAIN = "audio.output.gain";

    // Java Preferences instance for storing user preferences
    private final Preferences prefs;

    /**
     * Constructs a PreferenceManager and initializes the Preferences node.
     * Uses the user node for this package to store preferences.
     */
    public PreferenceManager() {
        this.prefs = Preferences.userNodeForPackage(PreferenceManager.class);
    }

    /**
     * Saves the input audio device name to preferences.
     *
     * @param deviceName The name of the input device to save, or null to skip saving
     */
    public void saveInputDevice(String deviceName) {
        // Only save if deviceName is not null
        if (deviceName != null) {
            prefs.put(PREF_INPUT_DEVICE_NAME, deviceName);
            System.out.println("Saved input device preference: " + deviceName);
        }
    }

    /**
     * Retrieves the saved input audio device name from preferences.
     *
     * @return The saved input device name, or null if not set
     */
    public String getInputDevice() {
        return prefs.get(PREF_INPUT_DEVICE_NAME, null);
    }

    /**
     * Saves the output audio device name to preferences.
     *
     * @param deviceName The name of the output device to save, or null to skip saving
     */
    public void saveOutputDevice(String deviceName) {
        // Only save if deviceName is not null
        if (deviceName != null) {
            prefs.put(PREF_OUTPUT_DEVICE_NAME, deviceName);
            System.out.println("Saved output device preference: " + deviceName);
        }
    }

    /**
     * Retrieves the saved output audio device name from preferences.
     *
     * @return The saved output device name, or null if not set
     */
    public String getOutputDevice() {
        return prefs.get(PREF_OUTPUT_DEVICE_NAME, null);
    }

    /**
     * Saves the input audio gain level to preferences.
     *
     * @param gain The input gain level to save
     */
    public void saveInputGain(float gain) {
        prefs.putFloat(PREF_INPUT_GAIN, gain);
    }

    /**
     * Retrieves the saved input audio gain level from preferences.
     *
     * @return The saved input gain level, or 1.0f (default) if not set
     */
    public float getInputGain() {
        return prefs.getFloat(PREF_INPUT_GAIN, 1.0f);
    }

    /**
     * Saves the output audio gain level to preferences.
     *
     * @param gain The output gain level to save
     */
    public void saveOutputGain(float gain) {
        prefs.putFloat(PREF_OUTPUT_GAIN, gain);
    }

    /**
     * Retrieves the saved output audio gain level from preferences.
     *
     * @return The saved output gain level, or 1.0f (default) if not set
     */
    public float getOutputGain() {
        return prefs.getFloat(PREF_OUTPUT_GAIN, 1.0f);
    }

    /**
     * Clears all saved preferences.
     * Removes all stored audio device and gain settings.
     */
    public void clearAll() {
        try {
            // Clear all preferences from this node
            prefs.clear();
            System.out.println("Cleared all preferences");
        } catch (Exception e) {
            // Log any errors that occur during clearing
            System.err.println("Failed to clear preferences: " + e.getMessage());
        }
    }
}