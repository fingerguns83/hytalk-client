// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.controller;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import net.fg83.hytalkclient.ui.event.PairingEvent;

import java.time.Instant;

/**
 * Controller for the pairing UI that displays a pairing code and monitors its expiration.
 */
public class PairingController {
    // Root pane for firing pairing events
    @FXML
    private AnchorPane PAIRING_ROOT;
    // Label that displays the pairing code to the user
    @FXML
    private Label PAIRING_CODE_LABEL;

    // The timestamp when the pairing code expires
    private Instant pairingExpiration;
    // Timer that continuously checks if the pairing code has expired
    private AnimationTimer expirationTimer;

    private final Timeline clipboardFlashTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, e -> PAIRING_CODE_LABEL.setEffect(new Bloom())),
            new KeyFrame(Duration.millis(100), e -> PAIRING_CODE_LABEL.setEffect(null))
    );
    /**
     * Initializes the pairing view with a code and expiration time.
     *
     * @param pairingCode the pairing code to display
     * @param expiration  the instant when the pairing code expires
     */
    public void setup(String pairingCode, Instant expiration) {
        // Display the pairing code in the UI
        PAIRING_CODE_LABEL.setText(pairingCode);
        // Store the expiration time
        this.pairingExpiration = expiration;
        // Start monitoring for expiration
        startExpirationTimer();
    }

    /**
     * Starts or restarts the animation timer that monitors pairing code expiration.
     */
    private void startExpirationTimer() {
        // Stop any existing timer to avoid multiple timers running
        if (expirationTimer != null) {
            expirationTimer.stop();
        }

        // Create a new animation timer that checks expiration on each frame
        expirationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Check if the current time has passed the expiration time
                if (Instant.now().isAfter(pairingExpiration)) {
                    // Stop the timer as the pairing has expired
                    this.stop();
                    // Fire an event to notify that the pairing has expired
                    PAIRING_ROOT.fireEvent(new PairingEvent(PairingEvent.PAIRING_EXPIRED));
                }
            }
        };
        // Start the timer
        expirationTimer.start();
    }

    /**
     * Handles the cancel button click event.
     * Stops the expiration timer and fires a pairing cancelled event.
     */
    @FXML
    private void handleCancel() {
        // Stop the expiration timer if it's running
        if (expirationTimer != null) {
            expirationTimer.stop();
        }
        // Fire an event to notify that the user cancelled the pairing
        PAIRING_ROOT.fireEvent(new PairingEvent(PairingEvent.PAIRING_CANCELLED));
    }

    /**
     * Cleans up resources by stopping the expiration timer.
     * Should be called when the controller is no longer needed.
     */
    public void cleanup() {
        // Stop the expiration timer to prevent memory leaks
        if (expirationTimer != null) {
            expirationTimer.stop();
        }
    }

    public void onCodeClick(MouseEvent mouseEvent) {
        
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString("/hytalk pair " + PAIRING_CODE_LABEL.getText());
        clipboard.setContent(content);


        clipboardFlashTimeline.play();
    }
}