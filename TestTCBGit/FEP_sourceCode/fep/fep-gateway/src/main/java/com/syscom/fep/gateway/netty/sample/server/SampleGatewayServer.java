package com.syscom.fep.gateway.netty.sample.server;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.frmcommon.netty.NettyEventExecutorDataController;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServer;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionServer;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

public class SampleGatewayServer extends
        NettyTransmissionServer<SampleGatewayServerConfiguration, SampleGatewayServerChannelInboundHandlerAdapter, SampleGatewayServerRuleIpFilter, SampleGatewayServerProcessRequestManager, SampleGatewayServerProcessRequest> {
    @Override
    protected void initData() {
        super.initData();
        this.setReestablishConnectionAfterTerminateConnection(true);
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state, t);
        if (state == NettyTransmissionConnState.SERVER_BOUND) {
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.ATM);
            this.logContext.setRemark(StringUtils.join(Gateway.SAMPLEGW, StringUtils.SPACE, SocketType.Server, " Begin Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
            if (this.configuration.isUseDynamicEventExecutorGroup()) {
                NettyEventExecutorDataController.addCollector(configuration.getGateway().name().toLowerCase() + "Svr", this);
            }
        } else if (state == NettyTransmissionConnState.SERVER_SHUTTING_DOWN) {
            requestManager.handleAllProcessRequest(NettyTransmissionChannelProcessRequestServer::closeConnection);
            requestManager.clearAllProcessRequest();
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.ATM);
            this.logContext.setRemark(StringUtils.join(Gateway.SAMPLEGW, StringUtils.SPACE, SocketType.Server, " Stop Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
            if (this.configuration.isUseDynamicEventExecutorGroup()) {
                NettyEventExecutorDataController.removeCollector(configuration.getGateway().name().toLowerCase() + "Svr");
            }
        }
    }
}
