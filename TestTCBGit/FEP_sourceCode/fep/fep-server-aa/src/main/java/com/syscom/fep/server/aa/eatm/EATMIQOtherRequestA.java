package com.syscom.fep.server.aa.eatm;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_Header;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_NS1MsgRs;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_MsgRs_Header;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_MsgRs_SvcRs;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_NS1MsgRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Jaime
 */
public class EATMIQOtherRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;

    private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;

    public EATMIQOtherRequestA(ATMData txnData) throws Exception {
        super(txnData, "eatm");
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            // 1. Prepare : 交易記錄初始資料
            rtnCode = getATMBusiness().prepareFEPTXN();

            if (rtnCode == FEPReturnCode.Normal) {
                // 2. AddTxData: 新增交易記錄(FEPTXN)
                this.addTxData();
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                this.checkBusinessRule();
            }

            FISC fiscBusiness = getFiscBusiness();
            if (rtnCode == FEPReturnCode.Normal) {
                // 4. 組送往 FISC 之 Request 電文並等待財金之 Response
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
            }

            boolean repRcEq4001 = true;
            if (rtnCode == FEPReturnCode.Normal) {
                // 5. CheckResponseFromFISC:檢核回應電文是否正確
                rtnCode = fiscBusiness.checkResponseMessage();
                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 6. label_END_OF_FUNC判斷是否需組 CON 電文回財金
                this.labelEndOfFunc();
            }

            // 7. 組ATM回應電文 & 回 ATMMsgHandler
            rtnMessage = this.response();

            // 8. 更新交易記錄(FEPTXN)
            this.updateTxData();

        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
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
     * 2. AddTxData: 新增交易記錄( FEPTxn)
     *
     * @return
     * @throws Exception
     */
    private void addTxData() throws Exception {
        try {
            // 新增交易記錄(FEPTxn) Returning FEPReturnCode
            /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
            if (insertCount <= 0) { // 新增失敗
                rtnCode = FEPReturnCode.FEPTXNInsertError;
            }
        } catch (Exception ex) { // 新增失敗
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".addTxData");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNInsertError;
        }
    }

    /**
     * 3. CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private void checkBusinessRule() throws Exception {

        /* 檢核 ATM 電文 */
        rtnCode = checkRequestFromATM(getATMtxData());
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
        }

    }

    /**
     * 7. label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
     *
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            //RCV_EATM_GeneralTrans_RQ atmReq = getATMBusiness().getEatmReq();
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmheader = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmbody = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getSvcRq();
            String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                    FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            ATMENCHelper atmEncHelper = new ATMENCHelper(this.getATMtxData());
            RefString rfs = new RefString();
            String totaToact;
            if (rtnCode != FEPReturnCode.Normal || NormalRC.FISC_OK.equals(feptxn.getFeptxnRepRc())) {
                SEND_EATM_FAA_CC1APC rs = new SEND_EATM_FAA_CC1APC();
                SEND_EATM_FAA_CC1APC_Body rsbody = new SEND_EATM_FAA_CC1APC_Body();
                SEND_EATM_FAA_CC1APC_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1APC_Body_NS1MsgRs();
                SEND_EATM_FAA_CC1APC_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1APC_Body_MsgRs_Header();
                SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs();
                msgrs.setSvcRq(msgbody);
                msgrs.setHeader(header);
                rsbody.setRs(msgrs);
                rs.setBody(rsbody);

                header.setCLIENTTRACEID(atmheader.getCLIENTTRACEID());
                header.setCHANNEL(feptxn.getFeptxnChannel());
                header.setMSGID(atmheader.getMSGID());
                header.setCLIENTDT(atmheader.getCLIENTDT());
                header.setSYSTEMID("FEP");
                header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                // 組Header(OUTPUT-1)
                msgbody.setWSID(atmbody.getWSID());
                msgbody.setRECFMT("1");
                msgbody.setMSGCAT("F");
                msgbody.setMSGTYP("PC"); // - response
                header.setSEVERITY("ERROR");
                msgbody.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                msgbody.setTRANTIME(systemTime.substring(8, 14)); // 系統時間
                msgbody.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                msgbody.setTDRSEG(atmbody.getTDRSEG()); // 回覆FAA
                msgbody.setCARDACT("0"); // 晶片卡不留置:固定放”0”

                // 組D0(OUTPUT-2)畫面顯示(Display message)
                // 組 D0(004)
                msgbody.setDATATYPE("D0");
                msgbody.setDATALEN("004");
                msgbody.setACKNOW("0");

                if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) {
                    msgbody.setPAGENO(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC, FEPChannel.ATM,
                            getTxData().getLogContext()));
                    msgbody.setPRCCODE(StringUtils.rightPad(feptxn.getFeptxnRepRc(),4," "));
                } else {
                    msgbody.setPRCCODE(StringUtils.rightPad(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                            getTxData().getLogContext()),4," "));
                    msgbody.setPAGENO(TxHelper.getRCFromErrorCode(msgbody.getPRCCODE(), FEPChannel.CBS, FEPChannel.ATM,
                            getTxData().getLogContext()));
                }
                //else 226
                if (StringUtils.isBlank(msgbody.getPAGENO())) {
                    msgbody.setPAGENO("226");
                    msgbody.setPRCCODE("2999");
                }

                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                msgbody.setPTYPE("S0");
                msgbody.setPLEN("191");
                msgbody.setPBMPNO("010000"); // FPC
                // 西元年轉民國年
                msgbody.setPDATE(new SimpleDateFormat("yyy/MM/dd").format(new SimpleDateFormat("yyyMMdd").parse(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()))));
                msgbody.setPTIME(new SimpleDateFormat(FormatUtil.FORMAT_TIME_HH_MM_SS).format(new SimpleDateFormat("HHmmss").parse(feptxn.getFeptxnTxTime())));
                // CBS下送的交易種類
                msgbody.setPTXTYPE(feptxn.getFeptxnTxCode());
                msgbody.setPTID(feptxn.getFeptxnAtmno());
                // 格式 :$$$,$$$,$$9 ex :$10,000
                msgbody.setPTXAMT(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmt(), "$#,##0"), 11, " "));
                // 格式:$999.0 ex :$0.0
                msgbody.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0.0"), 4, " "));
                // 格式：$$$,$$$,$$$,$$9.99 ex：$97,947.00
                if(StringUtils.isNotBlank(feptxn.getFeptxnBala().toString())){
                    msgbody.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnBala(), "$#,##0.00"), 18, " "));
                } else {
                    msgbody.setPBAL(StringUtils.leftPad("", 18, " "));
                }
                //他行帳號:16位,第10~12位隱碼 ex：123456789***3456 (明細表顯示內容)
                // CBS下送的轉出帳號(明細表顯示內容)
                msgbody.setPACCNO(feptxn.getFeptxnTroutActno());
                msgbody.setPOTXBKNO(feptxn.getFeptxnTroutActno());
                msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
                msgbody.setPSTAN(feptxn.getFeptxnStan());

                // CBS下送ATM的促銷應用訊息
                msgbody.setPARPC(feptxn.getFeptxnLuckyno());
                /* CALL ENC 取得MAC 資料 */
                String res = msgbody.makeMessage();
                rfs.set("");
                rtnCode3 = atmEncHelper.makeAtmMac("NEATM001", res, rfs);
                if (rtnCode3 != FEPReturnCode.Normal) {
                    msgbody.setPRCCODE(StringUtils.rightPad(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode3.getValue()), FEPChannel.FEP, getTxData().getTxChannel(), getLogContext()),4," "));
                    msgbody.setMACCODE("");
                } else {
                    msgbody.setMACCODE(rfs.get()); /* 訊息押碼 */
                }
                msgbody.setOUTDATA(msgbody.makeMessage());
                rtnMessage = XmlUtil.toXML(rs);
            } else {
                SEND_EATM_FAA_CC1APN rs = new SEND_EATM_FAA_CC1APN();
                SEND_EATM_FAA_CC1APN_Body rsbody = new SEND_EATM_FAA_CC1APN_Body();
                SEND_EATM_FAA_CC1APN_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1APN_Body_NS1MsgRs();
                SEND_EATM_FAA_CC1APN_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1APN_Body_MsgRs_Header();
                SEND_EATM_FAA_CC1APN_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FAA_CC1APN_Body_MsgRs_SvcRs();
                msgrs.setSvcRq(msgbody);
                msgrs.setHeader(header);
                rsbody.setRs(msgrs);
                rs.setBody(rsbody);


                header.setCLIENTTRACEID(atmheader.getCLIENTTRACEID());
                header.setCHANNEL(feptxn.getFeptxnChannel());
                header.setMSGID(atmheader.getMSGID());
                header.setCLIENTDT(atmheader.getCLIENTDT());
                header.setSYSTEMID("FEP");
                header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                // 組Header(OUTPUT-1)
                msgbody.setWSID(atmbody.getWSID());
                msgbody.setRECFMT("1");
                msgbody.setMSGCAT("F");
                msgbody.setMSGTYP("PN"); // + response
                header.setSEVERITY("INFO");
                msgbody.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                msgbody.setTRANTIME(systemTime.substring(8, 14)); // 系統時間
                msgbody.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                msgbody.setTDRSEG(atmbody.getTDRSEG()); // 回覆FAA
                msgbody.setCARDACT("0"); // 晶片卡不留置:固定放”0”

                msgbody.setDATATYPE("D0");
                msgbody.setDATALEN("022");
                msgbody.setACKNOW("0");
                msgbody.setPAGENO("030");

                if (StringUtils.isNotBlank(feptxn.getFeptxnBalb().toString())) {
                    BigDecimal BALANCE = feptxn.getFeptxnBala();
                    // 格式 :正值放$,負值放-,$99,999,999,999.00(共18位)
                    // ex:$99,355,329;-33,123.00
                    DecimalFormat df;
                    if (BALANCE.compareTo(BigDecimal.ZERO) >= 0) {
                        df = new DecimalFormat("$#,##0.00");
                    } else {
                        df = new DecimalFormat("-#,##0.00");
                    }
                    msgbody.setBALANCE(StringUtils.leftPad(df.format(BALANCE), 18, " "));
                } else {
                    msgbody.setBALANCE(StringUtils.leftPad("", 18, " "));
                }

                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                msgbody.setPTYPE("S0");
                msgbody.setPLEN("191");
                msgbody.setPBMPNO("000010"); // FPN
                // 西元年轉民國年
                msgbody.setPDATE(new SimpleDateFormat("yyy/MM/dd").format(new SimpleDateFormat("yyyMMdd").parse(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()))));
                msgbody.setPTIME(new SimpleDateFormat(FormatUtil.FORMAT_TIME_HH_MM_SS).format(new SimpleDateFormat("HHmmss").parse(feptxn.getFeptxnTxTime())));
                // CBS下送的交易種類
                msgbody.setPTXTYPE(feptxn.getFeptxnTxCode());
                msgbody.setPTID(feptxn.getFeptxnAtmno());
                // 格式 :$$$,$$$,$$9 ex :$10,000
                msgbody.setPTXAMT(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmt(), "$#,##0"), 11, " "));
                // 格式:$999.0 ex :$0.0
                msgbody.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"), 4, " "));
                // 格式：$$$,$$$,$$$,$$9.99 ex：$97,947.00
                if(StringUtils.isNotBlank(feptxn.getFeptxnBala().toString())){
                    msgbody.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnBala(), "$#,##0.00"), 18, " "));
                } else {
                    msgbody.setPBAL(StringUtils.leftPad("", 18, " "));
                }
                //他行帳號:16位,第10~12位隱碼 ex：123456789***3456 (明細表顯示內容)
                // CBS下送的轉出帳號(明細表顯示內容)
                msgbody.setPACCNO(feptxn.getFeptxnTroutActno());
                msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
                msgbody.setPSTAN(feptxn.getFeptxnStan());

                // CBS下送ATM的促銷應用訊息
                msgbody.setPARPC(feptxn.getFeptxnLuckyno());
                /* CALL ENC 取得MAC 資料 */
                String res = msgbody.makeMessage();
                rfs.set("");
                rtnCode3 = atmEncHelper.makeAtmMac("NEATM001", res, rfs);
                if (rtnCode3 != FEPReturnCode.Normal) {
                    msgbody.setPRCCODE(StringUtils.rightPad(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode3.getValue()), FEPChannel.FEP, getTxData().getTxChannel(), getLogContext()),4," "));
                    msgbody.setMACCODE("");
                } else {
                    msgbody.setMACCODE(rfs.get()); /* 訊息押碼 */
                }
                msgbody.setOUTDATA(msgbody.makeMessage());
                rtnMessage = XmlUtil.toXML(rs);
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * 6. label_END_OF_FUNC判斷是否需組 CON 電文回財金
     */
    private void labelEndOfFunc() {
        String feptxnRepRc = feptxn.getFeptxnRepRc();
        if ((rtnCode == FEPReturnCode.Normal) && "4001".equals(feptxnRepRc)) {
            feptxn.setFeptxnReplyCode("    "); // 4個SPACES  回覆 ATM正常
            feptxn.setFeptxnTxrust("B"); /* PENDING */
            feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
        } else if (StringUtils.isNotBlank(feptxnRepRc)) { /*交易失敗FEPReturnCode<>Normal or FEPTXN_REP_RC <> 4001*/
            feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
            feptxn.setFeptxnTxrust("R"); /* Reject-normal */
            if (NormalRC.FISC_ATM_OK.equals(feptxnRepRc)) { /* +REP */
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                        FEPChannel.ATM, getATMtxData().getLogContext()));
            } else { /* -REP */
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FISC,
                        FEPChannel.ATM, getATMtxData().getLogContext()));
            }
        } else { /* 交易失敗FEPReturnCode <> Normal */
            feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
            if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                        FEPChannel.ATM, getATMtxData().getLogContext()));
            }
        }
    }

    /**
     * 8. 更新交易記錄(FEPTXN)
     */
    private void updateTxData() {
        feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode.getValue());
        } else if (rtnCode2 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode2.getValue());
        } else {
            feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
        }
        feptxn.setFeptxnAaComplete((short) 1); /* AA Close */
        this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
    }

    /**
     * 更新feptxn
     *
     * @return
     */
    private void updateFeptxn() {
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
        }
    }
}
