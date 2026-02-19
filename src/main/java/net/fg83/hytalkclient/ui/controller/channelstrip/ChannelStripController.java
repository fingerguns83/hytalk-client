package net.fg83.hytalkclient.ui.controller.channelstrip;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import net.fg83.hytalkclient.ui.event.GainChangeEvent;
import net.fg83.hytalkclient.util.AppConstants;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static java.lang.Math.round;

public abstract class ChannelStripController {
    @FXML
    private VBox CHANNEL_STRIP_ROOT;

    protected ImageView faderCap;
    protected Line VUMeter;
    protected TextField scribbleStrip;

    protected UUID playerId;
    protected String playerName;

    protected double faderLocation = -100;

    protected double vuLocation = 260;

    protected double lastY;
    protected boolean isPressed = false;


    /* Event Handlers */
    @FXML
    protected void onMouseScrollFader(ScrollEvent scrollEvent) {
        renderFader(faderLocation - (scrollEvent.getTotalDeltaY() * 0.2));
    }

    @FXML
    protected void onMouseClickFader(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2){
            renderFader(AppConstants.UI.ChannelStrip.FADER_MAX_Y - (.8 * AppConstants.UI.ChannelStrip.FADER_MAX_Y));
        }
    }

    @FXML
    protected void onMousePressedFader(MouseEvent mouseEvent) {
        lastY = round(mouseEvent.getScreenY());
        isPressed = true;
    }

    @FXML
    protected void onMouseReleasedFader(MouseEvent mouseEvent) {
        isPressed = false;
    }

    @FXML
    protected void onMouseDragFader(MouseEvent mouseEvent) {
        if (!isPressed) return;

        double newY = round(mouseEvent.getScreenY());

        double deltaY = lastY - newY;

        double sensitivity = Math.signum(deltaY) * Math.pow(Math.abs(deltaY), 1.2);
        double scalingFactor = 0.5;

        double valueDelta = sensitivity * scalingFactor;

        renderFader(faderLocation - valueDelta);
        lastY = newY;
    }

    /* Getters and Setters */
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName){ this.playerName = playerName; }

    public String getRootId(){ return CHANNEL_STRIP_ROOT.getId(); }
    public void setRootId(String rootId){ CHANNEL_STRIP_ROOT.setId(rootId); }

    /* Utility Methods */
    public void setup() throws IOException {
        this.scribbleStrip = (TextField) CHANNEL_STRIP_ROOT.lookup(".scribble-strip");
        initializeScribbleStrip();
        initializeFaderCap();
        initializeFaderLocation();
        initializeVUMeter();
    }

    protected void initializeScribbleStrip() {
        scribbleStrip.setText(playerName);
    }
    protected void initializeFaderCap() {
         faderCap = (ImageView) CHANNEL_STRIP_ROOT.lookup(".fader-cap");
    }
    protected void initializeFaderLocation() {
        if (faderLocation == -100){
            faderLocation = faderCap.getLayoutY();
        }
    }
    protected void initializeVUMeter() {
        VUMeter = (Line) CHANNEL_STRIP_ROOT.lookup(".vu-meter-mask");
    }

    protected void renderFader(double location) {
        faderLocation = Math.max(AppConstants.UI.ChannelStrip.FADER_MIN_Y, Math.min(AppConstants.UI.ChannelStrip.FADER_MAX_Y, location));
        faderCap.setLayoutY(faderLocation);
    }

    protected void setGain() {
        CHANNEL_STRIP_ROOT.fireEvent(new GainChangeEvent(playerId, calculateGainPercentage()));
    };

    protected float calculateGainPercentage() {
        return (float) (1 - (faderLocation - AppConstants.UI.ChannelStrip.FADER_MIN_Y) / (AppConstants.UI.ChannelStrip.FADER_MAX_Y - AppConstants.UI.ChannelStrip.FADER_MIN_Y));
    }

    public void updateMeter(float level) {
        if (Instant.now().getEpochSecond() % 2 == 0 && this instanceof InputChannelStripController){
            System.out.println("Level: " + level);
        }
        VUMeter.setEndY(Math.round((255 - (level * 255))) + 5);
    }
}
