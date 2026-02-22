package net.fg83.hytalkclient.model;


import net.fg83.hytalkclient.service.*;

public class ApplicationState {
    private final ErrorDialogManager errorDialogManager = new ErrorDialogManager();
    private final PreferenceManager preferenceManager = new PreferenceManager();
    private final ConnectionManager connectionManager = new ConnectionManager(this);
    private final PairingManager pairingManager = new PairingManager();
    private final PlayerManager playerManager = new PlayerManager();
    private final AudioStreamManager audioStreamManager = new AudioStreamManager(preferenceManager);
    private final AudioNetworkManager audioNetworkManager = new AudioNetworkManager();
    private final MixerManager mixerManager = new MixerManager(audioStreamManager);

    private ViewNavigationManager viewNavigationManager;


    /* GETTERS AND SETTERS */

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public PairingManager getPairingManager() {
        return pairingManager;
    }

    public AudioStreamManager getAudioStreamManager() {
        return audioStreamManager;
    }

    public AudioNetworkManager getAudioNetworkManager() {
        return audioNetworkManager;
    }

    public MixerManager getMixerManager() {
        return mixerManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ErrorDialogManager getErrorDialogManager() {
        return errorDialogManager;
    }

    public ViewNavigationManager getViewNavigationManager() {
        return viewNavigationManager;
    }
    public void setViewNavigationManager(ViewNavigationManager viewNavigationManager) {
        this.viewNavigationManager = viewNavigationManager;
    }
}