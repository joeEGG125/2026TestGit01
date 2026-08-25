package com.syscom.fep.vo.monitor;


/**
 * 系統狀態
 *
 * @author ZK
 *
 */
public class SystemStatus {
    /**
     * 服務主機名稱
     */
    private String sysHostname;

    /**
     * 服務主機IP
     */
    private String sysServiceIP;

    /**
     * CPU
     */
    private String sysCpu;

    /**
     * RAM
     */
    private String sysRam;

    /**
     * 使用中連接埠
     */
    private String sysUserport;

    /**
     * FEPDB
     */
    private String sysFEPDB;

    /**
     * EMSDB
     */
    private String sysEMSDB;

    /**
     * ENCDB
     */
    private String sysENCDB;

    /**
     * ENCLOG
     */
    private String sysENCLOGDB;

    /**
     * FEPHIS
     */
    private String sysFEPHIS;

    private String sysUpdateTime;

    public String getSysHostname() {
        return sysHostname;
    }

    public void setSysHostname(String sysHostname) {
        this.sysHostname = sysHostname;
    }

    public String getSysServiceIP() {
        return sysServiceIP;
    }

    public void setSysServiceIP(String sysServiceIP) {
        this.sysServiceIP = sysServiceIP;
    }

    public String getSysCpu() {
        return sysCpu;
    }

    public void setSysCpu(String sysCpu) {
        this.sysCpu = sysCpu;
    }

    public String getSysRam() {
        return sysRam;
    }

    public void setSysRam(String sysRam) {
        this.sysRam = sysRam;
    }

    public String getSysUserport() {
        return sysUserport;
    }

    public void setSysUserport(String sysUserport) {
        this.sysUserport = sysUserport;
    }

    public String getSysFEPDB() {
        return sysFEPDB;
    }

    public void setSysFEPDB(String sysFEPDB) {
        this.sysFEPDB = sysFEPDB;
    }

    public String getSysEMSDB() {
        return sysEMSDB;
    }

    public void setSysEMSDB(String sysEMSDB) {
        this.sysEMSDB = sysEMSDB;
    }

    public String getSysENCDB() {
        return sysENCDB;
    }

    public void setSysENCDB(String sysENCDB) {
        this.sysENCDB = sysENCDB;
    }

    public String getSysENCLOGDB() {
        return sysENCLOGDB;
    }

    public void setSysENCLOGDB(String sysENCLOGDB) {
        this.sysENCLOGDB = sysENCLOGDB;
    }

    public String getSysFEPHIS() {
        return sysFEPHIS;
    }

    public void setSysFEPHIS(String sysFEPHIS) {
        this.sysFEPHIS = sysFEPHIS;
    }

    public String getSysUpdateTime() {
        return sysUpdateTime;
    }

    public void setSysUpdateTime(String sysUpdateTime) {
        this.sysUpdateTime = sysUpdateTime;
    }
}
