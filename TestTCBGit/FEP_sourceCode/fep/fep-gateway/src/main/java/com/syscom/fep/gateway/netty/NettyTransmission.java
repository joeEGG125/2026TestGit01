package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.netty.NettyEventExecutorDataHandler;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.configuration.GatewayManager;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public abstract class NettyTransmission<Configuration extends NettyTransmissionConfiguration, HandlerAdapter extends NettyTransmissionChannelInboundHandlerAdapter<Configuration>, ProcessRequest extends NettyTransmissionChannelProcessRequest<Configuration>>
        extends FEPBase implements NettyTransmissionConnStateListener, NettyEventExecutorDataHandler {
    @Autowired
    protected GatewayManager manager;
    @Autowired
    protected Configuration configuration;
    @Autowired
    protected HandlerAdapter handlerAdapter;
    @Autowired
    protected NettyTransmissionNotification notification;

    @PostConstruct
    public void initNettyTransmission() {
        this.putMDC(null);
        manager.addTransmission(this);
    }

    public void run() {
        this.run(true);
    }

    public void run(boolean establishConnection) {
        this.putMDC(null);
        this.initData();
        this.printConfiguration();
        this.initialization();
        if (establishConnection)
            this.establishConnection();
    }

    /**
     * 專門用於非SpringBean模式下啟動Gateway
     *
     * @param configuration
     * @param handlerAdapter
     */
    public void initialization(Configuration configuration, HandlerAdapter handlerAdapter) {
        this.manager = SpringBeanFactoryUtil.getBean(GatewayManager.class);
        this.notification = SpringBeanFactoryUtil.getBean(NettyTransmissionNotification.class);
        this.configuration = configuration;
        this.configuration.initialization();
        this.handlerAdapter = handlerAdapter;
        this.initNettyTransmission();
    }

    protected void putMDC(Channel channel) {
        NettyTransmissionUtil.putMDC(channel, this.configuration);
    }

    protected abstract void initData();

    protected abstract void initialization();

    public abstract void establishConnection();

    public abstract void closeConnection();

    @PreDestroy
    public abstract void terminateConnection();

    protected void printConfiguration() {
        LogHelperFactory.getGeneralLogger().info(configuration);
    }

    /**
     * 由子類去實作電文解碼器, 用來處理一些特殊的電文
     *
     * @return
     */
    protected ByteToMessageDecoder getByteToMessageDecoder() {
        return null;
    }

    /**
     * 有新的Channel時, 初始化動作
     *
     * @param ch
     */
    protected void channelInitialization(SocketChannel ch) {}
}
