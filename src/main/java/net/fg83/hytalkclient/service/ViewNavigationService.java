package net.fg83.hytalkclient.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.net.URL;

public class ViewNavigationService {
    private final AnchorPane rootPane;

    public ViewNavigationService(AnchorPane rootPane) {
        this.rootPane = rootPane;
    }

    public <T> T navigateToView(URL fxmlPath, ViewConfigurator<T> configurator) throws IOException {
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
}