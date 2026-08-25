package com.syscom.fep.server.gateway.ims.keepalive;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import com.syscom.fep.invoker.netty.SimpleNettyServer;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.nio.charset.StandardCharsets;

@StackTracePointCut(caller = SvrConst.SVR_IMS_GATEWAY)
public class IMSGatewayKeepAliveServer extends SimpleNettyServer<IMSGatewayKeepAliveServerConfiguration, IMSGatewayKeepAliveServerProcessor, String, String> {
    /**
     * 程式或者服務名稱
     *
     * @return
     */
    @Override
    public String getName() {
        return SvrConst.SVR_IMS_GATEWAY;
    }

    /**
     * 接收遠端的訊息進來
     *
     * @param bytes
     * @return
     */
    @Override
    protected String bytesToMessageIn(byte[] bytes) {
        return ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 回應訊息給遠端
     *
     * @param s
     * @return
     */
    @Override
    protected byte[] messageOutToBytes(String s) {
        return ConvertUtil.toBytes(s, StandardCharsets.UTF_8);
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
        this.putMDC(channel);
        if (state == SimpleNettyConnState.SERVER_BOUND) {
            this.logContext.setRemark(StringUtils.join(this.getName(), " KeepAlive Server Begin Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
        } else if (state == SimpleNettyConnState.SERVER_SHUTTING_DOWN) {
            this.logContext.setRemark(StringUtils.join(this.getName(), " KeepAlive Server Stop Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            logMessage(Level.WARN, this.logContext);
        }
    }

    /**
     * 有新的Channel時, 初始化動作
     *
     * @param ch
     */
    @Override
    protected void channelInitialization(SocketChannel ch) {
        String clientIp = ReflectUtil.envokeMethod(ch.remoteAddress().getAddress(), "getHostAddress", StringUtils.EMPTY);
        infoMessage(ch, "Client accepted, clientIp = [", clientIp, "]");
        if (!this.configuration.isLogging()) {
            setChannelLoggingDisable(ch, true);
            infoMessage(ch, "[channelInitialization] Disabled all logging for clientIp = [", clientIp, "]");
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
        if (this.configuration != null && !this.configuration.isLogging()) {
            LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(false));
        }
    }
}
