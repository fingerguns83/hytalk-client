package net.fg83.hytalkclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.fg83.hytalkclient.config.AppConfig;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.service.ConnectionService;
import net.fg83.hytalkclient.service.ErrorDialogService;
import net.fg83.hytalkclient.service.ViewNavigationService;
import net.fg83.hytalkclient.ui.controller.HytalkClientController;

import java.io.IOException;
import java.net.URL;

public class HytalkClientApplication extends Application {

    private ApplicationState applicationState;
    private ConnectionService connectionService;
    private ErrorDialogService errorDialogService;
    private ViewNavigationService viewNavigationService;

    private HytalkClientController mainViewController;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;

        // Setup main window
        stage.setTitle("Hytalk Client v" + AppConfig.VERSION);
        FXMLLoader mainView = new FXMLLoader(getView("HytalkClient.fxml"));
        Parent root = mainView.load();
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        // Initialize services
        this.viewNavigationService = new ViewNavigationService((AnchorPane) root);
        initializeServices();

        // Setup main controller
        mainViewController = mainView.getController();
        mainViewController.setup(
                applicationState,
                viewNavigationService,
                connectionService,
                errorDialogService,
                stage
        );

        stage.setOnCloseRequest(event -> shutdown());
        stage.show();
    }

    private void initializeServices() {
        this.applicationState = new ApplicationState();
        this.errorDialogService = new ErrorDialogService();
        this.connectionService = new ConnectionService(applicationState, viewNavigationService, errorDialogService);

    }

    private void shutdown() {
        if (connectionService != null) {
            connectionService.disconnect();
        }
    }

    public static URL getView(String viewName) {
        return HytalkClientController.class.getResource("/net/fg83/hytalkclient/" + viewName);
    }

    public ApplicationState getApplicationState() {
        return applicationState;
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    public ErrorDialogService getErrorDialogService() {
        return errorDialogService;
    }

    public ViewNavigationService getViewNavigationService() {
        return viewNavigationService;
    }

    public void setViewNavigationService(ViewNavigationService viewNavigationService) {
        this.viewNavigationService = viewNavigationService;
    }
}