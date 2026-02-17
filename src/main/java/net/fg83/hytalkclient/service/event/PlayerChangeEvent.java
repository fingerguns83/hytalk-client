package net.fg83.hytalkclient.service.event;

import net.fg83.hytalkclient.model.VoiceChatPlayer;

import java.util.UUID;

public sealed interface PlayerChangeEvent permits
        PlayerChangeEvent.PlayerAddedEvent,
        PlayerChangeEvent.PlayerRemovedEvent,
        PlayerChangeEvent.PlayerUpdatedEvent {

    UUID getPlayerId();

    record PlayerAddedEvent(UUID playerId, VoiceChatPlayer player) implements PlayerChangeEvent {
        @Override
        public UUID getPlayerId() {
            return playerId;
        }
    }
    record PlayerRemovedEvent(UUID playerId) implements PlayerChangeEvent {
        @Override
        public UUID getPlayerId() {
            return playerId;
        }
    }
    record PlayerUpdatedEvent(UUID playerId, VoiceChatPlayer player) implements PlayerChangeEvent {
        @Override
        public UUID getPlayerId() {
            return playerId;
        }
    }
}
