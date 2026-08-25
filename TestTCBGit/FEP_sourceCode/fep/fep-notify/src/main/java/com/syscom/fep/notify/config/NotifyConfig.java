package com.syscom.fep.notify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.fep.notify")
public class NotifyConfig {
    private int retryCount;
    private long retrySleep;
    private String sendMiddlePlatForm_URL;

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getRetrySleep() {
        return retrySleep;
    }

    public void setRetrySleep(long retrySleep) {
        this.retrySleep = retrySleep;
    }

    public String getSendMiddlePlatForm_URL() {
        return sendMiddlePlatForm_URL;
    }

    public void setSendMiddlePlatForm_URL(String sendMiddlePlatForm_URL) {
        this.sendMiddlePlatForm_URL = sendMiddlePlatForm_URL;
    }

}
