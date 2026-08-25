package com.syscom.fep.gateway.netty;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.netty.NettyEventExecutorData;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.gateway.entity.GatewayCodeConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class NettyTransmissionClient<Configuration extends NettyTransmissionClientConfiguration, HandlerAdapter extends NettyTransmissionChannelInboundHandlerAdapterClient<Configuration, ProcessRequest>, ProcessRequest extends NettyTransmissionChannelProcessRequestClient<Configuration>>
        extends NettyTransmission<Configuration, HandlerAdapter, ProcessRequest> {
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private EventExecutorGroup bisEventExecutorGroup;
    private Channel channel;
    private final AtomicInteger establishConnectionTimes = new AtomicInteger(1);
    private boolean isShuttingDown = false;
    protected AtomicReference<NettyTransmissionConnState> currentConnState = new AtomicReference<NettyTransmissionConnState>();
    protected NettyTransmissionClientMonitor<Configuration> transmissionClientMonitor = new NettyTransmissionClientMonitor<>();
    protected NettyTransmissionChannelInboundHandlerAdapterClientException<Configuration, ProcessRequest> exceptionHandlerAdapter;

    @Override
    protected void initData() {}

    @Override
    protected void initialization() {
        this.putMDC(null);
        this.workerGroup = new NioEventLoopGroup(
                this.configuration.getWorkerThreadNum(),
                new NettyTransmissionThreadFactory(StringUtils.join(this.configuration.getGateway(), this.configuration.getSocketType(), "WorkerThread")));
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(this.workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, this.configuration.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, this.configuration.isTcpNodelay())
                .option(ChannelOption.SO_SNDBUF, this.configuration.getSoSndBuf())
                .option(ChannelOption.SO_RCVBUF, this.configuration.getSoRcvBuf())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.configuration.getConnectTimeoutMillis());
        if (this.configuration.getTcpKeepIdle() > 0) {
            try {
                this.bootstrap.option(NioChannelOption.of(NettyTransmissionSocketOptions.getSocketOption(NettyTransmissionSocketOptionName.TCP_KEEPIDLE)), configuration.getTcpKeepIdle());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPIDLE'");
            }
        }
        if (this.configuration.getTcpKeepInterval() > 0) {
            try {
                this.bootstrap.option(NioChannelOption.of(NettyTransmissionSocketOptions.getSocketOption(NettyTransmissionSocketOptionName.TCP_KEEPINTERVAL)), configuration.getTcpKeepInterval());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPINTERVAL'");
            }
        }
        if (this.configuration.getTcpKeepCount() > 0) {
            try {
                this.bootstrap.option(NioChannelOption.of(NettyTransmissionSocketOptions.getSocketOption(NettyTransmissionSocketOptionName.TCP_KEEPCOUNT)), configuration.getTcpKeepCount());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPCOUNT'");
            }
        }
        if (this.configuration.getRcvBufAllocator() == 0) {
            this.bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator()); // 動態緩衝區分配器
        } else if (this.configuration.getRcvBufAllocator() > 0) {
            this.bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(this.configuration.getRcvBufAllocator())); // 固定長度緩衝區分配器
        }
        if (this.configuration.getBisThreadNum() > 0) {
            if (this.configuration.isUseDynamicEventExecutorGroup()) {
                this.bisEventExecutorGroup = new NettyTransmissionEventExecutorGroup(
                        this.configuration.getBisThreadNum(),
                        this.configuration.getDynamicEventExecutorIdleCheckInterval(),
                        new NettyTransmissionThreadFactory(StringUtils.join(this.configuration.getGateway(), this.configuration.getSocketType(), "DynamicBusinessThread")));
            } else {
                this.bisEventExecutorGroup = new DefaultEventExecutorGroup(
                        this.configuration.getBisThreadNum(),
                        new NettyTransmissionThreadFactory(StringUtils.join(this.configuration.getGateway(), this.configuration.getSocketType(), "BusinessThread")));
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
                    SslHandler sslHandler = getSslHandler(ch);
                    if (sslHandler != null) {
                        if (bisEventExecutorGroup != null) {
                            pipeline.addLast(bisEventExecutorGroup, sslHandler);
                        } else {
                            pipeline.addLast(sslHandler);
                        }
                    }
                } catch (Exception e) {
                    NettyTransmissionUtil.errorMessage(ch, e, "SSL initialized failed!!!");
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
                // handlerAdapter
                // exceptionHandlerAdapter
                if (bisEventExecutorGroup != null) {
                    pipeline.addLast(bisEventExecutorGroup, handlerAdapter)
                            .addLast(bisEventExecutorGroup, getExceptionHandlerAdapter()); // exceptionHandlerAdapter一定要放在最後, 切記切記
                } else {
                    pipeline.addLast(handlerAdapter)
                            .addLast(getExceptionHandlerAdapter()); // exceptionHandlerAdapter一定要放在最後, 切記切記
                }
            }
        });
        notification.addConnStateListener(0, this.configuration.getNettyTransmissionNotificationKey(), this);
        transmissionClientMonitor.setTransmissionConfiguration(configuration);
    }

    @Override
    public void establishConnection() {
        this.putMDC(null);
        String host = this.configuration.getHost();
        int port = this.configuration.getPort();
        NettyTransmissionUtil.infoMessage(null, "Try to establish connection, host = [", host, "], port = [", port, "] at [", establishConnectionTimes.get(), "] times...");
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, NettyTransmissionConnState.CLIENT_CONNECTING);
        ChannelFuture connFuture = this.bootstrap.connect(host, port);
        connFuture.addListener((ChannelFuture future) -> {
            this.putMDC(future.channel());
            if (future.isSuccess()) {
                this.channel = future.channel();
                int connectTimes = establishConnectionTimes.getAndSet(0);
                NettyTransmissionUtil.infoMessage(this.channel, "Establish connection succeed, host = [", host, "], port = [", port, "] at [", connectTimes, "] times");
                // 需要自動重連
                if (this.configuration.isAutoReestablishConnection()) {
                    this.channel.closeFuture().addListener((ChannelFuture closeFut) -> {
                        this.scheduleToReestablishConnection(future);
                    });
                }
            } else {
                NettyTransmissionUtil.errorMessage(null, future.cause(), "Establish connection failed, host = [", host, "], port = [", port, "] at [", establishConnectionTimes.get(), "] times");
                // 如果是首次連線無法連線成功, 這個this.channel是null, 所以這裡要塞隻, 否則scheduleToReestablishConnection
                if (this.channel == null) this.channel = future.channel();
                notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), this.channel, NettyTransmissionConnState.CLIENT_CONNECTING_FAILED);
                // 需要自動重連
                if (this.configuration.isAutoReestablishConnection()) {
                    this.scheduleToReestablishConnection(future);
                }
            }
        });
    }

    private void scheduleToReestablishConnection(ChannelFuture future) {
        this.putMDC(future.channel());
        if (!this.isShuttingDown) {
            String host = this.configuration.getHost();
            int port = this.configuration.getPort();
            long delay = this.configuration.getReestablishConnectionInterval();
            NettyTransmissionUtil.warnMessage(null, "Try to reestablish connection, host = [", host, "], port = [", port, "] at [", establishConnectionTimes.incrementAndGet(), "] times after [", delay, "] milliseconds");
            future.channel().eventLoop().schedule(this::establishConnection, delay - GatewayCodeConstant.FIXED_MILLISECONDS, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void terminateConnection() {
        this.putMDC(this.channel);
        this.isShuttingDown = true;
        String host = this.configuration.getHost();
        int port = this.configuration.getPort();
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), this.channel, NettyTransmissionConnState.CLIENT_SHUTTING_DOWN);
        NettyTransmissionUtil.infoMessage(this.channel, "[Terminate Connection]Try to disconnect from host = [", host, "], port = [", port, "]...");
        if (this.handlerAdapter != null) {
            this.handlerAdapter.destroy();
        }
        if (this.bisEventExecutorGroup != null) {
            this.bisEventExecutorGroup.shutdownGracefully();
            this.bisEventExecutorGroup = null;
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
            this.workerGroup = null;
        }
        if (this.handlerAdapter != null) {
            long millis = handlerAdapter.waitForClose();
            NettyTransmissionUtil.warnMessage(this.channel, "[Terminate Connection]WaitForClose from host = [", host, "], port = [", port, "] for millis = [", millis, "]");
        }
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), this.channel, NettyTransmissionConnState.CLIENT_SHUT_DOWN);
        NettyTransmissionUtil.infoMessage(this.channel, "[Terminate Connection]Disconnect from host = [", host, "], port = [", port, "] successful");
        handlerAdapter.getProcessRequest().removeConnStateListener();
        notification.removeConnStateListener(this.configuration.getNettyTransmissionNotificationKey(), this);
        manager.removeTransmission(this);
    }

    protected SslHandler getSslHandler(SocketChannel ch) throws Exception {
        SslHandler sslHandler = NettyTransmissionUtil.getSslHandler(configuration.getSslConfig(), configuration.isSslNeedClientAuth(), configuration.isSslWantClientAuth(), true); // 客戶端認證方式
        if (sslHandler != null) {
            sslHandler.setHandshakeTimeoutMillis(configuration.getHandshakeTimeoutMillis());
        }
        return sslHandler;
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        NettyTransmissionUtil.infoMessage(channel, "connection state changed, listener = [", this.ProgramName, "], status = [", state, "], exception message = [", (t != null ? t.getMessage() : StringUtils.EMPTY), "]");
        this.currentConnState.set(state);
        this.transmissionClientMonitor.setConnState(this.currentConnState.get());
        if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
            NettyTransmissionChannelInformation channelInformation = NettyTransmissionUtil.getChannelInformation(new NettyTransmissionChannelInformation(), channel);
            this.transmissionClientMonitor.setLocal(StringUtils.join(channelInformation.getLocalIp(), ":", channelInformation.getLocalPort()));
            this.transmissionClientMonitor.setConnections(1);
            this.transmissionClientMonitor.setConnectedTime(Calendar.getInstance().getTimeInMillis());
        } else if (state == NettyTransmissionConnState.CLIENT_DISCONNECTED) {
            this.transmissionClientMonitor.setLocal(StringUtils.EMPTY);
            this.transmissionClientMonitor.setConnections(0);
            this.transmissionClientMonitor.setDisconnectedTime(Calendar.getInstance().getTimeInMillis());
        }
    }

    public void setTransmissionClientMonitor(NettyTransmissionClientMonitor<Configuration> transmissionClientMonitor) {
        this.transmissionClientMonitor = transmissionClientMonitor;
    }

    public NettyTransmissionClientMonitor<Configuration> getTransmissionClientMonitor() {
        return transmissionClientMonitor;
    }

    protected NettyTransmissionChannelInboundHandlerAdapterClientException<Configuration, ProcessRequest> getExceptionHandlerAdapter() {
        if (this.exceptionHandlerAdapter == null) {
            this.exceptionHandlerAdapter = new NettyTransmissionChannelInboundHandlerAdapterClientException<Configuration, ProcessRequest>();
            this.exceptionHandlerAdapter.initialization(this.configuration, this.handlerAdapter.getProcessRequest());
        }
        return this.exceptionHandlerAdapter;
    }

    @Override
    public void closeConnection() {
        if (this.channel != null) {
            this.channel.close();
        }
    }

    /**
     * delay上this.configuration.getReestablishConnectionInterval()毫秒後重新恢復連線
     */
    public void scheduleToReestablishConnection() {
        if (this.channel != null) {
            this.scheduleToReestablishConnection(this.channel.newPromise());
        }
    }

    /**
     * 獲取統計數據
     *
     * @return
     */
    @Override
    public NettyEventExecutorData getNettyEventExecutorData() {
        this.putMDC(this.channel);
        NettyEventExecutorData data = null;
        if (this.bisEventExecutorGroup != null && this.bisEventExecutorGroup instanceof NettyTransmissionEventExecutorGroup) {
            data = new NettyEventExecutorData();
            data.setExecutorSetupCount(((NettyTransmissionEventExecutorGroup) this.bisEventExecutorGroup).executorSetupCount());
            data.setExecutorActiveCount(((NettyTransmissionEventExecutorGroup) this.bisEventExecutorGroup).executorActiveCount());
            data.setExecutorIdleCount(((NettyTransmissionEventExecutorGroup) this.bisEventExecutorGroup).executorIdleCount());
            data.setExecutorConfigurationCount(this.configuration.getBisThreadNum());
            NettyTransmissionUtil.infoMessage(this.channel, "[getNettyEventExecutorData]NettyEventExecutorData:", data);
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
        this.putMDC(this.channel);
        if (this.bisEventExecutorGroup != null && this.bisEventExecutorGroup instanceof NettyTransmissionEventExecutorGroup) {
            if (nThreads <= 0) {
                nThreads = this.configuration.getBisThreadNum();
            }
            NettyTransmissionUtil.infoMessage(this.channel, "[setNettyEventExecutorThreads]nThreads:", nThreads);
            return ((NettyTransmissionEventExecutorGroup) this.bisEventExecutorGroup).setExecutorThreads(nThreads);
        }
        return false;
    }

    public AtomicInteger getEstablishConnectionTimes() {
        return establishConnectionTimes;
    }

    /**
     * 判斷是否為同一個Channel
     *
     * @param channel
     * @return
     */
    public boolean equals(Channel channel) {
        return this.channel != null && this.channel.equals(channel);
    }
}
