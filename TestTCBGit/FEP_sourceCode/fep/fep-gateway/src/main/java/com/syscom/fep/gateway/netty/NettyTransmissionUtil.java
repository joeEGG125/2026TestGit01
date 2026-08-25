package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.FEPBaseMethod;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.ssl.SslKeyTrust;
import com.syscom.fep.frmcommon.ssl.X509MultiTrustManager;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import javax.net.ssl.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NettyTransmissionUtil extends FEPBaseMethod {
    private NettyTransmissionUtil() {}

    public static SslHandler getSslHandler(SslKeyTrust sslKeyTrust, boolean needClientAuth, boolean wantClientAuth, boolean useClientMode) throws Exception {
        SSLEngine sslEngine = SslContextFactory.getSSLEngine(sslKeyTrust, needClientAuth, wantClientAuth, useClientMode);
        return sslEngine == null ? null : new SslHandler(sslEngine);
    }

    public static SslHandler getSsHandler(List<X509KeyManager> keyManagerList, List<X509TrustManager> trustManagerList,
                                          String certAlias, boolean needClientAuth, boolean useClientMode) throws Exception {
        if (CollectionUtils.isEmpty(keyManagerList) && CollectionUtils.isEmpty(trustManagerList))
            return null;
        KeyManager[] km = null;
        if (CollectionUtils.isNotEmpty(keyManagerList)) {
            km = new KeyManager[] {new NettyTransmissionSslX509MultiKeyManager(keyManagerList, certAlias, null)};
        }
        TrustManager[] tm = null;
        if (CollectionUtils.isNotEmpty(trustManagerList)) {
            tm = new TrustManager[] {new X509MultiTrustManager(trustManagerList)};
        }
        SslHandler sslHandler = null;
        SSLContext sslContext = SslContextFactory.getSslContext(km, tm);
        if (sslContext != null) {
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setNeedClientAuth(needClientAuth);
            sslEngine.setUseClientMode(useClientMode);
            sslHandler =  new SslHandler(sslEngine);
        }
        return sslHandler;
    }

    public static void putMDC(ChannelHandlerContext ctx, NettyTransmissionConfiguration configuration) {
        putMDC(ctx, configuration.getGateway(), configuration.getSocketType());
    }

    public static void putMDC(ChannelHandlerContext ctx, Gateway gateway, SocketType socketType) {
        putMDC(ctx != null ? ctx.channel() : null, gateway, socketType);
    }

    public static void putMDC(Channel channel, NettyTransmissionConfiguration configuration) {
        putMDC(channel, configuration.getGateway(), configuration.getSocketType());
    }

    public static void putMDC(Channel channel, Gateway gateway, SocketType socketType) {
        LogMDC.put(Const.MDC_GATEWAY, gateway.name());
        LogMDC.put(Const.MDC_PROFILE, gateway.name());
        LogMDC.put(Const.MDC_GATEWAY_SOCKET_TYPE, socketType.name());
        LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(!getChannelLoggingDisable(channel)));
    }

    public static CompletableFuture<NettyTransmissionWriteAndFlushResult> sendBytesMessage(Object invoker, NettyTransmissionConfiguration configuration, Channel channel, byte[] message) {
        CompletableFuture<NettyTransmissionWriteAndFlushResult> result = new CompletableFuture<>();
        if (channel != null && channel.isActive() && ArrayUtils.isNotEmpty(message)) {
            infoMessage(channel, Const.MESSAGE_OUT, StringUtil.toHex(message));
            // 注意這裡要將十六進制字串轉為bytes再轉為ByteBuf丟出去
            ChannelFuture future = channel.writeAndFlush(toByteBuf(message));
            future.addListener(f -> {
                putMDC(channel, configuration);
                if (f.isSuccess()) {
                    infoMessage(channel, StringUtil.toHex(message), " send Success");
                    result.complete(new NettyTransmissionWriteAndFlushResult(true));
                } else {
                    Throwable t = f.cause();
                    errorMessage(channel, t, "send message failed, message = [", StringUtil.toHex(message), "]");
                    LogData logData = new LogData();
                    logData.setProgramException(t);
                    logData.setProgramName(StringUtils.join(invoker.getClass().getSimpleName(), ".sendBytesMessage"));
                    sendEMS(logData);
                    result.complete(new NettyTransmissionWriteAndFlushResult(false, t));
                }
            });
        } else {
            if (channel != null && !channel.isActive()) {
                warnMessage(channel, "Cannot send message cause Inactive Channel!!!");
            }
            result.complete(new NettyTransmissionWriteAndFlushResult(false));
        }
        return result;
    }

    public static CompletableFuture<NettyTransmissionWriteAndFlushResult> sendHexMessage(Object invoker, NettyTransmissionConfiguration configuration, Channel channel, String message) {
        CompletableFuture<NettyTransmissionWriteAndFlushResult> result = new CompletableFuture<>();
        if (channel != null && channel.isActive() && message != null) {
            infoMessage(channel, Const.MESSAGE_OUT, message);
            // 注意這裡要將十六進制字串轉為bytes再轉為ByteBuf丟出去
            ChannelFuture future = channel.writeAndFlush(toByteBuf(ConvertUtil.hexToBytes(message)));
            future.addListener(f -> {
                putMDC(channel, configuration);
                if (f.isSuccess()) {
                    infoMessage(channel, message, " send Success");
                    result.complete(new NettyTransmissionWriteAndFlushResult(true));
                } else {
                    Throwable t = f.cause();
                    errorMessage(channel, t, "send message failed, message = [", message, "]");
                    LogData logData = new LogData();
                    logData.setProgramException(t);
                    logData.setProgramName(StringUtils.join(invoker.getClass().getSimpleName(), ".sendHexMessage"));
                    sendEMS(logData);
                    result.complete(new NettyTransmissionWriteAndFlushResult(false, t));
                }
            });
        } else {
            if (channel != null && !channel.isActive()) {
                warnMessage(channel, "Cannot send message cause Inactive Channel!!!");
            }
            result.complete(new NettyTransmissionWriteAndFlushResult(false));
        }
        return result;
    }

    public static CompletableFuture<NettyTransmissionWriteAndFlushResult> sendPlainMessage(Object invoker, NettyTransmissionConfiguration configuration, Channel channel, String message) {
        CompletableFuture<NettyTransmissionWriteAndFlushResult> result = new CompletableFuture<>();
        if (channel != null && channel.isActive() && message != null) {
            infoMessage(channel, Const.MESSAGE_OUT, message);
            ChannelFuture future = channel.writeAndFlush(toByteBuf(ConvertUtil.toBytes(message, StandardCharsets.UTF_8)));
            future.addListener(f -> {
                putMDC(channel, configuration);
                if (f.isSuccess()) {
                    infoMessage(channel, message, " send Success");
                    result.complete(new NettyTransmissionWriteAndFlushResult(true));
                } else {
                    Throwable t = f.cause();
                    errorMessage(channel, t, "send message failed, message = [", message, "]");
                    LogData logData = new LogData();
                    logData.setProgramException(t);
                    logData.setProgramName(StringUtils.join(invoker.getClass().getSimpleName(), ".sendPlainMessage"));
                    sendEMS(logData);
                    result.complete(new NettyTransmissionWriteAndFlushResult(false, t));
                }
            });
        } else {
            if (channel != null && !channel.isActive()) {
                warnMessage(channel, "Cannot send message cause Inactive Channel!!!");
            }
            result.complete(new NettyTransmissionWriteAndFlushResult(false));
        }
        return result;
    }

    public static NettyTransmissionChannelInformation getChannelInformation(NettyTransmissionChannelInformation channelInformation, Channel channel) {
        if (channelInformation == null) {
            channelInformation = new NettyTransmissionChannelInformation();
        }
        return channelInformation.setChannel(channel);
    }

    public static byte[] toBytes(ByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    public static ByteBuf toByteBuf(byte[] bytes) {
        return Unpooled.wrappedBuffer(bytes);
    }

    public static void infoMessage(Channel channel, Object... msgs) {
        logMessage(Level.INFO, channel, msgs);
    }

    public static void debugMessage(Channel channel, Object... msgs) {
        logMessage(Level.DEBUG, channel, msgs);
    }

    public static void warnMessage(Channel channel, Object... msgs) {
        warnMessage(channel, null, msgs);
    }

    public static void warnMessage(Channel channel, Throwable t, Object... msgs) {
        logMessage(Level.WARN, channel, t, msgs);
    }

    public static void errorMessage(Channel channel, Object... msgs) {
        errorMessage(channel, null, msgs);
    }

    public static void errorMessage(Channel channel, Throwable t, Object... msgs) {
        logMessage(Level.ERROR, channel, t, msgs);
    }

    public static void traceMessage(Channel channel, Object... msgs) {
        logMessage(Level.TRACE, channel, msgs);
    }

    private static void logMessage(Level level, Channel channel, Object... msgs) {
        logMessage(level, channel, null, msgs);
    }

    private static void logMessage(Level level, Channel channel, Throwable t, Object... msgs) {
        LogHelper logger = LogHelperFactory.getGatewayLogger();
        setLogContextField(channel);
        switch (level) {
            case ERROR:
                if (t == null) {
                    logger.error(msgs);
                } else {
                    logger.exceptionMsg(t, msgs);
                }
                break;
            case INFO:
                logger.info(msgs);
                break;
            case DEBUG:
                logger.debug(msgs);
                break;
            case WARN:
                if (t == null) {
                    logger.warn(msgs);
                } else {
                    logger.warn(t, msgs);
                }
                break;
            case TRACE:
                logger.trace(msgs);
                break;
        }
        FEPBase.clearMDC();
    }

    private static void setLogContextField(Channel channel) {
        NettyTransmissionChannelInformation information = getChannelInformation(new NettyTransmissionChannelInformation(), channel);
        LogMDC.put(Const.MDC_GATEWAY_REMOTE_IP, information.getRemoteIp());
        LogMDC.put(Const.MDC_GATEWAY_REMOTE_PORT, information.getRemotePort() == 0 ? StringUtils.EMPTY : String.valueOf(information.getRemotePort()));
        LogMDC.put(Const.MDC_GATEWAY_REMOTE_HOSTNAME, information.getRemoteHostName());
        LogMDC.put(Const.MDC_GATEWAY_LOCAL_IP, information.getLocalIp());
        LogMDC.put(Const.MDC_GATEWAY_LOCAL_PORT, information.getLocalPort() == 0 ? StringUtils.EMPTY : String.valueOf(information.getLocalPort()));
        LogMDC.put(Const.MDC_GATEWAY_LOCAL_HOSTNAME, information.getLocalHostName());
        LogMDC.put(Const.MDC_GATEWAY_CHANNEL_ID, information.getShortChannelId());
    }

    public static String getDateTimeStr() {
        // just for test
        // return StringUtils.join(", NOW:", FormatUtil.dateTimeInMillisFormat(System.currentTimeMillis()));
        return StringUtils.EMPTY;
    }

    /**
     * 設定不記錄log
     *
     * @param channel
     * @param disable
     */
    public static void setChannelLoggingDisable(Channel channel, boolean disable) {
        if (channel == null) return;
        AttributeKey<Boolean> key = AttributeKey.valueOf(NettyTransmissionAttributeKey.CHANNEL_LOGGING_DISABLE.name());
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
    public static boolean getChannelLoggingDisable(Channel channel) {
        if (channel == null) return false;
        AttributeKey<Boolean> key = AttributeKey.valueOf(NettyTransmissionAttributeKey.CHANNEL_LOGGING_DISABLE.name());
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
    public static void setChannelReject(Channel channel, boolean rejected) {
        if (channel == null) return;
        AttributeKey<Boolean> key = AttributeKey.valueOf(NettyTransmissionAttributeKey.CHANNEL_REJECTED.name());
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
    public static boolean getChannelReject(Channel channel) {
        if (channel == null) return false;
        AttributeKey<Boolean> key = AttributeKey.valueOf(NettyTransmissionAttributeKey.CHANNEL_REJECTED.name());
        if (channel.hasAttr(key)) {
            Attribute<Boolean> attr = channel.attr(key);
            return attr != null && attr.get() != null && attr.get();
        }
        return false;
    }
}
