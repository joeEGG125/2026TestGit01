package com.syscom.fep.common.sms.hiair;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

// @Validated
@Configuration
@ConfigurationProperties(prefix = "spring.fep.sms.hiair")
@ConditionalOnProperty(prefix = "spring.fep.sms.hiair", name = "enable", havingValue = "true")
@Lazy
// @RefreshScope
public class HiairSmsConfiguration {
    private HiairSmsProtocol protocol = HiairSmsProtocol.SOCKET;
    /**
     * 智慧簡訊
     */
    private String httpUrlSendSMS = "https://api.sms.hinet.net/api/sendSMS/v1";
    /**
     * 帳號
     */
    private String username;
    /**
     * 密碼
     */
    private String sscode;
    /**
     * 預設的接收門號
     */
    private String mobile;
    /**
     * 1~1440, 單位:分鐘. (有效範圍外之數值, 將預設為 1440 分鐘)
     */
    private Integer limitTime;
    /**
     * http建立連線超時時間, 單位毫秒
     */
    private int httpConnectTimeout = 30000;
    /**
     * http等待接收回應超時時間, 單位毫秒
     */
    private int httpReadTimeout = 60000;
    /**
     * http簡訊內容限制最長1530個純英數字
     */
    private int httpMessageAlphaNumericLengthLimit = 1530;
    /**
     * http簡訊內容限制最長670個中文字(包含英數字)
     */
    private int httpMessageLengthLimit = 670;
    /**
     * SMS伺服器Socket IP
     */
    private String socketHost = "202.39.54.130";
    /**
     * SMS伺服器Socket Port
     */
    private int socketPort = 8000;
    /**
     * SMS伺服器Socket超時時間, 單位毫秒
     */
    private int socketSoTimeout = 30000;
    /**
     * socket簡訊內容限制最長670個中文字(包含英數字)
     */
    private int socketMessageBytesLengthLimit = 159;
    /**
     * 異步發送簡訊線程池核心數設定
     */
    private int executorCorePoolSize = 10;
    /**
     * 異步發送簡訊執行緒線程池中線程alive時間, 單位毫秒
     */
    private long executorKeepAliveTime = 0;
    /**
     * 異步發送簡訊執行緒線程池Queue Size
     */
    private int executorQueueCapacity = Integer.MAX_VALUE;

    public HiairSmsProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(HiairSmsProtocol protocol) {
        this.protocol = protocol;
    }

    public String getHttpUrlSendSMS() {
        return httpUrlSendSMS;
    }

    public void setHttpUrlSendSMS(String httpUrlSendSMS) {
        this.httpUrlSendSMS = httpUrlSendSMS;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSscode() {
        return sscode;
    }

    public void setSscode(String sscode) {
        this.sscode = sscode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(Integer limitTime) {
        this.limitTime = limitTime;
    }

    public int getHttpConnectTimeout() {
        return httpConnectTimeout;
    }

    public void setHttpConnectTimeout(int httpConnectTimeout) {
        this.httpConnectTimeout = httpConnectTimeout;
    }

    public int getHttpReadTimeout() {
        return httpReadTimeout;
    }

    public void setHttpReadTimeout(int httpReadTimeout) {
        this.httpReadTimeout = httpReadTimeout;
    }

    public int getHttpMessageAlphaNumericLengthLimit() {
        return httpMessageAlphaNumericLengthLimit;
    }

    public void setHttpMessageAlphaNumericLengthLimit(int httpMessageAlphaNumericLengthLimit) {
        this.httpMessageAlphaNumericLengthLimit = httpMessageAlphaNumericLengthLimit;
    }

    public int getHttpMessageLengthLimit() {
        return httpMessageLengthLimit;
    }

    public void setHttpMessageLengthLimit(int httpMessageLengthLimit) {
        this.httpMessageLengthLimit = httpMessageLengthLimit;
    }

    public String getSocketHost() {
        return socketHost;
    }

    public void setSocketHost(String socketHost) {
        this.socketHost = socketHost;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public void setSocketPort(int socketPort) {
        this.socketPort = socketPort;
    }

    public int getSocketSoTimeout() {
        return socketSoTimeout;
    }

    public void setSocketSoTimeout(int socketSoTimeout) {
        this.socketSoTimeout = socketSoTimeout;
    }

    public int getSocketMessageBytesLengthLimit() {
        return socketMessageBytesLengthLimit;
    }

    public void setSocketMessageBytesLengthLimit(int socketMessageBytesLengthLimit) {
        this.socketMessageBytesLengthLimit = socketMessageBytesLengthLimit;
    }

    public int getExecutorCorePoolSize() {
        return executorCorePoolSize;
    }

    public void setExecutorCorePoolSize(int executorCorePoolSize) {
        this.executorCorePoolSize = executorCorePoolSize;
    }

    public long getExecutorKeepAliveTime() {
        return executorKeepAliveTime;
    }

    public void setExecutorKeepAliveTime(long executorKeepAliveTime) {
        this.executorKeepAliveTime = executorKeepAliveTime;
    }

    public int getExecutorQueueCapacity() {
        return executorQueueCapacity;
    }

    public void setExecutorQueueCapacity(int executorQueueCapacity) {
        this.executorQueueCapacity = executorQueueCapacity;
    }

    @PostConstruct
    public void postConstruct() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Hiair Sms Configuration"));
    }
}
