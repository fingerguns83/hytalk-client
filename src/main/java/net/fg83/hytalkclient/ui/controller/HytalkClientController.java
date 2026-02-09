package net.fg83.hytalkclient.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import net.fg83.hytalkclient.config.AppConfig;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.service.ConnectionService;
import net.fg83.hytalkclient.service.ErrorDialogService;
import net.fg83.hytalkclient.service.ViewNavigationService;
import net.fg83.hytalkclient.ui.event.ConnectionSetupEvent;
import net.fg83.hytalkclient.ui.event.ViewEvent;
import net.fg83.hytalkclient.util.message.MessageType;

import java.io.IOException;

import static net.fg83.hytalkclient.HytalkClientApplication.getView;

public class HytalkClientController {
    // Dependencies
    private ApplicationState state;
    private ViewNavigationService viewNavigationService;
    private ConnectionService connectionService;
    private ErrorDialogService errorDialogService;
    private Stage stage;

    @FXML
    private AnchorPane CLIENT_ROOT;

    @FXML
    private void initialize() {
        // FXML initialization - nothing yet
    }

    public void setup(
            ApplicationState applicationState,
            ViewNavigationService viewNavigationService,
            ConnectionService connectionService,
            ErrorDialogService errorDialogService,
            Stage stage
    ) {
        this.state = applicationState;
        this.viewNavigationService = viewNavigationService;
        this.connectionService = connectionService;
        this.errorDialogService = errorDialogService;
        this.stage = stage;

        setupViewEventHandlers();
        setupLogicEventHandlers();
        setupConnectionMessageHandlers();

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
            errorDialogService.showError("View Error", "Failed to load view: " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface IOExceptionRunnable {
        void run() throws IOException;
    }

    /* VIEW SETUP METHODS */
    private void setupLaunchView() throws IOException {
        System.out.println("Displaying launch view");

        LaunchController controller = viewNavigationService.navigateToView(
                getView("subviews/LaunchView.fxml"),
                null
        );

        setDimensions(AppConfig.WindowDimensions.LAUNCH_WIDTH, AppConfig.WindowDimensions.LAUNCH_HEIGHT);
        controller.run();
    }

    private void setupConnectionView() throws IOException {
        System.out.println("Displaying connection view");

        viewNavigationService.navigateToView(
                getView("subviews/ConnectionView.fxml"),
                null
        );

        setDimensions(AppConfig.WindowDimensions.CONNECTION_WIDTH, AppConfig.WindowDimensions.CONNECTION_HEIGHT);
    }

    private void setupConnectionPendingView() throws IOException {
        System.out.println("Displaying connection pending view");

        viewNavigationService.navigateToView(
                getView("subviews/ConnectionPendingView.fxml"),
                null
        );
    }

    private void setupPairingView() throws IOException {
        System.out.println("Displaying pairing view");

        PairingController controller = viewNavigationService.navigateToView(
                getView("subviews/PairingView.fxml"),
                pairingController -> pairingController.setup(state)
        );
    }

    private void setupMixerView() throws IOException {
        System.out.println("Displaying mixer view");

        MixerController controller = viewNavigationService.navigateToView(
                getView("subviews/MixerView.fxml"),
                MixerController::initialize
        );

        setDimensions(AppConfig.WindowDimensions.MIXER_WIDTH, AppConfig.WindowDimensions.MIXER_HEIGHT);
    }

    /* CONNECTION MESSAGE HANDLERS */
    private void setupConnectionMessageHandlers() {
        connectionService.addMessageHandler(messageEvent -> {
            MessageType type = messageEvent.getType();

            switch (type) {
                case PAIR -> {
                    // Server sent pairing code, show pairing view
                    CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_PAIRING_VIEW));
                }
                case READY -> {
                    // Server confirmed pairing, show mixer view
                    CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_MIXER_VIEW));
                }
                case POSITION_DATA -> {
                    // Handle position updates (if needed in future)
                    System.out.println("Received position data");
                }
                case PING -> {
                    // Handle ping (could respond with PONG)
                    System.out.println("Received ping");
                }
                default -> {
                    System.out.println("Unhandled message type: " + type);
                }
            }
        });
    }

    /* LOGIC EVENT HANDLERS */
    private void setupLogicEventHandlers() {
        CLIENT_ROOT.addEventHandler(ConnectionSetupEvent.CONNECTION_SETUP_EVENT, this::handleConnectionSetup);
    }

    private void handleConnectionSetup(ConnectionSetupEvent event) {
        System.out.println("Attempting connection to " + event.getServerAddress() + ":" + event.getServerPort());

        // Show connection pending view while connecting
        CLIENT_ROOT.fireEvent(new ViewEvent(ViewEvent.SHOW_CONNECTION_PENDING_VIEW));

        // Attempt connection (ConnectionService will handle callbacks)
        connectionService.connect(event.getServerAddress(), event.getServerPort());
    }

    /* UTILITY METHODS */
    private void setDimensions(double width, double height) {
        javafx.application.Platform.runLater(() -> {
            if (stage != null) {
                double currentSceneWidth = stage.getScene().getWidth();
                double currentSceneHeight = stage.getScene().getHeight();
                double currentStageWidth = stage.getWidth();
                double currentStageHeight = stage.getHeight();

                double decorationWidth = currentStageWidth - currentSceneWidth;
                double decorationHeight = currentStageHeight - currentSceneHeight;

                stage.setMinWidth(0);
                stage.setMinHeight(0);
                stage.setMaxWidth(Double.MAX_VALUE);
                stage.setMaxHeight(Double.MAX_VALUE);

                stage.setWidth(width + decorationWidth);
                stage.setHeight(height + decorationHeight);

                stage.centerOnScreen();
            }
        });
    }
}