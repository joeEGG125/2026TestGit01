package com.syscom.fep.frmcommon.os.data;

import java.io.Serializable;

public class MemoryData implements Serializable {
    private long total;
    private long available;
    private long used;
    private double usedPercent;
    private double availablePercent;
    private long pageSize;
    private long swapTotal;
    private long swapUsed;
    private long virtualMax;
    private long virtualInUse;
    private long swapPagesIn;
    private long swapPagesOut;
    private double virtualPercent;
    private double swapPercent;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getAvailable() {
        return available;
    }

    public void setAvailable(long available) {
        this.available = available;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public double getUsedPercent() {
        return usedPercent;
    }

    public void setUsedPercent(double usedPercent) {
        this.usedPercent = usedPercent;
    }

    public double getAvailablePercent() {
        return availablePercent;
    }

    public void setAvailablePercent(double availablePercent) {
        this.availablePercent = availablePercent;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getSwapTotal() {
        return swapTotal;
    }

    public void setSwapTotal(long swapTotal) {
        this.swapTotal = swapTotal;
    }

    public long getSwapUsed() {
        return swapUsed;
    }

    public void setSwapUsed(long swapUsed) {
        this.swapUsed = swapUsed;
    }

    public long getVirtualMax() {
        return virtualMax;
    }

    public void setVirtualMax(long virtualMax) {
        this.virtualMax = virtualMax;
    }

    public long getVirtualInUse() {
        return virtualInUse;
    }

    public void setVirtualInUse(long virtualInUse) {
        this.virtualInUse = virtualInUse;
    }

    public long getSwapPagesIn() {
        return swapPagesIn;
    }

    public void setSwapPagesIn(long swapPagesIn) {
        this.swapPagesIn = swapPagesIn;
    }

    public long getSwapPagesOut() {
        return swapPagesOut;
    }

    public void setSwapPagesOut(long swapPagesOut) {
        this.swapPagesOut = swapPagesOut;
    }

    public double getVirtualPercent() {
        return virtualPercent;
    }

    public void setVirtualPercent(double virtualPercent) {
        this.virtualPercent = virtualPercent;
    }

    public double getSwapPercent() {
        return swapPercent;
    }

    public void setSwapPercent(double swapPercent) {
        this.swapPercent = swapPercent;
    }
}
