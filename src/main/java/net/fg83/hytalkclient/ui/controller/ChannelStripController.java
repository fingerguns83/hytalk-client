package net.fg83.hytalkclient.ui.controller;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.UUID;

import static java.lang.Math.round;

public class ChannelStripController {
    private VBox channelStrip;
    private ImageView faderCap;
    private TextField scribbleStrip;

    private boolean isInput = false;
    private boolean isMaster = false;

    private UUID playerId;
    private String playerName;

    private double faderLocation = -100;
    private static final double FADER_MIN_Y = 0;
    private static final double FADER_MAX_Y = 182;

    private double vuLocation = 260;
    private static final double VU_LEVEL_MAX = 260;

    private double lastY;
    private boolean isPressed = false;

    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (Math.random() < 0.01){
                ((Line) channelStrip.lookup(".vu-meter-mask")).setEndY(Math.random() * VU_LEVEL_MAX);
            }
        }
    };


    /* Event Handlers */
    public void onMouseScroll(ScrollEvent scrollEvent) {
        setFaderLocation(faderLocation - (scrollEvent.getTotalDeltaY() * 0.2));
    }

    public void onMouseClick(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2){
            setFaderLocation(FADER_MAX_Y - (.8 * FADER_MAX_Y));
        }
    }

    public void onMousePressed(MouseEvent mouseEvent) {
        lastY = round(mouseEvent.getScreenY());
        isPressed = true;
    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        isPressed = false;
    }

    public void onMouseDrag(MouseEvent mouseEvent) {
        if (!isPressed) return;

        double newY = round(mouseEvent.getScreenY());

        double deltaY = lastY - newY;

        double sensitivity = Math.signum(deltaY) * Math.pow(Math.abs(deltaY), 1.2);
        double scalingFactor = 0.5;

        double valueDelta = sensitivity * scalingFactor;
        faderLocation = faderLocation - valueDelta;

        setFaderLocation(faderLocation);
        lastY = newY;
    }

    /* Getters and Setters */
    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    public void setInput(boolean inputStatus){
        isInput = inputStatus;
    }
    public void setMaster(boolean masterStatus){
        isMaster = masterStatus;
    }

    public UUID getPlayerId() {
        return playerId;
    }
    public String getPlayerName() {
        return playerName;
    }

    /* Utility Methods */
    public void initialize(VBox channelStrip){
        this.channelStrip = channelStrip;

        this.faderCap = (ImageView) channelStrip.lookup(".fader-cap");
        if (isInput){
            ColorAdjust colorAdjust = new ColorAdjust(-1, 0.76, 0, 0);
            colorAdjust.setInput(new ColorAdjust(0, -1, 0, 0));
            this.faderCap.setEffect(colorAdjust);
        }
        if (isMaster){
            ColorAdjust colorAdjust = new ColorAdjust(-0.07, 0.76, 0, 0);
            colorAdjust.setInput(new ColorAdjust(0, -1, 0, 0));
            this.faderCap.setEffect(colorAdjust);
        }

        channelStrip.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK, (this.isMaster) ? Color.TRANSPARENT : Color.BLACK, Color.BLACK, (this.isInput) ? Color.TRANSPARENT : Color.BLACK,
                                BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY, new BorderWidths(2), Insets.EMPTY
                        )
                )
        );



        this.scribbleStrip = (TextField) channelStrip.lookup(".scribble-strip");
        this.scribbleStrip.setText(playerName);

        if (faderLocation == -100){
            faderLocation = faderCap.getLayoutY();
        }
        timer.start();
    }

    public void setFaderLocation(double location){
        faderLocation = Math.max(FADER_MIN_Y, Math.min(FADER_MAX_Y, location));
        faderCap.setLayoutY(faderLocation);
    }
}
