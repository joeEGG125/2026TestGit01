package com.syscom.fep.common.monitor;

import java.util.Date;

/**
 * 監控數據系統
 */
public class MonitorData {
    private String smsServicename;
    private String smsServiceip;
    private String smsHostname;
    private Date smsUpdatetime;
    private String smsServicestate;
    private Date smsStarttime;
    private Integer smsPid;
    private Integer smsCpu;
    private Integer smsCpuThreshold;
    private Integer smsRam;
    private Integer smsRamThreshold;
    private Integer smsThreads;
    private Integer smsThreadsActive;
    private Integer smsThreadsThreshold;
    private Date smsStoptime;
    private String smsOthers;

    public String getSmsServicename() {
        return smsServicename;
    }

    public void setSmsServicename(String smsServicename) {
        this.smsServicename = smsServicename;
    }

    public String getSmsServiceip() {
        return smsServiceip;
    }

    public void setSmsServiceip(String smsServiceip) {
        this.smsServiceip = smsServiceip;
    }

    public String getSmsHostname() {
        return smsHostname;
    }

    public void setSmsHostname(String smsHostname) {
        this.smsHostname = smsHostname;
    }

    public Date getSmsUpdatetime() {
        return smsUpdatetime;
    }

    public void setSmsUpdatetime(Date smsUpdatetime) {
        this.smsUpdatetime = smsUpdatetime;
    }

    public String getSmsServicestate() {
        return smsServicestate;
    }

    public void setSmsServicestate(String smsServicestate) {
        this.smsServicestate = smsServicestate;
    }

    public Date getSmsStarttime() {
        return smsStarttime;
    }

    public void setSmsStarttime(Date smsStarttime) {
        this.smsStarttime = smsStarttime;
    }

    public Integer getSmsPid() {
        return smsPid;
    }

    public void setSmsPid(Integer smsPid) {
        this.smsPid = smsPid;
    }

    public Integer getSmsCpu() {
        return smsCpu;
    }

    public void setSmsCpu(Integer smsCpu) {
        this.smsCpu = smsCpu;
    }

    public Integer getSmsCpuThreshold() {
        return smsCpuThreshold;
    }

    public void setSmsCpuThreshold(Integer smsCpuThreshold) {
        this.smsCpuThreshold = smsCpuThreshold;
    }

    public Integer getSmsRam() {
        return smsRam;
    }

    public void setSmsRam(Integer smsRam) {
        this.smsRam = smsRam;
    }

    public Integer getSmsRamThreshold() {
        return smsRamThreshold;
    }

    public void setSmsRamThreshold(Integer smsRamThreshold) {
        this.smsRamThreshold = smsRamThreshold;
    }

    public Integer getSmsThreads() {
        return smsThreads;
    }

    public void setSmsThreads(Integer smsThreads) {
        this.smsThreads = smsThreads;
    }

    public Integer getSmsThreadsActive() {
        return smsThreadsActive;
    }

    public void setSmsThreadsActive(Integer smsThreadsActive) {
        this.smsThreadsActive = smsThreadsActive;
    }

    public Integer getSmsThreadsThreshold() {
        return smsThreadsThreshold;
    }

    public void setSmsThreadsThreshold(Integer smsThreadsThreshold) {
        this.smsThreadsThreshold = smsThreadsThreshold;
    }

    public Date getSmsStoptime() {
        return smsStoptime;
    }

    public void setSmsStoptime(Date smsStoptime) {
        this.smsStoptime = smsStoptime;
    }

    public String getSmsOthers() {
        return smsOthers;
    }

    public void setSmsOthers(String smsOthers) {
        this.smsOthers = smsOthers;
    }
}