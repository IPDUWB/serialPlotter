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
    private double r1Beacon = 20;
    private double x1Beacon = 10;
    private double y1Beacon = 10;
    private double r2Beacon = 31.6227766;
    private double x2Beacon = 60;
    private double y2Beacon = 20;
    private double r3Beacon = 29.222594;
    private double x3Beacon = 42.12;
    private double y3Beacon = 36.59;
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
    double[] difArrayX = new double[6];
    double[] difArrayY = new double[6];
    private int remember = 0;
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
                //Calculate x value
                difArrayX[0] = x1Tag-x2Tag;
                difArrayX[1] = x1Tag-x3Tag;
                difArrayX[2] = x1Tag-x4Tag;
                difArrayX[3] = x2Tag-x3Tag;
                difArrayX[4] = x2Tag-x4Tag;
                difArrayX[5] = x3Tag-x4Tag;

                xTag = Math.sqrt(difArrayX[0]*difArrayX[0]);;
                int i;
                for ( i = 1; i < 6; i++) {
                    difArrayX[i] = Math.sqrt(difArrayX[i]*difArrayX[i]);
                    if (xTag > difArrayX[i]){
                        remember = i;
                    }
                }
                if(remember==0 | remember==1 | remember==2){
                    xTag = x1Tag;
                }
                else if(remember==3 | remember==4){
                    xTag = x2Tag;
                }
                else{
                    xTag = x3Tag;
                }

                difArrayY[0] = y1Tag-y2Tag;
                difArrayY[1] = y1Tag-y3Tag;
                difArrayY[2] = y1Tag-y4Tag;
                difArrayY[3] = y2Tag-y3Tag;
                difArrayY[4] = y2Tag-y4Tag;
                difArrayY[5] = y3Tag-y4Tag;

                yTag = Math.sqrt(difArrayY[0]*difArrayY[0]);;
                int p;
                for ( p = 1; p < 6; p++) {
                    difArrayY[p] = Math.sqrt(difArrayY[p]*difArrayY[p]);
                    if (yTag > difArrayY[p]){
                        remember = p;
                    }
                }
                if(remember==0 | remember==1 | remember==2){
                    yTag = y1Tag;
                }
                else if(remember==3 | remember==4){
                    yTag = y2Tag;
                }
                else{
                    yTag = y3Tag;
                }

                System.out.println("c1 ( " + xTag + "," + yTag + " )");




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