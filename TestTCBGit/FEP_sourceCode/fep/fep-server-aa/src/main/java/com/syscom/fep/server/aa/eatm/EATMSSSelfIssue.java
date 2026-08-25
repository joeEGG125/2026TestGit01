package com.syscom.fep.server.aa.eatm;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.ChannelExtMapper;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.ims.IMSTextBase;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_Header;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_NS1MsgRs;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1B1PN;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1B1PN.SEND_EATM_FAA_CC1B1PN_Body;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1B1PN.SEND_EATM_FAA_CC1B1PN_Body_MsgRs_Header;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1B1PN.SEND_EATM_FAA_CC1B1PN_Body_MsgRs_SvcRs;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1B1PN.SEND_EATM_FAA_CC1B1PN_Body_NS1MsgRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.Calendar;

/**
 * @author Jaime
 */
public class EATMSSSelfIssue extends ATMPAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private ChannelExtMapper channelExtMapper = SpringBeanFactoryUtil.getBean(ChannelExtMapper.class);

    public EATMSSSelfIssue(ATMData txnData) throws Exception {
        super(txnData, "eatm");
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            // 1. Prepare():記錄MessageText & 準備回覆電文資料
            rtnCode = getATMBusiness().prepareFEPTXN();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                sendEMS(getLogContext());
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 2. AddTxData: 新增交易記錄(FEPTxn)
                this.addTxData(); // 新增交易記錄(FEPTxn)
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                this.checkBusinessRule();
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS:送往CBS主機處理
                this.sendToCBS();
            }

            // 5. UpdateTxData: 更新交易記錄(FEPTxn)
            this.updateTxData();

        } catch (Exception ex) {
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            // 6.Response:組ATM回應電文 & 回 ATMMsgHandler
            if (StringUtils.isBlank(getTxData().getTxResponseMessage())) {
                rtnMessage = this.response();
            } else {
                rtnMessage = getTxData().getTxResponseMessage();
            }
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                sendEMS(getLogContext());
            }

            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage(rtnMessage);
            getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
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
        // 3.1 檢核 ATM 電文
        rtnCode = getATMBusiness().CheckATMData();
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GO TO 5 /* 更新交易記錄 */
        }

        // 3.2 檢核ATM電文訊息押碼(MAC)
        /* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
        if (getTxData().getMsgCtl().getMsgctlReqmacType() != null) {
            /* 檢核 ATM 電文 MAC */
            String ATM_TITA_PICCMACD = getTxData().getTxObject().getRequest().getPICCMACD();
            if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
                rtnCode = FEPReturnCode.ENCCheckMACError; /* MAC Error */
                return; // GO TO 5 /* 更新交易記錄 */
            }
            String newMac = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(this.getTxData().getTxRequestMessage(), 742, 758));

            this.logContext.setMessage("Begin checkAtmMac mac:" + newMac);
            logMessage(this.logContext);

            rtnCode = new ENCHelper(getTxData()).checkAtmMacNew("NEATM001",
                    StringUtils.substring(this.getTxData().getTxRequestMessage(), 36, 742),
                    newMac);
            this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        }
    }


    /**
     * 4. SendToCBS:送往CBS主機處理
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
//		getFeptxn().setFeptxnStan(getATMBusiness().getStan());/*先取 STAN 以供主機電文使用*/
        /* 交易前置處理查詢處理 */
        String AATxTYPE = "0"; // 上CBS查詢、檢核
        String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
        rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
    }

    /**
     * 5. UpdateTxData: 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {
        feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); // (RESPONSE)
        feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                getTxData().getLogContext()));
        feptxn.setFeptxnAaRc(rtnCode.getValue());
        feptxn.setFeptxnAaComplete((short) 1); /* AA Complete */
        /* For報表, 寫入處理結果 */
        if (rtnCode == FEPReturnCode.Normal) {
            if (feptxn.getFeptxnWay() == 3) {
                feptxn.setFeptxnTxrust("B"); /* 處理結果=Pending */
            } else {
                feptxn.setFeptxnTxrust("A"); /* 處理結果=成功 */
            }
        } else {
            feptxn.setFeptxnTxrust("R"); /* 處理結果=Reject */
        }

        // 回寫 FEPTXN
        /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
        FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
        try {
            FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            rtnCode2 = FEPReturnCode.FEPTXNUpdateError;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
        }

        if (rtnCode2 != FEPReturnCode.Normal) {
            // 回寫檔案 (FEPTxn) 發生錯誤
            this.feptxn.setFeptxnReplyCode("L013");
            sendEMS(getLogContext());
        }
    }

    /**
     * 6. Response:組ATM回應電文 & 回 ATMMsgHandler
     *
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getSvcRq();

            String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                    FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
            RefString rfs = new RefString();

            if (rtnCode != FEPReturnCode.Normal) {

                SEND_EATM_FAA_CC1APC rs = new SEND_EATM_FAA_CC1APC();
                SEND_EATM_FAA_CC1APC_Body rsbody = new SEND_EATM_FAA_CC1APC_Body();
                SEND_EATM_FAA_CC1APC_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1APC_Body_NS1MsgRs();
                SEND_EATM_FAA_CC1APC_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1APC_Body_MsgRs_Header();
                SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs body = new SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs();
                msgrs.setHeader(header);
                msgrs.setSvcRq(body);
                rsbody.setRs(msgrs);
                rs.setBody(rsbody);

                header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                header.setCHANNEL(feptxn.getFeptxnChannel());
                header.setMSGID(atmReqheader.getMSGID());
                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                header.setSYSTEMID("FEP");
                if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
                    header.setSTATUSCODE("4001");
                } else {
                    header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                }
                // 組 Header(OUTPUT-1)
                body.setWSID(atmReqbody.getWSID());
                body.setRECFMT("1");
                body.setMSGCAT("F");
                body.setMSGTYP("PC");
                header.setSEVERITY("ERROR");
                body.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                body.setTRANTIME(systemTime); // 系統時間
                body.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                body.setTDRSEG(atmReqbody.getTDRSEG()); //

                body.setCARDACT("0"); //晶片卡不留置:固定放”0”

                // 組D0(OUTPUT-2)畫面顯示(Display message)
                // 組 D0(004)
                body.setDATATYPE("D0");
                body.setDATALEN("004");
                body.setACKNOW("0");
                // 以CBS_RC取得轉換後的PBMDPO編號
                body.setPAGENO(TxHelper.getRCFromErrorCode(String.valueOf(feptxn.getFeptxnCbsRc()), FEPChannel.CBS, FEPChannel.ATM,
                        getTxData().getLogContext()));

                if (StringUtils.isBlank(body.getPAGENO())) {
                    body.setPAGENO("226");
                }

                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                body.setPTYPE("S0");
                body.setPLEN("189");
                body.setPBMPNO("010000");
                // 西元年轉民國年，格式：YYY/MM/DD
                body.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
                // 時間格式：HH :MM :SS
                body.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
                // CBS下送的交易種類
                body.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                body.setPTID(feptxn.getFeptxnAtmno());

                // 格式 :$$$,$$$,$$9 ex :$10,000
                body.setPTXAMT(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"), 11, " "));
                // 格式:$999 ex :$0
                body.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"), 4, " "));

                String balance = this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue());
                if (StringUtils.isNotBlank(balance)) {
                    // 格式：$$$,$$$,$$$,$$9.99 ex：$97,947.00
                    body.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnBalb(), "$#,##0.00"), 18, " "));
                } else {
                    body.setPBAL(StringUtils.leftPad("", 18, " "));
                }


                // 轉出帳號(明細表顯示內容)
                String totaFromact = this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue());
                if (StringUtils.isNotBlank(totaFromact)) {
                    body.setPACCNO(totaFromact);
                }

                body.setPATXBKNO(feptxn.getFeptxnBkno());
                body.setPSTAN(feptxn.getFeptxnStan());
                //CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
                body.setPBUSINESSDATE(feptxn.getFeptxnTbsdy());
                body.setPITXBKNO(feptxn.getFeptxnTrinBkno());

                // ATM回應代碼(空白放 "000")
                String feptxnCbsRc = feptxn.getFeptxnCbsRc();
                if (StringUtils.isBlank(feptxnCbsRc)) {
                    body.setPRCCODE("000 ");
                } else {
                    body.setPRCCODE(StringUtils.rightPad(feptxnCbsRc, 4, " "));
                }
                // 轉出行
                body.setPOTXBKNO(feptxn.getFeptxnTroutBkno());

                //CBS下送ATM的促銷應用訊息
                body.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));

                /* CALL ENC 取得MAC 資料 */
                String res = body.makeMessage();
                rfs.set("");
                rtnCode = atmEncHelper.makeAtmMac("NEATM001", res, rfs);
                if (rtnCode != FEPReturnCode.Normal) {
                    body.setPRCCODE(StringUtils.rightPad(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getLogContext()), 4, " "));
                    body.setMACCODE("");
                } else {
                    body.setMACCODE(rfs.get()); /* 訊息押碼 */
                }

                rtnMessage = new IMSTextBase().makeMessage(body, "0");
                body.setOUTDATA(rtnMessage);
                rtnMessage = XmlUtil.toXML(rs);

            } else {
                SEND_EATM_FAA_CC1B1PN rs = new SEND_EATM_FAA_CC1B1PN();
                SEND_EATM_FAA_CC1B1PN_Body rsbody = new SEND_EATM_FAA_CC1B1PN_Body();
                SEND_EATM_FAA_CC1B1PN_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1B1PN_Body_NS1MsgRs();
                SEND_EATM_FAA_CC1B1PN_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1B1PN_Body_MsgRs_Header();
                SEND_EATM_FAA_CC1B1PN_Body_MsgRs_SvcRs body = new SEND_EATM_FAA_CC1B1PN_Body_MsgRs_SvcRs();
                msgrs.setHeader(header);
                msgrs.setSvcRq(body);
                rsbody.setRs(msgrs);
                rs.setBody(rsbody);

                header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                header.setCHANNEL(feptxn.getFeptxnChannel());
                header.setMSGID(atmReqheader.getMSGID());
                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                header.setSYSTEMID("FEP");
                if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
                    header.setSTATUSCODE("4001");
                } else {
                    header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                }
                // 組 Header(OUTPUT-1)
                body.setWSID(atmReqbody.getWSID());
                body.setRECFMT("1");
                body.setMSGCAT("F");
                body.setMSGTYP("PN");
                header.setSEVERITY("INFO");
                body.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                body.setTRANTIME(systemTime); // 系統時間
                body.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                body.setTDRSEG(atmReqbody.getTDRSEG()); //

                body.setCARDACT("0"); //晶片卡不留置:固定放”0”


                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                body.setPTYPE("S0");
                body.setPLEN("189");
                body.setPBMPNO("010000");
                // 西元年轉民國年，格式：YYY/MM/DD
                body.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
                // 時間格式：HH :MM :SS
                body.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
                // CBS下送的交易種類
                body.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                body.setPTID(feptxn.getFeptxnAtmno());

                // 格式 :$$$,$$$,$$9 ex :$10,000
                body.setPTXAMT(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"), 11, " "));
                // 格式:$999 ex :$0
                body.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"), 4, " "));

                String balance = this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue());
                if (StringUtils.isNotBlank(balance)) {
                    // 格式：$$$,$$$,$$$,$$9.99 ex：$97,947.00
                    body.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnBalb(), "$#,##0.00"), 18, " "));
                } else {
                    body.setPBAL(StringUtils.leftPad("", 18, " "));
                }


                // 轉出帳號(明細表顯示內容)
                String totaFromact = this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue());
                if (StringUtils.isNotBlank(totaFromact)) {
                    body.setPACCNO(totaFromact);
                }

                body.setPATXBKNO(feptxn.getFeptxnBkno());
                body.setPSTAN(feptxn.getFeptxnStan());
                //CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
                body.setPBUSINESSDATE(feptxn.getFeptxnTbsdy());
                body.setPITXBKNO(feptxn.getFeptxnTrinBkno());

                // ATM回應代碼(空白放 "000")
                String feptxnCbsRc = feptxn.getFeptxnCbsRc();
                if (StringUtils.isBlank(feptxnCbsRc)) {
                    body.setPRCCODE("000 ");
                } else {
                    body.setPRCCODE(StringUtils.rightPad(feptxnCbsRc, 4, " "));
                }
                // 轉出行
                body.setPOTXBKNO(feptxn.getFeptxnTroutBkno());

//				CBS下送ATM的促銷應用訊息
                body.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));

                /* CALL ENC 取得MAC 資料 */
                String res = body.makeMessage();
                rfs.set("");
                rtnCode = atmEncHelper.makeAtmMac("NEATM001", res, rfs);
                if (rtnCode != FEPReturnCode.Normal) {
                    body.setPRCCODE(StringUtils.rightPad(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getLogContext()), 4, " "));
                    body.setMACCODE("");
                } else {
                    body.setMACCODE(rfs.get()); /* 訊息押碼 */
                }

                rtnMessage = new IMSTextBase().makeMessage(body, "0");
                body.setOUTDATA(rtnMessage);
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
     * 西元日期(yyyyMMdd)轉成民國日期(yyy/MM/dd)
     *
     * @param date
     * @return
     */
    private String formatDate(String date) {
        if (StringUtils.isBlank(date)) {
            return date;
        } else if (date.length() != 8) {
            date = StringUtils.leftPad(StringUtils.right(date, 8), 8, '0');
        }
        StringBuilder sb = new StringBuilder();
        date = CalendarUtil.adStringToROCString(date);
        int dateLength = date.length();
        String year = StringUtils.substring(date, 0, dateLength - 4);
        String day = StringUtils.substring(date, dateLength - 2);
        String month = StringUtils.substring(date, dateLength - 4, dateLength - 2);
        sb.append(year).append('/').append(month).append('/').append(day);
        return sb.toString();
    }

    /**
     * 時間格式字串：HHmmss轉為HH:mm:ss
     *
     * @param time
     * @return
     */
    private String formatTime(String time) {
        if (StringUtils.isBlank(time)) {
            return time;
        } else if (time.length() != 6) {
            time = StringUtils.leftPad(StringUtils.right(time, 6), 6, '0');
        }
        StringBuilder sb = new StringBuilder();
        boolean addColon = true;
        for (int i = 0; i < time.length(); i++) {
            if (addColon) {
                sb.append(':');
            }
            sb.append(time.charAt(i));
            addColon = !addColon;
        }
        return sb.substring(1);
    }

}
