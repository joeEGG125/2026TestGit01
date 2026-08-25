package com.syscom.fep.gateway.netty.cbs.server;

import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import com.syscom.fep.gateway.ims.cbs.sender.CBSGatewayClientSenderProcessRequest;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServerManager;

public class CBSGatewayServerProcessRequestManager extends NettyTransmissionChannelProcessRequestServerManager<CBSGatewayServerConfiguration, CBSGatewayServerProcessRequest> {
    private CBSGatewayServerConfiguration configuration;
    private RoundRobin<CBSGatewayClientSenderProcessRequest> senderProcessRequest;

    public void initialization(CBSGatewayServerConfiguration configuration, RoundRobin<CBSGatewayClientSenderProcessRequest> senderProcessRequest) {
        this.configuration = configuration;
        this.senderProcessRequest = senderProcessRequest;
    }

    /**
     * 這裡覆寫這個方法, 非SpringBean方式
     *
     * @return
     */
    @Override
    protected CBSGatewayServerProcessRequest createProcessRequest() {
        CBSGatewayServerProcessRequest cbsGatewayServerProcessRequest = new CBSGatewayServerProcessRequest();
        cbsGatewayServerProcessRequest.initialization(configuration, this.senderProcessRequest);
        return cbsGatewayServerProcessRequest;
    }
}