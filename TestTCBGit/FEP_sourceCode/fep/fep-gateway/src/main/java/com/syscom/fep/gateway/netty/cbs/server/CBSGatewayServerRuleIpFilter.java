package com.syscom.fep.gateway.netty.cbs.server;

import com.syscom.fep.gateway.netty.NettyTransmissionServerRuleIpFilter;
import io.netty.channel.ChannelHandlerContext;

public class CBSGatewayServerRuleIpFilter extends NettyTransmissionServerRuleIpFilter<CBSGatewayServerConfiguration> {
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
