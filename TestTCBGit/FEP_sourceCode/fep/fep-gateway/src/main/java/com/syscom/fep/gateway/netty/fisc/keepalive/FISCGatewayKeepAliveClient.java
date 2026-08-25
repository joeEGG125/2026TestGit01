package com.syscom.fep.gateway.netty.fisc.keepalive;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.gateway.netty.NettyTransmissionClient;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import org.apache.commons.lang3.StringUtils;

public class FISCGatewayKeepAliveClient
        extends NettyTransmissionClient<FISCGatewayKeepAliveClientConfiguration, FISCGatewayKeepAliveClientChannelInboundHandlerAdapter, FISCGatewayKeepAliveClientProcessRequest> {

    @Override
    protected void initData() {
        super.initData();
        this.logContext.setSubSys(SubSystem.INBK);
        this.logContext.setChannel(FEPChannel.FISC);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".initData"));
        this.logContext.setRemark("Begin Service.");
        this.logMessage(this.logContext);
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state, t);
        // 如果沒有收到KeepAlive的回應電文, 則直接關閉長連線
        if (state == NettyTransmissionConnState.CHANNEL_CLOSE_BY_MANUAL) {
            this.terminateConnection();
        }
    }

    /**
     * 有新的Channel時, 初始化動作
     *
     * @param ch
     */
    @Override
    protected void channelInitialization(SocketChannel ch) {
        if (!this.configuration.isLogging()) {
            NettyTransmissionUtil.setChannelLoggingDisable(ch, true);
            NettyTransmissionUtil.infoMessage(ch, "[channelInitialization] Disabled all logging");
        }
    }
}
