package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.gateway.netty.NettyTransmissionChannelInboundHandlerAdapterServer;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class ATMGatewayServerChannelInboundHandlerAdapter extends NettyTransmissionChannelInboundHandlerAdapterServer<ATMGatewayServerConfiguration, ATMGatewayServerProcessRequestManager, ATMGatewayServerProcessRequest> {

    private boolean isChannelRejected(ChannelHandlerContext ctx) {
        return NettyTransmissionUtil.getChannelReject(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (this.isChannelRejected(ctx)) {
            // 如果rejected, 則跳過當前Handler程式的channelActive邏輯處理, 呼叫Next Handler的channelActive方法
            ctx.fireChannelActive();
        } else {
            // 如果accept, 則交給當前Handler程式(父類)的channelActive邏輯處理
            super.channelActive(ctx);
        }
    }
}
