package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.parse.StringToFieldAnnotationParser;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.request.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 處理來自ATMGW的電文Handler類
 *
 * @author Bruce
 */
public class ATMHandlerP2 extends HandlerBase {
    private Boolean isFisc = false;
    private String atmText;           //ATM原始電文(ASCII)
    private String atmNo;             //ATM機台代號
    private String fsCode;            //ATM交易代號
    private String atmSeq;            //ATM交易流水號
    private String msgCategory;        //Message category
    private String msgType;            //Message type
    private Boolean needMakeMac = false;
    private Boolean isConfirm = false;
    private Boolean isProgram = false;
    private FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");

    private ATMGeneral atmGeneral = new ATMGeneral();

    public String getAtmText() {
        return atmText;
    }

    public void setAtmText(String atmText) {
        this.atmText = atmText;
    }

    public String getAtmNo() {
        return atmNo;
    }

    public void setAtmNo(String atmNo) {
        this.atmNo = atmNo;
    }

    public String getFsCode() {
        return fsCode;
    }

    public void setFsCode(String fsCode) {
        this.fsCode = fsCode;
    }

    public String getAtmSeq() {
        return atmSeq;
    }

    public void setAtmSeq(String atmSeq) {
        this.atmSeq = atmSeq;
    }

    public String getMsgCategory() {
        return msgCategory;
    }

    public void setMsgCategory(String msgCategory) {
        this.msgCategory = msgCategory;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    /**
     * 2022/11/04 Bruce Modify
     * SPEC:TCB-FEP-SPC_Handler_ATMHandler(P1階段ATM交易控制程式)
     * 處理流程
     * 1.記錄LOG
     * 2.將ATM電文從EBCDIC轉成ASCII,並取出相關欄位
     * 3.讀取交易控制檔MsgCtl
     * 4.呼叫AA
     * 5.回傳Response電文
     */
    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        //1.宣告變數
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        ATMData atmData = new ATMData();
        String methodName = StringUtils.join(ProgramName, ".dispatch");
        String atmRes = StringUtils.EMPTY;
        //2.取EJ
        if (this.getEj() == 0) {
            this.setEj(TxHelper.generateEj());
        }
        if (StringUtils.isBlank(this.txRquid)) {
            this.txRquid = UUIDUtil.randomUUID(true);
        }
        //3.記錄FEPLOG內容
        LogData logData = new LogData();
        logData.setEj(this.getEj());
        logData.setChannel(channel);
        logData.setSubSys(SubSystem.ATMP);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(methodName);
        logData.setMessage(data);
        logData.setRemark("Enter dispatch");
        logData.setTxRquid(this.txRquid);
        logData.setAtmNo(atmNo);
        logMessage(logData);

        try {
        	
            //4.將Hex電文轉成ASCII, 並取出特定欄位供後續流程使用
//			this.setAtmText(StringUtil.fromHex(data));
//			this.setAtmText(data);
            this.setAtmText(CodeGenUtil.ebcdicToAsciiDefaultEmpty(data));

//			this.setAtmText(EbcdicConverter.fromHex(CCSID.English, data));
//			this.setAtmText(EbcdicConverter.toHex(CCSID.English, 12, FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMM_PLAIN)));
            if (this.getAtmText().length() < 100 && "CK".equals(getAtmText().substring(32, 34))) {
                this.setAtmSeq(this.getAtmText().substring(26, 30));
                this.setFsCode(this.getAtmText().substring(32, 34));
                this.setMsgCategory(this.getAtmText().substring(11, 12));
                this.setMsgType(this.getAtmText().substring(12, 14));
                atmData.setMessageID("ChangeKeyForATM");
                logData.setAtmSeq(this.getAtmSeq());
            } else if("EBTSPM0".equals(getAtmText().substring(1, 8))) {  //40C5C2E3E2D7D4F0
            	atmData.setMessageID("ATMTxForP1");  //補摺交易轉送主機
            } else {
                //一般交易電文
                this.setAtmSeq(this.getAtmText().substring(32, 36));
                this.setFsCode(this.getAtmText().substring(73, 75));
                this.setMsgCategory(this.getAtmText().substring(17, 18));
                this.setMsgType(this.getAtmText().substring(18, 20));
                atmData.setMessageID(this.getMsgId(data));
                if(isProgram) {
                	return atmRes;
                }
                
                logData.setMessageId(atmData.getMessageID());
                logData.setAtmSeq(this.getAtmSeq());
                needMakeMac = true;
            }
            
          	//5.將相關欄位存入ATMData中
        	atmData.setTxChannel(channel);
            atmData.setTxSubSystem(SubSystem.ATMP);
            atmData.setMessageFlowType(MessageFlow.Request);
            atmData.setAtmSeq(this.getAtmSeq());
//			atmData.setTxRequestMessage(this.getAtmText());
            atmData.setEj(this.getEj());
            atmData.setMsgCategory(this.getMsgCategory());
            atmData.setMsgType(this.getMsgType());
            atmData.setFscode(this.getFsCode());
            atmData.setAtmNo(atmNo);            //改由atmservice controller帶進來
            atmData.setLogContext(logData);
            atmData.setTxObject(this.atmGeneral);    //--ben--20220930Daniel指示:給小傑可順利測試用
            
            //6.讀取MSGCTL,並存入atmData
            Msgctl msgctl = FEPCache.getMsgctrl(atmData.getMessageID());
            atmData.setMsgCtl(msgctl);
            //讀不到MsgCtl先記EMS不回ATM
            if (atmData.getMsgCtl() == null) {
                logData.setReturnCode(CommonReturnCode.Abnormal);
                logData.setExternalCode("E551");
                logData.setRemark("於MSGCTL 找不到資料");
                sendEMS(logData);
                rtnCode = FEPReturnCode.MSGCTLNotFound;
                //return atmRes;
            }else {
            	this.setChannel(atmData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                String aaName = atmData.getMsgCtl().getMsgctlAaName();
                String msgctlCbsProc = atmData.getMsgCtl().getMsgctlCbsProc();
                
                if(!isConfirm) {
                	if (!StringUtils.equals(atmData.getMessageID(), "ChangeKeyForATM") && StringUtils.isNotBlank(msgctlCbsProc) && "Y".equals(msgctlCbsProc)) {
                        isFisc = false;
                        aaName = "ProcessATMForP1";
                    }
                }else {
                	String feptxnTxDateAtm = atmGeneral.getRequest().getTRANDATE();
                	feptxnTxDateAtm = String.valueOf(Integer.valueOf(feptxnTxDateAtm.substring(0, 2)) + 2000) + feptxnTxDateAtm.substring(2, 6);
                	String feptxnAtmNo = atmNo;
                    String feptxnTxCode = atmGeneral.getRequest().getFSCODE();
                	String feptxnAtmSeqno = atmGeneral.getRequest().getTRANSEQ();
                	String feptxnMsgid = atmGeneral.getRequest().getFSCODE();
                	logData.setRemark("feptxnTxDateAtm:" + feptxnTxDateAtm + ". feptxnAtmNo:" + feptxnAtmNo + ". feptxnAtmSeqno:" + feptxnAtmSeqno + ".");
                	logMessage(logData);
                	Feptxn oldFeptxn = feptxnDao.getFEPTXNForDSA(feptxnTxDateAtm, feptxnAtmNo, feptxnTxCode, feptxnAtmSeqno, feptxnMsgid); //feptxnMsgid like 'FSCODE%'
                	if(oldFeptxn == null) {
                        feptxnTxDateAtm = String.valueOf(Integer.parseInt(feptxnTxDateAtm) - 1);
                        oldFeptxn = feptxnDao.getFEPTXNForDSA(feptxnTxDateAtm, feptxnAtmNo, feptxnTxCode, feptxnAtmSeqno, feptxnMsgid); //feptxnMsgid like 'FSCODE%'
                        if(oldFeptxn == null) {
                            needMakeMac = false;
                            throw new Exception("not get request feptxn data");
                        }
                	} //前置交易無Con電文，濾掉此電文的處理
                    else if(getAtmText().substring(17, 19).equals("FC")) {
                        return atmRes;
                    }
                	logData.setRemark("Get request Feptxn. The request Feptxn Cbs Proc = " + oldFeptxn.getFeptxnCbsProc() + ". Confirm  msgctl cbs proc = " + msgctlCbsProc + ".");
                	logMessage(logData);
                	if(StringUtils.equals(oldFeptxn.getFeptxnCbsProc(), "Y")) {
                		isFisc = false;
                        aaName = "ProcessATMForP1";
                	}
                }
                

                //7.呼叫 AA
                atmData.setAaName(aaName);
                atmData.setTxStatus(DbHelper.toBoolean(atmData.getMsgCtl().getMsgctlStatus()));
                atmData.setTxRequestMessage(data);
//                if (atmData.isTxStatus()) {
                    atmRes = this.runAA(atmData);
//                } else {
//                	rtnCode = CommonReturnCode.Abnormal;
//                	logData.setRemark("此交易暫停服務");
//                    //return atmRes;        //不允許執行交易
//                }
            }
        } catch (Throwable e) {
            logContext.setProgramException(e);
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            rtnCode = FEPReturnCode.ProgramException;
        }finally {
        	if(rtnCode != FEPReturnCode.Normal) {
        		if(StringUtils.isBlank(atmRes)) {
        			atmRes = getErrorLog(data, atmData);
        		}
                
                logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
                logData.setMessageFlowType(MessageFlow.Response);
                logData.setProgramName(methodName);
                logData.setMessage(atmRes);
                logData.setRemark("FEP Exception, Exit dispatch");
                logMessage(logData);
        	}else {
                //9.回傳AA Response電文
                logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
                logData.setMessageFlowType(MessageFlow.Response);
                logData.setProgramName(methodName);
                logData.setMessage(atmRes);
                logData.setRemark("Exit dispatch");
                logMessage(logData);
        	}
        }
        return atmRes;
    }

    private String getErrorLog(String data, ATMData atmData) {
    	String reMsg = StringUtils.EMPTY;
        String WSID = data.substring(18, 28);
        String S1 = "40";
        String RECFMT = "F0";
        String POAPUSE = "40";
        String MSGCAT = "C6";
        String MSGTYP = "D7C3";
        String TRANDATE = data.substring(40, 52);
        String TRANTIME = data.substring(52, 64);
        String TRANSEQ = data.substring(64, 72);
        String TDRSEG = data.substring(72, 76);
        String PRCRDACT = "F0";
        RefString mac = new RefString(null);
        reMsg = WSID + S1 + RECFMT + POAPUSE + MSGCAT + MSGTYP + TRANDATE + TRANTIME + TRANSEQ + TDRSEG + PRCRDACT;
        if (needMakeMac) {
            ENCHelper encHelper = new ENCHelper(atmData);
            encHelper.makeAtmMac(atmNo, remainderToF0(reMsg), mac);
            reMsg = reMsg + EbcdicConverter.toHex(CCSID.English, 8, mac.get().substring(0, 8));
        } else {
        	reMsg = reMsg + "F0F0F0F0F0F0F0F0";
        }
    	return reMsg;
    }
    
    private String runAA(ATMData atmData) throws Throwable {
        try {
            if (!isFisc) {
                Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
                Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRequestData", ATMData.class);
                String atmRes = (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, atmData);
                return atmRes;
            } else {
                Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
                Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processATMInbkRequestData", ATMData.class);
                String atmRes = (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, atmData);
                return atmRes;
            }
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    /**
     * 2022/11/07 Bruce Add 取得MSGID新邏輯
     *
     * @param atmText 電文字串
     * @return
     * @throws Exception
     */
    private String getMsgId(String atmText) throws Exception {
        String msgId = "";
        //為了判斷AA要跑哪一支轉成ASCII

        if (getAtmText().length() != 509 && getAtmText().substring(73, 75).equals("FV")) {
            //上送 ClassName： ATM_FVTrans 指靜脈建置
            ATM_FVTrans request = new StringToFieldAnnotationParser<ATM_FVTrans>(ATM_FVTrans.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        } else if (getAtmText().substring(73, 75).equals("P5") || getAtmText().substring(73, 75).equals("P6")) {
            //上送 ClassName： ATM_IntlChangeSSCode 國際卡密碼變更
            ATM_IntlChangeSSCode request = new StringToFieldAnnotationParser<ATM_IntlChangeSSCode>(ATM_IntlChangeSSCode.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        } else if (getAtmText().length() == 508 || getAtmText().length() == 383) {
            //上送 ClassName： ATM_FAA_IntlTrans 國際卡EMV
            ATM_IntlTrans request = new StringToFieldAnnotationParser<ATM_IntlTrans>(ATM_IntlTrans.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        } else if (getAtmText().length() != 509 && getAtmText().substring(73, 75).equals("P1")) {
            //上送 ClassName： ATM_FAA_TRK2ChangeSSCode 磁條密碼變更
            ATM_TRK2ChangeSSCode request = new StringToFieldAnnotationParser<ATM_TRK2ChangeSSCode>(ATM_TRK2ChangeSSCode.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        } else if (getAtmText().substring(17, 20).equals("FC6")) {
            //上送 ClassName： ATM_FAA_D9Trans  FC_非晶片(查詢企業名稱FC6)
            ATM_D9Trans request = new StringToFieldAnnotationParser<ATM_D9Trans>(ATM_D9Trans.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        } else {
            //  上送 ClassName： ATM_FAA_GeneralTrans  一般交易
            ATM_GeneralTrans request = new StringToFieldAnnotationParser<ATM_GeneralTrans>(ATM_GeneralTrans.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        }

        // 檢核FC6(FSCODE：D9) 交易送第二道電文不處理
        if (getAtmText().substring(73, 75).equals("D9") && !getAtmText().substring(18, 20).equals("C6")) {
        	//FC6前置交易只有REQ電文，MSGTYP 不為C6時略過不處理不回應(20241226修)
        	isProgram = true;
        	return msgId;
        }

        // 自行前置交易：ATM 2 way交易(C1/C2/C4/C5/C6/C7/C8/C9/CC)
        // FEP前置交易：ATM 2 way交易(T1/T2)
        if (getAtmText().substring(17, 19).equals("FC") || getAtmText().substring(17, 19).equals("FT")) {
            msgId = getAtmText().substring(17, 20);
        } else if (getAtmText().substring(73, 75).equals("TD") || getAtmText().substring(73, 75).equals("DA")) { //跨行約定轉帳
        	isFisc = true;
            if (getAtmText().substring(18, 20).equals("AA")) {
                msgId = getAtmText().substring(73, 75) + "-" + getAtmText().substring(184, 188);
            } else {
                msgId = getAtmText().substring(73, 75) + "C-" + getAtmText().substring(184, 188);
                isConfirm = true;
            }
        } else if (getAtmText().substring(73, 75).equals("P1") && !getAtmText().substring(179, 184).equals("K 200")) {//磁條卡
            if (getAtmText().substring(18, 20).equals("AA")) {
                msgId = getAtmText().substring(73, 75) + "T";
            } else {
                msgId = getAtmText().substring(73, 75) + "TC";
                isConfirm = true;
            }
        } else if (getAtmText().substring(155, 158).equals(SysStatus.getPropertyValue().getSysstatHbkno()) &&
        			((getAtmText().substring(73, 74).equals("T") && (getAtmText().substring(77, 80).trim().equals("") ||
        					getAtmText().substring(77, 80).trim().equals(SysStatus.getPropertyValue().getSysstatHbkno()) )) ||
        			(getAtmText().substring(73, 74).equals("E") && (getAtmText().substring(53, 56).trim().equals("006"))) ||
        			getAtmText().substring(73, 74).equals("I") || getAtmText().substring(73, 74).equals("D") || 
        			getAtmText().substring(73, 74).equals("F") || getAtmText().substring(73, 74).equals("W") || 
        			getAtmText().substring(73, 75).equals("P1") || getAtmText().substring(73, 75).equals("P4") ||
        			getAtmText().substring(73, 75).equals("US") || getAtmText().substring(73, 75).equals("JP") ||
        			getAtmText().substring(73, 75).equals("AW") || getAtmText().substring(73, 75).equals("NP") ||
        			getAtmText().substring(73, 75).equals("SS") || getAtmText().substring(73, 75).equals("LF"))) {
            if (getAtmText().substring(18, 20).equals("AA")) {
                msgId = getAtmText().substring(73, 75);
            } else {
                msgId = getAtmText().substring(73, 75) + "C";
                isConfirm = true;
            }
        }else if (getAtmText().substring(73, 75).equals("FV") || getAtmText().substring(73, 75).equals("I5") ||
        			getAtmText().substring(73, 75).equals("D8") || getAtmText().substring(73, 75).equals("P5") ||
        			getAtmText().substring(73, 75).equals("P6") || getAtmText().substring(73, 75).equals("D6") || 
        			getAtmText().substring(73, 75).equals("DX") || getAtmText().substring(73, 75).equals("D7") ||
                    getAtmText().substring(73, 75).equals("WP")) {
        	   if (getAtmText().substring(18, 20).equals("AA")) {
                   msgId = getAtmText().substring(73, 75);
               } else {
                   msgId = getAtmText().substring(73, 75) + "C";
                   isConfirm = true;
               }
        } else {
            // 跨行交易：ATM 4 way交易
            isFisc = true;
            if (getMsgType().equals("AA")) {
            	msgId = getAtmText().substring(73, 75) + "-" + getAtmText().substring(184, 188);
            } else {
                msgId = getAtmText().substring(73, 75) + "C-" + getAtmText().substring(184, 188);
                isConfirm = true;
            }
        }

        return msgId;
    }
    
    private String remainderToF0(String inputData) {
        String rtnStr = "";
        int instr = inputData.length();
        instr = instr / 2;
        if (instr % 8 != 0) {
            int remainder = instr % 8;
            remainder = 8 - remainder;
            rtnStr = StringUtils.rightPad(rtnStr, remainder, '0');
            rtnStr = EbcdicConverter.toHex(CCSID.English, rtnStr.length(), rtnStr);
        }

        return inputData + rtnStr;
    }
    
    @Override
    public String dispatch(FEPChannel channel, String data) {
        return "";
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) {
        return true;
    }

}
