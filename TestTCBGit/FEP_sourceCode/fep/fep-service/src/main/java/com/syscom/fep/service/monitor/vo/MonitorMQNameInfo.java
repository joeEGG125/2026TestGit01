package com.syscom.fep.service.monitor.vo;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

// @Validated
public class MonitorMQNameInfo {
    @NotNull
    private String name;
    @NotNull
    private String type;
    private int queueMax = 1000; // Queue訊息數量最大值, 如果超過這個值則需要通知, 設為0則不通知

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQueueMax() {
        return queueMax;
    }

    public void setQueueMax(int queueMax) {
        this.queueMax = queueMax;
    }
}
