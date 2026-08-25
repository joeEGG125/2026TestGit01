package com.syscom.fep.service.ems.vo;

import java.util.Calendar;

public class EMSErrData {
    private Calendar lastNotifyTime;
    private int count;
    private EMSLogMessage eventData;

    public Calendar getLastNotifyTime() {
        return lastNotifyTime;
    }

    public void setLastNotifyTime(Calendar lastNotifyTime) {
        this.lastNotifyTime = lastNotifyTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void accumulateCount() {
        this.accumulateCount(1);
    }

    public void accumulateCount(int accumulateCount) {
        this.count += accumulateCount;
    }

    public EMSLogMessage getEventData() {
        return eventData;
    }

    public void setEventData(EMSLogMessage eventData) {
        this.eventData = eventData;
    }
}
