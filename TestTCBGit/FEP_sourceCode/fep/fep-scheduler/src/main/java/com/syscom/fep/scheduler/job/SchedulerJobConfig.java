package com.syscom.fep.scheduler.job;

import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

// @Validated
public class SchedulerJobConfig {
    private String identity;
    @NotNull
    private String cronExpression;
    private boolean sendEMS = true;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isSendEMS() {
        return sendEMS;
    }

    public void setSendEMS(boolean sendEMS) {
        this.sendEMS = sendEMS;
    }
}