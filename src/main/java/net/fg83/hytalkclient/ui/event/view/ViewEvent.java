// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.event.view;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Custom JavaFX event for managing view navigation within the application.
 * This event is fired when the UI needs to switch between different views/screens.
 */
public class ViewEvent extends Event {
    // Base event type for all view-related events
    public static final EventType<ViewEvent> VIEW_EVENT =
            new EventType<>(Event.ANY, "VIEW_EVENT");

    // Event type for displaying the launch/startup view
    public static final EventType<ViewEvent> SHOW_LAUNCH_VIEW =
            new EventType<>(VIEW_EVENT, "SHOW_LAUNCH_VIEW");

    // Event type for displaying the connection setup view
    public static final EventType<ViewEvent> SHOW_CONNECTION_VIEW =
            new EventType<>(VIEW_EVENT, "SHOW_CONNECTION_VIEW");

    // Event type for displaying the connection in-progress view
    public static final EventType<ViewEvent> SHOW_CONNECTION_PENDING_VIEW =
            new EventType<>(VIEW_EVENT, "SHOW_CONNECTION_PENDING_VIEW");

    // Event type for displaying the device pairing view
    public static final EventType<ViewEvent> SHOW_PAIRING_VIEW =
            new EventType<>(VIEW_EVENT, "SHOW_PAIRING_VIEW");

    // Event type for displaying the audio mixer view
    public static final EventType<ViewEvent> SHOW_MIXER_VIEW =
            new EventType<>(VIEW_EVENT, "SHOW_MIXER_VIEW");

    /**
     * Constructs a new ViewEvent with the specified event type.
     *
     * @param eventType the specific type of view event (e.g., SHOW_LAUNCH_VIEW)
     */
    public ViewEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
}