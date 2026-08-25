package com.syscom.fep.server.aa.atmp;

import java.util.Objects;

import com.syscom.fep.enchelper.enums.ENCKeyType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
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
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN;

/**
 * @author vincent
 */
public class SelfIssueC extends ATMPAABase {
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    boolean isGoResponse = false;
	private String atmno;
	private boolean needCheckMac;

    public SelfIssueC(ATMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
			// 記錄FEPLOG內容
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Confirmation);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage("ASCII TITA:" + EbcdicConverter.fromHex(CCSID.English, this.getTxData().getTxRequestMessage()));
            this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(this.logContext);
        	
            // 1. CheckBusinessRule: 商業邏輯檢核
            this.checkBusinessRule();
            
            //2.	FEP檢核錯誤處理
            if (this.rtnCode != CommonReturnCode.Normal) {
				feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
				feptxn.setFeptxnAaRc(rtnCode.getValue());
            }
            
            //3.	SendToCBS (if need):自行交易確認電文(for 磁條密碼變更交易)
            sendToCBS();
            
            if (!isGoResponse) {
                // 4. UpdateTxData: 更新交易記錄(FEPTxn)
                this.updateTxData();
            }

            // 5. Response:組ATM回應電文 & 回 ATMMsgHandler
            rtnMessage = this.response();
            if("EAT".equals(feptxn.getFeptxnChannel())) {
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
            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage("MessageToATM:" + rtnMessage);
            getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getTxData().getLogContext().setMessageFlowType(MessageFlow.ResponseConfirmation);
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
     * 1. CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private void checkBusinessRule() throws Exception {
        // 1.1 取得原交易之 FEPTXN
        Feptxn tempFeptxn = getATMBusiness().checkConData();
        feptxn = tempFeptxn;
        getATMBusiness().setFeptxn(feptxn);
        getTxData().setFeptxn(feptxn);
        if (getATMBusiness().getFeptxn() == null) {
            rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
            sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            isGoResponse = true;
            return; // GO TO  5    /* 組 ATM 回應電文 */
        }else {
            Feptxntcb tempFeptxntcb = getATMBusiness().checkFeptxntcbData(feptxn);
            feptxntcb = tempFeptxntcb;
            getATMBusiness().setFeptxntcb(feptxntcb);
            getTxData().setFeptxntcb(feptxntcb);
            if (getATMBusiness().getFeptxntcb() == null) {
                rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
                sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                isGoResponse = true;
                return; // GO TO  5    /* 組 ATM 回應電文 */
            }
        }
        
        String txCode = feptxn.getFeptxnTxCode().trim();
        String txrequestMessage = getTxData().getTxRequestMessage();
        String tmAscii = EbcdicConverter.fromHex(CCSID.English, this.getTxData().getTxRequestMessage());

        // 1.2 將ATM確認電文, 準備寫入原交易 FEPTXN欄位
        rtnCode = getATMBusiness().prepareConFEPTXN();
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GO TO  2    /* FEP檢核錯誤處理*/
        }

        // 1.3 交易確認電文檢核 MAC
        atmno = feptxn.getFeptxnAtmno();
        String FSCODE = this.getATMRequest().getFSCODE();
        if("FV".equals(FSCODE)) {
			needCheckMac = false;
			//GO TO  4    /* 更新交易記錄 */
			return ;
		}else if("I5".equals(FSCODE) ){
			needCheckMac = true;
			//GO TO  4    /* 更新交易記錄 */
			return ;
		}else {
			needCheckMac = true;
		}
        
        
        // CHANNEL = "EAT"，用ATMNO = "NEATM001"去押驗，在 CheckATMMACNew replace
        
        /* 檢核 ATM 電文 MAC */
		String PICCMACD = this.getATMRequest().getPICCMACD();
		if (StringUtils.isBlank(PICCMACD)) {
			this.rtnCode =  FEPReturnCode.ENCCheckMACError;
			return ; // GO TO  2    /* FEP檢核錯誤處理*/
		}
		
		String wkMAC;
		if ((StringUtils.equals("P1", txCode) && !StringUtils.equals("K 200", tmAscii.substring(179, 184)))
				|| StringUtils.equals("FC1", tmAscii.substring(17, 20))) {
            //磁條密碼變更&前置，ATM.REQ是轉ASCII後的位置
			int maxtxLength = txrequestMessage.length();
			int txLength = maxtxLength - 16;
			// 最後16碼轉 ASCII
			String ATMMAC = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(txrequestMessage, txLength));

			this.logContext.setRemark("Begin checkAtmMac mac:" + ATMMAC);
			logMessage(this.logContext);

			wkMAC = txrequestMessage.substring(36, txLength);// 從第36碼取到電文總長度-16

			this.rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno, wkMAC, ATMMAC);
			this.logContext.setRemark("after checkAtmMac RC:" + rtnCode.toString());
			logMessage(this.logContext);
		} else {
			String ATMMAC = PICCMACD;

			this.logContext.setRemark("Begin checkAtmMac mac:" + ATMMAC);
			logMessage(this.logContext);

			wkMAC = getATMBusiness().getAtmTxData().getTxRequestMessage().substring(36, 742);

			rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno, wkMAC, ATMMAC);
			this.logContext.setRemark("after checkAtmMac RC:" + rtnCode.toString());
			logMessage(this.logContext);
		}

		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO  2 /*  FEP檢核錯誤處理 */
		}

        // 1.4 磁條密碼轉換PINBLOCK上送主機
        ENCHelper encHelper = new ENCHelper(getTxData());
        if ("P1T".equals(feptxn.getFeptxnMsgid())) { //磁條密碼變更
            ENCKeyType keyType = ENCKeyType.S1; //1-DES
            String mode = "01";                 //IBM 3624

            // 取ASCII值(帳號10碼+序號2碼),ex: 999876512316301
            int card_maxLen = this.getATMRequest().getCARDPART1().length();
            String accno = "";
            if (card_maxLen >= 12) {
                int dl = card_maxLen - 12;
                accno = this.getATMRequest().getCARDPART1().substring(dl);
            }
            String atmSeqNo = StringUtils.rightPad(txrequestMessage.substring(64, 72).trim(), 16, "F0"); //用F0補滿16碼(右)

            String pin = this.getATMRequest().getPINCODE();
            RefString newPin = new RefString(null);

            this.logContext.setRemark("Fscode=" + getTxData().getFscode() + ",keyType:" + keyType + ",pin:" + pin + ",accno=" + accno + ",atmSeqNo=" + atmSeqNo + ",mode=" + mode);
            logMessage(this.logContext);

            rtnCode = encHelper.ConvertATMPinToIMSforP1(keyType, mode, atmno, atmSeqNo, accno, pin, newPin);

            if (rtnCode == FEPReturnCode.Normal) {
               feptxn.setFeptxnPinblock(newPin.get());
            }
        }
    }
    
	private void sendToCBS() throws Exception {
		String feptxnTxCode = StringUtils.trimToEmpty(feptxn.getFeptxnTxCode());
		String feptxnMsgid = StringUtils.trimToEmpty(feptxn.getFeptxnMsgid());
		// 磁條密碼變更交易CONFIRM時需送電文給IMS
		this.logContext.setProgramName(StringUtils.join(this.aaName, ".sendToCBS"));
		this.logContext.setRemark("FeptxnTxCode:" + feptxnTxCode + ", FeptxnMsgid:" + feptxnMsgid + ".");
		logMessage(this.logContext);
		if(StringUtils.equals("P1", feptxnTxCode) && StringUtils.equals("P1T", feptxnMsgid) 
				&& StringUtils.equals("A", feptxn.getFeptxnTxrust())) { // REQ成功才要上送主機
			this.logContext.setRemark("before CBS");
			logMessage(this.logContext);
			/* 交易前置處理查詢處理 */
			String AATxTYPE = "0"; // 上CBS查詢、檢核、申請、建置
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			feptxn.setFeptxnCbsTxCode(AA);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
			this.rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
			this.logContext.setProgramName(StringUtils.join(this.aaName, ".sendToCBS"));
			this.logContext.setRemark("after sendToCBS RC:" + rtnCode.toString());
			logMessage(this.logContext);
		}
	}

    /**
     * 4. UpdateTxData: 更新交易記錄(FEPTxn)
     */
	private void updateTxData() {
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response); // (RESPONSE)
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
     * 5. Response:組ATM回應電文 & 回 ATMMsgHandler
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
            atm_fsn_head2.setRECFMT("0"); //固定長度
            atm_fsn_head2.setMSGCAT("F");
            atm_fsn_head2.setMSGTYP("PC");
            atm_fsn_head2.setTRANDATE(atmReq.getTRANDATE()); 
            atm_fsn_head2.setTRANTIME(atmReq.getTRANTIME());
            atm_fsn_head2.setTRANSEQ(atmReq.getTRANSEQ());
            atm_fsn_head2.setTDRSEG(atmReq.getTDRSEG()); // 回覆FSN或FSE
            atm_fsn_head2.setPRCRDACT("0"); //第二趟固定放0
            if (feptxn == null) {
                atm_fsn_head2.setRECFMT("0");
            }

            if(needCheckMac) {
	            /* CALL ENC 取得MAC 資料 */
	            rfs.set("");
	            rtnMessage = atm_fsn_head2.makeMessage();
	            
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = atm_fsn_head2.getMSGCAT() + atm_fsn_head2.getMSGTYP() + atm_fsn_head2.getTRANDATE()
		    	 		+ atm_fsn_head2.getTRANTIME() + feptxn.getFeptxnStan()
		        		+ atmReq.getIPYDATA().substring(10, 18) +  atmReq.getTRANSEQ()
	            		+ atmReq.getTDRSEG() + "0000";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setProgramName(StringUtils.join(this.aaName, ".response"));
	            	this.logContext.setRemark("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	                logMessage(this.logContext);
	            }
	            
				rtnCode = new ENCHelper(getTxData()).makeAtmMacP3(atmno, rtnMessage, rfs);
				
				if (rtnCode != FEPReturnCode.Normal) {
					getLogContext().setRemark("after make Atm Mac P3 RC:" + rtnCode.toString());
					sendEMS(getLogContext());
					atm_fsn_head2.setMACCODE(""); /* 訊息押碼 */
				} else {
					atm_fsn_head2.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setRemark("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
            }
            
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
            RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();

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
