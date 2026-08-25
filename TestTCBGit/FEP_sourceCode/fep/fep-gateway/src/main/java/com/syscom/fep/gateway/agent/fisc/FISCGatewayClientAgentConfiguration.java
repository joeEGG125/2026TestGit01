package com.syscom.fep.gateway.agent.fisc;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.gateway.agent.fisc.job.FISCGatewayClientAgentProcessCommandJobConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// @Validated
@ConfigurationProperties(prefix = "spring.fep.gateway.agent.fisc")
// @RefreshScope
public class FISCGatewayClientAgentConfiguration {
    @NotNull
    private String cmdStart;
    @NotNull
    private String httpTerminate;
    @NotNull
    private String httpOperate;
    private boolean recordHttpLog = false;
    private boolean printInputStream = false;
    private List<FISCGatewayClientAgentProcessCommandJobConfig> cmdJobConfig;
    @NotNull
    private String gwLogPath;
    private int lastRows = 100;
    @NotNull
    private String httpChangeFEPAP;
    @NotNull
    private String httpCheckFEPAP;

    public String getCmdStart() {
        return cmdStart;
    }

    public void setCmdStart(String cmdStart) {
        this.cmdStart = cmdStart;
    }

    public String getHttpTerminate() {
        return httpTerminate;
    }

    public void setHttpTerminate(String httpTerminate) {
        this.httpTerminate = httpTerminate;
    }

    public String getHttpOperate() {
        return httpOperate;
    }

    public void setHttpOperate(String httpOperate) {
        this.httpOperate = httpOperate;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }

    public boolean isPrintInputStream() {
        return printInputStream;
    }

    public void setPrintInputStream(boolean printInputStream) {
        this.printInputStream = printInputStream;
    }

    public List<FISCGatewayClientAgentProcessCommandJobConfig> getCmdJobConfig() {
        return cmdJobConfig;
    }

    public void setCmdJobConfig(List<FISCGatewayClientAgentProcessCommandJobConfig> cmdJobConfig) {
        this.cmdJobConfig = cmdJobConfig;
    }

    public @NotNull String getGwLogPath() {
        return gwLogPath;
    }

    public void setGwLogPath(@NotNull String gwLogPath) {
        this.gwLogPath = gwLogPath;
    }

    public int getLastRows() {
        return lastRows;
    }

    public void setLastRows(int lastRows) {
        this.lastRows = lastRows;
    }

    public String getHttpChangeFEPAP() {
        return httpChangeFEPAP;
    }

    public void setHttpChangeFEPAP(String httpChangeFEPAP) {
        this.httpChangeFEPAP = httpChangeFEPAP;
    }

    public String getHttpCheckFEPAP() {
        return httpCheckFEPAP;
    }

    public void setHttpCheckFEPAP(String httpCheckFEPAP) {
        this.httpCheckFEPAP = httpCheckFEPAP;
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "FISCGateway Agent Configuration"));
    }
}
