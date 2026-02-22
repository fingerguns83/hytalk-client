package net.fg83.hytalkclient.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class ViewNavigationManager {
    private final AnchorPane rootPane;

    public ViewNavigationManager(AnchorPane rootPane) {
        this.rootPane = rootPane;
    }

    public <T> T navigateToView(URL fxmlPath, ViewConfigurator<T> configurator, double width, double height) throws IOException {
        setDimensions(width, height);

        FXMLLoader loader = new FXMLLoader(fxmlPath);
        Parent view = loader.load();
        T controller = loader.getController();

        rootPane.getChildren().clear();
        rootPane.getChildren().add(view);

        if (configurator != null) {
            configurator.configure(controller);
        }

        return controller;
    }

    @FunctionalInterface
    public interface ViewConfigurator<T> {
        void configure(T controller) throws IOException;
    }

    public void setDimensions(double width, double height) {
        javafx.application.Platform.runLater(() -> {
            if (rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
                javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
                double currentSceneWidth = stage.getScene().getWidth();
                double currentSceneHeight = stage.getScene().getHeight();
                double currentStageWidth = stage.getWidth();
                double currentStageHeight = stage.getHeight();

                double decorationWidth = currentStageWidth - currentSceneWidth;
                double decorationHeight = currentStageHeight - currentSceneHeight;

                stage.setMinWidth(0);
                stage.setMinHeight(0);
                stage.setMaxWidth(Double.MAX_VALUE);
                stage.setMaxHeight(Double.MAX_VALUE);

                stage.setWidth(width + decorationWidth);
                stage.setHeight(height + decorationHeight);

                stage.centerOnScreen();
            }
        });
    }
}