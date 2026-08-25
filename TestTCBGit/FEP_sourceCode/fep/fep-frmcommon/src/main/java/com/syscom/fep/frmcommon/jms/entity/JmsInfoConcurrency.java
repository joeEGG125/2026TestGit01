package com.syscom.fep.frmcommon.jms.entity;

/**
 * JMS並發數相關信息
 *
 * @author Richard
 */
public class JmsInfoConcurrency extends JmsInfoBase {
    /**
     * 初始設定的並發數
     */
    private String initConcurrency;
    /**
     * 當前設定的並發數
     */
    private String currentConcurrency;
    /**
     * 根據並發數設定需要創建的Consumer數
     */
    private int concurrentConsumers;
    /**
     * 根據並發數設定需要創建的最大Consumer數
     */
    private int maxConcurrentConsumers;
    /**
     * 當前處於活動狀態的Consumer數
     */
    private int activeConsumerCount;
    /**
     * 當前暫停中的任務數量
     */
    private int pausedTaskCount;
    /**
     * 當前已經啟動的總Consumer數
     */
    private int scheduledConsumerCount;
    /**
     * 當前空閒的線程數
     */
    private int idleInvokerCount;
    /**
     * 當前執行中的線程數
     */
    private int activeInvokerCount;

    public String getInitConcurrency() {
        return initConcurrency;
    }

    public void setInitConcurrency(String initConcurrency) {
        this.initConcurrency = initConcurrency;
    }

    public String getCurrentConcurrency() {
        return currentConcurrency;
    }

    public void setCurrentConcurrency(String currentConcurrency) {
        this.currentConcurrency = currentConcurrency;
    }

    public int getConcurrentConsumers() {
        return concurrentConsumers;
    }

    public void setConcurrentConsumers(int concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
    }

    public int getMaxConcurrentConsumers() {
        return maxConcurrentConsumers;
    }

    public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
        this.maxConcurrentConsumers = maxConcurrentConsumers;
    }

    public int getActiveConsumerCount() {
        return activeConsumerCount;
    }

    public void setActiveConsumerCount(int activeConsumerCount) {
        this.activeConsumerCount = activeConsumerCount;
    }

    public int getPausedTaskCount() {
        return pausedTaskCount;
    }

    public void setPausedTaskCount(int pausedTaskCount) {
        this.pausedTaskCount = pausedTaskCount;
    }

    public int getScheduledConsumerCount() {
        return scheduledConsumerCount;
    }

    public void setScheduledConsumerCount(int scheduledConsumerCount) {
        this.scheduledConsumerCount = scheduledConsumerCount;
        this.setActiveInvokerCount();
    }

    public int getIdleInvokerCount() {
        return idleInvokerCount;
    }

    public void setIdleInvokerCount(int idleInvokerCount) {
        this.idleInvokerCount = idleInvokerCount;
        this.setActiveInvokerCount();
    }

    public int getActiveInvokerCount() {
        return activeInvokerCount;
    }

    private void setActiveInvokerCount() {
        this.activeInvokerCount = this.scheduledConsumerCount - this.idleInvokerCount;
        if (this.activeInvokerCount < 0)
            this.activeInvokerCount = 0;
    }
}
