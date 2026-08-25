package com.syscom.fep.vo.monitor;


import java.io.Serializable;

/**
 * IIS Host服務狀態
 *
 * @author ZK
 *
 */
public class IISHostServiceStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主機
     */
    private String host;

    /**
     * 集區名稱
     */
    private String clusterName;

    /**
     * 服務名稱
     */
    private String serviceName;

    /**
     * 目前執行緒
     */
    private String currentThread;

    /**
     * 服務狀態
     */
    private String status;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCurrentThread() {
        return currentThread;
    }

    public void setCurrentThread(String currentThread) {
        this.currentThread = currentThread;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
