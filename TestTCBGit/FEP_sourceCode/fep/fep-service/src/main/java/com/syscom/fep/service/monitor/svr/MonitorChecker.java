package com.syscom.fep.service.monitor.svr;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class MonitorChecker extends Thread {
    private final LogHelper logger = LogHelperFactory.getTraceLogger();
    private boolean running = true;
    private final RefBoolean receiveMonitorDataTimeoutLock = new RefBoolean(Boolean.TRUE);
    @Autowired
    private MonitorCheckerConfiguration configuration;

    @PostConstruct
    public void init() {
        setName("MonitorChecker");
        this.start();
    }

    @PreDestroy
    public void terminate() {
        this.running = false;
    }

    public void monitorDataArrived() {
        synchronized (this.receiveMonitorDataTimeoutLock) {
            this.receiveMonitorDataTimeoutLock.set(false);
            this.receiveMonitorDataTimeoutLock.notifyAll();
        }
    }

    @Override
    public void run() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
        while (this.running) {
            boolean sendEMS = false;
            synchronized (this.receiveMonitorDataTimeoutLock) {
                receiveMonitorDataTimeoutLock.set(true);
                try {
                    this.receiveMonitorDataTimeoutLock.wait(configuration.getReceiveMonitorDataTimeout());
                } catch (InterruptedException e) {
                    logger.warn(e, e.getMessage());
                }
                if (receiveMonitorDataTimeoutLock.get()) {
                    sendEMS = true;
                }
            }
            if (sendEMS) {
                LogData logData = new LogData();
                logData.setProgramName("MonitorChecker.run()");
                logData.setRemark(StringUtils.join("Cannot receive any Monitor Data in ", configuration.getReceiveMonitorDataTimeout(), " milliseconds."));
                FEPBase.sendEMS(logData);
            }
        }
    }
}
