package com.syscom.fep.service.monitor.svr;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.fep.service.monitor.checker")
public class MonitorCheckerConfiguration {
    private long receiveMonitorDataTimeout = 300000L;

    public long getReceiveMonitorDataTimeout() {
        return receiveMonitorDataTimeout;
    }

    public void setReceiveMonitorDataTimeout(long receiveMonitorDataTimeout) {
        this.receiveMonitorDataTimeout = receiveMonitorDataTimeout;
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "AppMonitor Checker Configuration", true));
    }
}
