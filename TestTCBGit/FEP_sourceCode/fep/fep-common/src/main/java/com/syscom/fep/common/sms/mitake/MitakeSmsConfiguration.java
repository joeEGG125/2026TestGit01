package com.syscom.fep.common.sms.mitake;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

// @Validated
@Configuration
@ConfigurationProperties(prefix = "spring.fep.sms.mitake")
@ConditionalOnProperty(prefix = "spring.fep.sms.mitake", name = "enable", havingValue = "true")
@Lazy
// @RefreshScope
public class MitakeSmsConfiguration {
    private String domain;
    private String username;
    private String sscode;
    private String charsetUrl = "UTF-8";
    /**
     * http建立連線超時時間, 單位毫秒
     */
    private int httpConnectTimeout = 30000;
    /**
     * http等待接收回應超時時間, 單位毫秒
     */
    private int httpReadTimeout = 60000;
    /**
     * 異步發送簡訊線程池核心數設定
     */
    private int executorCorePoolSize = 10;
    /**
     * 異步發送簡訊執行緒線程池中線程alive時間, 單位毫秒
     */
    private long executorKeepAliveTime = 0;
    /**
     * 異步發送簡訊執行緒線程池Queue Size
     */
    private int executorQueueCapacity = Integer.MAX_VALUE;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSscode() {
        return sscode;
    }

    public void setSscode(String sscode) {
        this.sscode = sscode;
    }

    public String getCharsetUrl() {
        return charsetUrl;
    }

    public void setCharsetUrl(String charsetUrl) {
        this.charsetUrl = charsetUrl;
    }

    public int getHttpConnectTimeout() {
        return httpConnectTimeout;
    }

    public void setHttpConnectTimeout(int httpConnectTimeout) {
        this.httpConnectTimeout = httpConnectTimeout;
    }

    public int getHttpReadTimeout() {
        return httpReadTimeout;
    }

    public void setHttpReadTimeout(int httpReadTimeout) {
        this.httpReadTimeout = httpReadTimeout;
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
    public void postConstruct() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Mitake Sms Configuration"));
    }
}
