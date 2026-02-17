package net.fg83.hytalkclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.fg83.hytalkclient.service.*;
import net.fg83.hytalkclient.util.AppConstants;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.ui.controller.HytalkClientController;

import java.io.IOException;
import java.net.URL;

public class HytalkClientApplication extends Application {

    private final ApplicationState applicationState = new ApplicationState();

    @Override
    public void start(Stage stage) throws IOException {
        // Setup main window
        stage.setTitle("Hytalk Client v" + AppConstants.VERSION);
        FXMLLoader mainView = new FXMLLoader(getView("HytalkClient.fxml"));
        Parent root = mainView.load();
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        // Initialize services
        applicationState.setViewNavigationManager(new ViewNavigationManager((AnchorPane) root));

        // Setup main controller
        HytalkClientController mainViewController = mainView.getController();
        mainViewController.setup(applicationState);

        stage.setOnCloseRequest(event -> shutdown());
        stage.show();
    }

    private void shutdown() {
        if (applicationState.getConnectionManager() != null) {
            applicationState.getConnectionManager().disconnect();
        }
    }

    public static URL getView(String viewName) {
        return HytalkClientController.class.getResource("/net/fg83/hytalkclient/" + viewName);
    }
}