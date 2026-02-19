package net.fg83.hytalkclient.model;


import net.fg83.hytalkclient.service.*;

public class ApplicationState {
    private final ErrorDialogManager errorDialogManager = new ErrorDialogManager();
    private ViewNavigationManager viewNavigationManager;
    private final ConnectionManager connectionManager = new ConnectionManager(this);
    private final PairingManager pairingManager = new PairingManager();
    private final PlayerManager playerManager = new PlayerManager();
    private final AudioManager audioManager = new AudioManager();
    private final MixerManager mixerManager = new MixerManager(audioManager);


    public ApplicationState() { }

    public void setViewNavigationManager(ViewNavigationManager viewNavigationManager) {
        this.viewNavigationManager = viewNavigationManager;
    }
    public ViewNavigationManager getViewNavigationManager() {
        return viewNavigationManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public PairingManager getPairingManager() {
        return pairingManager;
    }

    public AudioManager getAudioManager() {
        return audioManager;
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
}