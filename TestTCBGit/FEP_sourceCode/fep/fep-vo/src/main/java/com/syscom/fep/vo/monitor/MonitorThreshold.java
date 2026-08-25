package com.syscom.fep.vo.monitor;

public class MonitorThreshold {
    private boolean notification = true;
    private boolean monitorQueue = true;
    private int cpuThreshold = 90;
    private int ramThreshold = 1024;
    private int threadThreshold = 250;
    private int queueThreshold = 1000;
    private int exceedThresholdTimes = 1;

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public boolean isMonitorQueue() {
        return monitorQueue;
    }

    public void setMonitorQueue(boolean monitorQueue) {
        this.monitorQueue = monitorQueue;
    }

    public int getCpuThreshold() {
        return cpuThreshold;
    }

    public void setCpuThreshold(int cpuThreshold) {
        this.cpuThreshold = cpuThreshold;
    }

    public int getRamThreshold() {
        return ramThreshold;
    }

    public void setRamThreshold(int ramThreshold) {
        this.ramThreshold = ramThreshold;
    }

    public int getThreadThreshold() {
        return threadThreshold;
    }

    public void setThreadThreshold(int threadThreshold) {
        this.threadThreshold = threadThreshold;
    }

    public int getQueueThreshold() {
        return queueThreshold;
    }

    public void setQueueThreshold(int queueThreshold) {
        this.queueThreshold = queueThreshold;
    }

    public int getExceedThresholdTimes() {
        return exceedThresholdTimes;
    }

    public void setExceedThresholdTimes(int exceedThresholdTimes) {
        this.exceedThresholdTimes = exceedThresholdTimes;
    }
}
