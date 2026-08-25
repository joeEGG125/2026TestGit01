package com.syscom.fep.gateway.netty;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

public class NettyTransmissionServerMonitor<Configuration extends NettyTransmissionConfiguration> {
    private Configuration configuration;
    // 服務名稱
    private String serviceName = StringUtils.EMPTY;
    // 服務主機IP
    private String hostIp = StringUtils.EMPTY;
    // 使用連接埠
    private int hostPort;
    // 目前連接數
    private final AtomicLong connections = new AtomicLong(0L);
    // 最後一次活躍時間
    private Calendar latestActiveDateTime;

    public void setTransmissionConfiguration(Configuration configuration) {
        this.configuration = configuration;
        this.serviceName = configuration.getGateway().name();
        this.hostIp = configuration.getHost();
        this.hostPort = configuration.getPort();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getHostIp() {
        return hostIp;
    }

    public int getHostPort() {
        return hostPort;
    }

    public long getConnections() {
        return connections.get();
    }

    public void setConnections(long connections) {
        this.connections.set(connections);
    }

    public Calendar getLatestActiveDateTime() {
        return latestActiveDateTime;
    }

    public void setLatestActiveDateTime(Calendar latestActiveDateTime) {
        this.latestActiveDateTime = latestActiveDateTime;
    }

    public long incrementAndGetConnections() {
        return this.connections.incrementAndGet();
    }

    public long decrementAndGetConnections() {
        return this.connections.decrementAndGet();
    }
}
