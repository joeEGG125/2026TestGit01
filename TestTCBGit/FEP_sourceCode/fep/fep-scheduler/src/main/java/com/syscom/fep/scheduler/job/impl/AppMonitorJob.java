package com.syscom.fep.scheduler.job.impl;

import com.google.gson.Gson;
import com.syscom.fep.base.enums.FEPDBName;
import com.syscom.fep.common.http.HttpClientConfigureConstant;
import com.syscom.fep.common.monitor.MonitorDataCollector;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.frmcommon.os.OperationSystemDataCollector;
import com.syscom.fep.frmcommon.os.data.DBPoolData;
import com.syscom.fep.frmcommon.os.data.ProcessData;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.scheduler.job.SchedulerJob;
import com.syscom.fep.vo.monitor.ServiceOthers;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.text.DecimalFormat;
import java.util.*;

/**
 * 執行獲取 Service 監控資料
 *
 * @author Chen Yang
 */
public class AppMonitorJob extends SchedulerJob<AppMonitorJobConfig> {
    private final int PID = OperationSystemDataCollector.getProcessID(); // 獲取當前進程的PID
    private final Gson gson = new Gson();
    @Autowired
    @Qualifier(HttpClientConfigureConstant.BEAN_NAME_MONITOR)
    private HttpClient2 httpClient2;

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, AppMonitorJobConfig config) throws Exception {
        Map<String, Object> smsMap = new HashMap<>();
        smsMap.put("smsServiceip", config.getServiceHostIp());
        smsMap.put("smsServicename", config.getServiceName());
        smsMap.put("smsHostname", config.getServiceHostName());
        // smsMap.put("smsRam", getServiceMemoData(config));
        // smsMap.put("smsCpu", getServiceCpuData(config));
        ProcessData processData = OperationSystemDataCollector.getProcessData(PID, config.getCpuRefreshInterval());
        smsMap.put("smsCpu", processData == null ? 0 : (int) processData.getCpuPercent() * 100);
        smsMap.put("smsRam", processData == null ? 0 : processData.getResidentSetSize() / 1024);
        // 2025-07-31 Richard modified 因為SpringBoot Actuator無法獲取, 故改為使用JMX獲取線程數
        // smsMap.put("smsThreads", getServiceThreadData(config));
        smsMap.put("smsThreads", OperationSystemDataCollector.calculateJvmThreadCount());
        // 2025-07-31 Richard modified 因為SpringBoot Actuator無法獲取, 故改為使用JMX獲取啟動時間
        // String startTime = getServicesStartTime(config);
        String startTime = FormatUtil.dateTimeFormat(OperationSystemDataCollector.getJvmStartTime());
        if (StringUtils.isNotBlank(startTime)) {
            smsMap.put("smsStarttime", startTime);
            smsMap.put("smsServicestate", "1");
        } else {
            smsMap.put("smsServicestate", "0");
        }
        smsMap.put("smsPid", PID);
        smsMap.put("smsOthers", this.fetchSmsOthers());
        sendMonitorMessage(config, smsMap);
    }

    private String fetchSmsOthers() {
        ServiceOthers smsOthers = new ServiceOthers();
        smsOthers.setJvmData(OperationSystemDataCollector.getJvmData());
        List<DBPoolData> dbPools = new ArrayList<>();
        for (FEPDBName name : FEPDBName.values()) {
            DBPoolData data = MonitorDataCollector.fetchDBPoolData(name.name().toLowerCase() + "DataSource");
            if (data != null) {
                dbPools.add(data);
            }
        }
        if (!dbPools.isEmpty()) {
            DBPoolData dbPoolStatistics = new DBPoolData();
            dbPools.forEach(t -> {
                dbPoolStatistics.setActiveConnections(dbPoolStatistics.getActiveConnections() + t.getActiveConnections());
                dbPoolStatistics.setIdleConnections(dbPoolStatistics.getIdleConnections() + t.getIdleConnections());
                dbPoolStatistics.setThreadsAwaitingConnection(dbPoolStatistics.getThreadsAwaitingConnection() + t.getThreadsAwaitingConnection());
                dbPoolStatistics.setTotalConnections(dbPoolStatistics.getTotalConnections() + t.getTotalConnections());
                dbPoolStatistics.setMinimumIdle(dbPoolStatistics.getMinimumIdle() + t.getMinimumIdle());
                dbPoolStatistics.setMaximumPoolSize(dbPoolStatistics.getMaximumPoolSize() + t.getMaximumPoolSize());
            });
            smsOthers.setDbPools(dbPools);
            smsOthers.setDbPoolStatistics(dbPoolStatistics);
        }
        return gson.toJson(smsOthers);
    }

    @PreDestroy
    public void terminateJob() {
        AppMonitorJobConfig config = this.getJobConfig();
        Map<String, Object> smsMap = new HashMap<>();
        smsMap.put("smsServiceip", config.getServiceHostIp());
        smsMap.put("smsServicename", config.getServiceName());
        smsMap.put("smsHostname", config.getServiceHostName());
        smsMap.put("smsRam", "0");
        smsMap.put("smsCpu", "0");
        smsMap.put("smsThreads", "0");
        smsMap.put("smsServicestate", "0");
        smsMap.put("smsStoptime", FormatUtil.dateTimeFormat(Calendar.getInstance().getTime()));
        smsMap.put("smsOthers", StringUtils.EMPTY);
        sendMonitorMessage(config, smsMap);
    }

    /*
     * 獲取 Service StartTime
     */
    private String getServicesStartTime(AppMonitorJobConfig config) {
        String startTime = "";
        String promQL = "/actuator/metrics/process.start.time";
        String url = StringUtils.join(config.getServiceUrl(), promQL);
        try {
            String jsonStr = httpClient2.getForObject(url, String.class);
            if (StringUtils.isNotBlank(jsonStr)) {
                JSONObject rootObject = new JSONObject(jsonStr);
                JSONArray measurementsJsonArray = rootObject.getJSONArray("measurements");
                if (measurementsJsonArray != null && !measurementsJsonArray.isEmpty()) {
                    JSONObject resultObject = measurementsJsonArray.getJSONObject(0);
                    double value = resultObject.getDouble("value");
                    if (value > 0) {
                        startTime = FormatUtil.dateTimeFormat((long) (value * 1000));
                    }
                }
            } else {
                warn("getServicesStartTime with empty response!!!");
            }
        } catch (Exception e) {
            warn("getServicesStartTime failed with exception occur = [", e.getMessage(), "]");
        }
        return startTime;
    }

    /*
     * 獲取 Service Memo
     */
    private Integer getServiceMemoData(AppMonitorJobConfig config) {
        int memo = 0;
        String promQL = "/actuator/metrics/jvm.memory.used";
        String url = StringUtils.join(config.getServiceUrl(), promQL);
        try {
            // String jsonStr = httpClient.doGet(url, false);
            String jsonStr = httpClient2.getForObject(url, String.class);
            if (StringUtils.isNotBlank(jsonStr)) {
                JSONObject rootObject = new JSONObject(jsonStr);
                JSONArray measurementsJsonArray = rootObject.getJSONArray("measurements");
                if (measurementsJsonArray != null && !measurementsJsonArray.isEmpty()) {
                    JSONObject resultObject = measurementsJsonArray.getJSONObject(0);
                    int value = resultObject.getInt("value");
                    if (value >= 0) {
                        memo = value / 1024;
                    }
                }
            } else {
                warn("getServiceMemoData with empty response!!!");
            }
        } catch (Exception e) {
            warn("getServiceMemoData failed with exception occur = [", e.getMessage(), "]");
        }
        return memo;
    }

    /*
     * 獲取 Service Cpu
     */
    private Integer getServiceCpuData(AppMonitorJobConfig config) {
        int cpu = 0;
        String promQL = "/actuator/metrics/process.cpu.usage";
        String url = StringUtils.join(config.getServiceUrl(), promQL);
        try {
            String jsonStr = httpClient2.getForObject(url, String.class);
            if (StringUtils.isNotBlank(jsonStr)) {
                JSONObject rootObject = new JSONObject(jsonStr);
                JSONArray measurementsJsonArray = rootObject.getJSONArray("measurements");
                if (measurementsJsonArray != null && !measurementsJsonArray.isEmpty()) {
                    JSONObject resultObject = measurementsJsonArray.getJSONObject(0);
                    double value = resultObject.getDouble("value");
                    if (value >= 0) {
                        cpu = Integer.parseInt(new DecimalFormat("0").format(value * 100 * 100));
                    }
                }
            } else {
                warn("getServiceCpuData with empty response!!!");
            }
        } catch (Exception e) {
            warn("getServiceCpuData failed with exception occur = [", e.getMessage(), "]");
        }
        return cpu;
    }

    /*
     * 獲取 Service Thread
     */
    private Integer getServiceThreadData(AppMonitorJobConfig config) {
        int thread = 0;
        String promQL = "/actuator/metrics/jvm.threads.states";
        String url = StringUtils.join(config.getServiceUrl(), promQL);
        try {
            String jsonStr = httpClient2.getForObject(url, String.class);
            if (StringUtils.isNotBlank(jsonStr)) {
                JSONObject rootObject = new JSONObject(jsonStr);
                JSONArray measurementsJsonArray = rootObject.getJSONArray("measurements");
                if (measurementsJsonArray != null && !measurementsJsonArray.isEmpty()) {
                    JSONObject resultObject = measurementsJsonArray.getJSONObject(0);
                    int value = resultObject.getInt("value");
                    if (value >= 0) {
                        thread = value;
                    }
                }
            } else {
                warn("getServiceThreadData with empty response!!!");
            }
        } catch (Exception e) {
            warn("getServiceThreadData failed with exception occur = [", e.getMessage(), "]");
        }
        return thread;
    }

    /**
     * 發送資料到Monitor Server
     *
     * @param config
     * @param smsMap
     */
    private void sendMonitorMessage(AppMonitorJobConfig config, Map<String, Object> smsMap) {
        boolean postFailed = false;
        String primaryUrl = config.getMonitorPrimaryUrl();
        if (StringUtils.isNotBlank(primaryUrl)) {
            try {
                httpClient2.postForEntity(primaryUrl, null, smsMap, String.class);
            } catch (Exception e) {
                postFailed = true;
                warn("sendMonitorMessage to primaryUrl = [", primaryUrl, "], failed with exception occur = [", e.getMessage(), "]");
                sendEMS(e, StringUtils.join("sendMonitorMessage to primaryUrl = [", primaryUrl, "] failed"));
            }
        } else {
            warn("cannot sendMonitorMessage, primaryUrl is blank!!!");
        }
        if (postFailed) {
            postFailed = false;
            String secondaryUrl = config.getMonitorSecondaryUrl();
            if (StringUtils.isNotBlank(secondaryUrl)) {
                try {
                    httpClient2.postForEntity(secondaryUrl, null, smsMap, String.class);
                } catch (Exception e) {
                    postFailed = true;
                    warn("sendMonitorMessage to secondaryUrl = [", secondaryUrl, "], failed with exception occur = [", e.getMessage(), "]");
                    sendEMS(e, StringUtils.join("sendMonitorMessage to secondaryUrl = [", secondaryUrl, "] failed"));
                }
            } else {
                warn("cannot sendMonitorMessage, secondaryUrl is blank!!!");
            }
        }
    }

    private void warn(Object... messages) {
        if (this.getJobConfig().isRecordHttpLog()) {
            ScheduleLogger.warn(messages);
        }
    }
}
