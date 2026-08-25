package com.syscom.fep.gateway.netty.fisc.keepalive;

import com.syscom.fep.gateway.netty.NettyTransmissionServerRuleIpFilter;
import io.netty.channel.ChannelHandlerContext;

public class FISCGatewayKeepAliveServerRuleIpFilter extends NettyTransmissionServerRuleIpFilter<FISCGatewayKeepAliveServerConfiguration> {
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
