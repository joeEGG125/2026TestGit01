package com.syscom.fep.gateway.netty.fisc.client.receiver;

import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.fisc.client.FISCGatewayClientConfiguration;
import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;

import jakarta.annotation.PostConstruct;

// @ConfigurationProperties(prefix = "spring.fep.gateway.transmission.fisc.receiver")
// @RefreshScope
public class FISCGatewayClientReceiverConfiguration extends FISCGatewayClientConfiguration {
    /**
     * 多久沒收到財金來的電文時需要斷線重連的Timer
     */
    private long disConnectInterval = 30000L;
    /**
     * 事件循環線程數量，預設值為CPU核心數*2
     */
    private int eventLoopThreads = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));

    /**
     * 專門用於非SpringBean模式下初始化
     */
    @Override
    public void initialization() {
        super.initialization();
        this.initReceiver();
    }

    @PostConstruct
    public void initReceiver() {
        super.setSocketType(SocketType.Receiver);
    }

    /**
     * @return
     */
    @Override
    public final SocketType getSocketType() {
        return super.getSocketType();
    }

    /**
     * @param socketType
     */
    @Override
    public final void setSocketType(SocketType socketType) {
    }

    public long getDisConnectInterval() {
        return disConnectInterval;
    }

    public void setDisConnectInterval(long disConnectInterval) {
        this.disConnectInterval = disConnectInterval;
    }

    public int getEventLoopThreads() {
        return eventLoopThreads;
    }

    public void setEventLoopThreads(int eventLoopThreads) {
        this.eventLoopThreads = eventLoopThreads;
    }
}