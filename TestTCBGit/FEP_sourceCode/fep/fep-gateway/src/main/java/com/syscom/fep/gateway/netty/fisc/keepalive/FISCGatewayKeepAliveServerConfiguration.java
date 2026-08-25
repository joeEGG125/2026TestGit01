package com.syscom.fep.gateway.netty.fisc.keepalive;

import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionServerConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import jakarta.annotation.PostConstruct;

@ConfigurationProperties(prefix = "spring.fep.gateway.transmission.fisc.keepalive.server")
// @RefreshScope
public class FISCGatewayKeepAliveServerConfiguration extends NettyTransmissionServerConfiguration {
    /**
     * 是否記錄log
     */
    private boolean logging = false;

    @PostConstruct
    public void init() {
        super.setSocketType(SocketType.Server);
        super.setGateway(Gateway.FISCGW);
    }

    /**
     * @return
     */
    @Override
    public final Gateway getGateway() {
        return super.getGateway();
    }

    /**
     * @param gateway
     */
    @Override
    public final void setGateway(Gateway gateway) {}

    /**
     * @return
     */
    @Override
    public final SocketType getSocketType() {
        return super.getSocketType();
    }

    /**
     * @param socketType
     */
    @Override
    public final void setSocketType(SocketType socketType) {}

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }
}
