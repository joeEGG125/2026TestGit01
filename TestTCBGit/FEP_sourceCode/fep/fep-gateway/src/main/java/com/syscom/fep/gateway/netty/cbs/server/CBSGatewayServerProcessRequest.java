package com.syscom.fep.gateway.netty.cbs.server;

import com.ibm.ims.connect.ImsConnectCommunicationException;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.delegate.MessageAsynchronousWaitReceiver;
import com.syscom.fep.frmcommon.delegate.MessageAsynchronousWaitReceiverManager;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.ims.cbs.sender.CBSGatewayClientSenderProcessRequest;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServer;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import com.syscom.fep.gateway.netty.NettyTransmissionWriteAndFlushResult;
import com.syscom.fep.vo.communication.ToCBSCommu;
import com.syscom.fep.vo.communication.ToFEPCBSCommu;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@StackTracePointCut(caller = SvrConst.SVR_CBS_GATEWAY)
public class CBSGatewayServerProcessRequest extends NettyTransmissionChannelProcessRequestServer<CBSGatewayServerConfiguration> {
    private RoundRobin<CBSGatewayClientSenderProcessRequest> senderProcessRequest;
    private final RefBoolean retrySendToIMSLock = new RefBoolean(true);

    public void initialization(CBSGatewayServerConfiguration configuration, RoundRobin<CBSGatewayClientSenderProcessRequest> senderProcessRequest) {
        super.initialization(configuration);
        this.senderProcessRequest = senderProcessRequest;
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state, t);
        if (!NettyTransmissionConnState.isClientConnected(state)) {
            synchronized (retrySendToIMSLock) {
                retrySendToIMSLock.set(false);
                retrySendToIMSLock.notifyAll();
            }
        } else if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
            synchronized (retrySendToIMSLock) {
                retrySendToIMSLock.set(true);
            }
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
        long receivedTime = System.currentTimeMillis(); // 接收到電文的時間
        String message = ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
        // logging
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.CBS);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
        logData.setMessage(message);
        logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
        logData.setRemark(StringUtils.join(Gateway.CBSGW, " received message from CBSAdapter"));
        this.logMessage(logData);
        try {
            String response = StringUtils.EMPTY;
            int timeout; // 等待IMS回應超時時間
            if (EnvPropertiesUtil.getProperty("spring.fep.gateway.transmission.cbs.forwardToIMS", true)) {
                ToCBSCommu toCBSCommu;
                if (Boolean.getBoolean("spring.fep.gateway.cbs.test")) { // 2026-02-06 Richard add just for test
                    toCBSCommu = new ToCBSCommu();
                    toCBSCommu.setCbsId("123456");
                    toCBSCommu.setChannel("ATM");
                    toCBSCommu.setMessage("Hello World");
                    toCBSCommu.setTimeout(10);
                } else {
                    toCBSCommu = ToCBSCommu.fromXML(message, ToCBSCommu.class);
                }
                timeout = toCBSCommu.getTimeout(); // 等待IMS回應超時時間
                ToFEPCBSCommu toFEPCBSCommu = this.receiveAndSend(logData, toCBSCommu, receivedTime);
                if (toFEPCBSCommu != null) {
                    response = toFEPCBSCommu.toString();
                }
            }
            // 2024-11-19 Richard add 不需要轉送IMS, just for test
            else {
                timeout = 50; // 等待IMS回應超時時間
                // 如果有設定delay, 則sleep一下模擬處理業務需要的時間
                int delay = EnvPropertiesUtil.getProperty("spring.fep.gateway.transmission.cbs.delay", 0);
                if (delay > 0) {
                    try {
                        Thread.sleep(delay * 1000L);
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                }
                ToFEPCBSCommu mock = new ToFEPCBSCommu();
                mock.setChannel(this.configuration.getCbsType());
                mock.setClientID("MOCK-CLIENT-ID");
                mock.setCbsId("MOCK-CBS-ID");
                mock.setMessage("D4C6C5D7C6D7E8F0C6C5D740F2F0F2F5F0F9F1F4F0F0F5F4F0F6F7F1F6F2F3F2F8F5C5D9F4F0F0F1404040F1F1F4F0F9F1F5E8D5F0F0F5F4F0F6F9F4F8F7F7F5F1F3F2F0F0F3F0F04040404040C64040404040404040404040404040404040407DC669C6D7D7F0F3C4F2F2F2F3F5F6F2F8F540404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040F5F34EF0F0F0F0F0F0F0F2F0F0F0F0F0F0F0F1F0F0F0F6F0F3F0F0F8F9F9F0F1F2F9F6F1404040F8F0F8F0F0F0F0F5F9F8F4F4F0F0F1F1F2F2F84EF0F0F0F0F0F0F7F9F2F5F1F0F0404040404040404040404040404040404EF0F0F0F0F0F0F7F9F2F5F1F0F040404040F0F04040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040");
                mock.setEj(99999999);
                response = mock.toString();
            }
            // logging
            logData.setSubSys(SubSystem.GW);
            logData.setChannel(FEPChannel.CBS);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramFlowType(ProgramFlow.CBSGatewayOut);
            logData.setMessage(response);
            logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
            logData.setRemark(StringUtils.join(Gateway.CBSGW, " send message to CBSAdapter {0}, timeout:", timeout));
            // 2025-09-24 Richard modified 如果等待IMS回應超時時間大於0
            if (timeout > 0) {
                // 並且有正常收到IMS的回應, 表示需要回應CBSAdapter
                if (StringUtils.isNotBlank(response)) {
                    CompletableFuture<NettyTransmissionWriteAndFlushResult> future = NettyTransmissionUtil.sendPlainMessage(this, this.configuration, ctx.channel(), response);
                    // 2025-09-16 Richard modified 麻煩看一下是否可以調整一下確定有送出成功才記錄CBSGW send message to CBSAdapter by Ashiang
                    future.thenAccept(result -> {
                        this.putMDC();
                        logData.setRemark(FormatUtil.messageFormat(logData.getRemark(), result.isSucceed() ? "succeed" : "failed"));
                        if (result.isSucceed()) {
                            logMessage(logData);
                        } else {
                            // 若遇到與前端socket斷線而無法送回的情況, 也請SendEMS讓我們明確知道有送不回去的情況 by Ashiang
                            logData.setProgramException(result.getError());
                            sendEMS(logData);
                        }
                    });
                }
                // 如果沒有收到IMS的回應, 比如超時, 則列印Warning的log
                else {
                    logData.setRemark(StringUtils.join(Gateway.CBSGW, " no need send message to CBSAdapter cause empty response, timeout:", timeout));
                    logMessage(Level.WARN, logData);
                }
            }
            // Timeout=0, 代表此交易不需等IMS回應, 也不應該走到回應給CBSAdapter
            else {
                logData.setRemark(StringUtils.join(Gateway.CBSGW, " no need send message to CBSAdapter cause timeout:", timeout));
                logMessage(Level.WARN, logData);
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
            logData.setRemark(StringUtils.join(Gateway.CBSGW, " doProcess failed"));
            sendEMS(logData);
        }
    }

    /**
     * 從CBSAdapter收到的請求電文, 轉發給IMS, 並async/wait得到IMS的回應
     *
     * @param logData
     * @param toCBSCommu
     * @param receivedTime
     * @return
     * @throws Exception
     */
    private ToFEPCBSCommu receiveAndSend(LogData logData, ToCBSCommu toCBSCommu, long receivedTime) throws Exception {
        this.putMDC();
        // logging
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.CBS);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
        logData.setMessage(toCBSCommu.toString());
        logData.setProgramName(StringUtils.join(ProgramName, ".receiveAndSend"));
        logData.setRemark(StringUtils.join(Gateway.CBSGW, " received message from CBSAdapter, CBSId:", toCBSCommu.getCbsId(), ", timeout:", toCBSCommu.getTimeout()));
        logData.setEj(toCBSCommu.getEj());
        logData.setMessageId(toCBSCommu.getCbsId());
        this.logMessage(logData);
        ToFEPCBSCommu toFEPCBSCommu = null;
        String response = StringUtils.EMPTY;
        CBSGatewayClientSenderProcessRequest sender = null; // 2026-02-06 Richard add 取出發送成功的Sender物件
        try {
            MessageAsynchronousWaitReceiver<String, ToFEPCBSCommu> callback = new MessageAsynchronousWaitReceiver<>(toCBSCommu.getCbsId(), ToFEPCBSCommu.class);
            MessageAsynchronousWaitReceiverManager.subscribeRepeatedly(this, callback);
            int retryTimes = 0;
            long startTime = System.currentTimeMillis();
            long tryTimeout = toCBSCommu.getTimeout() * 1000L;
            if (tryTimeout <= 0)
                tryTimeout = 50000L; // 如果沒有timeout, 則預設50秒
            tryTimeout = tryTimeout - (startTime - receivedTime);
            boolean sendEMSForWAIT_TO_RETRY = false;
            logData.setSubSys(SubSystem.GW);
            logData.setChannel(FEPChannel.CBS);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
            logData.setMessage(toCBSCommu.getMessage());
            logData.setEj(toCBSCommu.getEj());
            logData.setMessageId(toCBSCommu.getCbsId());
            logData.setProgramName(StringUtils.join(ProgramName, ".receiveAndSend"));
            logData.setRemark(StringUtils.join(Gateway.CBSGW, " start to sendToIMS, CBSId:", toCBSCommu.getCbsId(), ",receivedTime:", receivedTime, ",startTime:", startTime, ",tryTimeout:", tryTimeout));
            logMessage(logData);
            // 在限定的超時時間內嘗試
            while (System.currentTimeMillis() - startTime < tryTimeout) {
                retryTimes++;
                sendEMSForWAIT_TO_RETRY = false;
                logData.setSubSys(SubSystem.GW);
                logData.setChannel(FEPChannel.CBS);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
                logData.setMessage(toCBSCommu.getMessage());
                logData.setEj(toCBSCommu.getEj());
                logData.setMessageId(toCBSCommu.getCbsId());
                logData.setProgramName(StringUtils.join(ProgramName, ".receiveAndSend"));
                logData.setRemark(StringUtils.join(Gateway.CBSGW, " try to sendToIMS, CBSId:", toCBSCommu.getCbsId(), ",receivedTime:", receivedTime, ",startTime:", startTime, ",tryTimeout:", tryTimeout, ",retryTimes:", retryTimes, ",cbsType:", this.configuration.getCbsType()));
                logMessage(retryTimes > 1 ? Level.WARN : Level.INFO, logData);
                try {
                    sender = this.send(logData, toCBSCommu.getMessage(), callback);
                    sender.accumulateStatisticsTransactions(1); // 2026-02-06 Richard add Counter從sender腳位送出+1 by Ashiang
                    break;
                } catch (Exception e) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".receiveAndSend"));
                    logData.setProgramException(e);
                    // 當CBSGW Sender當一送出一旦出現HWS0008E的exception, 代表連線中斷, 此時會等一小段時間重連,
                    // 通常不超過10秒,因這個Exception很明確是知道斷線, 在重連成功後再重送這筆交易
                    // 因送主機timeout通常是50秒, 只要在50秒內, 若送出的exception都是HWS0008E, 就一直重連後再重送, 直到成功為止
                    if (e instanceof ImsConnectCommunicationException && "HWS0008E".equals(((ImsConnectCommunicationException) e).getErrorNumber())) {
                        logData.setRemark(StringUtils.join(Gateway.CBSGW, " cannot sendToIMS cause IMS Disconnected, CBSId:", toCBSCommu.getCbsId(), ",receivedTime:", receivedTime, ",startTime:", startTime, ",tryTimeout:", tryTimeout, ",retryTimes:", retryTimes, ",cbsType:", this.configuration.getCbsType()));
                        sendEMS(Level.WARN, logData);
                        // 2025-12-09 Richard add 依據設定決定是否需要重送
                        if (!this.configuration.isRetrySendToIMS()) {
                            logData.setSubSys(SubSystem.GW);
                            logData.setChannel(FEPChannel.CBS);
                            logData.setMessageFlowType(MessageFlow.Request);
                            logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
                            logData.setMessage(toCBSCommu.getMessage());
                            logData.setEj(toCBSCommu.getEj());
                            logData.setMessageId(toCBSCommu.getCbsId());
                            logData.setRemark(StringUtils.join(Gateway.CBSGW, " sendToIMS only one times, CBSId:", toCBSCommu.getCbsId(), ",receivedTime:", receivedTime, ",startTime:", startTime, ",tryTimeout:", tryTimeout, ",retryTimes:", retryTimes, ",cbsType:", this.configuration.getCbsType()));
                            logMessage(Level.WARN, logData);
                            break; // 不需要重送, 則直接跳出while
                        }
                    }
                    // IMS斷線, 或者是無可用的Sender, 則後面繼續嘗試送
                    else if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().startsWith("[WAIT_TO_RETRY]")) {
                        logData.setSubSys(SubSystem.GW);
                        logData.setChannel(FEPChannel.CBS);
                        logData.setMessageFlowType(MessageFlow.Request);
                        logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
                        logData.setMessage(toCBSCommu.getMessage());
                        logData.setEj(toCBSCommu.getEj());
                        logData.setMessageId(toCBSCommu.getCbsId());
                        logData.setRemark(StringUtils.join(Gateway.CBSGW, " cannot sendToIMS cause IMS Sender was not available, CBSId:", toCBSCommu.getCbsId(), ",receivedTime:", receivedTime, ",startTime:", startTime, ",tryTimeout:", tryTimeout, ",retryTimes:", retryTimes, ",cbsType:", this.configuration.getCbsType(), ",errorMessage:", e.getMessage()));
                        // 2025-07-14 Richard modified by Ashiang
                        // sendEMS(Level.WARN, logData);
                        logMessage(Level.WARN, logData);
                        sendEMSForWAIT_TO_RETRY = true;
                    }
                    // 2025-11-21 Richard add 前面有送過, 並且送失敗, 但實際上主機那邊有收到並且有回應, 則後面就不用再送了
                    // 2025-11-24 Richard marked Daniel指示先拿掉這個判斷
                    // else if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().startsWith("[MESSAGE_ARRIVED]")) {
                    //     logData.setSubSys(SubSystem.GW);
                    //     logData.setChannel(FEPChannel.CBS);
                    //     logData.setMessageFlowType(MessageFlow.Request);
                    //     logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
                    //     logData.setMessage(toCBSCommu.getMessage());
                    //     logData.setEj(toCBSCommu.getEj());
                    //     logData.setMessageId(toCBSCommu.getCbsId());
                    //     logData.setRemark(StringUtils.join(Gateway.CBSGW, " already received response before, CBSId:", toCBSCommu.getCbsId(), ",receivedTime:", receivedTime, ",startTime:", startTime, ",tryTimeout:", tryTimeout, ",retryTimes:", retryTimes, ",cbsType:", this.configuration.getCbsType()));
                    //     logMessage(Level.WARN, logData);
                    //     break;
                    // }
                    else {
                        throw e;
                    }
                    // 這裡wait一下, 避免太頻繁
                    synchronized (retrySendToIMSLock) {
                        retrySendToIMSLock.wait(500L);
                        if (!retrySendToIMSLock.get())
                            break;
                    }
                }
            }
            // 2025-07-14 Richard modified by Ashiang
            // 幫我把這個不要sendEMS,只要log就好, 因為這還沒timeout不算真正例外
            // 如果到了timeout還取不到, 才要sendEMS
            // 2025-09-16 Richard modified 忘記加上到了timeout的判斷
            if (sendEMSForWAIT_TO_RETRY && System.currentTimeMillis() - startTime >= tryTimeout) {
                logData.setSubSys(SubSystem.GW);
                logData.setChannel(FEPChannel.CBS);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
                logData.setMessage(toCBSCommu.getMessage());
                logData.setEj(toCBSCommu.getEj());
                logData.setMessageId(toCBSCommu.getCbsId());
                logData.setRemark(StringUtils.join(Gateway.CBSGW, " still cannot sendToIMS after timeout, CBSId:", toCBSCommu.getCbsId(), ",receivedTime:", receivedTime, ",startTime:", startTime, ",tryTimeout:", tryTimeout, ",retryTimes:", retryTimes, ",cbsType:", this.configuration.getCbsType()));
                sendEMS(Level.WARN, logData);
            }
            this.putMDC();
            if (toCBSCommu.getTimeout() > 0) {
                // 2025-10-23 Richard modified
                // cbsgw送出給ims開始等待的時間, 必須要減掉(現在時間-收到adapter的時間) by Ashiang
                // if (!callback.waitMessage(this, toCBSCommu.getTimeout() * 1000L)) {
                long timeout = toCBSCommu.getTimeout() * 1000L - (System.currentTimeMillis() - receivedTime);
                logData.setSubSys(SubSystem.GW);
                logData.setChannel(FEPChannel.CBS);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
                logData.setMessage(toCBSCommu.getMessage());
                logData.setEj(toCBSCommu.getEj());
                logData.setMessageId(toCBSCommu.getCbsId());
                logData.setRemark(StringUtils.join(Gateway.CBSGW, " start to wait receive message from IMS, CBSId:", toCBSCommu.getCbsId(), ", receivedTime:", receivedTime, ", timeout:", timeout));
                logData.setMessage(StringUtils.EMPTY);
                logMessage(logData);
                if (timeout < 0 || !callback.waitMessage(this, timeout)) {
                    logData.setSubSys(SubSystem.GW);
                    logData.setChannel(FEPChannel.CBS);
                    logData.setMessageFlowType(MessageFlow.Request);
                    logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
                    logData.setMessage(toCBSCommu.getMessage());
                    logData.setEj(toCBSCommu.getEj());
                    logData.setMessageId(toCBSCommu.getCbsId());
                    logData.setRemark(StringUtils.join(Gateway.CBSGW, " wait receive message from IMS timeout, CBSId:", toCBSCommu.getCbsId(), ", timeout:", timeout));
                    logData.setMessage(StringUtils.EMPTY);
                    logMessage(Level.WARN, logData);
                    MessageAsynchronousWaitReceiverManager.unsubscribe(this, toCBSCommu.getCbsId(), ToFEPCBSCommu.class);
                    return toFEPCBSCommu;
                }
            }
            toFEPCBSCommu = callback.getMessage();
            if (toFEPCBSCommu != null) {
                toFEPCBSCommu.setChannel(toCBSCommu.getChannel());
                toFEPCBSCommu.setEj(toCBSCommu.getEj());
                response = toFEPCBSCommu.toString();
            }
            logData.setSubSys(SubSystem.GW);
            logData.setChannel(FEPChannel.CBS);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
            logData.setMessage(toCBSCommu.getMessage());
            logData.setEj(toCBSCommu.getEj());
            logData.setMessageId(toCBSCommu.getCbsId());
            logData.setRemark(StringUtils.join(Gateway.CBSGW, " receive message from IMS succeed, CBSId:", toCBSCommu.getCbsId()));
            logData.setMessage(response);
            this.logMessage(logData);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".receiveAndSend"));
            logData.setRemark(StringUtils.join(Gateway.CBSGW, " receiveAndSend failed, CBSId:", toCBSCommu.getCbsId()));
            sendEMS(logData);
        } finally {
            // 避免忘記remove
            MessageAsynchronousWaitReceiverManager.unsubscribe(this, toCBSCommu.getCbsId(), ToFEPCBSCommu.class);
            // 2026-02-06 Richard add sender不是null, 表示前面有成功送出電文到IMS
            if (sender != null) {
                sender.accumulateStatisticsTransactions(-1); // 2026-02-06 Richard add receiver收到後送回Response(或timeout)則-1 by Ashiang
            }
        }
        return toFEPCBSCommu;
    }

    /**
     * 電文轉發給IMS
     *
     * @param logData
     * @param message
     * @param callback
     * @return
     * @throws Exception
     */
    private CBSGatewayClientSenderProcessRequest send(LogData logData, String message, MessageAsynchronousWaitReceiver<String, ToFEPCBSCommu> callback) throws Exception {
        if (this.senderProcessRequest != null && this.senderProcessRequest.size() > 0) {
            // 列印出當前可用的Sender信息
            printAvailableSender();
            // 針對同一種cbsType的多組傳送腳位輪流呼叫其中一組sender CBSGatewayProcess的SendToIMS方法
            int fetchTimes = 0;
            CBSGatewayClientSenderProcessRequest sender;
            // 因為可能有腳位會發生斷線, 則取出來會是null, 則繼續輪詢取下一筆
            while (true) {
                // 已經輪詢完所有的筆數, 則跳出
                if (fetchTimes == this.senderProcessRequest.size()) {
                    throw ExceptionUtil.createException("[WAIT_TO_RETRY]Still Cannot find CBSGatewayClientSenderProcessRequest after ", fetchTimes, " times!!!");
                }
                fetchTimes++;
                sender = this.senderProcessRequest.select();
                // 如果取到sender, 並且沒有正在跑sendToIMS方法中, 並且沒有暫停中, 並且對應的Receiver有連線到IMS, 表示可以送電文給主機, 則跳出
                if (sender != null && !sender.isPaused() && !sender.isLockSendToIMS() && sender.isReceiverConnected()) {
                    break;
                }
            }
            // 2025-11-21 Richard add 如果送電文給主機之前已經收到對應電文的回應, 那就不要再送了
            // 2025-11-24 Richard marked Daniel指示先拿掉這個判斷
            // if (callback.isMessageArrived()) {
            //     throw ExceptionUtil.createException("[MESSAGE_ARRIVED]Already got Response from IMS");
            // }
            if (Boolean.getBoolean("spring.fep.gateway.cbs.test")) { // 2026-02-06 Richard add just for test
                new Thread(() -> {
                    try {
                        // simulate to wait response
                        Thread.sleep(new SecureRandom().nextInt(10) * 1000L);
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                    ToFEPCBSCommu toFEPCBSCommu = new ToFEPCBSCommu();
                    toFEPCBSCommu.setMessage("Hello World Response");
                    toFEPCBSCommu.setCbsId("123456");
                    toFEPCBSCommu.setClientID("654321");
                    callback.messageArrived(this, toFEPCBSCommu);
                }).start();
            } else {
                sender.sendToIMS(logData, message);
            }
            return sender;
        } else {
            throw ExceptionUtil.createException("[WAIT_TO_RETRY]Cannot find CBSGatewayClientSenderProcessRequest!!!");
        }
    }

    private void printAvailableSender() {
        StringBuilder sb = new StringBuilder();
        List<String> availables = new ArrayList<>();
        List<CBSGatewayClientSenderProcessRequest> senderProcessRequests = this.senderProcessRequest.getList();
        for (CBSGatewayClientSenderProcessRequest sender : senderProcessRequests) {
            if (sender != null) {
                if (sender.isPaused()) {
                    sb.append("\t").append(sender.getConfiguration().getName()).append(":").append("Paused").append(System.lineSeparator());
                } else if (!sender.isConnected()) {
                    sb.append("\t").append(sender.getConfiguration().getName()).append(":").append("Disconnected").append(System.lineSeparator());
                } else if (sender.isLockSendToIMS()) {
                    sb.append("\t").append(sender.getConfiguration().getName()).append(":").append("SendingToIMS").append(System.lineSeparator());
                } else if (!sender.isReceiverConnected()) {
                    sb.append("\t").append(sender.getConfiguration().getName()).append(":").append("ReceiverDisconnected").append(System.lineSeparator());
                } else {
                    availables.add(sender.getConfiguration().getName());
                }
            }
        }
        if (sb.length() > 0) {
            sb.insert(0, System.lineSeparator())
                    .insert(0, System.lineSeparator() + "Unavailable Sender:");
        }
        if (!availables.isEmpty()) {
            sb.insert(0, StringUtils.join(availables, System.lineSeparator() + "\t"))
                    .insert(0, System.lineSeparator() + "Available Sender:" + System.lineSeparator() + "\t");
        }
        LogHelperFactory.getTraceLogger().debug(sb.toString());
    }
}