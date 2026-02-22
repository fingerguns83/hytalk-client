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
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.ui.controller.channelstrip.button.ButtonController;
import net.fg83.hytalkclient.ui.event.mixer.AudioDeviceEvent;
import net.fg83.hytalkclient.ui.event.mixer.GainChangeEvent;
import net.fg83.hytalkclient.util.ButtonType;

import java.io.IOException;
import java.util.List;

public class InputChannelStripController extends ChannelStripController {
    @FXML
    private VBox INPUT_CHANNEL_STRIP_ROOT;

    @FXML
    private HBox CHANNEL_BUTTON_HOLDER;

    @FXML
    private MenuButton INPUT_DEVICE_SELECTOR;

    private final ToggleGroup inputDeviceToggleGroup = new ToggleGroup();

    /* Utility Methods */
    @Override
    public void setup(ApplicationState applicationState) throws IOException {
        initializeDeviceSelector(applicationState);
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

    @Override
    protected void initializeButtons() throws IOException {
        initializeMuteButton(false);
        //initializeNoiseReductionButton();
    }

    private void initializeNoiseReductionButton() throws IOException{
        FXMLLoader nrButtonLoader = new FXMLLoader(HytalkClientApplication.class.getResource("widget/button/Button.fxml"));
        StackPane nrButton = (StackPane) nrButtonLoader.load();
        ButtonController nrButtonController = nrButtonLoader.getController();
        nrButtonController.setButtonType(ButtonType.NOISE_REDUCTION);
        CHANNEL_BUTTON_HOLDER.getChildren().add(nrButton);
        nrButtonController.setup(this);
    }

    private void initializeDeviceSelector(ApplicationState applicationState) {
        AudioIOManager.AudioDevice selected = applicationState.getAudioStreamManager().getAudioIOManager().getSelectedInputDevice();
        if (selected == null) {
            INPUT_DEVICE_SELECTOR.getTooltip().setText("Default Input Device");
            return;
        }
        else {
            INPUT_DEVICE_SELECTOR.getTooltip().setText(selected.name());
        }
        setDevices(AudioIOManager.getInputDevices(), selected);
    }

    @Override
    protected void setGain(){
        INPUT_CHANNEL_STRIP_ROOT.fireEvent(new GainChangeEvent(GainChangeEvent.INPUT_GAIN_CHANGE_EVENT, calculateGainPercentage()));
    }

    private void handleDeviceSelection(String device) {
        INPUT_DEVICE_SELECTOR.getTooltip().setText(device);
        INPUT_CHANNEL_STRIP_ROOT.fireEvent(new AudioDeviceEvent(AudioDeviceEvent.INPUT_DEVICE_CHANGED, AudioIOManager.getInputDevices().stream().filter(d -> d.name().equals(device)).findFirst().orElse(null)));
    }

    public void setDevices(List<AudioIOManager.AudioDevice> devices, AudioIOManager.AudioDevice selected) {
        INPUT_DEVICE_SELECTOR.getItems().clear();
        for (AudioIOManager.AudioDevice device : devices) {
            RadioMenuItem item = new RadioMenuItem(device.name());
            item.setOnAction(e -> handleDeviceSelection(device.name()));

            // Select the item if it matches the selected device
            if (selected != null && device.name().equals(selected.name())) {
                item.setSelected(true);
            }

            INPUT_DEVICE_SELECTOR.getItems().add(item);
        }

        INPUT_DEVICE_SELECTOR.getItems().forEach(item -> {
            if (item instanceof RadioMenuItem) {
                ((RadioMenuItem) item).setToggleGroup(inputDeviceToggleGroup);
            }
        });
    }
}

