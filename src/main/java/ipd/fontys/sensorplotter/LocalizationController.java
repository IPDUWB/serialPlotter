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
    double[] beacon12Array = new double[5];
    double[] beacon13Array = new double[5];
    double[] beacon23Array = new double[5];
    private double discr = 1;

    //variable values
    private double rIncrement = 0.1;

    //objects
    private Beacon beacon1 = new Beacon(4,3); //remove object and constructor if this should be variable
    private Beacon beacon2 = new Beacon(8,2);
    private Beacon beacon3 = new Beacon(7.45,8.39);
    private Crossing crossing12 = new Crossing();
    private Crossing crossing13 = new Crossing();
    private Crossing crossing23 = new Crossing();

    @FXML
    private BubbleChart<Number, Number> bubbleChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //open serial port
        Serial serialPort = ContainerController.getInstance().getSerial();
        //get radius
        serialPort.addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal.contains("x")) { //r beacon 1
                    beacon1.r = Double.valueOf(newVal.replaceAll("[a-z]", ""));
                }
                if (newVal.contains("y")) { //r beacon 2
                    beacon2.r = Double.valueOf(newVal.replaceAll("[a-z]", ""));
                }
                if (newVal.contains("z")) { //r beacon 3
                    beacon3.r = Double.valueOf(newVal.replaceAll("[a-z]", ""));
                }

                //crossings
                //beacon 1 and beacon 2
                beacon12Array = triangulation(beacon1.y,beacon2.y,beacon1.x,beacon2.x,beacon1.r,beacon2.r, discr);
                discr = beacon12Array[4];
                while(discr == 0)
                {
                    beacon1.r = beacon1.r + rIncrement;
                    beacon2.r = beacon2.r + rIncrement;
                    beacon12Array = triangulation(beacon1.y,beacon2.y,beacon1.x,beacon2.x,beacon1.r,beacon2.r, discr);
                    discr = beacon12Array[4];
                }
                crossing12.y1 = beacon12Array[0];
                crossing12.y2 = beacon12Array[1];
                crossing12.x1 = beacon12Array[2];
                crossing12.x2 = beacon12Array[3];

                //beacon 1 and beacon 3
                beacon13Array = triangulation(beacon1.y,beacon3.y,beacon1.x,beacon3.x,beacon1.r,beacon3.r, discr);
                discr = beacon13Array[4];
                while(discr == 0)
                {
                    beacon1.r = beacon1.r + rIncrement;
                    beacon3.r = beacon3.r + rIncrement;
                    beacon13Array = triangulation(beacon1.y,beacon3.y,beacon1.x,beacon3.x,beacon1.r,beacon3.r, discr);
                    discr = beacon13Array[4];
                }
                crossing13.y1 = beacon13Array[0];
                crossing13.y2 = beacon13Array[1];
                crossing13.x1 = beacon13Array[2];
                crossing13.x2 = beacon13Array[3];

                //beacon 2 and beacon 3
                beacon23Array = triangulation(beacon2.y,beacon3.y,beacon2.x,beacon3.x,beacon2.r,beacon3.r, discr);
                discr = beacon23Array[4];
                while(discr == 0)
                {
                    beacon2.r = beacon2.r + rIncrement;
                    beacon3.r = beacon3.r + rIncrement;
                    beacon23Array = triangulation(beacon2.y,beacon3.y,beacon2.x,beacon3.x,beacon2.r,beacon3.r, discr);
                    discr = beacon23Array[4];
                }
                crossing23.y1 = beacon23Array[0];
                crossing23.y2 = beacon23Array[1];
                crossing23.x1 = beacon23Array[2];
                crossing23.x2 = beacon23Array[3];

                //closed crossing
                crossing23 = determineR(crossing23.y1, crossing23.y2, crossing23.x1, crossing23.x2, beacon1.y, beacon1.x); //between crossing23 and beacon1
                crossing13 = determineR(crossing13.y1, crossing13.y2, crossing13.x1, crossing13.x2, beacon2.y, beacon2.x); //between crossing13 and beacon2
                crossing12 = determineR(crossing12.y1, crossing12.y2, crossing12.x1, crossing12.x2, beacon3.y, beacon3.x); //between crossing12 and beacon3

                //print
                System.out.println("crossing23(" + String.format("%.2f", crossing23.x1) +"," + String.format("%.2f",crossing23.y1) + ")");
                System.out.println("crossing13(" + String.format("%.2f", crossing13.x1) +"," + String.format("%.2f",crossing13.y1) + ")");
                System.out.println("crossing12(" + String.format("%.2f", crossing12.x1) +"," + String.format("%.2f",crossing12.y1) + ")");

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

    //functions
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
    double [] triangulation (double y1Coordinate, double y2Coordinate, double x1Coordinate, double x2Coordinate, double r1Coordinate, double r2Coordinate, double discriminant)
        {
            double A;
            double B;
            double C;
            double y2;
            double y1;
            double y0;

            double [] array;
            array = new double[5];
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
            if (D1 > 0)
            {
                D1 = Math.sqrt(D1);
                y1Tag = (-y1 + D1) / (2 * y2);
                y2Tag = (-y1 - D1) / (2 * y2);

                x1Tag = (C * y1Tag + A) / B;
                x2Tag = (C * y2Tag + A) / B;

                array[0] = y1Tag;
                array[1] = y2Tag;
                array[2] = x1Tag;
                array[3] = x2Tag;
                array[4] = 1.0;
                return array;
            }
            else
            {
                array[4] = 0.0;
               // System.out.println("Circles do not cross!");
                return array;
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