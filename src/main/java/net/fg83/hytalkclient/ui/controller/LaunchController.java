// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import net.fg83.hytalkclient.util.AppConstants;
import net.fg83.hytalkclient.ui.event.view.ViewEvent;

import java.io.IOException;
import java.time.Instant;

/**
 * Controller for the launch screen view.
 * Displays the application splash screen with version information
 * and automatically transitions to the connection view after 3 seconds.
 */
public class LaunchController {
    // Root pane of the launch view, injected by FXML
    @FXML
    private AnchorPane LAUNCH_ROOT;

    // Timestamp when the controller is instantiated, used to track elapsed time
    private final Instant initialTime = Instant.now();

    /**
     * FXML initialization method called after the FXML file is loaded.
     * Sets up the version information label on the launch screen.
     */
    @FXML
    private void initialize() {
        // Lookup the version label by its CSS ID and set the text to display the application version
        ((Label) LAUNCH_ROOT.lookup("#Version-Info")).setText("Hytalk Client " + AppConstants.VERSION);
    }

    /**
     * Starts the launch screen timer.
     * Creates and starts an animation timer that waits for 3 seconds before
     * transitioning to the connection view.
     *
     * @throws IOException if an I/O error occurs
     */
    public void run() throws IOException {
        // Create an animation timer to handle the automatic transition
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                // Check if 3 seconds have elapsed since initialization
                if (Instant.now().isAfter(initialTime.plusSeconds(3))) {
                    // Get reference to the main root pane
                    AnchorPane main = ((AnchorPane) LAUNCH_ROOT.getScene().getRoot());
                    // Stop the timer to prevent further execution
                    this.stop();
                    // Fire an event to trigger the transition to the connection view
                    main.fireEvent(new ViewEvent(ViewEvent.SHOW_CONNECTION_VIEW));
                }
            }
        };
        // Start the animation timer
        timer.start();
    }
}