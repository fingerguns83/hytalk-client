package net.fg83.hytalkclient.ui.event;

import com.google.gson.JsonArray;
import javafx.event.Event;
import javafx.event.EventType;

public class LocationUpdateEvent extends Event {
    public static final EventType<ConnectionSetupEvent> LOCATION_UPDATE_EVENT = new EventType<>(Event.ANY, "LOCATION_UPDATE_EVENT");

    public LocationUpdateEvent(JsonArray locations) {
        super(LOCATION_UPDATE_EVENT);
    }
}
