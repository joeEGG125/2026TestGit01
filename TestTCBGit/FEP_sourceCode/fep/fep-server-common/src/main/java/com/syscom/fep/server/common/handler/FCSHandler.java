package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.CardData;
import com.syscom.fep.base.aa.FCSData;
import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.rm.RMGeneral;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class FCSHandler extends HandlerBase {
    private static final String ProgramName = StringUtils.join(FCSHandler.class.getSimpleName(), ".");
    private RMData txBRSData;
    // add by 榮升 2013/12/24 FCS呼叫製卡批次AB檔檢核
    @SuppressWarnings("unused")
    private CardData txCardBRSData;
    @SuppressWarnings("unused")
    private FCSData txFCSData;

    public FCSHandler() {

    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        @SuppressWarnings("unused")
        String rmRes = "";
        @SuppressWarnings("unused")
        String Step = "";
        // FEPResponse FEPRsp = null;
        ej = TxHelper.generateEj();
        return null;
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return false;
    }

    /**
     * For UI 專用
     *
     * @param channel channel
     * @param data    data
     * @param txcd    交易代碼
     * @return
     * @throws Exception
     */
    public boolean dispatch(FEPChannel channel, RMGeneral data, String txcd) throws Exception {
        @SuppressWarnings("unused")
        String rtnMsg = "";
        @SuppressWarnings("unused")
        String flatFile = "";
        ej = TxHelper.generateEj();
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.RM);
        logData.setChannel(channel);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(ProgramName);
        logData.setEj(ej);
        try {
            // var req = new R1001_Request();
            // flatFile = req.MakeMessageFromGeneral(data);
            // logData.Message = flatFile;
            // LogMessage(LogLevel.Debug, logData);

            txBRSData = new RMData();
            // txBRSData.RMFEPResponse = new FEPResponse();
            txBRSData.setEj(ej);
            txBRSData.setTxObject(data);
            txBRSData.setTxChannel(channel);
            txBRSData.setTxSubSystem(SubSystem.RM);
            txBRSData.setTxRequestMessage("");
            txBRSData.setLogContext(logData);
            txBRSData.setMessageID(txcd);
            txBRSData.setMsgCtl(FEPCache.getMsgctrl(txcd));
            this.setChannel(txBRSData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            txBRSData.setEj(ej);
            txBRSData.setAaName(txcd);
            // Modify by Candy 20140812 CALL RUNAA 2 times, Delete 1 times
            // runAA(txcd, channel);
            rtnMsg = runAA(txcd, channel);
            // if (((FEPResponse)DeserializeFromXml(rtnMsg, typeof(FEPResponse))).RsHeader.RsStat.RsStatCode == "SUCCESS")
            return NormalRC.External_OK.equals(data.getResponse().getRsStatRsStateCode());
        } catch (Exception ex) {
            // SendEMS(SubSystem.RM, channel, EJ, ProgramName + MethodBase.GetCurrentMethod().Name, CommonReturnCode.ProgramException , ex, "");
            logData.setProgramException(ex);
            sendEMS(logData);
            return false;
        } finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(ProgramName);
            logData.setMessage("");
            logMessage(Level.DEBUG, logData);
        }
        // return true;
    }

    private String runAA(String msgId, FEPChannel channel) {
        String rmRes = StringUtils.EMPTY;

        if ("R1001".equals(msgId)) {
            this.setEj(ej);
            try {
                rmRes = this.runRMAA(msgId, txBRSData);
            } catch (Throwable e) {
                logContext.setProgramException(e);
                logContext.setProgramName(StringUtils.join(ProgramName, ".runAA"));
                sendEMS(logContext);
            }
        } else {
            logContext.setReturnCode(CommonReturnCode.MessageIDError);
            sendEMS(logContext);
        }
        return rmRes;
    }

    private String runRMAA(String msgId, RMData fData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRmRequestData", String.class, RMData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, fData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }

}
