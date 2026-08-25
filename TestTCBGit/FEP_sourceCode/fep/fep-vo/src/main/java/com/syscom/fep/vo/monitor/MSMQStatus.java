package com.syscom.fep.vo.monitor;


import java.io.Serializable;

/**
 * MSMQ狀態
 *
 * @author ZK
 *
 */
public class MSMQStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 服務名稱
     */
    private String serviceName;

    /**
     * QUEUE路徑
     */
    private String queueName;

    /**
     * 啟用日誌
     */
    private String useJournal;

    /**
     * 目前訊息數
     */
    private String queueCount;

    /**
     * 日誌訊息數
     */
    private String journalCount;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getUseJournal() {
        return useJournal;
    }

    public void setUseJournal(String useJournal) {
        this.useJournal = useJournal;
    }

    public String getQueueCount() {
        return queueCount;
    }

    public void setQueueCount(String queueCount) {
        this.queueCount = queueCount;
    }

    public String getJournalCount() {
        return journalCount;
    }

    public void setJournalCount(String journalCount) {
        this.journalCount = journalCount;
    }
}
