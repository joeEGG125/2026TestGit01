package com.syscom.fep.scheduler.job.impl;

import com.google.gson.Gson;
import com.syscom.fep.common.http.HttpClientConfigureConstant;
import com.syscom.fep.common.monitor.MonitorData;
import com.syscom.fep.common.monitor.MonitorDataCollector;
import com.syscom.fep.common.monitor.MonitorDataConstant;
import com.syscom.fep.common.monitor.MonitorDataDisk;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.frmcommon.os.OperationSystemDataCollector;
import com.syscom.fep.scheduler.job.SchedulerJob;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;

import jakarta.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SystemMonitorJob extends SchedulerJob<SystemMonitorJobConfig> implements MonitorDataConstant {
    private final int PID = OperationSystemDataCollector.getProcessID(); // 獲取當前進程的PID
    @Autowired
    @Qualifier(HttpClientConfigureConstant.BEAN_NAME_MONITOR)
    private HttpClient2 httpClient2;
    private String latestSystemHardDisk; // 上一次獲取的磁盤信息
    private long latestFetchSystemHardDisk; // 上一次獲取磁盤信息的時間

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, SystemMonitorJobConfig config) throws Exception {
        MonitorData system = createSystemMonitorData(config, PID);
        system.setSmsServicestate("1");
        system.setSmsCpu(this.fetchSystemCpuUsage(config));
        system.setSmsRam(this.fetchSystemMemoryUsage());
        system.setSmsOthers(this.fetchSystemHardDisk(config));
        sendMonitorMessage(config, system);
    }

    /**
     * 任務停止的時候, 將狀態改為0
     */
    @PreDestroy
    public void terminateJob() {
        MonitorData system = createSystemMonitorData(config, null);
        system.setSmsStoptime(Calendar.getInstance().getTime());
        sendMonitorMessage(config, system);
    }

    /**
     * 系統狀態---CPU
     *
     * @param config
     * @return
     */
    private int fetchSystemCpuUsage(SystemMonitorJobConfig config) {
        int cpu = 0;
        try {
            cpu = MonitorDataCollector.fetchSystemCpuUsage(httpClient2, config.getServiceUrl()).intValue();
        } catch (Throwable e) {
            warn(ProgramName, " fetchSystemCpuUsage with exception occur, ", e.getMessage());
        }
        return cpu;
    }

    /**
     * 系統狀態---RMA
     *
     * @return
     */
    private int fetchSystemMemoryUsage() {
        int memo = 0;
        try {
            memo = MonitorDataCollector.fetchSystemMemoryUsage().intValue();
        } catch (Throwable e) {
            warn(ProgramName, " fetchSystemMemoryUsage with exception occur, ", e.getMessage());
        }
        return memo;
    }

    /**
     * 系統DISK USED
     *
     * @param config
     * @return
     */
    private String fetchSystemHardDisk(SystemMonitorJobConfig config) {
        if (config.getSystemMountInterval() > 0 && latestFetchSystemHardDisk > 0
                && System.currentTimeMillis() - latestFetchSystemHardDisk <= config.getSystemMountInterval() && StringUtils.isNotBlank(latestSystemHardDisk)) {
            return latestSystemHardDisk;
        }
        String disk = StringUtils.EMPTY;
        try {
            List<MonitorDataDisk> monitorDataDiskList = null;
            // 如果沒有設定需要監控哪些資料夾, 則預設取OS的磁盤空間使用情況
            if (StringUtils.isBlank(config.getSystemMount())) {
                monitorDataDiskList = MonitorDataCollector.fetchSystemHardDisk(config.getSystemHostName(), config.getSystemHostIp());
            } else {
                String[] systemMounts = config.getSystemMount().split(",");
                if (ArrayUtils.isNotEmpty(systemMounts)) {
                    monitorDataDiskList = new ArrayList<>();
                    for (String systemMount : systemMounts) {
                        monitorDataDiskList.add(MonitorDataCollector.fetchMonitorDataDisk(config.getSystemHostName(), config.getSystemHostIp(), systemMount));
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(monitorDataDiskList)) {
                disk = new Gson().toJson(monitorDataDiskList);
            }
        } catch (Throwable e) {
            warn(ProgramName, " fetchSystemHardDisk with exception occur, ", e.getMessage());
        }
        if (config.getSystemMountInterval() > 0) {
            latestFetchSystemHardDisk = System.currentTimeMillis();
            latestSystemHardDisk = disk;
        }
        return disk;
    }

    private MonitorData createSystemMonitorData(SystemMonitorJobConfig config, Integer pid) {
        MonitorData system = new MonitorData();
        system.setSmsServicename(SERVICE_NAME_SYSTEM);
        system.setSmsServiceip(config.getSystemHostIp());
        system.setSmsHostname(config.getSystemHostName());
        system.setSmsUpdatetime(Calendar.getInstance().getTime());
        system.setSmsServicestate("0");
        system.setSmsCpu(0);
        system.setSmsRam(0);
        system.setSmsOthers(StringUtils.EMPTY);
        system.setSmsCpuThreshold(0);
        system.setSmsRamThreshold(0);
        system.setSmsThreads(0);
        system.setSmsThreadsActive(0);
        system.setSmsThreadsThreshold(0);
        system.setSmsPid(pid);
        return system;
    }

    private void sendMonitorMessage(SystemMonitorJobConfig config, MonitorData system) {
        boolean postFailed = false;
        String primaryUrl = config.getMonitorPrimaryUrl();
        if (StringUtils.isNotBlank(primaryUrl)) {
            try {
                httpClient2.postForEntity(primaryUrl, MediaType.APPLICATION_JSON, system, String.class);
            } catch (Exception e) {
                postFailed = true;
                warn("send System Monitor Data to primaryUrl = [", primaryUrl, "] failed with exception occur = [", e.getMessage(), "]");
                sendEMS(e, StringUtils.join("send System Monitor Data to primaryUrl = [", primaryUrl, " failed"));
            }
        } else {
            warn("cannot send System Monitor Data, primaryUrl is blank!!!");
        }
        if (postFailed) {
            postFailed = false;
            String secondaryUrl = config.getMonitorSecondaryUrl();
            if (StringUtils.isNotBlank(secondaryUrl)) {
                try {
                    httpClient2.postForEntity(secondaryUrl, MediaType.APPLICATION_JSON, system, String.class);
                } catch (Exception e) {
                    postFailed = true;
                    warn("send System Monitor Data to secondaryUrl = [", secondaryUrl, "] failed with exception occur = [", e.getMessage(), "]");
                    sendEMS(e, StringUtils.join("send System Monitor Data to secondaryUrl = [", secondaryUrl, " failed"));
                }
            } else {
                warn("cannot send System Monitor Data, secondaryUrl is blank!!!");
            }
        }
    }

    private void warn(Object... messages) {
        if (this.getJobConfig().isRecordHttpLog()) {
            ScheduleLogger.warn(messages);
        }
    }
}
