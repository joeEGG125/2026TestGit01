package com.syscom.fep.gateway.netty.fisc;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.ToFEPAPMode;
import com.syscom.fep.gateway.netty.NettyTransmissionClientMonitor;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionConnStateListener;
import com.syscom.fep.gateway.netty.NettyTransmissionNotification;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiver;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverConfiguration;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSender;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderConfiguration;
import com.syscom.fep.gateway.netty.fisc.server.FISCGatewayServerConfiguration;
import com.syscom.fep.invoker.netty.impl.ToFEPFISCNettyClientConfiguration;
import io.netty.channel.Channel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@ConfigurationProperties(prefix = "spring.fep.gateway.transmission.fisc")
// @RefreshScope
public class FISCGatewayManager extends FEPBase implements NettyTransmissionConnStateListener {
    /**
     * 訊息進到response queue若60秒未被收走, 自動drop掉
     */
    private long responseQueueExpiration = 60000L;
    /**
     * 交送Request或confirm交易至FEPAP方式, static固定只送第1台, dynamic代表2台輪流送
     */
    private ToFEPAPMode tofepapMode = ToFEPAPMode.STATIC;
    /**
     * 每次失敗後間隔多久後連另1台,單位為秒
     */
    private int tofepapRetryInterval = 3;
    /**
     * 最大重試次數, 若已達最大次數則不再retry, 直接sendEMS即可
     */
    private int tofepapRetryCount = 10;
    /**
     * 啟動一個onetime的timer, interval如下參數(單位為秒), 當時間到時, 再重設tofepapmode變數為config預設值
     */
    private int tofepapResetToFepApModeTimer = 300;
    @NestedConfigurationProperty
    private final List<ToFEPFISCNettyClientConfiguration> tofepap = new ArrayList<>();
    private RoundRobin<ToFEPFISCNettyClientConfiguration> tofepapRoundRobin;
    @NestedConfigurationProperty
    private final FISCGatewayConfiguration primary = new FISCGatewayConfiguration();
    @NestedConfigurationProperty
    private final FISCGatewayConfiguration secondary = new FISCGatewayConfiguration();
    private final List<FISCGatewayGroup> primaryGatewayGroupList = new ArrayList<>();
    private final List<FISCGatewayGroup> secondaryGatewayGroupList = new ArrayList<>();
    private final Object lockConnStateChanged = new Object();
    private boolean recordHttpLog = false;
    private String httpOperate;
    private int httpTimeout = 10000;
    private long sleepForStartPrimary = 3000L;
    private int stopSecondaryToOtherRetryCount = 3;
    private long stopSecondaryToOtherRetrySleep = 1000;
    @NestedConfigurationProperty
    private final FISCGatewayGroupConfiguration groupCfg = new FISCGatewayGroupConfiguration();
    @Autowired
    private NettyTransmissionNotification notification;
    private AtomicReference<ToFEPFISCNettyClientConfiguration> tofepapCfg = new AtomicReference<>();

    public FISCGatewayConfiguration getPrimary() {
        return primary;
    }

    public FISCGatewayConfiguration getSecondary() {
        return secondary;
    }

    public long getResponseQueueExpiration() {
        return responseQueueExpiration;
    }

    public void setResponseQueueExpiration(long responseQueueExpiration) {
        this.responseQueueExpiration = responseQueueExpiration;
    }

    public ToFEPAPMode getTofepapMode() {
        return tofepapMode;
    }

    public void setTofepapMode(ToFEPAPMode tofepapMode) {
        this.tofepapMode = tofepapMode;
    }

    public int getTofepapRetryInterval() {
        return tofepapRetryInterval;
    }

    public void setTofepapRetryInterval(int tofepapRetryInterval) {
        this.tofepapRetryInterval = tofepapRetryInterval;
    }

    public int getTofepapRetryCount() {
        return tofepapRetryCount;
    }

    public void setTofepapRetryCount(int tofepapRetryCount) {
        this.tofepapRetryCount = tofepapRetryCount;
    }

    public int getTofepapResetToFepApModeTimer() {
        return tofepapResetToFepApModeTimer;
    }

    public void setTofepapResetToFepApModeTimer(int tofepapResetToFepApModeTimer) {
        this.tofepapResetToFepApModeTimer = tofepapResetToFepApModeTimer;
    }

    public List<ToFEPFISCNettyClientConfiguration> getTofepap() {
        return tofepap;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }

    public String getHttpOperate() {
        return httpOperate;
    }

    public void setHttpOperate(String httpOperate) {
        this.httpOperate = httpOperate;
    }

    public int getHttpTimeout() {
        return httpTimeout;
    }

    public void setHttpTimeout(int httpTimeout) {
        this.httpTimeout = httpTimeout;
    }

    public long getSleepForStartPrimary() {
        return sleepForStartPrimary;
    }

    public void setSleepForStartPrimary(long sleepForStartPrimary) {
        this.sleepForStartPrimary = sleepForStartPrimary;
    }

    public long getStopSecondaryToOtherRetrySleep() {
        return stopSecondaryToOtherRetrySleep;
    }

    public void setStopSecondaryToOtherRetrySleep(long stopSecondaryToOtherRetrySleep) {
        this.stopSecondaryToOtherRetrySleep = stopSecondaryToOtherRetrySleep;
    }

    public int getStopSecondaryToOtherRetryCount() {
        return stopSecondaryToOtherRetryCount;
    }

    public void setStopSecondaryToOtherRetryCount(int stopSecondaryToOtherRetryCount) {
        this.stopSecondaryToOtherRetryCount = stopSecondaryToOtherRetryCount;
    }

    public FISCGatewayGroupConfiguration getGroupCfg() {
        return groupCfg;
    }

    @PostConstruct
    public void runGateway() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "FISC Gateway Manager Configuration",
                "tofepap", "primary", "secondary", "tofepapRoundRobin", "lockConnStateChanged", "notification", "tofepapCfg"));
        tofepapRoundRobin = new RoundRobin<>(this.tofepap);
        // 程式啟動後先傳送stopChannel secondary給另一台主機, 等待收到回應後,再繼續下面流程,若無法連接至另一台主機, 則一樣繼續以下流程
        // 啟動時預設皆以Primary群組腳位連接財金
        this.stopSecondaryToOtherFISCGatewayAndStartPrimary(true);
    }

    /**
     * 以Primary群組腳位連接財金
     *
     * @param postConstruct
     * @return
     */
    public boolean runPrimaryGateway(boolean postConstruct) {
        return this.runGateway(FISCGatewayMode.primary, primary, primaryGatewayGroupList, postConstruct);
    }

    /**
     * 以Secondary群組腳位連接財金
     *
     * @return
     */
    public boolean runSecondaryGateway(boolean postConstruct) {
        return this.runGateway(FISCGatewayMode.secondary, secondary, secondaryGatewayGroupList, postConstruct);
    }

    private boolean runGateway(FISCGatewayMode mode, FISCGatewayConfiguration configuration, List<FISCGatewayGroup> gatewayGroupList, boolean postConstruct) {
        synchronized (configuration.getConnState()) {
            if (configuration.getConnState().get() == NettyTransmissionConnState.CLIENT_RUNNING) {
                LogHelperFactory.getGeneralLogger().warn(mode.name(), " was running...");
                return false;
            }
            configuration.setConnState(NettyTransmissionConnState.CLIENT_READY_TO_RUN);
            // 這裡要判斷一下, 避免重複增加到List
            if (gatewayGroupList.isEmpty()) {
                this.setConfigurationPropertiesPrefix(mode, configuration);
                for (int i = 0; i < configuration.getSender().size(); i++) {
                    FISCGatewayGroup gatewayGroup = new FISCGatewayGroup(
                            mode,
                            groupCfg,
                            configuration.getSender().get(i),
                            configuration.getReceiver().get(i),
                            configuration.getFepap().get(i)
                    );
                    gatewayGroupList.add(gatewayGroup);
                    // 加入監聽
                    notification.addConnStateListener(configuration.getSender().get(i).getNettyTransmissionNotificationKey(), this);
                    notification.addConnStateListener(configuration.getReceiver().get(i).getNettyTransmissionNotificationKey(), this);
                    // postConstruct下會自動啟動Gateway程式
                    gatewayGroup.run(postConstruct);
                }
            } else {
                for (FISCGatewayGroup gatewayGroup : gatewayGroupList) {
                    // postConstruct下會自動啟動Gateway程式
                    gatewayGroup.run(postConstruct);
                }
            }
            configuration.setConnState(NettyTransmissionConnState.CLIENT_RUNNING);
            return true;
        }
    }

    private void setConfigurationPropertiesPrefix(FISCGatewayMode mode, FISCGatewayConfiguration configuration) {
        List<FISCGatewayClientSenderConfiguration> senderConfigurationList = configuration.getSender();
        for (int i = 0; i < senderConfigurationList.size(); i++) {
            senderConfigurationList.get(i).setConfigurationPropertiesPrefix(StringUtils.join("spring.fep.gateway.transmission.fisc.", mode.name(), ".sender[", i, "]"));
        }
        List<FISCGatewayClientReceiverConfiguration> receiverConfigurationList = configuration.getReceiver();
        for (int i = 0; i < receiverConfigurationList.size(); i++) {
            receiverConfigurationList.get(i).setConfigurationPropertiesPrefix(StringUtils.join("spring.fep.gateway.transmission.fisc.", mode.name(), ".receiver[", i, "]"));
        }
        List<FISCGatewayServerConfiguration> serverConfigurationList = configuration.getFepap();
        for (int i = 0; i < serverConfigurationList.size(); i++) {
            serverConfigurationList.get(i).setConfigurationPropertiesPrefix(StringUtils.join("spring.fep.gateway.transmission.fisc.", mode.name(), ".fepap[", i, "]"));
        }
    }

    @PreDestroy
    public void terminate() {
        // 關閉程序時, 要stop所有的socket連線
        this.stopPrimaryGateway();
        this.stopSecondaryGateway();
    }

    /**
     * 斷開Primary群組腳位與財金的連線
     *
     * @return
     */
    public boolean stopPrimaryGateway() {
        return this.stopGateway(FISCGatewayMode.primary, primary, primaryGatewayGroupList);
    }

    /**
     * 斷開Secondary群組腳位與財金的連線
     *
     * @return
     */
    public boolean stopSecondaryGateway() {
        return this.stopGateway(FISCGatewayMode.secondary, secondary, secondaryGatewayGroupList);
    }

    public boolean stopGateway(FISCGatewayMode mode, FISCGatewayConfiguration configuration, List<FISCGatewayGroup> gatewayGroupList) {
        synchronized (configuration.getConnState()) {
            if (configuration.getConnState().get() == NettyTransmissionConnState.CLIENT_SHUT_DOWN) {
                LogHelperFactory.getGeneralLogger().warn(mode.name(), " was stopped...");
                return false;
            }
            configuration.setConnState(NettyTransmissionConnState.CLIENT_SHUTTING_DOWN);
            for (FISCGatewayGroup gatewayGroup : gatewayGroupList) {
                // 移除監聽
                notification.removeConnStateListener(gatewayGroup.getSender().getConfiguration().getNettyTransmissionNotificationKey(), this);
                notification.removeConnStateListener(gatewayGroup.getReceiver().getConfiguration().getNettyTransmissionNotificationKey(), this);
                // 停止
                gatewayGroup.stop();
            }
            configuration.setConnState(NettyTransmissionConnState.CLIENT_SHUT_DOWN);
            return true;
        }
    }

    public List<FISCGatewayGroup> getPrimaryGatewayGroupList() {
        return primaryGatewayGroupList;
    }

    public List<FISCGatewayGroup> getSecondaryGatewayGroupList() {
        return secondaryGatewayGroupList;
    }

    /**
     * checkStatus: 顯示目前各腳位的連線狀態, 如下:
     * fepap1 : primary FISC(B889A01I) 172.X.X.X:5001 Connected
     * fepap1 : primary FISC(B889A01O) 172.X.X.X:5002 Connected
     * 若有接手secondary線路(因heartbeat自動接手或曾下過start Secondary指令), 則顯示以下訊息, 若無 則不顯示
     * fepap1 : secondary FISC(B889A02I) 172.X.X.X:5003 Connected
     * fepap1 : secondary FISC(B889A02O) 172.X.X.X:5004 Connected
     *
     * @return
     */
    public String checkStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.checkStatus(FISCGatewayMode.primary, primary, primaryGatewayGroupList));
        sb.append(this.checkStatus(FISCGatewayMode.secondary, secondary, secondaryGatewayGroupList));
        String rtn = sb.toString();
        if (StringUtils.isBlank(rtn)) {
            rtn = StringUtils.join(FEPConfig.getInstance().getHostName(), " : all Channel was stopped");
        }
        return rtn;
    }

    private String checkStatus(FISCGatewayMode mode, FISCGatewayConfiguration configuration, List<FISCGatewayGroup> gatewayGroupList) {
        synchronized (configuration.getConnState()) {
            if (configuration.getConnState().get() != NettyTransmissionConnState.CLIENT_RUNNING) {
                return StringUtils.EMPTY;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(gatewayGroupList)) {
            for (FISCGatewayGroup group : gatewayGroupList) {
                FISCGatewayClientSender sender = group.getSender();
                if (sender != null) {
                    NettyTransmissionClientMonitor<FISCGatewayClientSenderConfiguration> senderMonitorData = sender.getTransmissionClientMonitor();
                    sb.append(FEPConfig.getInstance().getHostName()).append(" : ")
                            .append(mode.name()).append(StringUtils.SPACE)
                            .append("FISC(").append(sender.getConfiguration().getClientId()).append(")").append(StringUtils.SPACE)
                            .append(senderMonitorData.getRemote()).append(StringUtils.SPACE)
                            .append(NettyTransmissionConnState.isClientConnected(senderMonitorData.getConnState()) ? "Connected" : "Disconnected")
                            .append("\r\n");
                }
                FISCGatewayClientReceiver receiver = group.getReceiver();
                if (receiver != null) {
                    NettyTransmissionClientMonitor<FISCGatewayClientReceiverConfiguration> receiverMonitorData = receiver.getTransmissionClientMonitor();
                    sb.append(FEPConfig.getInstance().getHostName()).append(" : ")
                            .append(mode.name()).append(StringUtils.SPACE)
                            .append("FISC(").append(receiver.getConfiguration().getClientId()).append(")").append(StringUtils.SPACE)
                            .append(receiverMonitorData.getRemote()).append(StringUtils.SPACE)
                            .append(NettyTransmissionConnState.isClientConnected(receiverMonitorData.getConnState()) ? "Connected" : "Disconnected")
                            .append("\r\n");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        synchronized (lockConnStateChanged) {
            if (state == NettyTransmissionConnState.CLIENT_DISCONNECTED) {
                // Priamry Gateway
                this.detectSenderAndReceiverConnectiveAndStopServer(FISCGatewayMode.primary, this.primary, this.primaryGatewayGroupList);
                // Secondeary Gateway
                this.detectSenderAndReceiverConnectiveAndStopServer(FISCGatewayMode.secondary, this.secondary, this.secondaryGatewayGroupList);
            }
        }
    }

    /**
     * 判斷所有腳位的Sender和Receiver是否都斷線, 如果都斷線則停止Server
     *
     * @param mode
     * @param configuration
     * @param gatewayGroupList
     */
    private void detectSenderAndReceiverConnectiveAndStopServer(FISCGatewayMode mode, FISCGatewayConfiguration configuration, List<FISCGatewayGroup> gatewayGroupList) {
        if (configuration.getConnState().get() == NettyTransmissionConnState.CLIENT_RUNNING) {
            // 當所有腳位的Sender與Receiver都已經斷線時, 才會停止所有腳位的Server
            boolean stopServer = true;
            for (FISCGatewayGroup gatewayGroup : gatewayGroupList) {
                if (gatewayGroup.getSender() != null && !NettyTransmissionConnState.isClientConnected(gatewayGroup.getSender().getTransmissionClientMonitor().getConnState())
                        && gatewayGroup.getReceiver() != null && !NettyTransmissionConnState.isClientConnected(gatewayGroup.getReceiver().getTransmissionClientMonitor().getConnState())) {
                    LogHelperFactory.getGeneralLogger().warn("[", mode.name(), "]Sender(", gatewayGroup.getSender().getTransmissionClientMonitor().getRemote(), ") and Receiver(", gatewayGroup.getReceiver().getTransmissionClientMonitor().getRemote(), ") has been disconnected from FISC");
                } else {
                    stopServer = false;
                    break;
                }
            }
            if (stopServer) {
                LogHelperFactory.getGeneralLogger().warn("[", mode.name(), "]All Sender and Receiver has been disconnected from FISC, start to stop Socket Server connection for FISCAdapter");
                for (FISCGatewayGroup gatewayGroup : gatewayGroupList) {
                    gatewayGroup.stopServer();
                }
            }
        }
    }

    /**
     * 程式啟動後先傳送stopChannel secondary給另一台主機, 等待收到回應後,再繼續下面流程,若無法連接至另一台主機, 則一樣繼續以下流程
     */
    private void stopSecondaryToOtherFISCGatewayAndStartPrimary(boolean postConstruct) {
        LogMDC.put(Const.MDC_PROFILE, Gateway.FISCGW.name());
        int times = 0;
        while (times++ < this.stopSecondaryToOtherRetryCount) {
            if (stopSecondaryToOtherFISCGateway(times)) {
                try {
                    Thread.sleep(this.sleepForStartPrimary);
                } catch (InterruptedException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherFISCGatewayAndStartPrimary"));
                this.logContext.setRemark(StringUtils.join("Start Primary Channel after succeed to stop other FISCGateway secondary channel"));
                this.logMessage(this.logContext);
                // 啟動時預設皆以Primary群組腳位連接財金
                this.runPrimaryGateway(postConstruct);
                return;
            }
            try {
                Thread.sleep(this.stopSecondaryToOtherRetrySleep);
            } catch (InterruptedException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
        }
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherFISCGatewayAndStartPrimary"));
        this.logContext.setRemark(StringUtils.join("Do not Start Primary Channel cause failed to stop other FISCGateway secondary channel"));
        logMessage(Level.WARN, this.logContext);
    }

    /**
     * 停止遠程FISCGW的secondary
     *
     * @return
     */
    private boolean stopSecondaryToOtherFISCGateway(int times) {
        LogMDC.put(Const.MDC_PROFILE, Gateway.FISCGW.name());
        HttpClient httpClient = new HttpClient(this.recordHttpLog);
        LogData logData = new LogData();
        logData.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherFISCGateway"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to do http post = [", this.httpOperate, "]"));
        this.logMessage(logData);
        String response = null;
        try {
            Map<String, String> args = new HashMap<>();
            args.put("mode", FISCGatewayMode.secondary.name());
            args.put("action", FISCGatewayCmdAction.stop.name());
            args.put("respType", FISCGatewayRespType.JSON.name());
            response = httpClient.doPost(this.httpOperate, MediaType.APPLICATION_FORM_URLENCODED, this.httpTimeout, args, true);
            logData.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherFISCGateway"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Get response = [", response, "], url = [", this.httpOperate, "]"));
            this.logMessage(logData);
        } catch (Throwable e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherFISCGateway"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Do http post = [", this.httpOperate, "] with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return true; // 如果有異常, 則回傳true, 讓程式繼續執行
        }
        if (StringUtils.isNotBlank(response)) {
            try {
                FISCGatewayResp[] resps = new Gson().fromJson(response, FISCGatewayResp[].class);
                // 停止遠程成功
                return ArrayUtils.isNotEmpty(resps) && resps[0].isResult(); // 應該只會返回一筆
            } catch (JsonSyntaxException e) {
                logData.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherFISCGateway"));
                logData.setProgramException(e);
                logData.setRemark(StringUtils.join("Parse Response failed, ", e.getMessage()));
                sendEMS(logData);
            }
        }
        return false;
    }

    /**
     * 如果host是IP字串, 則從this.tofepap中找出對應的數據
     * <p>
     * 如果host是int類型, 則取this.tofepap.get(host)
     *
     * @param host
     * @return
     */
    public String setToFEPAPHost(String host) {
        LogData logData = new LogData();
        logData.setProgramName(StringUtils.join(ProgramName, ".setToFEPAPHost"));
        logData.setRemark(StringUtils.join("Change FEPAP according to host:", host));
        logMessage(logData);
        if (StringUtils.isBlank(host)) {
            this.tofepapCfg.set(null);
        } else {
            if (StringUtils.isNumeric(host)) {
                int index = Integer.parseInt(host);
                if (index >= 0 && index < this.tofepap.size()) {
                    this.tofepapCfg.set(this.tofepap.get(index));
                } else {
                    this.tofepapCfg.set(null);
                }
            } else {
                this.tofepapCfg.set(this.tofepap.stream().filter(t -> t.getHost().equals(host)).findFirst().orElse(null));
            }
        }
        logData.setProgramName(StringUtils.join(ProgramName, ".setToFEPAPHost"));
        logData.setRemark(StringUtils.join("Set ToFEPFISC ", this.tofepapCfg.get() == null ? "[null]" : StringUtils.join("[host:", this.tofepapCfg.get().getHost(), ",port:", this.tofepapCfg.get().getPort(), "]")));
        logMessage(logData);
        return this.getToFEPAPHost();
    }

    public String getToFEPAPHost() {
        ToFEPFISCNettyClientConfiguration configuration = this.tofepapCfg.get();
        if (configuration != null) {
            return StringUtils.join("All Message from FISC will forward to FISC Service [", configuration.getHost(), ":", configuration.getPort(), "]");
        }
        return "All Message from FISC will forward to FISC Service by RoundRobin";
    }

    public ToFEPFISCNettyClientConfiguration getToFepApCfg() {
        // 優先取出從透過command指定的FEPAP
        ToFEPFISCNettyClientConfiguration configuration = this.tofepapCfg.get();
        // 如果沒有透過command指定的FEPAP, 則根據配置檔設定來取, static固定只送第1台, dynamic代表2台輪流送
        return configuration != null ? configuration : (this.getTofepapMode() == ToFEPAPMode.STATIC ? this.tofepapRoundRobin.get(0) : this.tofepapRoundRobin.select());
    }

    public boolean useToFEPAPHost() {
        return this.tofepapCfg.get() != null;
    }

    public RoundRobin<ToFEPFISCNettyClientConfiguration> getTofepapRoundRobin() {
        return tofepapRoundRobin;
    }
}