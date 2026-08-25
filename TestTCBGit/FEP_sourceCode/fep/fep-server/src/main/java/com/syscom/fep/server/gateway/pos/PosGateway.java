package com.syscom.fep.server.gateway.pos;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import com.syscom.fep.invoker.netty.SimpleNettyServer;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import org.apache.commons.lang3.StringUtils;

@StackTracePointCut(caller = SvrConst.SVR_POS_GATEWAY)
public class PosGateway extends SimpleNettyServer<PosGatewayConfiguration, PosGatewayProcessor, String, String> {
    @Override
    public String getName() {
        return SvrConst.SVR_POS_GATEWAY;
    }

    @Override
    protected String bytesToMessageIn(byte[] bytes) {
        return StringUtil.toHex(bytes);
    }

    @Override
    protected byte[] messageOutToBytes(String s) {
        return ConvertUtil.hexToBytes(s);
    }

    /**
     * 狀態發生改變
     *
     * @param channel
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t) {
        if (state == SimpleNettyConnState.SERVER_BOUND) {
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.POS);
            this.logContext.setRemark(StringUtils.join(this.getName(), " Begin Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
        } else if (state == SimpleNettyConnState.SERVER_SHUTTING_DOWN) {
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.POS);
            this.logContext.setRemark(StringUtils.join(this.getName(), " Stop Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
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
        String remoteIp = getRemoteIp(ch);
        if (PosGatewayConfiguration.BYPASSCHECKCLIENTIP_ALL.equalsIgnoreCase(this.configuration.getByPassCheckClientIp())
                || this.configuration.getByPassCheckClientIp().indexOf(remoteIp) > -1) {
            setChannelLoggingDisable(ch, true);
            infoMessage(ch, "[channelInitialization] Disabled all logging");
        }
    }

    @Override
    protected void putKeptMDC(Channel channel) {
        LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(!getChannelLoggingDisable(channel)));
        super.putKeptMDC(channel);
    }

    @Override
    protected void putMDC(Channel channel) {
        super.putMDC(channel);
        LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(!getChannelLoggingDisable(channel)));
    }
}
