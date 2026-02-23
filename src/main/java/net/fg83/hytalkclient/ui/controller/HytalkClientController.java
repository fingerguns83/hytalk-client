// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import net.fg83.hytalkclient.ui.event.*;
import net.fg83.hytalkclient.ui.event.handler.MixerEventHandler;
import net.fg83.hytalkclient.ui.event.mixer.AudioDeviceEvent;
import net.fg83.hytalkclient.ui.event.mixer.ChannelMuteEvent;
import net.fg83.hytalkclient.ui.event.mixer.GainChangeEvent;
import net.fg83.hytalkclient.ui.event.mixer.RegisterChannelControllerEvent;
import net.fg83.hytalkclient.ui.event.view.ResizeEvent;
import net.fg83.hytalkclient.ui.event.view.ViewEvent;
import net.fg83.hytalkclient.util.WindowDimensions;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.message.MessageType;

import java.io.IOException;

import static net.fg83.hytalkclient.HytalkClientApplication.getView;

/**
 * Main controller for the Hytalk client application.
 * Manages view navigation, event handling, and coordination between different application components.
 */
public class HytalkClientController {
    // Holds the application state including managers for connections, pairing, audio, etc.
    private ApplicationState applicationState;

    // Root pane of the client UI, injected from FXML
    @FXML
    private AnchorPane CLIENT_ROOT;


    /**
     * Initializes the controller with application state and sets up all event handlers.
     * Displays the launch view upon completion.
     *
     * @param applicationState the application state containing all managers and shared data
     */
    public void setup(ApplicationState applicationState) {
        this.applicationState = applicationState;

        // Register handlers for different categories of events
        setupViewEventHandlers();
        setupUtilityEventHandlers();
        setupConnectionMessageHandlers();
        setupPairingEventHandlers();
        setupMixerEventHandlers();

        // Show the initial launch view
        CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_LAUNCH_VIEW));
    }


    /**
     * Registers event handlers for view navigation events.
     * Each handler delegates to the corresponding view setup method.
     */
    private void setupViewEventHandlers() {
        CLIENT_ROOT.addEventHandler(ViewEvent.SHOW_LAUNCH_VIEW, event -> handleViewSetup(this::setupLaunchView));
        CLIENT_ROOT.addEventHandler(ViewEvent.SHOW_CONNECTION_VIEW, event -> handleViewSetup(this::setupConnectionView));
        CLIENT_ROOT.addEventHandler(ViewEvent.SHOW_CONNECTION_PENDING_VIEW, event -> handleViewSetup(this::setupConnectionPendingView));
        CLIENT_ROOT.addEventHandler(ViewEvent.SHOW_PAIRING_VIEW, event -> handleViewSetup(this::setupPairingView));
        CLIENT_ROOT.addEventHandler(ViewEvent.SHOW_MIXER_VIEW, event -> handleViewSetup(this::setupMixerView));
    }

    /**
     * Wraps view setup methods to handle IOExceptions gracefully.
     * Displays an error dialog if view loading fails.
     *
     * @param setupMethod the view setup method to execute
     */
    private void handleViewSetup(IOExceptionRunnable setupMethod) {
        try {
            setupMethod.run();
        }
        catch (IOException e) {
            applicationState.getErrorDialogManager().showError("View Error", "Failed to load view: " + e.getMessage());
        }
    }

    /**
     * Functional interface for view setup methods that may throw IOException.
     */
    @FunctionalInterface
    private interface IOExceptionRunnable {
        void run() throws IOException;
    }


    /**
     * Sets up and displays the launch view.
     * This is the initial view shown when the application starts.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    private void setupLaunchView() throws IOException {
        System.out.println("Displaying launch view");

        // Navigate to the launch view and get its controller
        LaunchController controller = applicationState.getViewNavigationManager().navigateToView(
                getView("subviews/LaunchView.fxml"),
                null,
                WindowDimensions.LAUNCH_WIDTH,
                WindowDimensions.LAUNCH_HEIGHT
        );

        // Start the launch controller
        controller.run();
    }

    /**
     * Sets up and displays the connection view.
     * Allows users to enter server connection details.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    private void setupConnectionView() throws IOException {
        System.out.println("Displaying connection view");

        applicationState.getViewNavigationManager().navigateToView(
                getView("subviews/ConnectionView.fxml"),
                null,
                WindowDimensions.CONNECTION_WIDTH,
                WindowDimensions.CONNECTION_HEIGHT
        );
    }

    /**
     * Sets up and displays the connection pending view.
     * Shown while waiting for a connection to be established.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    private void setupConnectionPendingView() throws IOException {
        System.out.println("Displaying connection pending view");

        applicationState.getViewNavigationManager().navigateToView(
                getView("subviews/ConnectionPendingView.fxml"),
                null,
                WindowDimensions.CONNECTION_WIDTH,
                WindowDimensions.CONNECTION_HEIGHT
        );
    }

    /**
     * Sets up and displays the pairing view.
     * Shows the pairing code and expiration time for device pairing.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    private void setupPairingView() throws IOException {
        System.out.println("Displaying pairing view");

        // Navigate to pairing view and initialize it with pairing code and expiration
        PairingController controller = applicationState.getViewNavigationManager().navigateToView(
                getView("subviews/PairingView.fxml"),
                pairingController -> {
                    pairingController.setup(
                            applicationState.getPairingManager().getPairingCode(),
                            applicationState.getPairingManager().getPairingExpiration()
                    );
                },
                WindowDimensions.PAIRING_WIDTH,
                WindowDimensions.PAIRING_HEIGHT
        );
    }

    /**
     * Sets up and displays the mixer view.
     * Initializes audio input/output and starts meter updates for audio visualization.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    private void setupMixerView() throws IOException {
        System.out.println("Displaying mixer view");

        // Navigate to mixer view and initialize it with application state
        MixerController controller = applicationState.getViewNavigationManager().navigateToView(
                getView("subviews/MixerView.fxml"),
                mixerController -> {
                    mixerController.setup(applicationState);
                },
                WindowDimensions.MIXER_WIDTH,
                WindowDimensions.MIXER_HEIGHT
        );

        // Start audio meter updates for level visualization
        try {
            applicationState.getMixerManager().startMeterUpdates();
        }
        catch (Exception e) {
            applicationState.getErrorDialogManager().showError("Audio Error", "Failed to start audio input: " + e.getMessage());
        }
        // Start audio input and output streams
        try {
            applicationState.getAudioStreamManager().startInput(applicationState.getAudioNetworkManager()::onCapturedFrame);
            applicationState.getAudioStreamManager().startOutput();
            System.out.println("Started audio IO");
        }
        catch (Exception e) {
            applicationState.getErrorDialogManager().showError(
                    "Audio Error",
                    "Failed to start audio input: " + e.getMessage()
            );
        }
    }


    /**
     * Registers handlers for connection-related messages from the server.
     * Responds to PAIR and READY messages by navigating to appropriate views.
     */
    private void setupConnectionMessageHandlers() {
        applicationState.getConnectionManager().addMessageHandler(messageEvent -> {
            MessageType type = messageEvent.getType();

            switch (type) {
                case PAIR ->
                        CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_PAIRING_VIEW)); // Server requests pairing
                case READY ->
                        CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_MIXER_VIEW)); // Connection ready for audio
                default -> {
                }
            }
        });
    }


    /**
     * Registers event handlers for pairing-related events.
     * Handles pairing expiration and cancellation scenarios.
     */
    private void setupPairingEventHandlers() {
        CLIENT_ROOT.addEventHandler(PairingEvent.PAIRING_EXPIRED, e -> handlePairingExpired());
        CLIENT_ROOT.addEventHandler(PairingEvent.PAIRING_CANCELLED, e -> handlePairingCancelled());
    }

    /**
     * Handles pairing expiration by clearing pairing data and returning to connection view.
     */
    private void handlePairingExpired() {
        System.out.println("Pairing expired");
        // Clear pairing code and expiration time
        applicationState.getPairingManager().setPairingCode(null);
        applicationState.getPairingManager().setPairingExpiration(null);
        try {
            setupConnectionView();
        }
        catch (IOException e) {
            applicationState.getErrorDialogManager().showError("View Error", "Failed to load connection view");
        }
    }

    /**
     * Handles user-initiated pairing cancellation by disconnecting and returning to connection view.
     */
    private void handlePairingCancelled() {
        System.out.println("Pairing cancelled by user");
        // Disconnect from server
        applicationState.getConnectionManager().disconnect();
        // Clear pairing data
        applicationState.getPairingManager().setPairingCode(null);
        applicationState.getPairingManager().setPairingExpiration(null);
        try {
            setupConnectionView();
        }
        catch (IOException e) {
            applicationState.getErrorDialogManager().showError("View Error", "Failed to load connection view");
        }
    }

    /**
     * Registers event handlers for mixer-related events.
     * Includes handlers for channel control, gain changes, mute events, and audio device changes.
     */
    private void setupMixerEventHandlers() {
        // Register channel controller when it's created
        CLIENT_ROOT.addEventHandler(RegisterChannelControllerEvent.REGISTER_CHANNEL_CONTROLLER_EVENT, (RegisterChannelControllerEvent event) -> MixerEventHandler.handleControllerRegistration(event, applicationState));
        // Handle gain changes for different audio channels
        CLIENT_ROOT.addEventHandler(GainChangeEvent.PLAYER_GAIN_CHANGE_EVENT, (GainChangeEvent event) -> MixerEventHandler.handlePlayerGainChange(event, applicationState));
        CLIENT_ROOT.addEventHandler(GainChangeEvent.INPUT_GAIN_CHANGE_EVENT, (GainChangeEvent event) -> MixerEventHandler.handleInputGainChange(event, applicationState));
        CLIENT_ROOT.addEventHandler(GainChangeEvent.OUTPUT_GAIN_CHANGE_EVENT, (GainChangeEvent event) -> MixerEventHandler.handleOutputGainChange(event, applicationState));
        // Handle channel mute/unmute events
        CLIENT_ROOT.addEventHandler(ChannelMuteEvent.CHANNEL_MUTE_EVENT, (ChannelMuteEvent event) -> MixerEventHandler.handleChannelMuteEvent(event, applicationState));

        // Handle audio device changes (input/output device selection)
        CLIENT_ROOT.addEventHandler(AudioDeviceEvent.INPUT_DEVICE_CHANGED, (AudioDeviceEvent event) -> MixerEventHandler.handleInputDeviceChange(event, applicationState));
        CLIENT_ROOT.addEventHandler(AudioDeviceEvent.OUTPUT_DEVICE_CHANGED, (AudioDeviceEvent event) -> MixerEventHandler.handleOutputDeviceChange(event, applicationState));
    }


    /**
     * Registers handlers for utility events such as connection setup and window resizing.
     */
    private void setupUtilityEventHandlers() {
        CLIENT_ROOT.addEventHandler(ConnectionSetupEvent.CONNECTION_SETUP_EVENT, this::handleConnectionSetup);
        CLIENT_ROOT.addEventHandler(ResizeEvent.RESIZE_EVENT, this::handleResize);
    }

    /**
     * Handles connection setup by initiating a connection to the specified server.
     * Displays the connection pending view while connecting.
     *
     * @param event the connection setup event containing server address and port
     */
    private void handleConnectionSetup(ConnectionSetupEvent event) {
        System.out.println("Attempting connection to " + event.getServerAddress() + ":" + event.getServerPort());

        // Show connection pending view
        CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_CONNECTION_PENDING_VIEW));

        // Initiate connection to server
        applicationState.getConnectionManager().connect(event.getServerAddress(), event.getServerPort());
    }

    /**
     * Handles window resize events by updating the stored window dimensions.
     *
     * @param event the resize event containing new width and height
     */
    private void handleResize(ResizeEvent event) {
        applicationState.getViewNavigationManager().setDimensions(event.getWidth(), event.getHeight());
    }
}