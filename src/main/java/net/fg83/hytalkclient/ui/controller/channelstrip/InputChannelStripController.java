package net.fg83.hytalkclient.ui.controller.channelstrip;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import net.fg83.hytalkclient.HytalkClientApplication;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.ui.controller.channelstrip.button.ButtonController;
import net.fg83.hytalkclient.ui.event.mixer.AudioDeviceEvent;
import net.fg83.hytalkclient.ui.event.mixer.GainChangeEvent;
import net.fg83.hytalkclient.util.ButtonType;

import java.io.IOException;
import java.util.List;

/**
 * Controller for the input channel strip UI component.
 * Manages audio input device selection, fader controls, and VU meter display.
 */
public class InputChannelStripController extends ChannelStripController {
    // Root container for the input channel strip UI
    @FXML
    private VBox INPUT_CHANNEL_STRIP_ROOT;

    // Container for channel control buttons (mute, noise reduction, etc.)
    @FXML
    private HBox CHANNEL_BUTTON_HOLDER;

    // Dropdown menu for selecting audio input devices
    @FXML
    private MenuButton INPUT_DEVICE_SELECTOR;

    // Toggle group to ensure only one input device is selected at a time
    private final ToggleGroup inputDeviceToggleGroup = new ToggleGroup();

    /**
     * Initializes the input channel strip with all necessary components.
     *
     * @param applicationState The current application state containing audio configuration
     * @param isDummy          Whether this is a dummy/placeholder channel strip
     * @throws IOException If FXML loading fails
     */
    @Override
    public void setup(ApplicationState applicationState, boolean isDummy) throws IOException {
        initializeDeviceSelector(applicationState);
        initializeFaderCap();
        initializeFaderLocation();
        initializeButtons();
        initializeVUMeter();
    }

    /**
     * Locates and assigns the fader cap image from the FXML scene graph.
     */
    @Override
    protected void initializeFaderCap() {
        faderCap = (ImageView) INPUT_CHANNEL_STRIP_ROOT.lookup(".fader-cap");
    }

    /**
     * Locates and assigns the VU meter line element from the FXML scene graph.
     */
    @Override
    protected void initializeVUMeter() {
        VUMeter = (Line) INPUT_CHANNEL_STRIP_ROOT.lookup(".vu-meter-mask");
    }

    /**
     * Initializes all control buttons for the input channel (mute, etc.).
     *
     * @throws IOException If button FXML loading fails
     */
    @Override
    protected void initializeButtons() throws IOException {
        initializeMuteButton(false);
        //initializeNoiseReductionButton(); // Currently disabled
    }

    /**
     * Creates and initializes the noise reduction button.
     * This method is currently not called (see initializeButtons).
     *
     * @throws IOException If button FXML loading fails
     */
    private void initializeNoiseReductionButton() throws IOException {
        // Load button FXML template
        FXMLLoader nrButtonLoader = new FXMLLoader(HytalkClientApplication.class.getResource("widget/button/Button.fxml"));
        StackPane nrButton = nrButtonLoader.load();
        ButtonController nrButtonController = nrButtonLoader.getController();
        // Configure as noise reduction button
        nrButtonController.setButtonType(ButtonType.NOISE_REDUCTION);
        // Add to button container and complete setup
        CHANNEL_BUTTON_HOLDER.getChildren().add(nrButton);
        nrButtonController.setup(this);
    }

    /**
     * Initializes the input device selector dropdown with available devices.
     * Sets the currently selected device or shows "Default Input Device" if none selected.
     *
     * @param applicationState The application state containing audio device configuration
     */
    private void initializeDeviceSelector(ApplicationState applicationState) {
        // Get currently selected input device from audio manager
        AudioIOManager.AudioDevice selected = applicationState.getAudioStreamManager().getAudioIOManager().getSelectedInputDevice();
        if (selected == null) {
            // Show default device text if no device explicitly selected
            INPUT_DEVICE_SELECTOR.getTooltip().setText("Default Input Device");
            return;
        }
        else {
            // Show selected device name in tooltip
            INPUT_DEVICE_SELECTOR.getTooltip().setText(selected.name());
        }
        // Populate device selector menu
        setDevices(AudioIOManager.getInputDevices(), selected);
    }

    /**
     * Fires a gain change event when the input fader is adjusted.
     * Notifies listeners of the new gain percentage.
     */
    @Override
    protected void setGain() {
        INPUT_CHANNEL_STRIP_ROOT.fireEvent(new GainChangeEvent(GainChangeEvent.INPUT_GAIN_CHANGE_EVENT, calculateGainPercentage()));
    }

    /**
     * Handles user selection of a different input device from the dropdown.
     * Updates tooltip and fires device change event.
     *
     * @param device The name of the newly selected device
     */
    private void handleDeviceSelection(String device) {
        // Update tooltip to show selected device
        INPUT_DEVICE_SELECTOR.getTooltip().setText(device);
        // Find the device object and fire change event
        INPUT_CHANNEL_STRIP_ROOT.fireEvent(new AudioDeviceEvent(AudioDeviceEvent.INPUT_DEVICE_CHANGED, AudioIOManager.getInputDevices().stream().filter(d -> d.name().equals(device)).findFirst().orElse(null)));
    }

    /**
     * Populates the device selector menu with available input devices.
     * Creates radio menu items for each device and marks the selected one.
     *
     * @param devices  List of available audio input devices
     * @param selected The currently selected device (may be null)
     */
    public void setDevices(List<AudioIOManager.AudioDevice> devices, AudioIOManager.AudioDevice selected) {
        // Clear existing menu items
        INPUT_DEVICE_SELECTOR.getItems().clear();
        // Create menu item for each device
        for (AudioIOManager.AudioDevice device : devices) {
            RadioMenuItem item = new RadioMenuItem(device.name());
            // Set up selection handler
            item.setOnAction(e -> handleDeviceSelection(device.name()));

            // Mark as selected if this is the current device
            if (selected != null && device.name().equals(selected.name())) {
                item.setSelected(true);
            }

            INPUT_DEVICE_SELECTOR.getItems().add(item);
        }

        // Add all radio items to toggle group for mutual exclusion
        INPUT_DEVICE_SELECTOR.getItems().forEach(item -> {
            if (item instanceof RadioMenuItem) {
                ((RadioMenuItem) item).setToggleGroup(inputDeviceToggleGroup);
            }
        });
    }
}