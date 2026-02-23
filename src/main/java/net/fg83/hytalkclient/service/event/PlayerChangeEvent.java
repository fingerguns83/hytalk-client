package net.fg83.hytalkclient.service.event;

import net.fg83.hytalkclient.model.VoiceChatPlayer;

/**
 * Sealed interface representing events related to player state changes in the voice chat system.
 * This interface uses the sealed feature to restrict which classes can implement it,
 * ensuring exhaustive handling of all possible player change event types.
 */
public sealed interface PlayerChangeEvent permits
        PlayerChangeEvent.PlayerAddedEvent,
        PlayerChangeEvent.PlayerRemovedEvent,
        PlayerChangeEvent.PlayerUpdatedEvent {

    /**
     * Gets the player associated with this change event.
     *
     * @return the VoiceChatPlayer affected by this event
     */
    VoiceChatPlayer getPlayer();

    /**
     * Event fired when a new player is added to the voice chat system.
     *
     * @param player the player that was added
     */
    record PlayerAddedEvent(VoiceChatPlayer player) implements PlayerChangeEvent {
        /**
         * Gets the player that was added to the voice chat system.
         *
         * @return the added player
         */
        @Override
        public VoiceChatPlayer getPlayer() {
            return player;
        }
    }

    /**
     * Event fired when a player is removed from the voice chat system.
     *
     * @param player the player that was removed
     */
    record PlayerRemovedEvent(VoiceChatPlayer player) implements PlayerChangeEvent {
        /**
         * Gets the player that was removed from the voice chat system.
         *
         * @return the removed player
         */
        @Override
        public VoiceChatPlayer getPlayer() {
            return player;
        }
    }

    /**
     * Event fired when a player's information is updated in the voice chat system.
     * This can include location changes, name changes, or other property updates.
     *
     * @param player the player that was updated
     */
    record PlayerUpdatedEvent(VoiceChatPlayer player) implements PlayerChangeEvent {
        /**
         * Gets the player that was updated in the voice chat system.
         *
         * @return the updated player
         */
        @Override
        public VoiceChatPlayer getPlayer() {
            return player;
        }

    }
}