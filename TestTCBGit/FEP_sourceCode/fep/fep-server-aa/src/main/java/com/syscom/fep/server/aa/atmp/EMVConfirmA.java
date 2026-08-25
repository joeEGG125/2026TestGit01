package com.syscom.fep.server.aa.atmp;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.mapper.IntltxnMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;

public class EMVConfirmA extends INBKAABase {
	private String rtnMessage = "";
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
	private boolean check_rtnCode = false;
	private IntltxnMapper intltxnMapper = SpringBeanFactoryUtil.getBean(IntltxnMapper.class);
	private String AATxTYPE = "";
    private String atmno;

	/**
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 *
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 */
	public EMVConfirmA(ATMData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		try {
	        // 記錄FEPLOG內容
			getLogContext().setProgramFlowType(ProgramFlow.AAIn);
    		getLogContext().setMessageFlowType(MessageFlow.Confirmation);
    		getLogContext().setProgramName(StringUtils.join(this.getATMtxData().getAaName(), ".processRequestData"));
    		getLogContext().setMessage("ASCII TITA:" + EbcdicConverter.fromHex(CCSID.English, this.getATMtxData().getTxRequestMessage()));
    		getLogContext().setRemark(StringUtils.join("Enter ", this.getATMtxData().getAaName()));
    		logMessage(getLogContext());
	        
			getFiscBusiness().getFISCTxData().setFiscTeleType(FISCSubSystem.EMVIC);
			// 3. 商業邏輯檢核
			_rtnCode = checkBusinessRule();

			// 4. 更新交易記錄(FEPTXN/INTLTXN)
			updateTxData();

			if (!StringUtils.equals(feptxn.getFeptxnPcode().substring(3, 4), "1") ) {
				// 5. SendToCBS
				this.sendToCBS();
				
				// 6. 送Confirm 電文至 FISC
				if ((feptxn.getFeptxnAccType() == 1 || feptxn.getFeptxnAccType() == 2)
						&& getATMtxData().getMsgCtl().getMsgctlFisc2way() == 0) { //3WAY
					if (StringUtils.equals(getATMRequest().getMSGTYP(), "SE")) {
						if(StringUtils.equals(getATMRequest().getPIARPCRC(), "E")) {
							feptxn.setFeptxnConRc("8120");  // IC 卡拒絕交易
						} else {
							feptxn.setFeptxnConRc(feptxn.getFeptxnCbsRc());
						}
						// 組 -con 電文給財金
						_rtnCode = getFiscBusiness().sendConfirmToFISCEMV(); // 送Confirm 電文至 FISC
						if(_rtnCode != FEPReturnCode.Normal){
							feptxn.setFeptxnAaRc(_rtnCode.getValue());
						}
					} else if(StringUtils.isNotBlank(feptxn.getFeptxnCbsRc())) {
						feptxn.setFeptxnConRc(feptxn.getFeptxnCbsRc());
						// 組 -con 電文給財金
						_rtnCode = getFiscBusiness().sendConfirmToFISCEMV(); // 送Confirm 電文至 FISC
						if(_rtnCode != FEPReturnCode.Normal){
							feptxn.setFeptxnAaRc(_rtnCode.getValue());
						}
					}
				}
			}

			// 7.更新交易紀錄
			this.updateFEPTXN();

			// 8. 組回應電文回給ATM
			this.rtnMessage = prepareResponseData();
			
			// 9. 交易通知(if need)
			getATMBusiness().sendToNotify();

			// 10. 交易結束通知主機(By PCODE)
			if( "A".equals(feptxn.getFeptxnTxrust()) && StringUtils.equals(feptxn.getFeptxnPcode().substring(0, 2), "26")
					&& !StringUtils.equals(feptxn.getFeptxnPcode().substring(3, 4), "1")){ // 提款類交易，成功才發結束通知
				AATxTYPE = "";
				String AATxRs = "N";
				String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid1();
				ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getATMtxData());
				_rtnCode = new CBS(hostAA, this.getATMtxData()).sendToCBS(AATxTYPE, AATxRs);
			}
		} catch (Exception ex) {
			this.rtnMessage = "";
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
			sendEMS(getLogContext());
		}

		try {
			if (check_rtnCode) {
				getLogContext().setProgramFlowType(ProgramFlow.AAOut);
				getLogContext().setMessage(this.rtnMessage);
				getLogContext().setProgramName(this.aaName);
				getLogContext().setMessageFlowType(MessageFlow.ResponseConfirmation);
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
				logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
				logMessage(Level.DEBUG, getLogContext());
			} else {
				getLogContext().setProgramFlowType(ProgramFlow.AAIn);
				getLogContext().setMessage(this.rtnMessage);
				getLogContext().setProgramName(this.aaName);
				getLogContext().setMessageFlowType(MessageFlow.ResponseConfirmation);
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
				logMessage(Level.DEBUG, getLogContext());
			}
		} catch (Exception ex) {
			this.rtnMessage = "";
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
			sendEMS(getLogContext());
		}

		return rtnMessage;
	}

	private void sendToCBS() throws Exception {
		/* 提款交易依ATM如發異常電文, 才送主機沖正或註記(I002電文) */
		AATxTYPE =""; //預設
		 //ATM第二道為失敗電文且FEP紀錄CBS已記帳
		if(getATMRequest().getMSGTYP().equals("SE") && DbHelper.toBoolean(feptxn.getFeptxnAccType())){
			AATxTYPE ="2"; //上CBS沖正
		}
		if(StringUtils.isNotBlank(AATxTYPE)){
			String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid();
			feptxn.setFeptxnCbsTxCode(AA);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
			_rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
			if(_rtnCode != FEPReturnCode.Normal){
				feptxn.setFeptxnAaRc(_rtnCode.getValue());
			}
		}
		// 主機確定沖正，沖轉APTOT
		if(feptxn.getFeptxnAccType() == 2 && getATMtxData().getMsgCtl().getMsgctlUpdateAptot() == 1) {
			/*沖轉跨行代收付*/
			_rtnCode = getFiscBusiness().processAptot(true);
			if(_rtnCode != FEPReturnCode.Normal){
				feptxn.setFeptxnAaRc(_rtnCode.getValue());
			}
		}

		/* 晶片驗證失敗ATM CON(-), 轉成 RC:‘8120’ 回給財金 */
		if(StringUtils.equals(getATMRequest().getMSGTYP(), "SE")
				&& StringUtils.equals(getATMRequest().getPITK2FRM(), "C")
				&& StringUtils.equals(getATMRequest().getCARDFMT(), "E")
				&& Integer.parseInt(getATMRequest().getPIARQCLN(), 16) > 0) {
			/* for EMV VISA卡(2620&2622) CON(-) BIT#60 */
			String feptxnPcode = feptxn.getFeptxnPcode();
			if (StringUtils.equals(feptxnPcode, "2620") && StringUtils.equals(feptxnPcode, "2622")) {
				String tk3 = getFiscBusiness().MAKEICCHECKDATA(getATMRequest().getPIARQCLN(), getATMRequest().getPIARQC());
				getLogContext().setProgramName(ProgramName + ".sendToCBS");
				getLogContext().setRemark("after MAKEICCHECKDATA, Trk3:" + tk3);
				logMessage(getLogContext());
				feptxn.setFeptxnTrk3(tk3);
			}
		}
	}

	/**
	 * 商業邏輯檢核
	 *
	 * @return
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode _rtnCode = FEPReturnCode.Normal;
		try {
			// (1) 檢核原交易帳號
			Feptxn tempFeptxn = getATMBusiness().checkConData();
			feptxn = tempFeptxn;
			getATMBusiness().setFeptxn(feptxn);
			getATMtxData().setFeptxn(feptxn);
			getFiscBusiness().setFeptxn(feptxn);

			if (feptxn == null) {
				/* 查無原交易 */
				// 將ERROR MSG送 EMS
				sendEMS(getLogContext());
				// GOTO STEP 5: 組回應電文回給 ATM
				return FEPReturnCode.OriginalMessageNotFound;
			}else {
	        	Feptxntcb tempFeptxntcb = getATMBusiness().checkFeptxntcbData(feptxn);
	            feptxntcb = tempFeptxntcb;
	            getATMBusiness().setFeptxntcb(feptxntcb);
	            getATMtxData().setFeptxntcb(feptxntcb);
	            getFiscBusiness().setFeptxntcb(feptxntcb);
	            if (getATMBusiness().getFeptxntcb() == null) {
	            	_rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
	                getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
	                getLogContext().setRemark("Feptxntcb is null");
	                sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
	                return _rtnCode; // GO TO 8 /* 組 ATM 回應電文 */
	            }
	        }
			
			atmno = feptxn.getFeptxnAtmno();
			this.check_rtnCode = true;
			// (2) 更新 FEPTXN
			_rtnCode = getATMBusiness().prepareConFEPTXN();
			if (_rtnCode != FEPReturnCode.Normal) {
				return _rtnCode;
			}

			// (3) 檢核 ATM Confirm MAC(if need)
			/* 因Confirm MAC error 需繼續執行其他步驟,故存入不同 RC */
			String PICCMACD = getATMRequest().getPICCMACD();
			if (StringUtils.isBlank(PICCMACD)) {
				getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
				getLogContext().setRemark("ATM_REQ_PICCMACD is empty.");
				logMessage(getLogContext());
				_rtnCode = FEPReturnCode.ENCCheckMACError;
			}
			
			getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
    		getLogContext().setRemark("Begin checkAtmMacNew mac:" + PICCMACD);
            logMessage(getLogContext());
            
			_rtnCode2 = new ENCHelper(getATMtxData()).checkAtmMacNew(atmno,
						StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 36, 750), PICCMACD); // EBCDIC(36,750)
			
			getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
			getLogContext().setRemark("after checkAtmMacNew RC:" + _rtnCode2.toString());
			logMessage(getLogContext());
			
			return _rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 更新交易記錄(FEPTXN/INTLTXN)
	 *
	 * @return
	 */
	private void updateTxData() {
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response);
			if (_rtnCode != FEPReturnCode.Normal) {
				feptxn.setFeptxnAaRc(_rtnCode.getValue());
			} else if (_rtnCode2 != FEPReturnCode.Normal) {
				feptxn.setFeptxnAaRc(_rtnCode2.getValue());
			}
			int updateCount = feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
			int updateCount2 = feptxnDao.updateByPrimaryKeySelective(this.feptxntcb);
            if (updateCount <= 0 || updateCount2 <= 0) { // 更新失敗
            	throw new Exception(); //2025.07.15 Transaction call review 調整
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "updateTxData");
			sendEMS(getLogContext());
		}
	}

	/**
	 * 組回應電文回給 ATM
	 *
	 * @return
	 */
	private String prepareResponseData() {
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
			_rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
			if (_rtnCode != FEPReturnCode.Normal) {
				atm_fsn_head2.setMACCODE(""); /* 訊息押碼 */
			} else {
				atm_fsn_head2.setMACCODE(rfs.get()); /* 訊息押碼 */
			}
			rtnMessage = atm_fsn_head2.makeMessage();
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}

	/**
	 * 更新 FEPTXN
	 *
	 * @return
	 * @throws Exception
	 */
    private void updateFEPTXN() {
    	/*Ignore ATM MAC ERROR*/
		// 2026.02.02 zonghao 調整setFeptxnTxrust()判斷
		if (StringUtils.equals(feptxn.getFeptxnPcode().substring(3, 4), "1")) {
			if (_rtnCode == FEPReturnCode.Normal && "B".equals(feptxn.getFeptxnTxrust())) {
				//餘額查詢無關帳務&不須回覆財金，FEP檢核成功即是成功
				feptxn.setFeptxnTxrust("A");  /*成功*/
			}
		} else if (feptxn.getFeptxnAccType() == 1){
			feptxn.setFeptxnTxrust("A");  /*成功*/
		} else if (feptxn.getFeptxnAccType() == 2) {
			feptxn.setFeptxnTxrust("C");  /*沖正*/
    	}
    	feptxn.setFeptxnPending((short) 2);  //取消 PENDING
    	feptxn.setFeptxnAaComplete((short)1); /*AA close*/
    	
		try {
			this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
		} catch (Exception ex) { 
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateFEPTXN");
			sendEMS(getLogContext());
			_rtnCode = FEPReturnCode.FEPTXNUpdateError;
		}
    	
		if (_rtnCode != FEPReturnCode.Normal) {
			getLogContext().setRemark("after update FEPTXN RC:" + _rtnCode.toString());
			sendEMS(getLogContext()); 
		}
		
		/* 更新 INTLTXN */
		Intltxn reqIntltxn = intltxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
		if (reqIntltxn != null) {
			reqIntltxn.setIntltxnConRc(feptxn.getFeptxnConRc());
			reqIntltxn.setIntltxnTxrust(feptxn.getFeptxnTxrust());
			reqIntltxn.setIntltxnTxDate(feptxn.getFeptxnTxDate());
			reqIntltxn.setIntltxnEjfno(feptxn.getFeptxnEjfno());
			
			int intltxnRes = intltxnMapper.updateByPrimaryKeySelective(reqIntltxn);
			if (intltxnRes < 1) {// return 不為正代表失敗
				getLogContext().setRemark("update Intltxn fail.");
				sendEMS(getLogContext()); 
			}
		}
    }

}
