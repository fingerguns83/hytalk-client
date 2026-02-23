// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

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
 * Controller for the audio mixer view.
 * Manages the creation and display of audio channel strips including input, output,
 * and individual player channels. Handles player addition/removal events and maintains
 * the visual order of player channels.
 */
public class MixerController {

    // Root container for the entire mixer UI, injected by FXML
    @FXML
    private HBox MIXER_ROOT;
    // Container pane for the input channel strip
    @FXML
    private Pane INPUT_CHANNEL_HOLDER;
    // Container pane for the output channel strip
    @FXML
    private Pane OUTPUT_CHANNEL_HOLDER;
    // Scrollable pane containing all player channel strips
    @FXML
    private ScrollPane PLAYER_CHANNEL_SCROLL;
    // Container for individual player channel strips within the scroll pane
    @FXML
    private HBox PLAYER_CHANNEL_HOLDER;

    // Reference to the loaded input channel pane
    private Pane INPUT_CHANNEL;
    // Reference to the loaded output channel pane
    private Pane OUTPUT_CHANNEL;

    // Controller for the input channel strip
    private InputChannelStripController inputController;
    // Controller for the output channel strip
    private OutputChannelStripController outputController;

    // Map tracking player objects to their corresponding UI channel strip panes
    private final Map<VoiceChatPlayer, Pane> playerFaders = new HashMap<>();

    /**
     * Initializes the mixer controller with the application state.
     * Sets up input/output channels, configures the player channels container,
     * and registers a listener for player change events.
     *
     * @param applicationState the application state containing player and audio management
     * @throws IOException if an I/O error occurs during channel creation
     */
    public void setup(ApplicationState applicationState) throws IOException {
        // Create the input and output channel strips
        initializeFaders(applicationState);
        // Configure the container for player channel strips
        initializePlayerChannelsContainer();

        // Register a listener to handle player addition, removal, and update events
        applicationState.getPlayerManager().addPlayerChangeListener(this::handlePlayerChange);
        //addDummyPlayers(10);
    }

    /**
     * Initializes the input and output channel faders.
     *
     * @param applicationState the application state for channel setup
     * @throws IOException if an I/O error occurs during channel creation
     */
    private void initializeFaders(ApplicationState applicationState) throws IOException {
        createInputChannel(applicationState);
        createOutputChannel(applicationState);
    }

    /**
     * Configures the player channels container properties.
     * Sets up spacing, scroll policies, and content binding for the player channel scroll pane.
     */
    private void initializePlayerChannelsContainer() {
        // Remove spacing between player channel strips
        PLAYER_CHANNEL_HOLDER.setSpacing(0);
        // Set the HBox as the content of the scroll pane
        PLAYER_CHANNEL_SCROLL.setContent(PLAYER_CHANNEL_HOLDER);

        // Enable horizontal scrollbar only when needed
        PLAYER_CHANNEL_SCROLL.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // Disable vertical scrollbar as channels scroll horizontally
        PLAYER_CHANNEL_SCROLL.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // Ensure the scroll pane fits the height of its content
        PLAYER_CHANNEL_SCROLL.setFitToHeight(true);
    }

    /**
     * Handles player change events by delegating to specific handler methods.
     *
     * @param event the player change event (add, remove, or update)
     */
    private void handlePlayerChange(PlayerChangeEvent event) {
        // Check the event type and delegate to the appropriate handler
        if (event instanceof PlayerChangeEvent.PlayerAddedEvent addedEvent) {
            handlePlayerAdded(addedEvent, false);
        }
        else if (event instanceof PlayerChangeEvent.PlayerRemovedEvent removedEvent) {
            handlePlayerRemoved(removedEvent);
        }
        else if (event instanceof PlayerChangeEvent.PlayerUpdatedEvent updatedEvent) {
            handlePlayerUpdated(updatedEvent);
        }
    }

    /**
     * Handles a player added event by creating a new channel strip for the player.
     *
     * @param event   the player added event containing the player information
     * @param isDummy flag indicating whether this is a dummy player for testing
     */
    private void handlePlayerAdded(PlayerChangeEvent.PlayerAddedEvent event, boolean isDummy) {
        // Extract player information from the event
        VoiceChatPlayer player = event.getPlayer();
        Pane fader = null;

        try {
            // Create the channel strip UI for this player
            fader = createPlayerChannelStrip(player.getPlayerId(), player.getPlayerName(), isDummy);
        }
        catch (IOException e) {
            // Log error if channel strip creation fails
            System.err.println("Failed to create fader for player: " + player.getPlayerName() + "[" + player.getPlayerId() + "]" + e.getMessage());
        }

        // If channel strip was successfully created, add it to the map and refresh the display
        if (fader != null) {
            playerFaders.put(player, fader);
            refreshPlayerChannelOrder();
        }

    }

    /**
     * Handles a player removed event by removing the player's channel strip from the UI.
     *
     * @param event the player removed event containing the player information
     */
    private void handlePlayerRemoved(PlayerChangeEvent.PlayerRemovedEvent event) {
        // Extract player information from the event
        VoiceChatPlayer player = event.getPlayer();
        // Remove the player's channel strip from the tracking map
        Pane fader = playerFaders.remove(player);

        // If a channel strip was found for this player, remove it from the UI
        if (fader != null) {
            PLAYER_CHANNEL_HOLDER.getChildren().remove(fader);
            // Refresh the display to maintain proper ordering
            refreshPlayerChannelOrder();
            System.out.println("Removed channel strip for player: " + player.getPlayerName() + "[" + player.getPlayerId() + "]");
        }
    }

    /**
     * Handles a player updated event.
     * Currently not implemented but allows for future player state updates.
     *
     * @param event the player updated event containing the updated player information
     */
    private void handlePlayerUpdated(PlayerChangeEvent.PlayerUpdatedEvent event) {
    }

    /**
     * Creates a new channel strip UI for a player.
     *
     * @param playerId   the unique identifier for the player
     * @param playerName the display name of the player
     * @param isDummy    flag indicating whether this is a dummy player for testing
     * @return the created channel strip pane
     * @throws IOException if an I/O error occurs during FXML loading
     */
    private Pane createPlayerChannelStrip(UUID playerId, String playerName, boolean isDummy) throws IOException {
        // Load the player channel strip FXML file
        FXMLLoader loader = new FXMLLoader(getView("widget/channelstrip/PlayerChannelStrip.fxml"));
        Pane channelRoot = loader.load();

        // Get the controller and configure it with player information
        ChannelStripController controller = loader.getController();
        controller.setPlayerId(playerId);
        controller.setPlayerName(playerName);
        controller.setRootId("CHANNEL-" + playerId);
        controller.setup(null, isDummy);

        // Fire an event to register this channel controller with the mixer
        MIXER_ROOT.fireEvent(new RegisterChannelControllerEvent(playerId, controller, false, false));

        return channelRoot;
    }

    /**
     * Creates and initializes the input channel strip.
     *
     * @param applicationState the application state for input channel setup
     * @throws IOException if an I/O error occurs during FXML loading
     */
    public void createInputChannel(ApplicationState applicationState) throws IOException {
        // Load the input channel strip FXML file
        FXMLLoader loader = new FXMLLoader(getView("widget/channelstrip/InputChannelStrip.fxml"));
        Parent channelRoot = loader.load();

        // Get the controller and initialize it
        inputController = loader.getController();
        inputController.setup(applicationState, false);

        // Store reference to the channel pane and add it to the holder
        INPUT_CHANNEL = (Pane) channelRoot;
        INPUT_CHANNEL_HOLDER.getChildren().addFirst(channelRoot);
        // Fire an event to register this input channel controller (isInput=true)
        MIXER_ROOT.fireEvent(new RegisterChannelControllerEvent(null, inputController, true, false));
    }

    /**
     * Creates and initializes the output channel strip.
     *
     * @param applicationState the application state for output channel setup
     * @throws IOException if an I/O error occurs during FXML loading
     */
    public void createOutputChannel(ApplicationState applicationState) throws IOException {
        // Load the output channel strip FXML file
        FXMLLoader loader = new FXMLLoader(getView("widget/channelstrip/OutputChannelStrip.fxml"));
        Parent channelRoot = loader.load();

        // Get the controller and initialize it
        outputController = loader.getController();
        outputController.setup(applicationState, false);

        // Store reference to the channel pane and add it to the holder
        OUTPUT_CHANNEL = (Pane) channelRoot;
        OUTPUT_CHANNEL_HOLDER.getChildren().addLast(channelRoot);
        // Fire an event to register this output channel controller (isOutput=true)
        MIXER_ROOT.fireEvent(new RegisterChannelControllerEvent(null, outputController, false, true));
    }

    /**
     * Updates the available input devices in the input channel strip.
     *
     * @param devices  list of available input audio devices
     * @param selected the currently selected input device
     */
    public void updateInputDevices(List<AudioIOManager.AudioDevice> devices,
                                   AudioIOManager.AudioDevice selected) {
        // Update the input controller if it has been initialized
        if (inputController != null) {
            inputController.setDevices(devices, selected);
        }
    }

    /**
     * Updates the available output devices in the output channel strip.
     *
     * @param devices  list of available output audio devices
     * @param selected the currently selected output device
     */
    public void updateOutputDevices(List<AudioIOManager.AudioDevice> devices,
                                    AudioIOManager.AudioDevice selected) {
        // Update the output controller if it has been initialized
        if (outputController != null) {
            outputController.setDevices(devices, selected);
        }
    }

    /**
     * Adds dummy players for testing purposes.
     * Creates the specified number of fake players with random names.
     *
     * @param count the number of dummy players to create
     */
    public void addDummyPlayers(int count) {
        // Initialize the Faker library for generating random data
        Faker faker = new Faker();

        // Create the specified number of dummy players
        for (int i = 1; i <= count; i++) {
            UUID dummyId = UUID.randomUUID();
            String dummyName = faker.name().username();

            // Add the dummy player as if it were a real player (with isDummy flag set)
            handlePlayerAdded(new PlayerChangeEvent.PlayerAddedEvent(new VoiceChatPlayer(dummyName, dummyId)), true);
        }
        // Refresh the display to show all dummy players in sorted order
        refreshPlayerChannelOrder();
    }

    /**
     * Refreshes the player channel order by sorting them alphabetically by name.
     * Also manages the styling of the last channel strip.
     */
    private void refreshPlayerChannelOrder() {
        // Clear all existing player channel strips from the display
        PLAYER_CHANNEL_HOLDER.getChildren().clear();

        // Sort players alphabetically by name (case-insensitive) and add them back to the display
        playerFaders.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(
                        (p1, p2) -> p1.getPlayerName().compareToIgnoreCase(p2.getPlayerName())
                ))
                .forEach(entry -> {
                    Pane fader = entry.getValue();
                    // Remove any existing "last-player-channel" style class
                    fader.getStyleClass().remove("last-player-channel");
                    // Add the channel strip back to the display
                    PLAYER_CHANNEL_HOLDER.getChildren().add(fader);
                });

        // Apply a special style class to the last player channel for visual distinction
        if (!PLAYER_CHANNEL_HOLDER.getChildren().isEmpty()) {
            javafx.scene.Node lastChild = PLAYER_CHANNEL_HOLDER.getChildren().getLast();
            lastChild.getStyleClass().add("last-player-channel");
        }
    }
}