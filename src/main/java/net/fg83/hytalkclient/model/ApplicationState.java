// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.model;


import net.fg83.hytalkclient.service.*;

/**
 * Central application state container that manages all service instances.
 * This class follows a singleton-like pattern to provide access to all major
 * application services from a single point.
 */
public class ApplicationState {
    // Manager for displaying error dialogs to the user
    private final ErrorDialogManager errorDialogManager = new ErrorDialogManager();
    // Manager for persisting and retrieving user preferences
    private final PreferenceManager preferenceManager = new PreferenceManager();
    // Manager for network connections (requires reference to this ApplicationState)
    private final ConnectionManager connectionManager = new ConnectionManager(this);
    // Manager for device pairing operations
    private final PairingManager pairingManager = new PairingManager();
    // Manager for player state and information
    private final PlayerManager playerManager = new PlayerManager();
    // Manager for audio streaming operations (uses preferences for device settings)
    private final AudioStreamManager audioStreamManager = new AudioStreamManager(preferenceManager);
    // Manager for audio network communication
    private final AudioNetworkManager audioNetworkManager = new AudioNetworkManager();
    // Manager for audio mixing operations (uses audio stream manager)
    private final MixerManager mixerManager = new MixerManager(audioStreamManager);

    // Manager for view navigation (set after initialization)
    private ViewNavigationManager viewNavigationManager;


    /**
     * Gets the connection manager for network operations
     *
     * @return the connection manager instance
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Gets the pairing manager for device pairing operations
     *
     * @return the pairing manager instance
     */
    public PairingManager getPairingManager() {
        return pairingManager;
    }

    /**
     * Gets the audio stream manager for audio I/O operations
     *
     * @return the audio stream manager instance
     */
    public AudioStreamManager getAudioStreamManager() {
        return audioStreamManager;
    }

    /**
     * Gets the audio network manager for network audio communication
     *
     * @return the audio network manager instance
     */
    public AudioNetworkManager getAudioNetworkManager() {
        return audioNetworkManager;
    }

    /**
     * Gets the mixer manager for audio mixing operations
     *
     * @return the mixer manager instance
     */
    public MixerManager getMixerManager() {
        return mixerManager;
    }

    /**
     * Gets the player manager for player state management
     *
     * @return the player manager instance
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Gets the error dialog manager for displaying error messages
     *
     * @return the error dialog manager instance
     */
    public ErrorDialogManager getErrorDialogManager() {
        return errorDialogManager;
    }

    /**
     * Gets the view navigation manager for UI navigation
     *
     * @return the view navigation manager instance
     */
    public ViewNavigationManager getViewNavigationManager() {
        return viewNavigationManager;
    }

    /**
     * Sets the view navigation manager (typically called during UI initialization)
     *
     * @param viewNavigationManager the view navigation manager to set
     */
    public void setViewNavigationManager(ViewNavigationManager viewNavigationManager) {
        this.viewNavigationManager = viewNavigationManager;
    }
}