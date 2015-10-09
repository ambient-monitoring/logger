package com.emilburzo.ambientlogger.main;

import com.emilburzo.ambientlogger.serial.Serial;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

public class Daemon implements org.apache.commons.daemon.Daemon {

    private Serial serial;

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        System.out.println("Main.init");
    }

    @Override
    public void start() throws Exception {
        serial = new Serial();
    }

    @Override
    public void stop() throws Exception {
        serial.setStop(true);
    }

    @Override
    public void destroy() {
        serial = null;
    }
}
