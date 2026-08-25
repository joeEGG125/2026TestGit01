package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Calendar;

public class NettyTransmissionChannelInformation implements Serializable {
    private static final long serialVersionUID = -1L;

    private String remoteHostName;
    private String remoteIp;
    private int remotePort;
    private String localHostName;
    private String localIp;
    private int localPort;
    private String longChannelId;
    private String shortChannelId;
    private Calendar onlineDateTime; // 在線時間
    private Calendar offlineDateTime; // 離線時間
    private Calendar lastActiveDateTime; // 最後一次活躍時間

    public NettyTransmissionChannelInformation setChannel(Channel channel) {
        if (channel == null)
            return this;
        InetSocketAddress remoteAddr = (InetSocketAddress) channel.remoteAddress();
        InetSocketAddress localAddr = (InetSocketAddress) channel.localAddress();
        if (remoteAddr != null) {
            // 2023/05/26 Richard modified start 這裡不要取hostName, Linux環境下會非常耗時會卡住
            // this.remoteHostName = remoteAddr.getHostName();
            // 2023/05/26 Richard modified end 這裡不要取hostName, Linux環境下會非常耗時會卡住
            // this.remoteIp = remoteAddr.getAddress().getHostAddress();
            this.remoteIp = ReflectUtil.envokeMethod(remoteAddr.getAddress(), "getHostAddress", StringUtils.EMPTY);
            this.remotePort = remoteAddr.getPort();
        }
        if (localAddr != null) {
            // this.localHostName = localAddr.getHostName();
            // this.localIp = localAddr.getAddress().getHostAddress();
            this.localHostName = FEPConfig.getInstance().getHostName();
            this.localIp = FEPConfig.getInstance().getHostIp();
            this.localPort = localAddr.getPort();
        }
        if (channel.id() != null) {
            this.longChannelId = channel.id().asLongText();
            this.shortChannelId = channel.id().asShortText();
        }
        return this;
    }

    public String getRemoteHostName() {
        return remoteHostName;
    }

    public void setRemoteHostName(String remoteHostName) {
        this.remoteHostName = remoteHostName;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getLocalHostName() {
        return localHostName;
    }

    public void setLocalHostName(String localHostName) {
        this.localHostName = localHostName;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getLongChannelId() {
        return longChannelId;
    }

    public void setLongChannelId(String id) {
        this.longChannelId = id;
    }

    public String getShortChannelId() {
        return shortChannelId;
    }

    public void setShortChannelId(String shortChannelId) {
        this.shortChannelId = shortChannelId;
    }

    public Calendar getOnlineDateTime() {
        return onlineDateTime;
    }

    public void setOnlineDateTime(Calendar onlineDateTime) {
        this.onlineDateTime = onlineDateTime;
    }

    public Calendar getOfflineDateTime() {
        return offlineDateTime;
    }

    public void setOfflineDateTime(Calendar offlineDateTime) {
        this.offlineDateTime = offlineDateTime;
    }

    public Calendar getLastActiveDateTime() {
        return lastActiveDateTime;
    }

    public void setLastActiveDateTime(Calendar lastActiveDateTime) {
        this.lastActiveDateTime = lastActiveDateTime;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
