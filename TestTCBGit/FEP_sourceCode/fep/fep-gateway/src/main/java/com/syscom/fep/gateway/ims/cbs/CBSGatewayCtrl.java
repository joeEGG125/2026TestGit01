package com.syscom.fep.gateway.ims.cbs;

import com.google.gson.Gson;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.ims.IMSTransmissionConnState;
import com.syscom.fep.gateway.ims.cbs.receiver.CBSGatewayClientReceiver;
import com.syscom.fep.gateway.ims.cbs.receiver.CBSGatewayClientReceiverConfiguration;
import com.syscom.fep.gateway.ims.cbs.sender.CBSGatewayClientSender;
import com.syscom.fep.gateway.ims.cbs.sender.CBSGatewayClientSenderConfiguration;
import com.syscom.fep.gateway.netty.cbs.server.CBSGatewayServerConfiguration;
import com.syscom.fep.vo.communication.ToCBSChangeLineStatusAction;
import com.syscom.fep.vo.communication.ToFEPCBSGetAllLineStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

public class CBSGatewayCtrl extends FEPBase {

    private void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.CBSGW.name());
    }

    /**
     * WebAPI供fepweb呼叫取得各線路的狀態, 取得各線路目前的連線狀態及是否啟用(Enable屬性), 照以下json格式回傳前端
     * {
     * "HostName": "fepap1T",
     * "CBSTypes": [
     * {
     * "TypeName": "CBS",
     * "Lines": [
     * {
     * "SClientId": "IFEPTA01",
     * "RClientId": "IFEPTB01",
     * "Enable": "true",
     * "IsConnected": "true",
     * "LineType": "primary"
     * },
     * {
     * "SClientId": "IFEPTC01",
     * "RClientId": "IFEPTD01",
     * "Enable": "true",
     * "IsConnected": "true",
     * "LineType": "alternative"
     * }
     * ]
     * },
     * {
     * "TypeName": "FISC",
     * "Lines": [
     * {
     * "SClientId": "IFEPTA11",
     * "RClientId": "IFEPTB11",
     * "Enable": "true",
     * "IsConnected": "true",
     * "LineType": "primary"
     * },
     * {
     * "SClientId": "IFEPTC11",
     * "RClientId": "IFEPTD11",
     * "Enable": "true",
     * "IsConnected": "true",
     * "LineType": "alternative"
     * }
     * ]
     * },
     * {
     * "TypeName": "473X",
     * "Lines": [
     * {
     * "SClientId": "IFEPTA21",
     * "RClientId": "IFEPTB21",
     * "Enable": "true",
     * "IsConnected": "true",
     * "LineType": "primary"
     * },
     * {
     * "SClientId": "IFEPTC21",
     * "RClientId": "IFEPTD21",
     * "Enable": "true",
     * "IsConnected": "true",
     * "LineType": "alternative"
     * }
     * ]
     * }
     * ]
     * }
     * <p>
     * curl -d "operator=admin" -X POST http://localhost:8305/recv/cbs/getAllLineStatus
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/cbs/getAllLineStatus", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageGetAllLineStatus(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageGetAllLineStatus"));
        logData.setRemark(StringUtils.join("Receive GetAllLineStatus Request, operator:", operator));
        this.logMessage(logData);
        ToFEPCBSGetAllLineStatus status = new ToFEPCBSGetAllLineStatus();
        status.setHostName(FEPConfig.getInstance().getHostName());
        CBSGatewayManager manager = SpringBeanFactoryUtil.getBean(CBSGatewayManager.class, false);
        if (manager != null) {
            List<CBSGatewayGroup> groups = manager.getGatewayGroups();
            if (CollectionUtils.isNotEmpty(groups)) {
                status.setCbsTypes(new ArrayList<>());
                for (CBSGatewayGroup group : groups) {
                    ToFEPCBSGetAllLineStatus.CBSTypes types = new ToFEPCBSGetAllLineStatus.CBSTypes();
                    List<CBSGatewayClientSenderConfiguration> senderConfigurations = group.getSenderConfigurations();
                    List<CBSGatewayClientReceiverConfiguration> receiverConfigurations = group.getReceiverConfigurations();
                    List<CBSGatewayClientSender> senders = group.getSenders();
                    List<CBSGatewayClientReceiver> receivers = group.getReceivers();
                    if (CollectionUtils.isNotEmpty(senderConfigurations) && CollectionUtils.isNotEmpty(receiverConfigurations)
                            && CollectionUtils.isNotEmpty(senders) && CollectionUtils.isNotEmpty(receivers)) {
                        types.setLines(new ArrayList<>());
                        for (int i = 0; i < senderConfigurations.size(); i++) {
                            ToFEPCBSGetAllLineStatus.Line line = new ToFEPCBSGetAllLineStatus.Line();
                            line.setConnected(senders.get(i) != null && IMSTransmissionConnState.isClientConnected(senders.get(i).getCurrentConnState()) && receivers.get(i) != null && IMSTransmissionConnState.isClientConnected(receivers.get(i).getCurrentConnState()));
                            line.setEnable(senderConfigurations.get(i).isEnable() || receiverConfigurations.get(i).isEnable());
                            line.setPause(senderConfigurations.get(i).isPause() && receiverConfigurations.get(i).isPause()); // 2026-02-09 Richard add
                            line.setLineType(ToFEPCBSGetAllLineStatus.LineType.valueOf(senderConfigurations.get(i).getLineType().name()));
                            line.setrClientId(receiverConfigurations.get(i).getClientId());
                            line.setsClientId(senderConfigurations.get(i).getClientId());
                            // 2026-02-09 Richard add start
                            // 設定狀態
                            if (line.isEnable()) {
                                if (line.isPause()) {
                                    line.setStatus(ToFEPCBSGetAllLineStatus.LineStatus.Pause);
                                } else {
                                    line.setStatus(line.isConnected() ? ToFEPCBSGetAllLineStatus.LineStatus.Connected : ToFEPCBSGetAllLineStatus.LineStatus.Disconnected);
                                }
                            } else {
                                line.setStatus(ToFEPCBSGetAllLineStatus.LineStatus.Disable);
                            }
                            // 塞入統計數據
                            CBSGatewayStatistic statistic = group.getStatistic(senderConfigurations.get(i));
                            line.setTransactions(statistic == null ? 0L : statistic.getTransactions());
                            line.setTotalTransactions(statistic == null ? 0L : statistic.getTotalTransactions());
                            // 2026-02-09 Richard add end
                            types.getLines().add(line);
                        }
                    }
                    CBSGatewayServerConfiguration serverConfiguration = group.getServerConfiguration();
                    if (serverConfiguration != null) {
                        ToFEPCBSGetAllLineStatus.Server server = new ToFEPCBSGetAllLineStatus.Server();
                        server.setEndPoint(serverConfiguration.getEndPoint());
                        server.setListen(group.getServer() != null && group.getServer().isListening());
                        server.setEnable(group.getServer() != null && !group.isBothAllSenderAndReceiverDisconnected()); // 2025-09-16 Richard modified
                        types.setServer(server);
                    }
                    types.setTypeName(group.getTypeName());
                    status.getCbsTypes().add(types);
                }
            }
        }
        String response = StringUtils.EMPTY;
        try {
            response = new Gson().toJson(status);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageGetAllLineStatus"));
            logData.setRemark(StringUtils.join("Convert Object to JSON string failed, operator:", operator));
            sendEMS(Level.WARN, logData);
        } finally {
            logData.setOperator(operator);
            logData.setProgramFlowType(ProgramFlow.CBSGatewayOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageGetAllLineStatus"));
            logData.setMessage(response);
            logData.setRemark(StringUtils.join("Send GetAllLineStatus Response, operator:", operator));
            this.logMessage(logData);
        }
        return response;
    }

    /**
     * 改變腳位狀態, 啟動或停止
     * <p>
     * curl -d "operator=admin&clientId=IFEPTA01&action=Enable&" -X POST http://localhost:8305/recv/cbs/changeLineStatus
     * <p>
     * curl -d "operator=admin&clientId=IFEPTA01&action=Pause&" -X POST http://localhost:8305/recv/cbs/changeLineStatus
     * <p>
     * curl -d "operator=admin&clientId=IFEPTA01&action=Disable&" -X POST http://localhost:8305/recv/cbs/changeLineStatus
     *
     * @param operator
     * @param clientId
     * @param action
     * @return
     */
    @RequestMapping(value = "/recv/cbs/changeLineStatus", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageChangeLineStatus(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
                                            @RequestParam(value = "clientId") String clientId, @RequestParam(value = "action") ToCBSChangeLineStatusAction action) {
        putMDC();
        String result = Boolean.FALSE.toString();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeLineStatus"));
        logData.setRemark(StringUtils.join("Receive ChangeLineStatus Request, clientId:", clientId, ",action:", action, ",operator:", operator));
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(clientId)) {
                logData.setOperator(operator);
                logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeLineStatus"));
                logData.setRemark(StringUtils.join("Cannot ChangeLineStatus cause clientId is empty, clientId:", clientId, ",action:", action, ",operator:", operator));
                logMessage(Level.WARN, logData);
            } else {
                CBSGatewayManager manager = SpringBeanFactoryUtil.getBean(CBSGatewayManager.class, false);
                if (manager != null) {
                    result = Boolean.toString(manager.changeLineStatus(logData, clientId, action));
                }
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeLineStatus"));
            logData.setRemark(StringUtils.join("ChangeLineStatus failed, clientId:", clientId, ",action:", action, ",operator:", operator));
            sendEMS(Level.WARN, logData);
        } finally {
            logData.setOperator(operator);
            logData.setProgramFlowType(ProgramFlow.CBSGatewayOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeLineStatus"));
            logData.setMessage(result);
            logData.setRemark(StringUtils.join("Send ChangeLineStatus Response, clientId:", clientId, ",action:", action, ",operator:", operator));
            this.logMessage(logData);
        }
        return result;
    }

    /**
     * 改變Server狀態, 啟動監聽或停止監聽
     * <p>
     * curl -d "operator=admin&endPoint=127.0.0.1:8752&enable=true" -X POST http://localhost:8305/recv/cbs/changeServerStatus
     *
     * @param operator
     * @param endPoint
     * @param enable
     * @return
     */
    @RequestMapping(value = "/recv/cbs/changeServerStatus", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageChangeServerStatus(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
                                              @RequestParam(value = "endPoint") String endPoint, @RequestParam(value = "enable") boolean enable) {
        putMDC();
        String result = Boolean.FALSE.toString();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeServerStatus"));
        logData.setRemark(StringUtils.join("Receive ChangeServerStatus Request, endPoint:", endPoint, ",enable:", enable, ",operator:", operator));
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(endPoint)) {
                logData.setOperator(operator);
                logData.setProgramFlowType(ProgramFlow.CBSGatewayIn);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeServerStatus"));
                logData.setRemark(StringUtils.join("Cannot ChangeServerStatus cause endPoint is empty, endPoint:", endPoint, ",enable:", enable, ",operator:", operator));
                logMessage(Level.WARN, logData);
            } else {
                CBSGatewayManager manager = SpringBeanFactoryUtil.getBean(CBSGatewayManager.class, false);
                if (manager != null) {
                    result = Boolean.toString(manager.changeServerStatus(logData, endPoint, enable));
                }
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeServerStatus"));
            logData.setRemark(StringUtils.join("ChangeServerStatus failed, endPoint:", endPoint, ",enable:", enable, ",operator:", operator));
            sendEMS(Level.WARN, logData);
        } finally {
            logData.setOperator(operator);
            logData.setProgramFlowType(ProgramFlow.CBSGatewayOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeServerStatus"));
            logData.setMessage(result);
            logData.setRemark(StringUtils.join("Send ChangeServerStatus Response, endPoint:", endPoint, ",enable:", enable, ",operator:", operator));
            this.logMessage(logData);
        }
        return result;
    }

    // /**
    //  * curl -X POST http://localhost:8305/recv/cbs/getClientIdToLocal
    //  *
    //  * @return
    //  */
    // @RequestMapping(value = "/recv/cbs/getClientIdToLocal", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    // @ResponseBody
    // public String onMessageGetClientIdToLocal() {
    //     CBSGatewayManager manager = SpringBeanFactoryUtil.getBean(CBSGatewayManager.class, false);
    //     if (manager != null) {
    //         return manager.getClientIdToLocal();
    //     }
    //     return StringUtils.EMPTY;
    // }
}
