package com.syscom.fep.gateway.netty.sample.client;

import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionClientConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import jakarta.annotation.PostConstruct;

@ConfigurationProperties(prefix = "spring.fep.gateway.transmission.sample.client")
// @RefreshScope
public class SampleGatewayClientConfiguration extends NettyTransmissionClientConfiguration {

    @PostConstruct
    public void initCBSGatewayClient() {
        super.setSocketType(SocketType.Client);
        super.setGateway(Gateway.SAMPLEGW);
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
}
