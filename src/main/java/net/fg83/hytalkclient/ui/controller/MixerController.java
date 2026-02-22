
package net.fg83.hytalkclient.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import net.datafaker.Faker;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.model.VoiceChatPlayer;
import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.service.event.PlayerChangeEvent;
import net.fg83.hytalkclient.ui.controller.channelstrip.ChannelStripController;
import net.fg83.hytalkclient.ui.controller.channelstrip.InputChannelStripController;
import net.fg83.hytalkclient.ui.controller.channelstrip.OutputChannelStripController;
import net.fg83.hytalkclient.ui.event.mixer.RegisterChannelControllerEvent;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.fg83.hytalkclient.HytalkClientApplication.getView;

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
    private HBox MIXER_ROOT;
    @FXML
    private Pane INPUT_CHANNEL_HOLDER;
    @FXML
    private Pane OUTPUT_CHANNEL_HOLDER;
    @FXML
    private ScrollPane PLAYER_CHANNEL_SCROLL;
    @FXML
    private HBox PLAYER_CHANNEL_HOLDER;

    private Pane INPUT_CHANNEL;
    private Pane OUTPUT_CHANNEL;

    private InputChannelStripController inputController;
    private OutputChannelStripController outputController;

    private final Map<VoiceChatPlayer, Pane> playerFaders = new HashMap<>();

    /**
     * Setup the mixer with a player change listener callback.
     * The parent controller provides this callback to bridge the gap.
     */
    public void setup(ApplicationState applicationState) throws IOException {
        initializeFaders(applicationState);
        initializePlayerChannelsContainer();

        // Register through parent, but handle locally
        applicationState.getPlayerManager().addPlayerChangeListener(this::handlePlayerChange);
    }

    private void initializeFaders(ApplicationState applicationState) throws IOException {
        createInputChannel(applicationState);
        createOutputChannel(applicationState);
    }

    private void initializePlayerChannelsContainer() {
        PLAYER_CHANNEL_HOLDER.setSpacing(0); // Adjust spacing as needed
        PLAYER_CHANNEL_SCROLL.setContent(PLAYER_CHANNEL_HOLDER);

        // Optional: Configure scroll behavior
        PLAYER_CHANNEL_SCROLL.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        PLAYER_CHANNEL_SCROLL.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        PLAYER_CHANNEL_SCROLL.setFitToHeight(true);
    }

    private void handlePlayerChange(PlayerChangeEvent event) {
        if (event instanceof PlayerChangeEvent.PlayerAddedEvent addedEvent) {
            handlePlayerAdded(addedEvent);
        }
        else if (event instanceof PlayerChangeEvent.PlayerRemovedEvent removedEvent) {
            handlePlayerRemoved(removedEvent);
        }
        else if (event instanceof PlayerChangeEvent.PlayerUpdatedEvent updatedEvent) {
            handlePlayerUpdated(updatedEvent);
        }
    }

    private void handlePlayerAdded(PlayerChangeEvent.PlayerAddedEvent event) {
        VoiceChatPlayer player = event.getPlayer();
        Pane fader = null;

        try {
            fader = createPlayerChannelStrip(player.getPlayerId(), player.getPlayerName());
        }
        catch (IOException e) {
            System.err.println("Failed to create fader for player: " + player.getPlayerName() + "[" + player.getPlayerId() + "]" + e.getMessage());
        }

        if (fader != null) {
            playerFaders.put(player, fader);
            refreshPlayerChannelOrder();
        }

    }

    private void handlePlayerRemoved(PlayerChangeEvent.PlayerRemovedEvent event) {
        VoiceChatPlayer player = event.getPlayer();
        Pane fader = playerFaders.remove(player);

        if (fader != null) {
            PLAYER_CHANNEL_HOLDER.getChildren().remove(fader);
            refreshPlayerChannelOrder();
            System.out.println("Removed channel strip for player: " + player.getPlayerName() + "[" + player.getPlayerId() + "]");
        }
    }

    private void handlePlayerUpdated(PlayerChangeEvent.PlayerUpdatedEvent event) {
        // Could update volume based on distance here
    }

    // === Channel Strip Creation ===
    private Pane createPlayerChannelStrip(UUID playerId, String playerName) throws IOException {
        FXMLLoader loader = new FXMLLoader(getView("widget/channelstrip/PlayerChannelStrip.fxml"));
        Pane channelRoot = (Pane) loader.load();

        ChannelStripController controller = (ChannelStripController) loader.getController();
        controller.setPlayerId(playerId);
        controller.setPlayerName(playerName);
        controller.setRootId("CHANNEL-" + playerId);
        controller.setup(null);

        MIXER_ROOT.fireEvent(new RegisterChannelControllerEvent(playerId, controller, false, false));

        return channelRoot;
    }



    public void createInputChannel(ApplicationState applicationState) throws IOException {
        FXMLLoader loader = new FXMLLoader(getView("widget/channelstrip/InputChannelStrip.fxml"));
        Parent channelRoot = loader.load();

        inputController = (InputChannelStripController) loader.getController();
        inputController.setup(applicationState);

        INPUT_CHANNEL = (Pane) channelRoot;
        INPUT_CHANNEL_HOLDER.getChildren().addFirst(channelRoot);
        MIXER_ROOT.fireEvent(new RegisterChannelControllerEvent(null, inputController, true, false));
    }

    public void createOutputChannel(ApplicationState applicationState) throws IOException {
        FXMLLoader loader = new FXMLLoader(getView("widget/channelstrip/OutputChannelStrip.fxml"));
        Parent channelRoot = loader.load();

        outputController = (OutputChannelStripController) loader.getController();
        outputController.setup(applicationState);

        OUTPUT_CHANNEL = (Pane) channelRoot;
        OUTPUT_CHANNEL_HOLDER.getChildren().addLast(channelRoot);
        MIXER_ROOT.fireEvent(new RegisterChannelControllerEvent(null, outputController, false, true));
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

    /* TESTING METHODS */
    /**
     * Adds dummy player channels for UI testing purposes.
     * This method does NOT affect the actual PlayerManager state.
     */
    public void addDummyPlayers(int count) {
        Faker faker = new Faker();

        for (int i = 1; i <= count; i++) {
            UUID dummyId = UUID.randomUUID();
            String dummyName = faker.name().username();

            handlePlayerAdded(new PlayerChangeEvent.PlayerAddedEvent(new VoiceChatPlayer(dummyName, dummyId)));
        }
        refreshPlayerChannelOrder();
    }

    /* UTILITY METHODS */
    private void refreshPlayerChannelOrder() {
        // Clear existing children
        PLAYER_CHANNEL_HOLDER.getChildren().clear();

        // Sort by player name and add back in order
        playerFaders.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(
                        (p1, p2) -> p1.getPlayerName().compareToIgnoreCase(p2.getPlayerName())
                ))
                .forEach(entry -> {
                    Pane fader = entry.getValue();
                    fader.getStyleClass().remove("last-player-channel");
                    PLAYER_CHANNEL_HOLDER.getChildren().add(fader);
                });

        // Mark the last channel for CSS styling
        if (!PLAYER_CHANNEL_HOLDER.getChildren().isEmpty()) {
            javafx.scene.Node lastChild = PLAYER_CHANNEL_HOLDER.getChildren().get(
                    PLAYER_CHANNEL_HOLDER.getChildren().size() - 1
            );
            lastChild.getStyleClass().add("last-player-channel");
        }
    }
}