package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.FEPRequest;
import com.syscom.fep.vo.text.FEPResponse;
import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.inbk.INBKGeneral;
import com.syscom.fep.vo.text.inbk.request.S0710Request;
import com.syscom.fep.vo.text.inbk.response.S0710Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class INBKHandler extends HandlerBase {
    private String ProgramName = StringUtils.join(this.getClass().getSimpleName(), ".");
    private INBKData txINBKData;
    private static final String S0710_ID = "S0710";
    private String _timeFormat = "yyyy/MM/ddTHH:mm:ss:sss";
    private LogData logData;

    public INBKHandler() {}

    @Override
    public String dispatch(FEPChannel channel, String data) {
        // data = data.Replace("\r\n", string.Empty);
        LogHelperFactory.getTraceLogger().trace("Dispatch rcv " + data);
        String inbkRes = "";
        // FEPResponse FEPRsp = null;
        if (getEj() == 0) {
            setEj(TxHelper.generateEj());
        }

        if (getLogContext() == null) {
            LogData tempVar = new LogData();
            tempVar.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            tempVar.setSubSys(SubSystem.INBK);
            tempVar.setChannel(channel);
            tempVar.setProgramFlowType(ProgramFlow.MsgHandlerIn);
            tempVar.setMessageFlowType(MessageFlow.Request);
            tempVar.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            tempVar.setMessage(data);
            tempVar.setEj(getEj());
            logData = tempVar;
        } else {
            logData = getLogContext();
        }

        logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        logData.setSubSys(SubSystem.INBK);
        logData.setChannel(channel);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
        logData.setMessage(data);
        logData.setEj(getEj());
        logMessage(Level.DEBUG, logData);

        // FEPRequest FEPReq = null; //Request電文物件

        try {
            // FEPReq = (FEPRequest)DeserializeFromXml(data, typeof(FEPRequest));
            // FEPRsp = new FEPResponse();

            // FEPRsp.RsHeader.ChlEJNo = FEPReq.RqHeader.ChlEJNo;
            //// modify 2010/12/10
            // FEPRsp.RsHeader.EJNo = DateTime.Now.ToString("YYYYMMDD") +EJ.ToString().PadLeft(12,'0');
            // FEPRsp.RsHeader.RqTime = FEPReq.RqHeader.ChlSendTime;
            // logData.MessageId = FEPReq.RqHeader.MsgID;

            if (data.toUpperCase().indexOf(S0710_ID.toUpperCase()) > -1) {
                logData.setChannel(channel);
                logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setMessage(data);
                logData.setMessageId(S0710_ID);
                logMessage(Level.DEBUG, logData);
                txINBKData = new INBKData();
                txINBKData.setMessageID(S0710_ID);
                txINBKData.setEj(getEj());
            } else {
                txINBKData = new INBKData();
                txINBKData.setMessageID("沒有此電文");
                txINBKData.setEj(getEj());
                throw new Exception("沒有此電文");
            }

            // var S0710Req = (S0710_Request)DeserializeFromXml(data, typeof(S0710_Request));
            // txINBKData.EJ = EJ;
            // txINBKData.MessageID = FEPReq.RqHeader.MsgID;
            // txINBKData.TxRequestMessage = data;
            // txINBKData.TxChannel = channel;
            // txINBKData.MessageFlowType = MessageFlow.Request;
            // txINBKData.LogContext = logData;
            // Parse電文
            txINBKData.setTxObject(parseFlatfile(data));
            txINBKData.setMsgCtl(FEPCache.getMsgctrl(txINBKData.getMessageID()));
            if (txINBKData.getMsgCtl() == null) {

                logData.setReturnCode(IOReturnCode.MSGCTLNotFound);
                sendEMS(logData);
                // initRsp(FEPReq, ref FEPRsp, IOReturnCode.MSGCTLNotFound); //要補上代碼
                // inbkRes = SerializeToXml(FEPRsp).Replace("&lt;", "<").Replace("&gt;", ">");
                inbkRes = getResStr(IOReturnCode.MSGCTLNotFound, txINBKData.getMessageID());
                // TO-DO throw exception
                return inbkRes;
            }

            this.setChannel(txINBKData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            txINBKData.setAaName(txINBKData.getMsgCtl().getMsgctlAaName());
            txINBKData.setTxStatus("1".equals(txINBKData.getMsgCtl().getMsgctlStatus()));
            txINBKData.setTxRequestMessage(data);
            txINBKData.setTxChannel(channel);
            txINBKData.setMessageFlowType(MessageFlow.Request);
            txINBKData.setLogContext(logData);

            LogHelperFactory.getTraceLogger().trace("根據MessageID決定要New那支AA");
            // 根據MessageID決定要New那支AA
            inbkRes = this.runAA(txINBKData.getMessageID());

            return inbkRes;
        } catch (Throwable ex) {
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.toString());
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.getStackTrace());
            // logData = new LogData();
            logData.setChannel(channel);
            logData.setMessage(data);
            logData.setMessageFlowType(MessageFlow.Request);
            if (txINBKData != null) {
                logData.setMessageId(txINBKData.getMessageID());
            }

            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setSubSys(SubSystem.INBK);
            logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            logData.setProgramException(ex);
            sendEMS(logData);
            // initRsp(FEPReq, ref FEPRsp, FEPReturnCode.ParseTelegramError); //要補上代碼
            // inbkRes = SerializeToXml(FEPRsp).Replace("&lt;", "<").Replace("&gt;", ">");
            inbkRes = getResStr(FEPReturnCode.ParseTelegramError, txINBKData.getMessageID());
            return inbkRes;
        } finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(inbkRes);
            logMessage(Level.DEBUG, logData);
        }
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return true;
    }

    public INBKGeneral parseFlatfile(String data) throws Exception {
        INBKGeneral inbkGeneral = new INBKGeneral();
        if ("S0710".equals(txINBKData.getMessageID())) {
            S0710Request rtS0710 = deserializeFromXml(data, S0710Request.class);
            rtS0710.toGeneral(inbkGeneral);
        }
        return inbkGeneral;
    }

    private String runAA(String msgId) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processInbkRequestData", String.class, INBKData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, txINBKData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    /**
     * 發生例外時，要產生錯誤訊息和回覆時間
     *
     * @param req  Req電文物件
     * @param rsp  Rsp電文物件
     * @param code 錯誤代碼
     */
    @SuppressWarnings("unused")
    private void initRsp(FEPRequest req, RefBase<FEPResponse> rsp, FEPReturnCode code) {
        if (rsp.get() == null) {
            rsp.set(new FEPResponse());
            rsp.get().getRsHeader().setChlEJNo("");
            // modify 2010/12/10
            rsp.get().getRsHeader().setEJNo(StringUtils.join(new SimpleDateFormat("YYYYMMDD").format(new Date()), StringUtils.leftPad(String.valueOf(getEj()), 12, '0')));
            rsp.get().getRsHeader().setRqTime(new SimpleDateFormat(_timeFormat).format(new Date()));
            rsp.get().getRs().setRs("");
        } else {
            rsp.get().getRsHeader().setChlEJNo(req.getRqHeader().getChlEJNo());
            rsp.get().getRs().setRs(req.getSvcRq().getRq());
        }
        rsp.get().getRsHeader().getRsStat().setRsStatCode(code.name());
        LogHelperFactory.getTraceLogger().trace(StringUtils.join("FEPReturnCode = ", code));
        rsp.get().getRsHeader().getRsStat().setDesc(TxHelper.getMessageFromFEPReturnCode(code)); // 要查錯誤碼
        LogHelperFactory.getTraceLogger().trace(StringUtils.join("Desc = ", rsp.get().getRsHeader().getRsStat().getDesc()));
        rsp.get().getRsHeader().setRsTime(new SimpleDateFormat(_timeFormat).format(new Date()));

    }

    // 組INBKAA 在建構式發生EXCEPTION處理
    private String getResStr(FEPReturnCode code, String MSGID) {
        String ResStr = "";
        @SuppressWarnings("unused")
        StringBuilder bodystring = new StringBuilder();
        if (S0710_ID.equals(MSGID)) {
            S0710Response s0710Rsp = new S0710Response();
            s0710Rsp.setRsHeader(new FEPRsHeader());
            s0710Rsp.getRsHeader().setRsStat(new FEPRsHeader.FEPRsHeaderRsStat());
            s0710Rsp.getRsHeader().getRsStat().setRsStatCode(new FEPRsHeader.FEPRsHeaderRsStatRsStatCode());
            s0710Rsp.getRsHeader().getRsStat().getRsStatCode().setValue(code.name());
            s0710Rsp.getRsHeader().getRsStat().setDesc(TxHelper.getMessageFromFEPReturnCode(code)); // 要查錯誤碼;
            s0710Rsp.getRsHeader().setRsTime(new SimpleDateFormat(_timeFormat).format(new Date()));
            s0710Rsp.getRsHeader().setChlEJNo(StringUtils.join(new SimpleDateFormat("yyyyMMdd").format(new Date()), StringUtils.leftPad(String.valueOf(getEj()), 12, '0')));
            ResStr = serializeToXml(s0710Rsp).replace("&lt;", "<").replace("&gt;", ">");
        } else {
            // 用S0710_Response回
            S0710Response errorRsp = new S0710Response();
            errorRsp.setRsHeader(new FEPRsHeader());
            errorRsp.getRsHeader().setRsStat(new FEPRsHeader.FEPRsHeaderRsStat());
            errorRsp.getRsHeader().getRsStat().setRsStatCode(new FEPRsHeader.FEPRsHeaderRsStatRsStatCode());
            errorRsp.getRsHeader().getRsStat().getRsStatCode().setValue(TxHelper.getRCFromErrorCode(String.valueOf(code.getValue()), FEPChannel.FEP, FEPChannel.BRANCH, getLogContext()));
            errorRsp.getRsHeader().getRsStat().setDesc(TxHelper.getMessageFromFEPReturnCode(code)); // 要查錯誤碼;
            errorRsp.getRsHeader().setChlEJNo(StringUtils.join(new SimpleDateFormat("yyyyMMdd").format(new Date()), StringUtils.leftPad(String.valueOf(getEj()), 12, '0')));
            errorRsp.getRsHeader().setRsTime(new SimpleDateFormat(_timeFormat).format(new Date()));
            ResStr = serializeToXml(errorRsp).replace("&lt;", "<").replace("&gt;", ">");
        }

        return ResStr;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }
}
