package com.syscom.fep.gateway.netty.fisc.keepalive;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.gateway.entity.GatewayCodeConstant;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServer;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;

@StackTracePointCut(caller = SvrConst.SVR_FISC_GATEWAY)
public class FISCGatewayKeepAliveServerProcessRequest extends NettyTransmissionChannelProcessRequestServer<FISCGatewayKeepAliveServerConfiguration> {
    /**
     * 處理Client進來的KeepAlive Req電文: HELLO
     * 並回應KeepAlive Res電文: HELLOOK
     * KeepAlive電文收送都不要記錄LOG
     *
     * @param ctx
     * @param bytes
     * @throws Exception
     */
    @Override
    public void doProcess(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        if (ArrayUtils.isNotEmpty(bytes)) {
            String keepAlive = ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
            if (GatewayCodeConstant.FISCGWKeepAliveRequest.equals(keepAlive)) {
                NettyTransmissionUtil.sendPlainMessage(this, this.configuration, ctx.channel(), GatewayCodeConstant.FISCGWKeepAliveResponse);
            }
        }
    }
}
