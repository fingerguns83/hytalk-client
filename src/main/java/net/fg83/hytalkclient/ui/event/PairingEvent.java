// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.event;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Custom JavaFX event class for handling pairing-related events in the application.
 * Extends the JavaFX Event class to provide specific event types for pairing operations.
 */
public class PairingEvent extends Event {
    /**
     * Base event type for all pairing events.
     * This serves as the parent event type for all pairing-related event subtypes.
     */
    public static final EventType<PairingEvent> PAIRING_EVENT =
            new EventType<>(Event.ANY, "PAIRING_EVENT");

    /**
     * Event type fired when a pairing request has expired.
     * This is a subtype of PAIRING_EVENT.
     */
    public static final EventType<PairingEvent> PAIRING_EXPIRED =
            new EventType<>(PAIRING_EVENT, "PAIRING_EXPIRED");

    /**
     * Event type fired when a pairing request has been cancelled.
     * This is a subtype of PAIRING_EVENT.
     */
    public static final EventType<PairingEvent> PAIRING_CANCELLED =
            new EventType<>(PAIRING_EVENT, "PAIRING_CANCELLED");

    /**
     * Constructs a new PairingEvent with the specified event type.
     *
     * @param eventType the specific type of pairing event to create
     */
    public PairingEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
}