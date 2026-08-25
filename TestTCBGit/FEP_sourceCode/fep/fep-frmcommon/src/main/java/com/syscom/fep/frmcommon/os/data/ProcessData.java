package com.syscom.fep.frmcommon.os.data;

import oshi.software.os.OSProcess.State;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ProcessData implements Serializable {
    private String name;
    private String path;
    private String commandLine;
    private List<String> arguments;
    private Map<String, String> environmentVariables;
    private String currentWorkingDirectory;
    private String user;
    private String userID;
    private String group;
    private String groupID;
    private State state;
    private int processID;
    private int parentProcessID;
    private int threadCount;
    private int priority;
    private long virtualSize;
    private long residentSetSize;
    private long kernelTime;
    private long userTime;
    private long upTime;
    private long startTime;
    private long bytesRead;
    private long bytesWritten;
    private long openFiles;
    private long softOpenFileLimit;
    private long hardOpenFileLimit;
    private double processCpuLoadCumulative;
    private int bitness;
    private long affinityMask;
    private boolean updateAttributes;
    private long minorFaults;
    private long majorFaults;
    private long contextSwitches;
    private double cpuPercent;
    private double memPercent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUpdateAttributes() {
        return updateAttributes;
    }

    public void setUpdateAttributes(boolean updateAttributes) {
        this.updateAttributes = updateAttributes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public String getCurrentWorkingDirectory() {
        return currentWorkingDirectory;
    }

    public void setCurrentWorkingDirectory(String currentWorkingDirectory) {
        this.currentWorkingDirectory = currentWorkingDirectory;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public int getParentProcessID() {
        return parentProcessID;
    }

    public void setParentProcessID(int parentProcessID) {
        this.parentProcessID = parentProcessID;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(long virtualSize) {
        this.virtualSize = virtualSize;
    }

    public long getResidentSetSize() {
        return residentSetSize;
    }

    public void setResidentSetSize(long residentSetSize) {
        this.residentSetSize = residentSetSize;
    }

    public long getKernelTime() {
        return kernelTime;
    }

    public void setKernelTime(long kernelTime) {
        this.kernelTime = kernelTime;
    }

    public long getUserTime() {
        return userTime;
    }

    public void setUserTime(long userTime) {
        this.userTime = userTime;
    }

    public long getUpTime() {
        return upTime;
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public void setBytesWritten(long bytesWritten) {
        this.bytesWritten = bytesWritten;
    }

    public long getOpenFiles() {
        return openFiles;
    }

    public void setOpenFiles(long openFiles) {
        this.openFiles = openFiles;
    }

    public long getSoftOpenFileLimit() {
        return softOpenFileLimit;
    }

    public void setSoftOpenFileLimit(long softOpenFileLimit) {
        this.softOpenFileLimit = softOpenFileLimit;
    }

    public long getHardOpenFileLimit() {
        return hardOpenFileLimit;
    }

    public void setHardOpenFileLimit(long hardOpenFileLimit) {
        this.hardOpenFileLimit = hardOpenFileLimit;
    }

    public double getProcessCpuLoadCumulative() {
        return processCpuLoadCumulative;
    }

    public void setProcessCpuLoadCumulative(double processCpuLoadCumulative) {
        this.processCpuLoadCumulative = processCpuLoadCumulative;
    }

    public int getBitness() {
        return bitness;
    }

    public void setBitness(int bitness) {
        this.bitness = bitness;
    }

    public long getAffinityMask() {
        return affinityMask;
    }

    public void setAffinityMask(long affinityMask) {
        this.affinityMask = affinityMask;
    }

    public long getMinorFaults() {
        return minorFaults;
    }

    public void setMinorFaults(long minorFaults) {
        this.minorFaults = minorFaults;
    }

    public long getMajorFaults() {
        return majorFaults;
    }

    public void setMajorFaults(long majorFaults) {
        this.majorFaults = majorFaults;
    }

    public long getContextSwitches() {
        return contextSwitches;
    }

    public void setContextSwitches(long contextSwitches) {
        this.contextSwitches = contextSwitches;
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
}
