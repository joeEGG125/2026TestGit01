package com.syscom.fep.server.gateway.ims.keepalive;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.server.gateway.ims.IMSGatewayConfiguration;
import com.syscom.fep.server.gateway.ims.IMSGatewayConst;
import com.syscom.fep.server.gateway.ims.IMSGatewayManager;
import com.syscom.fep.server.gateway.ims.IMSGatewayMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * 用來發送KeepAlive訊息到另一台IMSGW
 *
 * @author Richard
 */
@StackTracePointCut(caller = SvrConst.SVR_IMS_GATEWAY)
public class IMSGatewayKeepAliveRestfulClient extends FEPBase {
    @Autowired
    private IMSGatewayKeepAliveClientConfiguration configuration;
    @Autowired
    private IMSGatewayConfiguration imsGatewayConfiguration;
    @Autowired
    private IMSGatewayManager imsGatewayManager;
    private SendKeepAliveTimer sendKeepAliveTimer;
    private final HttpClient2 httpClient2 = new HttpClient2();

    @PostConstruct
    public void init() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        logContext.setChannel(FEPChannel.FEP);
        logContext.setProgramFlowType(ProgramFlow.IMSGWIn);
        logContext.setMessageFlowType(MessageFlow.Request);
        logContext.setProgramName(StringUtils.join(ProgramName, ".init"));
        logContext.setRemark("KeepAlive Restful Client initialize");
        this.logMessage(logContext);
        this.run();
    }

    @PreDestroy
    public void destroy() {
        this.terminateConnection();
        this.httpClient2.destroy();
    }

    public synchronized void run() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        logContext.clear();
        if (configuration.getStart() > 0) {
            if (sendKeepAliveTimer == null) {
                logContext.setChannel(FEPChannel.FEP);
                logContext.setProgramFlowType(ProgramFlow.IMSGWIn);
                logContext.setMessageFlowType(MessageFlow.Request);
                logContext.setProgramName(StringUtils.join(ProgramName, ".run"));
                logContext.setRemark("KeepAlive Restful Client Timer start to run");
                logMessage(logContext);
                sendKeepAliveTimer = new SendKeepAliveTimer();
            } else {
                logContext.setChannel(FEPChannel.FEP);
                logContext.setProgramFlowType(ProgramFlow.IMSGWIn);
                logContext.setMessageFlowType(MessageFlow.Request);
                logContext.setProgramName(StringUtils.join(ProgramName, ".run"));
                logContext.setRemark("KeepAlive Restful Client Timer already running");
                logMessage(Level.WARN, logContext);
            }
        } else {
            logContext.setChannel(FEPChannel.FEP);
            logContext.setProgramFlowType(ProgramFlow.IMSGWIn);
            logContext.setMessageFlowType(MessageFlow.Request);
            logContext.setProgramName(StringUtils.join(ProgramName, ".run"));
            logContext.setRemark("KeepAlive Restful Client Timer cannot start to run, cause the 'start' property in config file is <= 0");
            logMessage(Level.WARN, logContext);
        }
    }

    public synchronized void terminateConnection() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        logContext.clear();
        if (configuration.getStart() > 0) {
            if (sendKeepAliveTimer != null) {
                logContext.setChannel(FEPChannel.FEP);
                logContext.setProgramFlowType(ProgramFlow.IMSGWIn);
                logContext.setMessageFlowType(MessageFlow.Request);
                logContext.setProgramName(StringUtils.join(ProgramName, ".terminateConnection"));
                logContext.setRemark("KeepAlive Restful Client Timer was terminated");
                logMessage(Level.WARN, logContext);
                sendKeepAliveTimer.terminate();
                sendKeepAliveTimer = null;
            } else {
                logContext.setChannel(FEPChannel.FEP);
                logContext.setProgramFlowType(ProgramFlow.IMSGWIn);
                logContext.setMessageFlowType(MessageFlow.Request);
                logContext.setProgramName(StringUtils.join(ProgramName, ".terminateConnection"));
                logContext.setRemark("KeepAlive Restful Client Timer already terminated");
                logMessage(Level.WARN, logContext);
            }
        }
    }

    private class SendKeepAliveTimer extends Thread {
        private boolean running = true;
        private final Object lock = new Object();
        // 經過retry後依舊失敗, 設定為true, 則後面再retry時, 暫時不要記錄log, 避免log太多, 一直到送KeepAlive成功則再設定為false
        private boolean retryFailed = false;

        public SendKeepAliveTimer() {
            super(StringUtils.join("SendKeepAliveTimer"));
            start();
        }

        public void terminate() {
            running = false;
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        /**
         * Execute Task
         */
        @Override
        public void run() {
            LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
            // 程序啟動後, 等待configuration.getStart()秒後才開始執行
            if (configuration.getStart() > 0) {
                synchronized (lock) {
                    try {
                        lock.wait(configuration.getStart() * 1000L);
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                }
            }
            int retryCount = 0;
            boolean runSecondary = true;
            while (running) {
                // 開始新的log記錄
                logContext.clear();
                // 先送一次keepalive電文
                // 如果送keepalive電文失敗, 則進入retry流程
                if (!sendReceive(0, false)) {
                    // 如果首次發送失敗, 則wait timeout秒再繼續送
                    synchronized (lock) {
                        try {
                            lock.wait(configuration.getTimeout() * 1000L);
                        } catch (InterruptedException e) {
                            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        }
                    }
                    runSecondary = true;
                    retryCount = 0;
                    // 一共嘗試configuration.getRetryCount()次
                    while (running && retryCount++ < configuration.getRetryCount()) {
                        // 嘗試發送
                        if (sendReceive(retryCount, retryCount == configuration.getRetryCount())) {
                            runSecondary = false; // retry成功, 則不需要啟動secondary
                            if (!retryFailed) {
                                logContext.setChannel(FEPChannel.FEP);
                                logContext.setProgramFlowType(ProgramFlow.IMSGWIn);
                                logContext.setMessageFlowType(MessageFlow.Request);
                                logContext.setProgramName(StringUtils.join(ProgramName, ".SendKeepAliveTimer.run"));
                                logContext.setRemark(StringUtils.join("KeepAlive Restful Client Timer Retry to Send Keep Alive Succeed via url:", configuration.getHttpKeepAlive(), ", times:", retryCount));
                                logMessage(logContext);
                            }
                            retryFailed = false;
                            break;
                        } else {
                            // retry的次數用完, 則不要wait了
                            if (running && retryCount < configuration.getRetryCount()) {
                                // 如果失敗則每次wait retry interval秒再繼續送
                                synchronized (lock) {
                                    try {
                                        lock.wait(configuration.getRetryInterval() * 1000L);
                                    } catch (InterruptedException e) {
                                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                    // 上面retry失敗, 則啟動secondary
                    if (running && runSecondary) {
                        if (!retryFailed) {
                            logContext.setChannel(FEPChannel.FEP);
                            logContext.setProgramFlowType(ProgramFlow.IMSGWIn);
                            logContext.setMessageFlowType(MessageFlow.Request);
                            logContext.setProgramName(StringUtils.join(ProgramName, ".SendKeepAliveTimer.run"));
                            logContext.setRemark(StringUtils.join("KeepAlive Restful Client Timer Still no KeepAlive Response received and Start to run Secondary Line, url:", configuration.getHttpKeepAlive(), ",times:", configuration.getRetryCount()));
                            logMessage(Level.WARN, logContext);
                        }
                        imsGatewayManager.runGateway(IMSGatewayMode.secondary, imsGatewayConfiguration);
                        // 2024-10-29 Richard add 啟動Secondary之後, 則終止KeepAlive
                        terminateConnection();
                        retryFailed = true;
                    }
                } else {
                    retryFailed = false;
                }
                if (running) {
                    // 每次wait interval秒再繼續送
                    synchronized (lock) {
                        try {
                            lock.wait(configuration.getInterval() * 1000L);
                        } catch (InterruptedException e) {
                            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        }
                    }
                }
            }
        }

        private boolean sendReceive(int times, boolean finalTimes) {
            LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
            boolean logging = (configuration.isLogging() || times > 0) && !retryFailed;
            if (logging) {
                logContext.setChannel(FEPChannel.FEP);
                logContext.setProgramFlowType(ProgramFlow.IMSGWOut);
                logContext.setMessageFlowType(MessageFlow.Request);
                logContext.setProgramName(StringUtils.join(ProgramName, ".SendKeepAliveTimer.sendReceive"));
                logContext.setMessage(IMSGatewayConst.KeepAliveRequest);
                logContext.setRemark(StringUtils.join(times > 0 ? "Retry" : "Ready", " to Send Keep Alive via url:", configuration.getHttpKeepAlive(), times > 0 ? ", times:" + times : StringUtils.EMPTY));
                logMessage(times > 0 ? Level.WARN : Level.DEBUG, logContext);
            }
            try {
                Map<String, String> args = new HashMap<>();
                args.put("message", IMSGatewayConst.KeepAliveRequest);
                httpClient2.getConfig().setRecordLog(logging);
                String response = httpClient2.postForObject(configuration.getHttpKeepAlive(), MediaType.APPLICATION_FORM_URLENCODED, args, String.class);
                if (logging) {
                    logContext.setChannel(FEPChannel.FEP);
                    logContext.setProgramFlowType(ProgramFlow.IMSGWIn);
                    logContext.setMessageFlowType(MessageFlow.Response);
                    logContext.setProgramName(StringUtils.join(ProgramName, ".SendKeepAliveTimer.sendReceive"));
                    logContext.setMessage(response);
                    logContext.setRemark(StringUtils.join("Receive Keep Alive ack via url:", configuration.getHttpKeepAlive(), times > 0 ? ", times:" + times : StringUtils.EMPTY));
                    logMessage(times > 0 ? Level.WARN : Level.DEBUG, logContext);
                }
                return IMSGatewayConst.KeepAliveResponse.equals(response);
            } catch (Throwable e) {
                if (logging) {
                    logContext.setChannel(FEPChannel.FEP);
                    logContext.setProgramFlowType(ProgramFlow.IMSGWIn);
                    logContext.setMessageFlowType(MessageFlow.Response);
                    logContext.setProgramName(StringUtils.join(ProgramName, ".SendKeepAliveTimer.sendReceive"));
                    logContext.setRemark(StringUtils.join(times > 0 ? "Retry to " : StringUtils.EMPTY, "Send Keep Alive failed via url:", configuration.getHttpKeepAlive(), times > 0 ? ", times:" + times : StringUtils.EMPTY));
                    logContext.setProgramException(e);
                    if (finalTimes) { // 最後一次嘗試失敗, 則發送EMS
                        sendEMS(logContext);
                    } else {
                        logMessage(Level.WARN, logContext);
                    }
                }
            }
            return false;
        }
    }
}
