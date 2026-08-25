package com.syscom.fep.gateway.ims.cbs;

import com.google.gson.Gson;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.ims.IMSTransmission;
import com.syscom.fep.gateway.ims.IMSTransmissionMonitor;
import com.syscom.fep.gateway.ims.cbs.receiver.CBSGatewayClientReceiverConfiguration;
import com.syscom.fep.gateway.ims.cbs.sender.CBSGatewayClientSenderConfiguration;
import com.syscom.fep.gateway.netty.cbs.server.CBSGatewayServerConfiguration;
import com.syscom.fep.vo.communication.ToCBSChangeLineStatusAction;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "spring.fep.gateway.transmission.cbs")
// RefreshScope
public class CBSGatewayManager extends FEPBase {
    @NestedConfigurationProperty
    private final List<CBSGatewayServerConfiguration> fepap = new ArrayList<>();
    @NestedConfigurationProperty
    private final List<CBSGatewayClientSenderConfiguration> sender = new ArrayList<>();
    @NestedConfigurationProperty
    private final List<CBSGatewayClientReceiverConfiguration> receiver = new ArrayList<>();
    @NestedConfigurationProperty
    private final List<CBSGatewayClientSenderConfiguration> alternativeSender = new ArrayList<>();
    @NestedConfigurationProperty
    private final List<CBSGatewayClientReceiverConfiguration> alternativeReceiver = new ArrayList<>();
    /**
     * 線程池核心數設定
     */
    private int executorCorePoolSize = 1;
    /**
     * 線程池核心數最大值設定
     */
    private int executorMaximumPoolSize = 1;
    /**
     * 執行緒線程池中線程alive時間, 單位毫秒
     */
    private long executorKeepAliveTime = 0;
    /**
     * 執行緒線程池Queue Size
     */
    private int executorQueueCapacity = Integer.MAX_VALUE;
    /**
     * 排程線程池核心數設定
     */
    private int scheduledPoolSize = 1;
    /**
     * 同時處理Sender和Receiver
     * 例如, 一組腳位中不論sender或receiver若有發生斷線, 是否把該組另一個腳位也切斷
     * 啟動Sender則Sender啟動成功後再啟動Receiver
     */
    private boolean handleSenderAndReceiverBoth = true;
    /**
     * 一共有多少組Gateway
     */
    private final List<CBSGatewayGroup> gatewayGroups = new ArrayList<>();
    /**
     * 線程池
     */
    private ExecutorService executor;
    /**
     * 用來執行排程
     */
    private ScheduledExecutorService scheduledExecutorService;
    /**
     * 是否自動切換pause到disable
     */
    private boolean autoSwitchPauseToDisable = true;
    /**
     * 暫停後多少毫秒自動切換到停用
     */
    private long autoSwitchPauseToDisableDelayMilliseconds = 2 * 60 * 1000L;

    private void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.CBSGW.name());
    }

    public List<CBSGatewayServerConfiguration> getFepap() {
        return fepap;
    }

    public List<CBSGatewayClientSenderConfiguration> getSender() {
        return sender;
    }

    public List<CBSGatewayClientReceiverConfiguration> getReceiver() {
        return receiver;
    }

    public List<CBSGatewayClientSenderConfiguration> getAlternativeSender() {
        return alternativeSender;
    }

    public List<CBSGatewayClientReceiverConfiguration> getAlternativeReceiver() {
        return alternativeReceiver;
    }

    public int getExecutorCorePoolSize() {
        return executorCorePoolSize;
    }

    public void setExecutorCorePoolSize(int executorCorePoolSize) {
        this.executorCorePoolSize = executorCorePoolSize;
    }

    public int getExecutorMaximumPoolSize() {
        return executorMaximumPoolSize;
    }

    public void setExecutorMaximumPoolSize(int executorMaximumPoolSize) {
        this.executorMaximumPoolSize = executorMaximumPoolSize;
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

    public int getScheduledPoolSize() {
        return scheduledPoolSize;
    }

    public void setScheduledPoolSize(int scheduledPoolSize) {
        this.scheduledPoolSize = scheduledPoolSize;
    }

    public boolean isHandleSenderAndReceiverBoth() {
        return handleSenderAndReceiverBoth;
    }

    public void setHandleSenderAndReceiverBoth(boolean handleSenderAndReceiverBoth) {
        this.handleSenderAndReceiverBoth = handleSenderAndReceiverBoth;
    }

    public boolean isAutoSwitchPauseToDisable() {
        return autoSwitchPauseToDisable;
    }

    public void setAutoSwitchPauseToDisable(boolean autoSwitchPauseToDisable) {
        this.autoSwitchPauseToDisable = autoSwitchPauseToDisable;
    }

    public long getAutoSwitchPauseToDisableDelayMilliseconds() {
        return autoSwitchPauseToDisableDelayMilliseconds;
    }

    public void setAutoSwitchPauseToDisableDelayMilliseconds(long autoSwitchPauseToDisableDelayMilliseconds) {
        this.autoSwitchPauseToDisableDelayMilliseconds = autoSwitchPauseToDisableDelayMilliseconds;
    }

    /**
     * 只是為了方便列印參數明細log
     */
    private void setConfigurationPropertiesPrefix() {
        StringBuilder sb = new StringBuilder();
        ConfigurationProperties configurationProperties = this.getClass().getAnnotation(ConfigurationProperties.class);
        for (int i = 0; i < fepap.size(); i++) {
            fepap.get(i).setConfigurationPropertiesPrefix(StringUtils.join(configurationProperties.prefix(), ".fepap[", i, "]"));
            sb.append(ConfigurationPropertiesUtil.info(fepap.get(i), null, true));
        }
        for (int i = 0; i < sender.size(); i++) {
            sender.get(i).setConfigurationPropertiesPrefix(StringUtils.join(configurationProperties.prefix(), ".sender[", i, "]"));
            sender.get(i).setEnable(true); // 預設啟用
            sender.get(i).setLineType(CBSGatewayLineType.Primary);
            sb.append(ConfigurationPropertiesUtil.info(sender.get(i), null, true, sender.get(i).excludeFields()));
        }
        for (int i = 0; i < receiver.size(); i++) {
            receiver.get(i).setConfigurationPropertiesPrefix(StringUtils.join(configurationProperties.prefix(), ".receiver[", i, "]"));
            receiver.get(i).setEnable(true); // 預設啟用
            receiver.get(i).setLineType(CBSGatewayLineType.Primary);
            sb.append(ConfigurationPropertiesUtil.info(receiver.get(i), null, true, receiver.get(i).excludeFields()));
        }
        for (int i = 0; i < alternativeSender.size(); i++) {
            alternativeSender.get(i).setConfigurationPropertiesPrefix(StringUtils.join("spring.fep.gateway.transmission.cbs.alternative-sender[", i, "]"));
            alternativeSender.get(i).setEnable(false); // 預設不啟用
            alternativeSender.get(i).setLineType(CBSGatewayLineType.Alternative);
            sb.append(ConfigurationPropertiesUtil.info(alternativeSender.get(i), null, true, alternativeSender.get(i).excludeFields()));
        }
        for (int i = 0; i < alternativeReceiver.size(); i++) {
            alternativeReceiver.get(i).setConfigurationPropertiesPrefix(StringUtils.join("spring.fep.gateway.transmission.cbs.alternative-receiver[", i, "]"));
            alternativeReceiver.get(i).setEnable(false); // 預設不啟用
            alternativeReceiver.get(i).setLineType(CBSGatewayLineType.Alternative);
            sb.append(ConfigurationPropertiesUtil.info(alternativeReceiver.get(i), null, true, alternativeReceiver.get(i).excludeFields()));
        }
        sb.append(ConfigurationPropertiesUtil.info(this, null, "fepap", "sender", "receiver", "alternativeReceiver", "alternativeSender", "gatewayGroups", "executor", "scheduledExecutorService"));
        LogHelperFactory.getGeneralLogger().info("CBS Gateway Manager Configuration:\r\n", sb.toString());
    }

    public List<CBSGatewayGroup> getGatewayGroups() {
        return gatewayGroups;
    }

    @PostConstruct
    public void run() {
        this.putMDC();
        // 這裡依據配置檔中設定receiver的clientId決定executorPoolSize的值
        if (!receiver.isEmpty()) {
            int clientIdsCount = receiver.stream().mapToInt(t -> StringUtil.split(t.getClientId(), ',').size()).sum();
            if (executorCorePoolSize < clientIdsCount) {
                LogHelperFactory.getGeneralLogger().warn("The value of 'executorPoolSize' will be overwrite, old:", executorCorePoolSize, ", new:", clientIdsCount);
                executorCorePoolSize = clientIdsCount;
                executorMaximumPoolSize = clientIdsCount;
            }
        }
        // 這裡依據配置檔中設定alternative receiver的clientId決定executorMaximumPoolSize的值
        if (!alternativeReceiver.isEmpty()) {
            int clientIdsCount = receiver.stream().mapToInt(t -> StringUtil.split(t.getClientId(), ',').size()).sum();
            if (executorMaximumPoolSize < executorCorePoolSize + clientIdsCount) {
                LogHelperFactory.getGeneralLogger().warn("The value of 'executorMaximumPoolSize' will be overwrite, old:", executorMaximumPoolSize, ", new:", (executorCorePoolSize + clientIdsCount));
                executorMaximumPoolSize = executorCorePoolSize + clientIdsCount;
            }
        }
        this.executor = ThreadPoolFactory.newThreadPool(
                executorCorePoolSize,
                executorMaximumPoolSize,
                executorKeepAliveTime,
                TimeUnit.MILLISECONDS,
                executorQueueCapacity,
                new SimpleThreadFactory(StringUtils.join(SvrConst.SVR_CBS_GATEWAY, "Executor")),
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.scheduledExecutorService = Executors.newScheduledThreadPool(
                scheduledPoolSize,
                new SimpleThreadFactory(StringUtils.join(SvrConst.SVR_CBS_GATEWAY, "ScheduledExecutor")));
        this.setConfigurationPropertiesPrefix();
        this.runGateway(true);
    }

    @PreDestroy
    public void terminate() {
        this.putMDC();
        ThreadPoolFactory.shutdown(this.executor, SvrConst.SVR_CBS_GATEWAY, "Executor");
        ThreadPoolFactory.shutdown(this.scheduledExecutorService, SvrConst.SVR_CBS_GATEWAY, "ScheduledExecutor");
        for (CBSGatewayGroup gatewayGroup : gatewayGroups) {
            gatewayGroup.terminate();
            gatewayGroup = null;
        }
        gatewayGroups.clear();
    }

    public void runGateway() {
        this.runGateway(false);
    }

    private void runGateway(boolean postConstruct) {
        if (gatewayGroups.isEmpty()) {
            ConfigurationProperties configurationProperties = this.getClass().getAnnotation(ConfigurationProperties.class);
            for (int configurationIndex = 0; configurationIndex < sender.size(); configurationIndex++) {
                CBSGatewayServerConfiguration serverConfiguration = fepap.get(configurationIndex);
                CBSGatewayClientSenderConfiguration senderConfiguration = sender.get(configurationIndex);
                CBSGatewayClientReceiverConfiguration receiverConfiguration = receiver.get(configurationIndex);
                CBSGatewayClientSenderConfiguration alternativeSenderConfiguration = alternativeSender.get(configurationIndex);
                CBSGatewayClientReceiverConfiguration alternativeReceiverConfiguration = alternativeReceiver.get(configurationIndex);
                // 拆解多個ClientId
                List<String> senderClientIds = StringUtil.split(senderConfiguration.getClientId(), ',');
                List<String> receiverClientIds = StringUtil.split(receiverConfiguration.getClientId(), ',');
                List<String> alternativeSenderClientIds = StringUtil.split(alternativeSenderConfiguration.getClientId(), ',');
                List<String> alternativeReceiverClientIds = StringUtil.split(alternativeReceiverConfiguration.getClientId(), ',');
                List<CBSGatewayClientSenderConfiguration> senderConfigurations = new ArrayList<>();
                List<CBSGatewayClientReceiverConfiguration> receiverConfigurations = new ArrayList<>();
                // 根據拆開的ClientId, 一個ClientId對應一個IMS實例
                for (int clientIdIndex = 0; clientIdIndex < senderClientIds.size(); clientIdIndex++) {
                    // Primary
                    String lockConnStateChanged = StringUtils.join(Arrays.asList(senderConfiguration.getCbsType(), senderClientIds.get(clientIdIndex), receiverClientIds.get(clientIdIndex)), "-"); // 2026-07-03 Richard add 以A/B腳位為Group進行lock, 避免其他被其他腳位影響
                    // Sender Configuration
                    CBSGatewayClientSenderConfiguration newSenderConfiguration = new CBSGatewayClientSenderConfiguration();
                    BeanUtils.copyProperties(senderConfiguration, newSenderConfiguration);
                    newSenderConfiguration.setClientId(senderClientIds.get(clientIdIndex)); // 塞入拆解的Sender Client Id
                    newSenderConfiguration.setMappingIndex(senderConfigurations.size());
                    newSenderConfiguration.setConfigurationPropertiesPrefix(StringUtils.join(configurationProperties.prefix(), ".sender[", configurationIndex, "-", clientIdIndex, "]"));
                    newSenderConfiguration.setLockConnStateChanged(lockConnStateChanged); // 2026-07-03 Richard add 塞入sync lock物件, 以A/B腳位為Group進行lock, 避免其他被其他腳位影響
                    senderConfigurations.add(newSenderConfiguration);
                    // Receiver Configuration
                    CBSGatewayClientReceiverConfiguration newReceiverConfiguration = new CBSGatewayClientReceiverConfiguration();
                    BeanUtils.copyProperties(receiverConfiguration, newReceiverConfiguration);
                    newReceiverConfiguration.setClientId(receiverClientIds.get(clientIdIndex)); // 塞入拆解的Receiver Client Id
                    newReceiverConfiguration.setMappingIndex(receiverConfigurations.size());
                    newReceiverConfiguration.setConfigurationPropertiesPrefix(StringUtils.join(configurationProperties.prefix(), ".receiver[", configurationIndex, "-", clientIdIndex, "]"));
                    newReceiverConfiguration.setLockConnStateChanged(lockConnStateChanged); // 2026-07-03 Richard add 塞入sync lock物件, 以A/B腳位為Group進行lock, 避免其他被其他腳位影響
                    receiverConfigurations.add(newReceiverConfiguration);
                    // Alternative
                    String lockConnStateChangedAlternative = StringUtils.join(Arrays.asList(alternativeSenderConfiguration.getCbsType(), alternativeSenderClientIds.get(clientIdIndex), alternativeReceiverClientIds.get(clientIdIndex)), "-"); // 2026-07-03 Richard add 以A/B腳位為Group進行lock, 避免其他被其他腳位影響
                    // Alternative Sender Configuration
                    CBSGatewayClientSenderConfiguration newAlternativeSenderConfiguration = new CBSGatewayClientSenderConfiguration();
                    BeanUtils.copyProperties(alternativeSenderConfiguration, newAlternativeSenderConfiguration);
                    newAlternativeSenderConfiguration.setClientId(alternativeSenderClientIds.get(clientIdIndex)); // 塞入拆解的Sender Client Id
                    newAlternativeSenderConfiguration.setMappingIndex(senderConfigurations.size());
                    newAlternativeSenderConfiguration.setConfigurationPropertiesPrefix(StringUtils.join("spring.fep.gateway.transmission.cbs.alternative-sender[", configurationIndex, "-", clientIdIndex, "]"));
                    newAlternativeSenderConfiguration.setLockConnStateChanged(lockConnStateChangedAlternative); // 2026-07-03 Richard add 塞入sync lock物件, 以A/B腳位為Group進行lock, 避免其他被其他腳位影響
                    senderConfigurations.add(newAlternativeSenderConfiguration);
                    // Alternative Receiver Configuration
                    CBSGatewayClientReceiverConfiguration newAlternativeReceiverConfiguration = new CBSGatewayClientReceiverConfiguration();
                    BeanUtils.copyProperties(alternativeReceiverConfiguration, newAlternativeReceiverConfiguration);
                    newAlternativeReceiverConfiguration.setClientId(alternativeReceiverClientIds.get(clientIdIndex)); // 塞入拆解的Receiver Client Id
                    newAlternativeReceiverConfiguration.setMappingIndex(receiverConfigurations.size());
                    newAlternativeReceiverConfiguration.setConfigurationPropertiesPrefix(StringUtils.join("spring.fep.gateway.transmission.cbs.alternative-receiver[", configurationIndex, "-", clientIdIndex, "]"));
                    newAlternativeReceiverConfiguration.setLockConnStateChanged(lockConnStateChangedAlternative); // 2026-07-03 Richard add 塞入sync lock物件, 以A/B腳位為Group進行lock, 避免其他被其他腳位影響
                    receiverConfigurations.add(newAlternativeReceiverConfiguration);
                }
                // CBSGatewayGroup
                if (!senderConfigurations.isEmpty() && !receiverConfigurations.isEmpty()) {
                    CBSGatewayGroup gatewayGroup = new CBSGatewayGroup(this, serverConfiguration, senderConfigurations, receiverConfigurations);
                    gatewayGroups.add(gatewayGroup);
                    gatewayGroup.run(postConstruct); // postConstruct下會自動啟動Gateway程式
                } else {
                    LogHelperFactory.getGeneralLogger().warn("cannot create CBSGatewayGroup, index:", configurationIndex);
                }
            }
        } else {
            for (CBSGatewayGroup gatewayGroup : gatewayGroups) {
                gatewayGroup.run(postConstruct); // postConstruct下會自動啟動Gateway程式
            }
        }
    }

    public void execute(Runnable runnable) {
        this.executor.execute(() -> {this.putMDC(); runnable.run();});
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
        return this.scheduledExecutorService.schedule(() -> {this.putMDC(); runnable.run();}, delay, unit);
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
        boolean result = false;
        if (CollectionUtils.isNotEmpty(this.gatewayGroups)) {
            Iterator<CBSGatewayGroup> it = this.gatewayGroups.iterator();
            while (it.hasNext() && !result) {
                CBSGatewayGroup gatewayGroup = it.next();
                result = gatewayGroup.changeLineStatus(logData, clientId, action);
            }
        }
        return result;
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
        boolean result = false;
        if (CollectionUtils.isNotEmpty(this.gatewayGroups)) {
            Iterator<CBSGatewayGroup> it = this.gatewayGroups.iterator();
            while (it.hasNext() && !result) {
                CBSGatewayGroup gatewayGroup = it.next();
                result = gatewayGroup.changeServerStatus(logData, endPoint, enable);
            }
        }
        return result;
    }

    // public String getClientIdToLocal() {
    //     if (CollectionUtils.isNotEmpty(this.gatewayGroups)) {
    //         Map<String, List<String>> map = new HashMap<>();
    //         Iterator<CBSGatewayGroup> it = this.gatewayGroups.iterator();
    //         while (it.hasNext()) {
    //             CBSGatewayGroup gatewayGroup = it.next();
    //             gatewayGroup.getClientIdToIMSTransmissionMap().forEach((clientId, transmissions) -> {
    //                 map.computeIfAbsent(clientId, k -> new ArrayList<>()).addAll(transmissions.stream().map(IMSTransmission::getIMSTransmissionMonitor).map(IMSTransmissionMonitor::getLocal).collect(Collectors.toList()));
    //             });
    //         }
    //         Map<String, List<String>> result = map.entrySet().stream()
    //                 .sorted(Map.Entry.comparingByValue(Comparator.comparing(Object::toString)))
    //                 .collect(Collectors.toMap(
    //                         Map.Entry::getKey,
    //                         Map.Entry::getValue,
    //                         (a, b) -> a,
    //                         LinkedHashMap::new
    //                 ));
    //         return new Gson().toJson(result);
    //     }
    //     return StringUtils.EMPTY;
    // }
}