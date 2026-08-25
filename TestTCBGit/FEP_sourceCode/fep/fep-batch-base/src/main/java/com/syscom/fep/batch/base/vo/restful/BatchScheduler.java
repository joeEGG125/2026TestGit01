package com.syscom.fep.batch.base.vo.restful;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.quartz.Trigger.TriggerState;

import java.io.Serializable;
import java.util.Date;

@XStreamAlias("BatchScheduler")
public class BatchScheduler implements Serializable {
    @XStreamAlias("BatchId")
    private String batchId;
    @XStreamAlias("TriggerState")
    private TriggerState triggerState = TriggerState.NONE;
    @XStreamAlias("NextFireTime")
    private Date nextFireTime;
    @XStreamAlias("HostName")
    private String hostName;

    public BatchScheduler() {}

    public BatchScheduler(String batchId, String hostName) {
        this.batchId = batchId;
        this.hostName = hostName;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public TriggerState getTriggerState() {
        return triggerState;
    }

    public void setTriggerState(TriggerState triggerState) {
        this.triggerState = triggerState;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
