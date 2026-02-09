package net.fg83.hytalkclient.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.fg83.hytalkclient.HytalkClientApplication;

import java.io.IOException;
import java.util.UUID;

public class MixerController {
    Stage stage;

    @FXML
    private Pane MIXER_ROOT;

    public void initialize() throws IOException {
        createInputFader();
        createMasterFader();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void createFader(UUID playerId, String playerName, boolean isInput, boolean isMaster) throws IOException {

        FXMLLoader channel = new FXMLLoader(HytalkClientApplication.class.getResource("widget/ChannelStrip.fxml"));
        Parent channelRoot = channel.load();
        channelRoot.setLayoutX(MIXER_ROOT.getChildren().size() * 120);

        ChannelStripController channelStripController = channel.getController();

        channelStripController.setPlayerId(playerId);
        channelStripController.setPlayerName(playerName);
        channelStripController.setInput(isInput);
        channelStripController.setMaster(isMaster);


        channelStripController.initialize((VBox) channelRoot);

        MIXER_ROOT.getChildren().add(channelRoot);
    }
    public void createInputFader() throws IOException {
        createFader(UUID.randomUUID(), "Input", true, false);
    }
    public void createMasterFader() throws IOException {
        createFader(null, "Master", false, true);
    }
}
