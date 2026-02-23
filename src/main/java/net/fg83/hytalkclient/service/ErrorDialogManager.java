package net.fg83.hytalkclient.service;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;

/**
 * Manages the display of error dialogs in the application.
 * Ensures dialogs are shown on the JavaFX Application Thread.
 */
public class ErrorDialogManager {
    /**
     * Displays an error dialog with the specified title and message.
     * The dialog is shown on the JavaFX Application Thread to ensure thread safety.
     *
     * @param title   The title to display in the dialog (prefixed with "Hytalk Client - ")
     * @param message The error message to display in the dialog content
     */
    public void showError(String title, String message) {
        // Run on JavaFX Application Thread to ensure thread safety for UI operations
        Platform.runLater(() -> {
            // Create an error-type alert dialog
            Alert alert = new Alert(AlertType.ERROR);
            // Set the dialog title with application prefix
            alert.setTitle("Hytalk Client - " + title);
            // Remove the header text for a cleaner appearance
            alert.setHeaderText(null);
            // Set the main error message
            alert.setContentText(message);
            // Position the dialog at the top-left corner of the screen
            alert.setX(0);
            alert.setY(0);
            // Display the dialog and wait for user acknowledgment
            alert.showAndWait();
        });
    }
}