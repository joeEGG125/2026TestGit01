package com.syscom.fep.service.monitor.vo;

import java.util.Calendar;

public class MonitorServerLauncherInfo extends MonitorServerInfo {
    private String cmdStart;
    private String httpStart;
    private Calendar latestCmdStart;
    private boolean printInputStream;

    public String getCmdStart() {
        return cmdStart;
    }

    public void setCmdStart(String cmdStart) {
        this.cmdStart = cmdStart;
    }

    public String getHttpStart() {
        return httpStart;
    }

    public void setHttpStart(String httpStart) {
        this.httpStart = httpStart;
    }

    public Calendar getLatestCmdStart() {
        return latestCmdStart;
    }

    public void setLatestCmdStart(Calendar latestCmdStart) {
        this.latestCmdStart = latestCmdStart;
    }

    public boolean isPrintInputStream() {
        return printInputStream;
    }

    public void setPrintInputStream(boolean printInputStream) {
        this.printInputStream = printInputStream;
    }
}
