package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.CBSType;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.exception.FEPBaseException;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import com.syscom.fep.frmcommon.scheduler.AbstractScheduledTask;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.SocketUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.invoker.FEPInvoker;
import com.syscom.fep.invoker.SimpleNettyClientFactory;
import com.syscom.fep.vo.communication.ToCBSCommu;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.event.Level;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 負責傳送主機電文至CBSGW並接收回應
 *
 * @author Ashiang & Richard
 */
public class CBSAdapter extends AdapterBase {
    private final FEPInvoker invoker = SpringBeanFactoryUtil.getBean(FEPInvoker.class);
    /**
     * 交易電文物件
     */
    private final MessageBase txData;
    /**
     * CBSGW回應訊息
     */
    private String messageFromCBS;
    /**
     * 送給CBSGW的訊息
     */
    private String messageToCBS;
    /**
     * 交易的TranCode
     */
    private String tranCode;
    /**
     * CBS電文種類
     */
    private CBSType cbsType;
    /**
     * CBS電文CorrelationId
     */
    private String cbsId;
    /**
     * CBSAdapter配置類
     */
    private static final CBSAdapterConfiguration configuration;
    /**
     * 依據CBSType對應存儲CBSGW的連線元件
     */
    private static final Map<CBSType, CBSGatewayHostGroup> cbsGatewayHostGroupMap = new HashMap<>();

    static {
        SimpleNettyClientFactory.registerToCBSComponents(); // 2026-03-17 Richard add 這裡提前註冊ToCBS元件
        configuration = SpringBeanFactoryUtil.registerBean(CBSAdapterConfiguration.class);
    }

    public CBSAdapter(MessageBase txData) {
        this.txData = txData;
        this.timeout = CMNConfig.getInstance().getCBSTimeout();
        this.buildCbsGatewayHostGroupMap();
    }


    @Override
    public FEPReturnCode sendReceive() {
        this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
        this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
        this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
        this.txData.getLogContext().setMessage(messageToCBS);
        this.txData.getLogContext().setRemark(StringUtils.join("Fetch CBSGatewayHostGroup Object, cbsType:", this.cbsType));
        logMessage(this.txData.getLogContext());
        // 根據前端傳入的cbsType, 從對應的currentGroup_XXX變數中, 取出一組CBSGW的IP及PORT,
        // 然後將messageToCBS傳送至CBSGW並開始等待回應至timeout為止, 收到電文時, 設定到messageFromCBS變數中
        CBSGatewayHostGroup hostGroup = cbsGatewayHostGroupMap.get(this.cbsType);
        if (hostGroup != null) {
            String hostGroupInfo = hostGroup.toString();
            LogHelperFactory.getTraceLogger().debug(this.cbsType, " ======> ", hostGroupInfo); // 列印取出對應的HostGroup信息方便check問題
            this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
            this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
            this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            this.txData.getLogContext().setMessage(messageToCBS);
            this.txData.getLogContext().setRemark(StringUtils.join("Fetch CBSGatewayHostGroup Object succeed, cbsType:", this.cbsType, ", hostGroup:", hostGroupInfo));
            logMessage(this.txData.getLogContext());
            RefString refMessageFromCBS = new RefString(StringUtils.EMPTY);
            // 因為CBSGatewayHostGroup是存儲在static Map中, 所以每次取出的CBSGatewayHostGroup都是同一個物件
            // 所以CBSGatewayHostGroup內所需要的全域變數, 必須要在sendReceive()方法中, 重新塞入, 不可以直接使用
            FEPReturnCode rtnCode = hostGroup.sendReceive(
                    this.txData.getLogContext(),
                    this.cbsId,
                    this.txData.getChannel() != null ? this.txData.getChannel().getChannelName() : null,
                    this.txData.getEj(),
                    this.timeout,
                    this.messageToCBS,
                    refMessageFromCBS);
            this.messageFromCBS = refMessageFromCBS.get();
            return rtnCode;
        } else {
            this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
            this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
            this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            this.txData.getLogContext().setMessage(messageToCBS);
            this.txData.getLogContext().setRemark(StringUtils.join("Cannot Fetch CBSGatewayHostGroup Object, cbsType:", this.cbsType));
            logMessage(Level.WARN, this.txData.getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 取得CBS回應訊息
     *
     * @return
     */
    public String getMessageFromCBS() {
        return messageFromCBS;
    }

    /**
     * 設定CBS回應訊息
     *
     * @param messageFromCBS
     */
    public void setMessageFromCBS(String messageFromCBS) {
        this.messageFromCBS = messageFromCBS;
    }

    /**
     * 取得送給CBS的訊息
     *
     * @return
     */
    public String getMessageToCBS() {
        return messageToCBS;
    }

    /**
     * 設定送給CBS的訊息
     *
     * @param messageToCBS
     */
    public void setMessageToCBS(String messageToCBS) {
        this.messageToCBS = messageToCBS;
    }

    /**
     * 取得交易的TranCode
     *
     * @return
     */
    public String getTranCode() {
        return tranCode;
    }

    /**
     * 設定交易的TranCode
     *
     * @param tranCode
     */
    public void setTranCode(String tranCode) {
        this.tranCode = tranCode;
    }

    /**
     * 取得CBS電文種類
     *
     * @return
     */
    public CBSType getCbsType() {
        return cbsType;
    }

    /**
     * 設定CBS電文種類
     *
     * @param cbsType
     */
    public void setCbsType(CBSType cbsType) {
        this.cbsType = cbsType;
    }

    /**
     * 取得CBS電文CorrelationId
     *
     * @return
     */
    public String getCbsId() {
        return cbsId;
    }

    /**
     * 設定CBS電文CorrelationId
     *
     * @param cbsId
     */
    public void setCbsId(String cbsId) {
        this.cbsId = cbsId;
    }

    /**
     * 根據配置檔中的CBSType存入對應的CBSGatewayHostGroup物件
     */
    private synchronized void buildCbsGatewayHostGroupMap() {
        synchronized (cbsGatewayHostGroupMap) {
            if (MapUtils.isNotEmpty(cbsGatewayHostGroupMap))
                return;
            for (CBSGatewayHost host : configuration.getPrimary()) {
                CBSType type = CBSType.fromCode(host.getCbsType());
                CBSGatewayHostGroup hostGroup = cbsGatewayHostGroupMap.get(type);
                if (hostGroup == null) {
                    hostGroup = new CBSGatewayHostGroup(type);
                    cbsGatewayHostGroupMap.put(type, hostGroup);
                }
                hostGroup.addPrimary(host);
            }
            for (CBSGatewayHost host : configuration.getSecondary()) {
                CBSType type = CBSType.fromCode(host.getCbsType());
                CBSGatewayHostGroup hostGroup = cbsGatewayHostGroupMap.get(type);
                if (hostGroup == null) {
                    hostGroup = new CBSGatewayHostGroup(type);
                    cbsGatewayHostGroupMap.put(type, hostGroup);
                }
                hostGroup.addSecondary(host);
            }
            // 列印吃配置檔後的建立map的內容
            StringBuilder sb = new StringBuilder();
            Set<Map.Entry<CBSType, CBSGatewayHostGroup>> set = cbsGatewayHostGroupMap.entrySet();
            for (Map.Entry<CBSType, CBSGatewayHostGroup> entry : set) {
                sb.append("\t").append(entry.getKey()).append(" ======> ").append(entry.getValue()).append(System.lineSeparator());
            }
            LogHelperFactory.getTraceLogger().info("CBSAdapter after buildCbsGatewayHostGroupMap, ", System.lineSeparator(), sb.toString());
        }
    }

    @ConfigurationProperties(prefix = "spring.fep.cbs-adapter")
    // @RefreshScope
    public static class CBSAdapterConfiguration {
        @NestedConfigurationProperty
        private List<CBSGatewayHost> primary = new ArrayList<>();
        @NestedConfigurationProperty
        private List<CBSGatewayHost> secondary = new ArrayList<>();
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

        public List<CBSGatewayHost> getPrimary() {
            return this.primary;
        }

        public List<CBSGatewayHost> getSecondary() {
            return this.secondary;
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

        @PostConstruct
        public void postConstruct() {
            AtomicInteger index = new AtomicInteger(0);
            this.primary.forEach(t -> t.setIndex(index.getAndIncrement()));
            index.set(0);
            this.secondary.forEach(t -> t.setIndex(index.getAndIncrement()));
            LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "CBSAdapter Configuration", "type"));
        }

        @PreDestroy
        public void destroy() {
            // 停止內部的一些線程, 避免stop程式的時候, 會halt
            for (CBSGatewayHostGroup group : cbsGatewayHostGroupMap.values()) {
                group.destroy();
            }
        }
    }

    private class CBSGatewayHostGroup {
        /**
         * CBS Type
         */
        private final CBSType cbsType;
        /**
         * Primary輪播
         */
        private final RoundRobin<CBSGatewayHost> primary = new RoundRobin<>();
        /**
         * Secondary輪播
         */
        private final RoundRobin<CBSGatewayHost> secondary = new RoundRobin<>();
        /**
         * 當前Group, 預設是Primary
         */
        private final AtomicReference<CBSGatewayGroupType> currentGroupType = new AtomicReference<>(CBSGatewayGroupType.PRIMARY);
        /**
         * 啟動一個Timer, 每隔checkPrimaryInterval秒數, 偵測primary[0]是否可以連接成功
         */
        private CBSAdapterCheckPrimaryTimer timer;
        /**
         * 當發生斷線retry時, 後面進來的線程要wait
         */
        private final RefBoolean lock = new RefBoolean(Boolean.FALSE);

        public CBSGatewayHostGroup(CBSType cbsType) {
            this.cbsType = cbsType;
        }

        public CBSType getCbsType() {
            return this.cbsType;
        }

        public void setCurrentType(CBSGatewayGroupType type) {
            this.currentGroupType.set(type);
        }

        public CBSGatewayGroupType getCurrentGroupType() {
            return this.currentGroupType.get();
        }

        public void addPrimary(CBSGatewayHost host) {
            host.setType(CBSGatewayGroupType.PRIMARY);
            this.primary.add(host);
        }

        public void addSecondary(CBSGatewayHost host) {
            host.setType(CBSGatewayGroupType.SECONDARY);
            this.secondary.add(host);
        }

        /**
         * 發送信息給CBSGW並接收回應
         *
         * @param logData
         * @param cbsId
         * @param channel
         * @param ej
         * @param timeout
         * @param messageToCBS
         * @param messageFromCBS
         * @return
         */
        public FEPReturnCode sendReceive(LogData logData, String cbsId, String channel, int ej, int timeout, String messageToCBS, RefString messageFromCBS) {
            String logging = StringUtils.join(",cbsId:", cbsId, ",channel:", channel, ",ej:", ej, ",timeout:", timeout);
            // 如果之前發送失敗需要切換不同的CBSGatewayHost, 則這裡需要wait一下
            synchronized (this.lock) {
                if (this.lock.get()) {
                    long lockTime = (configuration.getLockTime() <= 0 ? timeout : configuration.getLockTime()) * 1000L;
                    LogHelperFactory.getTraceLogger().warn("******************[", ProgramName, "]Start to wait to unlock in [", lockTime, "] milliseconds..., CBSType:", this.cbsType.getCode(), logging);
                    long currentTimeMillis = System.currentTimeMillis();
                    try {
                        this.lock.wait(lockTime);
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                    LogHelperFactory.getTraceLogger().warn("******************[", ProgramName, "]After waiting unlock in [", System.currentTimeMillis() - currentTimeMillis, "] milliseconds, start to sendReceive, CBSType:", this.cbsType.getCode(), logging);
                }
            }
            // 接收到電文時, 將目前時間放入recvTime變數,供後面retry時判斷是否需繼續retry
            long recvTime = System.currentTimeMillis();
            FEPReturnCode rtnCode = null;
            boolean startToRetry = false, isRetrying = false;
            int times = 0;
            while (true) {
                times++;
                rtnCode = FEPReturnCode.Normal;
                // 取出下一個CBSGW host
                CBSGatewayHost host = this.fetchNext(logData, isRetrying, logging);
                // logging
                logData.setProgramFlowType(ProgramFlow.AdapterIn);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramName(StringUtils.join(ProgramName, ".CBSGatewayHostGroup.sendReceive"));
                logData.setMessage(messageToCBS);
                logData.setRemark(StringUtils.join("Ready Send data to CBS at [", times, "] times, index:", host.getIndex(), ",host:", host.getHost(), ",port:", host.getPort(), ",groupType:", host.getType().name(), ",CBSType:", this.cbsType.getCode(), logging));
                logMessage(logData);
                // prepare ToCBSCommu
                ToCBSCommu toCBSCommu = new ToCBSCommu();
                toCBSCommu.setCbsId(cbsId);
                toCBSCommu.setChannel(channel);
                toCBSCommu.setHost(host.getHost());
                toCBSCommu.setPort(host.getPort());
                toCBSCommu.setEj(ej);
                toCBSCommu.setTimeout(timeout);
                toCBSCommu.setMessage(messageToCBS);
                try {
                    // send to CBSGW and receive response
                    messageFromCBS.set(invoker.sendReceiveToCBSGW(toCBSCommu, timeout * 1000));
                    // logging
                    logData.setProgramFlowType(ProgramFlow.AdapterOut);
                    logData.setMessageFlowType(MessageFlow.Response);
                    logData.setProgramName(StringUtils.join(ProgramName, ".CBSGatewayHostGroup.sendReceive"));
                    logData.setMessage(messageFromCBS.get());
                    logData.setRemark(StringUtils.join("Get data fromCBS at [", times, "] times, index:", host.getIndex(), ",host:", host.getHost(), ",port:", host.getPort(), ",groupType:", host.getType().name(), ",CBSType:", this.cbsType.getCode(), logging));
                    logMessage(logData);
                } catch (Exception e) {
                    // 走socket無法連線FISCGateway
                    if (e instanceof FEPBaseException) {
                        FEPBaseException fepBaseException = (FEPBaseException) e;
                        if (fepBaseException.getRtnCode() != null) {
                            rtnCode = fepBaseException.getRtnCode();
                        }
                    } else if (e instanceof SocketTimeoutException) { // 2024-08-09 Richard add 判斷是SocketTimeoutException時, rtnCode回傳HostResponseTimeout
                        rtnCode = FEPReturnCode.HostResponseTimeout;
                    } else {
                        rtnCode = CommonReturnCode.ProgramException;
                    }
                    // 發生例外時需sendEMS
                    logData.setProgramName(StringUtils.join(ProgramName, ".CBSGatewayHostGroup.sendReceive"));
                    logData.setProgramException(e);
                    logData.setMessage(messageToCBS);
                    logData.setReturnCode(rtnCode);
                    logData.setRemark(StringUtils.join("Send data to CBS failed at [", times, "] times, index:", host.getIndex(), ",host:", host.getHost(), ",port:", host.getPort(), ",groupType:", host.getType().name(), ",CBSType:", this.cbsType.getCode(), logging));
                    sendEMS(logData);
                }
                // 若連接失敗時, 則以下列順序在Timeout時間內Retry, 直到成功或(Now-recvTime)的秒數>(Timeout- Delay)秒為止
                if (rtnCode == FEPReturnCode.CanNotConnectRemoteHost) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".CBSGatewayHostGroup.sendReceive"));
                    logData.setRemark(StringUtils.join("Cannot connect to CBSGW, try to rebuild Connection to CBSGW, CBSType:", this.cbsType.getCode(), logging));
                    logMessage(Level.WARN, logData);
                    // 連線失敗則lock住
                    synchronized (lock) {
                        lock.set(Boolean.TRUE);
                    }
                    // 開始嘗試重新建立連線
                    if (!startToRetry) {
                        startToRetry = true;
                        // logging
                        logData.setProgramName(StringUtils.join(ProgramName, ".CBSGatewayHostGroup.sendReceive"));
                        logData.setRemark(StringUtils.join("Start to try Build Connection to CBSGW, CBSType:", this.cbsType.getCode(), logging));
                        logMessage(Level.WARN, logData);
                        // 這裡一定要先reset, 從第一個開始
                        primary.reset();
                        secondary.reset();
                    }
                    // 開始進入重新建立連線中
                    isRetrying = true;
                    if (System.currentTimeMillis() - recvTime > (timeout - configuration.getDelay()) * 1000L) {
                        logData.setProgramName(StringUtils.join(ProgramName, ".CBSGatewayHostGroup.sendReceive"));
                        logData.setMessage(messageToCBS);
                        logData.setReturnCode(rtnCode);
                        logData.setRemark(StringUtils.join("Still cannot Send data to CBS, index:", host.getIndex(), ",host:", host.getHost(), ",port:", host.getPort(), ",groupType:", host.getType().name(), ",CBSType:", this.cbsType.getCode(), logging));
                        sendEMS(logData);
                        break;
                    }
                    // Delay 3秒後
                    try {
                        Thread.sleep(configuration.getDelay() * 1000L);
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                } else {
                    if (rtnCode == FEPReturnCode.Normal) {
                        // 當currentGroup_XXX被設定為secondary時, 啟動一個Timer, 每隔checkPrimaryInterval秒數,
                        // 偵測primary[0]是否可以連接成功, 若可以, 則將currentGroup_XXX=primary, 並停止偵測的Timer
                        if (this.currentGroupType.get() == CBSGatewayGroupType.SECONDARY && (this.timer == null || this.timer.isCancelled())) {
                            // logging
                            logData.setProgramName(StringUtils.join(ProgramName, ".CBSGatewayHostGroup.sendReceive"));
                            logData.setRemark(StringUtils.join("Start an Timer to check CBSGW Connection, interval:", configuration.getCheckPrimaryInterval(), ",CBSType:", this.cbsType.getCode(), logging));
                            logMessage(Level.WARN, logData);
                            if (this.timer == null) {
                                this.timer = new CBSAdapterCheckPrimaryTimer(this);
                            }
                            this.timer.setLogData(logData);
                            this.timer.scheduleAtFixedRate(0, configuration.getCheckPrimaryInterval(), TimeUnit.SECONDS);
                        }
                    }
                    logData.setProgramName(StringUtils.join(ProgramName, ".CBSGatewayHostGroup.sendReceive"));
                    logData.setRemark(StringUtils.join("Send Data to CBSGW finished and break while at [", times, "] times, CBSType:", this.cbsType.getCode(), logging));
                    logMessage(logData);
                    break;
                }
            }
            // 一旦連線及傳送成功後再解除lock
            synchronized (this.lock) {
                this.lock.set(Boolean.FALSE);
                this.lock.notifyAll();
            }
            logData.setProgramName(StringUtils.join(ProgramName, ".CBSGatewayHostGroup.sendReceive"));
            logData.setRemark(StringUtils.join("Send Data to CBSGW finished with [", times, "] total times, CBSType:", this.cbsType.getCode(), logging));
            logMessage(logData);
            return rtnCode;
        }

        /**
         * 取下一個CBSGW Host
         * <p>
         * 同一種cbsType, 若currentGroup_XXX內有多組連線參數, 每一筆交易輪流傳送同一組currentGroup_XXX的不同PORT;
         * 若連接失敗時, 則以下列順序在Timeout時間內Retry, 直到成功或(Now-recvTime)的秒數>(Timeout- Delay)秒為止
         * 以下以currentGroup_XXX=primary為例
         * Primary[0]失敗=>等待delay秒數=>連Primary[1] 失敗=>等待delay秒數=>連Secondary[0]失敗=>等待delay秒數=>連Secondary[1] 失敗 =>等待delay秒數=> Primary[0]….
         *
         * @param logData
         * @param isRetrying 重試進行中
         * @param logging
         * @return
         */
        private CBSGatewayHost fetchNext(LogData logData, boolean isRetrying, String logging) {
            CBSGatewayHost fetch = null;
            // 正常情況下, currentGroupType是哪個Group則依次從哪個Group取
            if (!isRetrying) {
                if (currentGroupType.get() == CBSGatewayGroupType.PRIMARY) {
                    fetch = primary.select();
                } else {
                    fetch = secondary.select();
                }
            }
            // 進入輪詢重新建立連線
            // 在第一次進入之前, Primary和Secondary一定要先reset
            else {
                // 如果當前是Primary, 則先取Primary再取Secondary
                if (currentGroupType.get() == CBSGatewayGroupType.PRIMARY) {
                    // Primary沒有輪詢完, 則從Primary取, 否則從Secondary取
                    if (!primary.isRoundRobinAll()) {
                        fetch = primary.select();
                    } else {
                        primary.reset(); // 這裡記得要reset, 以便Secondary輪詢完了之後, 繼續從Primary中取
                        fetch = secondary.select();
                    }
                } else {
                    if (!secondary.isRoundRobinAll()) {
                        fetch = secondary.select();
                    } else {
                        secondary.reset(); // 這裡記得要reset, 以便Primary輪詢完了之後, 繼續從Secondary中取
                        fetch = primary.select();
                    }
                }
                // 取出來之後, 更新當前的GroupType
                currentGroupType.set(fetch.getType());
            }
            logData.setProgramName(StringUtils.join(ProgramName, ".CBSGatewayHostGroup.fetchNext"));
            logData.setRemark(StringUtils.join("fetch next CBS Gateway, index:", fetch.getIndex(), ",host:", fetch.getHost(), ",port:", fetch.getPort(), ",groupType:", fetch.getType().name(), ",CBSType:", this.cbsType.name(), logging));
            logMessage(logData);
            return fetch;
        }

        public CBSGatewayHost getPrimaryFirst() {
            return primary.get(0);
        }

        public void destroy() {
            synchronized (this.lock) {
                this.lock.notifyAll();
            }
            this.timer.destroy();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("cbsType", cbsType)
                    .append("primary", primary.getList())
                    .append("secondary", secondary.getList())
                    .toString();
        }
    }

    /**
     * 用來check
     *
     * @author Richard
     */
    private class CBSAdapterCheckPrimaryTimer extends AbstractScheduledTask {
        private final CBSGatewayHostGroup hostGroup;
        private final CBSGatewayHost checkHost;
        private int times = 0;
        private LogData logData;
        private String mdcProfile;

        public CBSAdapterCheckPrimaryTimer(CBSGatewayHostGroup hostGroup) {
            super(StringUtils.join("CBSAdapterCheckPrimaryTimer-", hostGroup.getCbsType().getCode()));
            this.hostGroup = hostGroup;
            this.checkHost = hostGroup.getPrimaryFirst();
        }

        public void setLogData(LogData logData) {
            this.logData = logData;
            this.mdcProfile = LogMDC.get(Const.MDC_PROFILE);
        }

        /**
         * Execute Task
         */
        @Override
        public void execute() {
            LogMDC.put(Const.MDC_PROFILE, mdcProfile);
            times++;
            // 偵測primary[0]是否可以連接成功, 若可以, 則將CurrentType=primary, 並停止偵測的Timer
            this.logData.setProgramName(StringUtils.join(this.taskName, ".execute"));
            this.logData.setRemark(StringUtils.join("Try to detect CBS [index:", this.checkHost.getIndex(), ",host:", this.checkHost.getHost(), ",port:", this.checkHost.getPort(), ",groupType:", this.checkHost.getType().name(), "] connective at [", times, "] times, CBSType:", this.hostGroup.getCbsType().getCode()));
            logMessage(Level.WARN, this.logData);
            if (SocketUtil.isTcpAvailable(this.checkHost.getHost(), this.checkHost.getPort())) {
                // 將CurrentType=primary
                this.hostGroup.setCurrentType(this.checkHost.getType());
                this.logData.setProgramName(StringUtils.join(this.taskName, ".execute"));
                this.logData.setRemark(StringUtils.join("CBS [index:", this.checkHost.getIndex(), ",host:", this.checkHost.getHost(), ",port:", this.checkHost.getPort(), ",groupType:", this.checkHost.getType().name(), "] is Connectable, and set GroupType = [", this.hostGroup.getCurrentGroupType().name(), "],  [", times, "] detected times, CBSType:", this.hostGroup.getCbsType().getCode()));
                logMessage(this.logData);
                times = 0;
                // 停止偵測的Timer
                this.cancel();
            }
        }
    }

    /**
     * CBSGW主機設定
     *
     * @author Richard
     */
    public static class CBSGatewayHost {
        private int index;
        private String host;
        private int port;
        private String cbsType;
        private CBSGatewayGroupType type;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
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

        public String getCbsType() {
            return cbsType;
        }

        public void setCbsType(String cbsType) {
            this.cbsType = cbsType;
        }

        public CBSGatewayGroupType getType() {
            return type;
        }

        public void setType(CBSGatewayGroupType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("index", index)
                    .append("host", host)
                    .append("port", port)
                    .append("cbsType", cbsType)
                    .append("type", type)
                    .toString();
        }
    }

    private enum CBSGatewayGroupType {
        PRIMARY, SECONDARY;
    }
}
