package com.syscom.fep.frmcommon.os.ps;

import com.syscom.fep.frmcommon.log.LogHelper;
import oshi.util.ParseUtil;

/**
 * 執行ps aux之後返回的結果
 */
public class PsAuxData implements PsConstant {
    /**
     * 用戶
     */
    private String user;
    /**
     * 進程id
     */
    private int pid;
    /**
     * cpu占用率
     */
    private double cpuPercent;
    /**
     * 內存占用率
     */
    private double memPercent;
    /**
     * 虛擬內存
     */
    private long vsz;
    /**
     * 物理內存
     */
    private long rss;
    /**
     * （所有標誌）進程的控制終端：
     * -
     * 進程與終端無關。
     * ?
     * 未知。
     * 編號
     * TTY 號。 例如，條目 2 指示 TTY2。
     */
    private String tty;
    /**
     * 進程的狀態
     */
    private String stat;
    /**
     * 進程的起始時間
     */
    private String start;
    /**
     * 進程的總時間
     */
    private String time;
    /**
     * 進程的命令行
     */
    private String command;

    public PsAuxData(String[] fields) throws Exception {
        try {
            this.user = fields[AUX_DATA_FIELD_USER];
            this.pid = ParseUtil.parseIntOrDefault(fields[AUX_DATA_FIELD_PID], 0);
            this.cpuPercent = ParseUtil.parseDoubleOrDefault(fields[AUX_DATA_FIELD_CPU], 0);
            this.memPercent = ParseUtil.parseDoubleOrDefault(fields[AUX_DATA_FIELD_MEM], 0);
            this.vsz = ParseUtil.parseLongOrDefault(fields[AUX_DATA_FIELD_VSZ], 0);
            this.rss = ParseUtil.parseLongOrDefault(fields[AUX_DATA_FIELD_RSS], 0);
            this.tty = fields[AUX_DATA_FIELD_TTY];
            this.stat = fields[AUX_DATA_FIELD_STAT];
            this.start = fields[AUX_DATA_FIELD_START];
            this.time = fields[AUX_DATA_FIELD_TIME];
            this.command = fields[AUX_DATA_FIELD_COMMAND];
        } catch (IndexOutOfBoundsException e) {
            throw new Exception("Invalid fields size: " + fields.length, e);
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public double getCpuPercent() {
        return cpuPercent;
    }

    public void setCpuPercent(double cpuPercent) {
        this.cpuPercent = cpuPercent;
    }

    public double getMemPercent() {
        return memPercent;
    }

    public void setMemPercent(double memPercent) {
        this.memPercent = memPercent;
    }

    public long getVsz() {
        return vsz;
    }

    public void setVsz(long vsz) {
        this.vsz = vsz;
    }

    public long getRss() {
        return rss;
    }

    public void setRss(long rss) {
        this.rss = rss;
    }

    public String getTty() {
        return tty;
    }

    public void setTty(String tty) {
        this.tty = tty;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "PsAuxData{" +
                "user='" + user + '\'' +
                ", pid=" + pid +
                ", cpuPercent=" + cpuPercent +
                ", memPercent=" + memPercent +
                ", vsz=" + vsz +
                ", rss=" + rss +
                ", tty='" + tty + '\'' +
                ", stat='" + stat + '\'' +
                ", start='" + start + '\'' +
                ", time='" + time + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}