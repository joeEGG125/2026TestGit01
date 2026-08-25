package com.syscom.fep.server.controller.configuration;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.fep.server.ims")
public class IMSConfiguration {
    // 執行緒線程池固定大小
    private int executorCorePoolSize = 50;
    // 執行緒線程池中線程alive時間, 單位毫秒
    private long executorKeepAliveTime = 0;
    // 執行緒線程池Queue Size
    private int executorQueueCapacity = Integer.MAX_VALUE;

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

    @PostConstruct
    public void postConstruct() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "IMS Service Configuration"));
    }
}
