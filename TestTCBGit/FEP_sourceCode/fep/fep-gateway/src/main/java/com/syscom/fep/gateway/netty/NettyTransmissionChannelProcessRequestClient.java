package com.syscom.fep.gateway.netty;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.PostConstruct;

public abstract class NettyTransmissionChannelProcessRequestClient<Configuration extends NettyTransmissionClientConfiguration> extends NettyTransmissionChannelProcessRequest<Configuration> {
    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     */
    @Override
    public void initialization(Configuration configuration) {
        super.initialization(configuration);
        this.initNettyChannelProcessRequest();
    }

    @PostConstruct
    public void initNettyChannelProcessRequest() {
        this.notification.addConnStateListener(this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    @Override
    public void closeConnection() {
        putMDC();
        this.notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), this.channelHandlerContext.channel(), NettyTransmissionConnState.CLIENT_DISCONNECTED);
    }

    /**
     * 從notification移除連線狀態監聽器, 避免出現Memory Leak
     */
    public final void removeConnStateListener() {
        this.notification.removeConnStateListener(this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state, t);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
        if (state == NettyTransmissionConnState.CLIENT_CONNECTING) {
            this.logContext.setRemark(
                    StringUtils.join("[", configuration.getSocketType(),
                            " IP:", configuration.getHost(),
                            ",Port:", configuration.getPort(),
                            "] connecting..."));
            this.logMessage(this.logContext);
        } else if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
            this.logContext.setRemark(
                    StringUtils.join("[", configuration.getSocketType(),
                            " IP:", configuration.getHost(),
                            ",Port:", configuration.getPort(),
                            ",LocalPort:", channelInformation.getLocalPort(),
                            "] Connected OK"));
            this.logMessage(this.logContext);
        } else if (state == NettyTransmissionConnState.CLIENT_DISCONNECTED) {
            this.logContext.setRemark(
                    StringUtils.join("[", configuration.getSocketType(),
                            " IP:", configuration.getHost(),
                            ",Port:", configuration.getPort(),
                            ",LocalPort:", channelInformation.getLocalPort(),
                            "] Disconnected", t != null ? StringUtils.join(" with exception occur ", t.getMessage()) : StringUtils.EMPTY));
            this.logMessage(this.logContext);
        }
    }
}
