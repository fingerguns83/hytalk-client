package net.fg83.hytalkclient.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import net.fg83.hytalkclient.ui.event.ConnectionSetupEvent;
import net.fg83.hytalkclient.ui.event.ViewEvent;

public class ConnectionController {

    @FXML
    private AnchorPane CONNECTION_ROOT;

    public void attemptConnection(ActionEvent actionEvent) {
        AnchorPane main = ((AnchorPane) CONNECTION_ROOT.getScene().getRoot());

        String serverAddress = ((TextField) CONNECTION_ROOT.lookup("#server-address")).getText().trim();
        String serverPort = ((TextField) CONNECTION_ROOT.lookup("#server-control-port")).getText().trim();
        if (serverPort.isBlank()){
            serverPort = "5222";
        }
        int serverPortInt;
        try {
            serverPortInt = Integer.parseInt(serverPort);
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid port number");
            return;
        }

        if (Integer.parseInt(serverPort) > 65535) {
            System.out.println("Port number out of range");
        }
        else {
            System.out.println("Attempting connection to " + serverAddress + ":" + serverPort);
            main.fireEvent(new ConnectionSetupEvent(serverAddress, Integer.parseInt(serverPort)));
            main.fireEvent(new ViewEvent(ViewEvent.SHOW_CONNECTION_PENDING_VIEW));
        }
    }
}
