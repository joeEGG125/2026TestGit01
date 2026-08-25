package com.syscom.fep.gateway.entity;

/**
 * 連線ATMService的設定
 *
 * @author Richard
 */
public class ToFEPATM {
    private String host;
    private int port;
    private boolean enable = true;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "ToFEPATM{" +
                "host='" + host + '\'' +
                ",port=" + port +
                ",enable=" + enable +
                '}';
    }
}
