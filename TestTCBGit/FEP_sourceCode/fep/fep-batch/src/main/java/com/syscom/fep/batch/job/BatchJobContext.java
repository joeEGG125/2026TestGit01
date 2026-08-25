package com.syscom.fep.batch.job;

import com.google.gson.Gson;
import com.syscom.fep.batch.base.enums.ScheduleType;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

public class BatchJobContext implements Serializable {
    private String hostName;
    private String batchId; // 對應quartz中JobKey和TriggerKey的name定義, 預設等於batchId
    private String group; // 對應quartz中JobKey和TriggerKey的group定義, 預設等於batchId
    private String name;
    private String description;
    private String action;
    private String actionArguments;
    private Date triggerStartDateTime;
    private Date nextFireDateTime;
    private Date latestExecutedStartDateTime;
    private Date latestExecutedEndDateTime;
    private String scheduleInfo;
    private ScheduleType scheduleType;
    private String instanceId;

    public BatchJobContext() {}

    public BatchJobContext(ScheduleType scheduleType, String hostName, String batchId, String taskName, String taskDescription, Date triggerStartTime, String action, String actionArguments) {
        this.scheduleType = scheduleType;
        this.hostName = hostName;
        this.batchId = batchId;
        this.group = batchId;
        this.name = taskName;
        this.description = taskDescription;
        this.triggerStartDateTime = triggerStartTime;
        this.action = action;
        this.actionArguments = actionArguments;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionArguments() {
        return actionArguments;
    }

    public void setActionArguments(String actionArguments) {
        this.actionArguments = actionArguments;
    }

    public Date getTriggerStartDateTime() {
        return triggerStartDateTime;
    }

    public void setTriggerStartDateTime(Date triggerStartDateTime) {
        this.triggerStartDateTime = triggerStartDateTime;
    }

    public Date getNextFireDateTime() {
        return nextFireDateTime;
    }

    public void setNextFireDateTime(Date nextFireDateTime) {
        this.nextFireDateTime = nextFireDateTime;
    }

    public Date getLatestExecutedStartDateTime() {
        return latestExecutedStartDateTime;
    }

    public void setLatestExecutedStartDateTime(Date latestExecutedStartDateTime) {
        this.latestExecutedStartDateTime = latestExecutedStartDateTime;
    }

    public Date getLatestExecutedEndDateTime() {
        return latestExecutedEndDateTime;
    }

    public void setLatestExecutedEndDateTime(Date latestExecutedEndDateTime) {
        this.latestExecutedEndDateTime = latestExecutedEndDateTime;
    }

    public String getScheduleInfo() {
        return scheduleInfo;
    }

    public void setScheduleInfo(String scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getLogContent() {
        return StringUtils.join(StringUtils.join(Arrays.asList(this.getBatchId(), this.getName(), this.getDescription()), "|"), "|");
    }

    /**
     * 是否同一天已經跑過批次
     *
     * @return
     */
    public boolean hasTriggeredInDays() {
        if (this.latestExecutedStartDateTime == null)
            return false;
        return CalendarUtil.dateValue(CalendarUtil.clone(this.latestExecutedStartDateTime)) == CalendarUtil.dateValue(Calendar.getInstance());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BatchJobContext)) return false;
        BatchJobContext that = (BatchJobContext) o;
        return Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(batchId);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public void putJobDataMap(Map<String, Object> map) {
        map.put(this.getClass().getName(), toString());
    }

    public static BatchJobContext getJobDataMap(Map<String, Object> map) {
        return fromString((String) map.get(BatchJobContext.class.getName()));
    }

    public static BatchJobContext fromString(String json) {
        if (StringUtils.isBlank(json))
            return null;
        return new Gson().fromJson(json, BatchJobContext.class);
    }
}
