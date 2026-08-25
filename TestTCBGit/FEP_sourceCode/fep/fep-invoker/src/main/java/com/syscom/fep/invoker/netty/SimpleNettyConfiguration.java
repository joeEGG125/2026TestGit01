package com.syscom.fep.invoker.netty;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ssl.SslKeyTrust;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;

// @Validated
public class SimpleNettyConfiguration extends SslKeyTrust {
    private String NettyTransmissionNotificationKey;
    private String hostName;
    private String host;
    private int port;
    private int tcpKeepIdle = 120;
    private int tcpKeepInterval = 10;
    private int tcpKeepCount = 0;
    private long reestablishConnectionInterval = 5000L;
    private boolean sslNeedClientAuth;
    private boolean sslWantClientAuth;
    private int rcvBufAllocator = 4192;
    private int soSndBuf = 4192;
    private int soRcvBuf = 4192;
    private boolean soKeepalive = true;
    private boolean tcpNodelay = true;
    private boolean useDynamicEventExecutorGroup = false;
    private long dynamicEventExecutorIdleCheckInterval = 600000L;
    private long handshakeTimeoutMillis = 10000;
    private int connectTimeoutMillis = 5000;

    public String getNettyTransmissionNotificationKey() {
        if (NettyTransmissionNotificationKey == null)
            NettyTransmissionNotificationKey = UUIDUtil.randomUUID(true);
        return NettyTransmissionNotificationKey;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTcpKeepIdle() {
        return tcpKeepIdle;
    }

    public void setTcpKeepIdle(int tcpKeepIdle) {
        this.tcpKeepIdle = tcpKeepIdle;
    }

    public int getTcpKeepInterval() {
        return tcpKeepInterval;
    }

    public void setTcpKeepInterval(int tcpKeepInterval) {
        this.tcpKeepInterval = tcpKeepInterval;
    }

    public int getTcpKeepCount() {
        return tcpKeepCount;
    }

    public void setTcpKeepCount(int tcpKeepCount) {
        this.tcpKeepCount = tcpKeepCount;
    }

    public long getReestablishConnectionInterval() {
        return reestablishConnectionInterval;
    }

    public void setReestablishConnectionInterval(long reestablishConnectionInterval) {
        this.reestablishConnectionInterval = reestablishConnectionInterval;
    }

    public boolean isSslNeedClientAuth() {
        return sslNeedClientAuth;
    }

    public void setSslNeedClientAuth(boolean sslNeedClientAuth) {
        this.sslNeedClientAuth = sslNeedClientAuth;
    }

    public boolean isSslWantClientAuth() {
        return sslWantClientAuth;
    }

    public void setSslWantClientAuth(boolean sslWantClientAuth) {
        this.sslWantClientAuth = sslWantClientAuth;
    }

    public int getRcvBufAllocator() {
        return rcvBufAllocator;
    }

    public void setRcvBufAllocator(int rcvBufAllocator) {
        this.rcvBufAllocator = rcvBufAllocator;
    }

    public int getSoSndBuf() {
        return soSndBuf;
    }

    public void setSoSndBuf(int soSndBuf) {
        this.soSndBuf = soSndBuf;
    }

    public int getSoRcvBuf() {
        return soRcvBuf;
    }

    public void setSoRcvBuf(int soRcvBuf) {
        this.soRcvBuf = soRcvBuf;
    }

    public boolean isSoKeepalive() {
        return soKeepalive;
    }

    public void setSoKeepalive(boolean soKeepalive) {
        this.soKeepalive = soKeepalive;
    }

    public boolean isTcpNodelay() {
        return tcpNodelay;
    }

    public void setTcpNodelay(boolean tcpNodelay) {
        this.tcpNodelay = tcpNodelay;
    }

    public boolean isUseDynamicEventExecutorGroup() {
        return useDynamicEventExecutorGroup;
    }

    public void setUseDynamicEventExecutorGroup(boolean useDynamicEventExecutorGroup) {
        this.useDynamicEventExecutorGroup = useDynamicEventExecutorGroup;
    }

    public long getDynamicEventExecutorIdleCheckInterval() {
        return dynamicEventExecutorIdleCheckInterval;
    }

    public void setDynamicEventExecutorIdleCheckInterval(long dynamicEventExecutorIdleCheckInterval) {
        this.dynamicEventExecutorIdleCheckInterval = dynamicEventExecutorIdleCheckInterval;
    }

    public long getHandshakeTimeoutMillis() {
        return handshakeTimeoutMillis;
    }

    public void setHandshakeTimeoutMillis(long handshakeTimeoutMillis) {
        this.handshakeTimeoutMillis = handshakeTimeoutMillis;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public String getEndPoint() {
        return StringUtils.join(host, ":", port);
    }

    @PostConstruct
    public void print() {
        String msg = StringUtils.EMPTY;
        ConfigurationProperties annotation2 = this.getClass().getAnnotation(ConfigurationProperties.class);
        if (annotation2 != null) {
            String prefix = annotation2.prefix();
            List<Field> fieldList = ReflectUtil.getAllFields(this);
            if (CollectionUtils.isNotEmpty(fieldList)) {
                int repeat = 2;
                // 列印配置檔內容
                StringBuilder sb = new StringBuilder();
                sb.append(this.getClass().getSimpleName()).append(":\r\n");
                for (Field field : fieldList) {
                    ReflectionUtils.makeAccessible(field);
                    sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                            .append(prefix).append(".")
                            .append(field.getName())
                            .append(" = ").append(ReflectionUtils.getField(field, this)).append("\r\n");
                }
                msg = sb.toString();
            }
        } else {
            msg = StringUtils.join(this.getClass().getSimpleName(), ":\r\n", ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE));
        }
        LogHelperFactory.getGeneralLogger().info(msg);
    }
}
