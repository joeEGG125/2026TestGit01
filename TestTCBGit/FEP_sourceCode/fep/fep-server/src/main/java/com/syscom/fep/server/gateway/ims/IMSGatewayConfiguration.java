package com.syscom.fep.server.gateway.ims;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.server.gateway.ims.processor.IMSGatewayProcessorGroupConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import jakarta.annotation.PostConstruct;

/**
 * IMS Gateway配置檔對應的Spring物件
 * <p>
 * 配置檔名為application-server-imsgw.properties
 *
 * @author Richard & Ashiang
 */
@ConfigurationProperties(prefix = "spring.fep.server.gateway.ims")
// @RefreshScope
public class IMSGatewayConfiguration {
    /**
     * 出現異常後需要重新建立連線前先wait毫秒數, 避免一直失敗立刻就重連太頻繁
     */
    private long sleepForRebuildConnectionInMilliseconds = 100L;
    /**
     * 在terminate時, 如果有業務正在處理中, 是否等待業務處理完畢, 再終止線程
     */
    private boolean waitTransactionExecutedFinishedBeforeTerminate = true;
    /**
     * 在terminate時, 如果有業務正在處理中, 等待業務處理完畢的最長時間, 避免等待的時間過長
     */
    private long waitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds = 10000L;
    /**
     * IMS Client Id
     */
    private String clientId;
    /**
     * IMS的RSM訊息,非交易訊息
     */
    private String messagePrefixRSM = "*REQSTS*";
    /**
     * 模擬收送訊息, only for Test
     */
    private boolean simulatorReceiveAndSend = false;
    /**
     * Primary
     */
    @NestedConfigurationProperty
    private final IMSGatewayProcessorGroupConfiguration primary = new IMSGatewayProcessorGroupConfiguration();
    /**
     * Secondary
     */
    @NestedConfigurationProperty
    private final IMSGatewayProcessorGroupConfiguration secondary = new IMSGatewayProcessorGroupConfiguration();
    /**
     * IMSConnect API Log路徑
     */
    private String imsApiLogPath = "/fep/logs/";
    /**
     * IMSConnect API Log檔名
     */
    private String imsApiLogFileName = "IMSConnect.log";
    /**
     * 送http請求是否記錄log
     */
    private boolean recordHttpLog = false;
    /**
     * 操控channel的http請求
     */
    private String httpChannel;
    /**
     * Http請求超時時間
     */
    private int httpTimeout = 10000;
    /**
     * 服務啟動時, 先送一個stopChannel secondary給另一台IMSGW, 發送後隔3秒, 再啟動primary的channel
     */
    private long sleepForStartPrimary = 3000L;
    /**
     * 發mail通知, 多筆用逗號間隔
     */
    @Value("${spring.fep.server.gateway.ims.mailAddress:}")
    private String mailTo;
    /**
     * 停止另外一台IMSGW重試次數
     */
    private int stopSecondaryToOtherRetryCount = 3;
    /**
     * 停止另外一台IMSGW重試間隔毫秒
     */
    private long retryStopSecondaryToOtherSleep = 1000;
    /**
     * 線程池核心數設定
     */
    private int executorCorePoolSize = 1;
    /**
     * 線程池核心數最大值設定
     */
    private int executorMaximumPoolSize = 1;
    /**
     * 執行緒線程池中線程alive時間, 單位毫秒
     */
    private long executorKeepAliveTime = 0;
    /**
     * 執行緒線程池Queue Size
     */
    private int executorQueueCapacity = Integer.MAX_VALUE;
    /**
     * 同時處理Sender和Receiver
     * 例如, 一組腳位中不論sender或receiver若有發生斷線, 是否把該組另一個腳位也切斷
     * 啟動Sender則Sender啟動成功後再啟動Receiver
     */
    private boolean handleSenderAndReceiverBoth = true;

    public long getSleepForRebuildConnectionInMilliseconds() {
        return sleepForRebuildConnectionInMilliseconds;
    }

    public void setSleepForRebuildConnectionInMilliseconds(long sleepForRebuildConnectionInMilliseconds) {
        this.sleepForRebuildConnectionInMilliseconds = sleepForRebuildConnectionInMilliseconds;
    }

    public boolean isWaitTransactionExecutedFinishedBeforeTerminate() {
        return waitTransactionExecutedFinishedBeforeTerminate;
    }

    public void setWaitTransactionExecutedFinishedBeforeTerminate(boolean waitTransactionExecutedFinishedBeforeTerminate) {
        this.waitTransactionExecutedFinishedBeforeTerminate = waitTransactionExecutedFinishedBeforeTerminate;
    }

    public long getWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds() {
        return waitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds;
    }

    public void setWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds(long waitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds) {
        this.waitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds = waitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getMessagePrefixRSM() {
        return messagePrefixRSM;
    }

    public void setMessagePrefixRSM(String messagePrefixRSM) {
        this.messagePrefixRSM = messagePrefixRSM;
    }

    public boolean isSimulatorReceiveAndSend() {
        return simulatorReceiveAndSend;
    }

    public void setSimulatorReceiveAndSend(boolean simulatorReceiveAndSend) {
        this.simulatorReceiveAndSend = simulatorReceiveAndSend;
    }

    public int getExecutorCorePoolSize() {
        return executorCorePoolSize;
    }

    public void setExecutorCorePoolSize(int executorCorePoolSize) {
        this.executorCorePoolSize = executorCorePoolSize;
    }

    public int getExecutorMaximumPoolSize() {
        return executorMaximumPoolSize;
    }

    public void setExecutorMaximumPoolSize(int executorMaximumPoolSize) {
        this.executorMaximumPoolSize = executorMaximumPoolSize;
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

    /**
     * @return the primary
     */
    public IMSGatewayProcessorGroupConfiguration getPrimary() {
        primary.setMode(IMSGatewayMode.primary);
        return primary;
    }

    /**
     * @return the secondary
     */
    public IMSGatewayProcessorGroupConfiguration getSecondary() {
        secondary.setMode(IMSGatewayMode.secondary);
        return secondary;
    }

    /**
     * @return the imsApiLogPath
     */
    public String getImsApiLogPath() {
        return imsApiLogPath;
    }

    /**
     * @param imsApiLogPath the imsApiLogPath to set
     */
    public void setImsApiLogPath(String imsApiLogPath) {
        this.imsApiLogPath = imsApiLogPath;
    }

    /**
     * @return the imsApiLogFileName
     */
    public String getImsApiLogFileName() {
        return imsApiLogFileName;
    }

    /**
     * @param imsApiLogFileName the imsApiLogFileName to set
     */
    public void setImsApiLogFileName(String imsApiLogFileName) {
        this.imsApiLogFileName = imsApiLogFileName;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }

    public String getHttpChannel() {
        return httpChannel;
    }

    public void setHttpChannel(String httpChannel) {
        this.httpChannel = httpChannel;
    }

    public int getHttpTimeout() {
        return httpTimeout;
    }

    public void setHttpTimeout(int httpTimeout) {
        this.httpTimeout = httpTimeout;
    }

    public long getSleepForStartPrimary() {
        return sleepForStartPrimary;
    }

    public void setSleepForStartPrimary(long sleepForStartPrimary) {
        this.sleepForStartPrimary = sleepForStartPrimary;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public int getStopSecondaryToOtherRetryCount() {
        return stopSecondaryToOtherRetryCount;
    }

    public void setStopSecondaryToOtherRetryCount(int stopSecondaryToOtherRetryCount) {
        this.stopSecondaryToOtherRetryCount = stopSecondaryToOtherRetryCount;
    }

    public long getRetryStopSecondaryToOtherSleep() {
        return retryStopSecondaryToOtherSleep;
    }

    public void setRetryStopSecondaryToOtherSleep(long retryStopSecondaryToOtherSleep) {
        this.retryStopSecondaryToOtherSleep = retryStopSecondaryToOtherSleep;
    }

    public boolean isHandleSenderAndReceiverBoth() {
        return handleSenderAndReceiverBoth;
    }

    public void setHandleSenderAndReceiverBoth(boolean handleSenderAndReceiverBoth) {
        this.handleSenderAndReceiverBoth = handleSenderAndReceiverBoth;
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "IMS Gateway Configuration"));
    }
}
