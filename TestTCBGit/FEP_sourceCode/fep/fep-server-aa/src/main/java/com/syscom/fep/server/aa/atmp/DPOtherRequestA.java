package com.syscom.fep.server.aa.atmp;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
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
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;

/**
 * 負責處理 ATM 發動跨行存款交易電文
 *
 * @author mickey
 */
public class DPOtherRequestA extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;
	private String atmno;
	private boolean isGarbageR = false;
    private Integer fepfail = 0;		//預設FEP處理成功
    private Integer fiscfail = 0;		//預設FISC處理成功
    private Integer cbsfail = 0;		//預設CBS處理成功
    private Integer cbsqryfail = 0;		//預設CBS查詢處理成功
    private Integer atmrepfpc = 1; 		//預設下送ATM FPC

	public DPOtherRequestA(ATMData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * AA進入點主程式
	 */
	@Override
	public String processRequestData() throws Exception {
		String rtnMessage = "";
		// 記錄FEPLOG內容
		getLogContext().setProgramFlowType(ProgramFlow.AAIn);
		getLogContext().setMessageFlowType(MessageFlow.Request);
		getLogContext().setProgramName(StringUtils.join(this.getATMtxData().getAaName(), ".processRequestData"));
		getLogContext().setMessage("ASCII TITA:" + EbcdicConverter.fromHex(CCSID.English, this.getATMtxData().getTxRequestMessage()));
		getLogContext().setRemark(StringUtils.join("Enter ", this.getATMtxData().getAaName()));
		logMessage(getLogContext());
		try {
			// 1. Prepare : 交易記錄初始資料
			rtnCode = getATMBusiness().prepareFEPTXN();
			if (this.rtnCode != FEPReturnCode.Normal) {
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

			if (rtnCode == FEPReturnCode.Normal) {
				// 4. SendToCBS/ASC(if need): 本行卡-進帳務主機查詢帳號
				this.SendToCBS();
			}

			FISC fiscBusiness = getFiscBusiness();
			if (rtnCode == FEPReturnCode.Normal) {
				// 5. 組送往 FISC 之 Request 電文並等待財金之 Response
				rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
				if (rtnCode != FEPReturnCode.Normal) {
					 fepfail = 1;      
					 //GO TO  8    /* 更新交易紀錄 */
				}
			}

			boolean repRcEq4001 = false;
			if (rtnCode == FEPReturnCode.Normal) {
				// 6. CheckResponseFromFISC:檢核回應電文是否正確
				rtnCode = fiscBusiness.checkResponseMessage();
				getLogContext().setProgramName(ProgramName + ".checkResponseMessage");
				getLogContext().setRemark("after checkResponseMessage RepRc:" + feptxn.getFeptxnRepRc());
				logMessage(getLogContext());

				if (StringUtils.equals("4001", feptxn.getFeptxnRepRc())) {
					repRcEq4001 = true;
				}
				if (rtnCode != FEPReturnCode.Normal) {
					fepfail = 1;
				}else if(!repRcEq4001) {
					fiscfail = 1;
				}
			}

			if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
				// 7. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
				this.SendToCBS2();
			}
			
			// 8. 判斷是否需組 CON 電文回財金
			this.sendToConfirm();
			//主機入扣帳逾時不回覆前端
			if(cbsfail == 1 && feptxn.getFeptxnCbsTimeout() == 1) {
				getLogContext().setProgramName(ProgramName + ".sendToConfirm");
				getLogContext().setRemark("CBS TIMEOUT.");
				sendEMS(getLogContext());
				return rtnMessage; // 回空字串，不回覆ATM
			}

			// 9. label_END_OF_FUNC: 組ATM回應電文 & 回 ATMMsgHandler
			if (!isGarbageR) {
				// 9. 組ATM回應電文 & 回 ATMMsgHandler
				rtnMessage = this.response();
			} else {
				rtnMessage = this.garbageResponse();
	       	}
			
		} catch (Exception ex) {
			rtnMessage = "";
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
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
	 * 2. AddTxData: 新增交易記錄(FEPTXN)
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
		// 3.1 檢核ATM電文
		rtnCode = checkRequestFromATM(getATMtxData());
		if (rtnCode != FEPReturnCode.Normal) {
			fepfail = 1;       // FEP有誤
			return; // GOTO  8      /* 更新交易紀錄 */
		}
		
		// 3.2 檢核ATM電文訊息押碼(MAC)
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		String ATM_REQ_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_REQ_PICCMACD)) {
			fepfail = 1;       // FEP有誤
			rtnCode = FEPReturnCode.ENCCheckMACError;
			return; // GO TO 8      /* 更新交易紀錄 */
		}
		
	    getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
		getLogContext().setRemark("Begin checkAtmMac mac:" + ATM_REQ_PICCMACD);
		
        logMessage(getLogContext());
		rtnCode = new ENCHelper(getATMtxData()).checkAtmMacNew(atmno, StringUtils.substring(getATMtxData().getTxRequestMessage(), 36,742), ATM_REQ_PICCMACD); // EBCDIC(36,742)
		
		getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
		getLogContext().setRemark("after checkAtmMac RC:" + rtnCode.toString());
		logMessage(getLogContext());
		
		if (rtnCode != FEPReturnCode.Normal) {
			fepfail = 1; // FEP有誤
			return; // GOTO 8      /* 更新交易紀錄 */
		}
		
		// 3.3 檢核單筆限額
		
		//發卡行為本行時，改檢核本行卡跨行存款限額
		String feptxnTxCode = feptxn.getFeptxnTxCode();
		String cardData = StringUtils.substring(this.getATMRequest().getCARDDATA(), 13, 16);
		if(StringUtils.equals("DA", feptxnTxCode)  && StringUtils.equals(cardData, SysStatus.getPropertyValue().getSysstatHbkno())) {
			getATMtxData().getMsgCtl().setMsgctlCheckLimit((short)9);
		}
		
		rtnCode = getATMBusiness().checkTransLimit(getATMtxData().getMsgCtl());
		if (rtnCode != FEPReturnCode.Normal) {
			fepfail = 1; // FEP有誤
			return; // GOTO 8      /* 更新交易紀錄 */
		}
	}

	/**
	 * 4. SendToCBS/ASC(if need): 本一律送主機檢核
	 *
	 * @throws Exception
	 */
	private void SendToCBS() throws Exception {
		try {
			// 本行卡,先送CBS查詢帳號
			feptxn.setFeptxnTxrust("S"); // Reject-abnormal
			String AATxTYPE = "0"; // 上CBS查詢、檢核
			String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid();
			feptxn.setFeptxnCbsTxCode(AA);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
			rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
			tota = hostAA.getTota();
			if (rtnCode != FEPReturnCode.Normal) {
				cbsqryfail = 1;
				if (feptxn.getFeptxnCbsTimeout() == 0) { // HostResponse無Timeout
					// 回前端主機的處理結果
					feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
				} else {
					// HostResponseTimeout
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
				}
			}
		} catch (Exception ex) {
			cbsqryfail = 1;
			rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName);
			sendEMS(getLogContext());
		}
	}

	/**
	 * 7. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
	 *
	 * @throws Exception
	 */
	private void SendToCBS2() throws Exception {
		try {
			/* 進主機入扣帳/手續費 */
			String AATxTYPE = "1"; // 上CBS入扣帳
			String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid();
			feptxn.setFeptxnCbsTxCode(AA);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
			rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
			tota = hostAA.getTota();
	        if(rtnCode != FEPReturnCode.Normal){
	        	cbsfail = 1;
	        	if(feptxn.getFeptxnCbsTimeout() == 0) { // HostResponse無Timeout
					// 回前端主機的處理結果
	        		feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
				} else {
					// HostResponseTimeout
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
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
	 * 9. 組ATM回應電文 & 回 ATMMsgHandler
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
				ATM_FAA_CC1APC msgbody = new ATM_FAA_CC1APC();
				msgbody.setWSID(atmReq.getWSID());
				msgbody.setRECFMT("1");
				msgbody.setMSGCAT("F");
				msgbody.setMSGTYP("PC"); // - response
				msgbody.setTRANDATE(atmReq.getTRANDATE());
				msgbody.setTRANTIME(atmReq.getTRANTIME());
				msgbody.setTRANSEQ(atmReq.getTRANSEQ());
				msgbody.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				msgbody.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”
				// 組D0(OUTPUT-2)畫面顯示(Display message)
				// 組 D0(004)
				msgbody.setDATATYPE("D0");
				msgbody.setDATALEN("004");
				msgbody.setACKNOW("0");
				
				String pageNo;
                //此欄給主機回應的代碼，尚未走到主機就給空值
                if (StringUtils.isBlank(feptxn.getFeptxnCbsRc())) { //交易尚未送主機
					pageNo = "226";
				} else {
					pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM, this.getTxData().getLogContext());
					if (StringUtils.equals(pageNo, "2999")) {
						pageNo = "226";
					}
				}
                
				msgbody.setPAGENO(pageNo);
				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				msgbody.setPTYPE("S0");
				msgbody.setPLEN("191");
				msgbody.setPBMPNO("010000"); // FPC
				// 西元年轉民國年，格式：YYY/MM/DD
				msgbody.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
				// 時間格式：HH :MM :SS
				msgbody.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
				msgbody.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				msgbody.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmt(), "$#,##0"));
				//代理行
				msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
				//交易序號
				msgbody.setPSTAN(feptxn.getFeptxnStan());
				// CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
				msgbody.setPBUSINESSDATE(this.dateStrToYYMMDD(feptxn.getFeptxnTbsdy()));
				// ATM回應代碼
				msgbody.setPRCCODE(feptxn.getFeptxnReplyCode());
				//取得原存行的促銷應用訊息
				msgbody.setPARPC(feptxn.getFeptxnLuckyno());
				if (tota != null) {
					//交易種類
					msgbody.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//存款(轉入)行
					msgbody.setPOTXBKNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue()));
					//卡片帳號(明細表顯示內容)
					msgbody.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
					//卡片帳號行
					msgbody.setPITXBKNO(this.getImsPropertiesValue(tota, ImsMethodName.TOBANK.getValue()));
					//存款(轉入)帳號(明細表顯示內容)
					String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
                	if(StringUtils.isNotBlank(FROMACT)) {
                		if(StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
                			//合庫帳號13位,第7~9位隱碼
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 6) {
    						}else {
    							if(len_F > 9) {
    								FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
    							}else {
    								FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
    							}
    						}
                		}else {
                			//他行帳號:16位,第10~12位隱碼，
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
                	}
					msgbody.setPACCNO(FROMACT);
				}else {
					//交易種類
					msgbody.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
					//存款(轉入)行
					msgbody.setPOTXBKNO(feptxn.getFeptxnTrinBkno());
					//卡片帳號行
					msgbody.setPITXBKNO(feptxn.getFeptxnTroutBkno());
					//卡片帳號(明細表顯示內容)
					msgbody.setPTRINACCT(feptxn.getFeptxnTrinActno());
					//存款(轉入)帳號(明細表顯示內容)
					String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
                	if(StringUtils.isNotBlank(FROMACT)) {
                		if(StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
                			//合庫帳號13位,第7~9位隱碼
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 6) {
    						}else {
    							if(len_F > 9) {
    								FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
    							}else {
    								FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
    							}
    						}
                		}else {
                			//他行帳號:16位,第10~12位隱碼，
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
                	}
					msgbody.setPACCNO(FROMACT);
				}
				rfs.set("");
				rtnMessage = msgbody.makeMessage();
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				getLogContext().setProgramName(ProgramName + ".Response");
				getLogContext().setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
	            logMessage(getLogContext());
	            
				if (rtnCode != FEPReturnCode.Normal) {
					msgbody.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
							getATMtxData().getLogContext()));
					msgbody.setMACCODE(""); /* 訊息押碼 */
				} else {
					msgbody.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				rtnMessage = msgbody.makeMessage();
			} else {
				ATM_FAA_CC1B1PN msgbody = new ATM_FAA_CC1B1PN();
				msgbody.setWSID(atmReq.getWSID());
				msgbody.setRECFMT("1");
				msgbody.setMSGCAT("F");
				msgbody.setMSGTYP("PN"); // + response
				msgbody.setTRANDATE(atmReq.getTRANDATE());
				msgbody.setTRANTIME(atmReq.getTRANTIME());
				msgbody.setTRANSEQ(atmReq.getTRANSEQ());
				msgbody.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				msgbody.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”
				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				msgbody.setPTYPE("S0");
				msgbody.setPLEN("191");
				msgbody.setPBMPNO("000010"); // FPN
				// 西元年轉民國年，格式：YYY/MM/DD
				msgbody.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
				// 時間格式：HH :MM :SS
				msgbody.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
				msgbody.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				msgbody.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
				// 格式:$999 ex :$0
				msgbody.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"));
				//代理行
				msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
				//交易序號
				msgbody.setPSTAN(feptxn.getFeptxnStan());
				// CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
				msgbody.setPBUSINESSDATE(this.dateStrToYYMMDD(feptxn.getFeptxnTbsdy()));
				// ATM回應代碼
				msgbody.setPRCCODE(feptxn.getFeptxnReplyCode());
				//取得原存行的促銷應用訊息
				msgbody.setPARPC(feptxn.getFeptxnLuckyno());
				if (tota != null) {
					//交易種類
					msgbody.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//存款(轉入)行
					msgbody.setPOTXBKNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue()));
					//卡片帳號行
					msgbody.setPITXBKNO(this.getImsPropertiesValue(tota, ImsMethodName.TOBANK.getValue()));
					//卡片帳號(明細表顯示內容)
					msgbody.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
					//存款(轉入)帳號(明細表顯示內容)
					String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
                	if(StringUtils.isNotBlank(FROMACT)) {
                		if(StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
                			//合庫帳號13位,第7~9位隱碼
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 6) {
    						}else {
    							if(len_F > 9) {
    								FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
    							}else {
    								FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
    							}
    						}
                		}else {
                			//他行帳號:16位,第10~12位隱碼，
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
                	}
					msgbody.setPACCNO(FROMACT);
				}else {
					//交易種類
					msgbody.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
					//存款(轉入)行
					msgbody.setPOTXBKNO(feptxn.getFeptxnTrinBkno());
					//卡片帳號行
					msgbody.setPITXBKNO(feptxn.getFeptxnTroutBkno());
					//卡片帳號(明細表顯示內容)
					msgbody.setPTRINACCT(feptxn.getFeptxnTrinActno());
					//存款(轉入)帳號(明細表顯示內容)
					String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
                	if(StringUtils.isNotBlank(FROMACT)) {
                		if(StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
                			//合庫帳號13位,第7~9位隱碼
    						FROMACT = FROMACT.trim();
    						int len_F = FROMACT.length();
    						if(len_F <= 6) {
    						}else {
    							if(len_F > 9) {
    								FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
    							}else {
    								FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
    							}
    						}
                		}else {
                			//他行帳號:16位,第10~12位隱碼，
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
                	}
					msgbody.setPACCNO(FROMACT);
				}
				rfs.set("");
				rtnMessage = msgbody.makeMessage();
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				getLogContext().setProgramName(ProgramName + ".Response");
				getLogContext().setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
	            logMessage(getLogContext());
	            
				if (rtnCode != FEPReturnCode.Normal) {
					msgbody.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
					msgbody.setMACCODE(""); /* 訊息押碼 */
				} else {
					msgbody.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				rtnMessage = msgbody.makeMessage();
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			throw ex;
		}
		return rtnMessage;
	}

	/**
	 * 8. 判斷是否需組 CON 電文回財金
	 */
	private void sendToConfirm() {
		getLogContext().setProgramName(ProgramName + ".sendToConfirm");
		getLogContext().setRemark("fepfail:" + fepfail + ", cbsqryfail:" + cbsqryfail +", fiscfail:" + fiscfail+ ", cbsfail:" + cbsfail);
    	logMessage(getLogContext());
        String feptxnRepRc = feptxn.getFeptxnRepRc();
		if(StringUtils.isNoneBlank(feptxnRepRc)) {
			feptxn.setFeptxnPending((short)2); /*解除 Pending*/
		}
        if (fepfail == 0 && cbsqryfail == 0  && fiscfail == 0  &&  cbsfail == 0 && rtnCode == FEPReturnCode.Normal) {
        	atmrepfpc = 0;
			/* +REP */
            feptxn.setFeptxnReplyCode(feptxnRepRc); // 回覆 ATM正常
            feptxn.setFeptxnTxrust("A"); /* 交易成功 */
			if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
				rtnCode2 = getFiscBusiness().processAptot(false);
				if (rtnCode2 != FEPReturnCode.Normal) {
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode2.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
					getLogContext().setProgramName(ProgramName + ".processAptot");
					getLogContext().setRemark("process Aptot ERROR");
					sendEMS(getLogContext());
				}
            }

			/* 回覆財金+CON */
			feptxn.setFeptxnConRc("4001");
			if(getATMtxData().getMsgCtl().getMsgctlFisc2way() == 0) {
				/* 組 CON 電文送財金 */
				rtnCode3 = getFiscBusiness().sendConfirmToFISC();
				if (rtnCode3 != FEPReturnCode.Normal) {
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode3.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
					getLogContext().setProgramName(ProgramName + ".sendConfirmToFISC");
					getLogContext().setRemark("send Confirm To FISC ERROR");
					sendEMS(getLogContext());
				}
			} else { //for 測試pending交易的控制
				feptxn.setFeptxnWay((short)3); // 配合人工補confirm 條件
			}

        } else if (cbsfail == 1) { /*CBS 失敗*/
            feptxn.setFeptxnTxrust("R"); /*Accept-Reverse*/
            if(fiscfail == 0) {
            	if (feptxn.getFeptxnCbsTimeout() == 1) {
            		//主機逾時不回覆財金
					feptxn.setFeptxnTxrust("B"); /*Pending*/
            	} else {
					feptxn.setFeptxnConRc(feptxn.getFeptxnCbsRc());
					feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
                    rtnCode3 = getFiscBusiness().sendConfirmToFISC();
                    if (rtnCode3 != FEPReturnCode.Normal) {
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode3.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
						getLogContext().setProgramName(ProgramName + ".sendConfirmToFISC");
        				getLogContext().setRemark("send Confirm To FISC ERROR");
        				sendEMS(getLogContext());
        			}
                }
			}else {
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
			}
        } else if (fiscfail == 1) { /* FISC失敗*/
        	feptxn.setFeptxnTxrust("R"); /* Reject-normal */
            feptxn.setFeptxnReplyCode(feptxn.getFeptxnRepRc());
        } else { /*Reject-abnormal*/
			if (feptxn.getFeptxnFiscTimeout() != null ) {
				feptxn.setFeptxnTxrust("R"); /* Reject-normal */
				//回覆財金-Con
				if(feptxn.getFeptxnFiscTimeout() == 1) {
					feptxn.setFeptxnConRc("0601");// 交易逾時
				}else {
					feptxn.setFeptxnConRc("0501");// 端末機故障
				}

				feptxn.setFeptxnReplyCode(feptxn.getFeptxnConRc());
				rtnCode3 = getFiscBusiness().sendConfirmToFISC();
				if (rtnCode3 != FEPReturnCode.Normal) {
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode3.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
					getLogContext().setProgramName(ProgramName + ".sendConfirmToFISC");
					getLogContext().setRemark("send Confirm To FISC ERROR");
					sendEMS(getLogContext());
				}
			}else {
				feptxn.setFeptxnTxrust("S"); /* Reject-normal */
			}

        	if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
	        	feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
        	}
        }
        feptxn.setFeptxnMsgflow("A2"); /* ATM Response */

		if(rtnCode3 != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode3.getValue());
		}else if(rtnCode2 != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode2.getValue());
		}else if(rtnCode != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode.getValue());
		}else {
			feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		}
        feptxn.setFeptxnAaComplete((short)1);
        FEPReturnCode rtnCode4 = this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
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
        // 電文被主機視為garbage時(所有電文)，只傳送HEAD 給前端
		String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
		String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());

		if ("XXXX".equals(IMSRC4_FISC) && "XXX".equals(IMSRC_TCB)) {
			isGarbageR = true;// GO TO 10 /*組GarbageResponse回覆ATM */
		}
	}
	
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
			throw ex;
		}
		return rtnMessage;
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
			feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
		} catch (Exception ex) {
			rtn = FEPReturnCode.FEPTXNUpdateError;
		}
		return rtn;
	}
}
