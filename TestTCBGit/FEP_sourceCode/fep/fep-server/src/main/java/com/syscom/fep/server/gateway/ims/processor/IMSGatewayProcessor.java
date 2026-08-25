package com.syscom.fep.server.gateway.ims.processor;

import com.ibm.ims.connect.*;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.scheduler.AbstractScheduledTask;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.server.common.handler.CBSHandler;
import com.syscom.fep.server.gateway.ims.IMSGatewayConfiguration;
import com.syscom.fep.server.gateway.ims.IMSGatewayConnState;
import com.syscom.fep.server.gateway.ims.IMSGatewayMode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 每個IMS Gateway Thread實例
 * <p>
 * 針對clientIds每一個clientId, 分別建立不同的Thread
 *
 * @author Richard & Ashiang
 */
@StackTracePointCut(caller = SvrConst.SVR_IMS_GATEWAY)
public class IMSGatewayProcessor extends Thread {
    private final String ProgramName = this.getClass().getSimpleName();
    private final IMSGatewayConfiguration configuration;
    private final String hostName;
    private final int hostPort;
    private final String clientId;
    private final String dsName;
    private final String tranCode;
    private final long reestablishConnectionInterval;
    private final long pingIMSInterval;
    private boolean running = true;
    private final LogData logData = new LogData();
    private Connection connection = null;
    private final RefBoolean waitProceed = new RefBoolean(Boolean.FALSE);
    private final IMSGatewayMode mode;
    private final IMSGatewayProcessorType type;
    private final IMSGatewayProcessor sender;
    private final List<String> messageToIMSList = new ArrayList<>();
    private final Executor executor;
    protected final AtomicReference<IMSGatewayConnState> currentConnState = new AtomicReference<>(IMSGatewayConnState.DISCONNECTED);
    private PingIMSTimer pingIMSTimer;
    private final AtomicBoolean pingIMSResult = new AtomicBoolean(true);
    private final IMSGatewayProcessorGroup processorGroup;
    private final IMSGatewayProcessorConfiguration processorConfiguration;
    private final Object reestablishConnectionLock = new Object();
    private boolean waitToBuildIMSConnectionWhenStart = false;

    public IMSGatewayProcessor(IMSGatewayProcessorGroup processorGroup, IMSGatewayConfiguration configuration, IMSGatewayProcessorConfiguration processorConfiguration, String tranCode, IMSGatewayProcessor sender, Executor executor) {
        super(processorConfiguration.getThreadName());
        this.configuration = configuration;
        this.hostName = processorConfiguration.getHost();
        this.hostPort = processorConfiguration.getPort();
        this.clientId = processorConfiguration.getClientId();
        this.dsName = processorConfiguration.getDataStore();
        this.tranCode = tranCode;
        this.reestablishConnectionInterval = processorConfiguration.getReestablishConnectionInterval();
        this.pingIMSInterval = processorConfiguration.getPingIMSInterval();
        this.mode = processorConfiguration.getMode();
        this.type = processorConfiguration.getProcessorType();
        this.sender = sender;
        this.executor = executor;
        this.processorGroup = processorGroup;
        this.processorConfiguration = processorConfiguration;
    }

    public void terminate() {
        if (!this.running)
            return;
        this.running = false;
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        logData.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        logData.setRemark(StringUtils.join("Thread ", this.getName(), " start to stopping..."));
        FEPBase.logMessage(Level.INFO, logData);
        // messageToIMSList要notifyAll一下
        synchronized (this.messageToIMSList) {
            this.messageToIMSList.notifyAll();
        }
        synchronized (this.reestablishConnectionLock) {
            this.reestablishConnectionLock.notifyAll();
        }
        // 如果有業務正在處理中, 等待業務處理完畢, 再終止線程
        if (configuration.isWaitTransactionExecutedFinishedBeforeTerminate()) {
            synchronized (waitProceed) {
                // 有業務正在處理中則等待
                if (waitProceed.get()) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".terminate"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " Wait Transaction executed finished", configuration.getWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds() > 0 ? StringUtils.join(" in ", configuration.getWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds(), " milliseconds at the most") : StringUtils.EMPTY, "..."));
                    FEPBase.logMessage(Level.INFO, logData);
                    try {
                        // 設置一個超時時間, 避免出現等待時間過長的情況
                        if (configuration.getWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds() > 0) {
                            waitProceed.wait(configuration.getWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds());
                        } else {
                            waitProceed.wait();
                        }
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                }
            }
        }
        this.closeConnection(connection);
        if (this.pingIMSTimer != null) {
            this.pingIMSTimer.destroy();
        }
        this.connStateChanged(IMSGatewayConnState.SHUTTING_DOWN, null);
        logData.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        logData.setRemark(StringUtils.join("Thread ", this.getName(), " has stopped"));
        FEPBase.logMessage(Level.INFO, logData);
        this.connStateChanged(IMSGatewayConnState.SHUT_DOWN, null);
    }

    private void connStateChanged(IMSGatewayConnState state, Throwable t) {
        this.currentConnState.set(state);
        this.processorGroup.connStateChanged(this.logData, this.processorConfiguration, state, t);
    }

    /**
     * 取得當前連線狀態
     *
     * @return
     */
    public IMSGatewayConnState getCurrentConnState() {
        return this.currentConnState.get();
    }

    /**
     * 獲取Mode
     *
     * @return
     */
    public IMSGatewayMode getMode() {
        return mode;
    }

    /**
     * 獲取ClientId
     *
     * @return
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 獲取遠端連線主機名稱
     *
     * @return
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * 獲取遠端連線port
     *
     * @return
     */
    public int getHostPort() {
        return hostPort;
    }

    /**
     * 判斷是否還在連線中
     *
     * @return
     */
    public boolean isConnected() {
        return this.connection != null && this.connection.isConnected();
    }

    /**
     * 連線成功後, 獲取本機端IP和Port
     *
     * @return
     */
    public String getLocalEndPoint() {
        if (this.connection != null) {
            Socket socket = ReflectUtil.getFieldValue(this.connection, "socket", null);
            return SocketUtil.getLocalAddress(socket);
        }
        return StringUtils.EMPTY;
    }

    public void setWaitToBuildIMSConnectionWhenStart(boolean waitToBuildIMSConnectionWhenStart) {
        this.waitToBuildIMSConnectionWhenStart = waitToBuildIMSConnectionWhenStart;
    }

    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void run() {
        final String messageForParameter = StringUtils.join("hostName:", hostName, ",hostPort:", hostPort, ",clientId:", clientId, ",dsName:", dsName, ",tranCode:", tranCode);
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        // 如果一開始運行需要等待一下再建立連線
        if (this.waitToBuildIMSConnectionWhenStart && !this.processorConfiguration.isAutoReestablishConnection()) {
            logData.setProgramName(StringUtils.join(ProgramName, ".run"));
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " will build connection to IMS after ", reestablishConnectionInterval, " milliseconds, ", messageForParameter));
            FEPBase.logMessage(Level.WARN, logData);
            synchronized (this.reestablishConnectionLock) {
                try {
                    this.reestablishConnectionLock.wait(this.reestablishConnectionInterval);
                } catch (InterruptedException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
            if (!this.running) {
                return;
            }
        }
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this.processorConfiguration, StringUtils.replace(this.getName(), "-", StringUtils.SPACE), true, "isPrinted", "scheduleToRun"));
        logData.setProgramName(StringUtils.join(ProgramName, ".run"));
        logData.setRemark(StringUtils.join("Thread ", this.getName(), " start to run...", messageForParameter));
        FEPBase.logMessage(Level.INFO, logData);
        ConnectionFactory connectionFactory = null;
        TmInteraction interaction = null;
        boolean exceptionOccur = false;
        boolean first = true; // 是否首次進入while
        while (running) {
            this.setWaitProceedState(Boolean.FALSE, false);
            logData.setStep(0); // step從0開始重新記錄
            if (!first) {
                if (exceptionOccur) {
                    exceptionOccur = false;
                    this.connStateChanged(IMSGatewayConnState.DISCONNECTED_ON_EXCEPTION_OCCUR, null);
                }
                this.closeConnection(connection); // 每次先把上一次的Connection關閉
                // 如果不需要自動恢復連線, 則退出線程
                if (!this.processorConfiguration.isAutoReestablishConnection())
                    break;
                if (this.reestablishConnectionInterval > 0) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " sleep ", this.reestablishConnectionInterval, " milliseconds before rebuild IMS Connection, ", messageForParameter));
                    FEPBase.logMessage(Level.INFO, logData);
                    synchronized (this.reestablishConnectionLock) {
                        try {
                            this.reestablishConnectionLock.wait(this.reestablishConnectionInterval);
                        } catch (InterruptedException e) {
                            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        }
                    }
                    if (!this.running) {
                        break;
                    }
                }
            }
            first = false;
            // (1) 建立IMS ConnectionFactory物件
            try {
                connectionFactory = buildConnectionFactory(this.hostName, this.hostPort, this.clientId);
                logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                logData.setRemark(StringUtils.join("Thread ", this.getName(), " Build IMS Connection Factory Succeed, ", messageForParameter));
                FEPBase.logMessage(Level.INFO, logData);
            } catch (Exception e) {
                if (running) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " Build IMS Connection Factory Failed, ", messageForParameter));
                    logData.setProgramException(e);
                    FEPBase.sendEMS(logData);
                } else {
                    logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " Build IMS Connection Factory Failed cause Terminate, ", messageForParameter));
                    FEPBase.logMessage(Level.WARN, logData);
                }
                continue; // build失敗, 則從(1)開始嘗試
            }
            // (2) 建立IMS Connection物件
            try {
                connection = buildConnection(connectionFactory);
                logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                logData.setRemark(StringUtils.join("Thread ", this.getName(), " Build IMS Connection Succeed, ", messageForParameter));
                FEPBase.logMessage(Level.INFO, logData);
            } catch (Exception e) {
                if (running) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " Build IMS Connection Failed, ", messageForParameter));
                    logData.setProgramException(e);
                    FEPBase.sendEMS(logData);
                } else {
                    logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " Build IMS Connection Failed cause Terminate, ", messageForParameter));
                    FEPBase.logMessage(Level.WARN, logData);
                }
                continue; // build失敗, 則從(1)開始嘗試
            }
            // sender連成功就先啟動
            if (this.type == IMSGatewayProcessorType.SENDER) {
                this.scheduleToPingIMS(this.logData, 0);
            }
            while (running) {
                if (this.type == IMSGatewayProcessorType.RECEIVER) {
                    // 失敗, 則從(1)開始嘗試
                    if (!doReceiverProcess(messageForParameter)) {
                        exceptionOccur = true;
                        break;
                    }
                } else if (this.type == IMSGatewayProcessorType.SENDER) {
                    // 失敗, 則從(1)開始嘗試
                    if (!doSenderProcess(messageForParameter)) {
                        exceptionOccur = true;
                        break;
                    }
                }
            }
        }
        this.setWaitProceedState(Boolean.FALSE, true);
    }

    /**
     * 接收電文
     *
     * @param messageForParameter
     * @return
     */
    private boolean doReceiverProcess(String messageForParameter) {
        this.setWaitProceedState(Boolean.FALSE, false);
        // (3) 建立IMS Interaction物件
        // try {
        // interaction = buildInteraction(connection, clientId, dsName, tranCode);
        // logData.setProgramName(StringUtils.join(ProgramName, ".doReceiverProcess"));
        // logData.setRemark(StringUtils.join("Thread ", this.getName(), " Build IMS TmInteraction Succeed, ", messageForParameter));
        // FEPBase.logMessage(Level.INFO, logData);
        // } catch (ImsConnectApiException e) {
        // if (running) {
        // logData.setProgramName(StringUtils.join(ProgramName, ".doReceiverProcess"));
        // logData.setRemark(StringUtils.join("Thread ", this.getName(), " Build IMS TmInteraction Failed, ", messageForParameter));
        // logData.setProgramException(e);
        // FEPBase.sendEMS(logData);
        // } else {
        // logData.setProgramName(StringUtils.join(ProgramName, ".doReceiverProcess"));
        // logData.setRemark(StringUtils.join("Thread ", this.getName(), " Build IMS TmInteraction Failed cause Terminate, ", messageForParameter));
        // FEPBase.logMessage(Level.WARN, logData);
        // }
        // break; // build失敗, 則從(1)開始嘗試
        // }
        // 模擬收送訊息, only for Test start
        // FEPReturnCode rtnCode = this.simulatorReceiveAndSend(interaction, messageForParameter);
        // if (rtnCode == FEPReturnCode.Normal) {
        // continue;
        // } else if (rtnCode == FEPReturnCode.ProgramException) {
        // break;
        // }
        // 模擬收送訊息, only for Test end
        try {
            // logData.setProgramName(StringUtils.join(ProgramName, ".doReceiverProcess"));
            // logData.setRemark(StringUtils.join("Thread ", this.getName(), " IMS TmInteraction start to Execute, ", messageForParameter));
            // FEPBase.logMessage(Level.INFO, logData);
            // (4) 開始等待接收資料
            resumeClearTpipe(connection);
            // interaction.execute();
        } catch (Exception e) {
            if (running) {
                logData.setProgramName(StringUtils.join(ProgramName, ".doReceiverProcess"));
                logData.setRemark(StringUtils.join("Thread ", this.getName(), " IMS TmInteraction Execute Failed, ", messageForParameter));
                logData.setProgramException(e);
                FEPBase.sendEMS(logData);
            } else {
                logData.setProgramName(StringUtils.join(ProgramName, ".doReceiverProcess"));
                logData.setRemark(StringUtils.join("Thread ", this.getName(), " IMS TmInteraction Execute Failed cause Terminate, ", messageForParameter));
                FEPBase.logMessage(Level.WARN, logData);
            }
            return false; // 失敗, 則從(1)開始嘗試
        }
        this.setWaitProceedState(Boolean.TRUE, false);
        // try {
        // // (5) 取得接收資料
        // outMsg = interaction.getOutputMessage();
        // req = outMsg.getDataAsString();
        // LogHelperFactory.getTraceLogger().info(Const.MESSAGE_IN, req);
        // // (6) 記錄FEPLOG內容
        // logData.setProgramFlowType(ProgramFlow.IMSGWIn);
        // logData.setMessageFlowType(MessageFlow.Request);
        // logData.setProgramName(StringUtils.join(ProgramName, ".doReceiverProcess"));
        // logData.setMessage(req);
        // logData.setRemark(StringUtils.join("Thread ", this.getName(), " IMS TmInteraction GetOutputMessage Succeed, ", messageForParameter));
        // // 如果有空白電文, 忽略
        // if (StringUtils.isBlank(req)) {
        // logData.setRemark(StringUtils.join("Thread ", this.getName(), " IMS TmInteraction GetOutputMessage Succeed, Ignore Empty String, ", messageForParameter));
        // FEPBase.logMessage(Level.WARN, logData);
        // continue;
        // }
        // // 21024-02-19 Richard add for *REQSTS*這個訊息代表是IMS的RSM訊息,非交易訊息, LOG後請直接忽略不用往後傳送 by Ashiang
        // else if (req.startsWith(configuration.getMessagePrefixRSM())) {
        // logData.setRemark(StringUtils.join("Thread ", this.getName(), " IMS TmInteraction GetOutputMessage Succeed, Ignore \"RSM\" Message", messageForParameter));
        // FEPBase.logMessage(Level.WARN, logData);
        // continue;
        // }
        // FEPBase.logMessage(Level.INFO, logData);
        // } catch (ImsConnectApiException e) {
        // logData.setProgramException(e);
        // logData.setProgramName(StringUtils.join(ProgramName, ".doReceiverProcess"));
        // logData.setRemark(StringUtils.join("Thread ", this.getName(), " IMS TmInteraction GetOutputMessage Failed, ", messageForParameter));
        // FEPBase.sendEMS(logData);
        // break; // 失敗, 則從(1)開始嘗試
        // }
        // (7) CALL CBSHandler
        return true;
    }

    /**
     * 傳送電文
     *
     * @param messageForParameter
     * @return
     */
    private boolean doSenderProcess(String messageForParameter) {
        boolean success = true;
        this.setWaitProceedState(Boolean.FALSE, false);
        String[] messages = null;
        synchronized (this.messageToIMSList) {
            if (this.messageToIMSList.isEmpty()) {
                try {
                    this.messageToIMSList.wait(600000);
                } catch (InterruptedException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
            messages = new String[this.messageToIMSList.size()];
            this.messageToIMSList.toArray(messages);
            this.messageToIMSList.clear();
        }
        if (ArrayUtils.isEmpty(messages)) {
            success = this.pingIMSResult.get();
        } else {
            // 送出前若有timer先停止定時ping計時
            if (pingIMSTimer != null) {
                pingIMSTimer.cancel();
            }
            logData.setProgramName(StringUtils.join(ProgramName, ".doSenderProcess"));
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " Start to SendToIMS, message count = [", messages.length, "]", messageForParameter));
            FEPBase.logMessage(Level.INFO, logData);
            boolean scheduleToPingIMS = false;
            for (String message : messages) {
                try {
                    this.SendToIMS(connection, message);
                    scheduleToPingIMS = true; // 只要有成功送出一筆, 就開始下面的定時任務
                } catch (Exception e) {
                    logData.setProgramException(e);
                    logData.setMessage(message);
                    logData.setProgramName(StringUtils.join(ProgramName, ".doSenderProcess"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " SendToIMS Failed", messageForParameter));
                    FEPBase.sendEMS(logData);
                    success = false;
                }
            }
            // 開始計時pingIMS
            if (scheduleToPingIMS) {
                this.scheduleToPingIMS(this.logData, this.pingIMSInterval);
            }
        }
        return success;
    }

    /**
     * 將要回應給IMS的電文放入列表中
     *
     * @param message
     */
    private void addMessageToIMS(String message) {
        synchronized (this.messageToIMSList) {
            this.messageToIMSList.add(message);
            if (this.messageToIMSList.size() == 1) {
                this.messageToIMSList.notifyAll();
            }
        }
    }

    private void setWaitProceedState(final boolean wait, final boolean notifyAll) {
        if (configuration.isWaitTransactionExecutedFinishedBeforeTerminate()) {
            synchronized (waitProceed) {
                waitProceed.set(wait);
                if (notifyAll) {
                    waitProceed.notifyAll();
                }
            }
        }
    }

    /**
     * (1) 建立IMS ConnectionFactory物件
     * <p>
     * 以_hostName, _hostPort, _clientId變數值建立ConnectionFactory物件
     *
     * @param hostName
     * @param portNumber
     * @param clientId
     * @return
     * @throws ImsConnectApiException
     */
    private ConnectionFactory buildConnectionFactory(String hostName, int portNumber, String clientId) throws ImsConnectApiException {
        this.connStateChanged(IMSGatewayConnState.CONNECTING, null);
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHostName(hostName);
            connectionFactory.setPortNumber(portNumber);
            connectionFactory.setClientId(clientId);
            return connectionFactory;
        } catch (ImsConnectApiException e) {
            this.connStateChanged(IMSGatewayConnState.CONNECTING_FAILED, e);
            throw e;
        }
    }

    /**
     * (2) 建立IMS Connection物件
     * <p>
     * 分別設定連線逾時及傳輸逾時屬性
     *
     * @param connectionFactory
     * @return
     * @throws ImsConnectApiException
     * @throws SocketException
     */
    private Connection buildConnection(ConnectionFactory connectionFactory) throws ImsConnectApiException, SocketException {
        this.connStateChanged(IMSGatewayConnState.CONNECTING, null);
        try {
            Connection connection = connectionFactory.getConnection();
            connection.setSocketType(ApiProperties.SOCKET_TYPE_PERSISTENT); // 長連接
            // connection.setSocketConnectTimeout(5000); // 連線逾時,單位毫秒
            connection.setSocketConnectTimeout(3000); // 這裏改成3000
            connection.connect();
            this.connStateChanged(IMSGatewayConnState.CONNECTED, null);
            return connection;
        } catch (ImsConnectApiException | SocketException e) {
            this.connStateChanged(IMSGatewayConnState.CONNECTING_FAILED, e);
            throw e;
        }
    }

    /**
     * (3) 建立IMS Interaction物件
     *
     * @param connection
     * @param ltermOverrideName
     * @param imsDatastoreName
     * @param tranCode
     * @return
     * @throws ImsConnectApiException
     */
    // private TmInteraction buildInteraction(Connection connection, String ltermOverrideName, String imsDatastoreName, String tranCode) throws ImsConnectApiException {
    // TmInteraction interaction = connection.createInteraction();
    // interaction.setLtermOverrideName(ltermOverrideName);
    // interaction.setImsDatastoreName(imsDatastoreName);
    // interaction.setTrancode(tranCode);
    // interaction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_RESUMETPIPE);
    // interaction.setResumeTpipeProcessing(ApiProperties.RESUME_TPIPE_SINGLE_WAIT);
    // interaction.setResumeTpipeRetrievalType(ApiProperties.RETRIEVE_SYNC_MESSAGE_ONLY); //接收訊息模式
    // return interaction;
    // }

    /**
     * 關閉連線
     *
     * @param connection
     */
    private void closeConnection(Connection connection) {
        if (connection != null) {
            this.connStateChanged(IMSGatewayConnState.DISCONNECTING, null);
            try {
                if (connection.isConnected()) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " Ready to Close IMS Connection"));
                    FEPBase.logMessage(Level.WARN, logData);
                    connection.disconnect();
                    logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " Close IMS Connection Succeed"));
                    FEPBase.logMessage(Level.INFO, logData);
                } else {
                    logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " Close IMS Connection Already, no need to close again"));
                    FEPBase.logMessage(Level.WARN, logData);
                }
            } catch (ImsConnectApiException e) {
                this.connStateChanged(IMSGatewayConnState.DISCONNECTING_FAILED, e);
                logData.setProgramException(e);
                logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                logData.setRemark(StringUtils.join("Thread ", this.getName(), " Close IMS Connection Failed"));
                FEPBase.sendEMS(logData);
            } finally {
                this.connection = null;
                this.connStateChanged(IMSGatewayConnState.DISCONNECTED, null);
            }
        }
    }

    private void InvokeAAProcess(String req) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        this.setWaitProceedState(Boolean.TRUE, false);

        try {
            logData.setProgramFlowType(ProgramFlow.IMSGWOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".InvokeAAProcess"));
            logData.setMessage(req);
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " Begin InvokeAAProcess"));
            FEPBase.logMessage(Level.INFO, logData);

            CBSHandler handler = new CBSHandler();
            String rtnData = handler.dispatch(FEPChannel.CBS, req);
            // (8) 記錄FEPLOG內容
            logData.setProgramFlowType(ProgramFlow.IMSGWOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".InvokeAAProcess"));
            logData.setMessage(rtnData);
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " Get Response from CBSHandler Succeed "));
            FEPBase.logMessage(Level.INFO, logData);
            if (StringUtils.isNotBlank(rtnData)) {
                // 2024-04-24 Richard modified
                // SendToIMS(conn, rtnData);
                // 透過sender回應給IMS
                sender.addMessageToIMS(rtnData);
            }
            // LogHelperFactory.getTraceLogger().info(Const.MESSAGE_OUT, rtnData);
            // 模擬測試原電文丟回去試看看
            // SendToIMS(conn, req);

        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".InvokeAAProcess"));
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " InvokeAAProcess Failed"));
            FEPBase.sendEMS(logData);
            // break; // 失敗, 則從(1)開始嘗試
        }
    }

    private void SendToIMS(Connection myConn, String inputData) throws Exception {
        this.setWaitProceedState(Boolean.TRUE, false);
        TmInteraction myTmInteraction;

        myTmInteraction = myConn.createInteraction();

        // 根據合庫建議改 SENDONLYACK 以避免送出returnCode 0卻沒收到
        myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_SENDONLYACK);
        myTmInteraction.setLtermOverrideName(this.clientId);
        myTmInteraction.setImsDatastoreName(this.dsName);
        myTmInteraction.setImsConnectTimeout(ApiProperties.TIMEOUT_5_SECONDS);
        myTmInteraction.setInteractionTimeout(ApiProperties.TIMEOUT_10_SECONDS);
        // logger.debug(ApiProperties.INTERACTION_TYPE_DESC_SENDONLY + " set Timeout as "
        // + myTmInteraction.getImsConnectTimeout() + "ms");
        myTmInteraction.setCommitMode(ApiProperties.COMMIT_MODE_0);
        myTmInteraction.setAckNakProvider(ApiProperties.CLIENT_ACK_NAK);
        myTmInteraction.setInputMessageDataSegmentsIncludeLlzzAndTrancode(ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_NOT_INCLUDE_LLZZ_AND_TRANCODE);
        String tCode = "";
        if (StringUtils.isNotBlank(inputData)) {
            String pCode = StringUtils.substring(inputData, 14, 22);
            pCode = EbcdicConverter.fromHex(CCSID.English, pCode);
            tCode = this.getTransCode(pCode) + " ";
            // Trancode = StringUtils.rightPad(tCode, 8, " ");
            // 財金電文前面3個byte改為TranCode + 空白
            inputData = EbcdicConverter.toHex(CCSID.English, tCode.length(), tCode) + inputData.substring(6);
        }

        myTmInteraction.setTrancode("");
        // get InputMessage instance from myTMInteraction
        InputMessage inMsg = myTmInteraction.getInputMessage();
        // populate input message data with indata byte array
        byte[] inputData_ = hexToBytes(inputData);

        inMsg.setInputMessageData(inputData_);

        // byte[] _inputData = inMsg.getDataAsByteArray();

        // String fileContent = "Hex=";
        // for (int i = 0; i < _inputData.length; i++) {
        // fileContent += String.format("%02x", _inputData[i]);
        // }

        logData.setMessage(inputData);
        logData.setRemark(StringUtils.join("Thread ", this.getName(), " before Send data to IMS (Trancode:", tCode + ",clientId:", this.clientId, ")"));
        FEPBase.logMessage(Level.INFO, logData);

        // execute the transaction
        myTmInteraction.execute();

        // get output from myTMInteraction
        OutputMessage outMsg = myTmInteraction.getOutputMessage();
        // get data from outMsg as a string
        String outData = outMsg.getDataAsString();

        logData.setProgramName(StringUtils.join(ProgramName, ".SendToIMS"));
        logData.setRemark(StringUtils.join("Thread ", this.getName(), " Send data to IMS Succeed, return code = ", myTmInteraction.getOutputMessage().getImsConnectReturnCode(), ",clientId:", this.clientId));
        logData.setMessage(outData);
        FEPBase.logMessage(Level.INFO, logData);

    }

    private void resumeClearTpipe(Connection myConn) throws Exception {

        try {
            // logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
            // logData.setRemark(StringUtils.join("Thread ", this.getName(), " begin resumeClearTpipe -", this.clientId));
            // FEPBase.logMessage(Level.INFO, logData);
            LogHelperFactory.getTraceLogger().debug("begin resumeClearTpipe:" + this.getClientId() +
                    ", Timeout:" + this.processorConfiguration.getResumeInterval());
            TmInteraction myTmInteraction = myConn.createInteraction();

            //first interaction send Resume TPIPE
            String result = resumeTPipe(myTmInteraction, tranCode);

            checkData(result, logData);
            while (true) {

                if (myTmInteraction.isAckNakNeeded()) {
                    // logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
                    // logData.setRemark(StringUtils.join(StringUtils.join("Thread ", this.getName(), " Before send Ack and Receive -", this.clientId)));
                    // FEPBase.logMessage(Level.INFO, logData);

                    myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_ACK);
                    // Execute ack interaction and Receive next TX
                    myTmInteraction.execute();

                    // logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
                    // logData.setRemark(StringUtils.join("Thread ", this.getName(), " After send ack -", this.clientId,
                    //         ", returnCode=", myTmInteraction.getOutputMessage().getImsConnectReturnCode(),
                    //         ", reasonCode=", myTmInteraction.getOutputMessage().getImsConnectReasonCode()));
                    // FEPBase.logMessage(Level.INFO, logData);
                }
                //IRM Timeout or 其他的returnCode, 重新resume TPIPE
                if (!(myTmInteraction.getOutputMessage().getImsConnectReturnCode() == ApiProperties.IMS_CONNECT_RETURN_CODE_SUCCESS &&
                        myTmInteraction.getImsConnectReasonCode() == ApiProperties.IMS_CONNECT_REASON_CODE_SUCCESS)) {
                    break;
                } else {
                    // 檢查收到內容是否符合下送格式
                    OutputMessage outMsg = myTmInteraction.getOutputMessage();
                    result = StringUtil.toHex(outMsg.getDataAsByteArray());
                    logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
                    logData.setMessage(result);
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), "ack get outData -", this.clientId));
                    FEPBase.logMessage(Level.INFO, logData);
                    checkData(result, logData);

                }

            }
            LogHelperFactory.getTraceLogger().debug("exit resumeClearTpipe:" + this.clientId);
            // logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
            // logData.setRemark(StringUtils.join("Thread ", this.getName(), "exit resumeClearTpipe:", this.clientId));
            // FEPBase.logMessage(Level.INFO, logData);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " resumeClearTpipe with exception occur, ", e.getMessage()));
            FEPBase.sendEMS(logData);
            throw e;
        }
    }

    private String resumeTPipe(TmInteraction myTmInteraction, String _trancode) throws Exception {
        // logData.setProgramName(StringUtils.join(ProgramName, ".resumeTPipe"));
        // logData.setRemark(StringUtils.join("Thread ", this.getName(), " Begin resumeTPipe"));
        // FEPBase.logMessage(Level.INFO, logData);

        byte[] emptyByteArray = {};
        myTmInteraction.setImsDatastoreName(dsName);
        myTmInteraction.setLtermOverrideName(this.clientId);
        myTmInteraction.setInputMessageDataSegmentsIncludeLlzzAndTrancode(ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_INCLUDE_LLZZ_AND_TRANCODE);
        myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_RESUMETPIPE);
        myTmInteraction.setCommitMode(ApiProperties.COMMIT_MODE_0);
        // 當connectionTimeout > interactionTimeout時, 會引發receive Timeout例外
        // 若未超過, 則到connectionTimeout時間未收到資料會getImsConnectReturnCode=40的rc
        // if (Boolean.getBoolean("spring.fep.imsgw.test")) {
        //     myTmInteraction.setImsConnectTimeout(ApiProperties.TIMEOUT_10_SECONDS);
        //     myTmInteraction.setInteractionTimeout(ApiProperties.TIMEOUT_10_SECONDS + ApiProperties.TIMEOUT_1_SECOND);
        // } else {
        // 這行點掉
        // myTmInteraction.setImsConnectTimeout(ApiProperties.TIMEOUT_1_MINUTE);
        // myTmInteraction.setInteractionTimeout(ApiProperties.TIMEOUT_1_MINUTE + ApiProperties.TIMEOUT_1_SECOND);
        myTmInteraction.setImsConnectTimeout(this.processorConfiguration.getResumeInterval());
        myTmInteraction.setInteractionTimeout(this.processorConfiguration.getResumeInterval() + ApiProperties.TIMEOUT_5_SECONDS);//直到收到資料再往下
        //}

        myTmInteraction.setResumeTpipeAlternateClientId(this.clientId);
        myTmInteraction.setTrancode("");
        myTmInteraction.setResumeTpipeProcessing(ApiProperties.RESUME_TPIPE_AUTO);
        myTmInteraction.setResumeTpipeRetrievalType(ApiProperties.RETRIEVE_SYNC_OR_ASYNC_MESSAGE);
        // myTmInteraction.setResumeTpipeRetrievalType(ApiProperties.RETRIEVE_SYNC_MESSAGE_ONLY);
        myTmInteraction.setAckNakProvider(ApiProperties.CLIENT_ACK_NAK);


        InputMessage inMsg = (InputMessage) myTmInteraction.getInputMessage();
        inMsg.setInputMessageData(emptyByteArray);
        myTmInteraction.execute();
        // if (myTmInteraction.isAckNakNeeded()) {
        // myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_ACK);
        // // Execute interaction
        // myTmInteraction.execute();
        // }
        OutputMessage outMsg = myTmInteraction.getOutputMessage();
        String result = StringUtil.toHex(outMsg.getDataAsByteArray());
        LogHelperFactory.getTraceLogger().debug(StringUtils.join("resumeTPipe get outData,ImsConnectReturnCode():",
                myTmInteraction.getOutputMessage().getImsConnectReturnCode(),
                ", clientId:" + this.clientId,
                " =>" + result));
        // logData.setProgramName(StringUtils.join(ProgramName, ".resumeTPipe"));
        // logData.setMessage(result);
        // logData.setRemark(StringUtils.join("Thread ", this.getName(), " resumeTPipe get outData,getImsConnectReturnCode():", myTmInteraction.getOutputMessage().getImsConnectReturnCode()));
        // FEPBase.logMessage(Level.INFO, logData);
        return result;

    }

    private void checkData(String result, LogData logData) {
        int csmPos = result.indexOf("5CC3E2D4D6D2E85C");
        // LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] csmPos: " + csmPos);
        //int dfs2082 = result.indexOf("C4C6E2");
        // LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] dfs2082: " + dfs2082);
        // System.out.println("Have *CSMOKY*:"+csmPos);
        // this.setMessageFromIMS(result);
        if (csmPos >= 0) {
            // LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString]
            // csmPos >= 0 return result.substring(0, csmPos) end ");
            String req = result.substring(0, csmPos);

            logData.setProgramName(StringUtils.join(ProgramName, ".checkData"));
            logData.setMessage(req);
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " before sendResponseToFEP - ", this.configuration.getClientId()));
            FEPBase.logMessage(Level.INFO, logData);
            if (StringUtils.isNotBlank(req)) {

                this.executor.execute(() -> {
                    InvokeAAProcess(req);
                });
            }
            logData.setProgramName(StringUtils.join(ProgramName, ".checkData"));
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " after sendResponseToFEP - ", this.configuration.getClientId()));
            FEPBase.logMessage(Level.INFO, logData);

        } else {
            // LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString]
            // csmPos < 0 return null ");
            //return "";
        }
    }

    /**
     * 模擬收送訊息, only for Test
     */
    private FEPReturnCode simulatorReceiveAndSend(TmInteraction interaction, String messageForParameter) {
        // 模擬收送訊息
        if (configuration.isSimulatorReceiveAndSend()) {
            logData.setProgramName(StringUtils.join(ProgramName, ".run"));
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " IMS TmInteraction start to Execute, ", messageForParameter));
            FEPBase.logMessage(Level.INFO, logData);
            // (4) 開始等待接收資料
            try {
                sleep(3000L);
            } catch (InterruptedException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            this.setWaitProceedState(Boolean.TRUE, false);
            // (5) 取得接收資料
            String req = StringUtil.toHex("Hello IMS Gateway");
            LogHelperFactory.getTraceLogger().info(Const.MESSAGE_IN, req);
            // (6) 記錄FEPLOG內容
            logData.setProgramFlowType(ProgramFlow.IMSGWIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".run"));
            logData.setMessage(req);
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " IMS TmInteraction GetOutputMessage Succeed, ", messageForParameter));
            FEPBase.logMessage(Level.INFO, logData);
            // (7) CALL CBSHandler
            try {
                sleep(3000L);
            } catch (InterruptedException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            String rtnData = StringUtil.toHex("Hello IMS Center");
            // (8) 記錄FEPLOG內容
            logData.setProgramFlowType(ProgramFlow.IMSGWOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".run"));
            logData.setMessage(rtnData);
            logData.setRemark(StringUtils.join("Thread ", this.getName(), " Get Response from CBSHandler Succeed, ", messageForParameter));
            FEPBase.logMessage(Level.INFO, logData);
            // (9) 準備送回IMS
            try {
                interaction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_SENDONLY_CALLOUT_RESPONSE);
                interaction.setTrancode(StringUtils.EMPTY);
                InputMessage inMsg = interaction.getInputMessage();
                inMsg.setInputMessageData(rtnData);
                interaction.execute();
                LogHelperFactory.getTraceLogger().info(Const.MESSAGE_OUT, rtnData);
            } catch (Exception e) {
                logData.setProgramException(e);
                logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                logData.setRemark(StringUtils.join("Thread ", this.getName(), " MS TmInteraction setInputMessageData or Execute Failed, ", messageForParameter));
                FEPBase.sendEMS(logData);
                return FEPReturnCode.ProgramException;// 失敗, 則從(1)開始嘗試
            }
            return FEPReturnCode.Normal;
        }
        return null;
    }

    private String getTransCode(String pCode) {
        String transCode = "FG";

        if (StringUtils.trimToEmpty(pCode).length() != 4) {
            return transCode;
        }

        if (StringUtils.indexOf(pCode, "24") == 0) {
            transCode = "WW";
        } else if (StringUtils.indexOf(pCode, "26") == 0) {
            transCode = "WW";
        } else if (StringUtils.indexOf(pCode, "252") == 0) {
            transCode = "TR";
        } else if (StringUtils.indexOf(pCode, "256") == 0 && !StringUtils.equals(pCode, "2566")) {
            transCode = "TX";
        } else {
            switch (pCode) {
                case "2510":
                    transCode = "WD";
                    break;
                case "2555":
                case "2556":
                    transCode = "TR";
                    break;
                case "2120":
                case "2130":
                case "2140":
                case "2150":
                case "2160":
                case "2270":
                case "2280":
                case "2290":
                case "2547":
                case "2549":
                case "2573":
                case "2574":
                    transCode = "RV";
                    break;
                case "2531":
                case "2532":
                case "2541":
                case "2542":
                case "2543":
                case "2551":
                case "2552":
                    transCode = "TX";
                    break;
                case "2261":
                case "2262":
                case "2263":
                case "2264":
                case "2566":
                    transCode = "PY";
                    break;
                case "2505":
                case "2545":
                case "2546":
                case "2571":
                case "2572":
                    transCode = "WW";
                    break;
            }
        }

        return transCode;
    }

    /**
     * 十六進制字串轉字節數組
     *
     * @param hexString
     * @return
     */
    private static final String HEX_STRING = "0123456789ABCDEF";

    public static byte[] hexToBytes(String hexString) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexString.length() / 2);
        for (int i = 0; i < hexString.length(); i += 2) {
            baos.write((HEX_STRING.indexOf(hexString.charAt(i)) << 4 | HEX_STRING.indexOf(hexString.charAt(i + 1))));
        }
        return baos.toByteArray();
    }

    /**
     * 定時PING IMS, 從sendToIMS成功之後開始計時
     *
     * @param logData
     * @param initialDelay
     */
    private void scheduleToPingIMS(LogData logData, long initialDelay) {
        if (pingIMSTimer == null) {
            pingIMSTimer = new PingIMSTimer(StringUtils.join(this.getName(), "-PingIMS"));
        }
        pingIMSTimer.setData(this.connection, logData);
        pingIMSTimer.scheduleAtFixedRate(initialDelay + 100, this.pingIMSInterval, TimeUnit.MILLISECONDS);
    }

    private class PingIMSTimer extends AbstractScheduledTask {
        private Connection _Connection;
        private LogData _LogData;

        public PingIMSTimer(String taskName) {
            super(taskName);
        }

        public void setData(Connection imsConnection, LogData logData) {
            this._Connection = imsConnection;
            this._LogData = logData;
        }

        /**
         * Execute Task
         */
        @Override
        public void execute() {
            LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
            try {
                _LogData.setProgramName("PingIMSTimer.execute");
                _LogData.setRemark(StringUtils.join("Timer ", this.taskName, " Begin Ping IMS"));
                FEPBase.logMessage(Level.INFO, _LogData);

                TmInteraction myTmInteraction;
                myTmInteraction = _Connection.createInteraction();
                myTmInteraction.setInputMessageDataSegmentsIncludeLlzzAndTrancode(ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_NOT_INCLUDE_LLZZ_AND_TRANCODE);
                myTmInteraction.setTrancode("");
                byte[] indata = (new String("PING IMS_CONNECT").getBytes(ApiProperties.DEFAULT_IMS_CONNECT_CODEPAGE));

                // get InputMessage instance from myTMInteraction
                InputMessage inMsg = myTmInteraction.getInputMessage();

                // populate input message data with indata byte array
                inMsg.setInputMessageData(indata);
                myTmInteraction.execute();
                OutputMessage outMsg = myTmInteraction.getOutputMessage();
                String outStr = getOutputString(outMsg);
                _LogData.setMessage(outStr);
                _LogData.setRemark(StringUtils.join("Timer ", this.taskName, " Get Ping Result"));
                FEPBase.logMessage(Level.INFO, _LogData);
                if (outMsg.getImsConnectReturnCode() == 0 && StringUtils.contains(outStr, "PING RESPONSE")) {
                    // ping成功, 不做任何處理
                    pingIMSResult.set(true);
                } else {
                    // PING失敗, 直接丟異常
                    throw ExceptionUtil.createException("PING Failed");
                }
            } catch (Exception e) {
                _LogData.setRemark(StringUtils.join("Timer ", this.taskName, " Ping with exception occur, ", e.getMessage()));
                FEPBase.logMessage(Level.ERROR, _LogData);
                // 有catch到異常則直接斷線, 並且停止Timer
                pingIMSResult.set(false);
                synchronized (messageToIMSList) {
                    messageToIMSList.notifyAll();
                }
                this.cancel();
            }
        }

        private String getOutputString(OutputMessage outMsg) throws ImsConnectApiException {
            String result;
            try {
                result = new String(outMsg.getDataAsByteArray(), ApiProperties.DEFAULT_IMS_CONNECT_CODEPAGE);

                int csmPos = result.indexOf("*CSMOKY*");
                if (csmPos < 0) {
                    return null;
                } else {
                    return result.substring(0, csmPos);
                }
            } catch (UnsupportedEncodingException e) {

                return null;
            }
        }
    }
}
