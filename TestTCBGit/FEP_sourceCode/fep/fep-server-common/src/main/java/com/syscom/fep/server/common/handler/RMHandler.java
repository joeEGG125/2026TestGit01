package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.FEPRequest;
import com.syscom.fep.vo.text.FEPResponse;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.syscom.fep.vo.text.rm.request.C1100_Request;
import com.syscom.fep.vo.text.rm.request.C1700_Request;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RMHandler extends HandlerBase {
    private String ProgramName = StringUtils.join(this.getClass().getSimpleName(), ".");
    private RMData txBRSData;
    @SuppressWarnings("unused")
    private LogData logData;

    public RMHandler() {}

    @Override
    public String dispatch(FEPChannel channel, String data) {
        String rmRes = "";
        String step = "";

        if (getEj() == 0) {
            setEj(TxHelper.generateEj());
        }

        LogData tempVar = new LogData();
        tempVar.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        tempVar.setSubSys(SubSystem.RM);
        tempVar.setChannel(channel);
        tempVar.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        tempVar.setMessageFlowType(MessageFlow.Request);
        tempVar.setProgramName(StringUtils.join(ProgramName, "dispatch"));
        tempVar.setMessage(data);
        tempVar.setEj(getEj());
        LogData logContext = tempVar;
        // 解析電文之前先記一次
        // Modify by Jim, 2011/04/27, 待抓到MessageId之後再記Log
        // LogMessage(LogLevel.Info, logContext);
        try {
            txBRSData = new RMData();
            if (!data.trim().equals("")) {
                int head = data.indexOf("<MsgID>") + (new String("<MsgID>")).length();
                int tail = data.indexOf("</MsgID>");
                txBRSData.setMessageID(data.substring(head, tail));
                if ("".equals(txBRSData.getMessageID().trim())) {
                    step = "MsgID錯誤";
                    return getRsp(step);
                }
            } else {
                step = "傳入電文為空";
                return getRsp(step);
            }

            logContext.setMessageId(txBRSData.getMessageID());
            logMessage(Level.INFO, logContext);

            step = "拆解電文錯誤";
            txBRSData.setTxObject(parseFlatfile(data));

            txBRSData.setEj(getEj());
            txBRSData.setTxRequestMessage(data);
            txBRSData.setTxChannel(channel);
            txBRSData.setMessageFlowType(MessageFlow.Request);
            txBRSData.setLogContext(logContext);
            txBRSData.setMsgCtl(FEPCache.getMsgctrl(txBRSData.getMessageID()));
            if (txBRSData.getMsgCtl() == null) {
                step = "MSGCTL無資料";
                // SendEMS(SubSystem.RM, channel, EJ, ProgramName + MethodBase.GetCurrentMethod().Name, IOReturnCode.MSGCTLNotFound, null, data);
                logContext.setReturnCode(IOReturnCode.MSGCTLNotFound);
                sendEMS(logContext);
                return getRsp(step);
            }

            this.setChannel(txBRSData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            txBRSData.setAaName(txBRSData.getMsgCtl().getMsgctlAaName());
            txBRSData.setTxStatus("1".equals(txBRSData.getMsgCtl().getMsgctlStatus()));
            // Modified by Jim, 2010/10/15, 把RqHeader的資料搬到內部系統的HEADER
            txBRSData.getTxObject().getRequest().setKINBR(txBRSData.getTxObject().getRequest().getBranchID().substring(txBRSData.getTxObject().getRequest().getBranchID().length() - 3));
            txBRSData.getTxObject().getRequest().setBRSNO(txBRSData.getTxObject().getRequest().getChlEJNo());
            txBRSData.getTxObject().getRequest().setENTTLRNO(txBRSData.getTxObject().getRequest().getUserID());
            txBRSData.getTxObject().getRequest().setSUPNO2("");
            if ("C".equals(txBRSData.getMessageID().substring(0, 1)) || "RT1101".equals(txBRSData.getMessageID())) { // 使用者就是主管
                txBRSData.getTxObject().getRequest().setSUPNO1(txBRSData.getTxObject().getRequest().getUserID());
                txBRSData.getTxObject().getRequest().setSUPNO2(txBRSData.getTxObject().getRequest().getSignID());
            } else {
                // Modified by ChenLi, 2013/08/30, 增加判斷TXNID為CXXX或RT1101，亦將USERID給與SUPNO1
                if ("C".equals(txBRSData.getTxObject().getRequest().getTxnID().substring(0, 1)) || "RT1101".equals(txBRSData.getTxObject().getRequest().getTxnID())) {
                    txBRSData.getTxObject().getRequest().setSUPNO1(txBRSData.getTxObject().getRequest().getUserID());
                    txBRSData.getTxObject().getRequest().setSUPNO2(txBRSData.getTxObject().getRequest().getSignID());
                } else {
                    txBRSData.getTxObject().getRequest().setSUPNO1(txBRSData.getTxObject().getRequest().getSignID());
                }
            }
            txBRSData.getTxObject().getRequest().setTBSDY(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            txBRSData.getTxObject().getRequest().setTIME(new SimpleDateFormat("HHmmss").format(new Date()));

            LogHelperFactory.getTraceLogger().trace("根據MessageID決定要New那支AA");
            // 根據MessageID決定要New那支AA
            rmRes = runAA(txBRSData.getAaName(), channel);

            return rmRes;
        } catch (Throwable ex) {
            // SendEMS(SubSystem.RM, channel, EJ, ProgramName + MethodBase.GetCurrentMethod().Name, CommonReturnCode.ProgramException , ex, data);
            logContext.setProgramException(ex);
            sendEMS(logContext);
            LogHelperFactory.getTraceLogger().trace("發生例外 : " + ex.getStackTrace());
            rmRes = getRsp(step);
        } finally {
            // 離開時記MessageLog
            logContext.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logContext.setMessageFlowType(MessageFlow.Response);
            logContext.setProgramName(StringUtils.join(ProgramName, "dispatch"));
            logContext.setMessage(rmRes);
            logMessage(Level.INFO, logContext);
        }
        return rmRes;

    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return true;
    }

    /**
     * For UI 專用
     *
     * @param channel
     * @param data
     * @param txcd    交易代碼
     *                <history>
     *                <modify>
     *                <modifier>Jim</modifier>
     *                <reason>New</reason>
     *                <date>2010/3/28</date>
     *                </modify>
     *                </history>
     */
    public boolean dispatch(FEPChannel channel, RMGeneral data, String txcd) {
        @SuppressWarnings("unused")
        String rtnMsg = "";
        String flatFile = "";
        setEj(TxHelper.generateEj());
        LogData tempVar = new LogData();
        tempVar.setSubSys(SubSystem.RM);
        tempVar.setChannel(channel);
        tempVar.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        tempVar.setMessageFlowType(MessageFlow.Request);
        tempVar.setProgramName(StringUtils.join(ProgramName, "dispatch"));
        tempVar.setEj(getEj());
        LogData logData = tempVar;
        try {
            flatFile = makeMessageFromGeneral(data, txcd);
            logData.setMessage(flatFile);
            logMessage(Level.DEBUG, logData);

            txBRSData = new RMData();
            txBRSData.setFepRequest(new FEPRequest());
            txBRSData.getFepRequest().getRqHeader().setChlName("FEPMon");
            txBRSData.setFepResponse(new FEPResponse());
            txBRSData.setEj(getEj());
            txBRSData.setTxObject(data);
            txBRSData.getTxObject().getRequest().setChlName(FEPChannel.FEP.toString());
            txBRSData.setTxChannel(channel);
            txBRSData.setTxSubSystem(SubSystem.RM);
            txBRSData.setTxRequestMessage("");
            txBRSData.setLogContext(logData);
            txBRSData.setMessageID(txcd);
            txBRSData.setMsgCtl(FEPCache.getMsgctrl(txcd));
            this.setChannel(txBRSData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            txBRSData.setEj(getEj());
            txBRSData.setAaName(txcd);
            rtnMsg = runAA(txcd, channel);
            if ("SUCCESS".equals(txBRSData.getFepResponse().getRsHeader().getRsStat().getRsStatCode())) {
                return true;
            } else if (NormalRC.External_OK.equals(txBRSData.getTxObject().getResponse().getRsStatRsStateCode())) {// for新版電文寫法
                return true;
            } else {
                return false;
            }
        } catch (Throwable ex) {
            // SendEMS(SubSystem.RM, channel, EJ, ProgramName + MethodBase.GetCurrentMethod().Name, CommonReturnCode.ProgramException , ex, "");
            logData.setProgramException(ex);
            sendEMS(logData);
            // data.Response.MSGID = "1111"; //故意讓MSGID不為正常
            return false;
        } finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(ProgramName + "dispatch");
            logData.setMessage("");
            logMessage(Level.DEBUG, logData);
        }
    }

    private String runAA(String msgId, FEPChannel channel) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRmRequestData", String.class, RMData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, txBRSData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    /**
     * 發生例外時，要產生錯誤訊息和回覆時間
     *
     * @param step Req電文物件
     * @param step 錯誤代碼
     */
    private String getRsp(String step) {
        LogData log = new LogData();
        // R1001_Response rsp = new R1001_Response();
        txBRSData.setTxObject(new RMGeneral());
        txBRSData.getTxObject().getResponse().setEJNo(getEj().toString());
        txBRSData.getTxObject().getResponse().setRqTime(new SimpleDateFormat("yyyy/MM/ddTHH:mm:ss:sss").format(new Date()));
        txBRSData.getTxObject().getResponse().setRsTime(new SimpleDateFormat("yyyy/MM/ddTHH:mm:ss:sss").format(new Date()));
        txBRSData.getTxObject().getResponse().setRsStatDesc(step);
        TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.ProgramException, log);
        txBRSData.getTxObject().getResponse().setRsStatRsStateCode(log.getExternalCode());

        return makeMessageFromGeneral(txBRSData.getTxObject(), logContext.getMessageId());
    }

    public RMGeneral parseFlatfile(String data) throws Exception {
        RMGeneral rmGeneral = new RMGeneral();
        // TxMessage內的XML沒有Root,所以補上Root
        LogHelperFactory.getTraceLogger().trace("Enter ParseFlatfile ; MessageID = " + txBRSData.getMessageID());

        if (txBRSData.getMessageID().equals("R0100")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("R0101")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("RT0011")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("R0230")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("R0231")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("R0140")) {
            // TODO

        } else if (txBRSData.getMessageID().equals("R1400")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("C1400")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("RT1301")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("RIM001")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("RIM002")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("RIM003")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("RIM011")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("C1100")) {
            C1100_Request C1100 = (C1100_Request) deserializeFromXml(data, C1100_Request.class);
            C1100.toGeneral(rmGeneral);
        } else if (txBRSData.getMessageID().equals("C1200")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("R1200")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("C1000")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("R1600")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("C1600")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("C1700")) {
            C1700_Request C1700 = (C1700_Request) deserializeFromXml(data, C1700_Request.class);
            C1700.toGeneral(rmGeneral);
        } else if (txBRSData.getMessageID().equals("R1100")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("R0800")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("R2300")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("RT2301")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("R2400")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("RT1101")) {
            // TODO
        } else if (txBRSData.getMessageID().equals("R5100")) {
            // TODO
        }

        // = txBRSData.TxObject.Request.;
        return rmGeneral;
    }

    public String makeMessageFromGeneral(RMGeneral data, String txcd) {
        String flatFile = "";
        if (txcd.equals("R1000")) {

        } else if (txcd.equals("C1000")) {
            // TODO
        } else if (txcd.equals("R1100")) {
            // TODO
        } else if (txcd.equals("C1100")) {
            /*
             * C1100_Request C1100 = new C1100_Request(); todo
             * C1100.RqHeader = new FEPRqHeader();
             * C1100.SvcRq = new C1100SvcRq();
             * C1100.SvcRq.Rq = new C1100Rq();
             * flatFile = C1100.MakeMessageFromGeneral(data);
             */
        } else if (txcd.equals("R1200")) {
            // TODO
        } else if (txcd.equals("C1200")) {
            // TODO
        } else if (txcd.equals("R1400")) {
            // TODO
        } else if (txcd.equals("C1400")) {
            // TODO
        } else if (txcd.equals("R1600")) {
            // TODO
        } else if (txcd.equals("C1600")) {
            // TODO
        } else if (txcd.equals("C1700")) {
            // TODO
        } else if (txcd.equals("R2300")) {
            // TODO
        } else if (txcd.equals("R2400")) {
            // TODO
        } else if (txcd.equals("R0100")) {
            // TODO
        } else if (txcd.equals("R0101")) {
            // TODO
        } else if (txcd.equals("R0230")) {
            // TODO
        } else if (txcd.equals("R0231")) {
            // TODO
        } else if (txcd.equals("R0140")) {
            // TODO
        } else if (txcd.equals("R0800")) {
            // TODO
        } else if (txcd.equals("RT0011")) {
            // TODO
        } else if (txcd.equals("R5100")) {
            // TODO
        }
        return flatFile;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }
}
