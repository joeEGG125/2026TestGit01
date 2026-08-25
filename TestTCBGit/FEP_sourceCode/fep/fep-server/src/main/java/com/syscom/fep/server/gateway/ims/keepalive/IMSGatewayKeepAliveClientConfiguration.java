package com.syscom.fep.server.gateway.ims.keepalive;

import com.syscom.fep.invoker.netty.SimpleNettyClientConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.server.gateway.ims.keepalive.client")
// @RefreshScope
public class IMSGatewayKeepAliveClientConfiguration extends SimpleNettyClientConfiguration {
    /**
     * 啟動後經過多久(單位為秒)開始發第一道keepalive電文,若未設代表不做keepalive功能
     */
    private int start = 2;
    /**
     * 每隔多久發一次keepAlive,單位為秒
     */
    private int interval = 10;
    /**
     * 當keepAlive無回應時retry次數
     */
    private int retryCount = 3;
    /**
     * 當keepAlive無回應時每次retry的間隔秒數
     */
    private int retryInterval = 5;
    /**
     * 多長時間沒有收到keepalive回應,單位為秒
     */
    private int timeout = 5;
    /**
     * 是否列印log
     */
    private boolean logging = false;
    /**
     * 發送KeepAlive到另外一台的http url
     */
    private String httpKeepAlive;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public String getHttpKeepAlive() {
        return httpKeepAlive;
    }

    public void setHttpKeepAlive(String httpKeepAlive) {
        this.httpKeepAlive = httpKeepAlive;
    }
}
