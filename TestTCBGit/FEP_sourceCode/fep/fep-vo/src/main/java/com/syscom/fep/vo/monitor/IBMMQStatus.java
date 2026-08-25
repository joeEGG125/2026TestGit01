package com.syscom.fep.vo.monitor;

import java.io.Serializable;

/**
 * IBM MQ狀態
 *
 * @author ZK
 */
public class IBMMQStatus implements Serializable {
    /**
     * 名稱
     */
    private String name;
    /**
     * HostName
     */
    private String serviceHostName;
    /**
     * MQSERVER IP
     */
    private String serviceIP;
    /**
     * 類型
     */
    private String objectType;
    /**
     * 目前訊息
     */
    private String queueCount;
    /**
     * 通道狀態
     */
    private String status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceHostName() {
        return serviceHostName;
    }

    public void setServiceHostName(String serviceHostName) {
        this.serviceHostName = serviceHostName;
    }

    public String getServiceIP() {
        return serviceIP;
    }

    public void setServiceIP(String serviceIP) {
        this.serviceIP = serviceIP;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getQueueCount() {
        return queueCount;
    }

    public void setQueueCount(String queueCount) {
        this.queueCount = queueCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
