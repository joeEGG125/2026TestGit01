package com.syscom.fep.gateway.netty.fisc.client.sender;

import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.fisc.client.FISCGatewayClientConfiguration;

import jakarta.annotation.PostConstruct;

// @ConfigurationProperties(prefix = "spring.fep.gateway.transmission.fisc.sender")
// @RefreshScope
public class FISCGatewayClientSenderConfiguration extends FISCGatewayClientConfiguration {

    /**
     * 專門用於非SpringBean模式下初始化
     */
    @Override
    public void initialization() {
        super.initialization();
        this.initSender();
    }

    @PostConstruct
    public void initSender() {
        super.setSocketType(SocketType.Sender);
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
