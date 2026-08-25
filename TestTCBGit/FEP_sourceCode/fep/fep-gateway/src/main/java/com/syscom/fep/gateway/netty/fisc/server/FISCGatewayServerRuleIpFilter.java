package com.syscom.fep.gateway.netty.fisc.server;

import com.syscom.fep.gateway.netty.NettyTransmissionServerRuleIpFilter;
import io.netty.channel.ChannelHandlerContext;

public class FISCGatewayServerRuleIpFilter extends NettyTransmissionServerRuleIpFilter<FISCGatewayServerConfiguration> {
    /**
     * @param ctx
     * @param remoteIp
     * @param remotePort
     * @return
     */
    @Override
    protected boolean accept(ChannelHandlerContext ctx, String remoteIp, int remotePort) {
        return true;
    }
}
