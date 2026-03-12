// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.service;

import net.fg83.hytalkclient.model.VoiceChatPlayer;
import net.fg83.hytalkclient.service.event.PlayerChangeEvent;

import java.util.*;
import java.util.function.Consumer;

/**
 * Manages voice chat players and their state changes.
 * Tracks the local client player and all remote players in the voice chat,
 * notifying registered listeners when players are added, updated, or removed.
 */
public class PlayerManager {
    // The local client player instance
    private VoiceChatPlayer clientPlayer;

    // Map of all remote voice chat players, keyed by their unique UUID
    private final Map<UUID, VoiceChatPlayer> voiceChatPlayers = new HashMap<>();
    // List of listeners that are notified when player changes occur
    private final List<Consumer<PlayerChangeEvent>> changeListeners = new ArrayList<>();


    /**
     * Constructs a new PlayerManager.
     */
    public PlayerManager() {}

    /**
     * Sets the client player and marks them as the local user.
     *
     * @param player The voice chat player representing the local client
     */
    public void setClientPlayer(VoiceChatPlayer player) {
        this.clientPlayer = player;
        // Mark this player as the local user
        this.clientPlayer.setLocalUser(true);
    }

    /**
     * Gets the local client player.
     *
     * @return The voice chat player representing the local client
     */
    public VoiceChatPlayer getClientPlayer() {
        return this.clientPlayer;
    }

    /**
     * Registers a listener to be notified of player changes.
     *
     * @param listener The listener to add
     */
    public void addPlayerChangeListener(Consumer<PlayerChangeEvent> listener) {
        changeListeners.add(listener);
    }

    /**
     * Notifies all registered listeners of a player change event.
     *
     * @param event The player change event to broadcast
     */
    private void notifyListeners(PlayerChangeEvent event) {
        changeListeners.forEach(listener -> listener.accept(event));
    }

    /**
     * Updates the collection of voice chat players based on new player data.
     * Synchronizes the local player list with the provided map, adding new players,
     * updating existing ones, and removing players that are no longer present.
     * Notifies listeners of all changes (additions, updates, removals).
     *
     * @param newVoiceChatPlayers Map of player UUIDs to their current state
     */
    public void updateVoiceChatPlayers(Map<UUID, VoiceChatPlayer> newVoiceChatPlayers) {
        // Process all players in the new data
        newVoiceChatPlayers.forEach((uuid, player) -> {
            VoiceChatPlayer voiceChatPlayer;
            // If this is the client player, just update their location
            if (clientPlayer.getPlayerId().equals(uuid)) {
                clientPlayer.setPlayerLocation(player.getPlayerLocation());
                return;
            }

            // Check if this player already exists
            if (this.voiceChatPlayers.containsKey(uuid)) {
                // Update existing player's location
                voiceChatPlayer = this.voiceChatPlayers.get(uuid);
                voiceChatPlayer.setPlayerLocation(player.getPlayerLocation());
                // Notify listeners that the player was updated
                notifyListeners(new PlayerChangeEvent.PlayerUpdatedEvent(voiceChatPlayer));
            }
            else {
                // Add new player to the collection
                voiceChatPlayer = player;
                this.voiceChatPlayers.put(uuid, voiceChatPlayer);
                // Notify listeners that a new player was added
                notifyListeners(new PlayerChangeEvent.PlayerAddedEvent(voiceChatPlayer));
            }

            // Calculate the distance between this player and the client
            voiceChatPlayer.calculateDistance(clientPlayer.getPlayerLocation());
        });
        removeStalePlayers(newVoiceChatPlayers);
    }

    private void removeStalePlayers(Map<UUID, VoiceChatPlayer> newVoiceChatPlayers){
        List<UUID> toRemove = this.voiceChatPlayers.keySet().stream()
                .filter(uuid -> !newVoiceChatPlayers.containsKey(uuid))
                .toList();

        // Remove disconnected players and notify listeners
        toRemove.forEach(uuid -> {
            VoiceChatPlayer player = voiceChatPlayers.get(uuid);
            this.voiceChatPlayers.remove(uuid);
            // Notify listeners that the player was removed
            notifyListeners(new PlayerChangeEvent.PlayerRemovedEvent(player));
        });
    }

    /**
     * Gets the map of all remote voice chat players.
     *
     * @return Map of player UUIDs to their VoiceChatPlayer instances
     */
    public Map<UUID, VoiceChatPlayer> getVoiceChatPlayers() {
        return voiceChatPlayers;
    }
}