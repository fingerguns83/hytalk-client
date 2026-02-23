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

/**
 * Controller class for managing mixer button behavior in a channel strip.
 * Handles button appearance, state, and interaction logic for different button types
 * (Mute, Noise Reduction, Limiter).
 */
public class ButtonController {
    // Root button component from FXML
    @FXML
    private Button MIXER_BUTTON_ROOT;

    // Label displaying button text from FXML
    @FXML
    private Label MIXER_BUTTON_LABEL;

    // Type of button (MUTE, NOISE_REDUCTION, or LIMITER)
    private ButtonType buttonType;

    // Color used when button is engaged
    private String buttonColor;

    // Tracks whether the button is currently engaged/active
    private boolean isEngaged = false;

    // Reference to the parent channel strip controller
    private ChannelStripController parentController;

    /**
     * Initializes the button with its parent controller and sets up appearance.
     *
     * @param parentController The channel strip controller that owns this button
     */
    public void setup(ChannelStripController parentController) {
        this.parentController = parentController;

        setButtonColor();
        setButtonText();
        styleLimiterButton();
    }

    /**
     * Gets the type of this button.
     *
     * @return The ButtonType enum value
     */
    public ButtonType getButtonType() {
        return buttonType;
    }

    /**
     * Sets the type of this button.
     *
     * @param buttonType The ButtonType to assign
     */
    public void setButtonType(ButtonType buttonType) {
        this.buttonType = buttonType;
    }

    /**
     * Sets the button color based on its type.
     * Initializes label text to black and assigns appropriate engaged color.
     */
    public void setButtonColor() {
        // Set initial text color to black
        MIXER_BUTTON_LABEL.setStyle("-fx-text-fill: black;");
        // Assign color based on button type
        switch (buttonType) {
            case MUTE -> buttonColor = "red";
            case NOISE_REDUCTION -> buttonColor = "gold";
            case LIMITER -> buttonColor = "orange";
        }
    }

    /**
     * Sets the button label text based on its type.
     */
    public void setButtonText() {
        switch (buttonType) {
            case MUTE -> MIXER_BUTTON_LABEL.setText("M");
            case NOISE_REDUCTION -> MIXER_BUTTON_LABEL.setText("NR");
            case LIMITER -> MIXER_BUTTON_LABEL.setText("\uD802\uDCEB"); // Special Unicode character
        }
    }

    /**
     * Applies special styling for limiter buttons.
     * Rotates the label 90 degrees counter-clockwise for limiter type.
     */
    public void styleLimiterButton() {
        if (buttonType == ButtonType.LIMITER) {
            MIXER_BUTTON_LABEL.rotateProperty().set(-90);
        }
    }

    /**
     * Checks if the button is currently engaged.
     *
     * @return true if engaged, false otherwise
     */
    public boolean isEngaged() {
        return isEngaged;
    }

    /**
     * Sets the engaged state of the button.
     *
     * @param engaged The new engaged state
     */
    public void setEngaged(boolean engaged) {
        isEngaged = engaged;
    }

    /**
     * Gets the parent channel strip controller.
     *
     * @return The parent ChannelStripController instance
     */
    public ChannelStripController getParentController() {
        return parentController;
    }

    /**
     * Handles button press events.
     * Only responds to primary (left) mouse button clicks.
     * Toggles button state, appearance, and triggers associated feature.
     *
     * @param mouseEvent The mouse event that triggered this handler
     */
    public void onButtonPress(MouseEvent mouseEvent) {
        // Only handle left mouse button clicks
        if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }
        toggleButtonColor();
        setEngaged(!isEngaged);
        toggleButtonFeature();
    }

    /**
     * Toggles the button's associated feature based on its type.
     * For MUTE buttons, fires appropriate ChannelMuteEvent based on parent controller type.
     */
    private void toggleButtonFeature() {
        switch (buttonType) {
            case MUTE -> {
                // Handle input channel mute
                if (parentController instanceof InputChannelStripController) {
                    MIXER_BUTTON_ROOT.fireEvent(new ChannelMuteEvent(null, true, false, isEngaged));
                }
                // Handle output channel mute
                else if (parentController instanceof OutputChannelStripController) {
                    MIXER_BUTTON_ROOT.fireEvent(new ChannelMuteEvent(null, false, true, isEngaged));
                }
                // Handle regular channel mute
                else {
                    if (parentController.isDummy()) return;
                    MIXER_BUTTON_ROOT.fireEvent(new ChannelMuteEvent(parentController.getPlayerId(), false, false, isEngaged));
                }
            }
            case NOISE_REDUCTION -> {
            } // Not yet implemented
            case LIMITER -> {
            } // Not yet implemented
            default -> {
            }
        }
    }

    /**
     * Toggles the visual appearance of the button based on engaged state.
     * Engaged: black text, no effects
     * Not engaged: colored text with bloom effect
     */
    private void toggleButtonColor() {
        if (isEngaged) {
            // Set to default black when engaged
            MIXER_BUTTON_LABEL.setStyle("-fx-text-fill: black;");
            MIXER_BUTTON_LABEL.setEffect(null);
        }
        else {
            // Set to button-specific color with bloom effect when not engaged
            MIXER_BUTTON_LABEL.setStyle("-fx-text-fill: " + buttonColor + ";");
            MIXER_BUTTON_LABEL.setEffect(new Bloom());
        }
    }

    /**
     * Placeholder for hotkey binding functionality.
     * Currently not implemented.
     *
     * @param mouseEvent The mouse event for hotkey binding
     */
    public void bindControlToHotkey(MouseEvent mouseEvent) {
        /*if (!mouseEvent.getButton().equals(MouseButton.SECONDARY)){ return; }

        MIXER_BUTTON_LABEL.setStyle("-fx-text-fill: violet;");*/
    }
}