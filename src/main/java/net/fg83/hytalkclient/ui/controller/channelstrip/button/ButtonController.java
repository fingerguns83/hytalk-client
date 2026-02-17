package net.fg83.hytalkclient.ui.controller.channelstrip.button;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import net.fg83.hytalkclient.util.ButtonType;


public class ButtonController {
    @FXML
    private Button MIXER_BUTTON_ROOT;

    @FXML
    private Label MIXER_BUTTON_LABEL;

    private ButtonType buttonType;

    private String buttonColor;

    private boolean isEngaged = false;

    public void setup(){
        setButtonColor();
        setButtonText();
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
            case MUTE:
                buttonColor = "red";
                break;
            case NOISE_REDUCTION:
                buttonColor = "gold";
                break;
        }
    }
    public void setButtonText(){
        switch (buttonType){
            case MUTE:
                MIXER_BUTTON_LABEL.setText("M");
                break;
            case NOISE_REDUCTION:
                MIXER_BUTTON_LABEL.setText("NR");
                break;
        }
    }

    public void toggleButtonFeature(MouseEvent mouseEvent) {
        if (isEngaged) {
            MIXER_BUTTON_LABEL.setStyle("-fx-text-fill: black;");
        }
        else {
            MIXER_BUTTON_LABEL.setStyle("-fx-text-fill: " + buttonColor + ";");
        }

        setEngaged(!isEngaged);
    }

    public boolean isEngaged() {
        return isEngaged;
    }

    public void setEngaged(boolean engaged) {
        isEngaged = engaged;
    }
}
