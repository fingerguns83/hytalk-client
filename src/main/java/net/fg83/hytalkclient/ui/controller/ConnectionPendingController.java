package net.fg83.hytalkclient.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import net.fg83.hytalkclient.ui.event.view.ViewEvent;

public class ConnectionPendingController {
    @FXML
    private AnchorPane CONNECTION_PENDING_ROOT;

    public void onConnectionCancel(ActionEvent actionEvent) {
        CONNECTION_PENDING_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_CONNECTION_VIEW));
    }
}
