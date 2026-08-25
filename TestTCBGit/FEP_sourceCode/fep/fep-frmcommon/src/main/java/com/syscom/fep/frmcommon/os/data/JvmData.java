package com.syscom.fep.frmcommon.os.data;

public class JvmData {
    /**
     * 最大的記憶體
     */
    private long memoryMax;
    /**
     * 已分配的記憶體
     */
    private long memoryCommitted;
    /**
     * 已分配的記憶體中剩餘的部分
     */
    private long memoryFree;
    /**
     * 已使用的記憶體
     */
    private long memoryUsed;
    /**
     * 可用最大記憶體大小
     */
    private long memoryAvailable;
    /**
     * CPU負載
     */
    private double cpuUsage;

    public long getMemoryMax() {
        return memoryMax;
    }

    public void setMemoryMax(long memoryMax) {
        this.memoryMax = memoryMax;
    }

    public long getMemoryCommitted() {
        return memoryCommitted;
    }

    public void setMemoryCommitted(long memoryCommitted) {
        this.memoryCommitted = memoryCommitted;
    }

    public long getMemoryFree() {
        return memoryFree;
    }

    public void setMemoryFree(long memoryFree) {
        this.memoryFree = memoryFree;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public long getMemoryAvailable() {
        return memoryAvailable;
    }

    public void setMemoryAvailable(long memoryAvailable) {
        this.memoryAvailable = memoryAvailable;
    }
}
