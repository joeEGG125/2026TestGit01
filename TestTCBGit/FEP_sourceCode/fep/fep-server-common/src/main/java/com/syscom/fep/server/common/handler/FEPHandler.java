package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 從UI或是批次發動要送往財金的交易(Channel=FEP)
 * <p>
 * 從UI或是批次發動要送往外部(不一定是財金)的交易, 都改呼叫FEPHandler
 *
 * @author Richard
 */
public class FEPHandler extends HandlerBase {
    private FISCGeneral general;
    private String RClientID;

    public FEPHandler() {}

    @Override
    public String dispatch(FEPChannel channel, String data) {
        return null;
    }

    /**
     * @param channel
     * @param atmNo
     * @param data
     * @return
     * @throws Exception
     */
    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) throws Exception {
        return null;
    }

    /**
     * @param subSystem
     * @param fiscSubSystem
     * @param channel
     * @param data
     * @return
     */
    public String dispatch(SubSystem subSystem, FISCSubSystem fiscSubSystem, FEPChannel channel, String data) {
        return null;
    }

    /**
     * For UI 專用
     *
     * @throws Exception
     */
    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        this.general = (FISCGeneral) data;
        FISCData fData = null;
        //20221116 Bruce add
        String msgId = StringUtils.EMPTY;
        FISCHeader header = null;
        this.setEj(TxHelper.generateEj());
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.INBK);
        logData.setChannel(channel);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
        logData.setEj(this.getEj());
        logData.setTxRquid(this.txRquid);
        logData.setMessage(StringUtils.EMPTY);
        logData.setBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        try {
            if (this.general.getSubSystem() == FISCSubSystem.INBK) {
                // add By Maxine on 2011/09/02 for EMS加上TxUser
                if (this.general.getINBKRequest().getLogContext() != null) {
                    logData.setTxUser(this.general.getINBKRequest().getLogContext().getTxUser());
                }
                logData.setSubSys(SubSystem.INBK);
                logMessage(Level.DEBUG, logData);

                header = this.general.getINBKRequest();
                fData = this.prepareFISCData(SubSystem.INBK, general.getSubSystem(), channel, StringUtils.EMPTY);
                fData.setLogContext(logData);
                fData.getTxObject().setINBKRequest(this.general.getINBKRequest());
                fData.getTxObject().setINBKResponse(new FISC_INBK());
                fData.getTxObject().setINBKConfirm(new FISC_INBK());
                fData.setEj(this.getEj());
                fData.setTxRquid(this.txRquid);
                logData.setTxRquid(this.txRquid);
                msgId = this.getMsgId(this.general.getINBKRequest());
                fData.setMessageID(msgId);
                logData.setMessageId(msgId);
                fData.setMsgCtl(FEPCache.getMsgctrl(msgId));
                this.setChannel(fData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                fData.setEj(this.getEj());
                fData.setTxRquid(this.txRquid);
                logData.setTxRquid(this.txRquid);
                fData.setAaName(fData.getMsgCtl().getMsgctlAaName());
                fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
                this.runAA(msgId, header, fData);
                this.general.setINBKResponse(fData.getTxObject().getINBKResponse());
            } else if (this.general.getSubSystem() == FISCSubSystem.OPC) {
                // add By Maxine on 2011/09/02 for EMS加上TxUser
                if (this.general.getOPCRequest().getLogContext() != null) {
                    logData.setTxUser(this.general.getOPCRequest().getLogContext().getTxUser());
                }
                logData.setSubSys(SubSystem.INBK);
                logMessage(Level.DEBUG, logData);

                header = this.general.getOPCRequest();
                fData = this.prepareFISCData(SubSystem.INBK, general.getSubSystem(), channel, StringUtils.EMPTY);
                fData.setLogContext(logData);
                fData.getTxObject().setOPCRequest(this.general.getOPCRequest());
                fData.getTxObject().setOPCResponse(new FISC_OPC());
                fData.getTxObject().setOPCConfirm(new FISC_OPC());
                msgId = this.getMsgId(this.general.getOPCRequest());
                fData.setMessageID(msgId);
                logData.setMessageId(msgId);
                fData.setMsgCtl(FEPCache.getMsgctrl(msgId));
                this.setChannel(fData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                fData.setEj(this.getEj());
                fData.setTxRquid(this.txRquid);
                logData.setTxRquid(this.txRquid);
                fData.setAaName(fData.getMsgCtl().getMsgctlAaName());
                this.runAA(msgId, header, fData);
                this.general.setOPCResponse(fData.getTxObject().getOPCResponse());
            } else if (this.general.getSubSystem() == FISCSubSystem.RM) {
                // add By Maxine on 2011/09/02 for EMS加上TxUser
                if (this.general.getRMRequest().getLogContext() != null) {
                    logData.setTxUser(general.getRMRequest().getLogContext().getTxUser());
                }
                logData.setSubSys(SubSystem.RM);
                logMessage(Level.DEBUG, logData);

                header = this.general.getRMRequest();
                fData = this.prepareFISCData(SubSystem.RM, general.getSubSystem(), channel, StringUtils.EMPTY);
                fData.setMessageFlowType(MessageFlow.Request);
                fData.setLogContext(logData);
                fData.getTxObject().setRMRequest(general.getRMRequest());
                fData.getTxObject().setRMResponse(new FISC_RM());
                msgId = this.getMsgId(general.getRMRequest());
                fData.setMessageID(msgId);
                logData.setMessageId(msgId);
                fData.setMsgCtl(FEPCache.getMsgctrl(msgId));
                this.setChannel(fData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                fData.setEj(this.getEj());
                fData.setTxRquid(this.txRquid);
                logData.setTxRquid(this.txRquid);
                fData.setAaName(fData.getMsgCtl().getMsgctlAaName());
                this.runRMAA(msgId, header, fData);
                this.general.setRMResponse(fData.getTxObject().getRMResponse());
            } else if (this.general.getSubSystem() == FISCSubSystem.CLR) {
                // add By Maxine on 2011/09/02 for EMS加上TxUser
                if (this.general.getCLRRequest().getLogContext() != null) {
                    logData.setTxUser(this.general.getCLRRequest().getLogContext().getTxUser());
                }
                logData.setSubSys(SubSystem.INBK);
                logMessage(Level.DEBUG, logData);

                header = this.general.getCLRRequest();
                fData = this.prepareFISCData(SubSystem.INBK, this.general.getSubSystem(), channel, StringUtils.EMPTY);
                fData.setLogContext(logData);
                fData.getTxObject().setCLRRequest(general.getCLRRequest());
                fData.getTxObject().setCLRResponse(new FISC_CLR());
                msgId = this.getMsgId(this.general.getCLRRequest());
                fData.setMessageID(msgId);
                logData.setMessageId(msgId);
                fData.setMsgCtl(FEPCache.getMsgctrl(msgId));
                this.setChannel(fData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                fData.setEj(this.ej);
                fData.setTxRquid(this.txRquid);
                fData.setAaName(fData.getMsgCtl().getMsgctlAaName());
                this.runAA(msgId, header, fData);
                this.general.setCLRResponse(fData.getTxObject().getCLRResponse());
            } else if (this.general.getSubSystem() == FISCSubSystem.FCCLR) {
                // add By Maxine on 2011/09/02 for EMS加上TxUser
                if (this.general.getFCCLRRequest().getLogContext() != null) {
                    logData.setTxUser(this.general.getFCCLRRequest().getLogContext().getTxUser());
                }
                logData.setSubSys(SubSystem.INBK);
                logMessage(Level.DEBUG, logData);

                header = this.general.getFCCLRRequest();
                fData = this.prepareFISCData(SubSystem.INBK, general.getSubSystem(), channel, StringUtils.EMPTY);
                fData.setLogContext(logData);
                fData.getTxObject().setFCCLRRequest(this.general.getFCCLRRequest());
                fData.getTxObject().setFCCLRResponse(new FISC_USDCLR());
                msgId = this.getMsgId(general.getFCCLRRequest());
                fData.setMessageID(msgId);
                logData.setMessageId(msgId);
                fData.setMsgCtl(FEPCache.getMsgctrl(msgId));
                this.setChannel(fData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                fData.setEj(this.ej);
                fData.setTxRquid(this.txRquid);
                fData.setAaName(fData.getMsgCtl().getMsgctlAaName());
                this.runAA(msgId, header, fData);
                this.general.setFCCLRResponse(fData.getTxObject().getFCCLRResponse());
            } else if (this.general.getSubSystem() == FISCSubSystem.EMVIC) {
                // Fly 2016/08/31 For EMV
                if (this.general.getEMVICRequest().getLogContext() != null) {
                    logData.setTxUser(this.general.getEMVICRequest().getLogContext().getTxUser());
                }
                logData.setSubSys(SubSystem.INBK);
                logMessage(Level.DEBUG, logData);

                header = this.general.getEMVICRequest();
                fData = this.prepareFISCData(SubSystem.INBK, this.general.getSubSystem(), channel, StringUtils.EMPTY);
                fData.setLogContext(logData);
                fData.getTxObject().setEMVICRequest(this.general.getEMVICRequest());
                fData.getTxObject().setEMVICResponse(new FISC_EMVIC());
                fData.getTxObject().setEMVICConfirm(new FISC_EMVIC());
                msgId = this.getMsgId(this.general.getEMVICRequest());
                fData.setMessageID(msgId);
                logData.setMessageId(msgId);
                fData.setMsgCtl(FEPCache.getMsgctrl(msgId));
                this.setChannel(fData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                fData.setEj(this.ej);
                fData.setTxRquid(this.txRquid);
                fData.setAaName(fData.getMsgCtl().getMsgctlAaName());
                this.runAA(msgId, header, fData);
                this.general.setEMVICResponse(fData.getTxObject().getEMVICResponse());
            }
            return true;
        } catch (Throwable e) {
            logData.setProgramException(e);
            sendEMS(logData);
            return false;
        } finally {
            // 記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(StringUtils.EMPTY);
            logMessage(Level.DEBUG, logData);
        }
    }

    private FISCData prepareFISCData(SubSystem subSystem, FISCSubSystem fiscSubSystem, FEPChannel channel, String data) {
        FISCData fData = new FISCData();
        fData.setTxObject(this.general);
        fData.setTxChannel(channel);
        fData.setTxSubSystem(subSystem);
        fData.setFiscTeleType(fiscSubSystem);
        fData.setTxRequestMessage(data);
        fData.setEj(this.getEj());
        fData.setTxRquid(this.txRquid);
        fData.setRClientID(this.RClientID);
        return fData;
    }

    private String getMsgId(FISCHeader msgReq) {
        // 讀出交易控制檔本筆交易資料
        // INBK MsgID=PCODE+MsgType後2碼
        String msgId = StringUtils.join(msgReq.getProcessingCode(), msgReq.getMessageType().substring(2));
        if ("0202".equals(msgReq.getMessageType()) && "2000".equals(msgReq.getProcessingCode())) {
            msgId = msgReq.getSyncCheckItem();
        }
        return msgId;
    }

    private String runAA(String msgId, FISCHeader msgReq, FISCData fData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processInbkRequestData", String.class, FISCHeader.class, FISCData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, msgReq, fData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    private String runRMAA(String msgId, FISCHeader msgReq, FISCData fData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRmRequestData", String.class, FISCHeader.class, FISCData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, msgReq, fData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    public String getRClientID() {
        return RClientID;
    }

    public void setRClientID(String RClientID) {
        this.RClientID = RClientID;
    }
}
