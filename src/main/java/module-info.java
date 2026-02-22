module net.fg83.hytalkclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop; // For javax.sound.sampled
    requires com.google.gson;
    requires org.java_websocket;
    requires java.prefs;
    requires net.datafaker;

    opens net.fg83.hytalkclient to javafx.fxml;
    opens net.fg83.hytalkclient.ui.controller to javafx.fxml;
    opens net.fg83.hytalkclient.ui.controller.channelstrip to javafx.fxml;
    opens net.fg83.hytalkclient.ui.controller.channelstrip.button to javafx.fxml;

    exports net.fg83.hytalkclient;
    exports net.fg83.hytalkclient.audio;
    exports net.fg83.hytalkclient.model;
    exports net.fg83.hytalkclient.service;
    exports net.fg83.hytalkclient.service.event;
    exports net.fg83.hytalkclient.ui.event;
    exports net.fg83.hytalkclient.ui.event.mixer;
    exports net.fg83.hytalkclient.ui.event.view;
    exports net.fg83.hytalkclient.ui.controller;
    exports net.fg83.hytalkclient.ui.controller.channelstrip;
    exports net.fg83.hytalkclient.ui.controller.channelstrip.button;
    exports net.fg83.hytalkclient.util;
    exports org.concentus;
}