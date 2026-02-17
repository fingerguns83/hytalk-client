package net.fg83.hytalkclient.ui.controller.channelstrip;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.ui.event.AudioDeviceEvent;
import net.fg83.hytalkclient.ui.event.GainChangeEvent;
import net.fg83.hytalkclient.util.AppConstants;

import javax.sound.sampled.Line;

import java.util.List;

import static java.lang.Math.round;

public class OutputChannelStripController extends ChannelStripController{
    @FXML
    private VBox OUTPUT_CHANNEL_STRIP_ROOT;

    @FXML
    private MenuButton OUTPUT_DEVICE_SELECTOR;

    private final ToggleGroup outputDeviceToggleGroup = new ToggleGroup();

    @Override
    public void setup(){
        initializeDeviceSelector();
        initializeFaderCap();
        initializeFaderLocation();
    }

    @Override
    protected void initializeFaderCap() {
        faderCap = (ImageView) OUTPUT_CHANNEL_STRIP_ROOT.lookup(".fader-cap");
    }
    @Override
    protected void initializeVUMeter() {
        VUMeter = (Line) OUTPUT_CHANNEL_STRIP_ROOT.lookup(".vu-meter-mask");
    }

    protected void initializeDeviceSelector(){
        setDevices(AudioIOManager.getOutputDevices(), null);

    }

    @Override
    public void setGain(){
        OUTPUT_CHANNEL_STRIP_ROOT.fireEvent(new GainChangeEvent(GainChangeEvent.OUTPUT_GAIN_CHANGE_EVENT, calculateGainPercentage()));
    }

    private void handleDeviceSelection(String device) {
        OUTPUT_CHANNEL_STRIP_ROOT.fireEvent(new AudioDeviceEvent(AudioDeviceEvent.OUTPUT_DEVICE_CHANGED, AudioIOManager.getInputDevices().stream().filter(d -> d.name().equals(device)).findFirst().orElse(null)));
    }

    public void setDevices(List<AudioIOManager.AudioDevice> devices, AudioIOManager.AudioDevice selected) {
        OUTPUT_DEVICE_SELECTOR.getItems().clear();
        for (AudioIOManager.AudioDevice device : devices) {
            RadioMenuItem item = new RadioMenuItem(device.name());
            //item.setStyle();
            item.setOnAction(e -> handleDeviceSelection(device.name()));

            if (selected != null){
                item.setSelected(device.name().equals(selected.name()));
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
