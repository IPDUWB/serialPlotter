/*
 * Copyright 2016 Bart Monhemius.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ipd.fontys.sensorplotter;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;
import jssc.SerialPort;
import ipd.fontys.serial.Serial;

public class ChartController implements Initializable {

    private final static int MAX_SAMPLES = 40;
    private final static int REFRESH_RATE = 150;
    private int xNumOfSamples = 0;


    private Series<Number, Number> xDataSeries;

    private Serial serialPort;

    private Collection<Data<Number, Number>> xDataCollection;


    @FXML
    private NumberAxis timeAxis;
    @FXML
    private ToggleButton connButton;
    @FXML
    private ChoiceBox<String> serialDevBox;
    @FXML
    private LineChart<Number, Number> sensorChart;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serialPort = new Serial("");
        // Chart and chartdata
        xDataCollection = new ArrayList<>();
        xDataSeries = new Series<>();
        timeAxis.setTickLabelFormatter(new StringConverter<Number>(){
            @Override
            public String toString(Number t) {
                return new SimpleDateFormat("HH:mm:ss").
                        format(new Date(t.longValue()));
            }
            @Override
            public Number fromString(String string) {
                throw new UnsupportedOperationException("Not supported");
            }
        });
        xDataSeries.setName("X");
        sensorChart.getData().add(xDataSeries);

        // Serial port choicebpox
        serialDevBox.getItems().setAll(Serial.getSerialPorts());
        serialDevBox.getSelectionModel().selectFirst();
        // Serial port connection button
        serialPort.addListener((obs, oldVal, newVal) -> {
            try {
                System.out.println("New Value is" + newVal);
                if(newVal.contains("x")) {
                    xDataCollection.add(
                            new Data<>(System.currentTimeMillis(),
                                    Double.valueOf(newVal.replace("x",""))));
                }
            } catch(NumberFormatException e) {
                e.printStackTrace(System.err);
            }
        });
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
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized(xDataSeries) {
                    Platform.runLater(()-> {
                        xDataSeries.getData().addAll(xDataCollection);
                        xNumOfSamples = xDataSeries.getData().size();
                        if(xNumOfSamples > MAX_SAMPLES)
                            xDataSeries.getData().remove(0,
                                    xNumOfSamples - MAX_SAMPLES);
                        xDataCollection.clear();
                    });
                }
            }
        }, 0, REFRESH_RATE);
    }
}
