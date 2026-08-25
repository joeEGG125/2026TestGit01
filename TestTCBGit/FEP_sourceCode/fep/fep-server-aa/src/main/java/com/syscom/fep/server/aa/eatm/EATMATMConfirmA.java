package com.syscom.fep.server.aa.eatm;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.ext.mapper.IntltxnExtMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_Header;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_SvcRs;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_NS1MsgRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.Calendar;

/**
 * @author Jaime
 */
public class EATMATMConfirmA extends INBKAABase {
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    boolean isGoResponse = false;

    private IntltxnExtMapper intltxnExtMapper = SpringBeanFactoryUtil.getBean(IntltxnExtMapper.class);
    private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);

    public EATMATMConfirmA(ATMData txnData) throws Exception {
        super(txnData,"eatm");
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmheader = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
            getATMRequest().setCLIENTTRACEID(atmheader.getCLIENTTRACEID());

            // 1. 記錄文字記錄檔Log (MessageText)
            // Do Nothing

            // 2. 基本交易參數設定(定義於 MSGCTL table, 程式不必撰寫)
            // Do Nothing

            // 3. CheckBusinessRule: 商業邏輯檢核
            this.checkBusinessRule();

            // 4. UpdateTxData: 更新交易記錄(FEPTXN/INTLTXN)
            if (!isGoResponse) {
                this.updateTxData();

                // 5.交易通知 (if need)
                this.sendToMailHunter();

                // 6.交易結束通知主機(By PCODE)
                this.sendToCBS();
            }

            // 7. 組回應電文回給 ATM
            rtnMessage = this.response();



        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            feptxn.setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getATMtxData().getLogContext().setMessage(rtnMessage);
            getATMtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getATMtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logMessage(Level.DEBUG, this.logContext);
        }
        return rtnMessage;
    }

    /**
     * 6.交易結束通知主機(By PCODE)
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        if (feptxn.getFeptxnPcode().equals("2510") || feptxn.getFeptxnPcode().equals("2521")
                || feptxn.getFeptxnPcode().equals("2522") || feptxn.getFeptxnPcode().substring(0, 3).equals("253")
                || feptxn.getFeptxnPcode().substring(0, 3).equals("256")) {
            String AATxTYPE = "";
            String AATxRs ="N";
            String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid1();
            getATMtxData().setFeptxn(feptxn);// AA = MSGCTL_TWCBSTXID的電文
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
            rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE,AATxRs);
        }
    }
    /**
     * 3. CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private void checkBusinessRule() throws Exception {

        // (1)檢核原交易帳號
        getATMBusiness().setFeptxn(getATMBusiness().eatm_checkConData());
        feptxn = getATMBusiness().getFeptxn();
        if (feptxn == null) {
            // FEPReturnCode = "E944" /* 查無原交易 */
            rtnCode = FEPReturnCode.OriginalMessageNotFound;
            // 將ERROR MSG送 EVENT MONITOR SYSTEM
            sendEMS(getLogContext());
            isGoResponse = true;
            return; // GOTO STEP 5: 組回應電文回給 ATM
        }

        // (2)更新 FEPTXN
        rtnCode = getATMBusiness().prepareConFEPTXN();
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GOTO NEXT STEP 4: UpdateTxData
        }

        // (3)檢核 ATM Confirm MAC(if need)
        /* 因Confirm MAC error 需繼續執行其他步驟,故存入不同 RC */
        /* ATM 電文檢核 Confirm MAC */
        String ATM_TITA_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
        if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
            rtnCode2 = FEPReturnCode.ENCCheckMACError;
        } else {
            String ATMREQ = getATMtxData().getTxRequestMessage();
            String ATMMAC = CodeGenUtil.ebcdicToAsciiDefaultEmpty(ATMREQ.substring(742,758)); //轉 ASCII
            rtnCode2 = new ENCHelper(getATMtxData()).checkAtmMacNew("NEATM001",ATMREQ.substring(36,742),ATMMAC);
        }
    }

    /**
     * 4. 更新交易記錄(FEPTXN/INTLTXN)
     */
    private void updateTxData() {
        feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response); /* ATM Confirm Response */
        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode.getValue());
            feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(rtnCode.name(), FEPChannel.FEP, FEPChannel.ATM,
                    getATMtxData().getLogContext()));
        } else if (rtnCode2 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode2.getValue());
            feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(rtnCode2.name(), FEPChannel.FEP, FEPChannel.ATM,
                    getATMtxData().getLogContext()));
        }
        feptxn.setFeptxnTxrust("A"); /*成功*/
        feptxn.setFeptxnAaComplete((short) 1); /*AA close*/

        /* 更新 FEPTXN */
        rtnCode2 = this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
        /* 若更新 FEPTXN 失敗時不做處理, 程式結束 */
    }

    /**
     * 7. 組回應電文回給 ATM
     *
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getSvcRq();

            String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                    FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            ATMENCHelper atmEncHelper = new ATMENCHelper(this.getATMtxData());
            RefString rfs = new RefString();

            SEND_EATM_FSN_HEAD2 rs = new SEND_EATM_FSN_HEAD2();
            SEND_EATM_FSN_HEAD2_Body rsbody = new SEND_EATM_FSN_HEAD2_Body();
            SEND_EATM_FSN_HEAD2_Body_NS1MsgRs msgrs = new SEND_EATM_FSN_HEAD2_Body_NS1MsgRs();
            SEND_EATM_FSN_HEAD2_Body_MsgRs_Header header = new SEND_EATM_FSN_HEAD2_Body_MsgRs_Header();
            SEND_EATM_FSN_HEAD2_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FSN_HEAD2_Body_MsgRs_SvcRs();
            msgrs.setSvcRq(msgbody);
            msgrs.setHeader(header);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
            header.setCHANNEL(atmReqheader.getCHANNEL());
            header.setMSGID(atmReqheader.getMSGID());
            header.setCLIENTDT(atmReqheader.getCLIENTDT());
            header.setSYSTEMID("FEP");
            if(feptxn != null && StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())){
                header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
            }
            if(feptxn == null){ //原交易之 FEPTXN  is Nothing
                header.setSTATUSCODE("EF0305");
                header.setSTATUSDESC("OriginalMessageNotFound");
            }
            if(StringUtils.isBlank(header.getSTATUSCODE())){
                header.setSTATUSCODE("4001");
            }

            // 組Header(OUTPUT-1)
            msgbody.setWSID(atmReqbody.getWSID());
            msgbody.setRECFMT("1");
            msgbody.setMSGCAT("F");
            msgbody.setMSGTYP("PC"); // - response
            msgbody.setTRANDATE(atmReqbody.getTRANDATE()); // 西元後兩碼+系統月日共六碼
            msgbody.setTRANTIME(atmReqbody.getTRANTIME()); // 系統時間
            msgbody.setTRANSEQ(atmReqbody.getTRANSEQ());
            msgbody.setTDRSEG(atmReqbody.getTDRSEG()); // 回覆FSN或FSE
            // PRCRDACT = 0 或4都是未留置卡片, 2 是吃卡, 只有磁條密碼變更交易(FC1、P1)主機才有可能依據狀況要求吃卡
            msgbody.setPRCRDACT("0");

            if (feptxn == null) {
                msgbody.setRECFMT("0");
            }

            /* CALL ENC 取得MAC 資料 */
            rfs.set("");
            String res = msgbody.makeMessage();
            rtnCode = atmEncHelper.makeAtmMac(res, rfs);
            if (rtnCode != FEPReturnCode.Normal) {
                msgbody.setMACCODE(""); /* 訊息押碼 */
            } else {
                msgbody.setMACCODE(rfs.get()); /* 訊息押碼 */
            }
            msgbody.setOUTDATA(msgbody.makeMessage());
            rtnMessage = XmlUtil.toXML(rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * 5.交易通知 (if need)
     *
     * @return
     * @throws Exception
     */
    private void sendToMailHunter() {
        if ("4001".equals(feptxn.getFeptxnRepRc()) && "4001".equals(feptxn.getFeptxnConRc())) {
            try {
                switch (feptxn.getFeptxnNoticeType()) {
                    case "P": /* 送推播 */
                        getATMBusiness().preparePush(this.feptxn);
                        break;
                    case "M": /* 簡訊 */
                        getATMBusiness().prepareSms(this.feptxn);
                        break;
                    case "E": /* Email */
                        getATMBusiness().prepareMail(this.feptxn);
                        break;
                }
            } catch (Exception ex) {
                this.logContext.setProgramException(ex);
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
                sendEMS(this.logContext);
            }
        }
    }

    /**
     * 7.更新交易記錄(FEPTXN)
     *
     * @return
     */
    private FEPReturnCode updateFeptxn() {
        FEPReturnCode fepReturnCode = FEPReturnCode.Normal;
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            fepReturnCode = FEPReturnCode.FEPTXNUpdateError;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
        }
        return fepReturnCode;
    }

}
