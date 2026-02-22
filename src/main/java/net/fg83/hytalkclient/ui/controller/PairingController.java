package net.fg83.hytalkclient.ui.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import net.fg83.hytalkclient.ui.event.PairingEvent;

import java.time.Instant;

public class PairingController {
    @FXML
    private AnchorPane PAIRING_ROOT;
    @FXML
    private Label PAIRING_CODE_LABEL;

    private Instant pairingExpiration;
    private AnimationTimer expirationTimer;

    public void setup(String pairingCode, Instant expiration) {
        PAIRING_CODE_LABEL.setText(pairingCode);
        this.pairingExpiration = expiration;
        startExpirationTimer();
    }

    private void startExpirationTimer() {
        if (expirationTimer != null) {
            expirationTimer.stop();
        }

        expirationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (Instant.now().isAfter(pairingExpiration)) {
                    this.stop();
                    PAIRING_ROOT.fireEvent(new PairingEvent(PairingEvent.PAIRING_EXPIRED));
                }
            }
        };
        expirationTimer.start();
    }

    @FXML
    private void handleCancel() {
        if (expirationTimer != null) {
            expirationTimer.stop();
        }
        PAIRING_ROOT.fireEvent(new PairingEvent(PairingEvent.PAIRING_CANCELLED));
    }

    public void cleanup() {
        if (expirationTimer != null) {
            expirationTimer.stop();
        }
    }
}