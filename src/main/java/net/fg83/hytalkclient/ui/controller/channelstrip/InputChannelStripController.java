package net.fg83.hytalkclient.ui.controller.channelstrip;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import net.fg83.hytalkclient.HytalkClientApplication;
import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.ui.controller.channelstrip.button.ButtonController;
import net.fg83.hytalkclient.ui.event.AudioDeviceEvent;
import net.fg83.hytalkclient.ui.event.GainChangeEvent;
import net.fg83.hytalkclient.util.ButtonType;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import static java.lang.Math.round;

public class InputChannelStripController extends ChannelStripController {
    @FXML
    private VBox INPUT_CHANNEL_STRIP_ROOT;

    @FXML
    private HBox CHANNEL_BUTTON_HOLDER;

    @FXML
    private Pane MUTE_BUTTON_HOLDER;

    @FXML
    private Pane NR_BUTTON_HOLDER;

    private StackPane muteButton;

    private StackPane nrButton;

    @FXML
    private MenuButton INPUT_DEVICE_SELECTOR;

    private final ToggleGroup inputDeviceToggleGroup = new ToggleGroup();

    @FXML
    private void initialize() throws IOException {

    }

    /* Utility Methods */
    @Override
    public void setup() throws IOException {
        initializeDeviceSelector();
        initializeFaderCap();
        initializeFaderLocation();
        initializeButtons();
        initializeVUMeter();
    }
    @Override
    protected void initializeFaderCap() {
        faderCap = (ImageView) INPUT_CHANNEL_STRIP_ROOT.lookup(".fader-cap");
    }

    @Override
    protected void initializeVUMeter() {
        VUMeter = (Line) INPUT_CHANNEL_STRIP_ROOT.lookup(".vu-meter-mask");
    }

    protected void initializeButtons() throws IOException {
        FXMLLoader muteButtonloader = new FXMLLoader(HytalkClientApplication.class.getResource("widget/button/Button.fxml"));
        muteButton = (StackPane) muteButtonloader.load();
        ButtonController muteButtonController = muteButtonloader.getController();
        muteButtonController.setButtonType(ButtonType.MUTE);
        HBox.setMargin(muteButton, new Insets(0, 10, 0, 0));
        CHANNEL_BUTTON_HOLDER.getChildren().add(muteButton);
        muteButtonController.setup();


        FXMLLoader nrButtonLoader = new FXMLLoader(HytalkClientApplication.class.getResource("widget/button/Button.fxml"));
        nrButton = (StackPane) nrButtonLoader.load();
        ButtonController nrButtonController = nrButtonLoader.getController();
        nrButtonController.setButtonType(ButtonType.NOISE_REDUCTION);
        CHANNEL_BUTTON_HOLDER.getChildren().add(nrButton);
        nrButtonController.setup();
    }

    protected void initializeDeviceSelector() {
        setDevices(AudioIOManager.getInputDevices(), null);

    }

    @Override
    protected void setGain(){
        INPUT_CHANNEL_STRIP_ROOT.fireEvent(new GainChangeEvent(GainChangeEvent.INPUT_GAIN_CHANGE_EVENT, calculateGainPercentage()));
    }

    private void handleDeviceSelection(String device) {
        INPUT_CHANNEL_STRIP_ROOT.fireEvent(new AudioDeviceEvent(AudioDeviceEvent.INPUT_DEVICE_CHANGED, AudioIOManager.getInputDevices().stream().filter(d -> d.name().equals(device)).findFirst().orElse(null)));
    }

    public void setDevices(List<AudioIOManager.AudioDevice> devices, AudioIOManager.AudioDevice selected) {
        INPUT_DEVICE_SELECTOR.getItems().clear();
        for (AudioIOManager.AudioDevice device : devices) {
            RadioMenuItem item = new RadioMenuItem(device.name());
            item.setOnAction(e -> handleDeviceSelection(device.name()));
            INPUT_DEVICE_SELECTOR.getItems().add(item);
        }

        INPUT_DEVICE_SELECTOR.getItems().forEach(item -> {
            if (item instanceof RadioMenuItem) {
                ((RadioMenuItem) item).setToggleGroup(inputDeviceToggleGroup);
            }
        });
    }
}

