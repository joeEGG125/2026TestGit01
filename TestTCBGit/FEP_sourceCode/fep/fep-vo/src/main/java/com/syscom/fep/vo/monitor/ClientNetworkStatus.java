package com.syscom.fep.vo.monitor;

/**
 * 網路狀態-CLIENT
 *
 * @author ZK
 */
public class ClientNetworkStatus extends ServiceStatus {
    /**
     * 本機連接埠
     */
    private String localEndPoint;
    /**
     * 遠端連接埠
     */
    private String remoteEndPoint;
    /**
     * 連線狀態
     */
    private String state;
    /**
     * 目前連接數
     */
    private String socketCount;
    /**
     * 最後連線時間
     */
    private long lastConnectedTime;
    /**
     * 最後斷線時間
     */
    private long lastDisconnectedTime;

    public String getLocalEndPoint() {
        return localEndPoint;
    }

    public void setLocalEndPoint(String localEndPoint) {
        this.localEndPoint = localEndPoint;
    }

    public String getRemoteEndPoint() {
        return remoteEndPoint;
    }

    public void setRemoteEndPoint(String remoteEndPoint) {
        this.remoteEndPoint = remoteEndPoint;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSocketCount() {
        return socketCount;
    }

    public void setSocketCount(String socketCount) {
        this.socketCount = socketCount;
    }

    public long getLastConnectedTime() {
        return lastConnectedTime;
    }

    public void setLastConnectedTime(long lastConnectedTime) {
        this.lastConnectedTime = lastConnectedTime;
    }

    public long getLastDisconnectedTime() {
        return lastDisconnectedTime;
    }

    public void setLastDisconnectedTime(long lastDisconnectedTime) {
        this.lastDisconnectedTime = lastDisconnectedTime;
    }
}
