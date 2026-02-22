package net.fg83.hytalkclient.service.event;

import net.fg83.hytalkclient.model.VoiceChatPlayer;

public sealed interface PlayerChangeEvent permits
        PlayerChangeEvent.PlayerAddedEvent,
        PlayerChangeEvent.PlayerRemovedEvent,
        PlayerChangeEvent.PlayerUpdatedEvent {

    VoiceChatPlayer getPlayer();

    record PlayerAddedEvent(VoiceChatPlayer player) implements PlayerChangeEvent {
        @Override
        public VoiceChatPlayer getPlayer() {
            return player;
        }
    }
    record PlayerRemovedEvent(VoiceChatPlayer player) implements PlayerChangeEvent {
        @Override
        public VoiceChatPlayer getPlayer() { return player; }
    }
    record PlayerUpdatedEvent(VoiceChatPlayer player) implements PlayerChangeEvent {
        @Override
        public VoiceChatPlayer getPlayer() {
            return player;
        }

    }
}
