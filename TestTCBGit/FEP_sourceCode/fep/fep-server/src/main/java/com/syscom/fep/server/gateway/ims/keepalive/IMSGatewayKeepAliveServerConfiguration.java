package com.syscom.fep.server.gateway.ims.keepalive;

import com.syscom.fep.invoker.netty.SimpleNettyServerConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.server.gateway.ims.keepalive.server")
// @RefreshScope
public class IMSGatewayKeepAliveServerConfiguration extends SimpleNettyServerConfiguration {
    /**
     * 是否記錄log
     */
    private boolean logging = false;

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }
}
