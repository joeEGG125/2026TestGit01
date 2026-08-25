package com.syscom.fep.gateway.ims;

import com.ibm.ims.connect.*;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SocketUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.configuration.GatewayManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * IMS連線元件
 *
 * @param <Configuration>
 * @param <ProcessRequest>
 */
public abstract class IMSTransmission<Configuration extends IMSTransmissionConfiguration, ProcessRequest extends IMSTransmissionProcessRequest<Configuration>> extends Thread implements IMSTransmissionConnStateListener<Configuration> {
    protected final String ProgramName = this.getClass().getSimpleName();
    protected final LogData logData = new LogData();
    protected final AtomicReference<IMSTransmissionConnState> currentConnState = new AtomicReference<>(IMSTransmissionConnState.CLIENT_DISCONNECTED);
    private final Object reestablishConnectionLock = new Object();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private ConnectionFactory connectionFactory = null;
    private Connection connection = null;
    @Autowired
    protected GatewayManager manager;
    @Autowired
    protected IMSTransmissionNotification notification;
    @Autowired
    protected Configuration configuration;
    @Autowired
    protected ProcessRequest processRequest;
    // 配置IMS日誌
    private static ApiLoggingConfiguration apiLoggingConfig;
    protected Socket socket = null;
    protected IMSTransmissionMonitor<Configuration> transmissionMonitor = new IMSTransmissionMonitor<>();

    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, configuration.getGateway().name());
    }

    @PostConstruct
    public void postConstruct() {
        this.putMDC();
        this.setName(this.configuration.getName());
        this.transmissionMonitor.setTransmissionConfiguration(this.configuration);
        this.initLoggingConfiguration();
        this.initialization();
        this.notification.addConnStateListener(this.configuration, this, 0);
        this.manager.addTransmission(this);
    }

    /**
     * 專門用於非SpringBean模式下啟動Gateway
     *
     * @param configuration
     * @param processRequest
     */
    public void initialization(Configuration configuration, ProcessRequest processRequest) {
        this.manager = SpringBeanFactoryUtil.getBean(GatewayManager.class);
        this.notification = SpringBeanFactoryUtil.getBean(IMSTransmissionNotification.class);
        this.configuration = configuration;
        this.configuration.initialization();
        this.processRequest = processRequest;
        this.processRequest.initialization(configuration);
        this.postConstruct();
    }

    /**
     * 初始化
     */
    protected abstract void initialization();

    /**
     * 配置IMS日誌
     */
    private void initLoggingConfiguration() {
        boolean enableTrace = EnvPropertiesUtil.getProperty("spring.fep.ims.enableTrace", false);
        if (enableTrace && apiLoggingConfig == null) {
            apiLoggingConfig = new ApiLoggingConfiguration();
            try {
                String logPath = EnvPropertiesUtil.getProperty("spring.fep.log.path", "/fep/logs/");
                String apName = EnvPropertiesUtil.getProperty("management.metrics.tags.application", StringUtils.join("fep-gateway-", this.configuration.getGateway().getNamePrefix()));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String apiLog = Paths.get(logPath, LocalDateTime.now().format(formatter), apName + "-IMSConnect.log").toString();
                File directory = new File(apiLog).getParentFile();
                if (!directory.exists()) {
                    directory.mkdirs();
                } else {
                    File f = new File(apiLog);
                    if (f.exists()) {
                        f.delete();
                    }
                }
                apiLoggingConfig.configureApiLogging(apiLog, ApiProperties.TRACE_LEVEL_INTERNAL);
            } catch (ImsConnectApiException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
        }
    }

    /**
     * 接收連線狀態發生變化
     *
     * @param configuration
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(Configuration configuration, IMSTransmissionConnState state, Throwable t) {
        this.putMDC();
        this.currentConnState.set(state);
        this.transmissionMonitor.setConnState(this.currentConnState.get());
        if (state == IMSTransmissionConnState.CLIENT_CONNECTED) {
            this.transmissionMonitor.setLocal(this.configuration.getSocketLocal());
            this.transmissionMonitor.setConnections(1);
            this.transmissionMonitor.setConnectedTime(Calendar.getInstance().getTimeInMillis());
        } else if (state == IMSTransmissionConnState.CLIENT_DISCONNECTED) {
            this.configuration.setSocketLocal(StringUtils.EMPTY);
            this.transmissionMonitor.setLocal(this.configuration.getSocketLocal());
            this.transmissionMonitor.setConnections(0);
            this.transmissionMonitor.setDisconnectedTime(Calendar.getInstance().getTimeInMillis());
        }
    }

    /**
     * 獲取配置檔物件
     *
     * @return
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 取得當前連線狀態
     *
     * @return
     */
    public IMSTransmissionConnState getCurrentConnState() {
        return this.currentConnState.get();
    }

    /**
     * 取得IMSTransmissionMonitor物件
     *
     * @return
     */
    public IMSTransmissionMonitor<Configuration> getIMSTransmissionMonitor() {
        return transmissionMonitor;
    }

    /**
     * 終止
     */
    @PreDestroy
    public void terminate() {
        if (!this.running.getAndSet(false))
            return;
        this.putMDC();
        this.logData.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        this.logData.setRemark(StringUtils.join("Thread ", this.getName(), " start to stopping..."));
        FEPBase.logMessage(Level.INFO, this.logData);
        synchronized (this.reestablishConnectionLock) {
            this.reestablishConnectionLock.notifyAll();
        }
        this.closeConnection();
        this.processRequest.preDestroy();
        this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_SHUTTING_DOWN);
        this.logData.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        this.logData.setRemark(StringUtils.join("Thread ", this.getName(), " has stopped"));
        FEPBase.logMessage(Level.INFO, this.logData);
        this.notification.removeConnStateListener(this.configuration, this);
        this.manager.removeTransmission(this);
        this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_SHUT_DOWN);
    }

    @Override
    public void run() {
        this.putMDC();
        this.logData.setProgramName(StringUtils.join(ProgramName, ".run"));
        this.logData.setRemark(StringUtils.join("Thread ", this.getName(), " enter run"));
        FEPBase.logMessage(Level.INFO, this.logData);
        String messageForParameter;
        Throwable processThrowable = null;
        boolean first = true; // 是否首次進入while
        while (this.running.get()) {
            this.configuration.setSocketLocal(StringUtils.EMPTY); // 因為斷線重連, 這裡記得要清掉SocketLocal
            messageForParameter = this.configuration.forLogging();
            this.logData.setStep(0); // step從0開始重新記錄
            if (!first) {
                if (processThrowable != null)
                    this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_PROCESS_WITH_EXCEPTION_OCCUR, processThrowable);
                this.closeConnection(); // 每次先把上一次的Connection關閉
                // 如果不需要自動恢復連線, 則退出線程
                if (!this.configuration.isAutoReestablishConnection())
                    break;
                if (this.configuration.getReestablishConnectionInterval() > 0) {
                    this.logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                    this.logData.setRemark(StringUtils.join("Sleep ", this.configuration.getReestablishConnectionInterval(), " milliseconds before rebuild IMS Connection, ", messageForParameter));
                    FEPBase.logMessage(Level.INFO, this.logData);
                    synchronized (this.reestablishConnectionLock) {
                        try {
                            this.reestablishConnectionLock.wait(this.configuration.getReestablishConnectionInterval());
                        } catch (InterruptedException e) {
                            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        }
                    }
                    if (!this.running.get()) {
                        break;
                    }
                }
            }
            first = false;
            // 建立IMS ConnectionFactory物件
            this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_CONNECTING);
            // 2026-07-03 Richard add ConnectionFactory只建立一次就好, 不要每次都創建
            if (this.connectionFactory == null) {
                try {
                    this.logData.setProgramName(StringUtils.join(ProgramName, ".buildConnectionFactory"));
                    this.logData.setRemark(StringUtils.join("Try to Build IMS Connection Factory..., ", messageForParameter));
                    FEPBase.logMessage(Level.INFO, this.logData);
                    this.connectionFactory = buildConnectionFactory();
                    this.logData.setProgramName(StringUtils.join(ProgramName, ".buildConnectionFactory"));
                    this.logData.setRemark(StringUtils.join("Build IMS Connection Factory Succeed, ", messageForParameter));
                    FEPBase.logMessage(Level.INFO, this.logData);
                } catch (Exception e) {
                    if (this.running.get()) {
                        this.logData.setProgramName(StringUtils.join(ProgramName, ".buildConnectionFactory"));
                        this.logData.setRemark(StringUtils.join("Build IMS Connection Factory Failed, ", messageForParameter));
                        this.logData.setProgramException(e);
                        FEPBase.sendEMS(this.logData);
                    } else {
                        this.logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                        this.logData.setRemark(StringUtils.join("Build IMS Connection Factory Failed cause Terminate, ", messageForParameter));
                        FEPBase.logMessage(Level.WARN, this.logData);
                    }
                    this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_CONNECTING_FAILED, e);
                    continue; // build失敗, 則重新嘗試連線
                }
            }
            // 建立IMS Connection物件
            // 2026-07-03 Richard marked 上面在建立ConnectionFactory時已經notify過, 這裡應該不用再notify了
            // this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_CONNECTING);
            try {
                this.logData.setProgramName(StringUtils.join(ProgramName, ".buildConnection"));
                this.logData.setRemark(StringUtils.join("Try to Build IMS Connection..., ", messageForParameter));
                FEPBase.logMessage(Level.INFO, this.logData);
                this.buildConnection(this.connectionFactory);
                messageForParameter = this.configuration.forLogging();
                this.logData.setProgramName(StringUtils.join(ProgramName, ".buildConnection"));
                this.logData.setRemark(StringUtils.join("Build IMS Connection Succeed, ", messageForParameter));
                FEPBase.logMessage(Level.INFO, this.logData);
                this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_CONNECTED);
            } catch (Exception e) {
                if (this.running.get()) {
                    this.logData.setProgramName(StringUtils.join(ProgramName, ".buildConnection"));
                    this.logData.setRemark(StringUtils.join("Build IMS Connection Failed, ", messageForParameter));
                    this.logData.setProgramException(e);
                    FEPBase.sendEMS(this.logData);
                } else {
                    this.logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                    this.logData.setRemark(StringUtils.join("Build IMS Connection Failed cause Terminate, ", messageForParameter));
                    FEPBase.logMessage(Level.WARN, this.logData);
                }
                this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_CONNECTING_FAILED, e);
                continue; // build失敗, 則重新嘗試連線
            }
            while (this.running.get()) {
                this.logData.setStep(0); // step從0開始重新記錄
                processThrowable = null;
                try {
                    // 處理業務邏輯
                    this.processRequest.doProcess(this.logData, this.connection);
                } catch (Throwable t) {
                    processThrowable = t;
                    if (this.running.get()) {
                        this.logData.setProgramName(StringUtils.join(this.processRequest.getClass().getSimpleName(), ".doProcess"));
                        this.logData.setRemark(StringUtils.join(this.processRequest.getClass().getSimpleName(), ".doProcess Failed, ", messageForParameter));
                        this.logData.setProgramException(t);
                        FEPBase.sendEMS(this.logData);
                    } else {
                        this.logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                        this.logData.setRemark(StringUtils.join(this.processRequest.getClass().getSimpleName(), ".doProcess Failed cause Terminate, ", messageForParameter));
                        FEPBase.logMessage(Level.WARN, this.logData);
                    }
                    break;
                }
            }
        }
        this.logData.setProgramName(StringUtils.join(ProgramName, ".run"));
        this.logData.setRemark(StringUtils.join("Thread ", this.getName(), " exit run"));
        FEPBase.logMessage(Level.INFO, this.logData);
    }

    /**
     * 建立IMS ConnectionFactory物件
     *
     * @return
     * @throws ImsConnectApiException
     */
    private ConnectionFactory buildConnectionFactory() throws ImsConnectApiException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHostName(this.configuration.getHost());
        connectionFactory.setPortNumber(this.configuration.getPort());
        connectionFactory.setClientId(this.configuration.getClientId());
        return connectionFactory;
    }

    /**
     * 建立IMS Connection物件
     *
     * @param connectionFactory
     * @return
     * @throws ImsConnectApiException
     * @throws SocketException
     */
    private void buildConnection(ConnectionFactory connectionFactory) throws ImsConnectApiException, SocketException {
        this.connection = connectionFactory.getConnection();
        this.connection.setSocketType(ApiProperties.SOCKET_TYPE_PERSISTENT); //長連接
        this.connection.setSocketConnectTimeout(this.configuration.getConnectTimeout());  //連線逾時,單位毫秒
        this.connection.connect();
        // 把connection物件塞給processRequest
        this.processRequest.setConnection(this.connection);
        // 透過反射取出Socket物件, 並設定給configuration的socketLocal
        this.socket = ReflectUtil.getFieldValue(this.connection, "socket", null);
        this.configuration.setSocketLocal(SocketUtil.getLocalAddress(this.socket));
    }

    /**
     * 關閉連線
     */
    private void closeConnection() {
        if (this.connection != null) {
            this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_DISCONNECTING);
            // 2026-07-05 Richard add 這裡取出Socket物件強制斷線
            if (this.configuration.isForceCloseSocket()) {
                try {
                    if (this.socket != null && !this.socket.isClosed()) {
                        this.logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                        this.logData.setRemark(StringUtils.join("Try to force to close Socket, ", this.configuration.forLogging()));
                        FEPBase.logMessage(Level.WARN, this.logData);
                        this.socket.close(); // close socket
                        this.logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                        this.logData.setRemark(StringUtils.join("Close Socket Succeed, ", this.configuration.forLogging()));
                        FEPBase.logMessage(Level.INFO, this.logData);
                    } else {
                        logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                        logData.setRemark(StringUtils.join("Close IMS Connection Already, no need to close again, ", this.configuration.forLogging()));
                        FEPBase.logMessage(Level.WARN, logData);
                    }
                } catch (IOException e) {
                    this.logData.setProgramException(e);
                    this.logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                    this.logData.setRemark(StringUtils.join("Force Close Socket Failed, ", this.configuration.forLogging()));
                    FEPBase.sendEMS(this.logData);
                }
            }
            try {
                if (this.connection.isConnected()) {
                    this.logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                    this.logData.setRemark(StringUtils.join("Try to close IMS Connection, ", this.configuration.forLogging()));
                    FEPBase.logMessage(Level.WARN, this.logData);
                    this.connection.disconnect(); // close connection
                    this.logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                    this.logData.setRemark(StringUtils.join("Close IMS Connection Succeed, ", this.configuration.forLogging()));
                    FEPBase.logMessage(Level.INFO, this.logData);
                } else {
                    logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                    logData.setRemark(StringUtils.join("Close IMS Connection Already, no need to close again, ", this.configuration.forLogging()));
                    FEPBase.logMessage(Level.WARN, logData);
                }
            } catch (ImsConnectApiException e) {
                this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_DISCONNECTING_FAILED, e);
                this.logData.setProgramException(e);
                this.logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                this.logData.setRemark(StringUtils.join("Close IMS Connection Failed, ", this.configuration.forLogging()));
                FEPBase.sendEMS(this.logData);
            } finally {
                this.connection = null;
                this.notification.notifyConnStateChanged(this.configuration, IMSTransmissionConnState.CLIENT_DISCONNECTED);
            }
        }
    }
}
