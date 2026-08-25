package com.syscom.fep.common.mail;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.mail.MailProperties;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

// @Validated
@Configuration
@ConfigurationProperties(prefix = "spring.fep.mail")
@ConditionalOnProperty(prefix = "spring.fep.mail", name = "enable", havingValue = "true")
@Lazy
// @RefreshScope
public class MailConfiguration extends MailProperties {
    /**
     * 異步發送mail線程池核心數設定
     */
    private int executorCorePoolSize = 10;
    /**
     * 異步發送mail執行緒線程池中線程alive時間, 單位毫秒
     */
    private long executorKeepAliveTime = 0;
    /**
     * 異步發送mail執行緒線程池Queue Size
     */
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
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Mail Configuration"));
    }
}
