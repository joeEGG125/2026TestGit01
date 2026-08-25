package com.syscom.fep.gateway.ims.cbs;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.ims.*;
import com.syscom.fep.gateway.ims.cbs.receiver.CBSGatewayClientReceiver;
import com.syscom.fep.gateway.ims.cbs.receiver.CBSGatewayClientReceiverConfiguration;
import com.syscom.fep.gateway.ims.cbs.receiver.CBSGatewayClientReceiverProcessRequest;
import com.syscom.fep.gateway.ims.cbs.sender.CBSGatewayClientSender;
import com.syscom.fep.gateway.ims.cbs.sender.CBSGatewayClientSenderConfiguration;
import com.syscom.fep.gateway.ims.cbs.sender.CBSGatewayClientSenderProcessRequest;
import com.syscom.fep.gateway.netty.cbs.server.*;
import com.syscom.fep.vo.communication.ToCBSChangeLineStatusAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CBSGatewayGroup extends FEPBase implements IMSTransmissionConnStateListener<CBSGatewayClientConfiguration> {
    private final LogHelper logger = LogHelperFactory.getTraceLogger();
    private final IMSTransmissionNotification notification = SpringBeanFactoryUtil.getBean(IMSTransmissionNotification.class);
    private final CBSGatewayServerConfiguration serverConfiguration;
    private final List<CBSGatewayClientSenderConfiguration> senderConfigurations;
    private final List<CBSGatewayClientReceiverConfiguration> receiverConfigurations;
    private CBSGatewayServer server;
    private List<CBSGatewayClientSender> senders;
    private List<CBSGatewayClientReceiver> receivers;
    private RoundRobin<CBSGatewayClientSenderProcessRequest> senderProcessRequests;
    private final CBSGatewayManager cbsGatewayManager;
    private List<AtomicReference<CBSGatewayStatistic>> statistics;
    private final Map<String, ScheduledFuture<?>> scheduleToRunSenderScheduledFutureMap = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> autoSwitchPauseToDisableScheduledFutureMap = new ConcurrentHashMap<>();
    private final ServerHandler serverHandler;
    // private final Map<String, List<IMSTransmission<?, ?>>> clientIdToIMSTransmissionMap = new ConcurrentHashMap<>();

    public CBSGatewayGroup(CBSGatewayManager cbsGatewayManager, CBSGatewayServerConfiguration serverConfiguration, List<CBSGatewayClientSenderConfiguration> senderConfigurations, List<CBSGatewayClientReceiverConfiguration> receiverConfigurations) {
        this.cbsGatewayManager = cbsGatewayManager;
        this.serverConfiguration = serverConfiguration;
        this.senderConfigurations = this.overwriteProperty(senderConfigurations);
        this.receiverConfigurations = this.overwriteProperty(receiverConfigurations);
        this.serverHandler = new ServerHandler();
    }

    private <T extends IMSTransmissionConfiguration> List<T> overwriteProperty(List<T> configuration) {
        if (this.cbsGatewayManager.isHandleSenderAndReceiverBoth()) {
            configuration.forEach(t -> t.setAutoReestablishConnection(false)); // 設置不要自動重連, 後面會手動重連
        }
        return configuration;
    }

    private void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.CBSGW.name());
    }

    /**
     * 運行
     */
    public void run(boolean postConstruct) {
        this.logContext.clear();
        // 初始化List
        this.senders = new ArrayList<>(this.senderConfigurations.size());
        this.receivers = new ArrayList<>(this.receiverConfigurations.size());
        this.senderProcessRequests = new RoundRobin<>();
        this.statistics = new ArrayList<>(this.senderConfigurations.size());
        // 加入null先保留位置
        this.senderConfigurations.forEach(cfg -> {
            this.senders.add(null);
            this.senderProcessRequests.add(null);
            this.statistics.add(null);
        });
        this.receiverConfigurations.forEach(cfg -> this.receivers.add(null));
        // 啟動sender
        this.senderConfigurations.forEach(cfg -> this.runSender(this.logContext, cfg, postConstruct));
        // 如果不需要依次啟動, 則直接啟動receiver
        if (!this.cbsGatewayManager.isHandleSenderAndReceiverBoth()) {
            this.receiverConfigurations.forEach(cfg -> this.runReceiver(this.logContext, cfg, postConstruct));
        }
    }

    /**
     * 停止, @PreDestroy時呼叫
     */
    public void terminate() {
        this.logContext.clear();
        // 以下先移除掉監聽, 以確保後續每一步都有走到
        this.senderConfigurations.forEach(cfg -> {
            // 移除掉監聽
            notification.removeConnStateListener(cfg, this);
        });
        this.receiverConfigurations.forEach(cfg -> {
            // 移除掉監聽
            notification.removeConnStateListener(cfg, this);
        });
        // 停止server
        this.terminateServer(this.logContext);
        // 停止receiver
        if (this.receivers != null) {
            this.receivers.forEach(t -> {
                if (t != null)
                    this.terminateReceiver(this.logContext, t.getConfiguration());
            });
        }
        // 停止sender
        if (this.senders != null) {
            this.senders.forEach(t -> {
                if (t != null)
                    this.terminateSender(this.logContext, t.getConfiguration());
            });
            this.senders.clear();
            this.senders = null;
        }
        // clear senderProcessRequest
        if (this.senderProcessRequests != null) {
            this.senderProcessRequests.clear();
            this.senderProcessRequests = null;
        }
        // clear statistics
        if (this.statistics != null) {
            this.statistics.clear();
            this.statistics = null;
        }
        // clear scheduleToRunSenderScheduledFutureMap
        if (!this.scheduleToRunSenderScheduledFutureMap.isEmpty()) {
            this.scheduleToRunSenderScheduledFutureMap.forEach((clientId, futre) -> {
                futre.cancel(true);
                futre = null;
            });
            this.scheduleToRunSenderScheduledFutureMap.clear();
        }
        // clear autoSwitchPauseToDisableScheduledFutureMap
        if (!this.autoSwitchPauseToDisableScheduledFutureMap.isEmpty()) {
            this.autoSwitchPauseToDisableScheduledFutureMap.forEach((clientId, futre) -> {
                futre.cancel(true);
                futre = null;
            });
            this.autoSwitchPauseToDisableScheduledFutureMap.clear();
        }
        // 停止serverHandler
        serverHandler.terminate();
    }

    /**
     * 啟動sender建立與IMS的連線
     *
     * @param logData
     * @param configuration
     * @param postConstruct
     */
    private void runSender(LogData logData, CBSGatewayClientSenderConfiguration configuration, boolean postConstruct) {
        synchronized (configuration.getClientId()) {
            // 如果腳位沒有啟用或者已经暂停, 則不要運行
            if (!configuration.isEnable() || configuration.isPause()) {
                logger.warn("[", ProgramName, ".runSender]", configuration.getName(), " cannot to run and build connection to IMS, MappingIndex:", configuration.getMappingIndex(), ",Enable:", configuration.isEnable(), ",Pause:", configuration.isPause());
                return;
            }
            if (this.senders.get(configuration.getMappingIndex()) == null) {
                logData.setProgramName(StringUtils.join(ProgramName, ".runSender"));
                logData.setRemark(StringUtils.join(configuration.getName(), " start to run and build connection to IMS, MappingIndex:", configuration.getMappingIndex()));
                this.logMessage(logData);
                // 啟動時能夠列印參數
                configuration.setPrinted(false);
                // Processor, 必須
                CBSGatewayClientSenderProcessRequest senderProcessRequest = new CBSGatewayClientSenderProcessRequest();
                senderProcessRequest.setStatistics(this.createStatistics(configuration)); // 塞入統計物件
                senderProcessRequest.initialization(configuration);
                // Gateway, 必須
                CBSGatewayClientSender sender = new CBSGatewayClientSender();
                sender.initialization(configuration, senderProcessRequest);
                // 監聽sender的狀態
                notification.addConnStateListener(configuration, this);
                // just warning for check if already exists
                // if (this.senders.get(configuration.getMappingIndex()) != null) {
                //     logger.warn("[", ProgramName, ".runSender]", configuration.getName(), " already exists, MappingIndex: " + configuration.getMappingIndex());
                // }
                // 增加到List中
                this.senders.set(configuration.getMappingIndex(), sender);
                this.senderProcessRequests.set(configuration.getMappingIndex(), senderProcessRequest);
                // 增加到Map中
                // this.clientIdToIMSTransmissionMap.computeIfAbsent(configuration.getClientId(), k -> new ArrayList<>()).add(sender);
                // 2026-06-08 Richard modified start的動作放在這裡, 保證上面三行代碼有先走到, 否則後續的notify流程可能會有問題
                if (!postConstruct) {
                    sender.start();
                }
            } else {
                logger.warn("[", ProgramName, ".runSender]", configuration.getName(), " is running, MappingIndex:", configuration.getMappingIndex());
            }
        }
    }

    /**
     * 啟動receiver建立與IMS的連線
     *
     * @param logData
     * @param configuration
     * @param postConstruct
     */
    private void runReceiver(LogData logData, CBSGatewayClientReceiverConfiguration configuration, boolean postConstruct) {
        synchronized (configuration.getClientId()) {
            // 如果腳位沒有啟用或者已经暂停, 則不要運行
            if (!configuration.isEnable() || configuration.isPause()) {
                logger.warn("[", ProgramName, ".runReceiver]", configuration.getName(), " cannot to run and build connection to IMS, MappingIndex:", configuration.getMappingIndex(), ",Enable:", configuration.isEnable(), ",Pause:", configuration.isPause());
                return;
            }
            if (this.receivers.get(configuration.getMappingIndex()) == null) {
                logData.setProgramName(StringUtils.join(ProgramName, ".runReceiver"));
                logData.setRemark(StringUtils.join(configuration.getName(), " start to run and build connection to IMS, MappingIndex:", configuration.getMappingIndex()));
                this.logMessage(logData);
                // 啟動時能夠列印參數
                configuration.setPrinted(false);
                // Processor, 必須
                CBSGatewayClientReceiverProcessRequest receiverProcessRequest = new CBSGatewayClientReceiverProcessRequest();
                receiverProcessRequest.initialization(configuration);
                // Gateway, 必須
                CBSGatewayClientReceiver receiver = new CBSGatewayClientReceiver();
                receiver.initialization(configuration, receiverProcessRequest);
                // 監聽receiver的狀態
                notification.addConnStateListener(configuration, this);
                // just warning for check if already exists
                // if (this.receivers.get(configuration.getMappingIndex()) != null) {
                //     logger.warn("[", ProgramName, ".runReceiver]", configuration.getName(), " already exists, MappingIndex: " + configuration.getMappingIndex());
                // }
                // 增加到List中
                this.receivers.set(configuration.getMappingIndex(), receiver);
                // 增加到Map中
                // this.clientIdToIMSTransmissionMap.computeIfAbsent(configuration.getClientId(), k -> new ArrayList<>()).add(receiver);
                // 2026-06-08 Richard modified start的動作放在這裡, 保證上面三行代碼有先走到, 否則後續的notify流程可能會有問題
                if (!postConstruct) {
                    receiver.start();
                }
            } else {
                logger.warn("[", ProgramName, ".runReceiver]", configuration.getName(), " is running, MappingIndex:", configuration.getMappingIndex());
            }
        }
    }

    /**
     * 啟動server接收CBSAdapter丟過來的電文
     *
     * @param logData
     * @param configuration
     */
    private void runServer(LogData logData, CBSGatewayServerConfiguration configuration) {
        synchronized (this.serverConfiguration) {
            // 如果沒有啟用, 則不要運行
            if (this.server == null) {
                logData.setProgramName(StringUtils.join(ProgramName, ".runServer"));
                logData.setRemark(StringUtils.join(configuration.getGateway(), " start to build Socket Server connection for CBSAdapter"));
                this.logMessage(logData);
                // ProcessorManager, 必須
                CBSGatewayServerProcessRequestManager manager = new CBSGatewayServerProcessRequestManager();
                manager.initialization(configuration, this.senderProcessRequests);
                // HandlerAdapter, 必須
                CBSGatewayServerChannelInboundHandlerAdapter adapter = new CBSGatewayServerChannelInboundHandlerAdapter();
                adapter.initialization(configuration, manager);
                // IP過濾規則, 必須
                CBSGatewayServerRuleIpFilter ruleIpFilter = new CBSGatewayServerRuleIpFilter();
                ruleIpFilter.initialization(configuration);
                // Gateway, 必須
                this.server = new CBSGatewayServer();
                this.server.initialization(configuration, adapter, manager, ruleIpFilter);
                this.server.run(configuration.isEnable());
                if (!configuration.isEnable()) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".runServer"));
                    logData.setRemark(StringUtils.join(configuration.getGateway(), " cannot to build Socket Server connection for CBSAdapter, Enable:", configuration.isEnable()));
                    logMessage(Level.WARN, logData);
                }
            } else if (!this.server.isListening()) {
                if (!configuration.isEnable()) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".runServer"));
                    logData.setRemark(StringUtils.join(configuration.getGateway(), " cannot to re-build Socket Server connection for CBSAdapter, Enable:", configuration.isEnable()));
                    logMessage(Level.WARN, logData);
                    return;
                }
                logData.setProgramName(StringUtils.join(ProgramName, ".runServer"));
                logData.setRemark(StringUtils.join(configuration.getGateway(), " start to re-build Socket Server connection for CBSAdapter"));
                this.logMessage(logData);
                this.server.establishConnection();
            } else {
                logger.warn("[", ProgramName, ".runServer]", configuration.getGateway(), " already build Socket Server connection for CBSAdapter");
            }
        }
    }

    /**
     * 接收連線狀態發生變化
     *
     * @param configuration
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(CBSGatewayClientConfiguration configuration, IMSTransmissionConnState state, Throwable t) {
        // 2025-10-28 Richard add 目前只需要判斷以下三種狀態, 如果不是則直接跳出
        if (!(state == IMSTransmissionConnState.CLIENT_CONNECTED || state == IMSTransmissionConnState.CLIENT_CONNECTING_FAILED || state == IMSTransmissionConnState.CLIENT_DISCONNECTED)) {
            return;
        }
        LogData logData = new LogData();
        logger.debug("[", ProgramName, ".connStateChanged][", state, "][", configuration.getCbsType(), "][", configuration.getClientId(), "]enter");
        synchronized (configuration.getLockConnStateChanged()) { // 2026-07-03 Richard modified 以A/B腳位為Group進行lock, 避免其他被其他腳位影響
            logger.debug("[", ProgramName, ".connStateChanged][", state, "][", configuration.getCbsType(), "][", configuration.getClientId(), "][", configuration.getLockConnStateChanged(), "]enter synchronized");
            if (state == IMSTransmissionConnState.CLIENT_CONNECTED) {
                // 需要依次啟動
                if (this.cbsGatewayManager.isHandleSenderAndReceiverBoth()) {
                    // 如果是sender連線成功, 則啟動receiver
                    if (configuration instanceof CBSGatewayClientSenderConfiguration) {
                        CBSGatewayClientSenderConfiguration senderConfiguration = (CBSGatewayClientSenderConfiguration) configuration;
                        CBSGatewayClientReceiverConfiguration receiverConfiguration = this.receiverConfigurations.get(senderConfiguration.getMappingIndex());
                        logData.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
                        logData.setRemark(StringUtils.join(senderConfiguration.getName(), " has been connected to IMS, ", receiverConfiguration.getName(), " start to run and build connection to IMS, MappingIndex:", senderConfiguration.getMappingIndex()));
                        this.logMessage(logData);
                        this.runReceiver(logData, receiverConfiguration, false);
                    }
                    // 如果是receiver連線成功
                    else if (configuration instanceof CBSGatewayClientReceiverConfiguration) {
                        CBSGatewayClientReceiverConfiguration receiverConfiguration = (CBSGatewayClientReceiverConfiguration) configuration;
                        CBSGatewayClientSenderConfiguration senderConfiguration = this.senderConfigurations.get(receiverConfiguration.getMappingIndex());
                        senderConfiguration.setReceiverConnected(true); // receiver連線成功
                        logger.debug("[", ProgramName, ".connStateChanged][", state, "][", configuration.getCbsType(), "][", configuration.getClientId(), "][", configuration.getLockConnStateChanged(), "]receiverConnected=", senderConfiguration.isReceiverConnected());
                    }
                }
                // 2026-07-06 Richard modified 判斷啟動Server交給Thread處理
                serverHandler.handle(ServerHandle.START);
            } else if (state == IMSTransmissionConnState.CLIENT_CONNECTING_FAILED) {
                if (this.cbsGatewayManager.isHandleSenderAndReceiverBoth()) {
                    // 當有一個腳位重新建立連線失敗時, 則對應腳位斷開
                    this.connStateChanged(configuration, IMSTransmissionConnState.CLIENT_DISCONNECTED, t);
                }
            } else if (state == IMSTransmissionConnState.CLIENT_DISCONNECTED) {
                // 當有一個腳位斷開時, 則對應腳位斷開
                if (this.cbsGatewayManager.isHandleSenderAndReceiverBoth()) {
                    CBSGatewayClientSenderConfiguration senderConfiguration = null;
                    CBSGatewayClientReceiverConfiguration receiverConfiguration = null;
                    // 如果是Sender斷線, 則對應取出Receiver的Configuration物件
                    if (configuration instanceof CBSGatewayClientSenderConfiguration) {
                        senderConfiguration = (CBSGatewayClientSenderConfiguration) configuration;
                        receiverConfiguration = this.receiverConfigurations.get(senderConfiguration.getMappingIndex());
                    }
                    // 如果是Receiver斷線, 則對應取出Sender的Configuration物件
                    else if (configuration instanceof CBSGatewayClientReceiverConfiguration) {
                        receiverConfiguration = (CBSGatewayClientReceiverConfiguration) configuration;
                        senderConfiguration = this.senderConfigurations.get(receiverConfiguration.getMappingIndex());
                        senderConfiguration.setReceiverConnected(false); // receiver斷線
                        logger.debug("[", ProgramName, ".connStateChanged][", state, "][", configuration.getCbsType(), "][", configuration.getClientId(), "][", configuration.getLockConnStateChanged(), "]receiverConnected=", senderConfiguration.isReceiverConnected());
                    }
                    if (senderConfiguration != null && receiverConfiguration != null) {
                        // 斷開Receiver
                        this.terminateReceiver(logData, receiverConfiguration);
                        // 斷開Sender
                        this.terminateSender(logData, senderConfiguration);
                        // 重新啟動sender的連線
                        this.scheduleToRunSender(logData, senderConfiguration);
                    }
                }
                // 2026-07-06 Richard modified 判斷停止Server交給Thread處理
                serverHandler.handle(ServerHandle.STOP);
            }
            logger.debug("[", ProgramName, ".connStateChanged][", state, "][", configuration.getCbsType(), "][", configuration.getClientId(), "][", configuration.getLockConnStateChanged(), "]exit synchronized");
        }
        logger.debug("[", ProgramName, ".connStateChanged][", state, "][", configuration.getCbsType(), "][", configuration.getClientId(), "]exit");
    }

    /**
     * 是否全部的Sender和Receiver都已經連線中
     *
     * @return
     */
    public boolean isBothAllSenderAndReceiverConnected() {
        return CollectionUtils.isNotEmpty(senders) && senders.stream().filter(s -> s != null && IMSTransmissionConnState.isClientConnected(s.getCurrentConnState())).count() == this.senderConfigurations.stream().filter(CBSGatewayClientSenderConfiguration::isActived).count()
                && CollectionUtils.isNotEmpty(receivers) && receivers.stream().filter(s -> s != null && IMSTransmissionConnState.isClientConnected(s.getCurrentConnState())).count() == this.receiverConfigurations.stream().filter(CBSGatewayClientReceiverConfiguration::isActived).count();
    }

    /**
     * 是否全部的Sender和Receiver都已經斷開
     *
     * @return
     */
    public boolean isBothAllSenderAndReceiverDisconnected() {
        return CollectionUtils.isNotEmpty(senders) && this.senders.stream().filter(s -> s != null && IMSTransmissionConnState.isClientConnected(s.getCurrentConnState())).count() == 0
                && CollectionUtils.isNotEmpty(receivers) && receivers.stream().filter(s -> s != null && IMSTransmissionConnState.isClientConnected(s.getCurrentConnState())).count() == 0;
    }

    /**
     * 終止Sender連線並銷毀
     *
     * @param logData
     * @param configuration
     */
    private void terminateSender(LogData logData, CBSGatewayClientSenderConfiguration configuration) {
        synchronized (configuration.getClientId()) {
            CBSGatewayClientSender sender = this.senders.get(configuration.getMappingIndex());
            if (sender != null) {
                // 從Map中移除
                // this.clientIdToIMSTransmissionMap.computeIfAbsent(configuration.getClientId(), k -> new ArrayList<>()).remove(sender);
                logData.setProgramName(StringUtils.join(ProgramName, ".terminateSender"));
                logData.setRemark(StringUtils.join(configuration.getName(), " start to disconnect from IMS, MappingIndex:", configuration.getMappingIndex()));
                logMessage(Level.WARN, logData);
                sender.terminate();
                sender = null;
                this.senders.set(configuration.getMappingIndex(), null);
                // 記得這裡一定要移除掉
                notification.removeConnStateListener(configuration, this);
            } else {
                logger.warn("[", ProgramName, ".terminateSender]", configuration.getName(), " already terminate, MappingIndex:", configuration.getMappingIndex());
            }
            CBSGatewayClientSenderProcessRequest senderProcessRequest = this.senderProcessRequests.get(configuration.getMappingIndex());
            if (senderProcessRequest != null) {
                logData.setProgramName(StringUtils.join(ProgramName, ".terminateSender"));
                logData.setRemark(StringUtils.join(configuration.getName(), " start to destroy ProcessRequest Object, MappingIndex:", configuration.getMappingIndex()));
                logMessage(Level.WARN, logData);
                senderProcessRequest = null;
                this.senderProcessRequests.set(configuration.getMappingIndex(), null);
            } else {
                logger.warn("[", ProgramName, ".terminateSender]", configuration.getName(), " already destroy ProcessRequest Object, MappingIndex:", configuration.getMappingIndex());
            }
        }
    }

    /**
     * 終止Receiver連線並銷毀
     *
     * @param logData
     * @param configuration
     */
    private void terminateReceiver(LogData logData, CBSGatewayClientReceiverConfiguration configuration) {
        synchronized (configuration.getClientId()) {
            CBSGatewayClientReceiver receiver = this.receivers.get(configuration.getMappingIndex());
            if (receiver != null) {
                // 從Map中移除
                // this.clientIdToIMSTransmissionMap.computeIfAbsent(configuration.getClientId(), k -> new ArrayList<>()).remove(receiver);
                logData.setProgramName(StringUtils.join(ProgramName, ".terminateReceiver"));
                logData.setRemark(StringUtils.join(configuration.getName(), " start to disconnect from IMS, MappingIndex:", configuration.getMappingIndex()));
                logMessage(Level.WARN, logData);
                receiver.terminate();
                receiver = null;
                this.receivers.set(configuration.getMappingIndex(), null);
                // 記得這裡一定要移除掉
                notification.removeConnStateListener(configuration, this);
            } else {
                logger.warn("[", ProgramName, ".terminateReceiver]", configuration.getName(), " already terminate, MappingIndex:", configuration.getMappingIndex());
            }
        }
    }

    /**
     * delay並重新啟動Sender
     *
     * @param logData
     * @param configuration
     */
    private void scheduleToRunSender(LogData logData, final CBSGatewayClientSenderConfiguration configuration) {
        synchronized (configuration.getClientId()) {
            if (!configuration.isEnable() || configuration.isPause()) {
                logger.warn("[", ProgramName, "scheduleToRunSender]", configuration.getName(), " cannot to run and build connection to IMS, MappingIndex:", configuration.getMappingIndex(), ",Enable:", configuration.isEnable(), ",Pause:", configuration.isPause());
                return;
            }
            // 2026-07-06 Richard modified 這裡加上判斷, 避免重複排程
            if (scheduleToRunSenderScheduledFutureMap.get(configuration.getClientId()) != null) {
                logger.warn("[", ProgramName, ".scheduleToRunSender]", configuration.getName(), " is already scheduled to build connection to IMS in ", configuration.getReestablishConnectionInterval(), " milliseconds, MappingIndex:", configuration.getMappingIndex());
            } else if (this.senders.get(configuration.getMappingIndex()) == null) {
                logData.setProgramName(StringUtils.join(ProgramName, ".scheduleToRunSender"));
                logData.setRemark(StringUtils.join(configuration.getName(), " will build connection to IMS after ", configuration.getReestablishConnectionInterval(), " milliseconds, MappingIndex:", configuration.getMappingIndex()));
                logMessage(Level.WARN, logData);
                ScheduledFuture<?> future = this.cbsGatewayManager.schedule(() -> {
                    putMDC();
                    logData.setProgramName(StringUtils.join(ProgramName, ".scheduleToRunSender"));
                    logData.setRemark(StringUtils.join(configuration.getName(), " start to run and build connection to IMS, MappingIndex:", configuration.getMappingIndex(), ", ReestablishConnectionInterval:", configuration.getReestablishConnectionInterval()));
                    logMessage(Level.WARN, logData);
                    this.runSender(logData, configuration, false);
                    scheduleToRunSenderScheduledFutureMap.remove(configuration.getClientId()); // 這裡移除掉
                }, configuration.getReestablishConnectionInterval(), TimeUnit.MILLISECONDS);
                scheduleToRunSenderScheduledFutureMap.put(configuration.getClientId(), future); // 存入map中
            } else {
                logger.warn("[", ProgramName, "scheduleToRunSender]", configuration.getName(), " is running, MappingIndex:", configuration.getMappingIndex());
            }
        }
    }

    /**
     * 停止Server
     *
     * @param logData
     */
    private void terminateServer(LogData logData) {
        synchronized (this.serverConfiguration) {
            if (this.server != null) {
                logData.setProgramName(StringUtils.join(ProgramName, ".terminateServer"));
                logData.setRemark(StringUtils.join("start to disconnect Socket Server connection for CBSAdapter"));
                logMessage(Level.WARN, logData);
                this.server.terminateConnection();
                this.server = null;
            } else {
                logData.setProgramName(StringUtils.join(ProgramName, ".terminateServer"));
                logData.setRemark(StringUtils.join("already terminate"));
                logMessage(Level.WARN, logData);
            }
        }
    }

    /**
     * 停止Server
     *
     * @param logData
     */
    private void stopServerListen(LogData logData) {
        synchronized (this.serverConfiguration) {
            if (this.server != null) {
                logData.setProgramName(StringUtils.join(ProgramName, ".stopServerListen"));
                logData.setRemark(StringUtils.join("start to stop Socket Server Listen for CBSAdapter"));
                logMessage(Level.WARN, logData);
                this.server.stopConnectionListen();
            } else {
                logData.setProgramName(StringUtils.join(ProgramName, ".stopServerListen"));
                logData.setRemark(StringUtils.join("already stop listen"));
                logMessage(Level.WARN, logData);
            }
        }
    }

    public List<CBSGatewayClientReceiver> getReceivers() {
        return receivers;
    }

    public List<CBSGatewayClientSender> getSenders() {
        return senders;
    }

    public String getTypeName() {
        return this.serverConfiguration.getCbsType();
    }

    public List<CBSGatewayClientSenderConfiguration> getSenderConfigurations() {
        return senderConfigurations;
    }

    public List<CBSGatewayClientReceiverConfiguration> getReceiverConfigurations() {
        return receiverConfigurations;
    }

    public CBSGatewayServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    public CBSGatewayServer getServer() {
        return server;
    }

    /**
     * 改變腳位狀態, 啟動或停止
     *
     * @param logData
     * @param clientId
     * @param action
     * @return
     */
    public boolean changeLineStatus(LogData logData, String clientId, ToCBSChangeLineStatusAction action) {
        if (CollectionUtils.isNotEmpty(this.senderConfigurations) && CollectionUtils.isNotEmpty(this.receiverConfigurations)) {
            // 先嘗試依據clientId從Sender裡找
            CBSGatewayClientConfiguration configuration = this.senderConfigurations.stream().filter(t -> t.getClientId().equals(clientId)).findFirst().orElse(null);
            if (configuration == null) {
                // 如果Sender裡找不到, 則再嘗試從Receiver中找
                configuration = this.receiverConfigurations.stream().filter(t -> t.getClientId().equals(clientId)).findFirst().orElse(null);
                if (configuration == null) {
                    // 如果Sender和Receiver中都沒有, 說明不在此Group中
                    return false;
                }
            }
            if (this.cbsGatewayManager.isHandleSenderAndReceiverBoth()) {
                CBSGatewayClientSenderConfiguration senderConfiguration = this.senderConfigurations.get(configuration.getMappingIndex());
                CBSGatewayClientReceiverConfiguration receiverConfiguration = this.receiverConfigurations.get(configuration.getMappingIndex());
                // 那麼啟動時也要Sender和Reciever同時啟動, 因為Sender啟動成功後, 會自動再啟動Receiver, 所以這裡不用call啟動Receiver的方法
                if (action == ToCBSChangeLineStatusAction.Enable) {
                    destroyScheduledTask(senderConfiguration); // 先將之前delay執行的Task停掉
                    destroyScheduledTask(receiverConfiguration); // 先將之前delay執行的Task停掉
                    senderConfiguration.setEnable(true); // 更新enable屬性
                    receiverConfiguration.setEnable(true); // 更新enable屬性
                    senderConfiguration.setPause(false); // 更新pause屬性
                    receiverConfiguration.setPause(false); // 更新pause屬性
                    this.runSender(logData, senderConfiguration, false);
                }
                // 這裡斷開Receiver就好, Sender會跟著一起斷開
                else if (action == ToCBSChangeLineStatusAction.Disable) {
                    destroyScheduledTask(senderConfiguration); // 先將之前delay執行的Task停掉
                    destroyScheduledTask(receiverConfiguration); // 先將之前delay執行的Task停掉
                    senderConfiguration.setEnable(false); // 更新enable屬性
                    receiverConfiguration.setEnable(false); // 更新enable屬性
                    senderConfiguration.setPause(false); // 更新pause屬性
                    receiverConfiguration.setPause(false); // 更新pause屬性
                    this.terminateReceiver(logData, receiverConfiguration);
                }
            } else {
                if (configuration instanceof CBSGatewayClientSenderConfiguration) {
                    if (action == ToCBSChangeLineStatusAction.Enable) {
                        destroyScheduledTask(configuration); // 先將之前delay執行的Task停掉
                        configuration.setEnable(true); // 更新enable屬性
                        configuration.setPause(false); // 更新pause屬性
                        this.runSender(logData, (CBSGatewayClientSenderConfiguration) configuration, false);
                    } if (action == ToCBSChangeLineStatusAction.Disable) {
                        destroyScheduledTask(configuration); // 先將之前delay執行的Task停掉
                        configuration.setEnable(false); // 更新enable屬性
                        configuration.setPause(false); // 更新pause屬性
                        this.terminateSender(logData, (CBSGatewayClientSenderConfiguration) configuration);
                    }
                } else {
                    if (action == ToCBSChangeLineStatusAction.Enable) {
                        destroyScheduledTask(configuration); // 先將之前delay執行的Task停掉
                        configuration.setEnable(true); // 更新enable屬性
                        configuration.setPause(false); // 更新pause屬性
                        this.runReceiver(logData, (CBSGatewayClientReceiverConfiguration) configuration, false);
                    } if (action == ToCBSChangeLineStatusAction.Disable) {
                        destroyScheduledTask(configuration); // 先將之前delay執行的Task停掉
                        configuration.setEnable(false); // 更新enable屬性
                        configuration.setPause(false); // 更新pause屬性
                        this.terminateReceiver(logData, (CBSGatewayClientReceiverConfiguration) configuration);
                    }
                }
            }
            // 暫停
            if (action == ToCBSChangeLineStatusAction.Pause) {
                // 先設定pause為true, 這樣可以暫停該腳位將電文送給IMS主機
                if (this.cbsGatewayManager.isHandleSenderAndReceiverBoth()) {
                    CBSGatewayClientSenderConfiguration senderConfiguration = this.senderConfigurations.get(configuration.getMappingIndex());
                    CBSGatewayClientReceiverConfiguration receiverConfiguration = this.receiverConfigurations.get(configuration.getMappingIndex());
                    // 先判斷一下, 避免動作重複
                    if (!senderConfiguration.isEnable() || senderConfiguration.isPause() || !receiverConfiguration.isEnable() || receiverConfiguration.isPause()) {
                        logData.setProgramName(StringUtils.join(ProgramName, ".changeLineStatus"));
                        logData.setRemark(StringUtils.join(configuration.getName(), " cannot pause cause already pause or disable, MappingIndex:", configuration.getMappingIndex()));
                        logMessage(Level.WARN, logData);
                        return true;
                    }
                    destroyScheduledTask(senderConfiguration); // 先將之前delay執行的Task停掉
                    destroyScheduledTask(receiverConfiguration); // 先將之前delay執行的Task停掉
                    senderConfiguration.setPause(true); // 更新pause屬性
                    receiverConfiguration.setPause(true); // 更新pause屬性
                } else {
                    // 先判斷一下, 避免動作重複
                    if (!configuration.isEnable() || configuration.isPause()) {
                        logData.setProgramName(StringUtils.join(ProgramName, ".changeLineStatus"));
                        logData.setRemark(StringUtils.join(configuration.getName(), " cannot pause cause already pause or disable, MappingIndex:", configuration.getMappingIndex()));
                        logMessage(Level.WARN, logData);
                        return true;
                    }
                    destroyScheduledTask(configuration); // 先將之前delay執行的Task停掉
                    destroyScheduledTask(configuration); // 先將之前delay執行的Task停掉
                    configuration.setPause(true); // 更新pause屬性
                }
                // 如果自動切換Pause到Disable
                if (this.cbsGatewayManager.isAutoSwitchPauseToDisable()) {
                    // 指定時間之後自動切換
                    if (this.cbsGatewayManager.getAutoSwitchPauseToDisableDelayMilliseconds() > 0) {
                        logData.setProgramName(StringUtils.join(ProgramName, ".changeLineStatus"));
                        logData.setRemark(StringUtils.join(configuration.getName(), " will auto switch pause to disable after ",
                                this.cbsGatewayManager.getAutoSwitchPauseToDisableDelayMilliseconds(), " milliseconds, MappingIndex:", configuration.getMappingIndex()));
                        logMessage(Level.WARN, logData);
                        CBSGatewayClientConfiguration finalConfiguration = configuration;
                        ScheduledFuture<?> future = this.cbsGatewayManager.schedule(() -> {
                            autoSwitchPauseToDisableScheduledFutureMap.remove(finalConfiguration.getClientId()); // 這裡移除掉
                            putMDC();
                            logData.setProgramName(StringUtils.join(ProgramName, ".changeLineStatus"));
                            logData.setRemark(StringUtils.join(finalConfiguration.getName(), " auto switch pause to disable, MappingIndex:", finalConfiguration.getMappingIndex()));
                            logMessage(Level.WARN, logData);
                            this.changeLineStatus(logData, clientId, ToCBSChangeLineStatusAction.Disable);
                        }, this.cbsGatewayManager.getAutoSwitchPauseToDisableDelayMilliseconds(), TimeUnit.MILLISECONDS);
                        autoSwitchPauseToDisableScheduledFutureMap.put(configuration.getClientId(), future); // 存入map中
                    }
                    // 立即切換
                    else {
                        this.changeLineStatus(logData, clientId, ToCBSChangeLineStatusAction.Disable);
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 將delay執行的task停掉
     *
     * @param configuration
     */
    private void destroyScheduledTask(CBSGatewayClientConfiguration configuration) {
        ScheduledFuture<?> scheduleToRunSenderFuture = scheduleToRunSenderScheduledFutureMap.remove(configuration.getClientId());
        if (scheduleToRunSenderFuture != null) {
            scheduleToRunSenderFuture.cancel(true);
            scheduleToRunSenderFuture = null;
        }
        ScheduledFuture<?> autoSwitchPauseToDisableScheduledFuture = autoSwitchPauseToDisableScheduledFutureMap.remove(configuration.getClientId());
        if (autoSwitchPauseToDisableScheduledFuture != null) {
            autoSwitchPauseToDisableScheduledFuture.cancel(true);
            autoSwitchPauseToDisableScheduledFuture = null;
        }
    }

    /**
     * 改變Server狀態, 啟動監聽或停止監聽
     *
     * @param logData
     * @param endPoint
     * @param enable
     * @return
     */
    public boolean changeServerStatus(LogData logData, String endPoint, boolean enable) {
        if (this.serverConfiguration != null && this.serverConfiguration.getEndPoint().equals(endPoint)) {
            if (this.server != null) {
                logData.setProgramName(StringUtils.join(ProgramName, ".changeServerStatus"));
                logData.setRemark(StringUtils.join("Ready to Change Server Status, endPoint:", this.serverConfiguration.getEndPoint(), ", enable:", enable));
                this.logMessage(logData);
                try {
                    this.serverConfiguration.setEnable(enable); // 更新enable屬性
                    if (enable) {
                        this.server.establishConnection();
                    } else {
                        this.server.stopConnectionListen();
                    }
                    logData.setProgramName(StringUtils.join(ProgramName, ".changeServerStatus"));
                    logData.setRemark(StringUtils.join("Change Server Status succeed, endPoint:", this.serverConfiguration.getEndPoint(), ", enable:", enable));
                    this.logMessage(logData);
                } catch (Exception e) {
                    logData.setProgramException(e);
                    logData.setProgramName(StringUtils.join(ProgramName, ".changeServerStatus"));
                    logData.setRemark(StringUtils.join("Change Server Status failed, endPoint:", this.serverConfiguration.getEndPoint(), ", enable:", enable));
                    sendEMS(logData);
                }
            } else {
                logData.setProgramName(StringUtils.join(ProgramName, ".changeServerStatus"));
                logData.setRemark(StringUtils.join("Cannot Change Server Status cause server is null, endPoint:", this.serverConfiguration.getEndPoint(), ", enable:", enable));
                logMessage(Level.WARN, logData);
            }
            return true;
        }
        return false;
    }

    /**
     * 建立或獲取統計物件
     *
     * @param configuration
     * @return
     */
    private AtomicReference<CBSGatewayStatistic> createStatistics(CBSGatewayClientConfiguration configuration) {
        AtomicReference<CBSGatewayStatistic> statistic = this.statistics.get(configuration.getMappingIndex());
        if (statistic == null) {
            statistic = new AtomicReference<>(new CBSGatewayStatistic());
            this.statistics.set(configuration.getMappingIndex(), statistic);
        } else {
            statistic.updateAndGet(x -> CBSGatewayStatistic.clone(x, true));
        }
        return statistic;
    }

    /**
     * 獲取統計
     *
     * @param configuration
     * @return
     */
    public CBSGatewayStatistic getStatistic(CBSGatewayClientConfiguration configuration) {
        AtomicReference<CBSGatewayStatistic> statistic = this.statistics.get(configuration.getMappingIndex());
        if (statistic != null) {
            return statistic.get();
        }
        return null;
    }

    private class ServerHandler extends Thread {
        private boolean running = true;
        private final List<ServerHandle> handles = new ArrayList<>();

        public ServerHandler() {
            super(StringUtils.join("ServerHandler-", serverConfiguration.getCbsType()));
            start();
        }

        public void terminate() {
            this.running = false;
            synchronized (this.handles) {
                this.handles.notifyAll();
            }
        }

        public void handle(ServerHandle handle) {
            synchronized (this.handles) {
                this.handles.add(handle);
                this.handles.notifyAll();
            }
        }

        @Override
        public void run() {
            putMDC();
            while (running) {
                ServerHandle[] arrays;
                synchronized (this.handles) {
                    if (this.handles.isEmpty()) {
                        logger.warn("No handles, waiting for notification");
                        try {
                            this.handles.wait();
                        } catch (InterruptedException e) {
                            logger.warn(e, e.getMessage());
                        }
                        logger.warn("Notified, continue to run, ", this.handles.size());
                    }
                    arrays = new ServerHandle[this.handles.size()];
                    this.handles.toArray(arrays);
                    this.handles.clear();
                }
                for (ServerHandle handle : arrays) {
                    switch (handle) {
                        case START:
                            // 當sender和receiver都成功連到IMS時, 才啟動server
                            if (isBothAllSenderAndReceiverConnected()) {
                                logContext.setProgramName(StringUtils.join(ProgramName, ".ServerHandler.run"));
                                logContext.setRemark(StringUtils.join("Both All Sender(", StringUtils.join(senderConfigurations.stream().map(IMSTransmissionConfiguration::getName).collect(Collectors.toList()), ","), ") and Receiver(", StringUtils.join(receiverConfigurations.stream().map(IMSTransmissionConfiguration::getName).collect(Collectors.toList()), ","), ") has been connected to IMS, start to build Socket Server connection for CBSAdapter"));
                                logMessage(logContext);
                                runServer(logContext, serverConfiguration);
                            }
                            break;
                        case STOP:
                            // 如果Sender和Receiver全部都斷開了, 則停止Server
                            if (isBothAllSenderAndReceiverDisconnected() && server != null) {
                                logContext.setProgramName(StringUtils.join(ProgramName, ".ServerHandler.run"));
                                logContext.setRemark(StringUtils.join("Both All Sender(", StringUtils.join(senderConfigurations.stream().map(IMSTransmissionConfiguration::getName).collect(Collectors.toList()), ","), ") and Receiver(", StringUtils.join(receiverConfigurations.stream().map(IMSTransmissionConfiguration::getName).collect(Collectors.toList()), ","), ") has been disconnected from IMS, start to stop Socket Server Listen for CBSAdapter"));
                                logMessage(Level.WARN, logContext);
                                // 2025-09-16 Richard modified 停止Server Listen Port
                                stopServerListen(logContext);
                            }
                            break;
                    }
                }
            }
        }
    }

    private enum ServerHandle {
        START, STOP;
    }

    // public Map<String, List<IMSTransmission<?, ?>>> getClientIdToIMSTransmissionMap() {
    //     return clientIdToIMSTransmissionMap;
    // }
}
