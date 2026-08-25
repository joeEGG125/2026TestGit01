package com.syscom.fep.service.cbstimeoutrerun;

import com.syscom.fep.base.FEPBaseMethod;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.vo.service.APIResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 2025-03-07 Richard add
 *
 * @author Richard
 */
@StackTracePointCut(caller = SvrConst.SVR_CBSTimeOutRerun)
public class CBSTimeOutRerunController extends FEPBaseMethod {

    /**
     * curl -d "value=message1|message2|message3" -X POST http://localhost:8202/api/CustomCommand/AddCBSTimeoutRerunMessage
     *
     * @param value
     * @return
     */
    @RequestMapping(value = "/api/CustomCommand/AddCBSTimeoutRerunMessage", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public APIResult addCBSTimeoutRerunMessage(@RequestParam(value = "value") String value) {
        CBSTimeOutRerun svcCBSTimeoutRerun = SpringBeanFactoryUtil.getBean(CBSTimeOutRerun.class);
        svcCBSTimeoutRerun.putMDC();
        APIResult result = new APIResult();
        if (StringUtils.isBlank(value)) {
            result.setErrorMessage("value parameter can't be empty");
            return result;
        }
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.INBK);
        logData.setChannel(FEPChannel.CBS);
        logData.setProgramFlowType(ProgramFlow.ChannelGWIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".addCBSTimeoutRerunMessage"));
        logData.setMessage(value);
        logData.setRemark(StringUtils.join(SvrConst.SVR_CBSTimeOutRerun, " Receive Command"));
        this.logMessage(logData);
        try {
            result.setStatus(svcCBSTimeoutRerun.addCBSTimeoutRerunMessage(value));
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".addCBSTimeoutRerunMessage"));
            logData.setProgramException(e);
            sendEMS(logData);
        }
        logData.setSubSys(SubSystem.INBK);
        logData.setChannel(FEPChannel.CBS);
        logData.setProgramFlowType(ProgramFlow.ChannelGWOut);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramName(StringUtils.join(ProgramName, ".addCBSTimeoutRerunMessage"));
        logData.setMessage(value);
        logData.setRemark(StringUtils.join(SvrConst.SVR_CBSTimeOutRerun, " Response result:", result));
        this.logMessage(logData);
        return result;
    }
}
