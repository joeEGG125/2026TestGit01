package com.syscom.fep.frmcommon.os.data;

import java.io.Serializable;

public class CpuData implements Serializable {
    private int logicalProcessorCount;
    private int physicalProcessorCount;
    private double systemUsage;
    private double userUsage;
    private double ioWait;
    private double idle;

    public int getLogicalProcessorCount() {
        return logicalProcessorCount;
    }

    public void setLogicalProcessorCount(int logicalProcessorCount) {
        this.logicalProcessorCount = logicalProcessorCount;
    }

    public int getPhysicalProcessorCount() {
        return physicalProcessorCount;
    }

    public void setPhysicalProcessorCount(int physicalProcessorCount) {
        this.physicalProcessorCount = physicalProcessorCount;
    }

    public double getSystemUsage() {
        return systemUsage;
    }

    public void setSystemUsage(double systemUsage) {
        this.systemUsage = systemUsage;
    }

    public double getUserUsage() {
        return userUsage;
    }

    public void setUserUsage(double userUsage) {
        this.userUsage = userUsage;
    }

    public double getIoWait() {
        return ioWait;
    }

    public void setIoWait(double ioWait) {
        this.ioWait = ioWait;
    }

    public double getIdle() {
        return idle;
    }

    public void setIdle(double idle) {
        this.idle = idle;
    }
}
