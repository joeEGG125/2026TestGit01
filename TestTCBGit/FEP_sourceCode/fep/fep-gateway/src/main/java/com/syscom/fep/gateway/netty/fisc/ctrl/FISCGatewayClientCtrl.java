package com.syscom.fep.gateway.netty.fisc.ctrl;

import com.google.gson.Gson;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.netty.NettyTransmissionClientMonitor;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.fisc.*;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiver;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverConfiguration;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSender;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderConfiguration;
import com.syscom.fep.gateway.netty.fisc.keepalive.FISCGatewayKeepAliveClient;
import com.syscom.fep.gateway.netty.fisc.keepalive.FISCGatewayKeepAliveRestfulClient;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class FISCGatewayClientCtrl extends FEPBase {
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.FISCGW.name());
    }

    /**
     * 獲取監控數據
     *
     * @return
     */
    protected String getMonitorData() {
        putMDC();
        FISCGatewayManager manager = SpringBeanFactoryUtil.getBean(FISCGatewayManager.class, false);
        if (manager == null) {
            return null;
        }
        LogData logData = new LogData();
        logData.clear();
        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".getMonitorData"));
        logData.setRemark("fetch Monitor Data");
        this.logMessage(logData);
        StringBuilder sb = new StringBuilder();
        this.fetchMonitorData(sb, "Primary", manager.getPrimaryGatewayGroupList());
        this.fetchMonitorData(sb, "Secondary", manager.getSecondaryGatewayGroupList());
        logData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramName(StringUtils.join(ProgramName, ".getMonitorData"));
        logData.setRemark(StringUtils.join("Response Data:\r\n", sb.toString()));
        this.logMessage(logData);
        return sb.toString();
    }

    private void fetchMonitorData(StringBuilder sb, String type, List<FISCGatewayGroup> gatewayGroupList) {
        if (!gatewayGroupList.isEmpty()) {
            for (FISCGatewayGroup group : gatewayGroupList) {
                // receiver
                FISCGatewayClientReceiver receiver = group.getReceiver();
                if (receiver != null) {
                    NettyTransmissionClientMonitor<FISCGatewayClientReceiverConfiguration> receiverMonitorData = receiver.getTransmissionClientMonitor();
                    sb.append(type).append(StringUtils.SPACE).append(receiverMonitorData.getServiceName()).append("(").append(FEPConfig.getInstance().getHostIp()).append(")").append(":\r\n");
                    sb.append("\tCurrent Connections:").append(FormatUtil.longFormat(receiverMonitorData.getConnections())).append("\r\n");
                    sb.append("\tConnection Status:").append(receiverMonitorData.getConnState().getDescription()).append("\r\n");
                    sb.append("\tLocal:").append(receiverMonitorData.getLocal()).append("\r\n");
                    sb.append("\tRemote:").append(receiverMonitorData.getRemote()).append("\r\n");
                }
                // sender
                FISCGatewayClientSender sender = group.getSender();
                if (sender != null) {
                    NettyTransmissionClientMonitor<FISCGatewayClientSenderConfiguration> senderMonitorData = sender.getTransmissionClientMonitor();
                    sb.append(type).append(StringUtils.SPACE).append(senderMonitorData.getServiceName()).append("(").append(FEPConfig.getInstance().getHostIp()).append(")").append(":\r\n");
                    sb.append("\tCurrent Connections:").append(FormatUtil.longFormat(senderMonitorData.getConnections())).append("\r\n");
                    sb.append("\tConnection Status:").append(senderMonitorData.getConnState().getDescription()).append("\r\n");
                    sb.append("\tLocal:").append(senderMonitorData.getLocal()).append("\r\n");
                    sb.append("\tRemote:").append(senderMonitorData.getRemote()).append("\r\n");
                }
            }
        }
    }

    /**
     * 操控GW
     * <p>
     * 4.FISCGW增加接收指令的功能, 如下:
     * Start FISCGW Primary   (啟動FISCGW的Primary腳位)
     * Stop FISCGW Primary   (停止FISCGW的Primary腳位))
     * Start FISCGW Secondary   (啟動FISCGW的Secondary腳位, 並停止keepalive動作)
     * Stop FISCGW Secondary   (停止FISCGW的Secondary腳位, 經過keepalive.start秒後開始進行keepalive電文發送)
     * 執行命令結果, 若成功啟動/停止, 則顯示 FISCGW Start/Stop Primary/Secondary port OK!
     * 若目前該腳位已啟動或停止, 則顯示FISCGW Primary/Secondary port has started/stopped already!
     *
     * @param mode
     * @param action
     * @param respType
     * @return
     */
    protected String doOperateGateway(FISCGatewayMode mode, FISCGatewayCmdAction action, FISCGatewayRespType respType) {
        putMDC();
        FISCGatewayManager manager = SpringBeanFactoryUtil.getBean(FISCGatewayManager.class, false);
        if (manager == null) {
            return null;
        }
        LogData logData = new LogData();
        logData.clear();
        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".doOperateGateway"));
        logData.setRemark(StringUtils.join("Operate FISCGateway, mode:", mode.name(), ", action:", action));
        this.logMessage(logData);
        String response = StringUtils.EMPTY;
        boolean result = false;
        if (mode == FISCGatewayMode.all) {
            StringBuilder sb = new StringBuilder();
            sb.append(StringUtils.substring(this.doOperateGateway(FISCGatewayMode.primary, action, respType), 1, -1));
            sb.append(respType == null ? "\r\n" : ",");
            sb.append(StringUtils.substring(this.doOperateGateway(FISCGatewayMode.secondary, action, respType), 1, -1));
            if (respType == FISCGatewayRespType.JSON)
                sb.insert(0, "[").append("]");
            response = sb.toString();
            result = true;
        } else {
            switch (mode) {
                case primary:
                    if (action == FISCGatewayCmdAction.start) {
                        if (manager.runPrimaryGateway(false)) {
                            response = "FISC Gateway Start Primary port OK!";
                        } else {
                            response = "FISC Gateway Primary port has started already!";
                        }
                    } else if (action == FISCGatewayCmdAction.stop) {
                        if (manager.stopPrimaryGateway()) {
                            response = "FISC Gateway Stop Primary port OK!";
                        } else {
                            response = "FISC Gateway Primary port has stopped already!";
                        }
                    }
                    result = true;
                    break;
                case secondary:
                    try {
                        if (action == FISCGatewayCmdAction.start) {
                            if (manager.runSecondaryGateway(false)) {
                                response = "FISC Gateway Start Secondary port OK!";
                            } else {
                                response = "FISC Gateway Secondary port has started already!";
                            }
                        } else if (action == FISCGatewayCmdAction.stop) {
                            if (manager.stopSecondaryGateway()) {
                                response = "FISC Gateway Stop Secondary port OK!";
                            } else {
                                response = "FISC Gateway Secondary port has stopped already!";
                            }
                        }
                    } finally {
                        if (action == FISCGatewayCmdAction.start) {
                            this.doOperateKeepAliveClient(FISCGatewayCmdAction.stop);
                        } else if (action == FISCGatewayCmdAction.stop) {
                            this.doOperateKeepAliveClient(FISCGatewayCmdAction.start);
                        }
                    }
                    result = true;
                    break;
                default:
                    response = StringUtils.join("Incorrect parameter \"mode\" = ", mode);
                    break;
            }
            if (respType == FISCGatewayRespType.JSON) {
                FISCGatewayResp resp = new FISCGatewayResp();
                resp.setMode(mode);
                resp.setAction(action);
                resp.setResult(result);
                resp.setMessage(response);
                response = new Gson().toJson(new FISCGatewayResp[] {resp});
            }
        }
        logData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramName(StringUtils.join(ProgramName, ".doOperateGateway"));
        logData.setRemark(StringUtils.join("Operate FISCGateway and Get Response, mode:", mode.name(), ", action:", action, ", response:", response));
        this.logMessage(logData);
        return response;
    }

    /**
     * 操控送KeepAlive電文的Client物件
     *
     * @param action
     */
    private void doOperateKeepAliveClient(FISCGatewayCmdAction action) {
        // 2024-09-14 Richard modified for KeepAlive改用Restful
        // FISCGatewayKeepAliveClient client = SpringBeanFactoryUtil.getBean(FISCGatewayKeepAliveClient.class, false);
        FISCGatewayKeepAliveRestfulClient client = SpringBeanFactoryUtil.getBean(FISCGatewayKeepAliveRestfulClient.class, false);
        if (client == null) {
            return;
        }
        LogData logData = new LogData();
        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".doOperateKeepAliveClient"));
        logData.setRemark(StringUtils.join("Operate FISCGateway KeepAlive Client, action:", action));
        this.logMessage(logData);
        // 僅在shutdown的狀態下, 才可以start
        if (action == FISCGatewayCmdAction.start && client.getTransmissionClientMonitor().getConnState() == NettyTransmissionConnState.CLIENT_SHUT_DOWN) {
            client.run();
        }
        // 只要不是shutdown的狀態, 就可以終止
        else if (action == FISCGatewayCmdAction.stop && client.getTransmissionClientMonitor().getConnState() != NettyTransmissionConnState.CLIENT_SHUT_DOWN) {
            client.terminateConnection();
        }
    }

    /**
     * 可以透過command下以下指令給FISCGW:
     * checkStatus: 顯示目前各腳位的連線狀態, 如下:
     * fepap1 : primary FISC(B889A01I) 172.X.X.X:5001 Connected
     * fepap1 : primary FISC(B889A01O) 172.X.X.X:5002 Connected
     * 若有接手secondary線路(因heartbeat自動接手或曾下過start Secondary指令), 則顯示以下訊息, 若無 則不顯示
     * fepap1 : secondary FISC(B889A02I) 172.X.X.X:5003 Connected
     * fepap1 : secondary FISC(B889A02O) 172.X.X.X:5004 Connected
     *
     * @return
     */
    protected String checkStatus() {
        putMDC();
        FISCGatewayManager manager = SpringBeanFactoryUtil.getBean(FISCGatewayManager.class, false);
        if (manager == null) {
            return null;
        }
        LogData logData = new LogData();
        logData.clear();
        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".checkStatus"));
        logData.setRemark("check status");
        this.logMessage(logData);
        String rtn = manager.checkStatus();
        logData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramName(StringUtils.join(ProgramName, ".checkStatus"));
        logData.setRemark(StringUtils.join("Response Data:\r\n", rtn));
        this.logMessage(logData);
        return rtn;
    }
}
