package net.fg83.hytalkclient.ui.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import net.fg83.hytalkclient.util.AppConstants;
import net.fg83.hytalkclient.ui.event.view.ViewEvent;

import java.io.IOException;
import java.time.Instant;

public class LaunchController {
    @FXML
    private AnchorPane LAUNCH_ROOT;

    private final Instant initialTime = Instant.now();

    @FXML
    private void initialize() {
        ((Label) LAUNCH_ROOT.lookup("#Version-Info")).setText("Hytalk Client " + AppConstants.VERSION);
    }

    public void run() throws IOException {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (Instant.now().isAfter(initialTime.plusSeconds(3))){
                    AnchorPane main = ((AnchorPane) LAUNCH_ROOT.getScene().getRoot());
                    this.stop();
                    main.fireEvent(new ViewEvent(ViewEvent.SHOW_CONNECTION_VIEW));
                }
            }
        };
        timer.start();
    }
}
