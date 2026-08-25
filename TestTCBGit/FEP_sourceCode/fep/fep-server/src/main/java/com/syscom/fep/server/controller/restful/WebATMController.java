package com.syscom.fep.server.controller.restful;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.GWConfig;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.invoker.FEPInvoker;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.controller.BaseController;
import com.syscom.fep.vo.communication.ToFEPATMCommu;
import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderRsStat;
import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderRsStatRsStatCode;
import com.syscom.fep.vo.text.webatm.WebATMRequest;
import com.syscom.fep.vo.text.webatm.WebATMResponse;
import com.syscom.fep.vo.text.webatm.WebATMResponse.WebATMRs;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;

/**
 * WEB Bank電文入口
 *
 * @author Richard
 */

@StackTracePointCut(caller = SvrConst.SVR_WEBATM)
public class WebATMController extends BaseController {
    @Autowired
    private FEPInvoker invoker;

    @Override
    public String getName() {
        return SvrConst.SVR_WEBATM;
    }

// 2025-04-10 Richard modified start for [Unchecked Input for Loop Condition]
//    @RequestMapping(value = "/recv/webatm", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
//    @ResponseBody
//    public String sendReceive(@RequestBody String messageIn) {
//        // Restful進來的電文
//        try {
//            messageIn = ESAPI.validator().getValidInput("", messageIn, "inputValue", Integer.MAX_VALUE, false); // Cross-Site Scripting: Reflected
//            return this.processRequestData(ProgramFlow.RESTFulIn, messageIn);
//        } catch (Exception e) {
//            LogHelperFactory.getTraceLogger().error(e, "ESAPI.validator().getValidInput() error");
//        }
//        return StringUtils.EMPTY;
//    }
// 2025-04-10 Richard modified end for [Unchecked Input for Loop Condition]

    /**
     * 處理進來的電文並回應
     *
     * @param programFlow
     * @param messageIn
     * @return
     */
    @Override
    protected String processRequestData(final ProgramFlow programFlow, final String messageIn) {
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        LogHelperFactory.getTraceLogger().trace(this.getName(), " Receive Message:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.BRANCH);
        logData.setProgramFlowType(programFlow);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setMessage(messageIn);
        logData.setRemark(this.getName() + " Receive Request");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            WebATMRequest req = deserializeFromXml(messageIn, WebATMRequest.class);
            logData.setMessage(req.getSvcRq().getRq());
            logData.setMessageId(req.getRqHeader().getMsgID());
            logData.setAtmSeq(StringUtil.fromHex(req.getSvcRq().getRq().substring(124, 16 + 124)));
            // Send Restful Request
            ToFEPATMCommu toFEPATMCommu = new ToFEPATMCommu();
            toFEPATMCommu.setEj(String.valueOf(logData.getEj()));
            toFEPATMCommu.setMessage(req.getSvcRq().getRq());
            toFEPATMCommu.setTxRquid(UUIDUtil.randomUUID(true));
            toFEPATMCommu.setSync(false); // FEP接收後進行異步處理
            int timeout = GWConfig.getInstance().getAATimeout();
            logData.setProgramFlowType(FEPConfig.getInstance().getAtmProtocol() == Protocol.restful ? ProgramFlow.RESTFulIn : ProgramFlow.SocketIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setMessage(toFEPATMCommu.toString());
            logData.setRemark(this.getName() + " Send Message to " + SvrConst.SVR_ATM);
            this.logMessage(logData);
            String rtn = invoker.sendReceiveToFEPATM(toFEPATMCommu, (timeout + 5) * 1000);
            logData.setProgramFlowType(FEPConfig.getInstance().getAtmProtocol() == Protocol.restful ? ProgramFlow.RESTFulOut : ProgramFlow.SocketOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setMessage(rtn);
            logData.setRemark(this.getName() + " Get Response from " + SvrConst.SVR_ATM);
            this.logMessage(logData);
            // prepare response
            WebATMResponse rep = new WebATMResponse();
            rep.setRsHeader(new FEPRsHeader());
            rep.getRsHeader().setChlEJNo(req.getRqHeader().getChlEJNo());
            rep.getRsHeader().setEJNo(String.valueOf(logData.getEj()));
            rep.getRsHeader().setRqTime(req.getRqHeader().getChlSendTime());
            rep.getRsHeader().setRsTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_T_HH_MM_SS));
            rep.getRsHeader().setRsStat(new FEPRsHeaderRsStat());
            rep.getRsHeader().getRsStat().setRsStatCode(new FEPRsHeaderRsStatRsStatCode());
            rep.getRsHeader().getRsStat().getRsStatCode().setType(logData.getMessageId());
            rep.getRsHeader().getRsStat().getRsStatCode().setValue("0000");
            rep.setSvcRs(new WebATMRs());
            rep.getSvcRs().setRs(rtn);
            messageOut = serializeToXml(rep);
            logData.setProgramFlowType(this.getOutProgramFlow(programFlow));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(messageOut);
            logData.setRemark(this.getName() + " return Response to WEBATM");
            this.logMessage(logData);
        } catch (Exception e) {
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
            if (StringUtils.isNotBlank(messageOut)) {
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Message: ", messageOut);
            } else {
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Empty Message");
            }
        }
        return messageOut;
    }
}
