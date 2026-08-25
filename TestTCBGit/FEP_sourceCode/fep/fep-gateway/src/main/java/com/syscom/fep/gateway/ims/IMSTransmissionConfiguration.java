package com.syscom.fep.gateway.ims;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.PostConstruct;

/**
 * IMS連線物件配置檔
 */
public class IMSTransmissionConfiguration {
    /**
     * 配置檔前綴
     */
    private String configurationPropertiesPrefix;
    /**
     * Gateway類型
     */
    private Gateway gateway;
    /**
     * 類型
     */
    private SocketType socketType;
    /**
     * IMS主機HOST
     */
    private String host;
    /**
     * IMS主機PORT
     */
    private int port;
    /**
     * 發生斷線後, sleep多長時間後嘗試恢復連線, 單位毫秒
     */
    private long reestablishConnectionInterval = 10000;
    /**
     * IMS Client Id
     */
    private String clientId;
    /**
     * IMS Client Id
     */
    private String dataStore;
    /**
     * IMS連線逾時, 單位毫秒
     */
    private int connectTimeout = 5000;
    /**
     * IMS tranCode
     */
    private String tranCode = StringUtils.EMPTY;
    /**
     * IMS ImsConnectTimeout & InteractionTimeout
     */
    private int resumeInterval = 120000;
    /**
     * 診測IMS連線狀態的間隔時間, 單位毫秒
     */
    private int connectionDetectInterval = 5000;
    /**
     * 是否有列印過配置檔內容
     */
    private boolean printed;
    /**
     * 是否需要在斷線後自動恢復連線
     */
    private boolean autoReestablishConnection = true;
    /**
     * 是否需要強制關閉Socket
     */
    private boolean forceCloseSocket;
    /**
     * 用來記錄Socket的Local端口
     */
    private String socketLocal = StringUtils.EMPTY;

    @PostConstruct
    public void initialization() {
        print();
    }

    public String getConfigurationPropertiesPrefix() {
        return configurationPropertiesPrefix;
    }

    public void setConfigurationPropertiesPrefix(String configurationPropertiesPrefix) {
        this.configurationPropertiesPrefix = configurationPropertiesPrefix;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public SocketType getSocketType() {
        return socketType;
    }

    public void setSocketType(SocketType socketType) {
        this.socketType = socketType;
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

    public String getDataStore() {
        return dataStore;
    }

    public void setDataStore(String dataStore) {
        this.dataStore = dataStore;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getTranCode() {
        return tranCode;
    }

    public void setTranCode(String tranCode) {
        this.tranCode = tranCode;
    }

    public int getResumeInterval() {
        return resumeInterval;
    }

    public void setResumeInterval(int resumeInterval) {
        this.resumeInterval = resumeInterval;
    }

    public int getConnectionDetectInterval() {
        return connectionDetectInterval;
    }

    public void setConnectionDetectInterval(int connectionDetectInterval) {
        this.connectionDetectInterval = connectionDetectInterval;
    }

    public boolean isAutoReestablishConnection() {
        return autoReestablishConnection;
    }

    public void setAutoReestablishConnection(boolean autoReestablishConnection) {
        this.autoReestablishConnection = autoReestablishConnection;
    }

    public boolean isForceCloseSocket() {
        return forceCloseSocket;
    }

    public void setForceCloseSocket(boolean forceCloseSocket) {
        this.forceCloseSocket = forceCloseSocket;
    }

    public String getSocketLocal() {
        return socketLocal;
    }

    public void setSocketLocal(String socketLocal) {
        this.socketLocal = socketLocal;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getGateway().name());
        if (this.getSocketType() != null) {
            sb.append("-").append(this.getSocketType().name());
        }
        sb.append("-").append(clientId);
        return sb.toString();
    }

    public String forLogging() {
        StringBuilder sb = new StringBuilder();
        sb.append("gateway:").append(this.getGateway().name());
        if (this.getSocketType() != null)
            sb.append(",socketType:").append(this.getSocketType().name());
        sb.append(",socketRemote:").append(host).append(":").append(port);
        sb.append(",socketLocal:").append(socketLocal);
        sb.append(",clientId:").append(clientId);
        sb.append(",dataStore:").append(dataStore);
        return sb.toString();
    }

    private void print() {
        if (!this.printed) {
            this.printed = true;
            LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, StringUtils.replace(this.getName(), "-", StringUtils.SPACE), true, excludeFields()));
        }
    }

    public String[] excludeFields() {
        return new String[] {"printed", "lockConnStateChanged", "socketLocal", "receiverConnected"};
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
    }
}
