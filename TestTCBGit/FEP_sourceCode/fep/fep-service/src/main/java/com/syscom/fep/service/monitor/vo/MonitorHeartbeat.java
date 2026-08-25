package com.syscom.fep.service.monitor.vo;

public class MonitorHeartbeat {
    private String fromHostIp;
    private String fromHostName;
    private String fromAppName;

    public String getFromHostIp() {
        return fromHostIp;
    }

    public void setFromHostIp(String fromHostIp) {
        this.fromHostIp = fromHostIp;
    }

    public String getFromHostName() {
        return fromHostName;
    }

    public void setFromHostName(String fromHostName) {
        this.fromHostName = fromHostName;
    }

    public String getFromAppName() {
        return fromAppName;
    }

    public void setFromAppName(String fromAppName) {
        this.fromAppName = fromAppName;
    }
}
