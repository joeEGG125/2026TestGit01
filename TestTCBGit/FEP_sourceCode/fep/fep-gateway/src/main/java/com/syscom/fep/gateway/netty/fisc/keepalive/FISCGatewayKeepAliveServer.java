package com.syscom.fep.gateway.netty.fisc.keepalive;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServer;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionServer;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import org.apache.commons.lang3.StringUtils;

@StackTracePointCut(caller = SvrConst.SVR_FISC_GATEWAY)
public class FISCGatewayKeepAliveServer extends
        NettyTransmissionServer<FISCGatewayKeepAliveServerConfiguration, FISCGatewayKeepAliveServerChannelInboundHandlerAdapter, FISCGatewayKeepAliveServerRuleIpFilter, FISCGatewayKeepAliveServerProcessRequestManager, FISCGatewayKeepAliveServerProcessRequest> {
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
            this.logContext.setRemark(StringUtils.join(Gateway.FISCGW, StringUtils.SPACE, SocketType.Server, " Begin Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
        } else if (state == NettyTransmissionConnState.SERVER_SHUTTING_DOWN) {
            requestManager.handleAllProcessRequest(NettyTransmissionChannelProcessRequestServer::closeConnection);
            requestManager.clearAllProcessRequest();
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.ATM);
            this.logContext.setRemark(StringUtils.join(Gateway.FISCGW, StringUtils.SPACE, SocketType.Server, " Stop Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
        }
    }

    /**
     * 有新的Channel時, 初始化動作
     *
     * @param ch
     */
    @Override
    protected void channelInitialization(SocketChannel ch) {
        // 取出ClientIp, 判斷是否在BypassCheckAtmIp中, 如果有的話, 則不要列印log
        String clientIp = ReflectUtil.envokeMethod(ch.remoteAddress().getAddress(), "getHostAddress", StringUtils.EMPTY);
        NettyTransmissionUtil.infoMessage(ch, "Client accepted, clientIp = [", clientIp, "]");
        if (!this.configuration.isLogging()) {
            NettyTransmissionUtil.setChannelLoggingDisable(ch, true);
            NettyTransmissionUtil.infoMessage(ch, "[channelInitialization] Disabled all logging for clientIp = [", clientIp, "]");
        }
    }
}
