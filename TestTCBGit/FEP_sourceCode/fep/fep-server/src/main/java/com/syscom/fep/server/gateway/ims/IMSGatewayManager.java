package com.syscom.fep.server.gateway.ims;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ibm.ims.connect.ApiLoggingConfiguration;
import com.ibm.ims.connect.ApiProperties;
import com.ibm.ims.connect.ImsConnectApiException;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.server.gateway.ims.processor.IMSGatewayProcessorGroup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@StackTracePointCut(caller = SvrConst.SVR_IMS_GATEWAY)
public class IMSGatewayManager extends FEPBase {
    private static final ApiLoggingConfiguration apiLoggingConfig = new ApiLoggingConfiguration();
    private ExecutorService executor;
    @Autowired
    private IMSGatewayConfiguration configuration;
    private IMSGateway primary, secondary;
    private final Object operationLock = new Object();

    @PostConstruct
    public void postConstruct() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        // 這裡依據配置檔中設定primary receiver的clientId決定executorPoolSize的值
        int clientIdsCount = StringUtil.split(configuration.getPrimary().getReceiver().getClientId(), ',').size();
        if (configuration.getExecutorCorePoolSize() < clientIdsCount) {
            LogHelperFactory.getGeneralLogger().warn("The value of 'executorPoolSize' will be overwrite, old:", configuration.getExecutorCorePoolSize(), ", new:", clientIdsCount);
            configuration.setExecutorCorePoolSize(clientIdsCount);
            configuration.setExecutorMaximumPoolSize(clientIdsCount);
        }
        // 這裡依據配置檔中設定primary alternative receiver和secondary receiver/alternative receiver的clientId決定executorMaximumPoolSize的值
        int clientIdsCount2 = StringUtil.split(configuration.getSecondary().getReceiver().getClientId(), ',').size();
        clientIdsCount2 += StringUtil.split(configuration.getPrimary().getAlternativeReceiver().getClientId(), ',').size();
        clientIdsCount2 += StringUtil.split(configuration.getSecondary().getAlternativeReceiver().getClientId(), ',').size();
        if (configuration.getExecutorMaximumPoolSize() < configuration.getExecutorCorePoolSize() + clientIdsCount2) {
            LogHelperFactory.getGeneralLogger().warn("The value of 'executorMaximumPoolSize' will be overwrite, old:", configuration.getExecutorMaximumPoolSize(), ", new:", (configuration.getExecutorCorePoolSize() + clientIdsCount2));
            configuration.setExecutorMaximumPoolSize(configuration.getExecutorCorePoolSize() + clientIdsCount2);
        }
        this.executor = ThreadPoolFactory.newThreadPool(
                configuration.getExecutorCorePoolSize(),
                configuration.getExecutorMaximumPoolSize(),
                configuration.getExecutorKeepAliveTime(),
                TimeUnit.MILLISECONDS,
                configuration.getExecutorQueueCapacity(),
                new SimpleThreadFactory(StringUtils.join(SvrConst.SVR_IMS_GATEWAY, "Executor")),
                new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化IMS API的Logging設定
        this.initApiLoggingConfiguration();
        // 服務啟動時, 先送一個stopChannel secondary給另一台IMSGW
        // 有確定成功後, 再回應給發送端, 隔3秒, 發送端再啟動primary channel
        this.stopSecondaryToOtherIMSGatewayAndStartPrimary();
    }

    /**
     * 初始化IMS API的Logging設定
     */
    private void initApiLoggingConfiguration() {
        try {
            String logPath = configuration.getImsApiLogPath();
            String apName = FEPConfig.getInstance().getApplicationName();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String apiLog = Paths.get(logPath, LocalDate.now().format(formatter), apName, configuration.getImsApiLogFileName()).toString();
            File directory = new File(apiLog).getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
            apiLoggingConfig.configureApiLogging(apiLog, ApiProperties.TRACE_LEVEL_INTERNAL);
        } catch (ImsConnectApiException e) {
            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
        }
    }

    /**
     * 服務啟動時, 先送一個stopChannel secondary給另一台IMSGW
     * 有確定成功後, 再回應給發送端, 隔3秒, 發送端再啟動primary channel
     */
    private void stopSecondaryToOtherIMSGatewayAndStartPrimary() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        int times = 0;
        while (times++ < this.configuration.getStopSecondaryToOtherRetryCount()) {
            if (stopSecondaryToOtherIMSGateway(times)) {
                try {
                    Thread.sleep(this.configuration.getSleepForStartPrimary());
                } catch (InterruptedException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherIMSGatewayAndStartPrimary"));
                this.logContext.setRemark(StringUtils.join("Start Primary Channel after succeed to stop other IMSGateway secondary channel"));
                this.logMessage(this.logContext);
                // 程式啟動時, 以primary的sender及receiver去連接IMS
                this.runGateway(IMSGatewayMode.primary, configuration);
                return;
            }
            try {
                Thread.sleep(this.configuration.getRetryStopSecondaryToOtherSleep());
            } catch (InterruptedException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
        }
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherIMSGatewayAndStartPrimary"));
        this.logContext.setRemark(StringUtils.join("Do not Start Primary Channel cause failed to stop other IMSGateway secondary channel or which is still connected to IMS Server by one port at least"));
        logMessage(Level.WARN, this.logContext);
    }

    /**
     * 服務啟動時, 先送一個stopChannel secondary給另一台IMSGW
     *
     * @param times
     * @return
     */
    private boolean stopSecondaryToOtherIMSGateway(int times) {
        HttpClient httpClient = new HttpClient(configuration.isRecordHttpLog());
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherIMSGateway"));
        this.logContext.setMessageFlowType(MessageFlow.Request);
        this.logContext.setRemark(StringUtils.join("Start to do http post = [", configuration.getHttpChannel(), "] at times = [", times, "]"));
        this.logMessage(this.logContext);
        String response = null;
        try {
            Map<String, String> args = new HashMap<>();
            args.put("mode", IMSGatewayMode.secondary.name());
            args.put("action", IMSGatewayCmdAction.stop.name());
            args.put("respType", IMSGatewayRespType.JSON.name());
            response = httpClient.doPost(configuration.getHttpChannel(), MediaType.APPLICATION_FORM_URLENCODED, configuration.getHttpTimeout(), args, true);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherIMSGateway"));
            this.logContext.setMessageFlowType(MessageFlow.Response);
            this.logContext.setRemark(StringUtils.join("Get response = [", response, "], url = [", configuration.getHttpChannel(), "] at times = [", times, "]"));
            this.logMessage(this.logContext);
        } catch (Throwable e) {
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherIMSGateway"));
            this.logContext.setProgramException(e);
            this.logContext.setRemark(StringUtils.join("Do http post = [", configuration.getHttpChannel(), "] at times = [", times, "] with exception occur, ", e.getMessage()));
            sendEMS(this.logContext);
            return true; // 如果有異常, 則回傳true, 讓程式繼續執行
        }
        if (StringUtils.isNotBlank(response)) {
            try {
                IMSGatewayResp resp = new Gson().fromJson(response, IMSGatewayResp.class);
                // 停止遠程成功, 並且沒有存在任何連線
                return resp.isResult() && resp.getIsConnected() != null && !resp.getIsConnected();
            } catch (JsonSyntaxException e) {
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".stopSecondaryToOtherIMSGateway"));
                this.logContext.setProgramException(e);
                this.logContext.setRemark(StringUtils.join("Parse Response failed at times = [", times, "], ", e.getMessage()));
                sendEMS(this.logContext);
            }
        }
        return false;
    }

    @PreDestroy
    public void terminate() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Enter Terminate"));
        this.logMessage(this.logContext);
        ThreadPoolFactory.shutdown(this.executor, SvrConst.SVR_IMS_GATEWAY, "Executor");
        this.stopGateway(IMSGatewayMode.primary, null);
        this.stopGateway(IMSGatewayMode.secondary, null);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Exit Terminate"));
        this.logMessage(this.logContext);
    }

    /**
     * 啟動IMS Gateway
     *
     * @param mode
     * @param configuration
     */
    public synchronized boolean runGateway(IMSGatewayMode mode, IMSGatewayConfiguration configuration) {
        synchronized (operationLock) {
            // 已經有運行則不要重複運行
            if (mode == IMSGatewayMode.primary) {
                if (primary == null) {
                    primary = new IMSGateway(configuration, configuration.getPrimary(), executor, true);
                    return true;
                } else {
                    primary.startToRun();
                    return true;
                    // LogHelperFactory.getGeneralLogger().warn("IMSGateway Primary was ", primary.getState().name(), ", cannot run again");
                }
            } else {
                if (secondary == null) {
                    secondary = new IMSGateway(configuration, configuration.getSecondary(), executor, true);
                    return true;
                } else {
                    secondary.startToRun();
                    return true;
                    // LogHelperFactory.getGeneralLogger().warn("IMSGateway Secondary was ", secondary.getState().name(), ", cannot run again");
                }
            }
            // return false;
        }
    }

    /**
     * 停止IMS Gateway
     *
     * @param mode
     */
    public synchronized boolean stopGateway(IMSGatewayMode mode, RefBoolean isConnected) {
        synchronized (operationLock) {
            if (mode == IMSGatewayMode.primary) {
                if (primary != null) {
                    primary.terminate();
                    if (isConnected != null)
                        isConnected.set(primary.isConnected());
                    primary = null;
                    return true;
                }
                LogHelperFactory.getGeneralLogger().warn("IMSGateway Primary was stopped, cannot stop again");
            } else {
                if (secondary != null) {
                    secondary.terminate();
                    if (isConnected != null)
                        isConnected.set(secondary.isConnected());
                    secondary = null;
                    return true;
                }
                LogHelperFactory.getGeneralLogger().warn("IMSGateway Secondary was stopped, cannot stop again");
            }
            return false;
        }
    }

    /**
     * 檢查狀態
     *
     * @return
     */
    public String checkStatus() {
        StringBuilder sb = new StringBuilder();
        if (primary != null)
            sb.append(primary.checkStatus());
        if (secondary != null)
            sb.append(secondary.checkStatus());
        String rtn = sb.toString();
        if (StringUtils.isBlank(rtn)) {
            rtn = StringUtils.join(FEPConfig.getInstance().getHostName(), " : all Channel was stopped");
        }
        return rtn;
    }

    public IMSGateway getPrimary() {
        return primary;
    }

    public IMSGateway getSecondary() {
        return secondary;
    }

    /**
     * 改變腳位狀態, 啟動或停止
     *
     * @param logData
     * @param clientId
     * @param enable
     * @return
     */
    public boolean changeLineStatus(LogData logData, String clientId, boolean enable) {
        synchronized (operationLock) {
            // 先處理primary, 如果primary返回false, 則再處理secondary
            if (this.primary == null)
                // 如果primary為null, 說明還沒有初始化, 則這裡要new一下, 但是startToRun要塞入false
                this.primary = new IMSGateway(configuration, configuration.getPrimary(), executor, false);
            boolean result = this.changeLineStatus(logData, clientId, enable, this.primary);
            if (!result) {
                if (this.secondary == null)
                    // 如果secondary為null, 說明還沒有初始化, 則這裡要new一下, 但是startToRun要塞入false
                    this.secondary = new IMSGateway(configuration, configuration.getSecondary(), executor, false);
                result = this.changeLineStatus(logData, clientId, enable, this.secondary);
            }
            return result;
        }
    }

    private boolean changeLineStatus(LogData logData, String clientId, boolean enable, IMSGateway gateway) {
        boolean result = false;
        if (gateway != null) {
            List<IMSGatewayProcessorGroup> imsGatewayProcessorGroups = gateway.getProcessorGroups();
            if (CollectionUtils.isNotEmpty(imsGatewayProcessorGroups)) {
                Iterator<IMSGatewayProcessorGroup> it = imsGatewayProcessorGroups.iterator();
                while (it.hasNext() && !result) {
                    IMSGatewayProcessorGroup processorGroup = it.next();
                    result = processorGroup.changeLineStatus(logData, clientId, enable);
                }
            }
        }
        return result;
    }
}
