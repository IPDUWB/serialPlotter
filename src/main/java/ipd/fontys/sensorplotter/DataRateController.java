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

import ipd.fontys.rtt.RttReader;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataRateController implements Initializable {

    private final static int MAX_SAMPLES = 10;

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
    @SuppressWarnings("Duplicates")
    public void initialize(URL url, ResourceBundle rb) {
        // Chart and chartdata
        timeAxis.setTickLabelFormatter(new StringConverter<Number>(){
            @Override
            public String toString(Number t) {
                return new SimpleDateFormat("mm:ss").
                        format(new Date(t.longValue()));
            }
            @Override
            public Number fromString(String string) {
                throw new UnsupportedOperationException("Not supported");
            }
        });
        RttReader reader;
        try {
            reader = new RttReader();
            reader.connect("localhost", 19021);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unable to read from localhost:19021");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        Thread readerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                String msg = reader.getMessage();
                if(msg.startsWith("t")) {
                    int time = Integer.valueOf(StringUtils.substringBetween(msg, "time:", ";"));
                    int data = Integer.valueOf(StringUtils.substringAfter(msg, "len:"));
                    double dataRate = (double) data / (time * 1000) * 8;
                    dataCollection.add(new XYChart.Data<>(System.currentTimeMillis(), dataRate));

                    Platform.runLater(() -> {
                        textVal.setText(String.format("%.3f Mbit/s", dataRate));
                    });
                }
            }
        });

        readerThread.setDaemon(true);
        readerThread.start();

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
