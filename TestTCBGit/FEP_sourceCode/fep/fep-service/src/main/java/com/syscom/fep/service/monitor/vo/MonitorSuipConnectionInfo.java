package com.syscom.fep.service.monitor.vo;

public class MonitorSuipConnectionInfo extends MonitorServerInfo {
    private String cmd;
    private long timeout = -1;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
