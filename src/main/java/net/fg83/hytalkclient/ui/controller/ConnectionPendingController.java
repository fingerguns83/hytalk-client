package net.fg83.hytalkclient.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import net.fg83.hytalkclient.ui.event.view.ViewEvent;

/**
 * Controller for the connection pending view.
 * Manages the UI state while a connection attempt is in progress.
 */
public class ConnectionPendingController {
    // Root pane of the connection pending view, injected from FXML
    @FXML
    private AnchorPane CONNECTION_PENDING_ROOT;

    /**
     * Handles the cancel button action during a pending connection.
     * Fires a ViewEvent to navigate back to the connection view.
     *
     * @param actionEvent the action event triggered by the cancel button
     */
    public void onConnectionCancel(ActionEvent actionEvent) {
        // Fire event to show the connection view, effectively canceling the pending connection
        CONNECTION_PENDING_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_CONNECTION_VIEW));
    }
}