package net.fg83.hytalkclient.service;

import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.model.VoiceChatPlayer;
import net.fg83.hytalkclient.service.event.PlayerChangeEvent;

import java.util.*;
import java.util.function.Consumer;

public class PlayerManager {
    private VoiceChatPlayer clientPlayer;

    private final Map<UUID, VoiceChatPlayer> voiceChatPlayers = new HashMap<>();
    private final List<Consumer<PlayerChangeEvent>> changeListeners = new ArrayList<>();


    public PlayerManager() {}

    public void setClientPlayer(VoiceChatPlayer player) {
        this.clientPlayer = player;
        this.clientPlayer.setLocalUser(true);
    }

    public VoiceChatPlayer getClientPlayer(){
        return this.clientPlayer;
    }

    public void addPlayerChangeListener(Consumer<PlayerChangeEvent> listener) {
        changeListeners.add(listener);
    }

    public void removePlayerChangeListener(Consumer<PlayerChangeEvent> listener) {
        changeListeners.remove(listener);
    }

    private void notifyListeners(PlayerChangeEvent event) {
        changeListeners.forEach(listener -> listener.accept(event));
    }

    public void updateVoiceChatPlayers(Map<UUID, VoiceChatPlayer> newVoiceChatPlayers) {
        newVoiceChatPlayers.forEach((uuid, player) -> {
            VoiceChatPlayer voiceChatPlayer;
            if (clientPlayer.getPlayerId().equals(uuid)) {
                clientPlayer.setPlayerLocation(player.getPlayerLocation());
                return;
            }

            if (this.voiceChatPlayers.containsKey(uuid)) {
                voiceChatPlayer = this.voiceChatPlayers.get(uuid);
                voiceChatPlayer.setPlayerLocation(player.getPlayerLocation());
                notifyListeners(new PlayerChangeEvent.PlayerUpdatedEvent(voiceChatPlayer));
            }
            else {
                voiceChatPlayer = player;
                this.voiceChatPlayers.put(uuid, voiceChatPlayer);
                notifyListeners(new PlayerChangeEvent.PlayerAddedEvent(voiceChatPlayer));
            }

            voiceChatPlayer.calculateDistance(clientPlayer.getPlayerLocation());
        });

        List<UUID> toRemove = this.voiceChatPlayers.keySet().stream()
                .filter(uuid -> !newVoiceChatPlayers.containsKey(uuid))
                .toList();

        toRemove.forEach(uuid -> {
            VoiceChatPlayer player = voiceChatPlayers.get(uuid);
            this.voiceChatPlayers.remove(uuid);
            notifyListeners(new PlayerChangeEvent.PlayerRemovedEvent(player));
        });
    }

    public Map<UUID, VoiceChatPlayer> getVoiceChatPlayers() { return voiceChatPlayers; }
}
