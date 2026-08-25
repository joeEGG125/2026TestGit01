package com.syscom.fep.server.gateway.ims.job;

import com.syscom.fep.scheduler.job.SchedulerJobConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.fep.server.gateway.ims.app-monitor")
@ConditionalOnProperty(prefix = "spring.fep.server.gateway.ims.app-monitor", name = {"cronExpression", "monitorUrl"})
// @RefreshScope
public class IMSGatewayAppMonitorJobConfig extends SchedulerJobConfig {
    private String monitorUrl;
    private boolean recordHttpLog = false;
    private int timeout = 60000;

    public String getMonitorUrl() {
        return monitorUrl;
    }

    public void setMonitorUrl(String monitorUrl) {
        this.monitorUrl = monitorUrl;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
