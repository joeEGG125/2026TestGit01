package com.syscom.fep.server.gateway.ims.processor;

import com.syscom.fep.server.gateway.ims.IMSGatewayLineType;
import com.syscom.fep.server.gateway.ims.IMSGatewayMode;

public class IMSGatewayProcessorConfiguration {
    private IMSGatewayMode mode;
    private String host;
    private int port;
    private long reestablishConnectionInterval = 10000;
    private String clientId;
    private int resumeInterval = 120000;
    private String dataStore;
    private long pingIMSInterval = 5000L;
    private String configurationPropertiesPrefix;
    private boolean enable;
    private IMSGatewayLineType lineType;
    private String threadName;
    private IMSGatewayProcessorType processorType;
    private boolean autoReestablishConnection = true;

    public IMSGatewayMode getMode() {
        return mode;
    }

    public void setMode(IMSGatewayMode mode) {
        this.mode = mode;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getReestablishConnectionInterval() {
        return reestablishConnectionInterval;
    }

    public void setReestablishConnectionInterval(long reestablishConnectionInterval) {
        this.reestablishConnectionInterval = reestablishConnectionInterval;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getResumeInterval() {
        return resumeInterval;
    }

    public void setResumeInterval(int resumeInterval) {
        this.resumeInterval = resumeInterval;
    }

    public String getDataStore() {
        return dataStore;
    }

    public void setDataStore(String dataStore) {
        this.dataStore = dataStore;
    }

    public long getPingIMSInterval() {
        return pingIMSInterval;
    }

    public void setPingIMSInterval(long pingIMSInterval) {
        this.pingIMSInterval = pingIMSInterval;
    }

    public String getConfigurationPropertiesPrefix() {
        return configurationPropertiesPrefix;
    }

    public void setConfigurationPropertiesPrefix(String configurationPropertiesPrefix) {
        this.configurationPropertiesPrefix = configurationPropertiesPrefix;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public IMSGatewayLineType getLineType() {
        return lineType;
    }

    public void setLineType(IMSGatewayLineType lineType) {
        this.lineType = lineType;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public IMSGatewayProcessorType getProcessorType() {
        return processorType;
    }

    public void setProcessorType(IMSGatewayProcessorType processorType) {
        this.processorType = processorType;
    }

    public boolean isAutoReestablishConnection() {
        return autoReestablishConnection;
    }

    public void setAutoReestablishConnection(boolean autoReestablishConnection) {
        this.autoReestablishConnection = autoReestablishConnection;
    }
}
