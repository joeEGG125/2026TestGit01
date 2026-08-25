package com.syscom.fep.invoker.netty;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;

public class SimpleNettyServerRuleIpFilter<Configuration extends SimpleNettyServerConfiguration> extends RuleBasedIpFilter {
    private final Configuration configuration;

    public SimpleNettyServerRuleIpFilter(Configuration configuration) {
        this.configuration = configuration;
    }

    protected boolean accept(ChannelHandlerContext ctx, String remoteIp, int remotePort) {
        this.putMDC(ctx.channel());
        boolean accept = !StringUtils.isNotBlank(configuration.getAcceptIp()) || configuration.getAcceptIp().contains(remoteIp);
        if (!accept) {
            LogHelperFactory.getGeneralLogger().error(SimpleNettyBaseMethod.channelInfo(ctx.channel()), "Client rejected!!!");
        }
        return accept;
    }

    @Override
    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
//        String remoteIp = remoteAddress.getAddress().getHostAddress();
        String remoteIp = ReflectUtil.envokeMethod(remoteAddress.getAddress(), "getHostAddress", StringUtils.EMPTY);
        int remotePort = remoteAddress.getPort();
        return accept(ctx, remoteIp, remotePort);
    }

    protected void putMDC(Channel channel) {}
}
