package net.fg83.hytalkclient.ui.controller.channelstrip;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
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

public class OutputChannelStripController extends ChannelStripController{
    @FXML
    private VBox OUTPUT_CHANNEL_STRIP_ROOT;

    @FXML
    private HBox CHANNEL_BUTTON_HOLDER;

    private StackPane muteButton;

    private StackPane limButton;

    @FXML
    private MenuButton OUTPUT_DEVICE_SELECTOR;


    private final ToggleGroup outputDeviceToggleGroup = new ToggleGroup();

    @Override
    public void setup(ApplicationState applicationState) throws IOException {
        initializeDeviceSelector(applicationState);
        initializeFaderCap();
        initializeFaderLocation();
        initializeVUMeter();
        initializeButtons();
    }

    @Override
    protected void initializeFaderCap() {
        faderCap = (ImageView) OUTPUT_CHANNEL_STRIP_ROOT.lookup(".fader-cap");
    }
    @Override
    protected void initializeVUMeter() {
        VUMeter = (Line) OUTPUT_CHANNEL_STRIP_ROOT.lookup(".vu-meter-mask");
    }
    protected void initializeButtons() throws IOException {
        FXMLLoader muteButtonloader = new FXMLLoader(HytalkClientApplication.class.getResource("widget/button/Button.fxml"));
        muteButton = (StackPane) muteButtonloader.load();
        ButtonController muteButtonController = muteButtonloader.getController();
        muteButtonController.setButtonType(net.fg83.hytalkclient.util.ButtonType.MUTE);
        HBox.setMargin(muteButton, new Insets(0, 10, 0, 0));
        CHANNEL_BUTTON_HOLDER.getChildren().add(muteButton);
        muteButtonController.setup();


        FXMLLoader limButtonLoader = new FXMLLoader(HytalkClientApplication.class.getResource("widget/button/Button.fxml"));
        limButton = (StackPane) limButtonLoader.load();
        ButtonController limButtonController = limButtonLoader.getController();
        limButtonController.setButtonType(ButtonType.LIMITER);
        CHANNEL_BUTTON_HOLDER.getChildren().add(limButton);
        limButtonController.setup();
    }
    protected void initializeDeviceSelector(ApplicationState applicationState){
        AudioIOManager.AudioDevice selected = applicationState.getAudioStreamManager().getAudioIOManager().getSelectedOutputDevice();
        if (selected == null) {
            OUTPUT_DEVICE_SELECTOR.getTooltip().setText("Default Output Device");
            return;
        }
        else {
            OUTPUT_DEVICE_SELECTOR.getTooltip().setText(selected.name());
        }
        setDevices(AudioIOManager.getOutputDevices(), selected);
    }



    @Override
    public void setGain(){
        OUTPUT_CHANNEL_STRIP_ROOT.fireEvent(new GainChangeEvent(GainChangeEvent.OUTPUT_GAIN_CHANGE_EVENT, calculateGainPercentage()));
    }

    private void handleDeviceSelection(String device) {
        OUTPUT_DEVICE_SELECTOR.getTooltip().setText(device);
        OUTPUT_CHANNEL_STRIP_ROOT.fireEvent(new AudioDeviceEvent(AudioDeviceEvent.OUTPUT_DEVICE_CHANGED, AudioIOManager.getInputDevices().stream().filter(d -> d.name().equals(device)).findFirst().orElse(null)));
    }

    public void setDevices(List<AudioIOManager.AudioDevice> devices, AudioIOManager.AudioDevice selected) {
        OUTPUT_DEVICE_SELECTOR.getItems().clear();
        for (AudioIOManager.AudioDevice device : devices) {
            RadioMenuItem item = new RadioMenuItem(device.name());
            item.setOnAction(e -> handleDeviceSelection(device.name()));

            // Select the item if it matches the selected device
            if (selected != null && device.name().equals(selected.name())) {
                item.setSelected(true);
            }

            OUTPUT_DEVICE_SELECTOR.getItems().add(item);
        }

        OUTPUT_DEVICE_SELECTOR.getItems().forEach(item -> {
            if (item instanceof RadioMenuItem) {
                ((RadioMenuItem) item).setToggleGroup(outputDeviceToggleGroup);
            }
        });
    }
}
