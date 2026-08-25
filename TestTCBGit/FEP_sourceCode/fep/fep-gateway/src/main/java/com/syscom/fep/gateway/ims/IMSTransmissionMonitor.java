package com.syscom.fep.gateway.ims;

import org.apache.commons.lang3.StringUtils;

public class IMSTransmissionMonitor<Configuration extends IMSTransmissionConfiguration> {
    private Configuration configuration;
    // 服務名稱
    private String serviceName;
    // 本機連接埠
    private String local = StringUtils.EMPTY;
    // 遠端連接埠
    private String remote = StringUtils.EMPTY;
    // 連線狀態
    private IMSTransmissionConnState imsTransmissionConnState = IMSTransmissionConnState.CLIENT_DISCONNECTED;
    // 目前連接數
    private long connections;
    // 連線時間
    private long connectedTime;
    // 斷線時間
    private long disconnectedTime;

    public void setTransmissionConfiguration(Configuration configuration) {
        this.configuration = configuration;
        this.serviceName = StringUtils.join(configuration.getGateway().name(),
                configuration.getSocketType() != null ? StringUtils.join("_", configuration.getSocketType().name()) : StringUtils.EMPTY);
        this.remote = StringUtils.join(configuration.getHost(), ":", configuration.getPort());
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public IMSTransmissionConnState getConnState() {
        return imsTransmissionConnState;
    }

    public void setConnState(IMSTransmissionConnState nettyTransmissionConnState) {
        this.imsTransmissionConnState = nettyTransmissionConnState;
    }

    public long getConnections() {
        return connections;
    }

    public void setConnections(long connections) {
        this.connections = connections;
    }

    public long getConnectedTime() {
        return connectedTime;
    }

    public void setConnectedTime(long connectedTime) {
        this.connectedTime = connectedTime;
    }

    public long getDisconnectedTime() {
        return disconnectedTime;
    }

    public void setDisconnectedTime(long disconnectedTime) {
        this.disconnectedTime = disconnectedTime;
    }
}
