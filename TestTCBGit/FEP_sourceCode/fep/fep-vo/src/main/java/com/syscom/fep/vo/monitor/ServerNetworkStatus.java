package com.syscom.fep.vo.monitor;

/**
 * 網路狀態-SERVER
 *
 * @author ZK
 */
public class ServerNetworkStatus extends ServiceStatus {
    /**
     * 使用連接埠
     */
    private String servicePort;
    /**
     * 目前連接數
     */
    private String socketCount;

    public String getServicePort() {
        return servicePort;
    }

    public void setServicePort(String servicePort) {
        this.servicePort = servicePort;
    }

    public String getSocketCount() {
        return socketCount;
    }

    public void setSocketCount(String socketCount) {
        this.socketCount = socketCount;
    }
}
