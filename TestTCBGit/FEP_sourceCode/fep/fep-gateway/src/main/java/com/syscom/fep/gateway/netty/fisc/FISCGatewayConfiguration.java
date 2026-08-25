package com.syscom.fep.gateway.netty.fisc;

import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverConfiguration;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderConfiguration;
import com.syscom.fep.gateway.netty.fisc.server.FISCGatewayServerConfiguration;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

public class FISCGatewayConfiguration {
    private final RefBase<NettyTransmissionConnState> connState = new RefBase<>(NettyTransmissionConnState.CLIENT_SHUT_DOWN);
    @NestedConfigurationProperty
    private final List<FISCGatewayClientSenderConfiguration> sender = new ArrayList<>();
    @NestedConfigurationProperty
    private final List<FISCGatewayClientReceiverConfiguration> receiver = new ArrayList<>();
    @NestedConfigurationProperty
    private final List<FISCGatewayServerConfiguration> fepap = new ArrayList<>();

    public List<FISCGatewayClientSenderConfiguration> getSender() {
        return sender;
    }

    public List<FISCGatewayClientReceiverConfiguration> getReceiver() {
        return receiver;
    }

    public List<FISCGatewayServerConfiguration> getFepap() {
        return fepap;
    }

    public RefBase<NettyTransmissionConnState> getConnState() {
        return connState;
    }

    public void setConnState(NettyTransmissionConnState connState) {
        this.connState.set(connState);
    }
}
