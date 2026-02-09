module net.fg83.hytalkclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.xml.dom;
    requires org.java_websocket;
    requires com.google.gson;

    opens net.fg83.hytalkclient to javafx.fxml;
    exports net.fg83.hytalkclient;
    exports net.fg83.hytalkclient.ui.event;
    exports net.fg83.hytalkclient.ui.controller;
    opens net.fg83.hytalkclient.ui.controller to javafx.fxml;
    exports net.fg83.hytalkclient.network;
    opens net.fg83.hytalkclient.network to javafx.fxml;
}