package ipd.fontys.sensorplotter;

import ipd.fontys.serial.Serial;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.XYChart;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocalizationController implements Initializable {

    private final static int MAX_SAMPLES = 40;
    private int xNumOfSamples = 0;
    private final XYChart.Series<Number, Number> xDataSeries = new XYChart.Series<>();
    private final Collection<XYChart.Data<Number, Number>> xDataCollection = new CopyOnWriteArrayList<>();
    private double xValue;
    private double yValue;
    private double zValue;
    @FXML
    private BubbleChart<Number, Number> bubbleChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Chart and chartdata
        Serial serialPort = ContainerController.getInstance().getSerial();
        serialPort.addListener((obs, oldVal, newVal) -> {
            try {
                System.out.println("New Value is" + newVal);
                if(newVal.contains("x")) {
                    xValue = Double.valueOf(newVal.replaceAll("[a-z]",""));
                }
                if (newVal.contains("y")){
                    yValue = Double.valueOf(newVal.replaceAll("[a-z]",""));
                }
                if (newVal.contains("z")){
                    zValue = Double.valueOf(newVal.replaceAll("[a-z]",""));
                }
                System.out.println("test" + xValue);
                System.out.println("test" + yValue);
                System.out.println("test" + zValue);
            } catch(NumberFormatException e) {
                e.printStackTrace(System.err);
            }
        });



        xDataSeries.setName("Distance");
        bubbleChart.getData().add(xDataSeries);

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

            /*double A;
            double B;
            double C;
            A = C*C/B*B + 1;
            B = ((2*A*C/B*B)-(2*C*xValue/B)-2*yValue);

            //Finding out the roots
            double temp1 = Math.sqrt(B * B - 4 * A * C);

            double root1 = (-B +  temp1) / (2*A) ;
            double root2 = (-B -  temp1) / (2*A) ;

            System.out.println("The roots of the Quadratic Equation \"2x2 + 6x + 4 = 0\" are "+root1+" and "+root2);*/



    }

}