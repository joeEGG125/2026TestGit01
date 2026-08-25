package com.syscom.fep.server.gateway.pos;

import com.syscom.fep.invoker.netty.SimpleNettyServerConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.server.gateway.pos")
// @RefreshScope
public class PosGatewayConfiguration extends SimpleNettyServerConfiguration {
    public static final String BYPASSCHECKCLIENTIP_ALL = "ALL";
    /**
     * 過濾掉不想看的連線的LOG
     */
    private String byPassCheckClientIp;

    public String getByPassCheckClientIp() {
        return byPassCheckClientIp;
    }

    public void setByPassCheckClientIp(String byPassCheckClientIp) {
        this.byPassCheckClientIp = byPassCheckClientIp;
    }
}