package net.fg83.hytalkclient.ui.controller.channelstrip.button;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import net.fg83.hytalkclient.ui.controller.channelstrip.ChannelStripController;
import net.fg83.hytalkclient.ui.controller.channelstrip.InputChannelStripController;
import net.fg83.hytalkclient.ui.controller.channelstrip.OutputChannelStripController;
import net.fg83.hytalkclient.ui.event.mixer.ChannelMuteEvent;
import net.fg83.hytalkclient.util.ButtonType;


public class ButtonController {
    @FXML
    private Button MIXER_BUTTON_ROOT;

    @FXML
    private Label MIXER_BUTTON_LABEL;

    private ButtonType buttonType;

    private String buttonColor;

    private boolean isEngaged = false;

    private ChannelStripController parentController;

    public void setup(ChannelStripController parentController) {
        this.parentController = parentController;

        setButtonColor();
        setButtonText();
        styleLimiterButton();
    }


    public ButtonType getButtonType() {
        return buttonType;
    }
    public void setButtonType(ButtonType buttonType) {
        this.buttonType = buttonType;
    }

    public void setButtonColor(){
        MIXER_BUTTON_LABEL.setStyle("-fx-text-fill: black;");
        switch (buttonType){
            case MUTE -> buttonColor = "red";
            case NOISE_REDUCTION -> buttonColor = "gold";
            case LIMITER -> buttonColor = "orange";
        }
    }
    public void setButtonText(){
        switch (buttonType){
            case MUTE -> MIXER_BUTTON_LABEL.setText("M");
            case NOISE_REDUCTION -> MIXER_BUTTON_LABEL.setText("NR");
            case LIMITER -> MIXER_BUTTON_LABEL.setText("\uD802\uDCEB");
        }
    }

    public void styleLimiterButton(){
        if (buttonType == ButtonType.LIMITER){
            MIXER_BUTTON_LABEL.rotateProperty().set(-90);
        }
    }


    /* GETTERS AND SETTERS */
    public boolean isEngaged() {
        return isEngaged;
    }

    public void setEngaged(boolean engaged) {
        isEngaged = engaged;
    }

    public ChannelStripController getParentController() {
        return parentController;
    }

    /* EVENT HANDLERS */
    public void onButtonPress(MouseEvent mouseEvent) {
        if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)){ return; }
        toggleButtonColor();
        setEngaged(!isEngaged);
        toggleButtonFeature();
    }

    private void toggleButtonFeature(){
        switch (buttonType){
            case MUTE -> {
                if (parentController instanceof InputChannelStripController) {
                    MIXER_BUTTON_ROOT.fireEvent(new ChannelMuteEvent(null, true, false, isEngaged));
                }
                else if (parentController instanceof OutputChannelStripController) {
                    MIXER_BUTTON_ROOT.fireEvent(new ChannelMuteEvent(null, false, true, isEngaged));
                }
                else {
                    if (parentController.isDummy()) return;
                    MIXER_BUTTON_ROOT.fireEvent(new ChannelMuteEvent(parentController.getPlayerId(), false, false, isEngaged));
                }
            }
            case NOISE_REDUCTION -> {}
            case LIMITER -> {}
            default -> {}
        }
    }
    private void toggleButtonColor(){
        if (isEngaged) {
            MIXER_BUTTON_LABEL.setStyle("-fx-text-fill: black;");
            MIXER_BUTTON_LABEL.setEffect(null);
        }
        else {
            MIXER_BUTTON_LABEL.setStyle("-fx-text-fill: " + buttonColor + ";");
            MIXER_BUTTON_LABEL.setEffect(new Bloom());
        }
    }

    public void bindControlToHotkey(MouseEvent mouseEvent) {
        /*if (!mouseEvent.getButton().equals(MouseButton.SECONDARY)){ return; }

        MIXER_BUTTON_LABEL.setStyle("-fx-text-fill: violet;");*/
    }
}
