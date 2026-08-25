package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.MFTData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.inbk.response.S0710Response;
import com.syscom.fep.vo.text.mft.MFTGeneral;
import com.syscom.fep.vo.text.mft.request.FA_IBAF5102;
import com.syscom.fep.vo.text.mft.request.FA_IBAFRCV1;
import com.syscom.fep.vo.text.mft.request.FA_IPUFRCV1;
import com.syscom.fep.vo.text.mft.request.FE_IPUFPUT1;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MFTHandler extends HandlerBase {
    private MFTData mftData;
    private String ProgramName = StringUtils.join(this.getClass().getSimpleName(), ".");
    private String _timeFormat = "yyyy/MM/dd HH:mm:ss:sss";
    private LogData logData;

    @Override
    public String dispatch(FEPChannel channel, String data) {
        LogHelperFactory.getTraceLogger().trace("Dispatch rcv " + data);
        String res = "";
        MFTGeneral mftGeneral = new MFTGeneral();
        try {
            logData = new LogData();
            logData.setSubSys(SubSystem.MFT);
//            logData.setChannel(FEPChannel.MFT);
            logData.setProgramFlowType(ProgramFlow.MFTHandlerIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
            logData.setEj(this.getEj());
            logData.setMessageId(this.getMessageId());
            logData.setMessage(StringUtils.EMPTY);
            mftData = new MFTData();

            //拆解電文
            if (StringUtils.isNotBlank(data)) {
                switch (data.substring(4, 12)) {
                    case "IBAF5102":
                        FA_IBAF5102 ibaf5102 = new FA_IBAF5102();
                        mftGeneral = ibaf5102.parseFlatfile(data);
                        break;
                    case "IBAFRCV1":
                        FA_IBAFRCV1 ibafrcv1 = new FA_IBAFRCV1();
                        mftGeneral = ibafrcv1.parseFlatfile(data);
                        break;
                    case "IPUFRCV1":
                        FA_IPUFRCV1 ipufrcv1 = new FA_IPUFRCV1();
                        mftGeneral = ipufrcv1.parseFlatfile(data);
                        break;
                    // case "IPUFPUT1":
                    //     FE_IPUFPUT1 ipufput1 = new FE_IPUFPUT1();
                    //     mftGeneral = ipufput1.parseFlatfile(data);
                    //     break;
                }
            }
            //取得MSGID
            Msgctl msgctl = FEPCache.getMsgctrl(mftGeneral.getRequest().getTRAN_CODE() + mftGeneral.getRequest().getTD_CODE());
            mftData.setMsgCtl(msgctl);
            this.setChannel(mftData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            mftData.setMessageID(mftGeneral.getRequest().getTRAN_CODE() + mftGeneral.getRequest().getTD_CODE());
//            mftData.setTxChannel(FEPChannel.MFT);
            mftData.setLogContext(logData);
            mftData.setAaName(mftData.getMsgCtl().getMsgctlAaName());
            mftData.setTxMFTObject(mftGeneral);
            mftData.setEj(this.getEj());
            res = this.runAA(mftData.getMessageID());

            return res;
        } catch (Throwable ex) {
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.toString());
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.getStackTrace());
            // logData = new LogData();
            logData.setChannel(channel);
            logData.setMessage(data);
            logData.setMessageFlowType(MessageFlow.Request);
            if (mftData != null) {
                logData.setMessageId(mftData.getMessageID());
            }

            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setSubSys(SubSystem.MFT);
            logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            logData.setProgramException(ex);
            sendEMS(logData);
            return "";
        } finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(res);
            logMessage(Level.DEBUG, logData);
        }
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return false;
    }


    private String runAA(String msgId) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processMFTRequestData", MFTData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, mftData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }


    // 組INBKAA 在建構式發生EXCEPTION處理
    private String getResStr(FEPReturnCode code, String MSGID) {
        String ResStr = "";
        @SuppressWarnings("unused")
        StringBuilder bodystring = new StringBuilder();
//		if (S0710_ID.equals(MSGID)) {
//			S0710Response s0710Rsp = new S0710Response();
//			s0710Rsp.setRsHeader(new FEPRsHeader());
//			s0710Rsp.getRsHeader().setRsStat(new FEPRsHeader.FEPRsHeaderRsStat());
//			s0710Rsp.getRsHeader().getRsStat().setRsStatCode(new FEPRsHeader.FEPRsHeaderRsStatRsStatCode());
//			s0710Rsp.getRsHeader().getRsStat().getRsStatCode().setValue(code.name());
//			s0710Rsp.getRsHeader().getRsStat().setDesc(TxHelper.getMessageFromFEPReturnCode(code)); // 要查錯誤碼;
//			s0710Rsp.getRsHeader().setRsTime(new SimpleDateFormat(_timeFormat).format(new Date()));
//			s0710Rsp.getRsHeader().setChlEJNo(StringUtils.join(new SimpleDateFormat("yyyyMMdd").format(new Date()), StringUtils.leftPad(String.valueOf(getEj()), 12, '0')));
//			ResStr = serializeToXml(s0710Rsp).replace("&lt;", "<").replace("&gt;", ">");
//		} else {
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
//		}

        return ResStr;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }


}
