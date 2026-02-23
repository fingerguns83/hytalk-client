package net.fg83.hytalkclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import net.fg83.hytalkclient.service.*;
import net.fg83.hytalkclient.util.AppConstants;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.ui.controller.HytalkClientController;

import java.io.IOException;
import java.net.URL;

/**
 * Main JavaFX application class for the Hytalk Client.
 * Initializes the application window, loads resources, and sets up the application state.
 */
public class HytalkClientApplication extends Application {

    // Holds the global application state shared across controllers and services
    private final ApplicationState applicationState = new ApplicationState();

    /**
     * Entry point for the JavaFX application lifecycle.
     * Configures the primary stage, loads the main view, and initializes application components.
     *
     * @param stage the primary stage for this application
     * @throws IOException if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Set the window title with version information
        stage.setTitle("Hytalk Client v" + AppConstants.VERSION);
        // Load custom fonts for the application UI
        loadFonts();
        // Create FXML loader for the main application view
        FXMLLoader mainView = new FXMLLoader(getView("HytalkClient.fxml"));
        // Load the FXML and get the root node of the scene graph
        Parent root = mainView.load();
        // Create and set the scene with the loaded root node
        stage.setScene(new Scene(root));
        // Prevent window resizing to maintain fixed layout
        stage.setResizable(false);
        // Keep window on top of other windows
        stage.setAlwaysOnTop(true);

        // Initialize the view navigation manager with the root pane for switching views
        applicationState.setViewNavigationManager(new ViewNavigationManager((AnchorPane) root));

        // Get the controller instance and inject the application state
        HytalkClientController mainViewController = mainView.getController();
        mainViewController.setup(applicationState);

        // Register shutdown handler to clean up resources when window is closed
        stage.setOnCloseRequest(event -> shutdown());
        // Display the application window
        stage.show();
    }



    /**
     * Safely shuts down various application components to release resources
     * and ensure a clean termination of the application. This includes:
     * - Disconnecting the connection manager.
     * - Shutting down the audio stream manager.
     * - Shutting down the audio network manager.
     * - Shutting down the mixer manager.
     *
     * Each manager's respective shutdown or disconnect method is called
     * only if the manager instance is not null.
     */
    private void shutdown() {
        if (applicationState.getConnectionManager() != null) {
            applicationState.getConnectionManager().disconnect();
        }
        if (applicationState.getAudioStreamManager() != null) {
            applicationState.getAudioStreamManager().shutdown();
        }
        if (applicationState.getAudioNetworkManager() != null) {
            applicationState.getAudioNetworkManager().shutdown();
        }
        if (applicationState.getMixerManager() != null) {
            applicationState.getMixerManager().shutdown();
        }
    }

    /**
     * Resolves the URL for an FXML view file by name.
     *
     * @param viewName the name of the FXML file (e.g., "HytalkClient.fxml")
     * @return the URL to the FXML resource
     */
    public static URL getView(String viewName) {
        return HytalkClientController.class.getResource("/net/fg83/hytalkclient/" + viewName);
    }

    /**
     * Loads custom font files for use in the application UI.
     * Loads regular, bold, and italic variants of the BonaNovaSC font family.
     */
    private void loadFonts() {
        Font.loadFont(getClass().getResourceAsStream("/assets/fonts/BonaNovaSC-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/assets/fonts/BonaNovaSC-Bold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/assets/fonts/BonaNovaSC-Italic.ttf"), 14);
    }
}