package com.syscom.fep.gateway.netty.fisc;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.netty.*;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiver;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverChannelInboundHandlerAdapter;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverConfiguration;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverProcessRequest;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSender;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderChannelInboundHandlerAdapter;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderConfiguration;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderProcessRequest;
import com.syscom.fep.gateway.netty.fisc.server.*;
import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 財經Socket物件組
 * 一個Sender
 * 一個Receiver
 * 一個Server
 *
 * @author Richard
 */
public class FISCGatewayGroup extends FEPBase implements NettyTransmissionConnStateListener {
    private final FISCGatewayMode mode;
    private final FISCGatewayClientSenderConfiguration senderConfiguration;
    private final FISCGatewayClientReceiverConfiguration receiverConfiguration;
    private final FISCGatewayServerConfiguration serverConfiguration;
    private FISCGatewayClientSender sender;
    private FISCGatewayClientReceiver receiver;
    private FISCGatewayServer server;
    private FISCGatewayClientSenderProcessRequest senderProcessRequest;
    private final NettyTransmissionNotification notification = SpringBeanFactoryUtil.getBean(NettyTransmissionNotification.class);
    private final Object lockConnStateChanged = new Object(), lockServer = new Object();
    private final AtomicBoolean senderScheduleToReestablishConnection = new AtomicBoolean(false); // 判斷是否需要
    private final FISCGatewayGroupConfiguration groupCfg;

    public FISCGatewayGroup(FISCGatewayMode mode, FISCGatewayGroupConfiguration groupCfg, FISCGatewayClientSenderConfiguration senderConfiguration, FISCGatewayClientReceiverConfiguration receiverConfiguration, FISCGatewayServerConfiguration serverConfiguration) {
        this.mode = mode;
        this.groupCfg = groupCfg;
        this.senderConfiguration = this.overwriteProperty(senderConfiguration);
        this.receiverConfiguration = this.overwriteProperty(receiverConfiguration);
        this.serverConfiguration = serverConfiguration;
    }

    private <T extends NettyTransmissionClientConfiguration> T overwriteProperty(T configuration) {
        if (this.groupCfg.isDisconnectAllAfterDisconnectAnyPort()) {
            configuration.setAutoReestablishConnection(false); // 設置不要自動重連, 後面會手動重連
        }
        return configuration;
    }

    public FISCGatewayMode getMode() {
        return mode;
    }

    /**
     * 運行
     */
    public void run(boolean postConstruct) {
        this.logContext.clear();
        // 啟動sender
        this.runSender(this.senderConfiguration, postConstruct);
        // 啟動receiver
        // 如果斷開任意port則斷全部, 則依次啟動port, 先啟動sender, sender連線成功後再啟動receiver
        if (!this.groupCfg.isDisconnectAllAfterDisconnectAnyPort()) {
            this.runReceiver(this.receiverConfiguration, postConstruct);
        }
    }

    /**
     * 停止
     */
    public void stop() {
        // 移除掉狀態監聽
        notification.removeConnStateListener(this.senderConfiguration.getNettyTransmissionNotificationKey(), this);
        notification.removeConnStateListener(this.receiverConfiguration.getNettyTransmissionNotificationKey(), this);
        // 停止server
        this.stopServer();
        // 停止receiver
        if (this.receiver != null) {
            this.receiver.terminateConnection();
            this.receiver = null;
        }
        // 停止sender
        if (this.sender != null) {
            this.sender.terminateConnection();
            this.sender = null;
        }
    }

    /**
     * 啟動sender建立與財經的連線
     *
     * @param configuration
     * @param postConstruct
     */
    private void runSender(FISCGatewayClientSenderConfiguration configuration, boolean postConstruct) {
        // Processor, 必須
        this.senderProcessRequest = new FISCGatewayClientSenderProcessRequest();
        this.senderProcessRequest.initialization(configuration);
        // HandlerAdapter, 必須
        FISCGatewayClientSenderChannelInboundHandlerAdapter adapter = new FISCGatewayClientSenderChannelInboundHandlerAdapter();
        adapter.initialization(configuration, this.senderProcessRequest);
        // Gateway, 必須
        this.sender = new FISCGatewayClientSender();
        this.sender.initialization(configuration, adapter);
        // 監聽sender的狀態
        notification.addConnStateListener(configuration.getNettyTransmissionNotificationKey(), this);
        // postConstruct下會自動啟動Gateway程式, 所以這裡判斷一下, 只有在非postConstruct下才需要呼叫run方法
        if (!postConstruct) {
            this.sender.run();
        }
    }

    /**
     * 啟動receiver建立與財經的連線
     *
     * @param configuration
     * @param postConstruct
     */
    private void runReceiver(FISCGatewayClientReceiverConfiguration configuration, boolean postConstruct) {
        // Processor, 必須
        FISCGatewayClientReceiverProcessRequest processRequest = new FISCGatewayClientReceiverProcessRequest();
        processRequest.initialization(configuration);
        // HandlerAdapter, 必須
        FISCGatewayClientReceiverChannelInboundHandlerAdapter adapter = new FISCGatewayClientReceiverChannelInboundHandlerAdapter();
        adapter.initialization(configuration, processRequest);
        // Gateway, 必須
        this.receiver = new FISCGatewayClientReceiver();
        this.receiver.initialization(configuration, adapter);
        // 監聽receiver的狀態
        notification.addConnStateListener(configuration.getNettyTransmissionNotificationKey(), this);
        // postConstruct下會自動啟動Gateway程式, 所以這裡判斷一下, 只有在非postConstruct下才需要呼叫run方法
        if (!postConstruct) {
            this.receiver.run();
        }
    }

    /**
     * 啟動server接收FISCAdapter丟過來的電文
     *
     * @param configuration
     */
    private void runServer(FISCGatewayServerConfiguration configuration) {
        synchronized (lockServer) {
            // ProcessorManager, 必須
            FISCGatewayServerProcessRequestManager manager = new FISCGatewayServerProcessRequestManager();
            manager.initialization(configuration, this.senderProcessRequest);
            // HandlerAdapter, 必須
            FISCGatewayServerChannelInboundHandlerAdapter adapter = new FISCGatewayServerChannelInboundHandlerAdapter();
            adapter.initialization(configuration, manager);
            // IP過濾規則, 必須
            FISCGatewayServerRuleIpFilter ruleIpFilter = new FISCGatewayServerRuleIpFilter();
            ruleIpFilter.initialization(configuration);
            // Gateway, 必須
            this.server = new FISCGatewayServer();
            this.server.initialization(configuration, adapter, manager, ruleIpFilter);
            this.server.run();
        }
    }

    /**
     * 停止server
     */
    public void stopServer() {
        synchronized (lockServer) {
            if (this.server != null) {
                this.server.terminateConnection();
                this.server = null;
            }
        }
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        synchronized (lockConnStateChanged) {
            // 當sender和receiver都成功連到財經時, 才啟動server
            if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
                // 如果斷開任意port則斷全部, 則依次啟動port, 先啟動sender, sender連線成功後再啟動receiver
                if (this.groupCfg.isDisconnectAllAfterDisconnectAnyPort()) {
                    senderScheduleToReestablishConnection.set(false); // 確保sender可以重新嘗試恢復連線
                    // 如果是sender連線成功, 則啟動receiver
                    if (this.sender != null && this.sender.equals(channel)) {
                        // 如果receiver還沒有連線過, 說明沒有初始化, 則進行連線
                        if (this.receiver == null) {
                            LogHelperFactory.getGeneralLogger().info("Sender has been connected to FISC, Receiver start to run and build connection to FISC");
                            this.runReceiver(this.receiverConfiguration, false);
                        }
                        // 如果receiver有連線過, 則只需要建立連線
                        else if (!NettyTransmissionConnState.isClientConnected(this.receiver.getTransmissionClientMonitor().getConnState())) {
                            LogHelperFactory.getGeneralLogger().info("Sender has been connected to FISC, Receiver start to build connection to FISC");
                            this.receiver.establishConnection();
                        }
                    }
                    // 如果是receiver連線成功, 則啟動server
                    else if (this.receiver != null && this.receiver.equals(channel)) {
                        this.runServer();
                    }
                } else {
                    if (sender != null && NettyTransmissionConnState.isClientConnected(sender.getTransmissionClientMonitor().getConnState())
                            && receiver != null && NettyTransmissionConnState.isClientConnected(receiver.getTransmissionClientMonitor().getConnState())) {
                        this.runServer();
                    }
                }
            } else if (state == NettyTransmissionConnState.CLIENT_CONNECTING_FAILED) {
                if (this.groupCfg.isDisconnectAllAfterDisconnectAnyPort()) {
                    // 當有一個腳位重新建立連線失敗時, 則所有腳位都斷開
                    senderScheduleToReestablishConnection.set(false);
                    this.connStateChanged(channel, NettyTransmissionConnState.CLIENT_DISCONNECTED, t);
                }
            } else if (state == NettyTransmissionConnState.CLIENT_DISCONNECTED) {
                // 當有一個腳位斷開時, 則所有腳位都斷開
                if (this.groupCfg.isDisconnectAllAfterDisconnectAnyPort()) {
                    LogHelperFactory.getGeneralLogger().warn("Cut all connection from FISC");
                    this.closeConnection(this.sender);
                    this.closeConnection(this.receiver);
                    // 重新啟動sender的連線
                    if (!senderScheduleToReestablishConnection.get()) {
                        LogHelperFactory.getGeneralLogger().warn("Sender schedule to reestablish connection FISC");
                        senderScheduleToReestablishConnection.set(true);
                        this.sender.scheduleToReestablishConnection();
                    }
                }
            }
        }
    }

    private void runServer() {
        if (this.server == null) {
            LogHelperFactory.getGeneralLogger().info("Both Sender and Receiver has been connected to FISC, start to build Socket Server connection for FISCAdapter");
            this.runServer(this.serverConfiguration);
        }
    }

    /**
     * 斷開對應腳位的連線, 注意會先判斷是否有正在連線中, 如果有則進行斷線處理
     *
     * @param client
     */
    private void closeConnection(NettyTransmissionClient<?, ?, ?> client) {
        if (client != null && NettyTransmissionConnState.isClientConnected(client.getTransmissionClientMonitor().getConnState())) {
            client.closeConnection();
        }
    }

    public FISCGatewayClientSender getSender() {
        return sender;
    }

    public FISCGatewayClientReceiver getReceiver() {
        return receiver;
    }

    public FISCGatewayServer getServer() {
        return server;
    }
}
