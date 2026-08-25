package com.syscom.fep.invoker.netty;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.net.InetSocketAddress;
import java.util.Map;

public class SimpleNettyBaseMethod extends FEPBase {

    /**
     * 程式或者服務名稱
     *
     * @return
     */
    public String getName() {
        String mdcProfile = LogMDC.get(Const.MDC_PROFILE);
        return StringUtils.isNotBlank(mdcProfile) ? mdcProfile : ProgramName;
    }

    protected void putMDC(Channel channel) {
        LogMDC.put(LogMDC.getAll(Const.MDC_KEPT));
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(!getChannelLoggingDisable(channel)));
        if (channel != null) {
            AttributeKey<Map<String, String>> key = AttributeKey.valueOf(SimpleNettyAttributeKey.MDCMap.name());
            if (channel.hasAttr(key)) {
                Attribute<Map<String, String>> attr = channel.attr(key);
                LogMDC.put(attr.get());
            }
        }
    }

    protected void putKeptMDC(Channel channel) {
        if (channel == null) return;
        AttributeKey<Map<String, String>> key = AttributeKey.valueOf(SimpleNettyAttributeKey.MDCMap.name());
        Attribute<Map<String, String>> attr = channel.attr(key);
        attr.set(LogMDC.getAll(Const.MDC_KEPT));
    }

    @Override
    protected void infoMessage(Object... messages) {
        this.infoMessage(null, messages);
    }

    public void infoMessage(Channel channel, Object... messages) {
        super.infoMessage("[", getName(), "]", channelInfo(channel), StringUtils.join(messages));
    }

    @Override
    protected void debugMessage(Object... messages) {
        this.debugMessage(null, messages);
    }

    public void debugMessage(Channel channel, Object... messages) {
        super.debugMessage("[", getName(), "]", channelInfo(channel), StringUtils.join(messages));
    }

    @Override
    protected void warnMessage(Object... messages) {
        this.warnMessage((Channel) null, messages);
    }

    public void warnMessage(Channel channel, Object... messages) {
        super.warnMessage("[", getName(), "]", channelInfo(channel), StringUtils.join(messages));
    }

    @Override
    protected void warnMessage(Throwable t, Object... messages) {
        this.warnMessage((Channel) null, t, messages);
    }

    public void warnMessage(Channel channel, Throwable t, Object... messages) {
        super.warnMessage(t, "[", getName(), "]", channelInfo(channel), StringUtils.join(messages));
    }

    @Override
    protected void errorMessage(Throwable t, Object... messages) {
        this.errorMessage(null, t, messages);
    }

    public void errorMessage(Channel channel, Throwable t, Object... messages) {
        super.errorMessage(t, "[", getName(), "]", channelInfo(channel), StringUtils.join(messages));
    }

    public static String channelInfo(Channel channel) {
        if (channel == null)
            return "|||||||";
        StringBuilder sb = new StringBuilder();
        sb.append("|").append(getRemoteIp(channel));
        sb.append("|").append(getRemotePort(channel));
        sb.append("|").append(FEPConfig.getInstance().getHostName());
        sb.append("|").append(FEPConfig.getInstance().getHostIp());
        sb.append("|").append(channel.localAddress() != null ? ((InetSocketAddress) channel.localAddress()).getPort() : 0);
        sb.append("|").append(channel.id() != null ? channel.id().asShortText() : StringUtils.EMPTY);
        sb.append("|");
        return sb.toString();
    }

    public static String getRemoteIp(Channel channel) {
        InetSocketAddress remoteAddr = (InetSocketAddress) channel.remoteAddress();
        return remoteAddr != null ? ReflectUtil.envokeMethod(remoteAddr.getAddress(), "getHostAddress", StringUtils.EMPTY) : StringUtils.EMPTY;
    }

    public static int getRemotePort(Channel channel) {
        InetSocketAddress remoteAddr = (InetSocketAddress) channel.remoteAddress();
        return remoteAddr != null ? remoteAddr.getPort() : -1;
    }

    public static String getLocalIp() {
        return FEPConfig.getInstance().getHostIp();
    }

    public static int getLocalPort(Channel channel) {
        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
        return localAddress != null ? localAddress.getPort() : -1;
    }

    public static void logMessage(Level level, String name, Channel channel, Object... messages) {
        String message = StringUtils.join("[", name, "]", channelInfo(channel), StringUtils.join(messages));
        logMessage(level, null, message);
    }

    public static void logMessage(Level level, String name, Channel channel, Throwable t, Object... messages) {
        String message = StringUtils.join("[", name, "]", channelInfo(channel), StringUtils.join(messages));
        logMessage(level, t, message);
    }

    public static byte[] toBytes(ByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    public static ByteBuf toByteBuf(byte[] bytes) {
        return Unpooled.wrappedBuffer(bytes);
    }

    /**
     * 設定不記錄log
     *
     * @param channel
     * @param disable
     */
    public void setChannelLoggingDisable(Channel channel, boolean disable) {
        if (channel == null) return;
        AttributeKey<Boolean> key = AttributeKey.valueOf(SimpleNettyAttributeKey.CHANNEL_LOGGING_DISABLE.name());
        Attribute<Boolean> attr = channel.attr(key);
        attr.set(disable);
        warnMessage(channel, "[setChannelLoggingDisable]Channel was set to ", disable ? "disable" : "enable", " logging");
    }

    /**
     * 獲取不記錄log
     *
     * @param channel
     * @return
     */
    public boolean getChannelLoggingDisable(Channel channel) {
        if (channel == null) return false;
        AttributeKey<Boolean> key = AttributeKey.valueOf(SimpleNettyAttributeKey.CHANNEL_LOGGING_DISABLE.name());
        if (channel.hasAttr(key)) {
            Attribute<Boolean> attr = channel.attr(key);
            return attr != null && attr.get() != null && attr.get();
        }
        return false;
    }

    /**
     * 設定Channel被拒
     *
     * @param channel
     * @param rejected
     */
    public void setChannelReject(Channel channel, boolean rejected) {
        if (channel == null) return;
        AttributeKey<Boolean> key = AttributeKey.valueOf(SimpleNettyAttributeKey.CHANNEL_REJECTED.name());
        Attribute<Boolean> attr = channel.attr(key);
        attr.set(rejected);
        warnMessage(channel, "[setChannelReject]Channel was set to ", rejected ? "rejected" : "allow", " to connect to Server");
    }

    /**
     * 獲取Channel是否被拒
     *
     * @param channel
     * @return
     */
    public boolean getChannelReject(Channel channel) {
        if (channel == null) return false;
        AttributeKey<Boolean> key = AttributeKey.valueOf(SimpleNettyAttributeKey.CHANNEL_REJECTED.name());
        if (channel.hasAttr(key)) {
            Attribute<Boolean> attr = channel.attr(key);
            return attr != null && attr.get() != null && attr.get();
        }
        return false;
    }
}
