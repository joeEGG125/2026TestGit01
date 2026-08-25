package com.syscom.fep.gateway.netty.fisc.server;

import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionServerConfiguration;

import jakarta.annotation.PostConstruct;

// @ConfigurationProperties(prefix = "spring.fep.gateway.transmission.fisc.server")
// @RefreshScope
public class FISCGatewayServerConfiguration extends NettyTransmissionServerConfiguration {

    /**
     * 專門用於非SpringBean模式下初始化
     */
    @Override
    public void initialization() {
        super.initialization();
        this.init();
    }

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
    public final void setGateway(Gateway gateway) {
    }

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
    public final void setSocketType(SocketType socketType) {
    }
}
