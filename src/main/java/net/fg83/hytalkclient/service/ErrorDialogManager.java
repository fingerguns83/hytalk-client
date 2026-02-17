package net.fg83.hytalkclient.service;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;

public class ErrorDialogManager {
    public void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Hytalk Client - " + title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.setX(0);
            alert.setY(0);
            alert.showAndWait();
        });
    }
}