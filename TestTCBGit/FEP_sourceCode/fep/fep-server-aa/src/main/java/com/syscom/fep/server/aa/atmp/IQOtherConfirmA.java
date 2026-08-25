package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * @author vincent
 */
public class IQOtherConfirmA extends INBKAABase {
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private boolean isGoResponse = false;
    private String atmno;
    private String tita ;

    public IQOtherConfirmA(ATMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        // 記錄FEPLOG內容
        tita = EbcdicConverter.fromHex(CCSID.English, this.getATMtxData().getTxRequestMessage());
        this.logContext.setProgramFlowType(ProgramFlow.AAIn);
        this.logContext.setMessageFlowType(MessageFlow.Confirmation);
        this.logContext.setProgramName(StringUtils.join(this.getATMtxData().getAaName(), ".processRequestData"));
        this.logContext.setMessage("ASCII TITA:"+tita);
        this.logContext.setRemark(StringUtils.join("Enter ", this.getATMtxData().getAaName()));
        logMessage(this.logContext);

        try {
            // 3. CheckBusinessRule: 商業邏輯檢核
            this.checkBusinessRule();

            if (!isGoResponse) {
                // 4. 更新交易記錄(FEPTxn)
                this.updateTxData();
            }

            // 5. Response:組ATM回應電文 & 回 ATMMsgHandler
            rtnMessage = this.response();
            if ("EAT".equals(feptxn.getFeptxnChannel())) {
				rtnMessage = this.eatmResponse(rtnMessage);
			}
            
            // 6. 交易通知 (if need)
            getATMBusiness().sendToNotify();
        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        }

        try {
            getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getATMtxData().getLogContext().setMessage("MessageToATM:"+rtnMessage);
            getATMtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getATMtxData().getLogContext().setMessageFlowType(MessageFlow.ResponseConfirmation);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode,getLogContext()));
            logMessage(Level.DEBUG, this.logContext);
        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        }

        return rtnMessage;
    }

    /**
     * 3. CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private void checkBusinessRule() throws Exception {
        // 3.1 取得原交易之 FEPTXN
        Feptxn tempFeptxn = getATMBusiness().checkConData();
        feptxn = tempFeptxn;
        getATMBusiness().setFeptxn(tempFeptxn);
        getATMtxData().setFeptxn(feptxn);
        getFiscBusiness().setFeptxn(feptxn);
        if (getATMBusiness().getFeptxn() == null) {
            rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
            sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            isGoResponse = true;
            return; // GO TO 5 /* 組 ATM 回應電文 */
        }else {
        	Feptxntcb tempFeptxntcb = getATMBusiness().checkFeptxntcbData(feptxn);
            feptxntcb = tempFeptxntcb;
            getATMBusiness().setFeptxntcb(feptxntcb);
            getATMtxData().setFeptxntcb(feptxntcb);
            getFiscBusiness().setFeptxntcb(feptxntcb);
            if (getATMBusiness().getFeptxntcb() == null) {
                rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
                getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
                getLogContext().setRemark("Feptxntcb is null");
                sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                isGoResponse = true;
                return; // GO TO 5 /* 組 ATM 回應電文 */
            }
        }

        // 3.2 將ATM確認電文, 準備寫入原交易 FEPTXN欄位
        rtnCode = getATMBusiness().prepareConFEPTXN();
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GO TO 4 /* 更新交易記錄 */
        }

        // 3.3 交易確認電文檢核 MAC
        /*因Confirm MAC error 需繼續執行其他步驟,故存入不同 RC*/
		String ATM_TITA_PICCMACD = this.getATMRequest().getPICCMACD();
		String wkMAC;
		if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError;
			 return; // GO TO 4 /* 更新交易記錄 */
		}
		
		String newMac = ATM_TITA_PICCMACD;

		this.logContext.setRemark("Begin checkAtmMac mac:" + newMac);
		logMessage(this.logContext);
		
		wkMAC = StringUtils.substring(this.getATMtxData().getTxRequestMessage(), 36, 742);
		// CHANNEL = "EAT"，ATMNO以 "NEATM001"去驗，在 CheckATMMACNew replace
		atmno = feptxn.getFeptxnAtmno();
		
		rtnCode = new ENCHelper(getATMtxData()).checkAtmMacNew(atmno, wkMAC, newMac);
		
		this.logContext.setRemark("after checkAtmMac RC:" + rtnCode.toString());
		logMessage(this.logContext);
		
    }

    /**
     * 4. 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {
    	PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
        	feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response); // (RESPONSE)
            feptxn.setFeptxnAaRc(this.rtnCode.getValue());
            feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(this.rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
            feptxn.setFeptxnAaComplete((short) 1);
            int updateCount = feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
			int updateCount2 = feptxnDao.updateByPrimaryKeySelective(this.feptxntcb);
	        if (updateCount <= 0 || updateCount2 <= 0) { // 更新失敗
	        	throw new Exception(); //2025.07.15 Transaction call review 調整
			}
			transactionManager.commit(txStatus);
        } catch (Exception ex) {
        	transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
        }
    }

    /**
     * 6. Response:組ATM回應電文 & 回 ATMMsgHandler
     *
     * @return
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            // 組 Header
            ATMGeneralRequest atmReq = this.getATMRequest();
            RefString rfs = new RefString();
            ATM_FSN_HEAD2 atm_fsn_head2 = new ATM_FSN_HEAD2();
            atm_fsn_head2.setWSID(atmReq.getWSID());
            atm_fsn_head2.setRECFMT("1");
            atm_fsn_head2.setMSGCAT("F");
            atm_fsn_head2.setMSGTYP("PC");
            atm_fsn_head2.setTRANDATE(atmReq.getTRANDATE()); // 西元後兩碼+系統月日共六碼
            atm_fsn_head2.setTRANTIME(atmReq.getTRANTIME()); // 系統時間
            atm_fsn_head2.setTRANSEQ(atmReq.getTRANSEQ());
            atm_fsn_head2.setTDRSEG(atmReq.getTDRSEG()); // 回覆FSN或FSE
            // PRCRDACT = 0 或4都是未留置卡片, 2 是吃卡, 只有磁條密碼變更交易
            // (FC1、P1)主機才有可能依據狀況要求吃卡
            atm_fsn_head2.setPRCRDACT("0");
            if (feptxn == null) {
                atm_fsn_head2.setRECFMT("0");
            }

            /* CALL ENC 取得MAC 資料 */
            ENCHelper atmEncHelper = new ENCHelper(this.getATMtxData());

            rfs.set("");
            rtnMessage = atm_fsn_head2.makeMessage();
            
            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
            	rtnMessage = atm_fsn_head2.getMSGCAT() + atm_fsn_head2.getMSGTYP() 
            		+ atm_fsn_head2.getTRANDATE() + atm_fsn_head2.getTRANTIME() 
	 				+ feptxn.getFeptxnStan()
            		+ atmReq.getPICCBI11() +  atmReq.getTRANSEQ()
            		+ atmReq.getTDRSEG() + "0000";
            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
                this.logContext.setRemark("New inputData");
            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
                logMessage(this.logContext);
            }
            
            rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
            if (rtnCode != FEPReturnCode.Normal) {
                atm_fsn_head2.setMACCODE(""); /* 訊息押碼 */
            } else {
                atm_fsn_head2.setMACCODE(rfs.get()); /* 訊息押碼 */
            }
            this.logContext.setRemark("after makeAtmMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            rtnMessage = atm_fsn_head2.makeMessage();
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }
    
    private String eatmResponse(String outdata) throws Exception {
        String rtnMessage = "" ;
        try {
            /* 組 ATM Response OUT-TEXT */
            RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();

            if (rtnCode != FEPReturnCode.Normal) {
                SEND_EATM_FAA_CC1APC rs = new SEND_EATM_FAA_CC1APC();
                SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body rsbody = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body();
                SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_NS1MsgRs();
                SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_Header();
                SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs();
                msgrs.setSvcRq(msgbody);
                msgrs.setHeader(header);
                rsbody.setRs(msgrs);
                rs.setBody(rsbody);
                header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                header.setCHANNEL(feptxn.getFeptxnChannel());
                header.setMSGID(atmReqheader.getMSGID());
                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                header.setSYSTEMID("FEP");
                if(StringUtils.isNotBlank(feptxn.getFeptxnConReplyCode())){
                    header.setSTATUSCODE(feptxn.getFeptxnConReplyCode());
                }
                if(feptxn == null){ //原交易之 FEPTXN  is Nothing
    				header.setSTATUSCODE("EF0305");
    				header.setSTATUSDESC("OriginalMessageNotFound");
    			}
                header.setSEVERITY("ERROR");
                msgbody.setOUTDATA(outdata);
                rtnMessage = XmlUtil.toXML(rs);
            } else {
                SEND_EATM_FAA_CC1APN rs = new SEND_EATM_FAA_CC1APN();
                SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body rsbody = new SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body();
                SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_NS1MsgRs();
                SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_MsgRs_Header();
                SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_MsgRs_SvcRs();
                msgrs.setSvcRq(msgbody);
                msgrs.setHeader(header);
                rsbody.setRs(msgrs);
                rs.setBody(rsbody);
                header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                header.setCHANNEL(feptxn.getFeptxnChannel());
                header.setMSGID(atmReqheader.getMSGID());
                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                header.setSYSTEMID("FEP");
                if(StringUtils.isNotBlank(feptxn.getFeptxnConReplyCode())){
                    header.setSTATUSCODE(feptxn.getFeptxnConReplyCode());
                }
                if(feptxn == null){ //原交易之 FEPTXN  is Nothing
    				header.setSTATUSCODE("EF0305");
    				header.setSTATUSDESC("OriginalMessageNotFound");
    			}
                header.setSEVERITY("INFO");
                msgbody.setOUTDATA(outdata);
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
