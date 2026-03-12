// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import net.fg83.hytalkclient.HytalkClientApplication;
import net.fg83.hytalkclient.service.PreferenceManager;
import net.fg83.hytalkclient.ui.event.ConnectionSetupEvent;
import net.fg83.hytalkclient.ui.event.view.ViewEvent;
import net.fg83.hytalkclient.util.AppConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Controller for the connection setup view.
 * Handles user input for server connection parameters and initiates connection attempts.
 */
public class ConnectionController {

    // Root container for the connection view UI
    @FXML
    private AnchorPane CONNECTION_ROOT;

    public void setup(){
        ((TextField) CONNECTION_ROOT.lookup("#server-address")).setText(PreferenceManager.getServerAddress());
        if (PreferenceManager.getServerPort() != -1){
            ((TextField) CONNECTION_ROOT.lookup("#server-control-port")).setText(Integer.toString(PreferenceManager.getServerPort()));
        }
    }

    /**
     * Attempts to establish a connection to the server using user-provided parameters.
     * Validates port number and fires connection setup event if valid.
     *
     * @param actionEvent The action event that triggered this method (may be null)
     */
    public void attemptConnection(ActionEvent actionEvent) {
        // Get reference to the main application root pane
        AnchorPane main = ((AnchorPane) CONNECTION_ROOT.getScene().getRoot());

        // Retrieve and trim server address from input field
        String serverAddress = ((TextField) CONNECTION_ROOT.lookup("#server-address")).getText().trim();
        // Retrieve and trim server port from input field
        String serverPort = ((TextField) CONNECTION_ROOT.lookup("#server-control-port")).getText().trim();
        int serverPortInt;
        // Use default port if user left field blank
        if (serverPort.isBlank()) {
            serverPortInt = AppConstants.DEFAULT_SERVER_PORT;
        }
        else {
            // Parse user-provided port number
            try {
                serverPortInt = Integer.parseInt(serverPort);
            }
            catch (NumberFormatException e) {
                // Log error and exit if port is not a valid integer
                System.out.println("Invalid port number");
                return;
            }

        }

        // Validate port number is within valid range (1-65535)
        if (serverPortInt > 65535) {
            System.out.println("Port number out of range");
        }
        else {
            PreferenceManager.saveServerAddress(serverAddress);
            if (!serverPort.isBlank()){
                PreferenceManager.saveServerPort(serverPortInt);
            }
            // Log connection attempt
            System.out.println("Attempting connection to " + serverAddress + ":" + serverPort);
            // Fire event to initiate connection with validated parameters
            main.fireEvent(new ConnectionSetupEvent(serverAddress, serverPortInt));
            // Switch to connection pending view
            main.fireEvent(new ViewEvent(ViewEvent.SHOW_CONNECTION_PENDING_VIEW));
        }
    }

    /**
     * Handles key release events in the server address field.
     * Triggers connection attempt when Enter key is pressed.
     *
     * @param keyEvent The key event containing information about the pressed key
     */
    public void addressOnKeyReleased(KeyEvent keyEvent) {
        // Check if Enter key was pressed
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            // Trigger connection attempt
            attemptConnection(null);
        }
    }

    /**
     * Handles key release events in the server port field.
     * Triggers connection attempt when Enter key is pressed.
     *
     * @param keyEvent The key event containing information about the pressed key
     */
    public void portOnKeyReleased(KeyEvent keyEvent) {
        // Check if Enter key was pressed
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            // Trigger connection attempt
            attemptConnection(null);
        }
    }

    /**
     * Opens a dialog window displaying open source license information.
     * Reads and displays the Concentus library license text.
     *
     * @param mouseEvent The mouse event that triggered this method
     */
    public void openLicenseWindow(MouseEvent mouseEvent) {
        // Run on JavaFX application thread to ensure UI thread safety
        Platform.runLater(() -> {
            // Create information alert dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Open Source Software Information");
            alert.setHeaderText(null);
            try {
                // Read Concentus license file from resources and set as dialog content
                alert.setContentText(
                    Files.readString(
                        Path.of(
                            Objects.requireNonNull(HytalkClientApplication.class.getResource("/assets/misc/ConcentusLicense")).getPath()
                        )
                    )
                );
            }
            catch (IOException e) {
                // Wrap and rethrow IO exceptions as runtime exceptions
                throw new RuntimeException(e);
            }
            // Position dialog at top-left corner of screen
            alert.setX(0);
            alert.setY(0);
            // Display the dialog
            alert.show();
        });
    }
}