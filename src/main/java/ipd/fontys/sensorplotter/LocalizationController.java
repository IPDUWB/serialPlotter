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
    private double r1Beacon = 3.81;
    private double x1Beacon = 1.01;
    private double y1Beacon = 0.6;
    private double r2Beacon = 0.79;
    private double x2Beacon = 5.4;
    private double y2Beacon = 1.8;
    private double r3Beacon = 4.94;
    private double x3Beacon = 3;
    private double y3Beacon = 6;
    private double x1Tag;
    private double y1Tag;
    private double x2Tag;
    private double y2Tag;
    private double x3Tag;
    private double y3Tag;
    private double x4Tag;
    private double y4Tag;
    private double xTag;
    private double yTag;




    double[] value1Array = new double[4];
    double[] value2Array = new double[4];

    @FXML
    private BubbleChart<Number, Number> bubbleChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Chart and chartdata
        Serial serialPort = ContainerController.getInstance().getSerial();
        serialPort.addListener((obs, oldVal, newVal) -> {
            try {
               // System.out.println("New Value is" + newVal);
                if (newVal.contains("x")) {
                    //r1Beacon = Double.valueOf(newVal.replaceAll("[a-z]", ""));
                }
                if (newVal.contains("y")) {
                    //r2Beacon = Double.valueOf(newVal.replaceAll("[a-z]", ""));
                }
                if (newVal.contains("z")) {
                    //r3Beacon = Double.valueOf(newVal.replaceAll("[a-z]", ""));
                }
               // System.out.println("test1" + r1Beacon);
              //  System.out.println("test2" + r2Beacon);
              //  System.out.println("test3" + r3Beacon);
                value1Array = triangulation(y1Beacon,y2Beacon,x1Beacon,x2Beacon,r1Beacon,r2Beacon);
                y1Tag = value1Array[0];
                y2Tag = value1Array[1];
                x1Tag = value1Array[2];
                x2Tag = value1Array[3];
                value2Array = triangulation(y1Beacon,y3Beacon,x1Beacon,x3Beacon,r1Beacon,r3Beacon);
                y3Tag = value2Array[0];
                y4Tag = value2Array[1];
                x3Tag = value2Array[2];
                x4Tag = value2Array[3];


                System.out.println("c1 ( " + x1Tag + "," + y1Tag + " )");
                System.out.println("c2 ( " + x2Tag + "," + y2Tag + " )");
                System.out.println("c3 ( " + x3Tag + "," + y3Tag + " )");
                System.out.println("c4 ( " + x4Tag + "," + y4Tag + " )");




            } catch (NumberFormatException e) {
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
                if (xNumOfSamples > MAX_SAMPLES)
                    xDataSeries.getData().remove(0,
                            xNumOfSamples - MAX_SAMPLES);
                xDataCollection.clear();
            }
        }.start();
    }

        double [] triangulation (double y1Coordinate, double y2Coordinate, double x1Coordinate, double x2Coordinate, double r1Coordinate, double r2Coordinate)
        {
            double A;
            double B;
            double C;
            double y2;
            double y1;
            double y0;

            double [] array;
            array = new double[4];
            double y1Tag;
            double y2Tag;
            double x1Tag;
            double x2Tag;

            A = (r1Coordinate * r1Coordinate) - (r2Coordinate * r2Coordinate) - ((x1Coordinate * x1Coordinate) - (x2Coordinate * x2Coordinate)) - ((y1Coordinate * y1Coordinate) - (y2Coordinate * y2Coordinate));
            B = -2 * (x1Coordinate - x2Coordinate);
            C = (2 * y1Coordinate) - (2 * y2Coordinate);

            y2 = (C * C) / (B * B) + 1;
            y1 = (2 * A * C / (B * B)) - (2 * C * x1Coordinate )/ B - 2 * y1Coordinate;
            y0 = (A * A) / (B * B) - (2 * A * x1Coordinate) / B + x1Coordinate * x1Coordinate + y1Coordinate * y1Coordinate - r1Coordinate * r1Coordinate;

            //Finding out the roots

            double D1 = y1 * y1 - 4 * y2 * y0;
            if (D1 >= 0)
            {
                D1 = Math.sqrt(D1);
                y1Tag = (-y1 + D1) / (2 * y2);
                y2Tag = (-y1 - D1) / (2 * y2);

                x1Tag = (C * y1Tag + A) / B;
                x2Tag = (C * y2Tag + A) / B;

                // System.out.println("The roots of the Quadratic Equation \"2x2 + 6x + 4 = 0\" are " + y1Tag + " and " + y2Tag);
                array[0] = y1Tag;
                array[1] = y2Tag;
                array[2] = x1Tag;
                array[3] = x2Tag;
                return array;
            }
            else
            {
                System.out.println("Circles do not cross!");
                return array;
            }
        }

}