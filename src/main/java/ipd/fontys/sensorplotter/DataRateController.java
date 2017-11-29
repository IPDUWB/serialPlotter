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

import ipd.fontys.serial.Serial;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataRateController implements Initializable {

    private final static int MAX_SAMPLES = 300;

    private int numOfSamples = 0;
    private final Series<Number, Number> dataSeries = new Series<>();
    private final Collection<Data<Number, Number>> dataCollection = new CopyOnWriteArrayList<>();

    @FXML
    private NumberAxis timeAxis;

    @FXML
    private LineChart<Number, Number> sensorChart;

    @FXML
    private TextField textVal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Chart and chartdata
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
        Serial serialPort = ContainerController.getInstance().getSerial();
        serialPort.addListener((obs, oldVal, newVal) -> {
            try {
                System.out.println("New Value is" + newVal);
                if(newVal.contains("y")) {
                    dataCollection.add(
                            new XYChart.Data<>(System.currentTimeMillis(),
                                    Double.valueOf(newVal.replaceAll("[a-z]",""))));
                    double distance = Double.valueOf(newVal.replaceAll("[a-z]",""));
                    Platform.runLater(() -> {
                        textVal.setText(newVal.replaceAll("[a-z]",""));
                    });
                }
            } catch(NumberFormatException e) {
                e.printStackTrace(System.err);
            }
        });
        dataSeries.setName("Data Rate");
        sensorChart.getData().add(dataSeries);

        new AnimationTimer() {
            @Override
            public void handle(long l) {
                dataSeries.getData().addAll(dataCollection);
                numOfSamples = dataSeries.getData().size();
                if(numOfSamples > MAX_SAMPLES)
                    dataSeries.getData().remove(0,
                            numOfSamples - MAX_SAMPLES);
                dataCollection.clear();
            }
        }.start();
    }
}
