package net.fg83.hytalkclient.ui.event;

import javafx.event.Event;
import javafx.event.EventType;

public class ResizeEvent extends Event{
    public static final EventType<ResizeEvent> RESIZE_EVENT = new EventType<>(Event.ANY, "LOCATION_UPDATE_EVENT");
    private final double width;
    private final double height;

    public ResizeEvent(double width, double height) {
        super(RESIZE_EVENT);
        this.width = width;
        this.height = height;
    }
    public double getWidth() {
        return this.width;
    }
    public double getHeight() {
        return this.height;
    }
}
