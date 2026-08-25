package com.syscom.fep.service.monitor.job;

import com.syscom.fep.scheduler.job.SchedulerJobConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.service.monitor.heartbeat")
// @RefreshScope
public class MonitorHeartbeatJobConfig extends SchedulerJobConfig {
    private String remoteAppMonUrl;
    private boolean recordHttpLog = false;

    public String getRemoteAppMonUrl() {
        return remoteAppMonUrl;
    }

    public void setRemoteAppMonUrl(String remoteAppMonUrl) {
        this.remoteAppMonUrl = remoteAppMonUrl;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }
}
