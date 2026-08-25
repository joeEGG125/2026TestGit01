package com.syscom.fep.gateway.netty.fisc.server;

import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServerManager;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderProcessRequest;

public class FISCGatewayServerProcessRequestManager extends NettyTransmissionChannelProcessRequestServerManager<FISCGatewayServerConfiguration, FISCGatewayServerProcessRequest> {
    private FISCGatewayServerConfiguration configuration;
    private FISCGatewayClientSenderProcessRequest senderProcessRequest;

    public void initialization(FISCGatewayServerConfiguration configuration, FISCGatewayClientSenderProcessRequest senderProcessRequest) {
        this.configuration = configuration;
        this.senderProcessRequest = senderProcessRequest;
    }

    /**
     * 這裡覆寫這個方法, 非SpringBean方式
     *
     * @return
     */
    @Override
    protected FISCGatewayServerProcessRequest createProcessRequest() {
        FISCGatewayServerProcessRequest fiscGatewayServerProcessRequest = new FISCGatewayServerProcessRequest();
        fiscGatewayServerProcessRequest.initialization(configuration, this.senderProcessRequest);
        return fiscGatewayServerProcessRequest;
    }
}