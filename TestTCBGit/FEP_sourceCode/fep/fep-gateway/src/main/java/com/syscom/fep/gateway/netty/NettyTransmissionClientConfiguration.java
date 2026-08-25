package com.syscom.fep.gateway.netty;

import com.syscom.fep.frmcommon.ssl.SslKeyTrust;

public class NettyTransmissionClientConfiguration extends NettyTransmissionConfiguration {
    private long readerIdleTime = 0L;
    private long writerIdleTime = 120000L;
    private SslKeyTrust sslConfig;

    public long getReaderIdleTime() {
        return readerIdleTime;
    }

    public void setReaderIdleTime(long readerIdleTime) {
        this.readerIdleTime = readerIdleTime;
    }

    public long getWriterIdleTime() {
        return writerIdleTime;
    }

    public void setWriterIdleTime(long writerIdleTime) {
        this.writerIdleTime = writerIdleTime;
    }

    public SslKeyTrust getSslConfig() {
        return sslConfig;
    }

    public void setSslConfig(SslKeyTrust sslConfig) {
        this.sslConfig = sslConfig;
    }
}
