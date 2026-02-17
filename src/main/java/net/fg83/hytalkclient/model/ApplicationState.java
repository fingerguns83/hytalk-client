package net.fg83.hytalkclient.model;


import net.fg83.hytalkclient.service.*;

public class ApplicationState {
    private ViewNavigationManager viewNavigationManager;
    private ConnectionManager connectionManager = new ConnectionManager(this);
    private PairingManager pairingManager = new PairingManager();
    private PlayerManager playerManager = new PlayerManager();
    private MixerManager mixerManager = new MixerManager();
    private AudioManager audioManager = new AudioManager();
    private ErrorDialogManager errorDialogManager = new ErrorDialogManager();

    private AudioIOManager.AudioDevice selectedInputDevice;
    private AudioIOManager.AudioDevice selectedOutputDevice;


    public ApplicationState() { }

    public void setViewNavigationManager(ViewNavigationManager viewNavigationManager) {
        this.viewNavigationManager = viewNavigationManager;
    }
    public ViewNavigationManager getViewNavigationManager() {
        return viewNavigationManager;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setPairingManager(PairingManager pairingManager) {
        this.pairingManager = pairingManager;
    }
    public PairingManager getPairingManager() {
        return pairingManager;
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }
    public AudioManager getAudioManager() {
        return audioManager;
    }

    public void setMixerManager(MixerManager mixerManager) {
        this.mixerManager = mixerManager;
    }
    public MixerManager getMixerManager() {
        return mixerManager;
    }

    public void setPlayerManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public void setErrorDialogManager(ErrorDialogManager errorDialogManager) {
        this.errorDialogManager = errorDialogManager;
    }
    public ErrorDialogManager getErrorDialogManager() {
        return errorDialogManager;
    }
}