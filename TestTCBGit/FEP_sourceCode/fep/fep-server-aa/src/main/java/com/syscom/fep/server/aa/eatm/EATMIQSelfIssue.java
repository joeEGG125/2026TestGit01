package com.syscom.fep.server.aa.eatm;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
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
public class EATMIQSelfIssue extends ATMPAABase {
    private boolean goTO7 = true;
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private ChannelExtMapper channelExtMapper = SpringBeanFactoryUtil.getBean(ChannelExtMapper.class);

    public EATMIQSelfIssue(ATMData txnData) throws Exception {
        super(txnData,"eatm");
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "" ;
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

            if (!goTO7) {
                // 5. UpdateTxData: 更新交易記錄(FEPTxn)
                this.updateTxData();

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
            }
        } catch (Exception ex) {
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            //7.交易結束
            feptxn.setFeptxnAaRc(rtnCode.getValue());
            feptxn.setFeptxnAaComplete((short) 1);
            try {
                FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
                String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
                feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateTxData"));
                feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
            } catch (Exception ex) {
                rtnCode = FEPReturnCode.FEPTXNUpdateError;
                getLogContext().setProgramException(ex);
                getLogContext().setProgramName(ProgramName + ".updateTxData");
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
            String newMac = EbcdicConverter.fromHex(CCSID.English,StringUtils.substring(this.getTxData().getTxRequestMessage(), 742, 758));

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
        getFeptxn().setFeptxnStan(getATMBusiness().getStan());/*先取 STAN 以供主機電文使用*/
        /* 交易前置處理查詢處理 */
        String AATxTYPE = "0" ; // 上CBS查詢、檢核
        String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
        rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
        if (rtnCode != FEPReturnCode.Normal) {
            if (!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
                goTO7 = false;
            }
            if (feptxn.getFeptxnCbsTimeout() == 1) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
            }
        } else {
            goTO7 = false;
        }
    }

    /**
     * 5. UpdateTxData: 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {
        if (feptxn.getFeptxnCbsTimeout() == 0) {
            feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); // (RESPONSE)
            feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                    getTxData().getLogContext()));
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
        String rtnMessage = "" ;
        try {
            /* 組 ATM Response OUT-TEXT */
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getSvcRq();
            String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                    FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            String feptxnTxCode = feptxn.getFeptxnTxCode();
            ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
            RefString rfs = new RefString();
            String totaToact;

            if (rtnCode != FEPReturnCode.Normal) {
                SEND_EATM_FAA_CC1APC rs = new SEND_EATM_FAA_CC1APC();
                SEND_EATM_FAA_CC1APC_Body rsbody = new SEND_EATM_FAA_CC1APC_Body();
                SEND_EATM_FAA_CC1APC_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1APC_Body_NS1MsgRs();
                SEND_EATM_FAA_CC1APC_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1APC_Body_MsgRs_Header();
                SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs();
                msgrs.setSvcRq(msgbody);
                msgrs.setHeader(header);
                rsbody.setRs(msgrs);
                rs.setBody(rsbody);
                header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
                header.setCHANNEL(feptxn.getFeptxnChannel());
                header.setMSGID(atmReqheader.getMSGID());
                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                header.setSYSTEMID("FEP");
                if(StringUtils.isBlank(feptxn.getFeptxnReplyCode())){
                    header.setSTATUSCODE("4001");
                } else {
                    header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                }
                // 組 Header(OUTPUT-1)
                msgbody.setWSID(atmReqbody.getWSID());
                msgbody.setRECFMT("1");
                msgbody.setMSGCAT("F");
                msgbody.setMSGTYP("PC");
                header.setSEVERITY("ERROR");
                msgbody.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                msgbody.setTRANTIME(systemTime.substring(8, 14)); // 系統時間
                msgbody.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                msgbody.setTDRSEG(atmReqbody.getTDRSEG()); // 回覆FAA
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
                // 組S0明細表內容(PRINT message),依交易下送欄位,電文總長度也不同
                msgbody.setPTYPE("S0");
                msgbody.setPLEN("191");
                msgbody.setPBMPNO("010000"); // FPC
                // 西元年轉民國年
                msgbody.setPDATE(new SimpleDateFormat("yyy/MM/dd").format(new SimpleDateFormat("yyyMMdd").parse(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()))));
                msgbody.setPTIME(new SimpleDateFormat(FormatUtil.FORMAT_TIME_HH_MM_SS).format(new SimpleDateFormat("HHmmss").parse(feptxn.getFeptxnTxTime())));
                // CBS下送的交易種類
                msgbody.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                msgbody.setPTID(feptxn.getFeptxnAtmno());
                // 格式 :$$$,$$$,$$9 ex :$10,000
                msgbody.setPTXAMT(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"), 11, " "));
                if ("E".equals(feptxn.getFeptxnTxCode().subSequence(0, 1))) {
                    // 格式:$999.0 ex :$0.0
                    msgbody.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnNpsFeeCustpay(), "$#,##0.0"), 4, " "));
                } else {
                    // 格式:$999 ex :$0
                    msgbody.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"), 4, " "));
                }

                if(StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue()))){
                    msgbody.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnBalb(), "$#,##0.00"), 18, " "));
                } else {
                    msgbody.setPBAL(StringUtils.leftPad("", 18, " "));
                }

                // CBS下送的轉出帳號(明細表顯示內容)
                msgbody.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
                msgbody.setPSTAN(feptxn.getFeptxnStan());
                // 轉出行
                msgbody.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
                // CBS下送ATM的促銷應用訊息
                msgbody.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
                /* CALL ENC 取得MAC 資料 */
                String res = msgbody.makeMessage();
                rfs.set("");
                rtnCode = atmEncHelper.makeAtmMac("NEATM001", res, rfs);
                if (rtnCode != FEPReturnCode.Normal) {
                    msgbody.setPRCCODE(StringUtils.rightPad(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getLogContext()), 4, " "));
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
                header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                header.setCHANNEL(feptxn.getFeptxnChannel());
                header.setMSGID(atmReqheader.getMSGID());
                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                header.setSYSTEMID("FEP");
                if(StringUtils.isBlank(feptxn.getFeptxnReplyCode())){
                    header.setSTATUSCODE("4001");
                } else {
                    header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                }
                // 組 Header(OUTPUT-1)
                msgbody.setWSID(atmReqbody.getWSID());
                msgbody.setRECFMT("1");
                msgbody.setMSGCAT("F");
                msgbody.setMSGTYP("PN");
                header.setSEVERITY("INFO");
                msgbody.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                msgbody.setTRANTIME(systemTime.substring(8, 14)); // 系統時間
                msgbody.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                msgbody.setTDRSEG(atmReqbody.getTDRSEG()); // 回覆FAA
                msgbody.setCARDACT("0"); // 晶片卡不留置:固定放”0”

                // 組D0(OUTPUT-2)畫面顯示(Display message)
                // 組 D0(004)
                msgbody.setDATATYPE("D0");
                msgbody.setDATALEN("022");
                msgbody.setACKNOW("0");
                msgbody.setPAGENO("030");
                msgbody.setPRCCODE("000 ");

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

                // 組S0明細表內容(PRINT message),依交易下送欄位,電文總長度也不同
                msgbody.setPTYPE("S0");
                msgbody.setPLEN("191");
                msgbody.setPBMPNO("000010"); // FPN
                // 西元年轉民國年
                msgbody.setPDATE(new SimpleDateFormat("yyy/MM/dd").format(new SimpleDateFormat("yyyMMdd").parse(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()))));
                msgbody.setPTIME(new SimpleDateFormat(FormatUtil.FORMAT_TIME_HH_MM_SS).format(new SimpleDateFormat("HHmmss").parse(feptxn.getFeptxnTxTime())));
                // CBS下送的交易種類
                msgbody.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                msgbody.setPTID(feptxn.getFeptxnAtmno());
                // 格式 :$$$,$$$,$$9 ex :$10,000
                msgbody.setPTXAMT(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"), 11, " "));
                if ("E".equals(feptxn.getFeptxnTxCode().subSequence(0, 1))) {
                    // 格式:$999.0 ex :$0.0
                    msgbody.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnNpsFeeCustpay(), "$#,##0.0"), 4, " "));
                } else {
                    // 格式:$999 ex :$0
                    msgbody.setPFEE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"), 4, " "));
                }
                if(StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue()))){
                    msgbody.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnBalb(), "$#,##0.00"), 18, " "));
                } else {
                    msgbody.setPBAL(StringUtils.leftPad("", 18, " "));
                }
                // CBS下送的轉出帳號(明細表顯示內容)
                msgbody.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
                msgbody.setPSTAN(feptxn.getFeptxnStan());
                // 轉出行
                msgbody.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
                // CBS下送ATM的促銷應用訊息
                msgbody.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
                /* CALL ENC 取得MAC 資料 */
                String res = msgbody.makeMessage();
                rfs.set("");
                rtnCode = atmEncHelper.makeAtmMac("NEATM001", res, rfs);
                if (rtnCode != FEPReturnCode.Normal) {
                    msgbody.setPRCCODE(StringUtils.rightPad(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getLogContext()), 4, " "));
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
            return "" ;
        }
        return rtnMessage;
    }

}
