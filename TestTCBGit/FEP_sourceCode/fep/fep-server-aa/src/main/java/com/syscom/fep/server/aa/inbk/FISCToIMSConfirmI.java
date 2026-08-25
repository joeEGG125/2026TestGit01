package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.CBSType;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.adapter.CBSAdapter;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.text.fisc.FISCHeader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Calendar;

public class FISCToIMSConfirmI extends INBKAABase {
    private String fiscTeleType = StringUtils.EMPTY;
    private FISCHeader fiscHeader;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private String rtnMessage = StringUtils.EMPTY;
    private String pCode = null;

    public FISCToIMSConfirmI(FISCData txnData) throws Exception {
        super(txnData);
        fiscTeleType = getTxData().getFiscTeleType().toString();
        switch (fiscTeleType) {
            case "EMVIC":
                fiscHeader = getFiscEMVICCon();
                break;
            case "OPC":
                fiscHeader = getFiscOPCCon();
                break;
            case "INBK":
                fiscHeader = getFiscCon();
                break;
        }
    }

    @Override
    public String processRequestData() throws Exception {
        try {
            // 記錄LOG
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Confirmation);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage(this.getTxData().getTxRequestMessage());
            this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(this.logContext);
            pCode = getFiscBusiness().getFeptxn().getFeptxnPcode();
            //1.    檢核財金電文 Header
            rtnCode = getFiscBusiness().checkHeader(fiscHeader, true);
            this.logContext.setRemark("checkHeader : " + rtnCode);
            logMessage(this.logContext);

            /* 2025/7/14 點掉 */
//            if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
//                    || rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
//                getFiscBusiness().setFeptxn(null);
//                getFiscBusiness().sendGarbledMessage(fiscHeader.getEj(), rtnCode, fiscHeader);
//            }

            getFiscBusiness().setFeptxn(getFiscBusiness().getOriginalFEPTxn());
            getTxData().setFeptxn(getFiscBusiness().getFeptxn());

            // 2. 	CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
            if (rtnCode == FEPReturnCode.Normal){
                rtnCode = this.checkBusinessRule();
                this.logContext.setRemark("checkBusinessRule : " + rtnCode);
                logMessage(this.logContext);
            }


            // 3. 	SendToIMS送財金Con電文到主機
            sendToCBS();

            if (rtnCode != FEPReturnCode.Normal)
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());

            // 5. 	更新FEPTXN
            updateFEPTXN();

        } catch (Exception ex) {
            this.rtnCode = CommonReturnCode.ProgramException;
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(this.logContext);
        } finally {
            this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            this.getTxData().getLogContext().setMessage(rtnMessage);
            this.getTxData().getLogContext().setProgramName(this.aaName);
            this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.DEBUG, this.logContext);
        }

        return null;
    }

    private void sendToCBS() {
        String mMac;
        String conmacType = String.valueOf(getTxData().getMsgCtl().getMsgctlConmacType());
        CBSAdapter adapter = new CBSAdapter(this.getTxData());
        adapter.setTimeout(0);//不須接收回應
        adapter.setCbsType(CBSType.FISC);
        adapter.setCbsId(StringUtils.leftPad(this.getTxData().getStan(), 7, '0'));
        String apData = "";
        if (StringUtils.isNotBlank(conmacType) && rtnCode == FEPReturnCode.Normal) {
            mMac = "4001";
            String mac = EbcdicConverter.toHex(CCSID.English, mMac.length(), mMac);
            apData = fiscHeader.getFISCMessage();
            apData = apData.substring(0, (apData.length() - 8)) + mac;
        } else {
            mMac = "0302";
            String mac = EbcdicConverter.toHex(CCSID.English, mMac.length(), mMac);
            apData = fiscHeader.getFISCMessage();
            apData = apData.substring(0, (apData.length() - 8)) + mac;
        }
        adapter.setMessageToCBS(apData);
        rtnCode = adapter.sendReceive();
        this.logContext.setRemark("MessageToCBS");
        this.logContext.setMessage("MessageToCBS: " + adapter.getMessageToCBS() + ",CbsId:" + adapter.getCbsId());
        logMessage(this.logContext);
        this.logContext.setRemark("MessageFromCBS");
        this.logContext.setMessage("MessageFromCBS:" + adapter.getMessageFromCBS());
        logMessage(this.logContext);
        getTxData().setTxResponseMessage(adapter.getMessageFromCBS());
        fiscHeader.setFISCMessage(adapter.getMessageFromCBS());
        fiscHeader.parseFISCMsg();
    }

    private FEPReturnCode checkBusinessRule() {
        try {
            // 檢核 MAC 修改, 財金Confirm電文MAC類別有值, 才檢核 MAC
            if (StringUtils.isNotBlank(String.valueOf(getTxData().getMsgCtl().getMsgctlConmacType()))){
                ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
                getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());
                // 2017/11/17 Modify by Ruling for 收到財金確認電文時間寫入FEPTXN
                getFiscBusiness().getFeptxn().setFeptxnConTxTime(
                        FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
                checkMac();
            }

            if (rtnCode != FEPReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnConRc(null);
                rtnCode = FEPReturnCode.ENCCheckMACError;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    private void checkMac() {
        String messageType = "";
        String mac = "";
        switch (fiscTeleType) {

            case "EMVIC":
                messageType = getFiscEMVICCon().getMessageType();
                mac = getFiscEMVICCon().getMAC();
                break;
            case "OPC":
                messageType = getFiscOPCCon().getMessageType();
                mac = getFiscOPCCon().getMAC();
                break;
            case "INBK":
                messageType = getFiscCon().getMessageType();
                mac = getFiscCon().getMAC();
                break;
        }
        //(3) 	檢核 MAC ‘2505’,’2571’,’2572’,’2545’,’2546’
        String type = String.valueOf(getTxData().getMsgCtl().getMsgctlReqmacType());
        if (StringUtils.isNotBlank(type)){
            if (StringUtils.equals(pCode, "3201")) {
                rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData())
                        .checkOpcMac(pCode, messageType, mac);
            } else {
                rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData())
                        .checkFiscMac(messageType, mac);
            }
        }

    }

    private FEPReturnCode updateFEPTXN() throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            getFiscBusiness().getFeptxn().setFeptxnAaComplete((short)0);
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /*+REP*/
                if (NormalRC.FISC_ATM_OK.equals(fiscHeader.getResponseCode())) {  /*+CON*/
                    getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); /*成功*/
                    String balb = "";
                    String bala = "";

                    switch(fiscTeleType) {
                        case "EMVIC":
                            balb = getFiscEMVICCon().getBALB();
                            bala = getFiscEMVICCon().getBALA();
                            break;
                        case "OPC":
                            break;
                        case "INBK":
                            balb = getFiscCon().getBALB();
                            bala = getFiscCon().getBALA();
                            break;
                    }
                    if(StringUtils.isNotBlank(balb)) {
                        getFiscBusiness().getFeptxn().setFeptxnBalb(new BigDecimal(balb));
                    }
                    if(StringUtils.isNotBlank(bala)) {
                        getFiscBusiness().getFeptxn().setFeptxnBala(new BigDecimal(bala));
                    }
                } else {
                    /* 9/24 依SPEC修正Txrust */
                    getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse);  /*Reverse*/
                }
                /* 9/24 依SPEC修正FeptxnPending */
                getFiscBusiness().getFeptxn().setFeptxnPending((short) 2); /*解除 PENDING */
            }

            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm);
            getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
            getFiscBusiness().getFeptxn().setFeptxnConRc(fiscHeader.getResponseCode());

            rtnCode = getFiscBusiness().updateTxData();
            if (rtnCode != FEPReturnCode.Normal) {
                return rtnCode;
            }

        } catch (Exception ex) {
            logContext.setProgramException(ex);
            logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
            sendEMS(logContext);
            return CommonReturnCode.ProgramException;
        }

        return rtnCode;
    }
}
