package com.syscom.fep.gateway.netty.fisc.keepalive;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.thread.ThreadWrapper;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.GatewayCodeConstant;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestClient;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class FISCGatewayKeepAliveClientProcessRequest extends NettyTransmissionChannelProcessRequestClient<FISCGatewayKeepAliveClientConfiguration> {
    private ScheduledFuture<?> sendKeepAliveFuture, runSecondaryGatewayFuture;
    /**
     * retry的結果
     * CLIENT_DISCONNECTED會塞入null
     * 有收到Keepalive回應塞入true
     * 預設塞入false
     */
    private final RefBoolean retryResultLock = new RefBoolean(false);

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state, t);
        // 當連線成功後
        if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
            this.logContext.setRemark(StringUtils.join(Gateway.FISCGW, " KeepAlive Client(IP:", this.channelInformation.getLocalIp(), ",Port:", this.channelInformation.getLocalPort(), ") connect to Server(IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort(), ")Connected"));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            logMessage(this.logContext);
            // 開始定時送KeepAlive電文
            this.restartSendKeepAliveFuture(channel, this.configuration.getStart(), false);
        }
        // 關閉的時候, 要將timer停掉
        else if (state == NettyTransmissionConnState.CLIENT_SHUTTING_DOWN) {
            this.logContext.setRemark(StringUtils.join(Gateway.FISCGW, " KeepAlive Client(IP:", this.channelInformation.getLocalIp(), ",Port:", this.channelInformation.getLocalPort(), ") disconnected from Server(IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort(), ")Disconnected"));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            logMessage(Level.WARN, this.logContext);
            synchronized (retryResultLock) {
                retryResultLock.set(null);
                retryResultLock.notifyAll();
            }
            // cancel啟動Secondary Gateway的任務
            this.restartRetrySendKeepAliveFuture(channel, true);
            // cancel定時送KeepAlive電文的任務
            this.restartSendKeepAliveFuture(channel, this.configuration.getInterval(), true);
        }
    }

    /**
     * 處理Client進來的電文
     *
     * @param ctx
     * @param bytes
     * @throws Exception
     */
    @Override
    public void doProcess(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        if (ArrayUtils.isNotEmpty(bytes)) {
            String keepAlive = ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
            // 如果收到KeepAlive Ack
            if (GatewayCodeConstant.FISCGWKeepAliveResponse.equals(keepAlive)) {
                synchronized (retryResultLock) {
                    retryResultLock.set(true);
                    retryResultLock.notifyAll();
                }
                // cancel檢測超時的任務
                this.restartRetrySendKeepAliveFuture(ctx.channel(), true);
                // 再次發送KeepAlive電文
                this.restartSendKeepAliveFuture(ctx.channel(), this.configuration.getInterval(), false);
            }
        }
    }

    /**
     * 執行delay任務送KeepAlive
     *
     * @param channel
     * @param delay
     * @param onlyCancel
     */
    private void restartSendKeepAliveFuture(Channel channel, long delay, boolean onlyCancel) {
        if (sendKeepAliveFuture != null) {
            try {
                sendKeepAliveFuture.cancel(false);
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            } finally {
                sendKeepAliveFuture = null;
            }
        }
        // 若未設代表不做keepalive功能
        if (delay <= 0) {
            return;
        }
        if (!onlyCancel) {
            sendKeepAliveFuture = channel.eventLoop().schedule(() -> {
                this.putMDC();
                this.logContext.clear();
                this.logContext.setRemark(StringUtils.join("Send KeepAlive"));
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartSendKeepAliveFuture"));
                this.logMessage(this.logContext);
                // 送KeepAlive
                if (NettyTransmissionConnState.isClientConnected(this.currentConnState.get())) {
                    NettyTransmissionUtil.sendPlainMessage(this, this.configuration, channel, GatewayCodeConstant.FISCGWKeepAliveRequest);
                }
                // 開始delay執行啟動Secondary Gateway
                this.restartRetrySendKeepAliveFuture(channel, false);
            }, delay, TimeUnit.SECONDS);
        }
    }

    /**
     * 執行delay任務啟動Secondary Gateway
     * <p>
     * 如果沒有收到KeepAlive回應
     * 1. 啟動Secondary
     * 2. 並停止送KeepAlive, 並且斷開長連線
     *
     * @param channel
     * @param onlyCancel
     */
    private void restartRetrySendKeepAliveFuture(Channel channel, boolean onlyCancel) {
        if (runSecondaryGatewayFuture != null) {
            try {
                runSecondaryGatewayFuture.cancel(false);
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            } finally {
                runSecondaryGatewayFuture = null;
            }
        }
        if (!onlyCancel) {
            synchronized (retryResultLock) {
                retryResultLock.set(false);
            }
            runSecondaryGatewayFuture = channel.eventLoop().schedule(() -> {
                this.putMDC();
                // 這裡必須要啟動一個線程跑, 否則會卡住processRequestData接收電文進來
                new Thread(ThreadWrapper.wrap(() -> {
                    this.putMDC();
                    this.logContext.clear();
                    this.logContext.setRemark(StringUtils.join("Receive KeepAlive Response timeout, retry send KeepAlive"));
                    this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartRetrySendKeepAliveFuture"));
                    LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(true)); // 這裡讓log列印出來
                    logMessage(Level.WARN, this.logContext);
                    int retryCount = 0;
                    boolean succeed = false;
                    // 一共嘗試this.configuration.getRetryCount()次
                    while (retryCount++ < this.configuration.getRetryCount()) {
                        this.logContext.setRemark(StringUtils.join("Retry to send KeepAlive at [", retryCount, "] times"));
                        this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartRetrySendKeepAliveFuture"));
                        LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(true)); // 這裡讓log列印出來
                        logMessage(Level.WARN, this.logContext);
                        try {
                            // 送KeepAlive, 每次嘗試間隔為this.configuration.getRetryInterval()秒
                            if (NettyTransmissionConnState.isClientConnected(this.currentConnState.get())) {
                                NettyTransmissionUtil.sendPlainMessage(this, this.configuration, channel, GatewayCodeConstant.FISCGWKeepAliveRequest);
                            }
                            synchronized (retryResultLock) {
                                retryResultLock.wait(this.configuration.getRetryInterval() * 1000L);
                                if (retryResultLock.get() || retryResultLock.get() == null) {
                                    succeed = true;
                                    this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartRetrySendKeepAliveFuture"));
                                    if (retryResultLock.get()) {
                                        this.logContext.setRemark(StringUtils.join("Retry to send KeepAlive at [", retryCount, "] times succeed, and receive KeepAlive Response"));
                                        LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(true)); // 這裡讓log列印出來
                                        logMessage(this.logContext);
                                    } else {
                                        this.logContext.setRemark(StringUtils.join("Give up retry to send KeepAlive at [", retryCount, "] times"));
                                        LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(true)); // 這裡讓log列印出來
                                        logMessage(Level.WARN, this.logContext);
                                    }
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartRetrySendKeepAliveFuture"));
                            this.logContext.setRemark(StringUtils.join("Retry to send KeepAlive at [", retryCount, "] times failed!!!"));
                            this.logContext.setProgramException(e);
                            LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(true)); // 這裡讓log列印出來
                            sendEMS(Level.WARN, this.logContext);
                        }
                    }
                    // 直到this.configuration.getRetryCount()次都無回應時, 就啟動secondary線路
                    if (!succeed) {
                        this.logContext.setRemark(StringUtils.join("Still no KeepAlive Response received after ", retryCount, " times, start to run Secondary Gateway"));
                        this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartRetrySendKeepAliveFuture"));
                        LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(true)); // 這裡讓log列印出來
                        sendEMS(logContext);
                        // 啟動Secondary
                        this.runSecondaryGateway();
                        // 並停止送KeepAlive, 並且斷開長連線
                        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), channel, NettyTransmissionConnState.CHANNEL_CLOSE_BY_MANUAL);
                    }
                })).start();
            }, configuration.getTimeout(), TimeUnit.SECONDS);
        }
    }

    /**
     * 啟動Secondary
     */
    private void runSecondaryGateway() {
        // 則啟動Secondary
        FISCGatewayManager manager = SpringBeanFactoryUtil.getBean(FISCGatewayManager.class);
        manager.runSecondaryGateway(false);
    }
}
