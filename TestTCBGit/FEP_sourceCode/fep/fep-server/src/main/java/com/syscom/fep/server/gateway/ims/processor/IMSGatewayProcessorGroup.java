package com.syscom.fep.server.gateway.ims.processor;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.server.gateway.ims.IMSGatewayConfiguration;
import com.syscom.fep.server.gateway.ims.IMSGatewayConnState;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.concurrent.ExecutorService;

public class IMSGatewayProcessorGroup extends FEPBase {
    private final LogHelper logger = LogHelperFactory.getTraceLogger();
    private final IMSGatewayConfiguration configuration;
    private final IMSGatewayProcessorConfiguration senderConfiguration;
    private final IMSGatewayProcessorConfiguration receiverConfiguration;
    private final String tranCode;
    private final ExecutorService executor;
    private IMSGatewayProcessor sender;
    private IMSGatewayProcessor receiver;
    private final Object connStateChangedLock = new Object();
    private boolean connStateChangedDisconnected = false;

    public IMSGatewayProcessorGroup(IMSGatewayConfiguration configuration, IMSGatewayProcessorConfiguration senderConfiguration, IMSGatewayProcessorConfiguration receiverConfiguration, String tranCode, ExecutorService executor) {
        this.configuration = configuration;
        this.senderConfiguration = this.overwriteProperty(senderConfiguration);
        this.receiverConfiguration = this.overwriteProperty(receiverConfiguration);
        this.tranCode = tranCode;
        this.executor = executor;
    }

    private IMSGatewayProcessorConfiguration overwriteProperty(IMSGatewayProcessorConfiguration processorConfiguration) {
        if (this.configuration.isHandleSenderAndReceiverBoth()) {
            processorConfiguration.setAutoReestablishConnection(false); // 設置不要自動重連, 後面會手動重連
        }
        return processorConfiguration;
    }

    public IMSGatewayProcessorConfiguration getSenderConfiguration() {
        return senderConfiguration;
    }

    public IMSGatewayProcessorConfiguration getReceiverConfiguration() {
        return receiverConfiguration;
    }

    public IMSGatewayProcessor getSender() {
        return sender;
    }

    public IMSGatewayProcessor getReceiver() {
        return receiver;
    }

    public void run() {
        this.runSender(false);
        if (!this.configuration.isHandleSenderAndReceiverBoth()) {
            this.runReceiver();
        }
    }

    private void runSender(boolean waitToBuildIMSConnectionWhenStart) {
        synchronized (senderConfiguration) {
            // 如果腳位沒有啟用, 則不要運行
            if (!senderConfiguration.isEnable()) {
                logger.debug("[", ProgramName, ".runSender]", senderConfiguration.getThreadName(), " cannot to run and build connection to IMS, Enable:", senderConfiguration.isEnable());
                return;
            }
            if (this.sender == null) {
                this.sender = new IMSGatewayProcessor(this, configuration, senderConfiguration, tranCode, null, executor);
                this.sender.setWaitToBuildIMSConnectionWhenStart(waitToBuildIMSConnectionWhenStart);
                this.sender.start();
            } else {
                logger.debug("[", ProgramName, ".runSender]", senderConfiguration.getThreadName(), " is running");
            }
        }
    }

    private void runReceiver() {
        synchronized (receiverConfiguration) {
            // 如果腳位沒有啟用, 則不要運行
            if (!receiverConfiguration.isEnable()) {
                logger.debug("[", ProgramName, ".runReceiver]", receiverConfiguration.getThreadName(), " cannot to run and build connection to IMS, Enable:", receiverConfiguration.isEnable());
                return;
            }
            if (this.receiver == null) {
                this.receiver = new IMSGatewayProcessor(this, configuration, receiverConfiguration, tranCode, this.sender, executor);
                this.receiver.start();
            } else {
                logger.debug("[", ProgramName, ".runReceiver]", receiverConfiguration.getThreadName(), " is running");
            }
        }
    }

    public void terminate() {
        this.terminateReceiver();
        this.terminateSender();
    }

    private void terminateSender() {
        synchronized (senderConfiguration) {
            if (sender != null && sender.isRunning()) {
                sender.terminate();
                sender = null;
            }
        }
    }

    private void terminateReceiver() {
        synchronized (receiverConfiguration) {
            if (receiver != null && receiver.isRunning()) {
                receiver.terminate();
                receiver = null;
            }
        }
    }

    /**
     * 只要有一個連線中則表示有連線
     *
     * @return
     */
    public boolean isConnected() {
        return (receiver != null && receiver.isConnected()) || (sender != null && sender.isConnected());
    }

    /**
     * 全部連線中則表示全部都連線
     *
     * @return
     */
    public boolean isAllConnected() {
        return receiver != null && receiver.isConnected() && sender != null && sender.isConnected();
    }

    void connStateChanged(LogData logData, IMSGatewayProcessorConfiguration processorConfiguration, IMSGatewayConnState state, Throwable t) {
        synchronized (connStateChangedLock) {
            if (this.configuration.isHandleSenderAndReceiverBoth()) {
                if (state == IMSGatewayConnState.CONNECTING) {
                    this.connStateChangedDisconnected = false;
                } else if (state == IMSGatewayConnState.CONNECTED) {
                    // 如果是sender連線成功, 則啟動receiver
                    if (processorConfiguration.getProcessorType() == IMSGatewayProcessorType.SENDER) {
                        logData.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
                        logData.setRemark(StringUtils.join(processorConfiguration.getThreadName(), " has been connected to IMS, so that ", this.receiverConfiguration.getThreadName(), " start to run and build connection to IMS"));
                        logMessage(Level.WARN, logData);
                        this.runReceiver();
                    }
                } else if (state == IMSGatewayConnState.CONNECTING_FAILED) {
                    // 當有一個腳位重新建立連線失敗時, 則對應腳位斷開
                    this.connStateChanged(logData, processorConfiguration, IMSGatewayConnState.DISCONNECTED, t);
                } else if (state == IMSGatewayConnState.DISCONNECTED) {
                    // 避免重複動作, 加入connStateChangedDisconnected判斷
                    if (!this.connStateChangedDisconnected) {
                        this.connStateChangedDisconnected = true;
                        this.terminate();
                        this.runSender(true);
                    }
                }
            }
        }
    }

    /**
     * 改變腳位狀態, 啟動或停止
     *
     * @param logData
     * @param clientId
     * @param enable
     * @return
     */
    public boolean changeLineStatus(LogData logData, String clientId, boolean enable) {
        if (this.senderConfiguration != null && this.receiverConfiguration != null) {
            // 如果ClientId不正確, 則直接返回false
            if (!this.senderConfiguration.getClientId().equals(clientId) && !this.receiverConfiguration.getClientId().equals(clientId)) {
                return false;
            }
            // 如果是Sender和Receiver同步操作
            if (this.configuration.isHandleSenderAndReceiverBoth()) {
                this.senderConfiguration.setEnable(enable); // 更新enable屬性
                this.receiverConfiguration.setEnable(enable); // 更新enable屬性
                // 那麼啟動時也要Sender和Reciever同時啟動, 因為Sender啟動成功後, 會自動再啟動Receiver, 所以這裡不用call啟動Receiver的方法
                if (enable) {
                    this.runSender(false);
                }
                // 這裡斷開Receiver就好, Sender會跟著一起斷開
                else {
                    this.terminateReceiver();
                }
            } else {
                if (this.senderConfiguration.getClientId().equals(clientId)) {
                    this.senderConfiguration.setEnable(enable); // 更新enable屬性
                    if (enable) {
                        this.runSender(false);
                    } else {
                        this.terminateSender();
                    }
                } else if (this.receiverConfiguration.getClientId().equals(clientId)) {
                    this.receiverConfiguration.setEnable(enable); // 更新enable屬性
                    if (enable) {
                        this.runReceiver();
                    } else {
                        this.terminateReceiver();
                    }
                }
            }
            return true;
        }
        return false;
    }
}
