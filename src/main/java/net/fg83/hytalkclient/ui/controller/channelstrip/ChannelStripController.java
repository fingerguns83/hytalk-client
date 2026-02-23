package net.fg83.hytalkclient.ui.controller.channelstrip;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import net.fg83.hytalkclient.HytalkClientApplication;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.ui.controller.channelstrip.button.ButtonController;
import net.fg83.hytalkclient.ui.event.mixer.GainChangeEvent;
import net.fg83.hytalkclient.util.AppConstants;
import net.fg83.hytalkclient.util.ButtonType;

import java.io.IOException;
import java.util.UUID;

import static java.lang.Math.round;

/**
 * Controller for a channel strip in the audio mixer interface.
 * Manages fader controls, VU meter display, and channel-specific UI elements.
 */
public class ChannelStripController {
    // Root container for the entire channel strip UI
    @FXML
    private VBox CHANNEL_STRIP_ROOT;
    // Container for channel control buttons (mute, solo, etc.)
    @FXML
    private HBox CHANNEL_BUTTON_HOLDER;

    // Visual representation of the fader control
    protected ImageView faderCap;
    // Visual meter showing audio level
    protected Line VUMeter;
    // Label displaying the channel name
    protected Label scribbleStrip;

    // Unique identifier for the player associated with this channel
    protected UUID playerId;
    // Display name of the player
    protected String playerName;

    // Current Y position of the fader (-100 indicates uninitialized)
    protected double faderLocation = -100;

    // Last recorded Y position during mouse drag operations
    protected double lastY;
    // Flag indicating if the fader is currently being dragged
    protected boolean isPressed = false;

    // Flag indicating if this is a dummy channel (for testing/preview purposes)
    private boolean isDummy = false;


    /* Event Handlers */

    /**
     * Handles mouse scroll events on the fader to adjust gain.
     * Scrolling up increases gain, scrolling down decreases it.
     */
    @FXML
    protected void onMouseScrollFader(ScrollEvent scrollEvent) {
        // Adjust fader position based on scroll delta with sensitivity factor
        renderFader(faderLocation - (scrollEvent.getTotalDeltaY() * 0.2));
        setGain();
    }

    /**
     * Handles mouse click events on the fader.
     * Right-click resets the fader to approximately 80% position (unity gain).
     */
    @FXML
    protected void onMouseClickFader(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            // Reset fader to 20% from top (80% of maximum range)
            renderFader(AppConstants.UI.ChannelStrip.FADER_MAX_Y - (.8 * AppConstants.UI.ChannelStrip.FADER_MAX_Y));
            setGain();
        }
    }

    /**
     * Handles mouse press events on the fader to initiate dragging.
     * Changes cursor to closed hand and records initial position.
     */
    @FXML
    protected void onMousePressedFader(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) return;
        faderCap.setCursor(Cursor.CLOSED_HAND);
        lastY = round(mouseEvent.getScreenY());
        isPressed = true;
    }

    /**
     * Handles mouse release events on the fader to end dragging.
     * Changes cursor back to open hand.
     */
    @FXML
    protected void onMouseReleasedFader(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) return;
        faderCap.setCursor(Cursor.OPEN_HAND);
        isPressed = false;
    }

    /**
     * Handles mouse drag events on the fader to adjust gain.
     * Applies non-linear sensitivity to provide finer control for small movements.
     */
    @FXML
    protected void onMouseDragFader(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) return;
        if (!isPressed) return;

        double newY = round(mouseEvent.getScreenY());

        // Calculate the distance moved since last event
        double deltaY = lastY - newY;

        // Apply non-linear sensitivity curve (power of 1.2) for better control
        double sensitivity = Math.signum(deltaY) * Math.pow(Math.abs(deltaY), 1.2);
        double scalingFactor = 0.5;

        double valueDelta = sensitivity * scalingFactor;

        // Update fader position and gain
        renderFader(faderLocation - valueDelta);
        setGain();

        lastY = newY;
    }

    // Getter for player ID
    public UUID getPlayerId() {
        return playerId;
    }

    // Setter for player ID
    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    // Getter for player name
    public String getPlayerName() {
        return playerName;
    }

    // Setter for player name
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    // Getter for root element ID
    public String getRootId() {
        return CHANNEL_STRIP_ROOT.getId();
    }

    // Setter for root element ID
    public void setRootId(String rootId) {
        CHANNEL_STRIP_ROOT.setId(rootId);
    }

    // Getter for dummy flag
    public boolean isDummy() {
        return isDummy;
    }

    /**
     * Initializes the channel strip with all required components.
     *
     * @param applicationState The application state object
     * @param isDummy          Whether this is a dummy channel for testing
     * @throws IOException If FXML loading fails
     */
    public void setup(ApplicationState applicationState, boolean isDummy) throws IOException {
        this.isDummy = isDummy;
        // Lookup UI elements from the FXML
        this.scribbleStrip = (Label) CHANNEL_STRIP_ROOT.lookup(".scribble-strip");
        initializeScribbleStrip();
        initializeFaderCap();
        initializeFaderLocation();
        initializeVUMeter();
        initializeButtons();
    }

    /**
     * Initializes the scribble strip label with the player name.
     */
    protected void initializeScribbleStrip() {
        scribbleStrip.setText(playerName);
        scribbleStrip.getTooltip().setText(playerName);
    }

    /**
     * Initializes the fader cap image view by looking it up in the scene graph.
     */
    protected void initializeFaderCap() {
        faderCap = (ImageView) CHANNEL_STRIP_ROOT.lookup(".fader-cap");
    }

    /**
     * Initializes the fader location from the current layout position.
     * Only sets if not already initialized (default value is -100).
     */
    protected void initializeFaderLocation() {
        if (faderLocation == -100) {
            faderLocation = faderCap.getLayoutY();
        }
    }

    /**
     * Initializes the VU meter line element by looking it up in the scene graph.
     */
    protected void initializeVUMeter() {
        VUMeter = (Line) CHANNEL_STRIP_ROOT.lookup(".vu-meter-mask");
    }

    /**
     * Initializes all buttons on the channel strip.
     *
     * @throws IOException If button FXML loading fails
     */
    protected void initializeButtons() throws IOException {
        initializeMuteButton(false);
    }

    /**
     * Loads and initializes the mute button for this channel strip.
     *
     * @param addMargin Whether to add right margin to the button
     * @throws IOException If button FXML loading fails
     */
    protected void initializeMuteButton(boolean addMargin) throws IOException {
        FXMLLoader muteButtonloader = new FXMLLoader(HytalkClientApplication.class.getResource("widget/button/Button.fxml"));
        StackPane muteButton = muteButtonloader.load();
        ButtonController muteButtonController = muteButtonloader.getController();
        muteButtonController.setButtonType(ButtonType.MUTE);
        if (addMargin) {
            HBox.setMargin(muteButton, new Insets(0, 10, 0, 0));
        }
        CHANNEL_BUTTON_HOLDER.getChildren().add(muteButton);
        muteButtonController.setup(this);
    }

    /**
     * Updates the visual position of the fader, constrained to valid range.
     *
     * @param location The desired Y position for the fader
     */
    protected void renderFader(double location) {
        // Clamp fader location between min and max values
        faderLocation = Math.max(AppConstants.UI.ChannelStrip.FADER_MIN_Y, Math.min(AppConstants.UI.ChannelStrip.FADER_MAX_Y, location));
        faderCap.setLayoutY(faderLocation);
    }

    /**
     * Fires a gain change event based on the current fader position.
     * Does nothing if this is a dummy channel.
     */
    protected void setGain() {
        if (isDummy) return;
        CHANNEL_STRIP_ROOT.fireEvent(new GainChangeEvent(playerId, calculateGainPercentage()));
    }

    /**
     * Calculates the gain percentage (0.0 to 1.0) based on fader position.
     *
     * @return The gain value as a percentage
     */
    protected float calculateGainPercentage() {
        return (float) (1 - (faderLocation - AppConstants.UI.ChannelStrip.FADER_MIN_Y) / (AppConstants.UI.ChannelStrip.FADER_MAX_Y - AppConstants.UI.ChannelStrip.FADER_MIN_Y));
    }

    /**
     * Updates the VU meter display based on the audio level.
     * For dummy channels, generates random values for testing.
     *
     * @param level The audio level (0.0 to 1.0)
     */
    public void updateMeter(float level) {
        if (isDummy) {
            // Generate random meter value for dummy channels
            VUMeter.setEndY(Math.round((255 - (Math.random() * 255))) + 5);
        }
        // Convert level to Y position (inverted scale, offset by 5)
        VUMeter.setEndY(Math.round((255 - (level * 255))) + 5);
    }
}