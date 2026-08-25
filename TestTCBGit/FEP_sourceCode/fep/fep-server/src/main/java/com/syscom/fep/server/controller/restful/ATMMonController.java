package com.syscom.fep.server.controller.restful;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AtmstatExtMapper;
import com.syscom.fep.server.controller.BaseController;
import com.syscom.fep.vo.communication.ToATMMONCommu;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 2022-08-16
 * ATM 前置處理系統
 *
 * @author Han
 * @designer Ashiang
 */
@StackTracePointCut(caller = SvrConst.SVR_ATM)
public class ATMMonController extends BaseController {
    private final AtmstatExtMapper atmstatExtMapper = SpringBeanFactoryUtil.getBean(AtmstatExtMapper.class);
    private static final int MAX_LOOPS = Integer.MAX_VALUE; // 2024-11-05 Richard add for 【Unchecked Input for Loop Condition】

    @Override
    public String getName() {
        return SvrConst.SVR_ATM;
    }

    @RequestMapping(value = "/fep/ATMStatusChange", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String sendReceive(@RequestBody String messageIn) {
        // Restful進來的電文
        try {
            messageIn = ESAPI.validator().getValidInput("", messageIn, "inputValue", Integer.MAX_VALUE, false); // Cross-Site Scripting: Reflected
            return this.processRequestData(ProgramFlow.RESTFulIn, messageIn);
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().error(e, "ESAPI.validator().getValidInput() error");
        }
        return StringUtils.EMPTY;
    }

    /**
     * 處理進來的電文並回應
     *
     * @param programFlow
     * @param messageIn
     * @return
     */
    @Override
    protected String processRequestData(ProgramFlow programFlow, String messageIn) {
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        LogHelperFactory.getTraceLogger().trace(this.getName(), " Receive Message:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.ATMMON);
        logData.setMessage(messageIn);
        logData.setProgramFlowType(programFlow);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setRemark(this.getName() + " Receive Request");
        this.logMessage(logData);
        Gson gson = new Gson();
        Response resp = new Response();
        List<ResponseItem> items = new ArrayList<ResponseItem>();
        resp.setMsgRs(items);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }

            ToATMMONCommu atmmonMsg = gson.fromJson(messageIn, ToATMMONCommu.class);
            if (atmmonMsg == null || atmmonMsg.getMsgRq() == null) {
                messageOut = gson.toJson(resp);
                return messageOut;
            }
            // 2024-11-05 Richard add start for 【Unchecked Input for Loop Condition】
            int nMsg = atmmonMsg.getMsgRq().size();
            if (nMsg > MAX_LOOPS) {
                nMsg = MAX_LOOPS;
            }
            // 2024-11-05 Richard add end for 【Unchecked Input for Loop Condition】
            for (int i = 0; i < nMsg; i++) { // 2024-09-11 Richard modified start for 【Unchecked Input for Loop Condition】
                ResponseItem item = new ResponseItem();

                item.setATMNo(atmmonMsg.getMsgRq().get(i).getATMNo());
                int result = atmstatExtMapper.updateATMStatus(
                        atmmonMsg.getMsgRq().get(i).getATMNo(),
                        atmmonMsg.getMsgRq().get(i).getEnable(),
                        atmmonMsg.getMsgRq().get(i).getServiceStatus(),
                        atmmonMsg.getMsgRq().get(i).getConnectStatus());
                logData.setAtmNo(atmmonMsg.getMsgRq().get(i).getATMNo());
                logData.setProgramFlowType(this.getOutProgramFlow(programFlow));
                logData.setMessageFlowType(MessageFlow.Response);
                logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
                logData.setRemark(
                        StringUtils.join(this.getName(), " Return data to ATMMon",
                                " ATMNo:", atmmonMsg.getMsgRq().get(i).getATMNo(),
                                " Enable:", atmmonMsg.getMsgRq().get(i).getEnable(),
                                " ServiceStatus:", atmmonMsg.getMsgRq().get(i).getServiceStatus(),
                                " ConnectStatus:", atmmonMsg.getMsgRq().get(i).getConnectStatus(),
                                " result:", result));
                this.logMessage(logData);

                item.setResult(result > 0 ? 0 : -1);
                // if (result <= 0) {
                //     if (atmmonMsg.getMsgRq().size() == 1) {
                //         //messageOut = "-1";
                //         continue;
                //     }
                // }
                resp.msgRs.add(item);
                //messageOut = "{\"result\":0}";
            }
        } catch (Exception e) {
            //messageOut = "{\"result\":-1}";
            logData.setMessage(messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setProgramFlowType(programFlow);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setRemark(e.getMessage());
            logData.setProgramException(e);
            this.logMessage(logData);
            // sendEMS
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setProgramException(e);
            sendEMS(logData);
        } finally {
            messageOut = gson.toJson(resp);
            if (StringUtils.isNotBlank(messageOut)) {
                //LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Message: ", messageOut);
                logData.setMessage(messageOut);
                logData.setRemark("Return Message");
                this.logMessage(logData);
            }
        }
        return messageOut;
    }
}

class Response {
    /**
     *
     */
    @JsonProperty("MsgRs")
    public List<ResponseItem> msgRs;

    public List<ResponseItem> getMsgRs() {
        return msgRs;
    }

    public void setMsgRs(List<ResponseItem> msgRs) {
        this.msgRs = msgRs;
    }
}

class ResponseItem {
    private String ATMNo;
    private int result;

    public String getATMNo() {
        return ATMNo;
    }

    public void setATMNo(String atmNo) {
        ATMNo = atmNo;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int Result) {
        result = Result;
    }
}