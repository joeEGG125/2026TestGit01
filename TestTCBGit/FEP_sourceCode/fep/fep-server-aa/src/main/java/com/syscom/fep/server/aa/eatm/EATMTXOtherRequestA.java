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
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
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
public class EATMTXOtherRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;

    public EATMTXOtherRequestA(ATMData txnData) throws Exception {
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
                // 6. ProcessAPTOT:更新跨行代收付
                if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
                    rtnCode = fiscBusiness.processAptot(false);
                }
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 7. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
                this.sendToCBS2();
            }


            if (!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {

                // 8. label_END_OF_FUNC判斷是否需組 CON 電文回財金
                this.labelEndOfFunc();

                // 9. 組ATM回應電文 & 回 ATMMsgHandler
                rtnMessage = this.response();
            }

            // 10. 更新交易記錄(FEPTXN)
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
            FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
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
        rtnCode = checkRequestFromATM(getATMtxData());
    }

    /**
     * 4. SendToCBS/ASC(if need): 本行轉入-進帳務主機查詢帳號
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        if (StringUtils.equals(feptxn.getFeptxnTrinBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
            // 轉入方為本行時,先送CBS查詢帳號
            feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
            String AATxTYPE = "0"; // 上CBS查詢、檢核
            String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
            rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
        }
    }

    /**
     * 7. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
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
            if (rtnCode != FEPReturnCode.Normal) {
                if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
                    /* 沖回跨行代收付(APTOT) */
                    rtnCode2 = getFiscBusiness().processAptot(true);
                    if (feptxn.getFeptxnCbsTimeout() == 1) {
                        feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
                    }
                }
                // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
            }
        }
    }

    /**
     * 9. label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
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
                if(StringUtils.isBlank(feptxn.getFeptxnReplyCode())){
                    header.setSTATUSCODE("4001");
                } else {
                    header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
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
                msgbody.setPTIME(new SimpleDateFormat(FormatUtil.FORMAT_TIME_HH_MM_SS).format(new SimpleDateFormat("HHmmss").parse(feptxn.getFeptxnTxTime())));
                // CBS下送的交易種類
                msgbody.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                String bkno7 = "";
                if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7()) && feptxn.getFeptxnTroutBkno7().length() > 7) {
                    bkno7 = feptxn.getFeptxnTroutBkno7().substring(0, 3) + "-" + feptxn.getFeptxnTroutBkno7().substring(3, 7);
                }
                //端末機代號欄位=>繳稅交易放：轉出行-轉出分行財稅代號
                msgbody.setPTID(bkno7);
                // 格式 :$$$,$$$,$$9 ex :$10,000
                msgbody.setPTXAMT(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmt(), "$#,##0"), 11, " "));
                // 格式:$999 ex :$0
                msgbody.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"), 4, " "));
                //帳戶餘額欄位=>繳稅交易放：繳稅項目
                if ("T7".equals(feptxn.getFeptxnTxCode())) {//15類自繳稅
                    //資料格式=> "TYPE:"+繳費類別
                    msgbody.setPBAL("TYPE:" + feptxn.getFeptxnPaytype());
                } else if ("T8".equals(feptxn.getFeptxnTxCode())) {//非15類，核定稅
                    //資料格式=> "TYPE:"+繳費類別+"-"+繳款期限
                    String year = CalendarUtil.adStringToROCString(feptxn.getFeptxnDueDate());
                    msgbody.setPBAL("TYPE:" + feptxn.getFeptxnPaytype() + "-" + year.substring(year.length() - 6, year.length()));
                }
                // CBS下送的轉出帳號(明細表顯示內容)
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()))) {
                    msgbody.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                } else {
                    msgbody.setPACCNO(feptxn.getFeptxnTroutActno());
                }

                //轉入/存入欄位=>繳稅交易放：繳稅資料
                if ("T7".equals(feptxn.getFeptxnTxCode())) { // 15類自繳稅
                    // 稽徵機關+ID
                    msgbody.setPTRINACCT(feptxn.getFeptxnTaxUnit() + feptxn.getFeptxnIdno());
                } else if ("T8".equals(feptxn.getFeptxnTxCode())) { // 非15類，核定稅
                    // 銷帳編號
                    msgbody.setPTRINACCT(feptxn.getFeptxnReconSeqno());
                }
                msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
                msgbody.setPSTAN(feptxn.getFeptxnStan());
                // 轉出行
                msgbody.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
                //轉入行=>繳稅交易放 "TAX"
                msgbody.setPITXBKNO("TAX");
                // 取得原存行的促銷應用訊息
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
                if(StringUtils.isBlank(feptxn.getFeptxnReplyCode())){
                    header.setSTATUSCODE("4001");
                } else {
                    header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
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

                msgbody.setDATATYPE("D0");
                msgbody.setDATALEN("022");
                msgbody.setACKNOW("0");
                msgbody.setPAGENO("   ");
                msgbody.setPRCCODE("4001");

                // 格式 :正值放$,負值放-,$99,999,999,999.00(共18位)
                // ex:$99,355,329;-33,123.00
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
                msgbody.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                String bkno7 = "";
                if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7()) && feptxn.getFeptxnTroutBkno7().length() > 7) {
                    bkno7 = feptxn.getFeptxnTroutBkno7().substring(0, 3) + "-" + feptxn.getFeptxnTroutBkno7().substring(3, 7);
                }
                //端末機代號欄位=>繳稅交易放：轉出行-轉出分行財稅代號
                msgbody.setPTID(bkno7);
                // 格式 :$$$,$$$,$$9 ex :$10,000
                msgbody.setPTXAMT(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmt(), "$#,##0"), 11, " "));
                // 格式:$999 ex :$0
                msgbody.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"), 4, " "));
                //帳戶餘額欄位=>繳稅交易放：繳稅項目
                if ("T7".equals(feptxn.getFeptxnTxCode())) {//15類自繳稅
                    //資料格式=> "TYPE:"+繳費類別
                    msgbody.setPBAL("TYPE:" + feptxn.getFeptxnPaytype());
                } else if ("T8".equals(feptxn.getFeptxnTxCode())) {//非15類，核定稅
                    //資料格式=> "TYPE:"+繳費類別+"-"+繳款期限  FEPTXN_DUE_DATE轉為民國年取後面6位
                    String year = CalendarUtil.adStringToROCString(feptxn.getFeptxnDueDate());
                    msgbody.setPBAL("TYPE:" + feptxn.getFeptxnPaytype() + "-" + year.substring(year.length() - 6, year.length()));
                }
                // CBS下送的轉出帳號(明細表顯示內容)
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()))) {
                    msgbody.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                } else {
                    msgbody.setPACCNO(feptxn.getFeptxnTroutActno());
                }
                //轉入/存入欄位=>繳稅交易放：繳稅資料
                if ("T7".equals(feptxn.getFeptxnTxCode())) { // 15類自繳稅
                    // 稽徵機關+ID
                    msgbody.setPTRINACCT(feptxn.getFeptxnTaxUnit() + feptxn.getFeptxnIdno());
                } else if ("T8".equals(feptxn.getFeptxnTxCode())) { // 非15類，核定稅
                    // 銷帳編號
                    msgbody.setPTRINACCT(feptxn.getFeptxnReconSeqno());
                }
                msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
                msgbody.setPSTAN(feptxn.getFeptxnStan());
                // 轉出行
                msgbody.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
                //轉入行=>繳稅交易放 "TAX"
                msgbody.setPITXBKNO("TAX");
                // 取得原存行的促銷應用訊息
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
     * 8. label_END_OF_FUNC判斷是否需組 CON 電文回財金
     */
    private void labelEndOfFunc() {
        String feptxnRepRc = feptxn.getFeptxnRepRc();
        if ((rtnCode == FEPReturnCode.Normal) && "4001".equals(feptxnRepRc)) {
            /* +REP */
            feptxn.setFeptxnReplyCode("    "); // 4個SPACES 回覆 ATM正常
            if (!DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlAtm2way())) {
                /* for代理提款-ATM_3way 交易 */
                feptxn.setFeptxnTxrust("B"); /* PENDING */
                feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
                this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
            } else {
                /* ATM_2way 交易 */
                feptxn.setFeptxnTxrust("A"); /* 成功 */
                feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
                if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlFisc2way())) { /* for餘額查詢 */
                    feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
                    this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
                } else {
                    /* 交易直接送confirm to FISC */
                    feptxn.setFeptxnConRc("4001"); /* +CON */
                    /* 組 CON 電文送財金 */
                    rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                    feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
                    this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
                }
            }
        } else { /* 交易失敗FEPReturnCode <> Normal */
            if (StringUtils.isNotBlank(feptxnRepRc)) {
                feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
                if ("4001".equals(feptxnRepRc)) { /* +REP */
                    if (!DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlFisc2way())) { /* 3WAY */
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
                    } else {
                        /* 修改 for 2WAY */
                        feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                        feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                                FEPChannel.ATM, getATMtxData().getLogContext()));
                    }
                } else { /* -REP */
                    feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC,
                            FEPChannel.ATM, getATMtxData().getLogContext()));
                }
            } else { /* FEPReturnCode <> Normal */
                feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
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
     * 10. 更新交易記錄(FEPTXN)
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
    private FEPReturnCode updateFeptxn() {
        FEPReturnCode fpeReturnCode = FEPReturnCode.Normal;
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            fpeReturnCode = FEPReturnCode.FEPTXNUpdateError;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
        }
        return fpeReturnCode;
    }

    /**
     * 轉民國年,格式化為YY/MM/DD,年度取後碼,ex :11/06/29
     *
     * @param dateStr
     * @return
     */
    private String dateStrToYYMMDD(String dateStr) {
        String rtnDate;
        if ("00000000".equals(dateStr) || dateStr.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            rtnDate = "00/00/00";
        } else {
            dateStr = CalendarUtil.adStringToROCString(dateStr);
            int dateStrLength = dateStr.length();
            dateStr = dateStr.substring(dateStrLength - 6, dateStrLength).replaceAll("(.{2})", "$1/");
            rtnDate = dateStr.substring(0, dateStr.length() - 1);
        }
        return rtnDate;
    }
}
