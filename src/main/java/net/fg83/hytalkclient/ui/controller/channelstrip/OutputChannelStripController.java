// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.controller.channelstrip;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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
 * Controller for the output channel strip UI component.
 * Manages audio output device selection, fader controls, and VU meter display.
 */
public class OutputChannelStripController extends ChannelStripController {
    // Root container for the output channel strip UI
    @FXML
    private VBox OUTPUT_CHANNEL_STRIP_ROOT;

    // Container for channel control buttons (mute, limiter, etc.)
    @FXML
    private HBox CHANNEL_BUTTON_HOLDER;

    // Dropdown menu for selecting audio output devices
    @FXML
    private MenuButton OUTPUT_DEVICE_SELECTOR;

    // Toggle group to ensure only one output device is selected at a time
    private final ToggleGroup outputDeviceToggleGroup = new ToggleGroup();

    /**
     * Initializes the output channel strip with all necessary components.
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
        initializeVUMeter();
        initializeButtons();
    }

    /**
     * Locates and assigns the fader cap image from the FXML scene graph.
     */
    @Override
    protected void initializeFaderCap() {
        faderCap = (ImageView) OUTPUT_CHANNEL_STRIP_ROOT.lookup(".fader-cap");
    }

    /**
     * Locates and assigns the VU meter line element from the FXML scene graph.
     */
    @Override
    protected void initializeVUMeter() {
        VUMeter = (Line) OUTPUT_CHANNEL_STRIP_ROOT.lookup(".vu-meter-mask");
    }

    /**
     * Initializes all control buttons for the output channel (mute, etc.).
     *
     * @throws IOException If button FXML loading fails
     */
    @Override
    protected void initializeButtons() throws IOException {
        initializeMuteButton(false);
        //initializeLimiterButton(); // Currently disabled
    }

    /**
     * Creates and initializes the limiter button.
     * This method is currently not called (see initializeButtons).
     *
     * @throws IOException If button FXML loading fails
     */
    private void initializeLimiterButton() throws IOException {
        // Load button FXML template
        FXMLLoader limButtonLoader = new FXMLLoader(HytalkClientApplication.class.getResource("widget/button/Button.fxml"));
        StackPane limButton = limButtonLoader.load();
        ButtonController limButtonController = limButtonLoader.getController();
        // Configure as limiter button
        limButtonController.setButtonType(ButtonType.LIMITER);
        // Add to button container and complete setup
        CHANNEL_BUTTON_HOLDER.getChildren().add(limButton);
        limButtonController.setup(this);
    }

    /**
     * Initializes the output device selector dropdown with available devices.
     * Sets the currently selected device or shows "Default Output Device" if none selected.
     *
     * @param applicationState The application state containing audio device configuration
     */
    private void initializeDeviceSelector(ApplicationState applicationState) {
        // Get currently selected output device from audio manager
        AudioIOManager.AudioDevice selected = applicationState.getAudioStreamManager().getAudioIOManager().getSelectedOutputDevice();
        if (selected == null) {
            // Show default device text if no device explicitly selected
            OUTPUT_DEVICE_SELECTOR.getTooltip().setText("Default Output Device");
            return;
        }
        else {
            // Show selected device name in tooltip
            OUTPUT_DEVICE_SELECTOR.getTooltip().setText(selected.name());
        }
        // Populate device selector menu
        setDevices(AudioIOManager.getOutputDevices(), selected);
    }


    /**
     * Fires a gain change event when the output fader is adjusted.
     * Notifies listeners of the new gain percentage.
     */
    @Override
    public void setGain() {
        OUTPUT_CHANNEL_STRIP_ROOT.fireEvent(new GainChangeEvent(GainChangeEvent.OUTPUT_GAIN_CHANGE_EVENT, calculateGainPercentage()));
    }

    /**
     * Handles user selection of a different output device from the dropdown.
     * Updates tooltip and fires device change event.
     *
     * @param device The name of the newly selected device
     */
    private void handleDeviceSelection(String device) {
        // Update tooltip to show selected device
        OUTPUT_DEVICE_SELECTOR.getTooltip().setText(device);
        // Find the device object and fire change event
        OUTPUT_CHANNEL_STRIP_ROOT.fireEvent(new AudioDeviceEvent(AudioDeviceEvent.OUTPUT_DEVICE_CHANGED, AudioIOManager.getInputDevices().stream().filter(d -> d.name().equals(device)).findFirst().orElse(null)));
    }

    /**
     * Populates the device selector menu with available output devices.
     * Creates radio menu items for each device and marks the selected one.
     *
     * @param devices  List of available audio output devices
     * @param selected The currently selected device (may be null)
     */
    public void setDevices(List<AudioIOManager.AudioDevice> devices, AudioIOManager.AudioDevice selected) {
        // Clear existing menu items
        OUTPUT_DEVICE_SELECTOR.getItems().clear();
        // Create menu item for each device
        for (AudioIOManager.AudioDevice device : devices) {
            RadioMenuItem item = new RadioMenuItem(device.name());
            // Set up selection handler
            item.setOnAction(e -> handleDeviceSelection(device.name()));

            // Mark as selected if this is the current device
            if (selected != null && device.name().equals(selected.name())) {
                item.setSelected(true);
            }

            OUTPUT_DEVICE_SELECTOR.getItems().add(item);
        }

        // Add all radio items to toggle group for mutual exclusion
        OUTPUT_DEVICE_SELECTOR.getItems().forEach(item -> {
            if (item instanceof RadioMenuItem) {
                ((RadioMenuItem) item).setToggleGroup(outputDeviceToggleGroup);
            }
        });
    }
}