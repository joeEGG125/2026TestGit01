package com.syscom.fep.server.controller.restful;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.POSHandler;
import com.syscom.fep.server.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Jaime
 */

@StackTracePointCut(caller = SvrConst.SVR_ATM)
public class POSController extends BaseController {

    @Override
    public String getName() {
        return SvrConst.SVR_ATM;
    }

    @RequestMapping(value = "/recv/pos", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sendReceive(@RequestBody String messageIn) {
        // Restful進來的電文
        return this.processRequestData(ProgramFlow.RESTFulIn, messageIn);
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
        logData.setProgramFlowType(programFlow);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setMessage(messageIn);
        logData.setRemark(this.getName() + " Receive Request");
        logData.setServiceUrl("/pos/recv");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
        try {
            // HCERequest req = deserializeFromXml(messageIn, HCERequest.class);
            // logData.setMessageId(req.getRqHeader().getMSGID());
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            POSHandler posHandler = new POSHandler();
            posHandler.setEj(logData.getEj());
            posHandler.setLogContext(logData);
            messageOut = posHandler.dispatch(FEPChannel.POS, messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setProgramFlowType(this.getOutProgramFlow(programFlow));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(messageOut);
            logData.setRemark(this.getName() + " Get Response from POSHandler");
            this.logMessage(logData);
        } catch (Exception e) {
            logData.setMessage(messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setProgramFlowType(programFlow);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setRemark(e.getMessage());
            logData.setProgramException(e);
            logData.setServiceUrl("/pos/recv");
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
