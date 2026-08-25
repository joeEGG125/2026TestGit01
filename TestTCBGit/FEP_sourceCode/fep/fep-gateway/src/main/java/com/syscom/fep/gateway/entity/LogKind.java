package com.syscom.fep.gateway.entity;

import org.apache.commons.lang3.StringUtils;

public enum LogKind {
    tx("transaction"),
    rsm("RSM"),
    disconn("disconnect"),
    txcurrent("transaction");

    private String logName;

    private LogKind(String logName) {
        this.logName = logName;
    }

    public String getLogName() {
        return logName;
    }

    public static String showInfo() {
        return StringUtils.join(values(), "/");
    }
}
