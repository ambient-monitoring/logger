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
        while (true) {
            // todo
            // * retry connecting to mongodb if connection lost
            // * retry connecting to the serial port if connection lost
            // * ensure indexes are created

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

            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void onReadingAvailable(String decoded) {
        System.out.print(decoded);

        Reading reading = getReading(decoded);

        if (reading == null) {
            return;
        }

        String json = new Gson().toJson(reading);

        MongoDB.getReadingCollection().insertOne(Document.parse(json));
    }

    private Reading getReading(String decoded) {
        Reading reading = new Reading();

        // th,id,temp,humidity,voltage,packet count
        // example:
        // th,4,6.7,64.0,4361,44

        String[] split = decoded.split(VALUE_SEPARATOR);

        int i = 0;

        // sanity check
        // todo use factories for different sensor types
        if (!split[i++].equalsIgnoreCase("th")) {
            return null;
        }

        reading.id = Integer.valueOf(split[i++].trim());
        reading.temperature = Double.valueOf(split[i++].trim());
        reading.humidity = Double.valueOf(split[i++].trim());
        reading.voltage = Integer.valueOf(split[i++].trim());
        reading.counter = Integer.valueOf(split[i++].trim());
        reading.timestamp = new Date().getTime();

        return reading;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

}
