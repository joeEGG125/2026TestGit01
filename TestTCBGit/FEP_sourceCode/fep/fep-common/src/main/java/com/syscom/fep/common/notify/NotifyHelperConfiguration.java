package com.syscom.fep.common.notify;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.net.http.HttpClient2Config;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.NestedTestConfiguration;

@Configuration
@ConfigurationProperties(prefix = NotifyHelperConstant.CONFIGURATION_PROPERTIES_PREFIX)
@ConditionalOnProperty(prefix = NotifyHelperConstant.CONFIGURATION_PROPERTIES_PREFIX, name = {NotifyHelperConstant.CONFIGURATION_PROPERTIES_URL_SEND_NOTIFY, NotifyHelperConstant.CONFIGURATION_PROPERTIES_URL_LOG_NOTIFY})
@Lazy
// @RefreshScope
public class NotifyHelperConfiguration {
    /**
     * Notify服務提供的URL
     */
    private NotifyHelperConfigurationURL url;
    /**
     * Http Client設定
     */
    @NestedConfigurationProperty
    private final HttpClient2Config http = new HttpClient2Config();
    /**
     * 線程池核心數設定
     */
    private int executorCorePoolSize = 10;
    /**
     * 執行緒線程池中線程alive時間, 單位毫秒
     */
    private long executorKeepAliveTime = 0;
    /**
     * 執行緒線程池Queue Size
     */
    private int executorQueueCapacity = Integer.MAX_VALUE;

    public NotifyHelperConfigurationURL getUrl() {
        return url;
    }

    public void setUrl(NotifyHelperConfigurationURL url) {
        this.url = url;
    }

    public HttpClient2Config getHttp() {
        return http;
    }

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
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "NotifyHelper Configuration"));
    }
}
