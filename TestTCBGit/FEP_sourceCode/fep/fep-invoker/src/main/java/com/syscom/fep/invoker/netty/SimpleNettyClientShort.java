package com.syscom.fep.invoker.netty;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.exception.FEPBaseException;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.netty.NettyEventExecutorData;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 用建立短連線的Client
 *
 * @param <Configuration>
 * @param <MessageIn>
 * @param <MessageOut>
 */
public abstract class SimpleNettyClientShort<Configuration extends SimpleNettyClientConfiguration, MessageIn, MessageOut>
        extends SimpleNettyBase<Configuration, MessageIn, MessageOut> {
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private EventExecutorGroup bisEventExecutorGroup;
    private final NettyClientChannelInboundHandler handler = new NettyClientChannelInboundHandler();
    private final NettyClientChannelInboundHandlerException exceptionHandler = new NettyClientChannelInboundHandlerException();
    private String localIP;
    private int localPort;

    @Override
    protected void initialization() {
        this.putMDC(null);
        this.workerGroup = new NioEventLoopGroup(
                this.configuration.getClientWorkerThreadNum(),
                new SimpleNettyThreadFactory(StringUtils.join(this.getName(), "WorkerThread")));
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(this.workerGroup);
        this.bootstrap.channel(NioSocketChannel.class);
        this.bootstrap.option(ChannelOption.SO_KEEPALIVE, this.configuration.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, this.configuration.isTcpNodelay())
                .option(ChannelOption.SO_SNDBUF, this.configuration.getSoSndBuf())
                .option(ChannelOption.SO_RCVBUF, this.configuration.getSoRcvBuf())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.configuration.getConnectTimeoutMillis());
        if (this.configuration.getTcpKeepIdle() > 0) {
            try {
                this.bootstrap.option(NioChannelOption.of(SimpleNettySocketOptions.getSocketOption(SimpleNettySocketOptionName.TCP_KEEPIDLE)), configuration.getTcpKeepIdle());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPIDLE'");
            }
        }
        if (this.configuration.getTcpKeepInterval() > 0) {
            try {
                this.bootstrap.option(NioChannelOption.of(SimpleNettySocketOptions.getSocketOption(SimpleNettySocketOptionName.TCP_KEEPINTERVAL)), configuration.getTcpKeepInterval());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPINTERVAL'");
            }
        }
        if (this.configuration.getTcpKeepCount() > 0) {
            try {
                this.bootstrap.option(NioChannelOption.of(SimpleNettySocketOptions.getSocketOption(SimpleNettySocketOptionName.TCP_KEEPCOUNT)), configuration.getTcpKeepCount());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPCOUNT'");
            }
        }
        if (this.configuration.getRcvBufAllocator() == 0) {
            this.bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator()); // 動態緩衝區分配器
        } else if (this.configuration.getRcvBufAllocator() > 0) {
            this.bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(this.configuration.getRcvBufAllocator())); // 固定長度緩衝區分配器
        }
        if (this.configuration.getClientBisThreadNum() > 0) {
            if (this.configuration.isUseDynamicEventExecutorGroup()) {
                this.bisEventExecutorGroup = new SimpleNettyEventExecutorGroup(
                        this.getName(),
                        this.configuration.getClientBisThreadNum(),
                        this.configuration.getDynamicEventExecutorIdleCheckInterval(),
                        new SimpleNettyThreadFactory(StringUtils.join(this.getName(), "DynamicBusinessThread")));
            } else {
                this.bisEventExecutorGroup = new DefaultEventExecutorGroup(
                        this.configuration.getClientBisThreadNum(),
                        new SimpleNettyThreadFactory(StringUtils.join(this.getName(), "BusinessThread")));
            }
        }
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                putMDC(ch);
                channelInitialization(ch);
                ChannelPipeline pipeline = ch.pipeline();
                // import ssl key to enable SSL
                try {
                    SslHandler sslHandler = getSslHandler(ch, configuration, true); // 客戶端認證方式
                    if (sslHandler != null) {
                        if (bisEventExecutorGroup != null) {
                            pipeline.addLast(bisEventExecutorGroup, sslHandler);
                        } else {
                            pipeline.addLast(sslHandler);
                        }
                    }
                } catch (Exception e) {
                    errorMessage(e, "SSL initialized failed!!!");
                    throw ExceptionUtil.createException(e, "SSL initialized failed!!!");
                }
                // IdleStateHandler
                if (configuration.getReaderIdleTime() > 0 || configuration.getWriterIdleTime() > 0) {
                    pipeline.addLast(new IdleStateHandler(configuration.getReaderIdleTime(), configuration.getWriterIdleTime(), 0, TimeUnit.MILLISECONDS));
                }
                // ByteToMessageDecoder
                ByteToMessageDecoder decoder = getByteToMessageDecoder();
                if (decoder != null) {
                    if (bisEventExecutorGroup != null) {
                        pipeline.addLast(bisEventExecutorGroup, decoder);
                    } else {
                        pipeline.addLast(decoder);
                    }
                }
                if (bisEventExecutorGroup != null) {
                    pipeline.addLast(bisEventExecutorGroup, handler)
                            .addLast(bisEventExecutorGroup, exceptionHandler); // exceptionHandlerAdapter一定要放在最後, 切記切記
                } else {
                    pipeline.addLast(handler)
                            .addLast(exceptionHandler); // exceptionHandlerAdapter一定要放在最後, 切記切記
                }
            }
        });
        notification.addConnStateListener(0, this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    @Override
    protected void establishConnection() {
        // 短連線不需要覆寫這個方法
    }

    @Override
    public void closeConnection() {
        // 短連線不需要覆寫這個方法
    }

    @Override
    public void terminateConnection() {
        this.putMDC(null);
        String host = this.configuration.getHost();
        int port = this.configuration.getPort();
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.CLIENT_SHUTTING_DOWN);
        this.infoMessage("[Terminate Connection]Try to destroy all event executor group from host = [", host, "], port = [", port, "]...");
        if (this.bisEventExecutorGroup != null) {
            this.bisEventExecutorGroup.shutdownGracefully();
            this.bisEventExecutorGroup = null;
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
            this.workerGroup = null;
        }
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.CLIENT_SHUT_DOWN);
        this.infoMessage("[Terminate Connection]Destroy all event executor group from host = [", host, "], port = [", port, "] successful");
        notification.removeConnStateListener(this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    /**
     * 建立連線發送訊息並接收訊息
     *
     * @param messageOut
     * @param timeout
     * @return
     * @throws Exception
     */
    public MessageIn establishConnectionAndSendReceive(MessageOut messageOut, int timeout) throws Exception {
        return this.establishConnectionAndSendReceive(this.configuration, messageOut, timeout);
    }

    /**
     * 建立連線發送訊息並接收訊息
     *
     * @param configuration
     * @param messageOut
     * @param timeout
     * @return
     * @throws Exception
     */
    public MessageIn establishConnectionAndSendReceive(Configuration configuration, MessageOut messageOut, int timeout) throws Exception {
        this.putMDC(null);
        Channel channel = null;
        try {
            this.configuration = configuration;
            String host = configuration.getHost();
            int port = configuration.getPort();
            // send request
            if (messageOut == null) {
                throw ExceptionUtil.createException("Cannot send message to host = [", host, "], port = [", port, "], cause MessageOut is null!!!");
            }
            this.infoMessage("Try to establish connection, host = [", host, "], port = [", port, "]...");
            notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.CLIENT_CONNECTING);
            // connect
            try {
                channel = this.bootstrap.connect(host, port).sync().channel();
                putKeptMDC(channel); // 注意注意, 這裡一定要set一次
                this.putMDC(channel);
                this.infoMessage(channel, "Establish connection succeed, host = [", host, "], port = [", port, "]");
            } catch (Exception e) {
                notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.CLIENT_CONNECTING_FAILED);
                throw new FEPBaseException(FEPReturnCode.CanNotConnectRemoteHost, e, "Establish connection failed, host = [", host, "], port = [", port, "]");
            }
            try {
                this.infoMessage(channel, Const.MESSAGE_OUT, messageOut);
                channel.writeAndFlush(toByteBuf(messageOutToBytes(messageOut)));
            } catch (Exception e) {
                throw ExceptionUtil.createException(e, "Send message failed, message = [", messageOut, "], host = [", host, "], port = [", port, "]");
            }
            // wait and get response
            AttributeKey<MessageInComplete> key = AttributeKey.valueOf(SimpleNettyAttributeKey.MessageIn.name());
            Attribute<MessageInComplete> attr = channel.attr(key);
            if (timeout > 0) {
                long currentTimeMillis = System.currentTimeMillis();
                this.infoMessage(channel, "start to wait message in for timeout = [", timeout, "]");
                synchronized (attr) {
                    MessageInComplete messageInComplete = attr.get();
                    // 這裡要判斷一下是否在wait前有先收到電文, 如果是這樣就不需要wait
                    if (messageInComplete == null || !messageInComplete.completed) {
                        try {
                            attr.wait(timeout);
                        } catch (InterruptedException e) {
                            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        }
                    }
                }
                if (System.currentTimeMillis() - currentTimeMillis >= timeout) {
                    throw ExceptionUtil.createSocketTimeoutException("Receive timeout after ", timeout, " millisecond, host = [", host, "], port = [", port, "]");
                }
            }
            MessageInComplete messageInComplete = attr.get();
            return messageInComplete != null ? messageInComplete.messageIn : null;
        } catch (Exception e) {
            this.errorMessage(channel, e, e.getMessage());
            LogData logData = new LogData();
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".establishConnectionAndSendReceive"));
            logData.setRemark(e.getMessage());
            sendEMS(logData);
            throw e;
        } finally {
            // 這裡記得關閉連線
            if (channel != null) {
                channel.close();
            }
        }
    }

    @Sharable
    private class NettyClientChannelInboundHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            putMDC(ctx.channel());
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.SERVER_MESSAGE_INCOMING);
            AttributeKey<MessageInComplete> key = AttributeKey.valueOf(SimpleNettyAttributeKey.MessageIn.name());
            Attribute<MessageInComplete> attr = ctx.channel().attr(key);
            MessageIn messageIn = null;
            try {
                byte[] bytes = toBytes((ByteBuf) msg);
                messageIn = bytesToMessageIn(bytes);
                if (messageIn == null) {
                    warnMessage(ctx.channel(), "MessageIn is null!!! host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
                    return;
                }
                if (messageIn instanceof byte[]) {
                    infoMessage(ctx.channel(), Const.MESSAGE_IN, StringUtils.join("bytes:[", StringUtils.join(((byte[]) messageIn), ','), "]"));
                } else {
                    infoMessage(ctx.channel(), Const.MESSAGE_IN, messageIn);
                }
            } finally {
                ReferenceCountUtil.release(msg);
                notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_CONNECTED_IDLE);
                synchronized (attr) {
                    infoMessage(ctx.channel(), "start to call attr.set(messageIn) and attr.notifyAll()");
                    attr.set(new MessageInComplete(messageIn, true));
                    attr.notifyAll();
                    infoMessage(ctx.channel(), "finish to call attr.set(messageIn) and attr.notifyAll()");
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            putMDC(ctx.channel());
            super.channelActive(ctx);
            localIP = getLocalIp(); // 塞入本機的IP
            localPort = getLocalPort(ctx.channel()); // 塞入連線的Port
            infoMessage(ctx.channel(), "Connected to server, host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_CONNECTED);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            putMDC(ctx.channel());
            super.channelInactive(ctx);
            infoMessage(ctx.channel(), "Disconnected from server, host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_DISCONNECTED);
            ctx.close();
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            putMDC(ctx.channel());
            super.channelRegistered(ctx);
            // 綁定客戶端
            Executor executor = ctx.executor();
            if (executor instanceof SimpleNettyEventExecutor) {
                ((SimpleNettyEventExecutor) executor).bindChannelId(ctx.channel());
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            putMDC(ctx.channel());
            super.channelUnregistered(ctx);
            // 解綁客戶端
            Executor executor = ctx.executor();
            if (executor instanceof SimpleNettyEventExecutor) {
                ((SimpleNettyEventExecutor) executor).unbindChannelId(ctx.channel());
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            putMDC(ctx.channel());
            if (evt instanceof IdleStateEvent) {
                // --20221219 ben 暫將讀取資料庫部份全mark
                // infoMessage(ctx.channel(), "IdleStateEvent occur...");
                // --20221219 ben 暫將讀取資料庫部份全mark
            } else if (evt instanceof SslHandshakeCompletionEvent) {
                infoMessage(ctx.channel(), "SslHandshakeCompletionEvent occur...");
                SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);
                sslHandshakeCompletionEventTriggered(ctx, sslHandler, (SslHandshakeCompletionEvent) evt);
            }
        }
    }

    @Sharable
    private class NettyClientChannelInboundHandlerException extends ChannelInboundHandlerAdapter {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            putMDC(ctx.channel());
            errorMessage(ctx.channel(), cause, "Close the connection when an exception is raised, host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_DISCONNECTED, cause);
            LogData logData = new LogData();
            logData.setProgramException(cause);
            logData.setProgramName(StringUtils.join(ProgramName, ".exceptionHandler.exceptionCaught"));
            sendEMS(logData);
            ctx.close();
        }
    }

    /**
     * 可以讓子類去複寫
     *
     * @param ctx
     * @param sslHandler
     * @param evt
     */
    @Override
    protected void sslHandshakeCompletionEventTriggered(ChannelHandlerContext ctx, SslHandler sslHandler, SslHandshakeCompletionEvent evt) {
        putMDC(ctx.channel());
        super.sslHandshakeCompletionEventTriggered(ctx, sslHandler, evt);
        if (evt.isSuccess()) {
            notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.SSL_CERTIFICATE_ACCEPT);
        }
    }

    /**
     * 獲取統計數據
     *
     * @return
     */
    @Override
    public NettyEventExecutorData getNettyEventExecutorData() {
        this.putMDC(null);
        NettyEventExecutorData data = null;
        if (this.bisEventExecutorGroup != null && this.bisEventExecutorGroup instanceof SimpleNettyEventExecutorGroup) {
            data = new NettyEventExecutorData();
            data.setExecutorSetupCount(((SimpleNettyEventExecutorGroup) this.bisEventExecutorGroup).executorSetupCount());
            data.setExecutorActiveCount(((SimpleNettyEventExecutorGroup) this.bisEventExecutorGroup).executorActiveCount());
            data.setExecutorIdleCount(((SimpleNettyEventExecutorGroup) this.bisEventExecutorGroup).executorIdleCount());
            data.setExecutorConfigurationCount(this.configuration.getClientBisThreadNum());
            this.infoMessage(null, "[getNettyEventExecutorData]NettyEventExecutorData:", data);
        }
        return data;
    }

    /**
     * 設定線程數
     *
     * @param nThreads
     * @return
     * @throws Exception
     */
    @Override
    public boolean setNettyEventExecutorThreads(int nThreads) throws Exception {
        this.putMDC(null);
        if (this.bisEventExecutorGroup != null && this.bisEventExecutorGroup instanceof SimpleNettyEventExecutorGroup) {
            if (nThreads <= 0) {
                nThreads = this.configuration.getClientBisThreadNum();
            }
            this.infoMessage(null, "[setNettyEventExecutorThreads]nThreads:", nThreads);
            return ((SimpleNettyEventExecutorGroup) this.bisEventExecutorGroup).setExecutorThreads(nThreads);
        }
        return false;
    }

    public String getLocalIP() {
        return localIP;
    }

    public int getLocalPort() {
        return localPort;
    }

    private class MessageInComplete {
        MessageIn messageIn;
        boolean completed;

        public MessageInComplete(MessageIn messageIn, boolean completed) {
            this.messageIn = messageIn;
            this.completed = completed;
        }
    }
}
