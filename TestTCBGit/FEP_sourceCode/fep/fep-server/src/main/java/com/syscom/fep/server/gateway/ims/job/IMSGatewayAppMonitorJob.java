package com.syscom.fep.server.gateway.ims.job;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.common.http.HttpClientConfigureConstant;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.frmcommon.os.OperationSystemDataCollector;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.scheduler.job.SchedulerJob;
import com.syscom.fep.server.gateway.ims.IMSGateway;
import com.syscom.fep.server.gateway.ims.IMSGatewayConnState;
import com.syscom.fep.server.gateway.ims.IMSGatewayManager;
import com.syscom.fep.server.gateway.ims.processor.IMSGatewayProcessor;
import com.syscom.fep.server.gateway.ims.processor.IMSGatewayProcessorGroup;
import com.syscom.fep.vo.monitor.ClientNetworkStatus;
import com.syscom.fep.vo.monitor.MonitorConstant;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.List;

/**
 * 定時送網絡服務端監控資料給APPMon Service
 *
 * @author Richard
 */
@Component
@ConfigurationProperties(prefix = "spring.fep.server.gateway.ims.app-monitor")
@ConditionalOnProperty(prefix = "spring.fep.server.gateway.ims.app-monitor", name = {"cronExpression", "monitorUrl"})
// @RefreshScope
public class IMSGatewayAppMonitorJob extends SchedulerJob<IMSGatewayAppMonitorJobConfig> implements MonitorConstant {
    private final int PID = OperationSystemDataCollector.getProcessID(); // 獲取當前進程的PID
    private static final List<ClientNetworkStatus> clientNetworkStatusList = new ArrayList<>();
    @Autowired
    @Qualifier(HttpClientConfigureConstant.BEAN_NAME_MONITOR)
    private HttpClient2 httpClient2;

    @Override
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
        schedulerJobManager.scheduleJob(this);
    }

    @Override
    protected void executeJob(JobExecutionContext context, IMSGatewayAppMonitorJobConfig config) throws Exception {
        try {
            IMSGatewayManager manager = SpringBeanFactoryUtil.getBean(IMSGatewayManager.class, false);
            if (manager != null) {
                // 先重置所有數據的狀態, 下面再重新塞入
                for (ClientNetworkStatus status : clientNetworkStatusList) {
                    status.setLocalEndPoint(StringUtils.EMPTY);
                    status.setSocketCount(Long.toString(0L));
                    status.setServiceState(STATUS_CODE_STOPPED);
                    status.setState(NET_CLIENT_STATE_DISCONNECT);
                    status.setPid(PID);
                }
                this.fetchMonitorData("Primary", manager.getPrimary());
                this.fetchMonitorData("Secondary", manager.getSecondary());
                httpClient2.postForEntity(config.getMonitorUrl(), MediaType.APPLICATION_JSON, clientNetworkStatusList, String.class);
            }
        } catch (Exception e) {
            warn("send App Monitor Network Data failed with exception occur = [", e.getMessage(), "]");
            sendEMS(e, "send App Monitor Network Data failed");
        }
    }

    private void fetchMonitorData(String type, IMSGateway imsGateway) {
        if (imsGateway != null) {
            String serviceState = imsGateway.getState() == IMSGatewayConnState.SHUTTING_DOWN || imsGateway.getState() == IMSGatewayConnState.SHUT_DOWN ? STATUS_CODE_STOPPED : STATUS_CODE_NORMAL;
            List<IMSGatewayProcessorGroup> gatewayGroupList = imsGateway.getProcessorGroups();
            for (int i = 0; i < gatewayGroupList.size(); i++) {
                IMSGatewayProcessorGroup group = gatewayGroupList.get(i);
                // receiver
                IMSGatewayProcessor receiver = group.getReceiver();
                if (receiver != null) {
                    String serviceName = this.getServiceName(type, "Receiver", receiver);
                    ClientNetworkStatus receiverClientNetworkStatus = clientNetworkStatusList.stream().filter(t -> t.getServiceName().equals(serviceName)).findFirst().orElse(null);
                    if (receiverClientNetworkStatus == null) {
                        receiverClientNetworkStatus = new ClientNetworkStatus();
                        receiverClientNetworkStatus.setType(MONITOR_TYPE_IMSGW_NET_CLIENT);
                        clientNetworkStatusList.add(receiverClientNetworkStatus);
                    }
                    receiverClientNetworkStatus.setServiceHostName(FEPConfig.getInstance().getHostName());
                    receiverClientNetworkStatus.setServiceIP(FEPConfig.getInstance().getHostIp());
                    receiverClientNetworkStatus.setServiceName(serviceName);
                    receiverClientNetworkStatus.setLocalEndPoint(receiver.getLocalEndPoint());
                    receiverClientNetworkStatus.setRemoteEndPoint(this.getRemoteEndPoint(receiver));
                    receiverClientNetworkStatus.setSocketCount(IMSGatewayConnState.isConnected(receiver.getCurrentConnState()) ? "1" : "0");
                    receiverClientNetworkStatus.setState(IMSGatewayConnState.isConnected(receiver.getCurrentConnState()) ? NET_CLIENT_STATE_CONNECT : NET_CLIENT_STATE_DISCONNECT);
                    receiverClientNetworkStatus.setServiceState(serviceState);
                    receiverClientNetworkStatus.setPid(PID);
                }
                // sender
                IMSGatewayProcessor sender = group.getSender();
                if (sender != null) {
                    String serviceName = this.getServiceName(type, "Sender", sender);
                    ClientNetworkStatus senderClientNetworkStatus = clientNetworkStatusList.stream().filter(t -> t.getServiceName().equals(serviceName)).findFirst().orElse(null);
                    if (senderClientNetworkStatus == null) {
                        senderClientNetworkStatus = new ClientNetworkStatus();
                        senderClientNetworkStatus.setType(MONITOR_TYPE_IMSGW_NET_CLIENT);
                        clientNetworkStatusList.add(senderClientNetworkStatus);
                    }
                    senderClientNetworkStatus.setServiceHostName(FEPConfig.getInstance().getHostName());
                    senderClientNetworkStatus.setServiceIP(FEPConfig.getInstance().getHostIp());
                    senderClientNetworkStatus.setServiceName(serviceName);
                    senderClientNetworkStatus.setLocalEndPoint(sender.getLocalEndPoint());
                    senderClientNetworkStatus.setRemoteEndPoint(this.getRemoteEndPoint(sender));
                    senderClientNetworkStatus.setSocketCount(IMSGatewayConnState.isConnected(sender.getCurrentConnState()) ? "1" : "0");
                    senderClientNetworkStatus.setState(IMSGatewayConnState.isConnected(sender.getCurrentConnState()) ? NET_CLIENT_STATE_CONNECT : NET_CLIENT_STATE_DISCONNECT);
                    senderClientNetworkStatus.setServiceState(serviceState);
                    senderClientNetworkStatus.setPid(PID);
                }
            }
        }
    }

    private String getServiceName(String type, String socketType, IMSGatewayProcessor processor) {
        StringBuilder sb = new StringBuilder();
        sb.append("IMSGW").append(StringUtils.SPACE)
                .append(type).append(StringUtils.SPACE)
                .append(socketType)
                .append("(").append(processor.getClientId()).append(")");
        return sb.toString();
    }

    private String getRemoteEndPoint(IMSGatewayProcessor processor) {
        return StringUtils.join(processor.getHostName(), ":", processor.getHostPort());
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

    private void warn(Object... messages) {
        if (this.getJobConfig().isRecordHttpLog()) {
            ScheduleLogger.warn(messages);
        }
    }
}
