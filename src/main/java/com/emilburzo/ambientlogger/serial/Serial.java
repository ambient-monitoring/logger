package com.emilburzo.ambientlogger.serial;

import com.emilburzo.ambientlogger.model.Reading;
import com.emilburzo.ambientlogger.mongo.MongoDB;
import com.fazecast.jSerialComm.SerialPort;
import com.google.gson.Gson;
import org.bson.Document;

import java.util.Date;

public class Serial {

    private static final String ARDUINO_DESCRIPTIVE_PORT_NAME = "USB-to-Serial Port (ch341-uart)";
    private static final String VALUE_SEPARATOR = ",";

    private boolean stop = false;

    public Serial() {
        SerialPort arduino = null;

        SerialPort[] commPorts = SerialPort.getCommPorts();

        int i = 0;

        for (SerialPort port : commPorts) {
            System.out.println(String.format("[%d] %s (%s)", i, port.getSystemPortName(), port.getDescriptivePortName()));

            if (port.getDescriptivePortName().equalsIgnoreCase(ARDUINO_DESCRIPTIVE_PORT_NAME)) {
                arduino = port;
                break;
            }

            i++;
        }

        if (arduino == null) {
            System.out.println("Couldn't find Arduino on any port");
            System.exit(1);
        }

        arduino.openPort();

        try {
            while (!stop) {
                while (arduino.bytesAvailable() == 0) {
                    Thread.sleep(20);
                }

                byte[] readBuffer = new byte[arduino.bytesAvailable()];
                arduino.readBytes(readBuffer, readBuffer.length);

                String decoded = new String(readBuffer, "UTF-8");

                onReadingAvailable(decoded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("closing port");

            arduino.closePort();
        }
    }

    private void onReadingAvailable(String decoded) {
        System.out.print(decoded);

        Reading reading = getReading(decoded);

        String json = new Gson().toJson(reading);

        MongoDB.getReadingCollection().insertOne(Document.parse(json));
    }

    private Reading getReading(String decoded) {
        Reading reading = new Reading();

        // id,temp,humidity,voltage
        // example:
//        1,21.9,51.5,4137
//        2,22.6,48.0,4361
//        1,21.9,51.4,4137
//        2,22.5,47.9,4361
//        1,21.9,51.4,4137

        String[] split = decoded.split(VALUE_SEPARATOR);

        int i = 0;

        try {
            reading.id = Integer.valueOf(split[i++]);
        } catch (Exception e) {
        }

        try {
            reading.temperature = Double.valueOf(split[i++]);
        } catch (Exception e) {
        }

        try {
            reading.humidity = Double.valueOf(split[i++]);
        } catch (Exception e) {
        }

        try {
            reading.voltage = Integer.valueOf(split[i++]);
        } catch (Exception e) {
        }

        reading.timestamp = new Date().getTime();

        return reading;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

}
