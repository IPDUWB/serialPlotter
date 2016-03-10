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

package org.expo.serial;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author bart452
 */
public class Serial {
    
    private String serialPort;
    private StringBuilder stringBuilder = new StringBuilder();
    private SerialPort sp;
    private final StringProperty data = new SimpleStringProperty("");
    
    /**
     * Open a serialport
     * @param port Device file to open e.g. "/dev/ttyUSB0"
     */
    public Serial(String port) {
        serialPort = port;
        Runtime.getRuntime().addShutdownHook((new Thread() {
            @Override
            public void run() {
                close();
            }
        }));
    }
    
    /**
     * Change the serial device. If the serial device is already opened, it will
     * be closed.
     * @param serialPort 
     */
    public void setSerialPort(String serialPort) {
        if(isOpen())
            close();
        this.serialPort = serialPort;
    }
    
    /**
     * Get the name of the currently opened port
     * @return serialport devicefile name
     */
    public String getPortName() {
        return serialPort;
    }
    
    
    /**
     * Open the serialport and create a connection. This function creates a
     * event listener that is triggerd when serial data is available
     * @param baudrate  The baudrate to configure the serial device with
     * @return true of the port is succesfully opened, false otherwise
     */
    public boolean open(int baudrate) {
        try {
            sp = new SerialPort(serialPort);
            if(sp.openPort()) {
                sp.setParams(baudrate, 
                        SerialPort.DATABITS_8, 
                        SerialPort.STOPBITS_1, 
                        SerialPort.PARITY_NONE);
                sp.setEventsMask(SerialPort.MASK_RXCHAR);
                sp.addEventListener(event -> {
                    if(event.isRXCHAR()) {
                        try {
                            stringBuilder.append(sp.readString());
                            String s  = stringBuilder.toString();
                            if(s.contains("\n")) {
                                if(!s.substring(0, s.indexOf("\n")).equals(""))
                                        data.set(s.substring(0, s.indexOf("\n")));
                                stringBuilder = new StringBuilder();
                            }
                        } catch(SerialPortException e) {
                            System.out.println("Failed to read serial data \n" + e);
                        }
                    }
                });
            }
        } catch (SerialPortException e) {
            System.err.println("Failed to open serialport \n" + e);
            sp = null;
        }
        return (sp != null);
    }
    
    /**
     * Write a byte to the serialport. Serial device has to be opened
     * @param data Bytes to write
     */
    public void writeByte(byte data) {
        try {
            sp.writeByte(data);
        } catch(SerialPortException e) {
            System.err.println("Failed to write byte \n" +  e);
        }
    }
    
    /**
     * Write a byte array to the serialport. The device has to be open
     * @param data Bytes to write
     */
    public void writeBytes(byte data[]) {
        try {
            sp.writeBytes(data);
        } catch(SerialPortException e) {
            System.err.println("Failed to write byte \n" +  e);
        }
    }
    
    /**
     * Get the data from the serial device
     * @return The data received
     */
    public String getData() {
        return data.get();
    }
    
    /**
     * Add a callback when data is received
     * @param listener The callback to trigger when data is received
     */
    public void addListener(ChangeListener<String> listener) {
        data.addListener(listener);
    }
    
    /**
     * Test if the serial port is open
     * @return true is open, false is closed
     */
    public boolean isOpen() {
        return sp == null ? false : sp.isOpened();
    }
    
    /**
     * Close the serial device
     */
    public void close() {
        if(sp != null) {
            try {
                sp.removeEventListener();
                if(sp.isOpened())
                    sp.closePort();
            } catch(SerialPortException e) {
                System.err.println("Failed closing serial \n" + e);
            }
        }
        System.out.println("Closed serialport");
    }
    
    public static ArrayList<String> getSerialPorts() {
        return Arrays.asList(new File("/dev").listFiles())
                .stream()
                .filter((file) -> (file.getName().contains("USB") 
                        || file.getName().contains("ACM")))
                .map(File::getAbsolutePath)
                .collect(Collectors.toCollection(ArrayList<String>::new));
    }
    
}
