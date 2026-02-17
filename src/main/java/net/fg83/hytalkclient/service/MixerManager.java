package net.fg83.hytalkclient.service;

import javafx.scene.layout.Pane;


public class MixerManager {
    private Pane MIXER_ROOT;

    public MixerManager(Pane mixerRoot) {
        this.MIXER_ROOT = mixerRoot;
    }
    public MixerManager(){}

    public Pane getMixerRoot() {
        return MIXER_ROOT;
    }
    public void setMixerRoot(Pane mixerRoot) {
        this.MIXER_ROOT = mixerRoot;
    }

}
