package com.syscom.fep.server.common.adapter;

import com.google.gson.Gson;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.exception.FEPBaseException;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.jms.JmsConfigurationProperties;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.jms.vendor.MQFactory;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.scheduler.AbstractScheduledTask;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.invoker.FEPInvoker;
import com.syscom.fep.invoker.SimpleNettyClientFactory;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.instance.fisc.receiver.FISCQueueReceiverConfiguration;
import com.syscom.fep.jms.instance.fisc.receiver.alternative.FISCQueueReceiverAlternativeConfiguration;
import com.syscom.fep.vo.communication.ToFISCCommu;
import com.syscom.fep.vo.enums.RestfulResultCode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.event.Level;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 本模組目的為傳送電文至財金,並接收財金回應訊息,拆解過後傳回前端
 *
 * @author Ashiang & Richard
 */
public class FISCAdapter extends AdapterBase {
    /**
     * 從財金回來的電文
     */
    private String messageFromFISC;
    /**
     * 送給財金的電文
     */
    private String messageToFISC;
    /**
     * 財金STAN
     */
    private String stan;
    /**
     * 目前交易財金電文序號
     */
    private String fiscNo;
    /**
     * 目前交易之匯款行
     */
    private String bankNo;
    /**
     * 目前交易之通匯序號
     */
    private String rmNo;
    /**
     * 交易電文物件
     */
    private final MessageBase txData;
    // 當前財金電文交易序號
    private static final AtomicInteger currentFiscNo = new AtomicInteger();
    private static final Map<String, String> currentRmNo = new ConcurrentHashMap<>();
    /**
     * FISCAdapter配置類
     */
    private static final FISCAdapterConfiguration configuration;
    private final FEPInvoker invoker = SpringBeanFactoryUtil.getBean(FEPInvoker.class);
    private static ExecutorService executor;
    private static FISCAdapterNodeHealthCheckTimer timer;
    private ToFISCCommu toFISCCommu;
    private static final List<FiscNode> nodes; // 初始化時由配置檔讀入
    // 全域共享的輪詢計數器
    private static final AtomicInteger globalCounter = new AtomicInteger(0);
    private int totalSendTimes = 0; // 一共送了多少次電文到FISCGW, 用於列印

    static {
        SimpleNettyClientFactory.registerToFISCComponents(); // 2026-03-17 Richard add 這裡提前註冊ToFISC元件
        configuration = SpringBeanFactoryUtil.registerBean(FISCAdapterConfiguration.class);
        nodes = configuration.getNodes();
    }

    public FISCAdapter(MessageBase txData) {
        this.txData = txData;
    }

    public String getMessageFromFISC() {
        return messageFromFISC;
    }

    public void setMessageFromFISC(String messageFromFISC) {
        this.messageFromFISC = messageFromFISC;
    }

    public String getMessageToFISC() {
        return messageToFISC;
    }

    public void setMessageToFISC(String messageToFISC) {
        this.messageToFISC = messageToFISC;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getFiscNo() {
        return fiscNo;
    }

    public void setFiscNo(String fiscNo) {
        this.fiscNo = fiscNo;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getRmNo() {
        return rmNo;
    }

    public void setRmNo(String rmNo) {
        this.rmNo = rmNo;
    }

    @Override
    public FEPReturnCode sendReceive() {
        // 2025-10-17 Richard modified 修正取PCode, 應該要轉Ascii
        String pcode = null;
        if (FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic) {
            pcode = EbcdicConverter.fromHex(CCSID.English, messageToFISC.substring(14, 14 + 8));
        } else {
            try {
                pcode = StringUtil.fromHex(messageToFISC.substring(14, 14 + 8));
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
        }
        long startTime = System.currentTimeMillis();
        int totalNodes = nodes.size();
        try {
            // --- 階段 1: 如果有指定 ClientId, 先取出該clientId節點進行嘗試 ---
            String clientID = ((FISCData) this.txData).getRClientID();
            String logging = StringUtils.join(",ej:", ej, ",pcode:", pcode, ",stan:", stan, ",clientID:", clientID, ",timeout:", timeout);
            if (clientID != null) {
                FiscNode target = findNodeByClientId(nodes, clientID);
                if (target != null && isAlive(target)) {
                    try {
                        this.messageFromFISC = executeNodeWithRetry(this.txData.getLogContext(), target, pcode, logging);
                        return FEPReturnCode.Normal;
                    } catch (Exception e) {
                        this.txData.getLogContext().setMessage(messageToFISC);
                        this.txData.getLogContext().setEj(ej);
                        this.txData.getLogContext().setpCode(pcode);
                        this.txData.getLogContext().setStan(stan);
                        this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
                        this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
                        this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
                        this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, " 指定節點 ", clientID, " 交易失敗: ", e.getMessage(), ". 系統將嘗試從其他可用節點重試", logging));
                        logMessage(Level.WARN, this.txData.getLogContext());
                    }
                }
            }
            // --- 階段 2: 輪詢與逾時控制
            // 只取一次全域索引作為「本次交易的起點」
            int startIndex = getNextSafeIndex(totalNodes);
            int attempts = 0;  //紀錄已嘗試過的節點數量
            while (true) {
                // A. 逾時檢查
                if (this.timeout > 0 && (System.currentTimeMillis() - startTime > this.timeout * 1000L)) {
                    throw ExceptionUtil.createSocketTimeoutException(this.timeout, " seconds 內無法取得可用連線");
                }
                // B. 節點檢查 (當 timeout=0 時, 確保不會在同一次交易中重複嘗試同一個節點)
                if (attempts >= totalNodes) {
                    if (this.timeout <= 0) {
                        throw ExceptionUtil.createException("Timeout=0, 己走過所有節點, 無可用連線");
                    }
                    // 若還有時間但全掛, Sleep後重啟一輪嘗試
                    Thread.sleep(configuration.getDelay() * 1000L);
                    attempts = 0; // 重置計數, 開始下一輪掃描
                    continue;
                }
                // C. 計算本次偏移目標： startIndex + 偏移量
                // 輪詢的演算法, 採用每筆交易專屬的 「固定線性循環」作法, 先從全域globalIndex取出起始index, 再用局部變數控制下一index來遍歷node list的方式 ,
                // 比如一開始取到globalIndex = 2 時, 則輪詢時下一Index的順序一定為2->0->1->2->0->1…, 如下表所示:
                // 嘗試序號 (attempts)        計算式            結果 (targetIdx)
                // 第 1 次 (初始 attempts=0)  (2 + 0) % 3      2
                // 第 2 次 (attempts=1)      (2 + 1) % 3      0
                // 第 3 次 (attempts=2)      (2 + 2) % 3      1
                // 第 4 次 (attempts=3)      (2 + 3) % 3      2 (開始重複)
                // 公式為：targetIdx = (startIndex + attempts) % totalNodes
                int targetIdx = (startIndex + attempts) % totalNodes;
                FiscNode node = nodes.get(targetIdx);
                attempts++;
                // D. 狀態檢查：只挑選目前「活著」的節點
                if (!isAlive(node)) {
                    continue;
                }
                // E. 執行交易
                try {
                    this.messageFromFISC = executeNodeWithRetry(this.txData.getLogContext(), node, pcode, logging);
                    return FEPReturnCode.Normal;
                } catch (Exception e) {
                    // 當前節點 (Primary & Alternative) 都失敗了, 繼續 Loop 嘗試下一個 targetIdx
                    this.txData.getLogContext().setMessage(messageToFISC);
                    this.txData.getLogContext().setEj(ej);
                    this.txData.getLogContext().setpCode(pcode);
                    this.txData.getLogContext().setStan(stan);
                    this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
                    this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
                    this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
                    this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, "節點 ", node.getClientId(), " 全線失敗, 嘗試下一個節點", logging));
                    logMessage(Level.WARN, this.txData.getLogContext());
                }
            }
        } catch (Exception e) {
            this.txData.getLogContext().setMessage(messageToFISC);
            this.txData.getLogContext().setEj(ej);
            this.txData.getLogContext().setpCode(pcode);
            this.txData.getLogContext().setStan(stan);
            this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
            this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
            this.txData.getLogContext().setProgramException(e);
            this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, ".sendReceive exception occur"));
            if (e instanceof SocketTimeoutException) {
                this.txData.getLogContext().setReturnCode(FEPReturnCode.FISCTimeout);
            } else if (this.txData.getLogContext().getReturnCode() == null) {
                this.txData.getLogContext().setReturnCode(FEPReturnCode.ProgramException);
            }
            sendEMS(this.txData.getLogContext());
            return this.txData.getLogContext().getReturnCode();
        }
    }

    /**
     * 單一節點內部的 Primary -> Alternative 切換
     */
    private String executeNodeWithRetry(LogData logData, FiscNode node, String pcode, String logging) throws Exception {
        // 第一次嘗試 (依狀態決定 Host)
        String host = node.getTargetHost();
        try {
            return sendMsgToFISC(logData, pcode, host, node.getPort(), logging);
        } catch (Exception e) {
            FEPReturnCode rtnCode = handleException(logData, pcode, host, node.getPort(), e, logging); // 處理異常獲取ReturnCode
            // 如果第一次失敗且是 HEALTHY, 切換至 DEGRADED 試 Alternative
            if (node.getStatus() == NodeStatus.HEALTHY) {
                // 標記降級, 準備嘗試備援 IP
                node.setStatus(NodeStatus.DEGRADED);
                String secondaryHost = node.getSecondaryHost();
                logData.setMessage(messageToFISC);
                logData.setEj(ej);
                logData.setpCode(pcode);
                logData.setStan(stan);
                logData.setProgramFlowType(ProgramFlow.AdapterIn);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramName(StringUtils.join(ProgramName, ".executeNodeWithRetry"));
                logData.setRemark(StringUtils.join(ProgramName, " ClientID ", node.getClientId(), " Primary 節點 ", host, " 失敗，切換至備援 IP: ", secondaryHost, logging));
                logData.setProgramException(e);
                logData.setReturnCode(rtnCode);
                logMessage(Level.WARN, logData);
                try {
                    return sendMsgToFISC(logData, pcode, secondaryHost, node.getPort(), logging);
                } catch (Exception ex) {
                    rtnCode = handleException(logData, pcode, host, node.getPort(), ex, logging); // 處理異常獲取ReturnCode
                    node.setStatus(NodeStatus.DOWN); // Secondary也掛, 此節點標示為DOWN
                    Exception throwable = ExceptionUtil.createException(ex, "ClientID: ", node.getClientId(), " Primary IP ", host, " 及 Secondary IP ", secondaryHost, " 都無法提供服務");
                    logData.setMessage(messageToFISC);
                    logData.setEj(ej);
                    logData.setpCode(pcode);
                    logData.setStan(stan);
                    logData.setProgramFlowType(ProgramFlow.AdapterIn);
                    logData.setMessageFlowType(MessageFlow.Request);
                    logData.setProgramName(StringUtils.join(ProgramName, ".executeNodeWithRetry"));
                    logData.setRemark(StringUtils.join(ProgramName, StringUtils.SPACE, throwable.getMessage(), logging));
                    logData.setProgramException(throwable);
                    logData.setReturnCode(rtnCode);
                    sendEMS(logData);
                    throw throwable;
                }
            } else {
                // 原本就是 DEGRADED 還失敗, 直接判死
                node.setStatus(NodeStatus.DOWN);
                Exception throwable = ExceptionUtil.createException("ClientID: ", node.getClientId(), " 備援節點 IP ", host, " failed");
                logData.setMessage(messageToFISC);
                logData.setEj(ej);
                logData.setpCode(pcode);
                logData.setStan(stan);
                logData.setProgramFlowType(ProgramFlow.AdapterIn);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramName(StringUtils.join(ProgramName, ".executeNodeWithRetry"));
                logData.setRemark(StringUtils.join(ProgramName, StringUtils.SPACE, throwable.getMessage(), logging));
                logData.setProgramException(throwable);
                logData.setReturnCode(rtnCode);
                sendEMS(logData);
                throw throwable;
            }
        }
    }

    /**
     * 取下一個節點Index,可循環重置Index,跳到最大號後歸0
     *
     * @param poolSize
     * @return
     */
    private int getNextSafeIndex(int poolSize) {
        while (true) {
            int current = globalCounter.get();
            int next = (current == Integer.MAX_VALUE) ? 0 : current + 1;
            if (globalCounter.compareAndSet(current, next)) {
                return current % poolSize;
            }
        }
    }

    /**
     * 判斷節點的狀態是否是Alive
     *
     * @param n
     * @return
     */
    private boolean isAlive(FiscNode n) {
        return n.getStatus() == NodeStatus.HEALTHY || n.getStatus() == NodeStatus.DEGRADED;
    }

    /**
     * 根據傳入的ClientID獲取對應的節點設定
     *
     * @param nodes
     * @param clientId
     * @return
     */
    private FiscNode findNodeByClientId(List<FiscNode> nodes, String clientId) {
        return nodes.stream().filter(n -> n.getClientId().equals(clientId)).findFirst().orElse(null);
    }

    private FEPReturnCode handleException(LogData logData, String pcode, String host, int port, Exception e, String logging) {
        FEPReturnCode rtnCode = CommonReturnCode.ProgramException;
        // 走socket無法連線FISCGateway
        if (e instanceof FEPBaseException) {
            FEPBaseException fepBaseException = (FEPBaseException) e;
            if (fepBaseException.getRtnCode() != null) {
                rtnCode = fepBaseException.getRtnCode();
            }
        } else if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
            rtnCode = FEPReturnCode.FISCGWATMSendError;
        } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
            rtnCode = FEPReturnCode.FISCTimeout;
        } else if (e instanceof SocketTimeoutException) {
            rtnCode = FEPReturnCode.FISCTimeout;
        }
        // 發生例外時需sendEMS
        logData.setEj(ej);
        logData.setpCode(pcode);
        logData.setStan(stan);
        logData.setProgramName(StringUtils.join(ProgramName, ".FISCGatewayHostGroup.sendReceive"));
        logData.setProgramException(e);
        logData.setMessage(messageToFISC);
        logData.setReturnCode(rtnCode);
        logData.setRemark(StringUtils.join("FISCAdapter SendReceive Failed to FISCGateway at [", totalSendTimes, "] times:",
                configuration.getProtocol() == Protocol.restful ? FormatUtil.messageFormat(configuration.getSvcUrl(), host, port) : StringUtils.join(host, ":", port), logging));
        sendEMS(logData);
        return rtnCode;
    }

    /**
     * 發送訊息到FISCGW
     *
     * @param logData
     * @param pcode
     * @param host
     * @param port
     * @param logging
     * @return
     * @throws Exception
     */
    private String sendMsgToFISC(LogData logData, String pcode, String host, int port, String logging) throws Exception {
        logData.setMessage(messageToFISC);
        logData.setEj(ej);
        logData.setpCode(pcode);
        logData.setStan(stan);
        logData.setProgramFlowType(ProgramFlow.AdapterIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".sendMsgToFISC"));
        logData.setRemark(StringUtils.join(ProgramName, " Before Send Message to FISCGateway:",
                configuration.getProtocol() == Protocol.restful ? FormatUtil.messageFormat(configuration.getSvcUrl(), host, port) : StringUtils.join(host, ":", port), logging));
        logMessage(logData);
        boolean bAllowSend = false;
        int sendFiscNo = 0;
        // 如果有傳FISCNo則先檢查要送出的FiscNo是否連續,若不連續則等進入迴圈等至Timeout
        if (StringUtils.isNotBlank(fiscNo)) {
            sendFiscNo = Integer.parseInt(fiscNo);
            Calendar now = Calendar.getInstance();
            // LogHelperFactory.getTraceLogger().info("fiscNo:", Integer.parseInt(fiscNo), ", currentFiscNo:", currentFiscNo.get());
            logData.setMessage(messageToFISC);
            logData.setEj(ej);
            logData.setpCode(pcode);
            logData.setStan(stan);
            logData.setProgramFlowType(ProgramFlow.AdapterIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendMsgToFISC"));
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setRemark(StringUtils.join("fiscNo:", Integer.parseInt(fiscNo), ", currentFiscNo:", currentFiscNo.get(), logging));
            logMessage(Level.DEBUG, logData);
            while (CalendarUtil.getDiffMilliseconds(Calendar.SECOND, Calendar.getInstance().getTimeInMillis() - now.getTimeInMillis()) < timeout) {
                if (sendFiscNo == currentFiscNo.get()) {
                    throw ExceptionUtil.createException("財金電文序號重覆,不允許傳送");
                } else if (sendFiscNo == currentFiscNo.get() + 1) {
                    bAllowSend = true;
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
        } else {
            bAllowSend = true; // 沒傳FiscNo直接放行
        }
        if (bAllowSend) {
            totalSendTimes++;
            // LogHelperFactory.getTraceLogger().trace("FISCAdapter Before SendReceive:", messageToFISC);
            logData.setMessage(messageToFISC);
            logData.setEj(ej);
            logData.setpCode(pcode);
            logData.setStan(stan);
            logData.setProgramFlowType(ProgramFlow.AdapterIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendMsgToFISC"));
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setRemark(StringUtils.join("FISCAdapter SendReceive to FISCGateway at [", totalSendTimes, "] times:",
                    configuration.getProtocol() == Protocol.restful ? FormatUtil.messageFormat(configuration.getSvcUrl(), host, port) : StringUtils.join(host, ":", port), logging));
            logMessage(logData);
            // 送出電文後,不管是用socket或restful,都不用等回應, 所以timeout直接塞入0就好
            invoker.sendReceiveToFISCGW(this.createToFISCCommu(logData, host, port), 0);
            logData.setMessage(messageToFISC);
            logData.setEj(ej);
            logData.setpCode(pcode);
            logData.setStan(stan);
            logData.setProgramFlowType(ProgramFlow.AdapterIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendMsgToFISC"));
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setRemark(StringUtils.join("FISCAdapter SendReceive to FISCGateway Succeed at [", totalSendTimes, "] times:",
                    configuration.getProtocol() == Protocol.restful ? FormatUtil.messageFormat(configuration.getSvcUrl(), host, port) : StringUtils.join(host, ":", port), logging));
            logMessage(logData);
            if (sendFiscNo > 0) {
                currentFiscNo.set(sendFiscNo);
                // LogHelperFactory.getTraceLogger().info("sendFiscNo > 0  fiscNo:", sendFiscNo, ", currentFiscNo:", currentFiscNo.get());
                logData.setMessage(messageToFISC);
                logData.setEj(ej);
                logData.setpCode(pcode);
                logData.setStan(stan);
                logData.setProgramFlowType(ProgramFlow.AdapterIn);
                logData.setProgramName(StringUtils.join(ProgramName, ".sendMsgToFISC"));
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setRemark(StringUtils.join("sendFiscNo > 0  fiscNo:", sendFiscNo, ", currentFiscNo:", currentFiscNo.get()));
                logMessage(Level.DEBUG, logData);
                currentRmNo.put(bankNo, rmNo);
            }
            // 若傳入之timeout=0, 代表不用等待財金回應, 直接返回空字串
            if (timeout == 0) { // 2021-06-17 Richard add mark, timeout為0表示不需要等待回應
                return StringUtils.EMPTY;
            }
            // 若timeout>0, 送出成功後開始從receiver的QueueManager中等待FISCGW回應同一stan的訊息至timeout為止,
            // 收到電文時, 將電文設定到messageFromFISC變數中,並把receiver.alternative的QueueManager中同一stan的訊息一併移除
            else {
                // 連至本機MQ等待fiscgw送回Response訊息
                String rtn = this.receiveFromQueue(logData, ej, pcode, stan, timeout, logging);
                logData.setMessage(rtn);
                logData.setEj(ej);
                logData.setpCode(pcode);
                logData.setStan(stan);
                logData.setProgramName(StringUtils.join(ProgramName, ".sendMsgToFISC"));
                logData.setProgramFlowType(ProgramFlow.AdapterOut);
                logData.setMessageFlowType(MessageFlow.Response);
                logData.setRemark(StringUtils.join(ProgramName, " Receive Message from Queue", logging));
                logMessage(logData);
                // 收取到訊息後, 再連至連一台AP Server的Resposnse Queue, 收取同一筆correlationId的訊息, Timeout時間設為5秒, 此收取動作請以非同步方式進行,不影響原交易的處理動作
                this.receiveFromQueueAlternative(logData, ej, pcode, stan, timeout, logging);
                return rtn;
            }
        } else {
            throw ExceptionUtil.createException("財金電文序號不連續,不允許傳送");
        }
    }

    /**
     * 建立ToFISCCommu物件
     *
     * @param logData
     * @param host
     * @param port
     * @return
     */
    private ToFISCCommu createToFISCCommu(LogData logData, String host, int port) {
        this.toFISCCommu = new ToFISCCommu();
        this.toFISCCommu.setEj(ej);
        this.toFISCCommu.setMessage(messageToFISC);
        this.toFISCCommu.setMessageId(logData.getMessageId());
        this.toFISCCommu.setStan(stan);
        this.toFISCCommu.setTimeout(timeout);
        this.toFISCCommu.setTxRquid(logData.getTxRquid());
        // 以下幾個用於不同的Protocol
        this.toFISCCommu.setProtocol(configuration.getProtocol());
        this.toFISCCommu.setRestfulUrl(FormatUtil.messageFormat(configuration.getSvcUrl(), host, port));
        this.toFISCCommu.setHost(host);
        this.toFISCCommu.setPort(port);
        return this.toFISCCommu;
    }

    /**
     * 1. 但若timeout>0代表會有Response, 連至本機MQ等待fiscgw送回Response訊息
     * 2. 收取訊息的correlationId為此筆交易的Stan
     * 3. 等待MQ的時間就是該筆交易的Timeout時間, 若超過時間未收到則引發TimeoutException
     *
     * @param logData
     * @param ej
     * @param pcode
     * @param stan
     * @param timeout
     * @param logging
     * @return
     */
    private String receiveFromQueue(final LogData logData, final Integer ej, final String pcode, final String stan, final int timeout, final String logging) { // throws Exception {
        String response = StringUtils.EMPTY;
        // timeout為0表示不需要等待回應
        if (timeout == 0) {
            return response;
        }
        String destination = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class).getQueueNames().getFisc().getDestination();
        long receiveTimeout = timeout * 1000L;
        // 記錄FEPLOG
        logData.setEj(ej);
        logData.setpCode(pcode);
        logData.setStan(stan);
        logData.setProgramFlowType(ProgramFlow.AdapterIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
        logData.setRemark(StringUtils.join(ProgramName, " wait to receive from Queue, receiveTimeout:", receiveTimeout, ",destination:", destination, logging));
        logMessage(logData);
        // try {
        //     FISCQueueReceiverOperator operator = SpringBeanFactoryUtil.getBean(FISCQueueReceiverOperator.class);
        //     JmsProperty property = new JmsProperty();
        //     property.setMessageSelector(JmsFactory.getSelectorForCorrelationID(stan, StandardCharsets.UTF_8.name()));
        //     property.setReceiveTimeout(receiveTimeout);
        //     long currentTimeMillis = System.currentTimeMillis();
        //     // 注意下面的receiveQueueSelected方法會一直wait到timeout, 但是不會丟異常訊息, 所以這裡要自己判斷
        //     PlainTextMessage payload = (PlainTextMessage) operator.receiveQueue(destination, property, null);
        //     if (System.currentTimeMillis() - currentTimeMillis > receiveTimeout) {
        //         response = StringUtils.EMPTY; // 這裡直接給空字串
        //         logData.setEj(ej);
        //         logData.setpCode(pcode);
        //         logData.setStan(stan);
        //         logData.setProgramFlowType(ProgramFlow.AdapterOut);
        //         logData.setMessageFlowType(MessageFlow.Response);
        //         logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
        //         logData.setRemark(StringUtils.join(ProgramName, " receive from Queue timeout, receiveTimeout:", receiveTimeout, ",destination:", destination, logging));
        //         logMessage(Level.ERROR, logData);
        //         throw ExceptionUtil.createTimeoutException("Receive Queue timeout, destination:", destination);
        //     } else {
        //         response = payload.getPayload();
        //         logData.setEj(ej);
        //         logData.setpCode(pcode);
        //         logData.setStan(stan);
        //         logData.setProgramFlowType(ProgramFlow.AdapterOut);
        //         logData.setMessageFlowType(MessageFlow.Response);
        //         logData.setMessage(response);
        //         logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
        //         logData.setRemark(StringUtils.join(ProgramName, " receive from Queue, receiveTimeout:", receiveTimeout, ",destination:", destination, logging));
        //         logMessage(logData);
        //     }
        // } catch (Exception e) {
        //     logData.setEj(ej);
        //     logData.setpCode(pcode);
        //     logData.setStan(stan);
        //     logData.setProgramException(e);
        //     logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
        //     logData.setRemark(StringUtils.join("receive from Queue with exception occur, ", e.getMessage()));
        //     sendEMS(logData);
        //     throw e;
        // }
        try {
            FISCQueueReceiverConfiguration fiscQueueReceiverConfiguration = SpringBeanFactoryUtil.getBean(FISCQueueReceiverConfiguration.class);
            PlainTextMessage payload = receiveFromQueue(fiscQueueReceiverConfiguration.getProp(), destination, stan, receiveTimeout);
            response = payload.getPayload();
            logData.setEj(ej);
            logData.setpCode(pcode);
            logData.setStan(stan);
            logData.setProgramFlowType(ProgramFlow.AdapterOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(response);
            logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
            logData.setRemark(StringUtils.join(ProgramName, " receive from Queue, receiveTimeout:", receiveTimeout, ",destination:", destination, logging));
            logMessage(logData);
        } catch (SocketTimeoutException e) {
            logData.setEj(ej);
            logData.setpCode(pcode);
            logData.setStan(stan);
            logData.setProgramFlowType(ProgramFlow.AdapterOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
            logData.setRemark(StringUtils.join(ProgramName, " receive from Queue timeout, receiveTimeout:", receiveTimeout, ",destination:", destination, logging));
            // 2026-04-01 Richard modified 收queue timeout就只sendEMS by Ashiang
            // logMessage(Level.ERROR, logData);
            // throw ExceptionUtil.createTimeoutException(e, "Receive Queue timeout, destination:", destination);
            sendEMS(Level.WARN, logData);
        } catch (Exception e) {
            logData.setEj(ej);
            logData.setpCode(pcode);
            logData.setStan(stan);
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
            logData.setRemark(StringUtils.join("receive from Queue with exception occur, ", e.getMessage()));
            sendEMS(logData);
            // 2026-04-01 Richard modified 这里不要丢异常, 如果send成功己進開始等queue了, 就不能再重送了 by Ashiang
            // throw e;
        }
        return response;
    }

    /**
     * 連至連一台AP Server的Resposnse Queue, 收取同一筆correlationId的訊息, Timeout時間設為5秒, 此收取動作請以非同步方式進行,不影響原交易的處理動作
     *
     * @param logData
     * @param ej
     * @param pcode
     * @param stan
     * @param timeout
     * @param logging
     */
    private void receiveFromQueueAlternative(final LogData logData, final Integer ej, final String pcode, final String stan, final int timeout, final String logging) {
        // timeout為0表示不需要等待回應
        if (timeout == 0) {
            return;
        }
        // 這裡先將預設的MDC取出來, 下面再put進去, 否則log檔名會跑掉
        final Map<String, String> map = getMDCKeptValue();
        executor.execute(() -> {
            LogMDC.put(map);
            String destination = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class).getQueueNames().getFiscAlternative().getDestination();
            long receiveTimeout = configuration.getAlternativeQueueReceiveTimeout();
            // 記錄FEPLOG
            logData.setEj(ej);
            logData.setpCode(pcode);
            logData.setStan(stan);
            logData.setProgramFlowType(ProgramFlow.AdapterIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
            logData.setRemark(StringUtils.join(ProgramName, " wait to receive from Queue, receiveTimeout:", receiveTimeout, ",destination:", destination, logging));
            logMessage(logData);
            // try {
            //     FISCQueueReceiverAlternativeOperator operator = SpringBeanFactoryUtil.getBean(FISCQueueReceiverAlternativeOperator.class);
            //     JmsProperty property = new JmsProperty();
            //     property.setMessageSelector(JmsFactory.getSelectorForCorrelationID(stan, StandardCharsets.UTF_8.name()));
            //     property.setReceiveTimeout(receiveTimeout);
            //     long currentTimeMillis = System.currentTimeMillis();
            //     // 注意下面的receiveQueueSelected方法會一直wait到timeout, 但是不會丟異常訊息, 所以這裡要自己判斷
            //     PlainTextMessage payload = (PlainTextMessage) operator.receiveQueue(destination, property, null);
            //     if (System.currentTimeMillis() - currentTimeMillis > receiveTimeout) {
            //         String response = StringUtils.EMPTY; // 這裡直接給空字串
            //         logData.setEj(ej);
            //         logData.setpCode(pcode);
            //         logData.setStan(stan);
            //         logData.setProgramFlowType(ProgramFlow.AdapterOut);
            //         logData.setMessageFlowType(MessageFlow.Response);
            //         logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
            //         logData.setRemark(StringUtils.join(ProgramName, " receive from Queue timeout, receiveTimeout:", receiveTimeout, ",destination:", destination, logging));
            //         logMessage(Level.WARN, logData);
            //     } else {
            //         String response = payload.getPayload();
            //         logData.setEj(ej);
            //         logData.setpCode(pcode);
            //         logData.setStan(stan);
            //         logData.setProgramFlowType(ProgramFlow.AdapterOut);
            //         logData.setMessageFlowType(MessageFlow.Response);
            //         logData.setMessage(response);
            //         logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
            //         logData.setRemark(StringUtils.join(ProgramName, " receive from Queue, receiveTimeout:", receiveTimeout, ",destination:", destination, logging));
            //         logMessage(logData);
            //     }
            // } catch (Exception e) {
            //     logData.setEj(ej);
            //     logData.setpCode(pcode);
            //     logData.setStan(stan);
            //     logData.setProgramException(e);
            //     logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
            //     logData.setRemark(StringUtils.join("receive from Queue with exception occur, ", e.getMessage()));
            //     sendEMS(Level.WARN, logData);
            // }
            try {
                FISCQueueReceiverAlternativeConfiguration fiscQueueReceiverAlternativeConfiguration = SpringBeanFactoryUtil.getBean(FISCQueueReceiverAlternativeConfiguration.class);
                PlainTextMessage payload = receiveFromQueue(fiscQueueReceiverAlternativeConfiguration.getProp(), destination, stan, receiveTimeout);
                String response = payload.getPayload();
                logData.setEj(ej);
                logData.setpCode(pcode);
                logData.setStan(stan);
                logData.setProgramFlowType(ProgramFlow.AdapterOut);
                logData.setMessageFlowType(MessageFlow.Response);
                logData.setMessage(response);
                logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
                logData.setRemark(StringUtils.join(ProgramName, " receive from Queue, receiveTimeout:", receiveTimeout, ",destination:", destination, logging));
                logMessage(logData);
            } catch (SocketTimeoutException e) {
                logData.setEj(ej);
                logData.setpCode(pcode);
                logData.setStan(stan);
                logData.setProgramFlowType(ProgramFlow.AdapterOut);
                logData.setMessageFlowType(MessageFlow.Response);
                logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
                logData.setRemark(StringUtils.join(ProgramName, " receive from Queue timeout, receiveTimeout:", receiveTimeout, ",destination:", destination, logging));
                logMessage(Level.WARN, logData);
            } catch (Exception e) {
                logData.setEj(ej);
                logData.setpCode(pcode);
                logData.setStan(stan);
                logData.setProgramException(e);
                logData.setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
                logData.setRemark(StringUtils.join("receive from Queue with exception occur, ", e.getMessage()));
                sendEMS(Level.WARN, logData);
            }
        });
    }

    /**
     * 收取同一筆correlationId的訊息, 注意如果有超時會丟TimeoutException
     *
     * @param properties
     * @param queueName
     * @param correlationId
     * @param waitInterval
     * @return
     * @throws Exception
     */
    private PlainTextMessage receiveFromQueue(JmsConfigurationProperties properties, String queueName, String correlationId, long waitInterval) throws Exception {
        String payload = MQFactory.receiveQueue(properties, queueName, correlationId, (int) waitInterval);
        return new Gson().fromJson(payload, PlainTextMessage.class);
    }

    /**
     * 初始化財金電文交易序號
     *
     * @param logData
     * @param fiscNo
     */
    public static void initialFiscNo(LogData logData, String fiscNo) {
        LogHelperFactory.getTraceLogger().info("before set currentFiscNo, input fiscNo = [", fiscNo, "], currentFiscNo = [", currentFiscNo.get(), "]");
//        logData.setProgramName(StringUtils.join(FISCAdapter.class.getSimpleName(), ".initialFiscNo"));
//        logData.setProgramFlowType(ProgramFlow.AdapterIn);
//        logData.setMessageFlowType(MessageFlow.Request);
//        logData.setRemark(StringUtils.join("before set currentFiscNo, input fiscNo = [", fiscNo, "], currentFiscNo = [", currentFiscNo.get(), "]"));
//        logMessage(Level.DEBUG, logData);
        currentFiscNo.set(Integer.parseInt(fiscNo));
        LogHelperFactory.getTraceLogger().info("after set currentFiscNo, input fiscNo = [", fiscNo, "], currentFiscNo = [", currentFiscNo.get(), "]");
//        logData.setProgramName(StringUtils.join(FISCAdapter.class.getSimpleName(), ".initialFiscNo"));
//        logData.setProgramFlowType(ProgramFlow.AdapterIn);
//        logData.setMessageFlowType(MessageFlow.Request);
//        logData.setRemark(StringUtils.join("after set currentFiscNo, input fiscNo = [", fiscNo, "], currentFiscNo = [", currentFiscNo.get(), "]"));
//        logMessage(Level.DEBUG, logData);
    }

    /**
     * 取得目前成功傳送之最後一筆財金電文序號
     *
     * @return
     */
    public static String getCurrentFiscNo() {
        return StringUtils.leftPad(String.valueOf(currentFiscNo.get()), 7, '0');
    }

    public static String getRMNNoByBank(String bankNo) {
        String rmNo = currentRmNo.get(bankNo);
        return rmNo == null ? StringUtils.EMPTY : rmNo;
    }

    public ToFISCCommu getToFISCCommu() {
        return this.toFISCCommu;
    }

    @ConfigurationProperties(prefix = "spring.fep.fisc-adapter")
    // @RefreshScope
    public static class FISCAdapterConfiguration {
        @NestedConfigurationProperty
        private final List<FiscNode> nodes = new ArrayList<>();
        /**
         * 連接失敗時, delay秒數後重連另一組
         */
        private int delay = 3;
        /**
         * 當currentGroup設為secondary時, 啟動一個Timer每隔N杪偵測primary是否恢復正常
         */
        private int checkPrimaryInterval = 60;
        /**
         * 當發生斷線需要retry時, 鎖住後面線程的時間
         */
        private int lockTime;
        /**
         * Alternative Queue receive timeout
         */
        private long alternativeQueueReceiveTimeout = 5000L;
        /**
         * 預設socket
         */
        private Protocol protocol = Protocol.socket;
        /**
         * Just for Use VFISC Testing
         */
        private String svcUrl = "http://10.3.101.4:8913/api/VFISC/SendReceive";
        /**
         * 執行緒線程池固定大小
         */
        private int executorCorePoolSize = 20;
        /**
         * 執行緒線程池中線程alive時間, 單位毫秒
         */
        private long executorKeepAliveTime = 0;
        /**
         * 執行緒線程池Queue Size
         */
        private int executorQueueCapacity = Integer.MAX_VALUE;

        public List<FiscNode> getNodes() {
            return nodes;
        }

        public int getDelay() {
            return this.delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public int getCheckPrimaryInterval() {
            return this.checkPrimaryInterval;
        }

        public void setCheckPrimaryInterval(int checkPrimaryInterval) {
            this.checkPrimaryInterval = checkPrimaryInterval;
        }

        public int getLockTime() {
            return this.lockTime;
        }

        public void setLockTime(int lockTime) {
            this.lockTime = lockTime;
        }

        public long getAlternativeQueueReceiveTimeout() {
            return alternativeQueueReceiveTimeout;
        }

        public void setAlternativeQueueReceiveTimeout(long alternativeQueueReceiveTimeout) {
            this.alternativeQueueReceiveTimeout = alternativeQueueReceiveTimeout;
        }

        public Protocol getProtocol() {
            return protocol;
        }

        public void setProtocol(Protocol protocol) {
            this.protocol = protocol;
        }

        public String getSvcUrl() {
            return svcUrl;
        }

        public void setSvcUrl(String svcUrl) {
            this.svcUrl = svcUrl;
        }

        public int getExecutorCorePoolSize() {
            return executorCorePoolSize;
        }

        public void setExecutorCorePoolSize(int executorCorePoolSize) {
            this.executorCorePoolSize = executorCorePoolSize;
        }

        public long getExecutorKeepAliveTime() {
            return executorKeepAliveTime;
        }

        public void setExecutorKeepAliveTime(long executorKeepAliveTime) {
            this.executorKeepAliveTime = executorKeepAliveTime;
        }

        public int getExecutorQueueCapacity() {
            return executorQueueCapacity;
        }

        public void setExecutorQueueCapacity(int executorQueueCapacity) {
            this.executorQueueCapacity = executorQueueCapacity;
        }

        @PostConstruct
        public void postConstruct() {
            executor = ThreadPoolFactory.newFixedThreadPool(
                    this.executorCorePoolSize,
                    this.executorKeepAliveTime,
                    TimeUnit.MILLISECONDS,
                    this.executorQueueCapacity,
                    new SimpleThreadFactory("FISCAdapterExecutor"),
                    new ThreadPoolExecutor.CallerRunsPolicy());
            timer = new FISCAdapterNodeHealthCheckTimer(this.nodes);
            timer.scheduleAtFixedRate(0, this.checkPrimaryInterval, TimeUnit.SECONDS);
            LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "FISCAdapter Configuration"));
        }

        @PreDestroy
        public void destroy() {
            ThreadPoolFactory.shutdown(executor, "FISCAdapterExecutor");
            timer.destroy();
        }
    }

    /**
     * 用來check
     *
     * @author Richard
     */
    private static class FISCAdapterNodeHealthCheckTimer extends AbstractScheduledTask {
        private final List<FiscNode> nodes;
        private final LogData logData = new LogData();

        public FISCAdapterNodeHealthCheckTimer(List<FiscNode> nodes) {
            super("FISCAdapterNodeHealthCheckTimer");
            this.nodes = nodes;
        }

        /**
         * Execute Task
         */
        @Override
        public void execute() {
            try {
                if (CollectionUtils.isNotEmpty(this.nodes)) {
                    logData.clear();
                    for (FiscNode node : this.nodes) {
                        NodeStatus status = node.getStatus();
                        switch (status) {
                            case HEALTHY:
                                // 不用檢查
                                break;
                            case DEGRADED:
                                // 偵測它的 primaryHost IP是否恢復,若恢復了就把狀態改為 HEALTHY
                                detect(node, node.getPrimaryHost(), NodeStatus.HEALTHY);
                                break;
                            case DOWN:
                                // 先偵測primaryHost的IP，若primaryHost有通, 則status改為HEALTHY
                                boolean result = detect(node, node.getPrimaryHost(), NodeStatus.HEALTHY);
                                // 若不通則再偵測secondaryHost IP, 若有通, 則status改為DEGRADED
                                if (!result) {
                                    detect(node, node.getSecondaryHost(), NodeStatus.DEGRADED);
                                }
                                break;
                            default:
                        }
                    }
                }
            } catch (Throwable e) {
                this.logData.setProgramName(StringUtils.join(this.taskName, ".detect"));
                this.logData.setRemark("check exception occur");
                this.logData.setProgramException(e);
                logMessage(Level.WARN, this.logData);
            }
        }

        private boolean detect(FiscNode node, String host, NodeStatus status) {
            this.logData.setProgramName(StringUtils.join(this.taskName, ".detect"));
            this.logData.setRemark(StringUtils.join("[DetectConnectable]Try to detect FISCGateway [", host, ":", node.getPort(), "] connective, clientID:", node.getClientId(), ", status:", node.getStatus()));
            logMessage(Level.WARN, this.logData);
            if (SocketUtil.isTcpAvailable(host, node.getPort())) {
                node.setStatus(status);
                this.logData.setProgramName(StringUtils.join(this.taskName, ".detect"));
                this.logData.setRemark(StringUtils.join("[Connectable]FISCGateway [", host, ":", node.getPort(), "] is Connectable, clientID:", node.getClientId(), ", status:", node.getStatus()));
                logMessage(Level.INFO, this.logData);
                return true;
            } else {
                this.logData.setProgramName(StringUtils.join(this.taskName, ".detect"));
                this.logData.setRemark(StringUtils.join("[NotConnectable]FISCGateway [", host, ":", node.getPort(), "] is Not Connectable, clientID:", node.getClientId()));
                logMessage(Level.WARN, this.logData);
            }
            return false;
        }
    }

    private static class FiscNode {
        private String clientId;
        private String primaryHost;
        private String secondaryHost;
        private int port;
        // 預先先送primaryHost
        private final AtomicReference<NodeStatus> status = new AtomicReference<>(NodeStatus.HEALTHY);

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getPrimaryHost() {
            return primaryHost;
        }

        public void setPrimaryHost(String primaryHost) {
            this.primaryHost = primaryHost;
        }

        public String getSecondaryHost() {
            return secondaryHost;
        }

        public void setSecondaryHost(String secondaryHost) {
            this.secondaryHost = secondaryHost;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public NodeStatus getStatus() {
            return status.get();
        }

        public void setStatus(NodeStatus status) {
            this.status.set(status);
        }

        public String getTargetHost() {
            NodeStatus stats = getStatus();
            switch (stats) {
                case HEALTHY:
                    return getPrimaryHost();
                case DEGRADED:
                    return getSecondaryHost();
                case DOWN:
                default:
                    throw ExceptionUtil.createIllegalArgumentException("節點目前不可用, 無法取得 Host");
            }
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("clientId", clientId)
                    .append("primaryHost", primaryHost)
                    .append("secondaryHost", secondaryHost)
                    .append("port", port)
                    .append("status", status.get())
                    .toString();
        }
    }

    private enum NodeStatus {
        // 正常状態:PrimaryHost 連線正常, 交易優先送往 PrimaryHost
        HEALTHY,
        // 降級状葱:PrimaryHost失敗, 交易改送往 secondaryHost
        DEGRADED,
        // 不可用状態:Primary與secondary 兩者皆連不上
        DOWN;
    }
}