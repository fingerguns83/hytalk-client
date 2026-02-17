
package net.fg83.hytalkclient.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import net.fg83.hytalkclient.HytalkClientApplication;
import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.service.PlayerManager;
import net.fg83.hytalkclient.service.event.PlayerChangeEvent;
import net.fg83.hytalkclient.ui.controller.channelstrip.ChannelStripController;
import net.fg83.hytalkclient.ui.controller.channelstrip.InputChannelStripController;
import net.fg83.hytalkclient.ui.controller.channelstrip.OutputChannelStripController;
import net.fg83.hytalkclient.ui.event.ResizeEvent;
import net.fg83.hytalkclient.util.WindowDimensions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * MixerController is a REACTIVE VIEW that manages dynamic channel strips.
 *
 * Unlike other controllers that fire events upward, this controller directly
 * observes PlayerChangeEvents because it's responsible for the 1:1 mapping
 * between players and UI channel strips.
 *
 * This is an intentional architectural exception for performance and cohesion.
 */
public class MixerController {

    @FXML
    private Pane MIXER_ROOT;

    private Pane INPUT_FADER;
    private Pane MASTER_FADER;

    private InputChannelStripController inputController;
    private OutputChannelStripController outputController;

    private final Map<UUID, Pane> playerFaders = new HashMap<>();

    /**
     * Setup the mixer with a player change listener callback.
     * The parent controller provides this callback to bridge the gap.
     */
    public void setup(PlayerManager playerManager) throws IOException {
        initializeFaders();

        // Register through parent, but handle locally
        playerManager.addPlayerChangeListener(this::handlePlayerChange);
    }

    private void initializeFaders() throws IOException {
        createInputFader();
        createMasterFader();
    }

    // === Player Change Handling (Reactive) ===

    private void handlePlayerChange(PlayerChangeEvent event) {
        if (event instanceof PlayerChangeEvent.PlayerAddedEvent addedEvent) {
            handlePlayerAdded(addedEvent);
        } else if (event instanceof PlayerChangeEvent.PlayerRemovedEvent removedEvent) {
            handlePlayerRemoved(removedEvent);
        } else if (event instanceof PlayerChangeEvent.PlayerUpdatedEvent updatedEvent) {
            handlePlayerUpdated(updatedEvent);
        }
    }

    private void handlePlayerAdded(PlayerChangeEvent.PlayerAddedEvent event) {
        try {
            UUID playerId = event.playerId();
            String playerName = event.player().getPlayerName();

            Pane fader = createPlayerFader(playerId, playerName);
            playerFaders.put(playerId, fader);

            System.out.println("Created channel strip for player: " + playerName);
        } catch (IOException e) {
            System.err.println("Failed to create fader for player: " + e.getMessage());
        }
    }

    private void handlePlayerRemoved(PlayerChangeEvent.PlayerRemovedEvent event) {
        UUID playerId = event.playerId();
        Pane fader = playerFaders.remove(playerId);

        if (fader != null) {
            MIXER_ROOT.getChildren().remove(fader);
            repositionMasterFader();
            resizeMixer();
            System.out.println("Removed channel strip for player: " + playerId);
        }
    }

    private void handlePlayerUpdated(PlayerChangeEvent.PlayerUpdatedEvent event) {
        System.out.println("Player " + event.playerId() + " updated location");
        // Could update volume based on distance here
    }

    // === Channel Strip Creation ===

    public Pane createPlayerFader(UUID playerId, String playerName) throws IOException {
        Pane channelRoot = loadChannelStrip(playerId, playerName);
        insertChannelBeforeMaster(channelRoot);
        resizeMixer();
        return channelRoot;
    }

    private Pane loadChannelStrip(UUID playerId, String playerName) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                HytalkClientApplication.class.getResource("widget/channelstrip/PlayerChannelStrip.fxml")
        );
        Pane channelRoot = (Pane) loader.load();

        ChannelStripController controller = (ChannelStripController) loader.getController();
        controller.setPlayerId(playerId);
        controller.setPlayerName(playerName);
        controller.setRootId("CHANNEL-" + playerId);
        controller.setup();

        return channelRoot;
    }

    private void insertChannelBeforeMaster(Pane channelStrip) {
        MIXER_ROOT.getChildren().remove(MASTER_FADER);

        int position = MIXER_ROOT.getChildren().size();
        channelStrip.setLayoutX(position * WindowDimensions.CHANNEL_STRIP_WIDTH);
        MIXER_ROOT.getChildren().add(channelStrip);

        repositionMasterFader();
    }

    private void repositionMasterFader() {
        if (MASTER_FADER != null) {
            int position = MIXER_ROOT.getChildren().size();
            MASTER_FADER.setLayoutX(position * WindowDimensions.CHANNEL_STRIP_WIDTH);
            MIXER_ROOT.getChildren().add(MASTER_FADER);
        }
    }

    private void resizeMixer() {
        double newWidth = MIXER_ROOT.getChildren().size() * WindowDimensions.CHANNEL_STRIP_WIDTH;
        MIXER_ROOT.fireEvent(new ResizeEvent(newWidth, WindowDimensions.MIXER_HEIGHT));
    }

    public void createInputFader() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                HytalkClientApplication.class.getResource("widget/channelstrip/InputChannelStrip.fxml")
        );
        Parent channelRoot = loader.load();

        inputController = (InputChannelStripController) loader.getController();
        inputController.setup();

        INPUT_FADER = (Pane) channelRoot;
        MIXER_ROOT.getChildren().addFirst(channelRoot);
    }

    public void createMasterFader() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                HytalkClientApplication.class.getResource("widget/channelstrip/OutputChannelStrip.fxml")
        );
        Parent channelRoot = loader.load();

        outputController = (OutputChannelStripController) loader.getController();
        outputController.setup();

        MASTER_FADER = (Pane) channelRoot;
        MASTER_FADER.setLayoutX(MIXER_ROOT.getChildren().size() * WindowDimensions.CHANNEL_STRIP_WIDTH);
        MIXER_ROOT.getChildren().addLast(channelRoot);
    }

    // === Public API for Parent Controller ===

    public void updateInputDevices(List<AudioIOManager.AudioDevice> devices,
                                   AudioIOManager.AudioDevice selected) {
        if (inputController != null) {
            inputController.setDevices(devices, selected);
        }
    }

    public void updateOutputDevices(List<AudioIOManager.AudioDevice> devices,
                                    AudioIOManager.AudioDevice selected) {
        if (outputController != null) {
            outputController.setDevices(devices, selected);
        }
    }
}