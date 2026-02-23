package net.fg83.hytalkclient.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

/**
 * Manages view navigation and window sizing for JavaFX applications.
 * Handles loading FXML views, configuring controllers, and adjusting stage dimensions.
 */
public class ViewNavigationManager {
    // The root pane where views will be displayed
    private final AnchorPane rootPane;

    /**
     * Constructs a ViewNavigationManager with the specified root pane.
     *
     * @param rootPane the AnchorPane that will contain the loaded views
     */
    public ViewNavigationManager(AnchorPane rootPane) {
        this.rootPane = rootPane;
    }

    /**
     * Navigates to a new view by loading an FXML file and optionally configuring its controller.
     *
     * @param fxmlPath     the URL path to the FXML file
     * @param configurator optional callback to configure the controller after loading
     * @param width        the desired width for the stage
     * @param height       the desired height for the stage
     * @param <T>          the type of the controller
     * @return the controller instance of the loaded view
     * @throws IOException if the FXML file cannot be loaded
     */
    public <T> T navigateToView(URL fxmlPath, ViewConfigurator<T> configurator, double width, double height) throws IOException {
        // Set the stage dimensions before loading the view
        setDimensions(width, height);

        // Load the FXML file and get the view and controller
        FXMLLoader loader = new FXMLLoader(fxmlPath);
        Parent view = loader.load();
        T controller = loader.getController();

        // Replace the current view with the new one
        rootPane.getChildren().clear();
        rootPane.getChildren().add(view);

        // Apply custom configuration to the controller if provided
        if (configurator != null) {
            configurator.configure(controller);
        }

        // Return the controller for further use
        return controller;
    }

    /**
     * Functional interface for configuring a controller after view loading.
     *
     * @param <T> the type of the controller to configure
     */
    @FunctionalInterface
    public interface ViewConfigurator<T> {
        /**
         * Configures the controller with custom logic.
         *
         * @param controller the controller instance to configure
         * @throws IOException if configuration fails
         */
        void configure(T controller) throws IOException;
    }

    /**
     * Sets the dimensions of the stage containing the root pane.
     * Accounts for window decorations (title bar, borders) to set the correct scene size.
     *
     * @param width  the desired width of the scene content
     * @param height the desired height of the scene content
     */
    public void setDimensions(double width, double height) {
        // Run on JavaFX Application Thread to ensure thread safety
        javafx.application.Platform.runLater(() -> {
            // Check if the root pane is attached to a scene and window
            if (rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
                // Get the stage from the root pane's window
                javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();

                // Get current scene dimensions (content area)
                double currentSceneWidth = stage.getScene().getWidth();
                double currentSceneHeight = stage.getScene().getHeight();

                // Get current stage dimensions (including decorations)
                double currentStageWidth = stage.getWidth();
                double currentStageHeight = stage.getHeight();

                // Calculate the size of window decorations (title bar, borders)
                double decorationWidth = currentStageWidth - currentSceneWidth;
                double decorationHeight = currentStageHeight - currentSceneHeight;

                // Reset size constraints to allow resizing
                stage.setMinWidth(0);
                stage.setMinHeight(0);
                stage.setMaxWidth(Double.MAX_VALUE);
                stage.setMaxHeight(Double.MAX_VALUE);

                // Set the stage size including decorations to achieve desired scene size
                stage.setWidth(width + decorationWidth);
                stage.setHeight(height + decorationHeight);

                // Center the stage on the screen
                stage.centerOnScreen();
            }
        });
    }
}