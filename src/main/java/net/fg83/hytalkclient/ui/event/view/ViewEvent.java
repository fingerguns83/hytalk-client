package net.fg83.hytalkclient.ui.event.view;

import javafx.event.Event;
import javafx.event.EventType;

public class ViewEvent extends Event {
    public static final EventType<ViewEvent> VIEW_EVENT =
            new EventType<>(Event.ANY, "VIEW_EVENT");
    public static final EventType<ViewEvent> SHOW_LAUNCH_VIEW =
            new EventType<>(VIEW_EVENT, "SHOW_LAUNCH_VIEW");
    public static final EventType<ViewEvent> SHOW_CONNECTION_VIEW =
            new EventType<>(VIEW_EVENT, "SHOW_CONNECTION_VIEW");
    public static final EventType<ViewEvent> SHOW_CONNECTION_PENDING_VIEW =
            new EventType<>(VIEW_EVENT, "SHOW_CONNECTION_PENDING_VIEW");
    public static final EventType<ViewEvent> SHOW_PAIRING_VIEW =
            new EventType<>(VIEW_EVENT, "SHOW_PAIRING_VIEW");
    public static final EventType<ViewEvent> SHOW_MIXER_VIEW =
            new EventType<>(VIEW_EVENT, "SHOW_MIXER_VIEW");

    public ViewEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
}
