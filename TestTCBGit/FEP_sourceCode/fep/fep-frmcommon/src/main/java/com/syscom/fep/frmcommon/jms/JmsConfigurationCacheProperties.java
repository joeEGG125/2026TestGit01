package com.syscom.fep.frmcommon.jms;

import org.springframework.boot.autoconfigure.jms.JmsProperties;

public class JmsConfigurationCacheProperties extends JmsProperties.Cache {
    private boolean reconnectOnException = true;

    public boolean isReconnectOnException() {
        return reconnectOnException;
    }

    public void setReconnectOnException(boolean reconnectOnException) {
        this.reconnectOnException = reconnectOnException;
    }
}
