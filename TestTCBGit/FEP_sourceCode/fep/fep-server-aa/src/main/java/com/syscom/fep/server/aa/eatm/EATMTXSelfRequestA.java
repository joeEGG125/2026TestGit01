package com.syscom.fep.server.aa.eatm;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
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
public class EATMTXSelfRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;

    public EATMTXSelfRequestA(ATMData txnData) throws Exception {
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

            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS/ASC: 送主機檢核帳戶資料
                this.sendToCBS();
            }

            FISC fiscBusiness = getFiscBusiness();
            if (rtnCode == FEPReturnCode.Normal) {
                // 5. 組送往 FISC 之 Request 電文並等待財金之 Response
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
            }

            boolean repRcEq4001 = true;
            if (rtnCode == FEPReturnCode.Normal) {
                // 6. CheckResponseFromFISC:檢核回應電文是否正確
                rtnCode = fiscBusiness.checkResponseMessage();
                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 7. ProcessAPTOT:更新跨行代收付
                if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
                    rtnCode = fiscBusiness.processAptot(false);
                }
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 8. SendToCBS/ASC: 送主機處理帳務
                this.sendToCBS2();
                if (rtnCode != FEPReturnCode.Normal) {
                    if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
                        /* 沖回跨行代收付(APTOT) */
                        rtnCode2 = fiscBusiness.processAptot(true);
                    }
                    // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
                }
            }


            if (!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
                // 9. label_END_OF_FUNC判斷是否需組 CON 電文回財金
                this.labelEndOfFunc();

                // 10. 組ATM回應電文 & 回 ATMMsgHandler
                rtnMessage = this.response();
            }

            // 11. 更新交易記錄(FEPTXN)
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

    private void sendToCBS3() throws Exception {
        String AATxTYPE = "";
        String AATxRs = "N";
        String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid1();
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
        rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE, AATxRs);
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

        /* 檢核財金及參加單位之系統狀態 */
        rtnCode = getFiscBusiness().checkINBKStatus(feptxn.getFeptxnPcode(), true);
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
        }

        /* 檢核中文附言欄不得超過14個中文字 */
        String feptxnChrem = feptxn.getFeptxnChrem();
        if (StringUtils.isNotBlank(feptxnChrem)) {
            if (feptxnChrem.length() > 14) {
                rtnCode = FEPReturnCode.OtherCheckError; /* E711:其他類檢核錯誤 */
                return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
            }

            RefString output = new RefString("");
            getFiscBusiness().convertFiscEncode(getFeptxn().getFeptxnChrem(), output); // (UNICODE轉CNS11643)
            if (output.get().length() > 80) { // 長度>80
                rtnCode = FEPReturnCode.OtherCheckError; /* E711:其他類檢核錯誤 */
                getLogContext().setRemark("中文摘要轉成CNS11643超過80位" + output.get());
                logMessage(Level.INFO, getLogContext());
                return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
            }
        }
    }

    /**
     * 4. SendToCBS/ASC: 送主機檢核帳戶資料
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        feptxn.setFeptxnStan(getATMBusiness().getStan());
        // 轉入方為本行時,先送CBS查詢帳號
        feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
        String AATxTYPE = "0"; // 上CBS查詢、檢核
        String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
        rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
        if (rtnCode != FEPReturnCode.Normal) {
            if (feptxn.getFeptxnCbsTimeout() == 1) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
            }
        }
    }


    /**
     * 8. SendToCBS/ASC: 送主機處理帳務
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlCbsFlag())) {
            /* 進主機入扣帳/手續費 */
            String AATxTYPE = "1"; // 上CBS入扣帳
            String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
            rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
            if (feptxn.getFeptxnCbsTimeout() == 1) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
            }
        }
    }

    /**
     * 10. 組ATM回應電文 & 回 ATMMsgHandler
     *
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmheader = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmbody = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getSvcRq();
            String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                    FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            ATMENCHelper atmEncHelper = new ATMENCHelper(this.getATMtxData());
            RefString rfs = new RefString();
            if (rtnCode != CommonReturnCode.Normal || !NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                SEND_EATM_FAA_CC1APC rs = new SEND_EATM_FAA_CC1APC();
                SEND_EATM_FAA_CC1APC_Body rsbody = new SEND_EATM_FAA_CC1APC_Body();
                SEND_EATM_FAA_CC1APC_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1APC_Body_NS1MsgRs();
                SEND_EATM_FAA_CC1APC_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1APC_Body_MsgRs_Header();
                SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs();
                msgrs.setHeader(header);
                msgrs.setSvcRq(msgbody);
                rsbody.setRs(msgrs);
                rs.setBody(rsbody);
                //組 MQHeader
                header.setCLIENTTRACEID(atmheader.getCLIENTTRACEID());
                header.setCHANNEL(feptxn.getFeptxnChannel());
                header.setMSGID(atmheader.getMSGID());
                header.setCLIENTDT(atmheader.getCLIENTDT());
                header.setSYSTEMID("FEP");

                if(StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())){
                    header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                } else {
                    header.setSTATUSCODE("4001");
                }

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
                if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc())
                        && !feptxn.getFeptxnCbsRc().equals("000")
                        && !feptxn.getFeptxnCbsRc().equals("4001")
                        && !feptxn.getFeptxnCbsRc().equals("0000")) {
                    msgbody.setPAGENO(TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
                            getTxData().getLogContext()));
                    msgbody.setPRCCODE(StringUtils.rightPad(feptxn.getFeptxnCbsRc(),4," "));
                } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !feptxn.getFeptxnRepRc().equals("4001")) {
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
                msgbody.setPTIME(new SimpleDateFormat("HH:mm:ss").format(new SimpleDateFormat("HHmmss").parse(feptxn.getFeptxnTxTime())));
                // CBS下送的交易種類
                msgbody.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                // 端末機代號欄位=>繳稅交易放：轉出行-轉出分行財稅代號
                String feptxnTroutBkno7 = feptxn.getFeptxnTroutBkno7();
                msgbody.setPTID(StringUtils.substring(feptxnTroutBkno7, 0, 3) + "-"
                        + StringUtils.substring(feptxnTroutBkno7, 3, 7));
                // 格式 :$$$,$$$,$$9 ex :$10,000
                msgbody.setPTXAMT(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmt(), "$#,##0"), 11, " "));
                // 格式:$999 ex :$0
                msgbody.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0.0"), 4, " "));
                // 帳戶餘額欄位=>繳稅交易放：繳稅項目
                String feptxnTxCode = feptxn.getFeptxnTxCode();
                String feptxnPaytype = feptxn.getFeptxnPaytype();
                if ("T5".equals(feptxnTxCode)) { // 15類自繳稅
                    // 資料格式=> "TYPE:"+繳費類別
                    msgbody.setPBAL("TYPE:" + feptxn.getFeptxnPaytype());
                } else if ("T6".equals(feptxn.getFeptxnTxCode())) { // 非15類，核定稅
                    // 資料格式=> "TYPE:"+繳費類別+"-"+繳款期限
                    String year = CalendarUtil.adStringToROCString(feptxn.getFeptxnDueDate());
                    msgbody.setPBAL("TYPE:" + feptxn.getFeptxnPaytype() + "-" + year.substring(year.length() - 6, year.length()));
                }
                // CBS下送的轉出帳號(明細表顯示內容)
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()))) {
                    msgbody.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                } else {
                    msgbody.setPACCNO(feptxn.getFeptxnTroutActno());
                }

                // 轉入/存入欄位=>繳稅交易放：繳稅資料
                if ("T5".equals(feptxnTxCode)) { // 15類自繳稅
                    // 稽徵機關+ID
                    msgbody.setPTRINACCT(feptxn.getFeptxnTaxUnit() + feptxn.getFeptxnIdno());
                } else if ("T6".equals(feptxnTxCode)) { // 非15類，核定稅
                    // 銷帳編號
                    msgbody.setPTRINACCT(feptxn.getFeptxnReconSeqno());
                }
                msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
                msgbody.setPSTAN(feptxn.getFeptxnStan());
                // 轉出行
                msgbody.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
                // 轉入行=>繳稅交易放 "TAX"
                msgbody.setPITXBKNO("TAX");
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
                msgrs.setHeader(header);
                msgrs.setSvcRq(msgbody);
                rsbody.setRs(msgrs);
                rs.setBody(rsbody);
                //組 MQHeader
                header.setCLIENTTRACEID(atmheader.getCLIENTTRACEID());
                header.setCHANNEL(feptxn.getFeptxnChannel());
                header.setMSGID(atmheader.getMSGID());
                header.setCLIENTDT(atmheader.getCLIENTDT());
                header.setSYSTEMID("FEP");

                if(StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())){
                    header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                } else {
                    header.setSTATUSCODE("4001");
                }

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

                // 組D0(OUTPUT-2)畫面顯示(Display message)
                // 組 D0(022)
                msgbody.setDATATYPE("D0");
                msgbody.setDATALEN("022");
                msgbody.setACKNOW("0");
                msgbody.setPAGENO("   ");
                msgbody.setPRCCODE("4001");

                // 格式 :正值放$,負值放-,$99,999,999,999.00(共18位)
                // ex:$99,355,329;-33,123.00
                String ACTBALANCE_STR = this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue());
                if (StringUtils.isNotBlank(ACTBALANCE_STR)) {
                    BigDecimal BALANCE = feptxn.getFeptxnBalb();
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
                msgbody.setPDATE(new SimpleDateFormat(FormatUtil.FORMAT_DATE_YYY_MM_DD_SLASH).format(new SimpleDateFormat("yyyMMdd").parse(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()))));
                msgbody.setPTIME(new SimpleDateFormat(FormatUtil.FORMAT_TIME_HH_MM_SS).format(new SimpleDateFormat("HHmmss").parse(feptxn.getFeptxnTxTime())));
                // CBS下送的交易種類
                msgbody.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                // 端末機代號欄位=>繳稅交易放：轉出行-轉出分行財稅代號
                String feptxnTroutBkno7 = feptxn.getFeptxnTroutBkno7();
                msgbody.setPTID(StringUtils.substring(feptxnTroutBkno7, 0, 3) + "-"
                        + StringUtils.substring(feptxnTroutBkno7, 3, 7));
                // 格式 :$$$,$$$,$$9 ex :$10,000
                msgbody.setPTXAMT(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmt(), "$#,##0"), 11, " "));
                // 格式:$999 ex :$0
                msgbody.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0.0"), 4, " "));
                // 帳戶餘額欄位=>繳稅交易放：繳稅項目
                String feptxnTxCode = feptxn.getFeptxnTxCode();
                String feptxnPaytype = feptxn.getFeptxnPaytype();
                if ("T5".equals(feptxnTxCode)) { // 15類自繳稅
                    // 資料格式=> "TYPE:"+繳費類別
                    msgbody.setPBAL("TYPE:" + feptxnPaytype);
                } else if ("T6".equals(feptxnTxCode)) { // 非15類，核定稅
                    // 資料格式=> "TYPE:"+繳費類別+"-"+繳款期限
                    // 轉為民國年取後面6位
                    String year = CalendarUtil.adStringToROCString(feptxn.getFeptxnDueDate());
                    msgbody.setPBAL("TYPE:" + feptxn.getFeptxnPaytype() + "-" + year.substring(year.length() - 6, year.length()));
                }
                // CBS下送的轉出帳號(明細表顯示內容)
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()))) {
                    msgbody.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                } else {
                    msgbody.setPACCNO(feptxn.getFeptxnTroutActno());
                }
                // 轉入/存入欄位=>繳稅交易放：繳稅資料
                if ("T5".equals(feptxnTxCode)) { // 15類自繳稅
                    // 稽徵機關+ID
                    msgbody.setPTRINACCT(feptxn.getFeptxnTaxUnit() + feptxn.getFeptxnIdno());
                } else if ("T6".equals(feptxnTxCode)) { // 非15類，核定稅
                    // 銷帳編號
                    msgbody.setPTRINACCT(feptxn.getFeptxnReconSeqno());
                }
                msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
                msgbody.setPSTAN(feptxn.getFeptxnStan());
                // 轉出行
                msgbody.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
                // 轉入行=>繳稅交易放 "TAX"
                msgbody.setPITXBKNO("TAX");
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
     * 9. label_END_OF_FUNC判斷是否需組 CON 電文回財金
     */
    private void labelEndOfFunc() {
        String feptxnRepRc = feptxn.getFeptxnRepRc();
        if ((rtnCode == FEPReturnCode.Normal) && "4001".equals(feptxnRepRc)) {
            if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlAtm2way())) {
                feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
                feptxn.setFeptxnReplyCode("    "); // 4個SPACES
                feptxn.setFeptxnTxrust("A"); /* 成功 */
                /* 轉帳交易直接送 Confirm 給財金 */
                feptxn.setFeptxnConRc("4001"); /* +CON */
                rtnCode2 = getFiscBusiness().sendConfirmToFISC();
            } else {
                feptxn.setFeptxnReplyCode("    "); // 4個SPACES
                feptxn.setFeptxnTxrust("B"); /* PENDING */
            }
            feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
            this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
        } else { /* 交易失敗FEPReturnCode <> Normal */
            if (StringUtils.isNotBlank(feptxnRepRc)) {
                feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
                if ("4001".equals(feptxnRepRc)) { /* +REP */
                    if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                        feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                    } else {
                        feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode), FEPChannel.FEP,
                                FEPChannel.FISC, getATMtxData().getLogContext()));
                    }
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                            FEPChannel.ATM, getATMtxData().getLogContext()));
                    feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
                    rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                } else { /* -REP */
                    feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC,
                            FEPChannel.ATM, getATMtxData().getLogContext()));
                }
            } else {
                /* FEPReturnCode <> Normal */
                /* 2020/3/6 修改，主機有回應錯誤時，修改交易結果 */
                if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc())) {
                    feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                } else {
                    feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
                }

                if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                            FEPChannel.ATM, getATMtxData().getLogContext()));
                }
            }

            feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
            this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
        }
    }

    /**
     * 11. 更新交易記錄(FEPTXN)
     */
    private void updateTxData() {
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
