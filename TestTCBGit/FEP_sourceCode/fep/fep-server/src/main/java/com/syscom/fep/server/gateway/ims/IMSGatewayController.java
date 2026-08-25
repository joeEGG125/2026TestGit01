package com.syscom.fep.server.gateway.ims;

import com.google.gson.Gson;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.notify.NotifyHelper;
import com.syscom.fep.common.notify.NotifyHelperTemplateId;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.gateway.ims.keepalive.IMSGatewayKeepAliveRestfulClient;
import com.syscom.fep.server.gateway.ims.processor.IMSGatewayProcessor;
import com.syscom.fep.server.gateway.ims.processor.IMSGatewayProcessorConfiguration;
import com.syscom.fep.server.gateway.ims.processor.IMSGatewayProcessorGroup;
import com.syscom.fep.server.gateway.ims.processor.IMSGatewayProcessorGroupConfiguration;
import com.syscom.fep.vo.communication.ToFEPIMSGetAllLineStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@StackTracePointCut(caller = SvrConst.SVR_IMS_GATEWAY)
public class IMSGatewayController extends FEPBase {
    @Autowired
    private IMSGatewayConfiguration configuration;
    @Autowired
    private IMSGatewayManager manager;
    @Autowired
    private NotifyHelper notifyHelper;

    private void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
    }

    /**
     * 操控GW
     * <p>
     * curl -d "mode=primary&action=start" -X POST http://localhost:8213/recv/ims/channel
     * <p>
     * curl -d "mode=primary&action=stop" -X POST http://localhost:8213/recv/ims/channel
     * <p>
     * curl -d "mode=secondary&action=start" -X POST http://localhost:8213/recv/ims/channel
     * <p>
     * curl -d "mode=secondary&action=stop" -X POST http://localhost:8213/recv/ims/channel
     * <p>
     * curl -d "action=check" -X POST http://localhost:8213/recv/ims/channel
     *
     * @param mode
     * @param action
     * @return
     */
    @RequestMapping(value = "/recv/ims/channel", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageChannel(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
                                   @RequestParam(value = "mode", required = false) IMSGatewayMode mode,
                                   @RequestParam(value = "action") IMSGatewayCmdAction action,
                                   @RequestParam(value = "respType", required = false) IMSGatewayRespType respType) {
        putMDC();
        switch (action) {
            case start:
            case stop:
                return this.doOperateChannel(operator, mode, action, respType);
            case check:
                return this.checkStatus();
            default:
                return StringUtils.join("Incorrect parameter action = \"", action, "\"");
        }
    }

    /**
     * 啟動/停止對應channel
     *
     * @param operator
     * @param mode
     * @param action
     * @param respType
     * @return
     */
    private String doOperateChannel(String operator, IMSGatewayMode mode, IMSGatewayCmdAction action, IMSGatewayRespType respType) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramFlowType(ProgramFlow.IMSGWIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".doOperateChannel"));
        logData.setRemark(StringUtils.join("Operate IMSGateway Channel, mode:", mode.name(), ", action:", action));
        this.logMessage(logData);
        String response = StringUtils.EMPTY;
        boolean result = false;
        Boolean isConnected = null;
        if (mode == IMSGatewayMode.primary || mode == IMSGatewayMode.secondary) {
            try {
                RefBoolean isConnectedRef = null;
                if (action == IMSGatewayCmdAction.start) {
                    if (manager.runGateway(mode, configuration)) {
                        response = StringUtils.join("IMS Gateway Start ", StringUtils.capitalize(mode.name()), " port OK!");
                    } else {
                        response = StringUtils.join("IMS Gateway ", StringUtils.capitalize(mode.name()), " port has started already!");
                    }
                } else if (action == IMSGatewayCmdAction.stop) {
                    isConnectedRef = new RefBoolean(false);
                    if (manager.stopGateway(mode, isConnectedRef)) {
                        response = StringUtils.join("IMS Gateway Stop ", StringUtils.capitalize(mode.name()), " port OK!",
                                isConnectedRef.get() ? " But it is still connected to IMS Server by one port at least." : StringUtils.EMPTY);
                    } else {
                        response = StringUtils.join("IMS Gateway ", StringUtils.capitalize(mode.name()), " port has stopped already!");
                    }
                    isConnected = isConnectedRef.get();
                }
                result = true;
                // 不管那一台IMSGW, 只要有startChannel及stopChannel, 都發mail通知
                this.sendNotifyMail(logData, mode, action, isConnectedRef != null && isConnectedRef.get());
            } finally {
                // 2024-10-29 Richard add 針對secondary線路, 當start時, 則停止keepAlive, 當stop時, 則啟動keepAlive
                if (mode == IMSGatewayMode.secondary) {
                    IMSGatewayKeepAliveRestfulClient client = SpringBeanFactoryUtil.getBean(IMSGatewayKeepAliveRestfulClient.class, false);
                    if (client != null) {
                        if (action == IMSGatewayCmdAction.start)
                            client.terminateConnection();
                        else if (action == IMSGatewayCmdAction.stop)
                            client.run();
                    }
                }
            }
        } else {
            response = StringUtils.join("Incorrect parameter, mode = ", mode);
        }
        if (respType == IMSGatewayRespType.JSON) {
            IMSGatewayResp resp = new IMSGatewayResp();
            resp.setMode(mode);
            resp.setAction(action);
            resp.setResult(result);
            resp.setMessage(response);
            resp.setIsConnected(isConnected);
            response = new Gson().toJson(resp);
        }
        logData.setOperator(operator);
        logData.setProgramFlowType(ProgramFlow.IMSGWOut);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramName(StringUtils.join(ProgramName, ".doOperateChannel"));
        logData.setRemark(StringUtils.join("Operate IMSGateway Channel and Get Response, mode:", mode.name(), ", action:", action, ", response:", response));
        this.logMessage(logData);
        return response;
    }

    private String checkStatus() {
        putMDC();
        LogData logData = new LogData();
        logData.clear();
        logData.setProgramFlowType(ProgramFlow.IMSGWIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".checkStatus"));
        logData.setRemark("Check Status");
        this.logMessage(logData);
        String response = manager.checkStatus();
        logData.setProgramFlowType(ProgramFlow.IMSGWOut);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramName(StringUtils.join(ProgramName, ".checkStatus"));
        logData.setRemark(StringUtils.join("Check Status and Get Response:\r\n", response));
        this.logMessage(logData);
        return response;
    }

    /**
     * 不管那一台IMSGW, 只要有startChannel及stopChannel, 都發mail通知
     * Mail的內容為
     * IMSGW on fepap1T(10.3.101.142) 主要線路/備援線路 已啟動/已停止
     *
     * @param logData
     * @param mode
     * @param action
     * @param isConnected
     */
    private void sendNotifyMail(LogData logData, IMSGatewayMode mode, IMSGatewayCmdAction action, boolean isConnected) {
        if (StringUtils.isNotBlank(configuration.getMailTo())) {
            String body = StringUtils.join("IMSGW on ", FEPConfig.getInstance().getHostName(), "(", FEPConfig.getInstance().getHostIp(), ") ", mode.getDescription(), " 已", action.getDescription());
            // 如果是stop的動作, 並且沒有徹底完全斷開所有的連線, 則要補充說明
            if (action == IMSGatewayCmdAction.stop && isConnected)
                body = StringUtils.join(body, ", 但是它還是至少有一個port連線中");
            logData.setProgramName(StringUtils.join(ProgramName, ".sendNotifyMail"));
            logData.setRemark(StringUtils.join("Ready to send Notify Mail, mailAddress:", configuration.getMailTo(), ", body:", body));
            this.logMessage(logData);
            try {
                notifyHelper.sendSimpleMail(NotifyHelperTemplateId.IMS_GATEWAY, configuration.getMailTo(), body, true);
                logData.setProgramName(StringUtils.join(ProgramName, ".sendNotifyMail"));
                logData.setRemark(StringUtils.join("Send Notify Mail succeed, mailAddress:", configuration.getMailTo(), ", body:", body));
                this.logMessage(logData);
            } catch (Exception e) {
                logData.setProgramException(e);
                logData.setProgramName(StringUtils.join(ProgramName, ".sendNotifyMail"));
                logData.setRemark(StringUtils.join("Send Notify Mail failed, mailAddress:", configuration.getMailTo(), ", body:", body));
                sendEMS(logData);
            }
        }
    }

    /**
     * curl -d "operator=admin" -X POST http://localhost:8213/recv/ims/getAllLineStatus
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/ims/getAllLineStatus", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageGetAllLineStatus(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramFlowType(ProgramFlow.IMSGWIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageGetAllLineStatus"));
        logData.setRemark(StringUtils.join("Receive GetAllLineStatus Request, operator:", operator));
        this.logMessage(logData);
        ToFEPIMSGetAllLineStatus status = new ToFEPIMSGetAllLineStatus();
        status.setHostName(FEPConfig.getInstance().getHostName());
        List<ToFEPIMSGetAllLineStatus.IMSGatewayMode> modes = new ArrayList<>();
        // Primary
        ToFEPIMSGetAllLineStatus.IMSGatewayMode primaryImsGatewayMode = this.getToFEPIMSGetAllLineStatusIMSGatewayMode(manager.getPrimary(), configuration.getPrimary());
        if (primaryImsGatewayMode != null)
            modes.add(primaryImsGatewayMode);
        // Secondary
        ToFEPIMSGetAllLineStatus.IMSGatewayMode secondaryImsGatewayMode = this.getToFEPIMSGetAllLineStatusIMSGatewayMode(manager.getSecondary(), configuration.getSecondary());
        if (secondaryImsGatewayMode != null)
            modes.add(secondaryImsGatewayMode);
        if (!modes.isEmpty())
            status.setModes(modes);
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
            logData.setProgramFlowType(ProgramFlow.IMSGWOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageGetAllLineStatus"));
            logData.setMessage(response);
            logData.setRemark(StringUtils.join("Send GetAllLineStatus Response, operator:", operator));
            this.logMessage(logData);
        }
        return response;
    }

    private ToFEPIMSGetAllLineStatus.IMSGatewayMode getToFEPIMSGetAllLineStatusIMSGatewayMode(IMSGateway imsGateway, IMSGatewayProcessorGroupConfiguration processorGroupConfiguration) {
        if (imsGateway != null) {
            List<IMSGatewayProcessorGroup> processorGroups = imsGateway.getProcessorGroups();
            if (CollectionUtils.isNotEmpty(processorGroups)) {
                ToFEPIMSGetAllLineStatus.IMSGatewayMode mode = new ToFEPIMSGetAllLineStatus.IMSGatewayMode();
                mode.setLines(new ArrayList<>());
                for (IMSGatewayProcessorGroup group : processorGroups) {
                    IMSGatewayProcessorConfiguration senderConfiguration = group.getSenderConfiguration();
                    IMSGatewayProcessorConfiguration receiverConfiguration = group.getReceiverConfiguration();
                    IMSGatewayProcessor sender = group.getSender();
                    IMSGatewayProcessor receiver = group.getReceiver();
                    if (senderConfiguration != null && receiverConfiguration != null) {
                        ToFEPIMSGetAllLineStatus.Line line = new ToFEPIMSGetAllLineStatus.Line();
                        line.setConnected(sender != null && IMSGatewayConnState.isConnected(sender.getCurrentConnState()) && receiver != null && IMSGatewayConnState.isConnected(receiver.getCurrentConnState()));
                        line.setEnable(senderConfiguration.isEnable() || receiverConfiguration.isEnable());
                        line.setLineType(ToFEPIMSGetAllLineStatus.LineType.valueOf(senderConfiguration.getLineType().name()));
                        line.setrClientId(receiverConfiguration.getClientId());
                        line.setsClientId(senderConfiguration.getClientId());
                        mode.getLines().add(line);
                    }
                }
                mode.setModeName(StringUtils.capitalize(imsGateway.getProcessorGroupConfiguration().getMode().name()));
                return mode;
            }
        } else {
            // 2025-04-24 Richard add 如果線路沒有運行, 則根據Configuration物件, 獲取線路狀態訊息
            List<IMSGatewayProcessorConfiguration> senderConfigurations = processorGroupConfiguration.getSenderConfigurations();
            List<IMSGatewayProcessorConfiguration> receiverConfigurations = processorGroupConfiguration.getReceiverConfigurations();
            if (CollectionUtils.isNotEmpty(senderConfigurations) && CollectionUtils.isNotEmpty(receiverConfigurations)) {
                ToFEPIMSGetAllLineStatus.IMSGatewayMode mode = new ToFEPIMSGetAllLineStatus.IMSGatewayMode();
                mode.setLines(new ArrayList<>());
                for (int i = 0; i < senderConfigurations.size(); i++) {
                    IMSGatewayProcessorConfiguration senderConfiguration = senderConfigurations.get(i);
                    IMSGatewayProcessorConfiguration receiverConfiguration = receiverConfigurations.get(i);
                    if (senderConfiguration != null && receiverConfiguration != null) {
                        ToFEPIMSGetAllLineStatus.Line line = new ToFEPIMSGetAllLineStatus.Line();
                        line.setConnected(false);
                        line.setEnable(false); // 這裡設定為false, 表示停用中, 則可以通過UI控制啟用
                        line.setLineType(ToFEPIMSGetAllLineStatus.LineType.valueOf(senderConfiguration.getLineType().name()));
                        line.setrClientId(receiverConfiguration.getClientId());
                        line.setsClientId(senderConfiguration.getClientId());
                        mode.getLines().add(line);
                    }
                }
                mode.setModeName(StringUtils.capitalize(processorGroupConfiguration.getMode().name()));
                return mode;
            }
        }
        return null;
    }

    /**
     * 改變腳位狀態, 啟動或停止
     * <p>
     * curl -d "operator=admin&clientId=IFEPTA52&enable=true" -X POST http://localhost:8213/recv/ims/changeLineStatus
     *
     * @param operator
     * @param clientId
     * @param enable
     * @return
     */
    @RequestMapping(value = "/recv/ims/changeLineStatus", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageChangeLineStatus(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
                                            @RequestParam(value = "clientId") String clientId, @RequestParam(value = "enable") boolean enable) {
        putMDC();
        String result = Boolean.FALSE.toString();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramFlowType(ProgramFlow.IMSGWIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeLineStatus"));
        logData.setRemark(StringUtils.join("Receive ChangeLineStatus Request, clientId:", clientId, ",enable:", enable, ",operator:", operator));
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(clientId)) {
                logData.setOperator(operator);
                logData.setProgramFlowType(ProgramFlow.IMSGWIn);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeLineStatus"));
                logData.setRemark(StringUtils.join("Cannot ChangeLineStatus cause clientId is empty, clientId:", clientId, ",enable:", enable, ",operator:", operator));
                logMessage(Level.WARN, logData);
            } else {
                IMSGatewayManager manager = SpringBeanFactoryUtil.getBean(IMSGatewayManager.class, false);
                if (manager != null) {
                    result = Boolean.toString(manager.changeLineStatus(logData, clientId, enable));
                }
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeLineStatus"));
            logData.setRemark(StringUtils.join("ChangeLineStatus failed, clientId:", clientId, ",enable:", enable, ",operator:", operator));
            sendEMS(Level.WARN, logData);
        } finally {
            logData.setOperator(operator);
            logData.setProgramFlowType(ProgramFlow.IMSGWOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeLineStatus"));
            logData.setMessage(result);
            logData.setRemark(StringUtils.join("Send ChangeLineStatus Response, operator:", operator));
            this.logMessage(logData);
        }
        return result;
    }
}
