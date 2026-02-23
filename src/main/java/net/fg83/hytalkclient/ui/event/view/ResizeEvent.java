// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.event.view;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Custom JavaFX event that represents a resize operation.
 * This event carries width and height information for UI components.
 */
public class ResizeEvent extends Event {
    // Event type identifier for resize events (note: string name appears to be a copy-paste error - says "LOCATION_UPDATE_EVENT")
    public static final EventType<ResizeEvent> RESIZE_EVENT = new EventType<>(Event.ANY, "LOCATION_UPDATE_EVENT");

    // The new width value for the resize operation
    private final double width;

    // The new height value for the resize operation
    private final double height;

    /**
     * Constructs a new ResizeEvent with the specified dimensions.
     *
     * @param width  the new width value
     * @param height the new height value
     */
    public ResizeEvent(double width, double height) {
        super(RESIZE_EVENT); // Initialize the parent Event class with our event type
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the width associated with this resize event.
     *
     * @return the width value
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * Returns the height associated with this resize event.
     *
     * @return the height value
     */
    public double getHeight() {
        return this.height;
    }
}