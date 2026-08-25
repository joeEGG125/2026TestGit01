package com.syscom.fep.vo.service;

public class APIResult {
    private String status;
    private String errorMessage;
    private int currentThreads;
    private int currentRunning;
    private int currentSleepTime;
    private int currentState;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getCurrentThreads() {
        return currentThreads;
    }

    public void setCurrentThreads(int currentThreads) {
        this.currentThreads = currentThreads;
    }

    public int getCurrentRunning() {
        return currentRunning;
    }

    public void setCurrentRunning(int currentRunning) {
        this.currentRunning = currentRunning;
    }

    public int getCurrentSleepTime() {
        return currentSleepTime;
    }

    public void setCurrentSleepTime(int currentSleepTime) {
        this.currentSleepTime = currentSleepTime;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    @Override
    public String toString() {
        return "APIResult{" +
                "status='" + status + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", currentThreads=" + currentThreads +
                ", currentRunning=" + currentRunning +
                ", currentSleepTime=" + currentSleepTime +
                ", currentState=" + currentState +
                '}';
    }
}
