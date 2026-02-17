package net.fg83.hytalkclient.ui.event;

import javafx.event.Event;
import javafx.event.EventType;

public class PairingEvent extends Event {
    public static final EventType<PairingEvent> PAIRING_EVENT =
            new EventType<>(Event.ANY, "PAIRING_EVENT");
    public static final EventType<PairingEvent> PAIRING_EXPIRED =
            new EventType<>(PAIRING_EVENT, "PAIRING_EXPIRED");
    public static final EventType<PairingEvent> PAIRING_CANCELLED =
            new EventType<>(PAIRING_EVENT, "PAIRING_CANCELLED");

    public PairingEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
}