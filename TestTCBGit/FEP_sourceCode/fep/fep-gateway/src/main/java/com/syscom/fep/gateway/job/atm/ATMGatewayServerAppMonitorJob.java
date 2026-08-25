package com.syscom.fep.gateway.job.atm;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.common.http.HttpClientConfigureConstant;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.frmcommon.os.OperationSystemDataCollector;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.AtmStatus;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.netty.NettyTransmissionServerMonitor;
import com.syscom.fep.gateway.netty.atm.ATMGatewayServer;
import com.syscom.fep.gateway.netty.atm.ATMGatewayServerConfiguration;
import com.syscom.fep.scheduler.job.SchedulerJob;
import com.syscom.fep.vo.communication.ToATMCommuAtmstatList;
import com.syscom.fep.vo.monitor.ServerNetworkStatus;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;

import jakarta.annotation.PreDestroy;

import java.util.Collections;

/**
 * 定時送網絡服務端監控資料給APPMon Service
 */
public class ATMGatewayServerAppMonitorJob extends SchedulerJob<ATMGatewayServerAppMonitorJobConfig> {
    private final int PID = OperationSystemDataCollector.getProcessID(); // 獲取當前進程的PID
    private static final ServerNetworkStatus serverNetworkStatus = new ServerNetworkStatus();
    @Autowired
    @Qualifier(HttpClientConfigureConstant.BEAN_NAME_MONITOR)
    private HttpClient2 httpClient2;

    @Override
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.ATMGW.name());
    }

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, ATMGatewayServerAppMonitorJobConfig config) throws Exception {
        try {
            ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class, false);
            if (atmGatewayServer == null) return;
            NettyTransmissionServerMonitor<ATMGatewayServerConfiguration> monitorData = atmGatewayServer.getTransmissionServerMonitor();
            serverNetworkStatus.setServiceHostName(FEPConfig.getInstance().getHostName());
            serverNetworkStatus.setServiceName(monitorData.getServiceName());
            serverNetworkStatus.setServiceIP(monitorData.getHostIp());
            serverNetworkStatus.setServicePort(Integer.toString(monitorData.getHostPort()));
            // 2025-04-30 Richard modified for 取socket連線數改為透過ATM Service從db中取
            // serverNetworkStatus.setSocketCount(Long.toString(monitorData.getConnections()));
            ToATMCommuAtmstatList toATMCommuAtmstatList = atmGatewayServer.getAtmstatList(AtmStatus.Connected, true); // 只取Count數
            serverNetworkStatus.setSocketCount(Long.toString(toATMCommuAtmstatList.getCount()));
            serverNetworkStatus.setServiceState("1");
            serverNetworkStatus.setPid(PID);
            httpClient2.postForEntity(config.getMonitorUrl(), MediaType.APPLICATION_JSON, Collections.singletonList(serverNetworkStatus), String.class);
        } catch (Exception e) {
            warn("send App Monitor Network Data failed with exception occur = [", e.getMessage(), "]");
            sendEMS(e, "send App Monitor Network Data failed");
        }
    }

    @PreDestroy
    public void terminateJob() {
        try {
            serverNetworkStatus.setSocketCount(Long.toString(0L));
            serverNetworkStatus.setServiceState("0");
            serverNetworkStatus.setPid(null);
            httpClient2.postForEntity(this.getJobConfig().getMonitorUrl(), MediaType.APPLICATION_JSON, Collections.singletonList(serverNetworkStatus), String.class);
        } catch (Exception e) {
            warn("send App Monitor Network Data failed with exception occur = [", e.getMessage(), "]");
            sendEMS(e, "send App Monitor Network Data failed");
        }
    }

    private void warn(Object... messages) {
        if (this.getJobConfig().isRecordHttpLog()) {
            ScheduleLogger.warn(messages);
        }
    }
}
