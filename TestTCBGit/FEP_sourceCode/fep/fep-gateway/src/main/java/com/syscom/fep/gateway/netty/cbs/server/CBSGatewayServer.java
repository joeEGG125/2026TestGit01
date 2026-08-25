package com.syscom.fep.gateway.netty.cbs.server;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionBaseCommuByteToMessageDecoder;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServer;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionServer;
import io.netty.channel.Channel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.lang3.StringUtils;

@StackTracePointCut(caller = SvrConst.SVR_CBS_GATEWAY)
public class CBSGatewayServer extends
        NettyTransmissionServer<CBSGatewayServerConfiguration, CBSGatewayServerChannelInboundHandlerAdapter, CBSGatewayServerRuleIpFilter, CBSGatewayServerProcessRequestManager, CBSGatewayServerProcessRequest> {
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
            this.logContext.setChannel(FEPChannel.CBS);
            this.logContext.setRemark(StringUtils.join(Gateway.CBSGW, StringUtils.SPACE, SocketType.Server, " Begin Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
        } else if (state == NettyTransmissionConnState.SERVER_SHUTTING_DOWN) {
            requestManager.handleAllProcessRequest(NettyTransmissionChannelProcessRequestServer::closeConnection);
            requestManager.clearAllProcessRequest();
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.CBS);
            this.logContext.setRemark(StringUtils.join(Gateway.CBSGW, StringUtils.SPACE, SocketType.Server, " Stop Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
        }
    }

    /**
     * 由子類去實作電文解碼器, 用來處理一些特殊的電文
     *
     * @return
     */
    @Override
    protected ByteToMessageDecoder getByteToMessageDecoder() {
        // 從CBSAdapter丟過來的電文, CBSGW在接收時可能會被截斷, 故這裡需要進行特殊解碼處理
        return new NettyTransmissionBaseCommuByteToMessageDecoder();
    }
}
