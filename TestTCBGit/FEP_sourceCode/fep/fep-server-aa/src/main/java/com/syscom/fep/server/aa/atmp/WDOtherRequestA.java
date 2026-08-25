package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B1PN;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B3PC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B3PN;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author vincent
 */
public class WDOtherRequestA extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;
	private String atmno;
	private String tita ;
	private boolean isGarbageR = false;
    private Integer fepfail = 0;		//預設FEP處理成功
    private Integer fiscfail = 0;		//預設FISC處理成功
    private Integer cbsfail = 0;		//預設CBS處理成功
    private Integer atmrepfpc = 1;		//預設下送ATM FPC

	public WDOtherRequestA(ATMData txnData) throws Exception {
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
			tita =EbcdicConverter.fromHex(CCSID.English, this.getATMtxData().getTxRequestMessage());
			getLogContext().setProgramFlowType(ProgramFlow.AAIn);
			getLogContext().setMessageFlowType(MessageFlow.Request);
			getLogContext().setProgramName(StringUtils.join(this.getATMtxData().getAaName(), ".processRequestData"));
			getLogContext().setMessage("ASCII TITA:"+tita);
			getLogContext().setRemark(StringUtils.join("Enter ", this.getATMtxData().getAaName()));
			logMessage(getLogContext());

			// 1. Prepare : 交易記錄初始資料
			rtnCode = getATMBusiness().prepareFEPTXN();
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
				getLogContext().setRemark("FEPTXN PREPARE ERROR");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			} 
			
			rtnCode = getATMBusiness().prepareFEPTXNTCB();
			if (this.rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
				getLogContext().setRemark("FEPTXNTCB PREPARE ERROR");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			}
			

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
				/* for 無卡提款 */
				if ("W2".equals(feptxn.getFeptxnTxCode())) {
					rtnCode = fiscBusiness.sendNCRequestToFISC(getATMRequest());
				} else {
					rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
				}
				
				if (rtnCode != FEPReturnCode.Normal) {
					fepfail = 1; // GO TO  7    /* 更新交易紀錄 */
				}
			}

			boolean repRcEq4001 = false;
			if (rtnCode == FEPReturnCode.Normal) {
				// 5. CheckResponseFromFISC:檢核回應電文是否正確
				rtnCode = fiscBusiness.checkResponseMessage();
				getLogContext().setProgramName(ProgramName + ".checkResponseMessage");
                getLogContext().setRemark("after checkResponseMessage RepRc:" + feptxn.getFeptxnRepRc());
        		logMessage(getLogContext());
        		
        		if(StringUtils.equals("4001", feptxn.getFeptxnRepRc())) {
        			repRcEq4001 = true;
        		}
                
                if (rtnCode != FEPReturnCode.Normal) {
                	fepfail = 1;
                }else if( !repRcEq4001) {
                	fiscfail = 1;
                }
			}

			if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
				// 6. SendToCBS/ASC(if need): 代理提款-進帳務主機掛現金帳
				this.sendToCBS();
			}

			// 7. 判斷交易狀態,更新交易紀錄(FEPTXN)
			this.updateTxData();
			//主機入扣帳逾時不回覆前端
			if(cbsfail == 1 && feptxn.getFeptxnCbsTimeout() == 1) {
				getLogContext().setProgramName(ProgramName + ".updateTxData");
				getLogContext().setRemark("CBS TIMEOUT.");
				sendEMS(getLogContext());
				return rtnMessage; // 回空字串，不回覆ATM
			}

			if(!isGarbageR) {
				// 8. 組ATM回應電文 & 回 ATMMsgHandler
	            rtnMessage = this.response();
			}else {
				//9. GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler
				rtnMessage = this.garbageResponse();
			}

		} catch (Exception ex) {
			rtnMessage = "";
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			throw ex;
		} finally {
	       	 getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
	         getATMtxData().getLogContext().setMessage("MessageToATM:" + rtnMessage);
	         getATMtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
	         getATMtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
	         getATMtxData().getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
			 logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode,getLogContext()));
	         logMessage(Level.DEBUG, getATMtxData().getLogContext());
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
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
			int insertCount2 = feptxnDao.insertSelective(this.feptxntcb); // 新增TCB資料
			if (insertCount <= 0 || insertCount2 <= 0) { // 新增失敗
				throw new Exception(); //2025.07.15 Transaction call review 調整
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) { // 新增失敗
			transactionManager.rollback(txStatus);
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
		atmno = feptxn.getFeptxnAtmno();
		this.rtnCode = checkRequestFromATM(getATMtxData());
        if (this.rtnCode != FEPReturnCode.Normal) {
        	fepfail = 1;
			return ;
		}
		
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		String ATM_REQ_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_REQ_PICCMACD)) {
			this.rtnCode = FEPReturnCode.ENCCheckMACError;
			fepfail = 1;
			return ;
		}

		getLogContext().setProgramName(StringUtils.join(this.aaName, ".checkBusinessRule"));
		getLogContext().setRemark("Begin checkAtmMacNew mac:" + ATM_REQ_PICCMACD);
		logMessage(getLogContext());
		this.rtnCode = new ENCHelper(getATMtxData())
				.checkAtmMacNew(atmno,StringUtils.substring(this.getATMtxData().getTxRequestMessage(), 36, 742), ATM_REQ_PICCMACD); // EBCDIC(36,742)
		getLogContext().setProgramName(StringUtils.join(this.aaName, ".checkBusinessRule"));
		getLogContext().setRemark("after checkAtmMacNew RC:" + this.rtnCode.toString());
		logMessage(getLogContext());
		if (this.rtnCode != FEPReturnCode.Normal) {
			fepfail = 1;
			return ;
		}
		
		//檢核跨行無卡提款的密碼
		if(StringUtils.equals(feptxn.getFeptxnTxCode(), "W2")) {
			RefString rfs = new RefString();
			ENCKeyType keyType = ENCKeyType.T3;
			String mode = "03";
			String pin = this.getATMRequest().getIPYDATA().substring(2, 18);
			String atmSeqNo = StringUtils.rightPad(this.getATMRequest().getTRANSEQ(), 8, "0") ; 
			atmSeqNo = EbcdicConverter.toHex(CCSID.English, atmSeqNo.length(), atmSeqNo);
			String accno = this.getATMRequest().getFADATA().substring(6, 18);
			rfs.set("");
			try {
				 // ATM PIN 轉換為IMS PIN
				this.rtnCode = new ENCHelper(getATMtxData()).ConvertATMPinToIMS(keyType, mode, atmno, atmSeqNo, accno, pin, rfs);
			} catch (Exception e) {
				getLogContext().setProgramException(e);
				sendEMS(getLogContext());
				this.rtnCode = ENCReturnCode.ENCPINBlockConvertError;
			}
			
			if(this.rtnCode  == FEPReturnCode.Normal) {
				getLogContext().setProgramName(StringUtils.join(this.aaName, ".checkBusinessRule"));
				getLogContext().setRemark("after Convert ATM Pin To IMS, new pin:" + rfs.get());
		        logMessage(getLogContext());
				feptxn.setFeptxnPinblock(rfs.get());
			}else {
				fepfail = 1;
				// GO TO  7       /* 更新交易紀錄 */
				return ;
			}
			
			
			rfs = new RefString();
			pin = feptxn.getFeptxnPinblock(); 
			try {
				this.rtnCode = new ENCHelper(getATMtxData().getFeptxn(), getATMtxData()).ConvertATMPinToFISC(pin, rfs);
			} catch (Exception e) {
				getLogContext().setProgramException(e);
				sendEMS(getLogContext());
				this.rtnCode = ENCReturnCode.ENCPINBlockConvertError;
			}
			
			if (this.rtnCode == FEPReturnCode.Normal) {
				getLogContext().setProgramName(StringUtils.join(this.aaName, ".checkBusinessRule"));
				getLogContext().setRemark("after Convert ATM Pin To FISC, new pin:" + rfs.get());
		        logMessage(getLogContext());
				feptxn.setFeptxnPinblock(rfs.get());
			}else {
				fepfail = 1;
				// GO TO  7       /* 更新交易紀錄 */
			}
		}
	}

	/**
	 * 6. SendToCBS/ASC(if need): 代理提款-進帳務主機掛現金帳
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		try {
			if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlCbsFlag())) {
				/* 進主機掛現金帳 */
				String AATxTYPE = "1"; // 上CBS入扣帳
				String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid();
				feptxn.setFeptxnCbsTxCode(AA);
				ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
				rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
				tota = hostAA.getTota();
				if (rtnCode != FEPReturnCode.Normal) {
					cbsfail = 1;    // CBS有誤
					if(feptxn.getFeptxnCbsTimeout() == 0) { // HostResponse無Timeout
						// 回前端主機的處理結果
						feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
					} else {
						// HostResponseTimeout
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
					}
				}
			}
		} catch (Exception ex) {
			cbsfail = 1;
			rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName);
			sendEMS(getLogContext());
		}
	}

	/**
	 * 7. 	判斷交易狀態,更新交易紀錄(FEPTXN)
	 */
	private void updateTxData() {
		if(StringUtils.isNoneBlank(feptxn.getFeptxnRepRc())) {
			feptxn.setFeptxnPending((short)2); /*解除 Pending*/
		}
		// ATM 4WAY，FISC 3WAY，ATM Confirm時才回CON 給財金
		if (fepfail == 0  &&  fiscfail == 0  &&  cbsfail == 0 && rtnCode == FEPReturnCode.Normal) {
			atmrepfpc = 0;
			feptxn.setFeptxnTxrust("B"); /*Pending*/
			feptxn.setFeptxnReplyCode(feptxn.getFeptxnRepRc()); /*回覆 ATM正常*/
			//跨行清算統計
			if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
				rtnCode2 = getFiscBusiness().processAptot(false);
				if (rtnCode2 != FEPReturnCode.Normal) {
					getLogContext().setProgramName(ProgramName + ".updateTxData.processAptot");
					getLogContext().setRemark("process Aptot ERROR");
					sendEMS(getLogContext());
				}
			}
		} else if (cbsfail == 1) { /*CBS 失敗*/
			feptxn.setFeptxnTxrust("R"); /*Accept-Reverse*/
			if(fiscfail == 0) { /* FISC成功*/
				if(feptxn.getFeptxnCbsTimeout() == 1) { //主機逾時不回覆財金
					feptxn.setFeptxnTxrust("B"); /*Pending*/
				}else {
					feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
					feptxn.setFeptxnConRc(feptxn.getFeptxnCbsRc());
					rtnCode3 = getFiscBusiness().sendConfirmToFISC();
					if (rtnCode3 != FEPReturnCode.Normal) {
						getLogContext().setProgramName(ProgramName + ".sendConfirmToFISC");
						getLogContext().setRemark("send Confirm To FISC ERROR");
						sendEMS(getLogContext());
					}
				}
			}
		} else if (fiscfail == 1) { /* FISC失敗*/
			feptxn.setFeptxnTxrust("R"); /* Reject-normal */
			feptxn.setFeptxnReplyCode(feptxn.getFeptxnRepRc());

		} else { /* 其他失敗 */
			feptxn.setFeptxnTxrust("S"); /*Reject-abnormal*/
			if (feptxn.getFeptxnFiscTimeout() != null ) {
				feptxn.setFeptxnTxrust("R"); /* Reject-normal*/
				if(feptxn.getFeptxnFiscTimeout() == 1) {
					//提款交易逾時回覆財金-Con，回覆前端逾時
					feptxn.setFeptxnConRc("0601");// 交易逾時
					feptxn.setFeptxnReplyCode(feptxn.getFeptxnConRc());
				} else { //財金0210電文檢核錯誤
					feptxn.setFeptxnConRc("0101");
				}
				rtnCode3 = getFiscBusiness().sendConfirmToFISC();

				if (rtnCode3 != FEPReturnCode.Normal) {
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode3.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
					getLogContext().setProgramName(ProgramName + ".sendConfirmToFISC");
					getLogContext().setRemark("send Confirm To FISC ERROR");
					sendEMS(getLogContext());
				}
			}

			if(StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
			}
		}
		feptxn.setFeptxnMsgflow("A2"); /* ATM Response */

		//更新FEPTXN
		if (rtnCode3 != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode3.getValue());
		} else if (rtnCode2 != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode2.getValue());
		} else if (rtnCode != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode.getValue());
		} else {
			feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		}
		feptxn.setFeptxnAaComplete((short) 1); /* AA Close */

		FEPReturnCode rtnCode4 = this.updateFeptxn();
		if (rtnCode4 != FEPReturnCode.Normal) {
			// 回寫檔案 (FEPTxn) 發生錯誤
			if(feptxn.getFeptxnAaRc() == FEPReturnCode.Normal.getValue()) {
				this.feptxn.setFeptxnReplyCode("T452");//FEPTXNUpdateError
				rtnCode = rtnCode4;
			}
			// ERROR MSG 送 EVENT MONITOR SYSTEM
			getLogContext().setProgramName(ProgramName + ".updateFeptxn");
			getLogContext().setRemark("FEPTXN UPDATE ERROR");
			sendEMS(getLogContext());
		}

		// 電文被主機視為garbage時(所有電文)，只傳送HEAD 給ATM
		String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
		String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());

		if ("XXXX".equals(IMSRC4_FISC) && "XXX".equals(IMSRC_TCB)) {
			isGarbageR = true;// GO TO 9 /*組GarbageResponse回覆ATM */
		}
	}

	/**
	 * 8. 組ATM回應電文 & 回 ATMMsgHandler
	 *
	 * @throws Exception
	 */
	private String response() throws Exception {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
			ATMGeneralRequest atmReq = this.getATMRequest();
			ENCHelper atmEncHelper = new ENCHelper(this.getATMtxData());
			RefString rfs = new RefString();
			if (atmrepfpc == 1) {
				switch (feptxn.getFeptxnTxCode()) {
				case "US": // 外幣提款交易
				case "JP": // 外幣提款交易
					ATM_FAA_CC1B3PC atm_faa_cc1b3pc = new ATM_FAA_CC1B3PC();
					// 組Header(OUTPUT-1)
					atm_faa_cc1b3pc.setWSID(atmReq.getWSID());
					atm_faa_cc1b3pc.setRECFMT("1");
					atm_faa_cc1b3pc.setMSGCAT("F");
					atm_faa_cc1b3pc.setMSGTYP("PC"); // - response
					atm_faa_cc1b3pc.setTRANDATE(atmReq.getTRANDATE()); // 西元後兩碼+系統月日共六碼
					atm_faa_cc1b3pc.setTRANTIME(atmReq.getTRANTIME()); // 系統時間
					atm_faa_cc1b3pc.setTRANSEQ(atmReq.getTRANSEQ());
					atm_faa_cc1b3pc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b3pc.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

					// 組D0(OUTPUT-2)畫面顯示(Display message)
					// 組 D0(004)
					atm_faa_cc1b3pc.setDATATYPE("D0");
					atm_faa_cc1b3pc.setDATALEN("004");
					atm_faa_cc1b3pc.setACKNOW("0");
					// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
					String pageNo;
					// 此欄給主機回應的代碼，尚未走到主機就給空值
					if (StringUtils.isBlank(feptxn.getFeptxnCbsRc())) { //交易尚未送主機
						pageNo = "226";
					} else {
						pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM, this.getATMtxData().getLogContext());
						if (StringUtils.equals(pageNo, "2999")) {
							pageNo = "226";
						}
					}
					atm_faa_cc1b3pc.setPAGENO(pageNo);

					// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
					atm_faa_cc1b3pc.setPTYPE("S0");
					atm_faa_cc1b3pc.setPLEN("215");
					atm_faa_cc1b3pc.setPBMPNO("010000"); // FPC
					// 西元年轉民國年
					atm_faa_cc1b3pc.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1b3pc.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_cc1b3pc.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1b3pc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					
					atm_faa_cc1b3pc.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1b3pc.setPSTAN(feptxn.getFeptxnStan());
					//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
					atm_faa_cc1b3pc.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
					// ATM回應代碼(空白放 "000") // [20221216]
                    atm_faa_cc1b3pc.setPRCCODE(feptxn.getFeptxnReplyCode());
					// 轉出行
					atm_faa_cc1b3pc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 取得原存行的促銷應用訊息
					atm_faa_cc1b3pc.setPARPC(feptxn.getFeptxnLuckyno());
					// 提領外幣
					atm_faa_cc1b3pc.setPEXRATE(Objects.toString(feptxn.getFeptxnExrate()));
					atm_faa_cc1b3pc.setPAMT(Objects.toString(feptxn.getFeptxnTxAmt()));
                    // 處理有收到CBS Response的欄位值
                    if (tota != null) {
                        //交易種類
                        atm_faa_cc1b3pc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                        //轉出帳號(明細表顯示內容)，他行序號:16位,第10~12位隱碼
                        String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
    					if(StringUtils.isNotBlank(FROMACT)) {
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 9) {
    						}else {
    							if(len_F > 12) {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3) + FROMACT.substring(12);
    							}else {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3);
    							}
    						}
    					}
    					atm_faa_cc1b3pc.setPACCNO(FROMACT);
    					//外幣提款表單編號
                        atm_faa_cc1b3pc.setPTMEXNO(this.getImsPropertiesValue(tota, ImsMethodName.FWDTMEX_NO.getValue()));
                    }else {
                    	//交易種類
                        atm_faa_cc1b3pc.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
                        //轉出帳號(明細表顯示內容)，他行序號:16位,第10~12位隱碼
                        String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
    					if(StringUtils.isNotBlank(FROMACT)) {
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 9) {
    						}else {
    							if(len_F > 12) {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3) + FROMACT.substring(12);
    							}else {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3);
    							}
    						}
    					}
    					atm_faa_cc1b3pc.setPACCNO(FROMACT);
                    }
                    rfs.set("");
                    rtnMessage = atm_faa_cc1b3pc.makeMessage();
					rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
                    if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_cc1b3pc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getATMtxData().getLogContext()));
                        atm_faa_cc1b3pc.setMACCODE(""); /* 訊息押碼 */
                    } else {
                        atm_faa_cc1b3pc.setMACCODE(rfs.get()); /* 訊息押碼 */
                    }
                    getLogContext().setProgramName(StringUtils.join(this.aaName, ".response"));
					getLogContext().setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
					logMessage(getLogContext());
                    rtnMessage = atm_faa_cc1b3pc.makeMessage();
					break;
				default: // 其他提款交易
					ATM_FAA_CC1APC atm_faa_cc1apc = new ATM_FAA_CC1APC();
					// 組Header(OUTPUT-1)
					atm_faa_cc1apc.setWSID(atmReq.getWSID());
					atm_faa_cc1apc.setRECFMT("1");
					atm_faa_cc1apc.setMSGCAT("F");
					atm_faa_cc1apc.setMSGTYP("PC"); // - response
					atm_faa_cc1apc.setTRANDATE(atmReq.getTRANDATE());  // 西元後兩碼+系統月日共六碼
					atm_faa_cc1apc.setTRANTIME(atmReq.getTRANTIME()); // 系統時間
					atm_faa_cc1apc.setTRANSEQ(atmReq.getTRANSEQ());
					atm_faa_cc1apc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1apc.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

					// 組D0(OUTPUT-2)畫面顯示(Display message)
					// 組 D0(004)
					atm_faa_cc1apc.setDATATYPE("D0");
					atm_faa_cc1apc.setDATALEN("004");
					atm_faa_cc1apc.setACKNOW("0");
					// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
					// 此欄給主機回應的代碼，尚未走到主機就給空值
					if (StringUtils.isBlank(feptxn.getFeptxnCbsRc())) { //交易尚未送主機
						pageNo = "226";
					} else {
						pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM, this.getATMtxData().getLogContext());
						if (StringUtils.equals(pageNo, "2999")) {
							pageNo = "226";
						}
					}
					atm_faa_cc1apc.setPAGENO(pageNo);

					// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
					atm_faa_cc1apc.setPTYPE("S0");
					atm_faa_cc1apc.setPLEN("191");
					atm_faa_cc1apc.setPBMPNO("010000"); // FPC
					// 西元年轉民國年
					atm_faa_cc1apc.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1apc.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_cc1apc.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1apc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));

					atm_faa_cc1apc.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1apc.setPSTAN(feptxn.getFeptxnStan());
					//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
					atm_faa_cc1apc.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
					// ATM回應代碼(空白放 "000") // [20221216]
                    atm_faa_cc1apc.setPRCCODE(feptxn.getFeptxnReplyCode());
					// 轉出行
					atm_faa_cc1apc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 取得原存行的促銷應用訊息
					atm_faa_cc1apc.setPARPC(feptxn.getFeptxnLuckyno());
                    // 處理有收到CBS Response的欄位值
                    if (tota != null) {
                        //交易種類
                        atm_faa_cc1apc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                        //轉出帳號(明細表顯示內容)，他行序號:16位,第10~12位隱碼
                        String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
    					if(StringUtils.isNotBlank(FROMACT)) {
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 9) {
    						}else {
    							if(len_F > 12) {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3) + FROMACT.substring(12);
    							}else {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3);
    							}
    						}
    					}
    					atm_faa_cc1apc.setPACCNO(FROMACT);
                    }else {
                    	//交易種類
                    	atm_faa_cc1apc.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
                        //轉出帳號(明細表顯示內容)，他行序號:16位,第10~12位隱碼
                        String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
    					if(StringUtils.isNotBlank(FROMACT)) {
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 9) {
    						}else {
    							if(len_F > 12) {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3) + FROMACT.substring(12);
    							}else {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3);
    							}
    						}
    					}
    					atm_faa_cc1apc.setPACCNO(FROMACT);
                    }
                    rfs.set("");
                    rtnMessage = atm_faa_cc1apc.makeMessage();
                    rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
                    if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_cc1apc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getATMtxData().getLogContext()));
                        atm_faa_cc1apc.setMACCODE(""); /* 訊息押碼 */
                    } else {
                        atm_faa_cc1apc.setMACCODE(rfs.get()); /* 訊息押碼 */
                    }
                    getLogContext().setProgramName(StringUtils.join(this.aaName, ".response"));
					getLogContext().setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
					logMessage(getLogContext());
                    rtnMessage = atm_faa_cc1apc.makeMessage();
					break;
				}
			} else {
				switch (feptxn.getFeptxnTxCode()) {
				case "US": // 外幣提款交易
				case "JP": // 外幣提款交易
					ATM_FAA_CC1B3PN atm_faa_cc1b3pn = new ATM_FAA_CC1B3PN();
					// 組Header(OUTPUT-1)
					atm_faa_cc1b3pn.setWSID(atmReq.getWSID());
					atm_faa_cc1b3pn.setRECFMT("1");
					atm_faa_cc1b3pn.setMSGCAT("F");
					atm_faa_cc1b3pn.setMSGTYP("PN"); // + response
					atm_faa_cc1b3pn.setTRANDATE(atmReq.getTRANDATE());  // 西元後兩碼+系統月日共六碼
					atm_faa_cc1b3pn.setTRANTIME(atmReq.getTRANTIME()); // 系統時間
					atm_faa_cc1b3pn.setTRANSEQ(atmReq.getTRANSEQ());
					atm_faa_cc1b3pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b3pn.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

					// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
					atm_faa_cc1b3pn.setPTYPE("S0");
					atm_faa_cc1b3pn.setPLEN("215");
					atm_faa_cc1b3pn.setPBMPNO("000010"); // FPN
					// 西元年轉民國年
					atm_faa_cc1b3pn.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1b3pn.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_cc1b3pn.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1b3pn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					// 格式:$999 ex :$0
					atm_faa_cc1b3pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"));
					//帳戶餘額
					BigDecimal feptxnBalb = feptxn.getFeptxnBalb();
	                // 格式 :正值放$,負值放-,$99,999,999,999.00(共18位)
					if (feptxnBalb.compareTo(BigDecimal.ZERO) >= 0) {
						atm_faa_cc1b3pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "$#,##0.00"),18," "));
	                } else {
	                	atm_faa_cc1b3pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "-#,##0.00"),18," "));
	                }
					atm_faa_cc1b3pn.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1b3pn.setPSTAN(feptxn.getFeptxnStan());
					//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
					atm_faa_cc1b3pn.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
                    atm_faa_cc1b3pn.setPRCCODE(feptxn.getFeptxnReplyCode());
					// 轉出行
					atm_faa_cc1b3pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 取得原存行的促銷應用訊息
					atm_faa_cc1b3pn.setPARPC(feptxn.getFeptxnLuckyno());
					// 提領外幣
					atm_faa_cc1b3pn.setPEXRATE(Objects.toString(feptxn.getFeptxnExrate()));
					atm_faa_cc1b3pn.setPAMT(Objects.toString(feptxn.getFeptxnTxAmt()));
                    // 處理有收到CBS Response的欄位值
                    if (tota != null) {
                        //交易種類
                        atm_faa_cc1b3pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                        //轉出帳號(明細表顯示內容)，他行序號:16位,第10~12位隱碼
                        String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
    					if(StringUtils.isNotBlank(FROMACT)) {
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 9) {
    						}else {
    							if(len_F > 12) {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3) + FROMACT.substring(12);
    							}else {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3);
    							}
    						}
    					}
    					atm_faa_cc1b3pn.setPACCNO(FROMACT);
    					//外幣提款表單編號
                        atm_faa_cc1b3pn.setPTMEXNO(this.getImsPropertiesValue(tota, ImsMethodName.FWDTMEX_NO.getValue()));
                    }else {
                    	//交易種類
                    	atm_faa_cc1b3pn.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
                        //轉出帳號(明細表顯示內容)，他行序號:16位,第10~12位隱碼
                        String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
    					if(StringUtils.isNotBlank(FROMACT)) {
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 9) {
    						}else {
    							if(len_F > 12) {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3) + FROMACT.substring(12);
    							}else {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3);
    							}
    						}
    					}
    					atm_faa_cc1b3pn.setPACCNO(FROMACT);
                    }
                    rfs.set("");
                    rtnMessage = atm_faa_cc1b3pn.makeMessage();
                    rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
                    if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_cc1b3pn.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getATMtxData().getLogContext()));
                        atm_faa_cc1b3pn.setMACCODE(""); /* 訊息押碼 */
                    } else {
                        atm_faa_cc1b3pn.setMACCODE(rfs.get()); /* 訊息押碼 */
                    }
                    getLogContext().setProgramName(StringUtils.join(this.aaName, ".response"));
					getLogContext().setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
					logMessage(getLogContext());
                    rtnMessage = atm_faa_cc1b3pn.makeMessage();
					break;
				default: // 其他提款交易
					ATM_FAA_CC1B1PN atm_faa_cc1b1pn = new ATM_FAA_CC1B1PN();
					// 組Header(OUTPUT-1)
					atm_faa_cc1b1pn.setWSID(atmReq.getWSID());
					atm_faa_cc1b1pn.setRECFMT("1");
					atm_faa_cc1b1pn.setMSGCAT("F");
					atm_faa_cc1b1pn.setMSGTYP("PN"); // + response
					atm_faa_cc1b1pn.setTRANDATE(atmReq.getTRANDATE());  // 西元後兩碼+系統月日共六碼
					atm_faa_cc1b1pn.setTRANTIME(atmReq.getTRANTIME()); // 系統時間
					atm_faa_cc1b1pn.setTRANSEQ(atmReq.getTRANSEQ());
					atm_faa_cc1b1pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b1pn.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

					// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
					atm_faa_cc1b1pn.setPTYPE("S0");
					atm_faa_cc1b1pn.setPLEN("191");
					atm_faa_cc1b1pn.setPBMPNO("000010"); // FPN
					// 西元年轉民國年
					atm_faa_cc1b1pn.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1b1pn.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_cc1b1pn.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1b1pn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					// 格式:$999 ex :$0
					atm_faa_cc1b1pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"));
					//帳戶餘額
	                // 格式 :正值放$,負值放-,$99,999,999,999.00(共18位) // 已確認是單純只總長度，後面轉字串會補滿18位，這裡不用處理
					feptxnBalb = feptxn.getFeptxnBalb();
	                // 格式 :正值放$,負值放-,$99,999,999,999.00(共18位)
					if (feptxnBalb.compareTo(BigDecimal.ZERO) >= 0) {
						atm_faa_cc1b1pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "$#,##0.00"),18," "));
	                } else {
	                	atm_faa_cc1b1pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "-#,##0.00"),18," "));
	                }
					atm_faa_cc1b1pn.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1b1pn.setPSTAN(feptxn.getFeptxnStan());
					//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
					atm_faa_cc1b1pn.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
					// ATM回應代碼(空白放 "000") // [20221216]
                    atm_faa_cc1b1pn.setPRCCODE(feptxn.getFeptxnReplyCode());
					// 轉出行
					atm_faa_cc1b1pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 取得原存行的促銷應用訊息
					atm_faa_cc1b1pn.setPARPC(feptxn.getFeptxnLuckyno());
                    // 處理有收到CBS Response的欄位值
                    if (tota != null) {
                        //交易種類
                        atm_faa_cc1b1pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                        //轉出帳號(明細表顯示內容)，他行序號:16位,第10~12位隱碼
                        String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
    					if(StringUtils.isNotBlank(FROMACT)) {
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 9) {
    						}else {
    							if(len_F > 12) {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3) + FROMACT.substring(12);
    							}else {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3);
    							}
    						}
    					}
    					atm_faa_cc1b1pn.setPACCNO(FROMACT);
                    }else {
                    	//交易種類
                    	atm_faa_cc1b1pn.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
                        //轉出帳號(明細表顯示內容)，他行序號:16位,第10~12位隱碼
                        String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
    					if(StringUtils.isNotBlank(FROMACT)) {
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 9) {
    						}else {
    							if(len_F > 12) {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3) + FROMACT.substring(12);
    							}else {
    								FROMACT = FROMACT.substring(0, 9) + StringUtils.repeat("*", 3);
    							}
    						}
    					}
    					atm_faa_cc1b1pn.setPACCNO(FROMACT);
                    }
                    rfs.set("");
                    rtnMessage = atm_faa_cc1b1pn.makeMessage();
                    rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
                    if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_cc1b1pn.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getATMtxData().getLogContext()));
                        atm_faa_cc1b1pn.setMACCODE(""); /* 訊息押碼 */
                    } else {
                        atm_faa_cc1b1pn.setMACCODE(rfs.get()); /* 訊息押碼 */
                    }
                    getLogContext().setProgramName(StringUtils.join(this.aaName, ".response"));
					getLogContext().setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
					logMessage(getLogContext());
                    rtnMessage = atm_faa_cc1b1pn.makeMessage();
					break;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}
	
	//9. 	GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler
	private String garbageResponse() {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
            // 組 Header
            ATMGeneralRequest atmReq = this.getATMRequest();
            
            ATM_FSN_HEAD2 atm_fsn_head2 = new ATM_FSN_HEAD2();
            atm_fsn_head2.setWSID(atmReq.getWSID());
            atm_fsn_head2.setRECFMT("1");
            atm_fsn_head2.setMSGCAT("F");
            atm_fsn_head2.setMSGTYP("PC");
            atm_fsn_head2.setTRANDATE(atmReq.getTRANDATE());
            atm_fsn_head2.setTRANTIME(atmReq.getTRANTIME());
            atm_fsn_head2.setTRANSEQ(atmReq.getTRANSEQ());
            atm_fsn_head2.setTDRSEG(atmReq.getTDRSEG()); // 回覆FSN或FSE
            // PRCRDACT = 0 或4都是未留置卡片, 2 是吃卡, 只有磁條密碼變更交易
            // (FC1、P1)主機才有可能依據狀況要求吃卡
            atm_fsn_head2.setPRCRDACT("0");

            /* CALL ENC 取得MAC 資料 */
            ENCHelper atmEncHelper = new ENCHelper(this.getATMtxData());
            RefString rfs = new RefString();
            
            rfs.set("");
            rtnMessage = atm_fsn_head2.makeMessage();
			rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
			
			if (rtnCode != FEPReturnCode.Normal) {
				atm_fsn_head2.setMACCODE(""); /* 訊息押碼 */
			} else {
				atm_fsn_head2.setMACCODE(rfs.get()); /* 訊息押碼 */
			}
			
			getLogContext().setProgramName(ProgramName + ".garbageResponse");
            getLogContext().setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
            logMessage(getLogContext());
            
            rtnMessage = atm_fsn_head2.makeMessage();
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
		String month = StringUtils.substring(date, dateLength - 4, dateLength - 2);
		String day = StringUtils.substring(date, dateLength - 2);
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
	
	/**
	 * 更新feptxn
	 */
	private FEPReturnCode updateFeptxn() {
		FEPReturnCode rtn = FEPReturnCode.Normal;
		try {
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateFeptxn"));
			feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
		} catch (Exception ex) {
			rtn = FEPReturnCode.FEPTXNUpdateError;
		}
		return rtn;
	}
}
