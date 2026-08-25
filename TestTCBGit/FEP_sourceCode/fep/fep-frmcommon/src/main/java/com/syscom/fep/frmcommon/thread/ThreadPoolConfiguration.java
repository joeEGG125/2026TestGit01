package com.syscom.fep.frmcommon.thread;

public class ThreadPoolConfiguration {
    /**
     * 線程名稱
     */
    private String threadNamePrefix = "thread-pool-";
    /**
     * 線程池維護線程的最少數量，即使沒有任務需要執行，也會一直存活
     */
    private int corePoolSize = 10;
    /**
     * 線程池維護線程的最大數量
     */
    private int maxPoolSize = 20;
    /**
     * 緩存隊列（阻塞隊列）當核心線程數達到最大時，新任務會放在隊列中排隊等待執行
     */
    private int queueCapacity = 50;
    /**
     * 允許的空閑時間，當線程空閑時間達到keepAliveTime時，線程會退出，直到線程數量=corePoolSize
     */
    private int keepAlive = 60;
    /**
     * 等待所有任務結束後在關閉線程池
     */
    private boolean waitForJobsToCompleteOnShutdown = true;

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isWaitForJobsToCompleteOnShutdown() {
        return waitForJobsToCompleteOnShutdown;
    }

    public void setWaitForJobsToCompleteOnShutdown(boolean waitForJobsToCompleteOnShutdown) {
        this.waitForJobsToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
    }
}
