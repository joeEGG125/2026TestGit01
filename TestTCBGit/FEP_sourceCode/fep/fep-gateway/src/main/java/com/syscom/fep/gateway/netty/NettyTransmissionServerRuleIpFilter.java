package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;

public abstract class NettyTransmissionServerRuleIpFilter<Configuration extends NettyTransmissionServerConfiguration> extends RuleBasedIpFilter {
    protected final String ProgramName = this.getClass().getSimpleName();
    protected LogData logContext;
    @Autowired
    protected Configuration configuration;

    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     */
    public void initialization(Configuration configuration) {
        this.configuration = configuration;
    }

    protected abstract boolean accept(ChannelHandlerContext ctx, String remoteIp, int remotePort);

    public void setLogContext(LogData logContext) {
        this.logContext = logContext;
    }

    @Override
    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
        putMDC(ctx);
        // String remoteIp = remoteAddress.getAddress().getHostAddress();
        String remoteIp = ReflectUtil.envokeMethod(remoteAddress.getAddress(), "getHostAddress", StringUtils.EMPTY);
        int remotePort = remoteAddress.getPort();
        return accept(ctx, remoteIp, remotePort);
    }

    protected void putMDC(ChannelHandlerContext ctx) {
        NettyTransmissionUtil.putMDC(ctx, this.configuration);
    }
}
