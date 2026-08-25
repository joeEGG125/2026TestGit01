package com.syscom.fep.vo.monitor;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 系統及服務監控Model
 *
 * @author ZK
 */
public class ServiceMonitoring implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 刷新時間
     */
    private int reNewTime;

    /**
     * 更新時間
     */
    private String updateTime;

    /**
     * 暫停發送通知
     */
    private boolean stopNotification = false;

    /**
     * 系統狀態
     */
    private List<SystemStatus> systemStatusList;

    /**
     * 服務狀態
     */
    private List<ServiceStatus> serviceStatusList = new ArrayList<>();

    /**
     * 網路狀態-SERVER
     */
    private List<ServerNetworkStatus> serverNetworkStatusList;

    /**
     * 網路狀態-CLIENT
     */
    private List<ClientNetworkStatus> clientNetworkStatusList;

    /**
     * 磁碟空間
     */
    private List<DiskSpace> diskSpaceList = new ArrayList<>();

    /**
     * MSMQ狀態
     */
    private List<MSMQStatus> msMQStatusList;

    /**
     * IBM MQ狀態
     */
    private List<IBMMQStatus> ibmMQStatusList;

    /**
     * IIS Host服務狀態
     */
    private List<IISHostServiceStatus> iisHostServiceStatusList;

    private List<IFEPTXN> ifepTXNList;
    /**
     * 啟用自動重啟
     */
    private boolean enableAutoRestart = true;

    /**
     * 是否顯示PID欄位
     */
    private boolean showPid = false;

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isStopNotification() {
        return stopNotification;
    }

    public void setStopNotification(boolean stopNotification) {
        this.stopNotification = stopNotification;
    }

    public List<SystemStatus> getSystemStatusList() {
        return systemStatusList;
    }

    public void setSystemStatusList(List<SystemStatus> systemStatusList) {
        this.systemStatusList = systemStatusList;
    }

    public List<ServiceStatus> getServiceStatusList() {
        return serviceStatusList;
    }

    public void setServiceStatusList(List<ServiceStatus> serviceStatusList) {
        this.serviceStatusList = serviceStatusList;
    }

    public List<ServerNetworkStatus> getServerNetworkStatusList() {
        return serverNetworkStatusList;
    }

    public void setServerNetworkStatusList(List<ServerNetworkStatus> serverNetworkStatusList) {
        this.serverNetworkStatusList = serverNetworkStatusList;
    }

    public List<ClientNetworkStatus> getClientNetworkStatusList() {
        return clientNetworkStatusList;
    }

    public void setClientNetworkStatusList(List<ClientNetworkStatus> clientNetworkStatusList) {
        this.clientNetworkStatusList = clientNetworkStatusList;
    }

    public List<DiskSpace> getDiskSpaceList() {
        return diskSpaceList;
    }

    public void setDiskSpaceList(List<DiskSpace> diskSpaceList) {
        this.diskSpaceList = diskSpaceList;
    }

    public List<MSMQStatus> getMsMQStatusList() {
        return msMQStatusList;
    }

    public void setMsMQStatusList(List<MSMQStatus> msMQStatusList) {
        this.msMQStatusList = msMQStatusList;
    }

    public List<IBMMQStatus> getIbmMQStatusList() {
        return ibmMQStatusList;
    }

    public void setIbmMQStatusList(List<IBMMQStatus> ibmMQStatusList) {
        this.ibmMQStatusList = ibmMQStatusList;
    }

    public List<IISHostServiceStatus> getIisHostServiceStatusList() {
        return iisHostServiceStatusList;
    }

    public void setIisHostServiceStatusList(List<IISHostServiceStatus> iisHostServiceStatusList) {
        this.iisHostServiceStatusList = iisHostServiceStatusList;
    }

    public List<IFEPTXN> getIfepTXNList() {
        return ifepTXNList;
    }

    public void setIfepTXNList(List<IFEPTXN> ifepTXNList) {
        this.ifepTXNList = ifepTXNList;
    }

    public int getReNewTime() {
        return reNewTime;
    }

    public void setReNewTime(int reNewTime) {
        this.reNewTime = reNewTime;
    }

    public boolean isEnableAutoRestart() {
        return enableAutoRestart;
    }

    public void setEnableAutoRestart(boolean enableAutoRestart) {
        this.enableAutoRestart = enableAutoRestart;
    }

    public boolean isShowPid() {
        return showPid;
    }

    public void setShowPid(boolean showPid) {
        this.showPid = showPid;
    }
}
