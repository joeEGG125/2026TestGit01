package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.frmcommon.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.Executor;

public abstract class NettyTransmissionChannelInboundHandlerAdapterClient<Configuration extends NettyTransmissionClientConfiguration, ProcessRequest extends NettyTransmissionChannelProcessRequestClient<Configuration>>
        extends NettyTransmissionChannelInboundHandlerAdapter<Configuration> {
    @Autowired
    protected ProcessRequest processRequest;

    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     * @param processRequest
     */
    public void initialization(Configuration configuration, ProcessRequest processRequest) {
        super.initialization(configuration);
        this.processRequest = processRequest;
    }

    public ProcessRequest getProcessRequest() {
        return processRequest;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.putMDC(ctx);
        try {
            byte[] bytes = NettyTransmissionUtil.toBytes((ByteBuf) msg);
            NettyTransmissionUtil.infoMessage(ctx.channel(), Const.MESSAGE_IN, StringUtil.toHex(bytes));
            notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.SERVER_MESSAGE_INCOMING);
            List<byte[]> disassembledBytes = this.disassembleTransmissionMessage(ctx, bytes);
            if (CollectionUtils.isNotEmpty(disassembledBytes)) {
                if (disassembledBytes.size() == 1) {
                    doProcess(ctx, processRequest, disassembledBytes.get(0), false);
                } else {
                    for (byte[] disassembledByte : disassembledBytes) {
                        doProcess(ctx, processRequest, disassembledByte, this.configuration.isAsyncDisassembled());
                    }
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
            notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.CLIENT_CONNECTED_IDLE);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.putMDC(ctx);
        super.channelActive(ctx);
        processRequest.setChannelHandlerContext(ctx);
        NettyTransmissionUtil.infoMessage(ctx.channel(), "Connected to server");
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.CLIENT_CONNECTED);
        this.resetCloseLock();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.putMDC(ctx);
        super.channelInactive(ctx);
        NettyTransmissionUtil.infoMessage(ctx.channel(), "Disconnected from server");
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.CLIENT_DISCONNECTED);
        ctx.close().addListener(future -> {
            this.notifyForClose();
        });
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.putMDC(ctx);
        super.channelRegistered(ctx);
        // 綁定客戶端
        Executor executor = ctx.executor();
        if (executor instanceof NettyTransmissionEventExecutor) {
            ((NettyTransmissionEventExecutor) executor).bindChannelId(ctx.channel());
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.putMDC(ctx);
        super.channelUnregistered(ctx);
        // 解綁客戶端
        Executor executor = ctx.executor();
        if (executor instanceof NettyTransmissionEventExecutor) {
            ((NettyTransmissionEventExecutor) executor).unbindChannelId(ctx.channel());
        }
    }

    @Override
    protected void sslHandshakeCompletionEventTriggered(ChannelHandlerContext ctx, SslHandler sslHandler, SslHandshakeCompletionEvent evt) throws Exception {
        this.putMDC(ctx);
        super.sslHandshakeCompletionEventTriggered(ctx, sslHandler, evt);
        if (evt.isSuccess()) {
            notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.SSL_CERTIFICATE_ACCEPT);
        }
    }
}
