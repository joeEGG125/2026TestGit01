package com.syscom.fep.frmcommon.polyfill;

import com.syscom.fep.frmcommon.log.LogHelper;

public class ManualResetEvent {
    private final LogHelper logger = new LogHelper();
    private final Object monitor = new Object();
    private volatile boolean open = false;

    public ManualResetEvent(boolean open) {
        this.open = open;
    }

    public void waitOne() {
        synchronized (monitor) {
            while (!open) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    logger.warn(e, e.getMessage());
                }
            }
        }
    }

    public boolean waitOne(long milliseconds) {
        synchronized (monitor) {
            if (open) {
                return true;
            }
            try {
                monitor.wait(milliseconds);
            } catch (InterruptedException e) {
                logger.warn(e, e.getMessage());
            }
            return open;
        }
    }

    public void set() {
        synchronized (monitor) {
            open = true;
            monitor.notifyAll();
        }
    }

    public void reset() {
        open = false;
    }
}
