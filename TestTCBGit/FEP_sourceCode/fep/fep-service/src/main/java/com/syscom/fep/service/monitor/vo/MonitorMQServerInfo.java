package com.syscom.fep.service.monitor.vo;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class MonitorMQServerInfo extends MonitorServerInfo {
    @NotNull
    private String queueManagerName;
    @NotNull
    private String channel;
    @NotNull
    private String userID;
    @NotNull
    private String sscode;
    @NestedConfigurationProperty
    private List<MonitorMQNameInfo> queueNames = new ArrayList<>();

    public String getQueueManagerName() {
        return queueManagerName;
    }

    public void setQueueManagerName(String queueManagerName) {
        this.queueManagerName = queueManagerName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSscode() {
        return sscode;
    }

    public void setSscode(String sscode) {
        this.sscode = sscode;
    }

    public List<MonitorMQNameInfo> getQueueNames() {
        return queueNames;
    }
}
