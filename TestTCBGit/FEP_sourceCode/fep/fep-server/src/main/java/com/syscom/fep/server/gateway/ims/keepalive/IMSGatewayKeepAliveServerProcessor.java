package com.syscom.fep.server.gateway.ims.keepalive;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import com.syscom.fep.invoker.netty.SimpleNettyServerProcessor;
import com.syscom.fep.server.gateway.ims.IMSGatewayConst;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

@StackTracePointCut(caller = SvrConst.SVR_IMS_GATEWAY)
public class IMSGatewayKeepAliveServerProcessor extends SimpleNettyServerProcessor<String, String> {
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
     * 處理進來的電文並回應
     *
     * @param ctx
     * @param s
     * @return
     * @throws Exception
     */
    @Override
    public String processRequestData(ChannelHandlerContext ctx, String s) throws Exception {
        // Client進來的KeepAlive Req電文: HELLO
        // 並回應KeepAlive Res電文: HELLOOK
        if (IMSGatewayConst.KeepAliveRequest.equals(s)) {
            return IMSGatewayConst.KeepAliveResponse;
        }
        return null;
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
        if (state == SimpleNettyConnState.CLIENT_CONNECTED) {
            this.logContext.setRemark(StringUtils.join(SvrConst.SVR_IMS_GATEWAY, " KeepAlive Client(IP:", this.getClientIP(), ",Port:", this.getClientPort(), ")Connected"));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            logMessage(this.logContext);
        } else if (state == SimpleNettyConnState.CLIENT_DISCONNECTED) {
            this.logContext.setRemark(StringUtils.join(SvrConst.SVR_IMS_GATEWAY, " KeepAlive Client(IP:", this.getClientIP(), ",Port:", this.getClientPort(), ")Disconnected"));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            logMessage(Level.WARN, this.logContext);
        }
    }
}
