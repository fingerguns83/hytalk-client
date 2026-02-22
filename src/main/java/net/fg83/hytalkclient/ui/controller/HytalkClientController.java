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

public class HytalkClientController {
    // Dependencies
    private ApplicationState applicationState;


    @FXML
    private AnchorPane CLIENT_ROOT;

    @FXML
    private void initialize() {
        // FXML initialization - nothing yet
    }



    public void setup(ApplicationState applicationState) {
        this.applicationState = applicationState;

        setupViewEventHandlers();
        setupUtilityEventHandlers();
        setupConnectionMessageHandlers();
        setupPairingEventHandlers();
        setupMixerEventHandlers();

        // Show initial launch view
        CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_LAUNCH_VIEW));
    }



    /* VIEW EVENT HANDLERS */
    private void setupViewEventHandlers() {
        CLIENT_ROOT.addEventHandler(ViewEvent.SHOW_LAUNCH_VIEW, event -> handleViewSetup(this::setupLaunchView));
        CLIENT_ROOT.addEventHandler(ViewEvent.SHOW_CONNECTION_VIEW, event -> handleViewSetup(this::setupConnectionView));
        CLIENT_ROOT.addEventHandler(ViewEvent.SHOW_CONNECTION_PENDING_VIEW, event -> handleViewSetup(this::setupConnectionPendingView));
        CLIENT_ROOT.addEventHandler(ViewEvent.SHOW_PAIRING_VIEW, event -> handleViewSetup(this::setupPairingView));
        CLIENT_ROOT.addEventHandler(ViewEvent.SHOW_MIXER_VIEW, event -> handleViewSetup(this::setupMixerView));
    }

    private void handleViewSetup(IOExceptionRunnable setupMethod) {
        try {
            setupMethod.run();
        } catch (IOException e) {
            applicationState.getErrorDialogManager().showError("View Error", "Failed to load view: " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface IOExceptionRunnable {
        void run() throws IOException;
    }



    /* VIEW SETUP METHODS */
    private void setupLaunchView() throws IOException {
        System.out.println("Displaying launch view");

        LaunchController controller = applicationState.getViewNavigationManager().navigateToView(
                getView("subviews/LaunchView.fxml"),
                null,
                WindowDimensions.LAUNCH_WIDTH,
                WindowDimensions.LAUNCH_HEIGHT
        );

        controller.run();
    }

    private void setupConnectionView() throws IOException {
        System.out.println("Displaying connection view");

        applicationState.getViewNavigationManager().navigateToView(
                getView("subviews/ConnectionView.fxml"),
                null,
                WindowDimensions.CONNECTION_WIDTH,
                WindowDimensions.CONNECTION_HEIGHT
        );
    }

    private void setupConnectionPendingView() throws IOException {
        System.out.println("Displaying connection pending view");

        applicationState.getViewNavigationManager().navigateToView(
                getView("subviews/ConnectionPendingView.fxml"),
                null,
                WindowDimensions.CONNECTION_WIDTH,
                WindowDimensions.CONNECTION_HEIGHT
        );
    }

    private void setupPairingView() throws IOException {
        System.out.println("Displaying pairing view");

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

    private void setupMixerView() throws IOException {
        System.out.println("Displaying mixer view");

        MixerController controller = applicationState.getViewNavigationManager().navigateToView(
                getView("subviews/MixerView.fxml"),
                mixerController -> {
                    mixerController.setup(applicationState);
                },
                WindowDimensions.MIXER_WIDTH,
                WindowDimensions.MIXER_HEIGHT
        );

        try {

            applicationState.getMixerManager().startMeterUpdates();
        }
        catch (Exception e) {
            applicationState.getErrorDialogManager().showError("Audio Error", "Failed to start audio input: " + e.getMessage());
        }
        try {
            applicationState.getAudioStreamManager().startInput(applicationState.getAudioNetworkManager()::onCapturedFrame);
            applicationState.getAudioStreamManager().startOutput();
            System.out.println("Started audio IO");
        } catch (Exception e) {
            applicationState.getErrorDialogManager().showError(
                    "Audio Error",
                    "Failed to start audio input: " + e.getMessage()
            );
        }
    }


    /* CONNECTION MESSAGE HANDLERS */
    private void setupConnectionMessageHandlers() {
        applicationState.getConnectionManager().addMessageHandler(messageEvent -> {
            MessageType type = messageEvent.getType();

            switch (type) {
                case PAIR -> CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_PAIRING_VIEW));
                case READY -> CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_MIXER_VIEW));
                default -> {}
            }
        });
    }


    /* PAIRING EVENT HANDLERS */
    private void setupPairingEventHandlers() {
        CLIENT_ROOT.addEventHandler(PairingEvent.PAIRING_EXPIRED, e -> handlePairingExpired());
        CLIENT_ROOT.addEventHandler(PairingEvent.PAIRING_CANCELLED, e -> handlePairingCancelled());
    }

    private void handlePairingExpired() {
        System.out.println("Pairing expired");
        applicationState.getPairingManager().setPairingCode(null);
        applicationState.getPairingManager().setPairingExpiration(null);
        try {
            setupConnectionView();
        } catch (IOException e) {
            applicationState.getErrorDialogManager().showError("View Error", "Failed to load connection view");
        }
    }

    private void handlePairingCancelled() {
        System.out.println("Pairing cancelled by user");
        applicationState.getConnectionManager().disconnect();
        applicationState.getPairingManager().setPairingCode(null);
        applicationState.getPairingManager().setPairingExpiration(null);
        try {
            setupConnectionView();
        } catch (IOException e) {
            applicationState.getErrorDialogManager().showError("View Error", "Failed to load connection view");
        }
    }

    /* MIXER EVENT HANDLERS */

    /* MIXER EVENT HANDLERS */
    private void setupMixerEventHandlers() {
        CLIENT_ROOT.addEventHandler(RegisterChannelControllerEvent.REGISTER_CHANNEL_CONTROLLER_EVENT, (RegisterChannelControllerEvent event) -> MixerEventHandler.handleControllerRegistration(event, applicationState));
        CLIENT_ROOT.addEventHandler(GainChangeEvent.PLAYER_GAIN_CHANGE_EVENT, (GainChangeEvent event) -> MixerEventHandler.handlePlayerGainChange(event, applicationState));
        CLIENT_ROOT.addEventHandler(GainChangeEvent.INPUT_GAIN_CHANGE_EVENT, (GainChangeEvent event) -> MixerEventHandler.handleInputGainChange(event, applicationState));
        CLIENT_ROOT.addEventHandler(GainChangeEvent.OUTPUT_GAIN_CHANGE_EVENT, (GainChangeEvent event) -> MixerEventHandler.handleOutputGainChange(event, applicationState));
        CLIENT_ROOT.addEventHandler(ChannelMuteEvent.CHANNEL_MUTE_EVENT, (ChannelMuteEvent event) -> MixerEventHandler.handleChannelMuteEvent(event, applicationState));

        CLIENT_ROOT.addEventHandler(AudioDeviceEvent.INPUT_DEVICE_CHANGED, (AudioDeviceEvent event) -> MixerEventHandler.handleInputDeviceChange(event, applicationState));
        CLIENT_ROOT.addEventHandler(AudioDeviceEvent.OUTPUT_DEVICE_CHANGED, (AudioDeviceEvent event) -> MixerEventHandler.handleOutputDeviceChange(event, applicationState));
    }


    /* UTILITY EVENT HANDLERS */
    private void setupUtilityEventHandlers() {
        CLIENT_ROOT.addEventHandler(ConnectionSetupEvent.CONNECTION_SETUP_EVENT, this::handleConnectionSetup);
        CLIENT_ROOT.addEventHandler(ResizeEvent.RESIZE_EVENT, this::handleResize);
    }

    private void handleConnectionSetup(ConnectionSetupEvent event) {
        System.out.println("Attempting connection to " + event.getServerAddress() + ":" + event.getServerPort());

        CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_CONNECTION_PENDING_VIEW));

        applicationState.getConnectionManager().connect(event.getServerAddress(), event.getServerPort());
    }

    private void handleResize(ResizeEvent event) {
        applicationState.getViewNavigationManager().setDimensions(event.getWidth(), event.getHeight());
    }



    /* UTILITY METHODS */
}