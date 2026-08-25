package com.syscom.fep.frmcommon.os.data;

public class DBPoolData {
    /**
     * pool name
     */
    private String name;
    /**
     * the current number of idle connections in the pool
     */
    private int idleConnections;
    /**
     * the current number of active (in-use) connections in the pool
     */
    private int activeConnections;
    /**
     * the total number of connections in the pool
     */
    private int totalConnections;
    /**
     * the number of threads awaiting a connection from the pool
     */
    private int threadsAwaitingConnection;
    /**
     * The property controls the minimum number of idle connections
     */
    private int minimumIdle;
    /**
     * The property controls the maximum size that the pool is allowed to reach
     */
    private int maximumPoolSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIdleConnections() {
        return idleConnections;
    }

    public void setIdleConnections(int idleConnections) {
        this.idleConnections = idleConnections;
    }

    public int getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(int activeConnections) {
        this.activeConnections = activeConnections;
    }

    public int getTotalConnections() {
        return totalConnections;
    }

    public void setTotalConnections(int totalConnections) {
        this.totalConnections = totalConnections;
    }

    public int getThreadsAwaitingConnection() {
        return threadsAwaitingConnection;
    }

    public void setThreadsAwaitingConnection(int threadsAwaitingConnection) {
        this.threadsAwaitingConnection = threadsAwaitingConnection;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }
}
