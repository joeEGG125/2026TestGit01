package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.frmcommon.parse.StringToFieldAnnotationParser;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.request.ATM_GeneralTrans;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class POSHandler extends HandlerBase {
    private Boolean isFisc = false;
    private String ProgramName = StringUtils.join(this.getClass().getSimpleName(), ".");
    private ATMData atmData;
    private String atmText;            //ATM原始電文(ASCII)
    private String atmNo;            //ATM機台代號
    private String fsCode;            //ATM交易代號
    private String atmSeq;            //ATM交易流水號
    private String msgCategory;        //Message category
    private String msgType;            //Message type
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

    public POSHandler() {
    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        //1.宣告變數
        atmData = new ATMData();
        String methodName = StringUtils.join(ProgramName, ".dispatch");
        String atmRes = StringUtils.EMPTY;
        //2.取EJ
        if (this.getEj() == 0) {
            this.setEj(TxHelper.generateEj());
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
        logData.setTxRquid(this.txRquid);
        logData.setRemark("Enter dispatch");
        logMessage(Level.DEBUG, logData);

        try {
            //4.將Hex電文轉成ASCII, 並取出特定欄位供後續流程使用
            this.setAtmText(data);

            String tempText = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data);
            if (atmText.length() < 100 && "C3D2".equalsIgnoreCase(atmText.substring(64, 68))) {
                //this.setAtmText(EbcdicConverter.fromHex(CCSID.English, data.substring(0, 70)));
                //換KEY電文
                this.setAtmNo(tempText.substring(0, 8));
                this.setAtmSeq(tempText.substring(26, 30));
                this.setFsCode(tempText.substring(32, 34));
                this.setMsgCategory(tempText.substring(11, 12));
                this.setMsgType(tempText.substring(12, 14));
                atmData.setMessageID("ChangeKeyForATM");
            } else {
                //一般交易電文
				ATM_GeneralTrans request = new StringToFieldAnnotationParser<ATM_GeneralTrans>(ATM_GeneralTrans.class).readIn(data);
				request.toGeneral(atmGeneral);
				this.setAtmNo(atmGeneral.getRequest().getIPYDATA().substring(10, 18));
                this.setAtmSeq(atmGeneral.getRequest().getTRANSEQ());
                this.setFsCode(atmGeneral.getRequest().getFSCODE());
                this.setMsgCategory(atmGeneral.getRequest().getMSGCAT());
                this.setMsgType(atmGeneral.getRequest().getMSGTYP());
                atmData.setMessageID(this.getMsgId(tempText));
            }

            //5.將相關欄位存入ATMData中
            atmData.setTxChannel(channel);
            atmData.setTxSubSystem(SubSystem.ATMP);
            atmData.setMessageFlowType(MessageFlow.Request);
            atmData.setAtmSeq(this.getAtmSeq());
            atmData.setTxRequestMessage(this.getAtmText());
            atmData.setEj(this.getEj());
            atmData.setMsgCategory(this.getMsgCategory());
            atmData.setMsgType(this.getMsgType());
            atmData.setFscode(this.getFsCode());
            atmData.setAtmNo(this.getAtmNo());            //改由atmservice controller帶進來
            atmData.setLogContext(logData);
            atmData.setTxObject(this.atmGeneral);    //--ben--20220930Daniel指示:給小傑可順利測試用
            logData.setAtmNo(this.getAtmNo());
            logData.setMessageId(atmData.getMessageID());
            logData.setAtmSeq(atmData.getAtmSeq());
            //6.讀取MSGCTL,並存入atmData
            Msgctl msgctl = null;
        	if(StringUtils.isNotBlank(atmData.getMessageID())) {
        		msgctl = FEPCache.getMsgctrl(atmData.getMessageID());
        	}
            atmData.setMsgCtl(msgctl);
            this.setChannel(atmData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            //讀不到MsgCtl先記EMS不回ATM
            if (atmData.getMsgCtl() == null) {
                // logData.setReturnCode(CommonReturnCode.Abnormal);
                // logData.setExternalCode("E551");
                // logData.setRemark("於MSGCTL 找不到資料");
                // sendEMS(logData);
                // return atmRes;
                throw new Exception("Can't find " + atmData.getMessageID() + " in MSGCTL");
            }
            
            //7.呼叫 AA
            atmData.setAaName(msgctl.getMsgctlAaName());
            atmData.setTxStatus(atmData.getMsgCtl().getMsgctlStatus() == 1);
            atmData.setTxRequestMessage(data);
//            if (atmData.isTxStatus()) {
                atmRes = this.runAA(atmData);
//            } else {
//                return atmRes;        //不允許執行交易
//            }
            //8.回傳AA Response電文
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(methodName);
            logData.setMessage(atmRes);
            logData.setRemark("Exit dispatch");
            logMessage(logData);
            return atmRes;

        } catch (Throwable t) {
            logData.setProgramException(t);
            sendEMS(logData);
            //換key電文回FPC
            if (atmText.length() < 100) {
                atmRes = data.substring(0, 22)
                        + "C6D7C3"
                        + data.substring(28, 60)
                        + "F0F2"
                        + data.substring(64);
            }

        }
        return atmRes;
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
     * 3. 	取得ATM MSGID
     * 交易電文依代理交易Request 電文，提供 MSGID
     */
    private String getMsgId(String atmText) throws Exception {
        String msgId = "";
        //餘額查詢、繳費
//        if (atmText.length() == 509) {
            if (StringUtils.equals(atmText.substring(155, 158), FEPCache.getSysstat().getSysstatHbkno())
            		&& ((StringUtils.equals(atmText.substring(73, 74), "E") && StringUtils.equals(atmText.substring(53, 56), "006"))
            				|| StringUtils.equals(atmText.substring(73, 74), "I"))) { //自行交易
                if (StringUtils.equals(atmText.substring(18, 20), "AA")) { //Request電文
                    msgId = atmText.substring(73, 75);
                } else { //Confirm電文
                    msgId = atmText.substring(73, 75) + "C";
                }
            } else {
                // 跨行交易ATM 4 way交易
                isFisc = true;
                if (StringUtils.equals(atmText.substring(18, 20), "AA")) { //Request電文
                    msgId = atmText.substring(73, 75) + "-" + atmText.substring(184, 188);
                } else { //Confirm電文
                    msgId = atmText.substring(73, 75) + "C-" + atmText.substring(184, 188);
                }
            }
//        }
        return msgId;
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return true;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }
}