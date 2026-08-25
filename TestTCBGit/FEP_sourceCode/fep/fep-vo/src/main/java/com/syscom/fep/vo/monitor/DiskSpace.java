package com.syscom.fep.vo.monitor;


import java.io.Serializable;

/**
 * 磁碟空間
 *
 * @author ZK
 *
 */
public class DiskSpace implements Serializable {
    private static final long serialVersionUID = 1L;
    private String serviceHostName;
    /**
     * 服務IP
     */
    private String serviceIP;

    /**
     * 磁碟代號
     */
    private String serviceName;

    /**
     * 磁碟總空間
     */
    private String totalDisk;

    /**
     * 磁碟已使用空間
     */
    private String useDisk;
    /**
     * 已使用比例
     */
    private String disk;

    /**
     * 字體顔色是否為紅色
     */
    private boolean isRed;

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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTotalDisk() {
        return totalDisk;
    }

    public void setTotalDisk(String totalDisk) {
        this.totalDisk = totalDisk;
    }

    public String getUseDisk() {
        return useDisk;
    }

    public void setUseDisk(String useDisk) {
        this.useDisk = useDisk;
    }

    public String getDisk() {
        return disk;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }

    public boolean isRed() {
        return isRed;
    }

    public void setRed(boolean red) {
        isRed = red;
    }
}
