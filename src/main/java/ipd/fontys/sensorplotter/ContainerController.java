package ipd.fontys.sensorplotter;

import ipd.fontys.serial.Serial;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleButton;
import jssc.SerialPort;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ContainerController implements Initializable {

    @FXML
    public Tab distanceTab;
    @FXML
    public Tab dataRateTab;
    @FXML
    private ToggleButton connButton;
    @FXML
    private ChoiceBox<String> serialDevBox;

    private Serial serialPort;

    private static ContainerController containerController;

    public static ContainerController getInstance() {
        return containerController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        containerController = this;
        serialPort = new Serial("");
        // Serial port choicebpox
        serialDevBox.getItems().setAll(Serial.getSerialPorts());
        serialDevBox.getSelectionModel().selectFirst();
        // Serial port connection button
        connButton.setOnAction((ActionEvent event) -> {
            if(connButton.isSelected()) {
                serialPort.setSerialPort(serialDevBox.getValue());
                if(!serialPort.open(SerialPort.BAUDRATE_115200)) {
                    connButton.setSelected(false);
                }
            } else {
                serialPort.close();
            }
        });
        // This needs to be done last always!!
        try {
            distanceTab.setContent(FXMLLoader.load(getClass().getResource("/fxml/Distance.fxml")));
            dataRateTab.setContent(FXMLLoader.load(getClass().getResource("/fxml/DataRate.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Serial getSerial() {
        return serialPort;
    }
}
