package com.syscom.fep.gateway.netty.fisc.client.receiver;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.*;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.gateway.entity.Direction;
import com.syscom.fep.gateway.entity.GatewayCodeConstant;
import com.syscom.fep.gateway.entity.SendType;
import com.syscom.fep.gateway.entity.ToFEPAPMode;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionThreadFactory;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayManager;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayUtil;
import com.syscom.fep.gateway.netty.fisc.client.FISCGatewayClientProcessRequest;
import com.syscom.fep.gateway.util.GatewayUtil;
import com.syscom.fep.invoker.FEPInvoker;
import com.syscom.fep.invoker.netty.impl.ToFEPFISCNettyClient;
import com.syscom.fep.invoker.netty.impl.ToFEPFISCNettyClientConfiguration;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.instance.fisc.sender.FISCQueueSenderConfiguration;
import com.syscom.fep.jms.instance.fisc.sender.FISCQueueSenderOperator;
import com.syscom.fep.jms.instance.fisc.sender.alternative.FISCQueueSenderAlternativeConfiguration;
import com.syscom.fep.jms.instance.fisc.sender.alternative.FISCQueueSenderAlternativeOperator;
import com.syscom.fep.vo.communication.ToFEPFISCCommu;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.ScheduledFuture;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.util.concurrent.TimeUnit;

public class FISCGatewayClientReceiverProcessRequest extends FISCGatewayClientProcessRequest<FISCGatewayClientReceiverConfiguration> {
    // @Autowired
    // private GatewayCommuHelper commuHelper;
    @Autowired
    private FISCQueueSenderOperator fiscQueueSenderOperator;
    @Autowired
    private GatewayUtil gatewayUtil;
    @Autowired
    private FEPInvoker invoker;
    @Autowired
    private JmsMsgConfiguration jmsMsgConfiguration;
    @Autowired
    private FISCQueueSenderAlternativeOperator fiscQueueSenderAlternativeOperator;
    @Autowired
    private FISCQueueSenderConfiguration fiscQueueSenderConfiguration;
    @Autowired
    private FISCQueueSenderAlternativeConfiguration fiscQueueSenderAlternativeConfiguration;
    /**
     * 用來在一定時間內未收到財金來的電文時,重發Resume
     */
    private ScheduledFuture<?> resumeFuture;
    /**
     * 用來記錄有retry之後暫時keep的設定
     */
    private final static ToFEPAP toFEPAP = new ToFEPAP();
    /**
     * 專為Receiver建立一個線程池
     */
    private EventExecutorGroup receiverEventExecutorGroup;

    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     */
    @Override
    public void initialization(FISCGatewayClientReceiverConfiguration configuration) {
        super.initialization(configuration);
        // this.commuHelper = SpringBeanFactoryUtil.getBean(GatewayCommuHelper.class);
        this.fiscQueueSenderOperator = SpringBeanFactoryUtil.getBean(FISCQueueSenderOperator.class);
        this.gatewayUtil = SpringBeanFactoryUtil.getBean(GatewayUtil.class);
        this.invoker = SpringBeanFactoryUtil.getBean(FEPInvoker.class);
        this.jmsMsgConfiguration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
        this.fiscQueueSenderAlternativeOperator = SpringBeanFactoryUtil.getBean(FISCQueueSenderAlternativeOperator.class);
        this.fiscQueueSenderConfiguration = SpringBeanFactoryUtil.getBean(FISCQueueSenderConfiguration.class);
        this.fiscQueueSenderAlternativeConfiguration = SpringBeanFactoryUtil.getBean(FISCQueueSenderAlternativeConfiguration.class);
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state, t);
        // 當連線成功後,如果是Recv Socket則發出Resume Tpipe電文
        if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
            this.restartResumeTimer(channel, false);
            try {
                if (clientIdRepeat) {
                    clientIdRepeat = false;
                    this.send(channel, this.logContext, SendType.CR);
                } else {
                    // 參加單位Receiver端，當Connection建立後，需先傳送RESUME TPIPE訊息至財金公司後，才可開始接收訊息
                    //改作法2連線成功先送Cancel Client Id
                    this.send(channel, this.logContext, SendType.CR);
                }
            } catch (Exception e) {
                this.logContext.setProgramException(e);
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
                sendEMS(this.logContext);
            }
        } else if (state == NettyTransmissionConnState.SERVER_MESSAGE_INCOMING) {
            this.restartResumeTimer(channel, false);
        } else if (state == NettyTransmissionConnState.CLIENT_DISCONNECTED) {
            this.restartResumeTimer(channel, true);
        } else if (state == NettyTransmissionConnState.CLIENT_SHUTTING_DOWN) {
            // 這裡記得要關閉線程池物件
            if (this.receiverEventExecutorGroup != null) {
                this.receiverEventExecutorGroup.shutdownGracefully();
                this.receiverEventExecutorGroup = null;
            }
        }
    }

    /**
     * 用來在一定時間內未收到財金來的電文時,重發Resume
     *
     * @param channel
     */
    private void restartResumeTimer(Channel channel, boolean onlyCancel) {
        if (resumeFuture != null) {
            try {
                resumeFuture.cancel(false);
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            } finally {
                resumeFuture = null;
            }
        }
        if (!onlyCancel) {
            resumeFuture = channel.eventLoop().schedule(() -> {
                this.putMDC();
                LogData logData = new LogData();
                logData.setRemark(StringUtils.join(
                        "[", configuration.getSocketType(),
                        " IP:", configuration.getHost(),
                        ",Port:", configuration.getPort(), "] No data received in ", configuration.getDisConnectInterval(), " milliseconds, Disconnect !"));
                logData.setProgramName(StringUtils.join(ProgramName, ".resumeTimer.schedule"));
                logMessage(logData);
                channelHandlerContext.close();
            }, configuration.getDisConnectInterval() - GatewayCodeConstant.FIXED_MILLISECONDS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Recever Socket接收資料流程
     *
     * @param ctx
     * @param logData
     * @param message
     * @throws Exception
     */
    @Override
    protected void doProcess(ChannelHandlerContext ctx, LogData logData, String message) throws Exception {
        // 收到RSM電文
        if (message.length() == 40 && RSM_ID.equals(message.substring(8, 16 + 8))) {
            logData.setStan(StringUtils.EMPTY);
            logData.setEj(0);
            logData.setMessageId(StringUtils.EMPTY);
            logData.setRemark(StringUtils.join(
                    "[", configuration.getSocketType(),
                    " IP:", configuration.getHost(),
                    ",Port:", configuration.getPort(), "] Recv RSM Message"));
            logData.setMessage(message);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
            this.logMessage(logData);
            // 記錄內容至檔名為fep-gateway-fisc-FISCGW-RSM-yyyy-MM-dd
            FISCGatewayUtil.logRSMMessage(message);
            switch (message.substring(30, 2 + 30)) {
                // 判斷RSM電文的Return code欄位
                case "08":
                    // ClientID重覆
                    if ("38".equals(message.substring(38, 2 + 38))) {
                        logData.setStan(StringUtils.EMPTY);
                        logData.setEj(0);
                        logData.setMessageId(StringUtils.EMPTY);
                        logData.setRemark(StringUtils.join(
                                "[", configuration.getSocketType(),
                                " IP:", configuration.getHost(),
                                ",Port:", configuration.getPort(), "] ClientID重覆"));
                        logData.setMessage(message);
                        logData.setMessageFlowType(MessageFlow.Request);
                        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
                        logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
                        this.logMessage(logData);
                        sendEMS(logData);
                        clientIdRepeat = true;
                    }
                    // 若收到Cancelled Accept之RSM(08/3B)訊息後，財金端會主動斷線,此時參加單位務必再中斷連線，重新連結
                    else if ("3B".equals(message.substring(38, 2 + 38))) {
                        logData.setStan(StringUtils.EMPTY);
                        logData.setEj(0);
                        logData.setMessageId(StringUtils.EMPTY);
                        logData.setRemark(StringUtils.join(
                                "[", configuration.getSocketType(),
                                " IP:", configuration.getHost(),
                                ",Port:", configuration.getPort(), "] 財金端會主動斷線,此時參加單位務必再中斷連線，重新連結"));
                        logData.setMessage(message);
                        logData.setMessageFlowType(MessageFlow.Request);
                        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
                        logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
                        this.logMessage(logData);
                        ctx.close();
                    }
                    break;
                case "24":
                case "28":
                    // Receiver端收到return code=24 or 28(timeout),才發Resume Tpipe
                    this.send(ctx.channel(), logData, SendType.R);
                    break;
                case "63":
                    // CheckCode或ClientID檢核失敗
                    if ("28".equals(message.substring(38, 2 + 38))) {
                        logData.setStan(StringUtils.EMPTY);
                        logData.setEj(0);
                        logData.setMessageId(StringUtils.EMPTY);
                        logData.setRemark(StringUtils.join(
                                "[", configuration.getSocketType(),
                                " IP:", configuration.getHost(),
                                ",Port:", configuration.getPort(), "] CheckCode或ClientID檢核失敗"));
                        logData.setMessage(message);
                        logData.setMessageFlowType(MessageFlow.Request);
                        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
                        logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
                        sendEMS(logData);
                    }
                    break;
                default:
                    break;
            }
        }
        // 收到交易電文的處理
        else {
            // 實際資料長度為LL-4byte(LLZZ)
            int lenTele = (Integer.parseInt(message.substring(0, 4), 16) - 4) * 2;
            // 去掉LLZZ及CSM
            message = message.substring(8, lenTele + 8);
            if (message.length() < 90) {
                LogHelperFactory.getTraceLogger().info("ignore message, cause message.length = [", message.length(), "] < 90");
                return;
            }
            // 去掉000000
            String header = FISCGatewayUtil.fromHex(message.substring(6, 90 + 6));
            // 取MsgType後兩碼判斷
            String msgType = header.substring(2, 2 + 2);
            String pcode = header.substring(4, 4 + 4);
            String stan = header.substring(8, 7 + 8);
            String data = message;
            logData.setStan(stan);
            logData.setEj(0);
            logData.setMessageId(StringUtils.EMPTY);
            logData.setRemark(StringUtils.join(
                    "[", configuration.getSocketType(),
                    " IP:", configuration.getHost(),
                    ",Port:", configuration.getPort(), "] Recv Transaction Message"));
            logData.setMessage(data);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
            this.logMessage(logData);
            // 交易電文記錄到檔名為fep-gateway-fisc-FISCGW-transaction-yyyy-MM-dd
            FISCGatewayUtil.logTxMessage(this.configuration.getClientId(), Direction.Receive, message);
            // 收到交易電文,先回Ack
            this.send(ctx.channel(), logData, SendType.A);
            if (this.isRequest(msgType, pcode)) {
                try {
                    // 20210906 配合FISCService call AA改成同步, 呼叫FISCService_ATM改為非同步
                    this.getReceiverEventExecutorGroup().execute(() -> {
                        this.putMDC();
                        this.sendRequestToFEP(ctx.channel(), logData, msgType, pcode, stan, data);
                    });
                } catch (Exception e) {
                    logData.setRemark(StringUtils.join(
                            "[", configuration.getSocketType(),
                            " IP:", configuration.getHost(),
                            ",Port:", configuration.getPort(), "] Send To FISC Service Error"));
                    logData.setMessage(data);
                    logData.setProgramException(e);
                    logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
                    sendEMS(logData);
                }
            } else {
                try {
                    this.sendResponseToFEP(ctx.channel(), logData, stan, data);
                } catch (Exception e) {
                    logData.setRemark(StringUtils.join(
                            "[", configuration.getSocketType(),
                            " IP:", configuration.getHost(),
                            ",Port:", configuration.getPort(), "] Send To FISC Service Error"));
                    logData.setMessage(data);
                    logData.setProgramException(e);
                    logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
                    sendEMS(logData);
                }
            }
        }
    }

    /**
     * 根據config System決定進InQueue或Request Queue規則
     *
     * @param msgType
     * @param pcode
     * @return
     */
    private boolean isRequest(String msgType, String pcode) {
        // Response or PCODE=0103,0105 2010-11-19 modify by Ruling
        if ("10".equals(msgType) || ("02".equals(msgType) && ("0103".equals(pcode) || "0105".equals(pcode) || "0107".equals(pcode)))) {
            return false;
        }
        // Request or Confirm
        else {
            return true;
        }
    }

    /**
     * 給FISC Service發送請求類的電文, 也就是FISC主動發送過來的電文
     *
     * @param channel
     * @param logData
     * @param msgType
     * @param pcode
     * @param stan
     * @param data
     */
    private void sendRequestToFEP(Channel channel, LogData logData, String msgType, String pcode, String stan, String data) {
        // prepare message
        ToFEPFISCCommu request = new ToFEPFISCCommu();
        request.setEj(ej);
        request.setTxRquid(UUIDUtil.randomUUID(true));
        request.setMessage(data);
        request.setStan(stan);
        request.setStep(0);
        request.setSync(false); // FEP接收後進行異步處理
        request.setRClientID(this.configuration.getClientId()); // 放入fiscgw config的receiver ClientId的值
        request.setPcode(pcode);
        try {
            // logData.setSubSys(SubSystem.INBK);
            // logData.setChannel(FEPChannel.FISC);
            // logData.setStan(stan);
            // logData.setEj(ej);
            // logData.setTxRquid(request.getTxRquid());
            // logData.setMessageId(pcode);
            // logData.setMessageFlowType(MessageFlow.Request);
            // logData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
            // logData.setProgramName(StringUtils.join(ProgramName, ".sendRequestToFEP"));
            // logData.setMessage(data);
            // logData.setRemark(StringUtils.join("[", configuration.getSocketType(), " IP:", configuration.getHost(), ",Port:", configuration.getPort(), "]",
            //         " Ready Send(Stan=", stan, ") To FEP FISC Service "));
            // this.logMessage(logData);
            // invoker.sendReceiveToFEPFISC(request, 0);
            // logData.setMessageFlowType(MessageFlow.Request);
            // logData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
            // logData.setProgramName(StringUtils.join(ProgramName, ".sendRequestToFEP"));
            // logData.setMessage(data);
            // logData.setRemark(StringUtils.join("[", configuration.getSocketType(), " IP:", configuration.getHost(), ",Port:", configuration.getPort(), "]",
            //         " Succeed Send(Stan=", stan, ") To FEP FISC Service "));
            // this.logMessage(logData);
            this.sendRequestToFEP(channel, logData, request, 0);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendRequestToFEP"));
            logData.setRemark(StringUtils.join("[", configuration.getSocketType(), " IP:", configuration.getHost(), ",Port:", configuration.getPort(), "]",
                    " Send(Stan=", stan, ") TO FEP FISC Service Error"));
            sendEMS(logData);
        }
    }

    /**
     * 然後FISCGW增加以下參數,控制送FEP的方式:
     * #送Request或confirm交易至FEPAP方式, static固定只送第1台, dynamic代表2台輪流送
     * spring.fep.gateway.transmission.fisc.tofepapmode=static
     * #不管是static或dynamic, 當連不上第1台或目前連的這1台時, 必須開始retry連另一台(只有一組也要繼續retry這一組), 若連不上再連另一台…直到有某一台成功為止
     * # retryInterval代表每次失敗後間隔多久後連另1台,單位為秒, retryCount為最大重試次數, 若已達最大次數則不再retry, 直接sendEMS即可
     * spring.fep.gateway.transmission.fisc.retryInterval=3
     * spring.fep.gateway.transmission.fisc.retryCount=10
     * #一旦有連不上某一組, 開始retry成功後, 將tofepapmode變數暫設為staitic, 代表從此時起只連成功的這1台, 並啟動一個onetime的timer, interval如下參數(單位為秒), 當時間到時, 再重設tofepapmode變數為config預設值
     * spring.fep.gateway.transmission.fisc.resettofepapmodetimer=300
     *
     * @param channel
     * @param logData
     * @param request
     * @param timeout
     * @throws Exception
     */
    private void sendRequestToFEP(Channel channel, LogData logData, final ToFEPFISCCommu request, final int timeout) throws Exception {
        FISCGatewayManager manager = SpringBeanFactoryUtil.getBean(FISCGatewayManager.class);
        ToFEPFISCNettyClientConfiguration configuration = null; // 之前有retry後keep的設定，代表從此時起只連成功的這1台
        boolean isStatic = false;
        if (!manager.useToFEPAPHost()) {
            synchronized (toFEPAP) {
                if (toFEPAP.configuration != null) {
                    configuration = toFEPAP.configuration;
                    isStatic = true;
                }
            }
        }
        int retryCount = 0; // 重試次數
        int retryConfigurationIndex = 0; // 重試時記錄每次取到的Index
        while (true) {
            try {
                // 如果不是固定模式, 則根據配置檔設定的方式來取
                if (!isStatic) {
                    if (manager.useToFEPAPHost() || manager.getTofepapMode() == ToFEPAPMode.STATIC) {
                        configuration = manager.getToFepApCfg();
                    } else {
                        // 如果是第一次取, 則RoundRobin取
                        if (retryCount == 0) {
                            configuration = manager.getTofepapRoundRobin().select();
                        }
                        // 如果是第一次retry
                        else if (retryCount == 1) {
                            // 先取出和上一次不一樣的主機配置
                            while (true) {
                                ToFEPFISCNettyClientConfiguration retryConfiguration = manager.getTofepapRoundRobin().get(retryConfigurationIndex++);
                                if (configuration == null || !retryConfiguration.getHostName().equals(configuration.getHostName())) {
                                    configuration = retryConfiguration;
                                    break;
                                }
                            }
                        }
                        // 後續retry則每次取出和上一次不一樣的主機配置
                        else {
                            if (retryConfigurationIndex >= manager.getTofepapRoundRobin().size())
                                retryConfigurationIndex = 0;
                            configuration = manager.getTofepapRoundRobin().get(retryConfigurationIndex++);
                        }
                    }
                }
                this.sendRequestToFEP(logData, configuration, request, timeout, retryCount);
                break;
            } catch (Exception e) {
                // 若已達最大次數則不再retry, 直接丟異常出去
                if (retryCount == manager.getTofepapRetryCount()) {
                    throw ExceptionUtil.createException(e, "Still Cannot Send(Stan=", request.getStan(), ") TO FEP FISC Service after [", retryCount, "] times!!!");
                }
                // 如果是固定模式送失敗, 則重置
                if (isStatic) {
                    synchronized (toFEPAP) {
                        toFEPAP.toFEPAPMode = null;
                        toFEPAP.configuration = null;
                    }
                    isStatic = false;
                }
            }
            // 每次失敗後sleep一下再重新連
            try {
                Thread.sleep(manager.getTofepapRetryInterval() * 1000L);
            } catch (InterruptedException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            retryCount++;
        }
        // 開始retry成功後, 將tofepapmode變數暫設為static, 代表從此時起只連成功的這1台
        if (!manager.useToFEPAPHost() && retryCount > 0) {
            synchronized (toFEPAP) {
                toFEPAP.toFEPAPMode = ToFEPAPMode.STATIC;
                toFEPAP.configuration = configuration;
                logData.setProgramName(StringUtils.join(ProgramName, ".sendRequestToFEP"));
                logData.setRemark(StringUtils.join("[", this.configuration.getSocketType(), " IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort(), "]",
                        " Change ToFEPAPMode = ", toFEPAP.toFEPAPMode, " and Send to FEP FISC Service (Host=", toFEPAP.configuration.getHost(), ",Port=", toFEPAP.configuration.getPort(), ") Only"));
                logMessage(Level.WARN, logData);
            }
            logData.setProgramName(StringUtils.join(ProgramName, ".sendRequestToFEP"));
            logData.setRemark(StringUtils.join("[", this.configuration.getSocketType(), " IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort(), "]",
                    " It will Reset ToFEPAPMode after ", manager.getTofepapResetToFepApModeTimer(), " seconds..."));
            logMessage(Level.WARN, logData);
            // 並啟動一個onetime的timer, interval如下參數(單位為秒), 當時間到時, 再重設tofepapmode變數為config預設值
            channel.eventLoop().schedule(() -> {
                putMDC();
                synchronized (toFEPAP) {
                    toFEPAP.toFEPAPMode = null;
                    toFEPAP.configuration = null;
                    logData.setProgramName(StringUtils.join(ProgramName, ".sendRequestToFEP"));
                    logData.setRemark(StringUtils.join("[", this.configuration.getSocketType(), " IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort(), "]",
                            " Reset ToFEPAPMode = ", manager.getTofepapMode(), " by Default"));
                    logMessage(Level.WARN, logData);
                }
            }, manager.getTofepapResetToFepApModeTimer(), TimeUnit.SECONDS);
        }
    }

    /**
     * 將Request/Confirm電文送到指定的FISC Service via Socket
     *
     * @param logData
     * @param configuration
     * @param request
     * @param timeout
     * @param retryCount
     * @throws Exception
     */
    private void sendRequestToFEP(LogData logData, final ToFEPFISCNettyClientConfiguration configuration, final ToFEPFISCCommu request, final int timeout, final int retryCount) throws Exception {
        logData.setSubSys(SubSystem.INBK);
        logData.setChannel(FEPChannel.FISC);
        logData.setStan(request.getStan());
        logData.setEj(ej);
        logData.setTxRquid(request.getTxRquid());
        logData.setMessageId(request.getPcode());
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
        logData.setProgramName(StringUtils.join(ProgramName, ".sendRequestToFEP"));
        logData.setMessage(request.getMessage());
        logData.setRemark(StringUtils.join("[", this.configuration.getSocketType(), " IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort(), "]",
                " Ready Send(Stan=", request.getStan(), ",retryCount=", retryCount, ") To FEP FISC Service (Host=", configuration.getHost(), ",Port=", configuration.getPort(), ")"));
        this.logMessage(logData);
        // 交易電文記錄到檔名為fep-gateway-fisc-FISCGW-transaction-yyyy-MM-dd, 送FEP則顯示送的hostname, 從FISCGW送出為Send
        FISCGatewayUtil.logTxMessage(configuration.getHostName(), Direction.Send, request.getMessage());
        try {
            ToFEPFISCNettyClient toFEPFISCNettyClient = SpringBeanFactoryUtil.registerBean(ToFEPFISCNettyClient.class);
            toFEPFISCNettyClient.establishConnectionAndSendReceive(configuration, request, timeout);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendRequestToFEP"));
            logData.setMessage(request.getMessage());
            logData.setRemark(StringUtils.join("[", this.configuration.getSocketType(), " IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort(), "]",
                    " Succeed Send(Stan=", request.getStan(), ",retryCount=", retryCount, ") To FEP FISC Service (Host=", configuration.getHost(), ",Port=", configuration.getPort(), ")"));
            this.logMessage(logData);
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".sendRequestToFEP"));
            logData.setMessage(request.getMessage());
            logData.setRemark(StringUtils.join("[", this.configuration.getSocketType(), " IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort(), "]",
                    " Send(Stan=", request.getStan(), ",retryCount=", retryCount, ") To FEP FISC Service (Host=", configuration.getHost(), ",Port=", configuration.getPort(), ") Error"));
            logMessage(Level.WARN, logData);
            throw e;
        }
    }

    /**
     * 給FISC Service回應電文, 也就是FISC Service通過Sender發送電文到FISC, 再由FISC回應給Receiver, 再callback給Sender丟回給FISC Service
     *
     * @param channel
     * @param logData
     * @param stan
     * @param data
     * @throws Exception
     */
    private void sendResponseToFEP(Channel channel, LogData logData, final String stan, final String data) throws Exception {
        // logData.setProgramName(StringUtils.join(ProgramName, ".sendResponseToFEP"));
        // MessageAsynchronousWaitReceiver<String, ToFEPFISCCommu> callback = MessageAsynchronousWaitReceiverManager.unsubscribe(this, stan);
        // if (callback != null) {
        //     logData.setStan(stan);
        //     logData.setMessageFlowType(MessageFlow.Response);
        //     logData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
        //     logData.setRemark(StringUtils.join(
        //             "[", configuration.getSocketType(),
        //             " IP:", configuration.getHost(),
        //             ",Port:", configuration.getPort(), "] Send(Stan=", stan, ")"));
        //     logData.setMessage(data);
        //     this.logMessage(logData);
        //     // prepare message
        //     ToFEPFISCCommu request = new ToFEPFISCCommu();
        //     request.setMessage(data);
        //     request.setStan(stan);
        //     request.setStep(0);
        //     request.setSync(false); // FEP接收後進行異步處理
        //     // 包成ToFEPFISCCommu丟出去
        //     callback.messageArrived(this, request);
        // } else {
        //     // 前端已Timeout直接送至Dead Queue
        //     logData.setStan(stan);
        //     logData.setRemark(StringUtils.join(
        //             "[", configuration.getSocketType(),
        //             " IP:", configuration.getHost(),
        //             ",Port:", configuration.getPort(), "] Can't Find Stan [", stan, "] to callback"));
        //     logData.setMessage(data);
        //     this.logMessage(logData);
        //     String deadQueueName = commuHelper.getSysconfValueFromFEPFISC(logData, (short) 1, "DEAD_FISC", gatewayUtil.getTimeout(this.configuration));
        //     MsMessage<String> message = new MsMessage<String>(stan, data);
        //     jmsMsgPayloadOperator.sendQueue(deadQueueName, message, null, null);
        // }
        /**
         * 將此電文同時送到2台AP的response queue, 送回去的message, 必須設定以下2個MQ屬性
         * Expiry: 預設3秒, 代表此訊息進到response queue若3秒未被收走, 自動drop掉
         * correlationId: 以Response的stan作為correlationId
         */
        this.sendResponseToQueue(logData, fiscQueueSenderOperator, jmsMsgConfiguration.getQueueNames().getFisc().getDestination(), fiscQueueSenderConfiguration.getProp(), stan, data, "Put message to queue");
        this.sendResponseToQueue(logData, fiscQueueSenderAlternativeOperator, jmsMsgConfiguration.getQueueNames().getFiscAlternative().getDestination(), fiscQueueSenderAlternativeConfiguration.getProp(), stan, data, "Put message to Alternative queue");
    }

    /**
     * 將回應的電文丟進Queue中
     *
     * @param logData
     * @param operator
     * @param destination
     * @param properties
     * @param stan
     * @param data
     * @param remark
     */
    private void sendResponseToQueue(LogData logData, JmsPayloadOperator operator, String destination, JmsConfigurationProperties properties, String stan, String data, String remark) {
        logData.setStan(stan);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
        logData.setProgramName(StringUtils.join(ProgramName, ".sendResponseToQueue"));
        logData.setRemark(StringUtils.join(
                "[", configuration.getSocketType(),
                " IP:", configuration.getHost(),
                ",Port:", configuration.getPort(), "] ", remark, " (Stan=", stan, ",definition=", destination, ")"));
        logData.setMessage(data);
        this.logMessage(logData);
        // 交易電文記錄到檔名為fep-gateway-fisc-FISCGW-transaction-yyyy-MM-dd, 送FEP則顯示送的hostname, 從FISCGW送出為Send
        FISCGatewayUtil.logTxMessage(properties.getHostName(), Direction.Send, data);
        // prepare message
        // ToFEPFISCCommu request = new ToFEPFISCCommu();
        // request.setMessage(data);
        // request.setStan(stan);
        // request.setStep(0);
        // request.setSync(false); // FEP接收後進行異步處理
        // PlainTextMessage plainTextMessage = new PlainTextMessage(JmsKind.QUEUE, destination, request.toString());
        PlainTextMessage plainTextMessage = new PlainTextMessage(JmsKind.QUEUE, destination, data); // 2024-03-25 Richard modified for 這裡要送財經的電文給FISCAdapter
        JmsProperty property = new JmsProperty();
        // Expiry: 預設3秒, 代表此訊息進到response queue若60秒未被收走, 自動drop掉
        property.setTimeToLive(SpringBeanFactoryUtil.getBean(FISCGatewayManager.class).getResponseQueueExpiration());
        property.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        try {
            operator.sendQueue(plainTextMessage, property, new JmsHandler() {
                @Override
                public void setPropertyOut(Message message) throws JMSException {
                    // correlationId: 以Response的stan作為correlationId
                    JmsFactory.setCorrelationID(message, stan);
                }
            });
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".sendResponseToQueue"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join(
                    "[", configuration.getSocketType(),
                    " IP:", configuration.getHost(),
                    ",Port:", configuration.getPort(), "] ", remark, " (Stan=", stan, ",definition=", destination, ") Error"));
            sendEMS(logData);
        }
    }

    /**
     * 用來記錄retry成功之後暫時keep的設定
     */
    private static class ToFEPAP {
        public ToFEPAPMode toFEPAPMode;
        public ToFEPFISCNettyClientConfiguration configuration;
    }

    /**
     * 建立線程池物件
     *
     * @return
     */
    private EventExecutorGroup getReceiverEventExecutorGroup() {
        if (this.receiverEventExecutorGroup == null)
            this.receiverEventExecutorGroup = new DefaultEventExecutorGroup(this.configuration.getEventLoopThreads(),
                    new NettyTransmissionThreadFactory(StringUtils.join(this.configuration.getGateway(), this.configuration.getSocketType(), "ExecutorThread")));
        return this.receiverEventExecutorGroup;
    }
}
