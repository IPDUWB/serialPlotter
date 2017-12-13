package ipd.fontys.sensorplotter;

import ipd.fontys.serial.Serial;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocalizationController implements Initializable {


    @FXML
    private TextField textFieldx1;

    @FXML
    private TextField textFieldy1;

    @FXML
    private TextField textBoxR1;

    @FXML
    private TextField textFieldx2;

    @FXML
    private TextField textFieldy2;

    @FXML
    private TextField textBoxR2;

    @FXML
    private TextField textFieldx3;

    @FXML
    private TextField textFieldy3;

    @FXML
    private TextField textBoxR3;

    @FXML
    private Button buttonLoad;

    @FXML
    private BubbleChart<Number, Number> bubbleChart;

    private final static int MAX_SAMPLES = 40;
    private int xNumOfSamples = 0;
    final NumberAxis xAxis = new NumberAxis(0, 50, 4);
    final NumberAxis yAxis = new NumberAxis(0, 50, 10);
    private final XYChart.Series<Number, Number> xDataSeries = new XYChart.Series<>();
    private final Collection<XYChart.Data<Number, Number>> xDataCollection = new CopyOnWriteArrayList<>();
    double[] beacon12Array = new double[5];
    double[] beacon13Array = new double[5];
    double[] beacon23Array = new double[5];

    //variable values
    private double rIncrement = 0.1;

    //objects
    private Beacon beacon1 = new Beacon(6,4);
    private Beacon beacon2 = new Beacon(6,20);
    private Beacon beacon3 = new Beacon(22,8);
    private Crossing crossing12 = new Crossing();
    private Crossing crossing13 = new Crossing();
    private Crossing crossing23 = new Crossing();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        textFieldx1.setText(Double.toString(beacon1.x));
        textFieldx2.setText(Double.toString(beacon2.x));
        textFieldx3.setText(Double.toString(beacon3.x));
        textFieldy1.setText(Double.toString(beacon1.y));
        textFieldy2.setText(Double.toString(beacon2.y));
        textFieldy3.setText(Double.toString(beacon3.y));
        bubbleChart.getData().addAll(xDataSeries);
        //open serial port
        Serial serialPort = ContainerController.getInstance().getSerial();
        //get radius between beacons and tag
        serialPort.addListener((obs, oldVal, newVal) -> {
            try {

                if (newVal.contains("x")) { //r beacon 1
                    beacon1.r = Double.valueOf(newVal.replaceAll("[a-z]", "")); //r beacon 1
                    textBoxR1.setText(Double.toString(beacon1.r));
                }
                if (newVal.contains("y")) { //r beacon 2
                    beacon2.r = Double.valueOf(newVal.replaceAll("[a-z]", "")); //r beacon 2
                    textBoxR2.setText(Double.toString(beacon2.r));
                }
                if (newVal.contains("z")) { //r beacon 3
                    beacon3.r = Double.valueOf(newVal.replaceAll("[a-z]", "")); //r beacon 3
                    textBoxR3.setText(Double.toString(beacon3.r));
                }

                buttonLoad.setOnAction((ActionEvent event) ->{
                    beacon1.x = Double.valueOf(textFieldx1.getText());
                    beacon1.y = Double.valueOf(textFieldy1.getText());
                    beacon2.x = Double.valueOf(textFieldx2.getText());
                    beacon2.y = Double.valueOf(textFieldy2.getText());
                    beacon3.x = Double.valueOf(textFieldx3.getText());
                    beacon3.y = Double.valueOf(textFieldy3.getText());
                    xDataCollection.add(new XYChart.Data<>(beacon1.x,beacon1.y, beacon1.r));
                    xDataCollection.add(new XYChart.Data<>(beacon2.x,beacon2.y, beacon2.r));
                    xDataCollection.add(new XYChart.Data<>(beacon3.x,beacon3.y, beacon3.r));
                });

                //crossings
                double B;
                B = -2 * (beacon1.x - beacon2.x);
                if (B == 0){
                    beacon12Array = triangulation(beacon1.x, beacon2.x, beacon1.y, beacon2.y, beacon1.r, beacon2.r, rIncrement);  //beacon 1 and beacon 2
                    crossing12.x1 = beacon12Array[0];
                    crossing12.x2 = beacon12Array[1];
                    crossing12.y1 = beacon12Array[2];
                    crossing12.y2 = beacon12Array[3];
                }
                else {
                    beacon12Array = triangulation(beacon1.y, beacon2.y, beacon1.x, beacon2.x, beacon1.r, beacon2.r, rIncrement);
                    crossing12.y1 = beacon12Array[0];
                    crossing12.y2 = beacon12Array[1];
                    crossing12.x1 = beacon12Array[2];
                    crossing12.x2 = beacon12Array[3];
                }

                B = -2 * (beacon1.x - beacon3.x);
                if (B == 0){
                    beacon13Array = triangulation(beacon1.x, beacon3.x, beacon1.y, beacon3.y, beacon1.r, beacon3.r, rIncrement);  //beacon 1 and beacon 2
                    crossing13.x1 = beacon13Array[0];
                    crossing13.x2 = beacon13Array[1];
                    crossing13.y1 = beacon13Array[2];
                    crossing13.y2 = beacon13Array[3];
                }
                else {
                    beacon13Array = triangulation(beacon1.y, beacon3.y, beacon1.x, beacon3.x, beacon1.r, beacon3.r, rIncrement);
                    crossing13.y1 = beacon13Array[0];
                    crossing13.y2 = beacon13Array[1];
                    crossing13.x1 = beacon13Array[2];
                    crossing13.x2 = beacon13Array[3];
                }

                B = -2 * (beacon2.x - beacon3.x);
                if (B == 0){
                    beacon23Array = triangulation(beacon2.x, beacon3.x, beacon2.y, beacon3.y, beacon2.r, beacon3.r, rIncrement);  //beacon 2 and beacon 3
                    crossing23.x1 = beacon23Array[0];
                    crossing23.x2 = beacon23Array[1];
                    crossing23.y1 = beacon23Array[2];
                    crossing23.y2 = beacon23Array[3];
                }
                else {
                    beacon23Array = triangulation(beacon2.y, beacon3.y, beacon2.x, beacon3.x, beacon2.r, beacon3.r, rIncrement);
                    crossing23.y1 = beacon23Array[0];
                    crossing23.y2 = beacon23Array[1];
                    crossing23.x1 = beacon23Array[2];
                    crossing23.x2 = beacon23Array[3];
                }

                //closest crossing
                crossing23 = determineR(crossing23.y1, crossing23.y2, crossing23.x1, crossing23.x2, beacon1.y, beacon1.x); //between crossing23 and beacon1
                crossing13 = determineR(crossing13.y1, crossing13.y2, crossing13.x1, crossing13.x2, beacon2.y, beacon2.x); //between crossing13 and beacon2
                crossing12 = determineR(crossing12.y1, crossing12.y2, crossing12.x1, crossing12.x2, beacon3.y, beacon3.x); //between crossing12 and beacon3

                //print
                System.out.println("crossing23(" + String.format("%.2f", crossing23.x1) +"," + String.format("%.2f",crossing23.y1) + ")"); //coordinate 1
                System.out.println("crossing13(" + String.format("%.2f", crossing13.x1) +"," + String.format("%.2f",crossing13.y1) + ")"); //coordinate 2
                System.out.println("crossing12(" + String.format("%.2f", crossing12.x1) +"," + String.format("%.2f",crossing12.y1) + ")"); //coordinate 3

            } catch (NumberFormatException e) {
                e.printStackTrace(System.err);

            }
        });

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

    //functions

    //determine crossings of circles
    double [] triangulation (double y1Coordinate, double y2Coordinate, double x1Coordinate, double x2Coordinate, double r1Coordinate, double r2Coordinate, double rIncrement) {
        double A;
        double B;
        double C;
        double y2;
        double y1;
        double y0;

        double[] array;
        array = new double[4];
        double y1Tag;
        double y2Tag;
        double x1Tag;
        double x2Tag;
        int dOk = 0;

        while (dOk == 0) {
            A = (r1Coordinate * r1Coordinate) - (r2Coordinate * r2Coordinate) - ((x1Coordinate * x1Coordinate) - (x2Coordinate * x2Coordinate)) - ((y1Coordinate * y1Coordinate) - (y2Coordinate * y2Coordinate));
            B = -2 * (x1Coordinate - x2Coordinate);
            C = (2 * y1Coordinate) - (2 * y2Coordinate);

            y2 = (C * C) / (B * B) + 1;
            y1 = (2 * A * C / (B * B)) - (2 * C * x1Coordinate) / B - 2 * y1Coordinate;
            y0 = (A * A) / (B * B) - (2 * A * x1Coordinate) / B + x1Coordinate * x1Coordinate + y1Coordinate * y1Coordinate - r1Coordinate * r1Coordinate;

            //Finding out the roots
            double D1 = y1 * y1 - 4 * y2 * y0;
            if (D1 > 0) {
                D1 = Math.sqrt(D1);
                y1Tag = (-y1 + D1) / (2 * y2);
                y2Tag = (-y1 - D1) / (2 * y2);

                x1Tag = (C * y1Tag + A) / B;
                x2Tag = (C * y2Tag + A) / B;

                array[0] = y1Tag;
                array[1] = y2Tag;
                array[2] = x1Tag;
                array[3] = x2Tag;
                dOk = 1;
            } else {
                r1Coordinate = r1Coordinate + rIncrement;
                r2Coordinate = r2Coordinate + rIncrement;
                dOk = 0;
            }
        }
        return array;
    }

    //determine radius between crossing and independent beacon
    private Crossing determineR (double y1, double y2, double x1, double x2, double beaconY, double beaconX)
    {
        double a1;
        double b1;
        double r1;
        double a2;
        double b2;
        double r2;

        a1 = beaconX - x1;
        b1 = beaconY - y1;
        r1 = Math.sqrt(a1 * a1 + b1 *b1);

        a2 = beaconX - x2;
        b2 = beaconY - y2;
        r2 = Math.sqrt(a2 * a2 + b2 *b2);

        if (r1 < r2){
            return new Crossing(x1,y1);
        }
        else
        {
            return new Crossing(x2,y2);
        }

    }
        //classes
        private static class Beacon {
            public double x = 0;
            public double y = 0;
            public double r = 0;

            public Beacon(double x, double y) {
                this.x = x;
                this.y = y;
            }

            public Beacon() {
            }

        }
        private static class Crossing{
            //properties
            public double x1 = 0;
            public double y1 = 0;
            public double x2 = 0;
            public double y2 = 0;

            //constructors
            //all crossings
            public Crossing() {
            }

            //closed crossing
            public Crossing(double x1, double y1) {
                this.x1 = x1;
                this.y1 = y1;
            }
        }
}