package com.syscom.fep.server.aa.eatm;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.IMSTextBase;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FRN_HEAD3;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FRN_HEAD3.SEND_EATM_FRN_HEAD3_Body;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FRN_HEAD3.SEND_EATM_FRN_HEAD3_Body_MsgRs_Header;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FRN_HEAD3.SEND_EATM_FRN_HEAD3_Body_MsgRs_SvcRs;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FRN_HEAD3.SEND_EATM_FRN_HEAD3_Body_NS1MsgRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.Calendar;

/**
 * @author Jaime
 */
public class EATMRervese extends ATMPAABase {
    private Object tota = null;
    private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    boolean isGoResponse = false;
    String AATxTYPE = "";

    public EATMRervese(ATMData txnData) throws Exception {
        super(txnData, "eatm");
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            // 1. CheckBusinessRule: 商業邏輯檢核
            this.checkBusinessRule();


            if (!isGoResponse || rtnCode == FEPReturnCode.Normal) {
                // 2. SendToCBS(if need):
                this.sendToCBS();
            }


        } catch (Exception ex) {
            rtnMessage = "";
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
     * 1. CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private void checkBusinessRule() throws Exception {
        // 1.1 取得原交易之 FEPTXN
        getATMBusiness().setFeptxn(getATMBusiness().checkConData());
        if (getATMBusiness().getFeptxn() == null) {
            rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
            sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            isGoResponse = true;
            return; // GO TO 4 /* 組 ATM 回應電文 */
        }

        // 1.2 將ATM確認電文, 準備寫入原交易 FEPTXN欄位
        rtnCode = getATMBusiness().prepareConFEPTXN();
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GO TO 3 /* 更新交易記錄 */
        }

        // 1.3 交易確認電文檢核 MAC
        /* 檢核 ATM 電文 MAC */
        String ATM_TITA_PICCMACD = this.getATMRequest().getPICCMACD();
        if (StringUtils.isNotBlank(ATM_TITA_PICCMACD)) {
            String newMac = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(this.getTxData().getTxRequestMessage(), 742, 758));

            this.logContext.setMessage("Begin checkAtmMac mac:" + newMac);
            logMessage(this.logContext);

            rtnCode = new ENCHelper(getTxData()).checkAtmMacNew("NEATM001",
                    StringUtils.substring(this.getTxData().getTxRequestMessage(), 36, 742),
                    newMac);
            this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
            logMessage(this.logContext);

            if (rtnCode != FEPReturnCode.Normal) {
                return; // GO TO 3 /* 更新交易記錄 */
            }
        }
    }

    /**
     * 2. SendToCBS(if need)
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {

        if (feptxn.getFeptxnCbsTimeout() == 1) {
            /* 交易前置處理查詢處理 */
            String AATxTYPE = "0"; // 上CBS查詢、檢核
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
            if (rtnCode == FEPReturnCode.Normal) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
            }
        }


    }

    /**
     * 4. Response:組ATM回應電文 & 回 ATMMsgHandler
     *
     * @return
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            ATMGeneralRequest atmReq = this.getATMRequest();
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmheader = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmbody = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getSvcRq();

            String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                    FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            String feptxnTxCode = feptxn.getFeptxnTxCode();
            ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
            RefString rfs = new RefString();
            String totaToact;

            SEND_EATM_FRN_HEAD3 rs = new SEND_EATM_FRN_HEAD3();
            SEND_EATM_FRN_HEAD3_Body rsbody = new SEND_EATM_FRN_HEAD3_Body();
            SEND_EATM_FRN_HEAD3_Body_NS1MsgRs msgrs = new SEND_EATM_FRN_HEAD3_Body_NS1MsgRs();
            SEND_EATM_FRN_HEAD3_Body_MsgRs_Header header = new SEND_EATM_FRN_HEAD3_Body_MsgRs_Header();
            SEND_EATM_FRN_HEAD3_Body_MsgRs_SvcRs body = new SEND_EATM_FRN_HEAD3_Body_MsgRs_SvcRs();

            msgrs.setHeader(header);
            msgrs.setSvcRq(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);


            header.setCLIENTTRACEID(atmheader.getCLIENTTRACEID());
            header.setCHANNEL(feptxn.getFeptxnChannel());
            header.setMSGID(atmheader.getMSGID());
            header.setCLIENTDT(atmheader.getCLIENTDT());
            header.setSYSTEMID("FEP");
            if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
                header.setSTATUSCODE("4001");
            } else {
                header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
            }

            // 組 Header(OUTPUT-1)
            body.setWSID(atmbody.getWSID());
            body.setRECFMT("0");
            body.setMSGCAT("F");

            body.setMSGTYP("PC");
            header.setSEVERITY("INFO");

            body.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
            body.setTRANTIME(systemTime); // 系統時間
            body.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
            body.setTDRSEG("02"); // 回覆FR2
            body.setPRCRDACT("0");
            body.setRECFMT("0");

            /* CALL ENC 取得MAC 資料 */
            String res = body.makeMessage();
            rfs.set("");
            rtnCode = atmEncHelper.makeAtmMac("NEATM001", res, rfs);
            if (rtnCode != FEPReturnCode.Normal) {
                body.setMACCODE("");
            } else {
                body.setMACCODE(rfs.get()); /* 訊息押碼 */
            }

            if (rtnCode != FEPReturnCode.Normal) {
                body.setMACCODE(""); /* 訊息押碼 */
            } else {
                body.setMACCODE(rfs.get()); /* 訊息押碼 */
            }

            if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                if (StringUtils.isNotBlank(feptxn.getFeptxnConRc())) {
                    body.setERRCODE(feptxn.getFeptxnConRc());
                } else {
                    body.setERRCODE(feptxn.getFeptxnReplyCode());
                }
                body.setPROCSTA("ERR");
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) {
                body.setERRCODE(feptxn.getFeptxnRepRc());
                body.setPROCSTA("ERR");

            } else if (StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
                body.setERRCODE(feptxn.getFeptxnReplyCode());
                body.setPROCSTA("ERR");

            } else if (feptxn.getFeptxnAaRc() != FEPReturnCode.Normal.getValue()) {
                body.setERRCODE(String.valueOf(feptxn.getFeptxnAaRc()));
                body.setPROCSTA("ERR");

            } else {
                body.setERRCODE("");
                body.setPROCSTA("NO");
            }
            if (feptxn.getFeptxnPending() == 1) {
                body.setPROCSTA("NO");
            }

            if (feptxn.getFeptxnAccType() == 1) {
                body.setACTFG("Y");
            } else if (feptxn.getFeptxnAccType() == 2) {
                body.setRVSFG("Y");
            }

            rtnMessage = new IMSTextBase().makeMessage(body, "0");
            body.setOUTDATA(rtnMessage);
            rtnMessage = XmlUtil.toXML(rs);

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * 5. 交易通知 (if need)
     *
     * @return
     * @throws Exception
     */
    private void sendToMailHunter() {
        try {
            String noticeType = feptxn.getFeptxnNoticeType();
            if (StringUtils.isNotBlank(noticeType) && "4001".equals(feptxn.getFeptxnRepRc()) && "4001".equals(feptxn.getFeptxnConRc())) {
                switch (noticeType) {
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
            }
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
            sendEMS(this.logContext);
        }
    }

}
