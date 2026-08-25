package com.syscom.fep.server.common.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISCHeader;
import com.syscom.fep.vo.text.fisc.FISC_CLR;
import com.syscom.fep.vo.text.fisc.FISC_EMVIC;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.fisc.FISC_OPC;

public class CBSHandler extends HandlerBase {
    private static final String ProgramName = StringUtils.join(CBSHandler.class.getSimpleName(), "");
    private FISCGeneral general;
    private List<String> uiPcodeList = new ArrayList<>();

    private void setUiPcodeListData() {
    	uiPcodeList.add("2130");
    	uiPcodeList.add("2140");
    	uiPcodeList.add("2280");
    	uiPcodeList.add("2290");
    	uiPcodeList.add("2573");
    }
    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        return null;
    }

    @Override
	public String dispatch(FEPChannel channel, String data) {
    	setUiPcodeListData();
    	
		RefBase<SubSystem> refSubSystem = new RefBase<SubSystem>(null);
		RefBase<FISCSubSystem> refFISCSubSystem = new RefBase<FISCSubSystem>(null);
		this.getFISCSubSystem(data, refSubSystem, refFISCSubSystem);
		SubSystem subSystem = refSubSystem.get();
		FISCSubSystem fiscSubSystem = refFISCSubSystem.get();
		return dispatch(subSystem, fiscSubSystem, channel, data);
    }
    
    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return false;
    }
    
    public String dispatch(SubSystem subSystem, FISCSubSystem fiscSubSystem, FEPChannel channel, String data) {
    //1.Handler進入點
        String ctfRes = StringUtils.EMPTY;
        String programName = StringUtils.join(ProgramName, ".dispatch");
        FISCData fiscData = new FISCData();
        general = new FISCGeneral();
        general.setSubSystem(fiscSubSystem);

        this.setEj(TxHelper.generateEj());

        //記錄FEPLOG內容
        LogData logData = new LogData();
        logData.setChannel(channel);
        logData.setSubSys(subSystem);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(programName);
        logData.setEj(this.getEj());
        logData.setTxRquid(this.txRquid);
        logData.setMessage(data);
        logData.setRemark("Enter dispatch");
        logMessage(Level.DEBUG, logData);

        try {
            //2. 拆解電文, 取得MSGID
            RefBase<FISCHeader> refFiscHeader = new RefBase<FISCHeader>(null);
            FEPReturnCode rtn = this.getFISCRequestHeader(fiscSubSystem, data, refFiscHeader);// 拆財金電文Header 判斷狀態
            FISCHeader fiscHeader = refFiscHeader.get();
            String msgId = fiscHeader.getProcessingCode() + fiscHeader.getMessageType().substring(2);
            String aaName = "CBSRequestI";
            if("02".equals(fiscHeader.getMessageType().substring(2))) {
            	aaName = "CBSConfirmI";
            }
            
            //2開頭電文處理流程
			if(StringUtils.equals("2", fiscHeader.getProcessingCode().substring(0,1))) {
				Msgctl msgctl = null;
				String selPCode = fiscHeader.getProcessingCode();
				if(uiPcodeList.contains(selPCode)) {
					selPCode = msgId;
				}
				msgctl = FEPCache.getMsgctrlForIMS(selPCode);
				
				if(msgctl != null ) {
					msgctl.setMsgctlMsgid(msgId);
				}
				
				fiscData.setMsgCtl(msgctl);
			}else {
				fiscData.setMsgCtl(FEPCache.getMsgctrl(msgId));
			}
			
            this.setChannel(fiscData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            fiscData.setEj(this.getEj());
            fiscData.setMessageID(msgId);
            fiscData.setTxChannel(channel);
            fiscData.setTxSubSystem(subSystem);
            fiscData.setFiscTeleType(fiscSubSystem);
            fiscData.setTxRquid(this.txRquid);
            fiscData.setMessageFlowType(fiscHeader.getMessageKind());
            fiscData.setStan(fiscHeader.getSystemTraceAuditNo());
            fiscData.setTxObject(this.general);
            fiscData.setLogContext(logData);
            fiscData.setTxRequestMessage(data);
            logData.setMessageId(msgId);

            //3.CALL AA
            //FISCdata 為主機下送財金電文

            if (rtn == FEPReturnCode.Normal) {
                if (fiscData.getMsgCtl() != null) {
                    fiscData.setAaName(aaName);
                    fiscData.setTxStatus(DbHelper.toBoolean(fiscData.getMsgCtl().getMsgctlStatus()));
                    
                    ctfRes = this.runAA(fiscData);
                } else {
                    logData.setReturnCode(CommonReturnCode.Abnormal);
                    logData.setExternalCode("E551");
                    logData.setMessage("MessageID:" + msgId);
                    logData.setRemark("於MSGCTL 找不到資料");
                    logMessage(logData);
                    return ctfRes;
                }
            }

            //4.回傳AA Response電文( if need)
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(programName);
            logData.setMessage(ctfRes);
            logData.setRemark("Exit dispatch");
            logMessage(logData);
            return ctfRes;
        } catch (Throwable t) {
            logData.setProgramException(t);
            sendEMS(logData);
        }
        return ctfRes;
    }

    private FEPReturnCode getFISCRequestHeader(FISCSubSystem fiscSubSystem, String data, RefBase<FISCHeader> msgReq) {
        FISC_INBK tmpFISC_INBK;
        FISC_OPC tmpFISC_OPC;
        FISC_CLR tmpFISC_CLR;
        FISC_EMVIC tmpFISC_EMVIC;
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        switch (fiscSubSystem) {
            case INBK:
                tmpFISC_INBK = new FISC_INBK(data);
                rtnCode = tmpFISC_INBK.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_INBK.getMessageKind() == MessageFlow.Request) {
                        general.setINBKRequest(tmpFISC_INBK);
                        general.setINBKResponse(new FISC_INBK());
                        general.setINBKConfirm(new FISC_INBK());
                        msgReq.set(general.getINBKRequest());
                    }
                    if (tmpFISC_INBK.getMessageKind() == MessageFlow.Response) {
                        general.setINBKRequest(new FISC_INBK());
                        general.setINBKResponse(tmpFISC_INBK);
                        general.setINBKConfirm(new FISC_INBK());
                        msgReq.set(general.getINBKResponse());
                    }
                    if (tmpFISC_INBK.getMessageKind() == MessageFlow.Confirmation) {
                        general.setINBKRequest(new FISC_INBK());
                        general.setINBKResponse(new FISC_INBK());
                        general.setINBKConfirm(tmpFISC_INBK);
                        msgReq.set(general.getINBKConfirm());
                    }
                }
                break;
            case EMVIC:
                tmpFISC_EMVIC = new FISC_EMVIC(data);
                rtnCode = tmpFISC_EMVIC.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_EMVIC.getMessageKind() == MessageFlow.Request) {
                        general.setEMVICRequest(tmpFISC_EMVIC);
                        general.setEMVICResponse(new FISC_EMVIC());
                        general.setEMVICConfirm(new FISC_EMVIC());
                        msgReq.set(general.getEMVICRequest());
                    }
                    if (tmpFISC_EMVIC.getMessageKind() == MessageFlow.Response) {
                        general.setEMVICRequest(new FISC_EMVIC());
                        general.setEMVICResponse(tmpFISC_EMVIC);
                        general.setEMVICConfirm(new FISC_EMVIC());
                        msgReq.set(general.getEMVICResponse());
                    }
                    if (tmpFISC_EMVIC.getMessageKind() == MessageFlow.Confirmation) {
                        general.setEMVICRequest(new FISC_EMVIC());
                        general.setEMVICResponse(new FISC_EMVIC());
                        general.setEMVICConfirm(tmpFISC_EMVIC);
                        msgReq.set(general.getEMVICConfirm());
                    }
                }
                break;
            case OPC:
                tmpFISC_OPC = new FISC_OPC(data);
                rtnCode = tmpFISC_OPC.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_OPC.getMessageKind() == MessageFlow.Request) {
                        general.setOPCRequest(tmpFISC_OPC);
                        general.setOPCResponse(new FISC_OPC());
                        general.setOPCConfirm(new FISC_OPC());
                        msgReq.set(general.getOPCRequest());
                    }
                    if (tmpFISC_OPC.getMessageKind() == MessageFlow.Response) {
                        general.setOPCRequest(new FISC_OPC());
                        general.setOPCResponse(tmpFISC_OPC);
                        general.setOPCConfirm(new FISC_OPC());
                        msgReq.set(general.getOPCResponse());
                    }
                }
                break;
            case CLR:
                tmpFISC_CLR = new FISC_CLR(data);
                rtnCode = tmpFISC_CLR.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_CLR.getMessageKind() == MessageFlow.Request) {
                        general.setCLRRequest(tmpFISC_CLR);
                        general.setCLRResponse(new FISC_CLR());
                        msgReq.set(general.getCLRRequest());
                    }
                    if (tmpFISC_CLR.getMessageKind() == MessageFlow.Response) {
                        general.setCLRRequest(new FISC_CLR());
                        general.setCLRResponse(tmpFISC_CLR);
                        msgReq.set(general.getCLRResponse());
                    }
                }
                break;
            default:
                rtnCode = FEPReturnCode.Abnormal;
                break;
        }
        return rtnCode;
    }

    private String runAA(FISCData fiscData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRequestIMSData", FISCData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, fiscData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }

    }
    
    private void getFISCSubSystem(final String data, RefBase<SubSystem> refSubSystem, RefBase<FISCSubSystem> refFISCSubSystem) {
        String msgId = data.substring(14, 16);
        switch (msgId) {
            case "30":
            case "33":
            case "F0":
            case "F3":
                refSubSystem.set(SubSystem.INBK);
                refFISCSubSystem.set(FISCSubSystem.OPC);
                break;
            case "32":
            case "F2":
                refSubSystem.set(SubSystem.INBK);
                if(data.substring(14, 18).equals("3236") || data.substring(14, 18).equals("F2F6")) {
                	refFISCSubSystem.set(FISCSubSystem.EMVIC);
                }else {
                	refFISCSubSystem.set(FISCSubSystem.INBK);
                }
//                refFISCSubSystem.set(data.substring(14, 18).equals("3236") ? FISCSubSystem.EMVIC : FISCSubSystem.INBK);
                break;
            case "31":
            case "F1":
                refSubSystem.set(SubSystem.RM);
                refFISCSubSystem.set(data.substring(14, 18).equals("3136") ? FISCSubSystem.FCRM : FISCSubSystem.RM);
                break;
            case "35":
            case "F5":
                refSubSystem.set(SubSystem.INBK);
                refFISCSubSystem.set(data.substring(14, 18).equals("3538") ? FISCSubSystem.FCCLR : FISCSubSystem.CLR);
                break;
        }
    }
}
