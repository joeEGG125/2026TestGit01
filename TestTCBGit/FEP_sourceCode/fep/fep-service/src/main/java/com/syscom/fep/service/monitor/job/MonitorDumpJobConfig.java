package com.syscom.fep.service.monitor.job;

import com.syscom.fep.scheduler.job.SchedulerJobConfig;
import com.syscom.fep.service.monitor.vo.MonitorServerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

@ConfigurationProperties(prefix = "spring.fep.service.monitor.dump")
// @RefreshScope
public class MonitorDumpJobConfig extends SchedulerJobConfig {
    @NestedConfigurationProperty
    private MonitorServerInfo system = new MonitorServerInfo();
    private String path;
    private String delimiter = ";";
    @Value("#{'${spring.fep.service.monitor.dump.exclude.name:}'.split(',')}")
    private List<String> excludeServiceNameList;
    private long delayToStartDumpFile = 3 * 60 * 1000L; // 預設3分鐘後開始dump檔案
    private boolean logDumpFile = true;

    public MonitorServerInfo getSystem() {
        return system;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public List<String> getExcludeServiceNameList() {
        return excludeServiceNameList;
    }

    public void setExcludeServiceNameList(List<String> excludeServiceNameList) {
        this.excludeServiceNameList = excludeServiceNameList;
    }

    public long getDelayToStartDumpFile() {
        return delayToStartDumpFile;
    }

    public void setDelayToStartDumpFile(long delayToStartDumpFile) {
        this.delayToStartDumpFile = delayToStartDumpFile;
    }

    public boolean isLogDumpFile() {
        return logDumpFile;
    }

    public void setLogDumpFile(boolean logDumpFile) {
        this.logDumpFile = logDumpFile;
    }
}
