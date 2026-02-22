package net.fg83.hytalkclient.ui.controller.channelstrip;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.ui.event.mixer.GainChangeEvent;
import net.fg83.hytalkclient.util.AppConstants;

import java.io.IOException;
import java.util.UUID;

import static java.lang.Math.round;

public class ChannelStripController {
    @FXML
    private VBox CHANNEL_STRIP_ROOT;

    protected ImageView faderCap;
    protected Line VUMeter;
    protected Label scribbleStrip;

    protected UUID playerId;
    protected String playerName;

    protected double faderLocation = -100;

    protected double lastY;
    protected boolean isPressed = false;


    /* Event Handlers */
    @FXML
    protected void onMouseScrollFader(ScrollEvent scrollEvent) {
        renderFader(faderLocation - (scrollEvent.getTotalDeltaY() * 0.2));
        setGain();
    }

    @FXML
    protected void onMouseClickFader(MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseButton.SECONDARY){
            renderFader(AppConstants.UI.ChannelStrip.FADER_MAX_Y - (.8 * AppConstants.UI.ChannelStrip.FADER_MAX_Y));
            setGain();
        }
    }

    @FXML
    protected void onMousePressedFader(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) return;
        faderCap.setCursor(Cursor.CLOSED_HAND);
        lastY = round(mouseEvent.getScreenY());
        isPressed = true;
    }

    @FXML
    protected void onMouseReleasedFader(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) return;
        faderCap.setCursor(Cursor.OPEN_HAND);
        isPressed = false;
    }

    @FXML
    protected void onMouseDragFader(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) return;
        if (!isPressed) return;

        double newY = round(mouseEvent.getScreenY());

        double deltaY = lastY - newY;

        double sensitivity = Math.signum(deltaY) * Math.pow(Math.abs(deltaY), 1.2);
        double scalingFactor = 0.5;

        double valueDelta = sensitivity * scalingFactor;

        renderFader(faderLocation - valueDelta);
        setGain();

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
    public void setup(ApplicationState applicationState) throws IOException {
        this.scribbleStrip = (Label) CHANNEL_STRIP_ROOT.lookup(".scribble-strip");
        initializeScribbleStrip();
        initializeFaderCap();
        initializeFaderLocation();
        initializeVUMeter();
    }

    protected void initializeScribbleStrip() {
        scribbleStrip.setText(playerName);
        scribbleStrip.getTooltip().setText(playerName);
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
    }

    protected float calculateGainPercentage() {
        return (float) (1 - (faderLocation - AppConstants.UI.ChannelStrip.FADER_MIN_Y) / (AppConstants.UI.ChannelStrip.FADER_MAX_Y - AppConstants.UI.ChannelStrip.FADER_MIN_Y));
    }

    public void updateMeter(float level) {
        VUMeter.setEndY(Math.round((255 - (level * 255))) + 5);
    }
}
