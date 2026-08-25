package com.syscom.fep.gateway.netty.sample.client;

import com.syscom.fep.frmcommon.netty.NettyEventExecutorDataController;
import com.syscom.fep.gateway.netty.NettyTransmissionClient;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import io.netty.channel.Channel;

public class SampleGatewayClient extends NettyTransmissionClient<SampleGatewayClientConfiguration, SampleGatewayClientChannelInboundHandlerAdapter, SampleGatewayClientProcessRequest> {

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state, t);
        if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
            if (this.configuration.isUseDynamicEventExecutorGroup()) {
                NettyEventExecutorDataController.addCollector(configuration.getGateway().name().toLowerCase() + "Cl", this);
            }
        } else if (state == NettyTransmissionConnState.CLIENT_DISCONNECTED) {
            if (this.configuration.isUseDynamicEventExecutorGroup()) {
                NettyEventExecutorDataController.removeCollector(configuration.getGateway().name().toLowerCase() + "Cl");
            }
        }
    }
}
