package ipd.fontys.sensorplotter;

import ipd.fontys.serial.Serial;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DistanceController implements Initializable {

    private final static int MAX_SAMPLES = 300;
    private int xNumOfSamples = 0;
    private final XYChart.Series<Number, Number> xDataSeries = new XYChart.Series<>();
    private final Collection<XYChart.Data<Number, Number>> xDataCollection = new CopyOnWriteArrayList<>();
    @FXML
    private Slider sliderValue;
    @FXML
    private NumberAxis timeAxis;
    @FXML
    private LineChart<Number, Number> sensorChart;

    @FXML
    private TextField textValue;

    @Override
    @SuppressWarnings("Duplicates")
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

        Serial serialPort = ContainerController.getInstance().getSerial();
        serialPort.addListener((obs, oldVal, newVal) -> {
            try {
                System.out.println("New Value is" + newVal);
                if(newVal.contains("x")) {
                    xDataCollection.add(
                            new XYChart.Data<>(System.currentTimeMillis(),
                                    Double.valueOf(newVal.replaceAll("[a-z]",""))));
                    double distance = Double.valueOf(newVal.replaceAll("[a-z]",""));
                    Platform.runLater(() -> {
                        sliderValue.setValue(distance);
                        textValue.setText(newVal.replaceAll("[a-z]",""));
                    });
                }

            } catch(NumberFormatException e) {
                e.printStackTrace(System.err);
            }
        });

        xDataSeries.setName("Distance");
        sensorChart.getData().add(xDataSeries);

        new AnimationTimer() {
            @Override
            public void handle(long l) {
                xDataSeries.getData().addAll(xDataCollection);
                xNumOfSamples = xDataSeries.getData().size();
                if(xNumOfSamples > MAX_SAMPLES)
                    xDataSeries.getData().remove(0,
                            xNumOfSamples - MAX_SAMPLES);
                xDataCollection.clear();
            }
        }.start();
    }
}
