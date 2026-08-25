package com.syscom.fep.server.controller.configuration;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.fep.server.atm")
public class ATMConfiguration {
    // 執行緒線程池固定大小
    private int executorCorePoolSize = 50;
    // 執行緒線程池中線程alive時間, 單位毫秒
    private long executorKeepAliveTime = 0;
    // 執行緒線程池Queue Size
    private int executorQueueCapacity = Integer.MAX_VALUE;
    // 批次寫入table資料筆數
    private int flushStatementsTotal = 100;
    // 處理交易類電文是否按照異步處理
    private boolean txProcessAsync = true;

    public int getExecutorCorePoolSize() {
        return executorCorePoolSize;
    }

    public void setExecutorCorePoolSize(int executorCorePoolSize) {
        this.executorCorePoolSize = executorCorePoolSize;
    }

    public long getExecutorKeepAliveTime() {
        return executorKeepAliveTime;
    }

    public void setExecutorKeepAliveTime(long executorKeepAliveTime) {
        this.executorKeepAliveTime = executorKeepAliveTime;
    }

    public int getExecutorQueueCapacity() {
        return executorQueueCapacity;
    }

    public void setExecutorQueueCapacity(int executorQueueCapacity) {
        this.executorQueueCapacity = executorQueueCapacity;
    }

    public int getFlushStatementsTotal() {
        return flushStatementsTotal;
    }

    public void setFlushStatementsTotal(int flushStatementsTotal) {
        this.flushStatementsTotal = flushStatementsTotal;
    }

    public boolean isTxProcessAsync() {
        return txProcessAsync;
    }

    public void setTxProcessAsync(boolean txProcessAsync) {
        this.txProcessAsync = txProcessAsync;
    }

    @PostConstruct
    public void postConstruct() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "ATM Service Configuration"));
    }
}
