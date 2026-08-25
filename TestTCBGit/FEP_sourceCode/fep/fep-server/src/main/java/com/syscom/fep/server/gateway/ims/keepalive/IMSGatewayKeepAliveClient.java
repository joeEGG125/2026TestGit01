package com.syscom.fep.server.gateway.ims.keepalive;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.thread.ThreadWrapper;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.invoker.netty.SimpleNettyClient;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import com.syscom.fep.server.gateway.ims.IMSGatewayConfiguration;
import com.syscom.fep.server.gateway.ims.IMSGatewayConst;
import com.syscom.fep.server.gateway.ims.IMSGatewayManager;
import com.syscom.fep.server.gateway.ims.IMSGatewayMode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.ScheduledFuture;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@StackTracePointCut(caller = SvrConst.SVR_IMS_GATEWAY)
public class IMSGatewayKeepAliveClient extends SimpleNettyClient<IMSGatewayKeepAliveClientConfiguration, String, String> {
    private static final Logger log = LoggerFactory.getLogger(IMSGatewayKeepAliveClient.class);
    @Autowired
    private IMSGatewayConfiguration imsGatewayConfiguration;
    @Autowired
    private IMSGatewayManager imsGatewayManager;
    private ScheduledFuture<?> sendKeepAliveFuture, retrySendKeepAliveFuture;
    /**
     * retry的結果
     * 終止連線會塞入null
     * 有收到Keepalive回應塞入true
     * 預設塞入false
     */
    private final RefBoolean retryResultLock = new RefBoolean(false);

    /**
     * 程式或者服務名稱
     *
     * @return
     */
    @Override
    public String getName() {
        return SvrConst.SVR_IMS_GATEWAY;
    }

    /**
     * 接收遠端的訊息進來
     *
     * @param bytes
     * @return
     */
    @Override
    protected String bytesToMessageIn(byte[] bytes) {
        return ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 回應訊息給遠端
     *
     * @param s
     * @return
     */
    @Override
    protected byte[] messageOutToBytes(String s) {
        return ConvertUtil.toBytes(s, StandardCharsets.UTF_8);
    }

    /**
     * 狀態發生改變
     *
     * @param channel
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t) {
        this.putMDC(channel);
        if (state == SimpleNettyConnState.CLIENT_CONNECTED) {
            this.logContext.setRemark(StringUtils.join(SvrConst.SVR_IMS_GATEWAY, " KeepAlive Client(IP:", this.getLocalIP(), ",Port:", this.getLocalPort(), ") connect to Server(IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort(), ")Connected"));
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            logMessage(this.logContext);
            // 啟動定時送KeepAlive電文的任務
            this.restartSendKeepAliveFuture(channel, this.configuration.getStart(), false);
        } else if (state == SimpleNettyConnState.CLIENT_SHUTTING_DOWN) {
            this.logContext.setRemark(StringUtils.join(SvrConst.SVR_IMS_GATEWAY, " KeepAlive Client(IP:", this.getLocalIP(), ",Port:", this.getLocalPort(), ") disconnected from Server(IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort(), ")Disconnected"));
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

    @Override
    protected void processRequestData(ChannelHandlerContext ctx, String s) {
        this.putMDC(ctx.channel());
        // 如果收到KeepAlive Ack
        if (IMSGatewayConst.KeepAliveResponse.equals(s)) {
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

    /**
     * 執行delay任務送KeepAlive
     *
     * @param channel
     * @param delay
     * @param onlyCancel
     */
    private void restartSendKeepAliveFuture(Channel channel, long delay, boolean onlyCancel) {
        // 先要cancel已經運行的任務
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
                this.putMDC(channel);
                this.logContext.clear();
                this.logContext.setRemark(StringUtils.join("Send KeepAlive"));
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartSendKeepAliveFuture"));
                this.logMessage(this.logContext);
                try {
                    // 發送keepalive電文
                    sendReceive(IMSGatewayConst.KeepAliveRequest, 0);
                    // 啟動超時檢測任務
                    this.restartRetrySendKeepAliveFuture(channel, false);
                } catch (Exception e) {
                    this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartSendKeepAliveFuture"));
                    this.logContext.setProgramException(e);
                    sendEMS(this.logContext);
                }
            }, delay, TimeUnit.SECONDS);
        }
    }

    /**
     * 當keepAlive無回應時, 需要進行固定次數的嘗試
     *
     * @param channel
     * @param onlyCancel
     */
    private void restartRetrySendKeepAliveFuture(Channel channel, boolean onlyCancel) {
        // 先要cancel已經運行的任務
        if (retrySendKeepAliveFuture != null) {
            try {
                retrySendKeepAliveFuture.cancel(false);
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            } finally {
                retrySendKeepAliveFuture = null;
            }
        }
        if (!onlyCancel) {
            synchronized (retryResultLock) {
                retryResultLock.set(false);
            }
            retrySendKeepAliveFuture = channel.eventLoop().schedule(() -> {
                this.putMDC(channel);
                // 這裡必須要啟動一個線程跑, 否則會卡住processRequestData接收電文進來
                new Thread(ThreadWrapper.wrap(() -> {
                    this.putMDC(channel);
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
                            // 每次嘗試間隔為this.configuration.getRetryInterval()秒
                            sendReceive(IMSGatewayConst.KeepAliveRequest, 0);
                            synchronized (retryResultLock) {
                                try {
                                    retryResultLock.wait(this.configuration.getRetryInterval() * 1000L);
                                } catch (InterruptedException e) {
                                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                                }
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
                        this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartRetrySendKeepAliveFuture"));
                        this.logContext.setRemark(StringUtils.join("Still no KeepAlive Response received after ", retryCount, " times, start to run Secondary Line"));
                        LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(true)); // 這裡讓log列印出來
                        logMessage(Level.WARN, this.logContext);
                        imsGatewayManager.runGateway(IMSGatewayMode.secondary, imsGatewayConfiguration);
                    }
                })).start();
            }, this.configuration.getTimeout(), TimeUnit.SECONDS);
        }
    }

    /**
     * 有新的Channel時, 初始化動作
     *
     * @param ch
     */
    @Override
    protected void channelInitialization(SocketChannel ch) {
        if (!this.configuration.isLogging()) {
            setChannelLoggingDisable(ch, true);
            infoMessage(ch, "[channelInitialization] Disabled all logging");
        }
    }

    @Override
    protected void putKeptMDC(Channel channel) {
        LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(!getChannelLoggingDisable(channel)));
        super.putKeptMDC(channel);
    }

    @Override
    protected void putMDC(Channel channel) {
        super.putMDC(channel);
        if (this.configuration != null && !this.configuration.isLogging()) {
            LogMDC.put(Const.MDC_LOGENABLE, Boolean.toString(false));
        }
    }
}
