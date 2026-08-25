package com.syscom.fep.gateway.job.cbs;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.common.http.HttpClientConfigureConstant;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.frmcommon.os.OperationSystemDataCollector;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.ims.IMSTransmission;
import com.syscom.fep.gateway.ims.IMSTransmissionConfiguration;
import com.syscom.fep.gateway.ims.IMSTransmissionConnState;
import com.syscom.fep.gateway.ims.IMSTransmissionMonitor;
import com.syscom.fep.gateway.ims.cbs.CBSGatewayClientConfiguration;
import com.syscom.fep.gateway.ims.cbs.CBSGatewayGroup;
import com.syscom.fep.gateway.ims.cbs.CBSGatewayManager;
import com.syscom.fep.gateway.ims.cbs.receiver.CBSGatewayClientReceiver;
import com.syscom.fep.gateway.ims.cbs.sender.CBSGatewayClientSender;
import com.syscom.fep.scheduler.job.SchedulerJob;
import com.syscom.fep.vo.monitor.ClientNetworkStatus;
import com.syscom.fep.vo.monitor.MonitorConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * 定時送網絡服務端監控資料給APPMon Service
 */
public class CBSGatewayAppMonitorJob extends SchedulerJob<CBSGatewayAppMonitorJobConfig> implements MonitorConstant {
    private final int PID = OperationSystemDataCollector.getProcessID(); // 獲取當前進程的PID
    private static final List<ClientNetworkStatus> clientNetworkStatusList = new ArrayList<>();
    @Autowired
    @Qualifier(HttpClientConfigureConstant.BEAN_NAME_MONITOR)
    private HttpClient2 httpClient2;

    @Override
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.CBSGW.name());
    }

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, CBSGatewayAppMonitorJobConfig config) throws Exception {
        try {
            CBSGatewayManager manager = SpringBeanFactoryUtil.getBean(CBSGatewayManager.class, false);
            if (manager != null) {
                // 先重置所有數據的狀態, 下面再重新塞入
                for (ClientNetworkStatus status : clientNetworkStatusList) {
                    status.setLocalEndPoint(StringUtils.EMPTY);
                    status.setSocketCount(Long.toString(0L));
                    status.setServiceState(STATUS_CODE_STOPPED);
                    status.setState(NET_CLIENT_STATE_DISCONNECT);
                    status.setPid(PID);
                }
                this.fetchMonitorData(manager.getGatewayGroups());
                httpClient2.postForEntity(config.getMonitorUrl(), MediaType.APPLICATION_JSON, clientNetworkStatusList, String.class);
            }
        } catch (Exception e) {
            warn("send App Monitor Network Data failed with exception occur = [", e.getMessage(), "]");
            sendEMS(e, "send App Monitor Network Data failed");
        }
    }

    @PreDestroy
    public void terminateJob() {
        try {
            for (ClientNetworkStatus status : clientNetworkStatusList) {
                status.setLocalEndPoint(StringUtils.EMPTY);
                status.setSocketCount(Long.toString(0L));
                status.setServiceState(STATUS_CODE_STOPPED);
                status.setState(NET_CLIENT_STATE_DISCONNECT);
                status.setPid(null);
            }
            httpClient2.postForEntity(this.getJobConfig().getMonitorUrl(), MediaType.APPLICATION_JSON, clientNetworkStatusList, String.class);
        } catch (Exception e) {
            warn("send App Monitor Network Data failed with exception occur = [", e.getMessage(), "]");
            sendEMS(e, "send App Monitor Network Data failed");
        }
    }

    private void fetchMonitorData(List<CBSGatewayGroup> gatewayGroupList) {
        if (!gatewayGroupList.isEmpty()) {
            for (int i = 0; i < gatewayGroupList.size(); i++) {
                CBSGatewayGroup group = gatewayGroupList.get(i);
                // receiver
                List<CBSGatewayClientReceiver> receiverList = group.getReceivers();
                if (CollectionUtils.isNotEmpty(receiverList)) {
                    receiverList.forEach(this::fetchClientNetworkStatus);
                }
                // sender
                List<CBSGatewayClientSender> senderList = group.getSenders();
                if (CollectionUtils.isNotEmpty(senderList)) {
                    senderList.forEach(this::fetchClientNetworkStatus);
                }
            }
        }
    }

    private void fetchClientNetworkStatus(IMSTransmission<?, ?> imsTransmission) {
        if (imsTransmission == null)
            return;
        IMSTransmissionMonitor<?> monitorData = imsTransmission.getIMSTransmissionMonitor();
        String serviceName = this.getServiceName(imsTransmission.getConfiguration());
        ClientNetworkStatus clientNetworkStatus = clientNetworkStatusList.stream().filter(t -> t.getServiceName().equals(serviceName)).findFirst().orElse(null);
        if (clientNetworkStatus == null) {
            clientNetworkStatus = new ClientNetworkStatus();
            clientNetworkStatus.setType(MONITOR_TYPE_CBSGW_NET_CLIENT);
            clientNetworkStatusList.add(clientNetworkStatus);
        }
        clientNetworkStatus.setServiceHostName(FEPConfig.getInstance().getHostName());
        clientNetworkStatus.setServiceIP(FEPConfig.getInstance().getHostIp());
        clientNetworkStatus.setServiceName(serviceName);
        clientNetworkStatus.setLocalEndPoint(monitorData.getLocal());
        clientNetworkStatus.setRemoteEndPoint(monitorData.getRemote());
        clientNetworkStatus.setSocketCount(Long.toString(monitorData.getConnections()));
        clientNetworkStatus.setState(IMSTransmissionConnState.isClientConnected(monitorData.getConnState()) ? NET_CLIENT_STATE_CONNECT : NET_CLIENT_STATE_DISCONNECT);
        clientNetworkStatus.setServiceState(STATUS_CODE_NORMAL);
        clientNetworkStatus.setPid(PID);
    }

    private String getServiceName(IMSTransmissionConfiguration configuration) {
        StringBuilder sb = new StringBuilder();
        if (configuration instanceof CBSGatewayClientConfiguration) {
            sb.append(((CBSGatewayClientConfiguration) configuration).getLineType().name()).append(StringUtils.SPACE);
        }
        sb.append(configuration.getGateway().name()).append(StringUtils.SPACE)
                .append(configuration.getSocketType().name())
                .append("(").append(configuration.getClientId()).append(")");
        return sb.toString();
    }

    private void warn(Object... messages) {
        if (this.getJobConfig().isRecordHttpLog()) {
            ScheduleLogger.warn(messages);
        }
    }
}