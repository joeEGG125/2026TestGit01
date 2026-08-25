package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.MobileAdapter;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.communication.ToSendQueryAccountCommu;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B1PN;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN;
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
public class TRSelfIssue extends ATMPAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private String atmno;
	private boolean isGarbageR = false;

	public TRSelfIssue(ATMData txnData) throws Exception {
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
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage("ASCII TITA:" + EbcdicConverter.fromHex(CCSID.English, this.getTxData().getTxRequestMessage()));
            this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(this.logContext);
            
			// 1. Prepare():記錄MessageText & 準備回覆電文資料
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
				// 2. AddTxData: 新增交易記錄(FEPTxn)
				this.addTxData(); // 新增交易記錄(FEPTxn)
				if (this.rtnCode != FEPReturnCode.Normal) {
					// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
					getLogContext().setProgramName(ProgramName + ".AddTxData");
					getLogContext().setRemark("FEPTXN ADD ERROR");
					sendEMS(getLogContext());
					return rtnMessage; // RETUEN 空字串，不回覆ATM
				}
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 3. CheckBusinessRule: 商業邏輯檢核
				this.checkBusinessRule();
			}

			//4. 	FEP檢核錯誤處理
			if (rtnCode != FEPReturnCode.Normal) {
				setFeptxnErrorCode(FEPChannel.FEP, FEPChannel.ATM);
			}
			
			if (rtnCode == FEPReturnCode.Normal) {
				// 5. SendToCBS:送往CBS主機處理
				this.sendToCBS();
			}

			// 6. UpdateTxData: 更新交易記錄(FEPTxn)
			this.updateTxData();
			if (feptxn.getFeptxnCbsTimeout() != null && feptxn.getFeptxnCbsTimeout() == 1) {  // HostResponseTimeout
				getLogContext().setProgramName(ProgramName + ".updateTxData");
				getLogContext().setRemark("HostResponseTimeout");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			}

		} catch (Exception ex) {
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		}

		try {
			// 6.Response:組ATM回應電文 & 回 ATMMsgHandler
			if(!isGarbageR) {
				if (StringUtils.isBlank(getTxData().getTxResponseMessage())) {
					rtnMessage = this.response();
					if ("EAT".equals(feptxn.getFeptxnChannel())) {
						rtnMessage = this.eatmResponse(rtnMessage);
					}
				} else {
					rtnMessage = getTxData().getTxResponseMessage();
				}
			}else {
				rtnMessage = this.garbageResponse();
				if ("EAT".equals(feptxn.getFeptxnChannel())) {
					rtnMessage = this.eatmGarbageResponse(rtnMessage);
				}
			}
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage("MessageToATM:" + rtnMessage);
			getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode,getLogContext()));
			logMessage(Level.DEBUG, this.logContext);
		} catch (Exception ex) {
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
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
		// 3.1 檢核 ATM 電文
		rtnCode = getATMBusiness().CheckATMData();
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO 4    /* FEP檢核錯誤處理*/
		}

		// 3.2 檢核ATM電文訊息押碼(MAC)
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		String ATM_TITA_PICCMACD = getTxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError; /* MAC Error */
			return; // GO TO 4    /* FEP檢核錯誤處理*/
		}

		String ATMMAC = ATM_TITA_PICCMACD;
		this.logContext.setRemark("Begin checkAtmMac mac:" + ATMMAC);
		logMessage(this.logContext);
		
		// CHANNEL = "EAT"，用ATMNO = "NEATM001"去押驗，在 CheckATMMACNew replace
		
		rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno, StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 36, 742), ATMMAC);
        
		this.logContext.setRemark("after checkAtmMac RC:" + rtnCode.toString());
        logMessage(this.logContext);
        
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO 4    /* FEP檢核錯誤處理*/
		}
		
		//3.3	檢核ATM電文訊息押碼(MAC)
		String TaCode = getTxData().getTxObject().getRequest().getTACODE();
        if("TM".equals(TaCode)) {
        	//手機門號轉帳向手機門號平台查詢=> 回覆入帳行及戶名
			this.logContext.setRemark("Enter into MobileAdapter");
			logMessage(this.logContext);
			try {
				ToSendQueryAccountCommu request = new ToSendQueryAccountCommu();
				// 1.身份辨識欄：放 "AT" + 轉出帳號後8位 (數字)
				String ascText = EbcdicConverter.fromHex(CCSID.English,
						getTxData().getTxRequestMessage().substring(342, 358));
				request.setIdNo("AT" + ascText);
				// 2.手機門號
				// 2025-07-10 Richard modified start for [Cleartext Submission of Sensitive Information]
				// request.setMp(feptxn.getFeptxnTelephone());
				RefString ref = new RefString();
				RefBase.set(ref, feptxn, Feptxn::getFeptxnTelephone);
				request.setMp(ref.get());
				// 2025-07-10 Richard modified end for [Cleartext Submission of Sensitive Information]
				// 3.bankCode 不用給值
				request.setBankCode("");

				MobileAdapter mobileAdapter = new MobileAdapter(getTxData());
				mobileAdapter.setToSendQueryAccountCommu(request);
				rtnCode = mobileAdapter.sendReceive();
				if (rtnCode != FEPReturnCode.Normal) {
					feptxn.setFeptxnReplyCode(FEPReturnCode.toString(rtnCode));
					this.logContext.setRemark("FEPTXN_REPLYCODE:" + FEPReturnCode.toString(rtnCode));
					rtnCode = FEPReturnCode.MobileAPIError;
				}
				this.logContext.setRemark(StringUtils.join("into MobileAdapter rtnCode ", rtnCode.toString()));
				logMessage(this.logContext);
			} catch (Exception e) {
				getLogContext().setProgramException(e);
				getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
				sendEMS(getLogContext());
				rtnCode = FEPReturnCode.ProgramException;
			}
        }
	}

	/**
	 * 5. SendToCBS:送往CBS主機處理
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		/* 交易記帳處理 */
		String AATxTYPE = "1"; // 上CBS入扣帳
		String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
		feptxn.setFeptxnCbsTxCode(AA);
		ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
		rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
		tota = hostAA.getTota();
        if(feptxn.getFeptxnCbsTimeout() == 1) { // HostResponse Timeout
			// HostResponseTimeout
			feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
		} else {
			// 回前端主機的處理結果
        	feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
		}
        feptxn.setFeptxnAaRc(rtnCode.getValue());
	}

	/**
	 * 6. UpdateTxData: 更新交易記錄(FEPTxn)
	 */
	private void updateTxData() {
		feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); // (RESPONSE)
		feptxn.setFeptxnAaComplete((short) 1); /* AA Complete */
		/* For報表, 寫入處理結果 */
		if (rtnCode == FEPReturnCode.Normal) {
			feptxn.setFeptxnTxrust("A"); /* 處理結果=成功 */
		} else {
			feptxn.setFeptxnTxrust("R"); /* 處理結果=Reject */
		}

		// 回寫 FEPTXN
		FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
		try {
			feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
		} catch (Exception ex) {
			rtnCode2 = FEPReturnCode.FEPTXNUpdateError;
		}

		if (rtnCode2 != FEPReturnCode.Normal) {
			// 回寫檔案 (FEPTxn) 發生錯誤
			if(rtnCode == FEPReturnCode.Normal) {
        		this.feptxn.setFeptxnReplyCode("T452");//FEPTXNUpdateError
        		rtnCode = rtnCode2;
        	}
			getLogContext().setProgramName(ProgramName + ".updateTxData");
            getLogContext().setRemark("FEPTXN UPDATE ERROR");
			sendEMS(getLogContext());
		}
		
		// 電文被主機視為garbage時(所有電文)，只傳送HEAD 給ATM
		String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
		String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());

		if ("XXXX".equals(IMSRC4_FISC) && "XXX".equals(IMSRC_TCB)) {
			isGarbageR = true;// GO TO 8 /*組GarbageResponse回覆ATM */
		}
	}

	/**
	 * 7. Response:組ATM回應電文 & 回 ATMMsgHandler
	 *
	 * @throws Exception
	 */
	private String response() {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
			ATMGeneralRequest atmReq = this.getATMRequest();
			ENCHelper atmEncHelper = new ENCHelper(this.getTxData());
			RefString rfs = new RefString();
			if (rtnCode != FEPReturnCode.Normal) {
				ATM_FAA_CC1APC atm_faa_cc1apc = new ATM_FAA_CC1APC();
				// 組Header(OUTPUT-1)
				atm_faa_cc1apc.setWSID(atmReq.getWSID());
				atm_faa_cc1apc.setRECFMT("1");
				atm_faa_cc1apc.setMSGCAT("F");
				atm_faa_cc1apc.setMSGTYP("PC"); // - response
				atm_faa_cc1apc.setTRANDATE(atmReq.getTRANDATE());
				atm_faa_cc1apc.setTRANTIME(atmReq.getTRANTIME());
				atm_faa_cc1apc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				atm_faa_cc1apc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_cc1apc.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

				// 組D0(OUTPUT-2)畫面顯示(Display message)
				// 組 D0(004)
				atm_faa_cc1apc.setDATATYPE("D0");
				atm_faa_cc1apc.setDATALEN("004");
				atm_faa_cc1apc.setACKNOW("0");
				
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
				// CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後碼,ex :11/06/29)
				atm_faa_cc1apc.setPBUSINESSDATE(this.dateStrToYYMMDD(feptxn.getFeptxnTbsdy()));
				// 轉入行
				atm_faa_cc1apc.setPITXBKNO(feptxn.getFeptxnTrinBkno());

				//ATM回應代碼(CBSProcess已依主機下送規則處理)
				atm_faa_cc1apc.setPRCCODE(feptxn.getFeptxnReplyCode());
				
				// 轉出行
				atm_faa_cc1apc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					atm_faa_cc1apc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)，合庫帳號13位,第7~9位隱碼 
					String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
					if(StringUtils.isNotBlank(FROMACT)) {
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
					}
					atm_faa_cc1apc.setPACCNO(FROMACT);
					//轉入帳號(明細表顯示內容)
					atm_faa_cc1apc.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
					//促銷應用訊息
					atm_faa_cc1apc.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
				}else {
					//交易種類
					atm_faa_cc1apc.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
					//轉出帳號(明細表顯示內容)，合庫帳號13位,第7~9位隱碼 
					String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
					if(StringUtils.isNotBlank(FROMACT)) {
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
					}
					atm_faa_cc1apc.setPACCNO(FROMACT);
					//轉入帳號(明細表顯示內容)
					atm_faa_cc1apc.setPTRINACCT(feptxn.getFeptxnTrinActno());
				}
				rfs.set("");
				rtnMessage = atm_faa_cc1apc.makeMessage();
				
				if (!"ATM".equals(feptxn.getFeptxnChannel())) {
					rtnMessage = atm_faa_cc1apc.getMSGCAT() + atm_faa_cc1apc.getMSGTYP() + atm_faa_cc1apc.getTRANDATE()
							+ atm_faa_cc1apc.getTRANTIME() + feptxn.getFeptxnStan()
							+ atmReq.getIPYDATA().substring(10, 18) + atmReq.getTRANSEQ() + atmReq.getTDRSEG()
							+ StringUtils.rightPad(feptxn.getFeptxnReplyCode(), 4, " ");
					rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
					this.logContext.setProgramName(StringUtils.join(this.aaName, ".response"));
					this.logContext.setRemark("New inputData");
					this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel() + ", new inputData:" + rtnMessage);
					logMessage(this.logContext);
				}
				
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					atm_faa_cc1apc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
							getTxData().getLogContext()));
					atm_faa_cc1apc.setMACCODE(""); /* 訊息押碼 */
				} else {
					atm_faa_cc1apc.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setRemark("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
				rtnMessage = atm_faa_cc1apc.makeMessage();
			} else {
				ATM_FAA_CC1B1PN atm_faa_cc1b1pn = new ATM_FAA_CC1B1PN();
				// 組Header(OUTPUT-1)
				atm_faa_cc1b1pn.setWSID(atmReq.getWSID());
				atm_faa_cc1b1pn.setRECFMT("1");
				atm_faa_cc1b1pn.setMSGCAT("F");
				atm_faa_cc1b1pn.setMSGTYP("PN"); // + response
				atm_faa_cc1b1pn.setTRANDATE(atmReq.getTRANDATE());
				atm_faa_cc1b1pn.setTRANTIME(atmReq.getTRANTIME());
				atm_faa_cc1b1pn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				atm_faa_cc1b1pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_cc1b1pn.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

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
				atm_faa_cc1b1pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));

				//格式 :正值放$,負值放-,$99,999,999,999.00(共18位)右靠左補空白
				BigDecimal feptxnBalb = feptxn.getFeptxnBalb();
                if (feptxnBalb.compareTo(BigDecimal.ZERO) >= 0) {
                	atm_faa_cc1b1pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "$#,##0.00"),18," "));
                } else {
                	atm_faa_cc1b1pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "-#,##0.00"),18," "));
                }
				atm_faa_cc1b1pn.setPATXBKNO(feptxn.getFeptxnBkno());
				atm_faa_cc1b1pn.setPSTAN(feptxn.getFeptxnStan());
				// CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後碼,ex :11/06/29)
				atm_faa_cc1b1pn.setPBUSINESSDATE(this.dateStrToYYMMDD(feptxn.getFeptxnTbsdy()));
				// 轉入行
				atm_faa_cc1b1pn.setPITXBKNO(feptxn.getFeptxnTrinBkno());
				
				atm_faa_cc1b1pn.setPRCCODE(feptxn.getFeptxnReplyCode());
				
				// 轉出行
				atm_faa_cc1b1pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());

				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					atm_faa_cc1b1pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)，合庫帳號13位,第7~9位隱碼 
					String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
					if(StringUtils.isNotBlank(FROMACT)) {
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
					}
					atm_faa_cc1b1pn.setPACCNO(FROMACT);
					//轉入帳號(明細表顯示內容)
					atm_faa_cc1b1pn.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
					//促銷應用訊息
					atm_faa_cc1b1pn.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
				}else {
					//交易種類
					atm_faa_cc1b1pn.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
					//轉出帳號(明細表顯示內容)，合庫帳號13位,第7~9位隱碼 
					String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
					if(StringUtils.isNotBlank(FROMACT)) {
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
					}
					atm_faa_cc1b1pn.setPACCNO(FROMACT);
					//轉入帳號(明細表顯示內容)
					atm_faa_cc1b1pn.setPTRINACCT(feptxn.getFeptxnTrinActno());
				}
				rfs.set("");
				rtnMessage = atm_faa_cc1b1pn.makeMessage();
				
				if (!"ATM".equals(feptxn.getFeptxnChannel())) {
					rtnMessage = atm_faa_cc1b1pn.getMSGCAT() + atm_faa_cc1b1pn.getMSGTYP() + atm_faa_cc1b1pn.getTRANDATE()
							+ atm_faa_cc1b1pn.getTRANTIME() + feptxn.getFeptxnStan()
							+ atmReq.getIPYDATA().substring(10, 18) + atmReq.getTRANSEQ() + atmReq.getTDRSEG()
							+ StringUtils.rightPad(feptxn.getFeptxnReplyCode(), 4, " ");
					rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
					this.logContext.setProgramName(StringUtils.join(this.aaName, ".response"));
					this.logContext.setRemark("New inputData");
					this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel() + ", new inputData:" + rtnMessage);
					logMessage(this.logContext);
				}
				
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					atm_faa_cc1b1pn.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
							getTxData().getLogContext()));
					atm_faa_cc1b1pn.setMACCODE(""); /* 訊息押碼 */
				} else {
					atm_faa_cc1b1pn.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setRemark("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
				rtnMessage = atm_faa_cc1b1pn.makeMessage();
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}
	
	/**
	 * 6. Response:組EATM回應電文
	 * 
	 * @throws Exception
	 */
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
				header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
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
				header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
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
            atm_fsn_head2.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
            atm_fsn_head2.setTDRSEG(atmReq.getTDRSEG()); // 回覆FSN或FSE
            // PRCRDACT = 0 或4都是未留置卡片, 2 是吃卡, 只有磁條密碼變更交易
            // (FC1、P1)主機才有可能依據狀況要求吃卡
            atm_fsn_head2.setPRCRDACT("0");

            /* CALL ENC 取得MAC 資料 */
            ENCHelper atmEncHelper = new ENCHelper(this.getTxData());
            RefString rfs = new RefString();
            
            rfs.set("");
            rtnMessage = atm_fsn_head2.makeMessage();
            
			if (!"ATM".equals(feptxn.getFeptxnChannel())) {
				rtnMessage = atm_fsn_head2.getMSGCAT() + atm_fsn_head2.getMSGTYP() + atm_fsn_head2.getTRANDATE()
						+ atm_fsn_head2.getTRANTIME() + feptxn.getFeptxnStan()
						+ atmReq.getIPYDATA().substring(10, 18) + atmReq.getTRANSEQ() + atmReq.getTDRSEG()
						+ StringUtils.rightPad(feptxn.getFeptxnReplyCode(), 4, " ");
				rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
				this.logContext.setProgramName(StringUtils.join(this.aaName, ".response"));
				this.logContext.setRemark("New inputData");
				this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel() + ", new inputData:" + rtnMessage);
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
	
	private String eatmGarbageResponse(String outdata) throws Exception {
		String rtnMessage = "" ;
		try {
			/* 組 ATM Response OUT-TEXT */
			RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
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
			header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
			header.setSEVERITY("Garbage");
			msgbody.setOUTDATA(outdata);
			rtnMessage = XmlUtil.toXML(rs);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "" ;
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
	
	private void setFeptxnErrorCode(FEPChannel channel_1, FEPChannel channel_2) {
		setFeptxnErrorCode(Objects.toString(rtnCode.getValue()), channel_1, channel_2);
	}
	
	private void setFeptxnErrorCode(String errCode, FEPChannel channel_1, FEPChannel channel_2) {
		if (feptxn.getFeptxnReplyCode() == null) {
			feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(errCode, channel_1, channel_2, getTxData().getLogContext()));
		}
		feptxn.setFeptxnAaRc(rtnCode.getValue());
	}
}
