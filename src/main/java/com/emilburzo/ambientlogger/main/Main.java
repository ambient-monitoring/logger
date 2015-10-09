package com.emilburzo.ambientlogger.main;

import com.emilburzo.ambientlogger.serial.Serial;

public class Main {

    private static Serial serial;

    public static void main(String[] args) {
        System.out.println("Main.main");

        serial = new Serial();

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    static class ShutdownHook extends Thread {

        @Override
        public void run() {
            System.out.println("ShutdownHook.run");

            serial.setStop(true);
        }
    }
}
