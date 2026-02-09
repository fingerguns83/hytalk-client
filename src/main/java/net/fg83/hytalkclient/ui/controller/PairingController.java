package net.fg83.hytalkclient.ui.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.ui.event.ViewEvent;

import java.time.Instant;

public class PairingController {
    @FXML
    private AnchorPane PAIRING_ROOT;
    @FXML
    private Label PAIRING_CODE_LABEL;

    private ApplicationState applicationState;

    @FXML
    private void initialize() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (applicationState == null){
                    return;
                }
                if (Instant.now().isAfter(applicationState.getPairingExpiration())){
                    this.stop();
                    applicationState.setPairingCode(null);
                    applicationState.setPairingExpiration(null);
                    PAIRING_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_CONNECTION_VIEW));
                }
            }
        };
        timer.start();
    }

    // Call this AFTER loading the FXML
    public void setup(ApplicationState applicationState) {
        this.applicationState = applicationState;
        updateView();
    }

    private void updateView() {
        if (PAIRING_CODE_LABEL != null && applicationState != null) {
            PAIRING_CODE_LABEL.setText(applicationState.getPairingCode());
        }
    }
}