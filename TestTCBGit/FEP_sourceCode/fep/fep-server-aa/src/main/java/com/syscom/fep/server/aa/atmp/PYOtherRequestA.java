package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B2PC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B2PN;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2;
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
public class PYOtherRequestA extends INBKAABase {
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

	public PYOtherRequestA(ATMData txnData) throws Exception {
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
            getLogContext().setProgramFlowType(ProgramFlow.AAIn);
            getLogContext().setMessageFlowType(MessageFlow.Request);
            getLogContext().setProgramName(StringUtils.join(this.getATMtxData().getAaName(), ".processRequestData"));
            getLogContext().setMessage("ASCII TITA:" + EbcdicConverter.fromHex(CCSID.English, this.getATMtxData().getTxRequestMessage()));
            getLogContext().setRemark(StringUtils.join("Enter ", this.getATMtxData().getAaName()));
            logMessage(getLogContext());
            
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
				// 4. SendToCBS/ASC(if need): 本行轉入-進帳務主機查詢帳號
				this.sendToCBS();
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

			// 7. SendToCBS/ASC(if need): 代理提款-進帳務主機掛現金帳
			if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
				this.sendToCBS2();
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

			if (!isGarbageR) {
				// 9. 組ATM回應電文 & 回 ATMMsgHandler
				rtnMessage = this.response();
				if ("EAT".equals(feptxn.getFeptxnChannel())) {
					rtnMessage = this.eatmResponse(rtnMessage);
				}
			} else {
				rtnMessage = this.garbageResponse();
				if ("EAT".equals(feptxn.getFeptxnChannel())) {
					rtnMessage = this.eatmGarbageResponse(rtnMessage);
				}
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
		
	    getLogContext().setProgramName(ProgramName + ".updateTxData");
		getLogContext().setRemark("Begin checkAtmMac mac:" + ATM_REQ_PICCMACD);
        logMessage(getLogContext());
        
		rtnCode = new ENCHelper(getATMtxData()).checkAtmMacNew(atmno, StringUtils.substring(getATMtxData().getTxRequestMessage(), 36,742), ATM_REQ_PICCMACD); // EBCDIC(36,742)
		
		getLogContext().setProgramName(ProgramName + ".updateTxData");
		getLogContext().setRemark("after checkAtmMac RC:" + rtnCode.toString());
		logMessage(getLogContext());
		
		if (rtnCode != FEPReturnCode.Normal) {
			fepfail = 1; // FEP有誤
			return; // GOTO 8      /* 更新交易紀錄 */
		}	
		
		// 檢核單筆限額，跨行繳費: MSGCTL_CHECK_LIMIT=4
		 /* 1. 全國繳費單筆交易限額: 單筆限額 200萬
			2. 「繳費移轉計畫」視為約定轉帳: 單筆限額200萬 */
		rtnCode = getATMBusiness().checkTransLimit(getATMtxData().getMsgCtl());
		if (rtnCode != FEPReturnCode.Normal) {
			fepfail = 1; // FEP有誤
			return; // GOTO 8      /* 更新交易紀錄 */
		}

	}

	/**
	 * 4. SendToCBS/ASC(if need): 本行轉入-進帳務主機查詢帳號
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		try {
			String etflag = "";
			if(StringUtils.equals("18888888",feptxn.getFeptxnBusinessUnit())
					&& StringUtils.equals("59999", feptxn.getFeptxnPaytype())
					&& StringUtils.equals("9999", feptxn.getFeptxnPayno())
					&& StringUtils.equals("99991231", feptxn.getFeptxnDueDate())){
				etflag = "Y";	//繳費移轉計畫(轉帳三萬元限制)
			}else{
				etflag = "N";	//非繳費移轉計畫
			}
			if (StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())
					|| StringUtils.equals(feptxn.getFeptxnTrinBkno(), SysStatus.getPropertyValue().getSysstatHbkno())
					|| StringUtils.equals("N", etflag)) {	//需檢核帳務代理行相關訊息
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
			cbsqryfail = 1;
			rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName);
			sendEMS(getLogContext());
		}
	}

	/**
	 * 7. SendToCBS/ASC(if need): 代理提款-進帳務主機掛現金帳
	 *
	 * @throws Exception
	 */
	private void sendToCBS2() throws Exception {
		try {
			if (getATMtxData().getMsgCtl().getMsgctlCbsFlag() == 1) {
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
	 * 9. label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
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
				ATM_FAA_CC1B2PC atm_faa_cc1b2pc = new ATM_FAA_CC1B2PC();
				// 組Header(OUTPUT-1)
				atm_faa_cc1b2pc.setWSID(atmReq.getWSID());
				atm_faa_cc1b2pc.setRECFMT("1");
				atm_faa_cc1b2pc.setMSGCAT("F");
				atm_faa_cc1b2pc.setMSGTYP("PC"); // - response
				atm_faa_cc1b2pc.setTRANDATE(atmReq.getTRANDATE());
				atm_faa_cc1b2pc.setTRANTIME(atmReq.getTRANTIME());
				atm_faa_cc1b2pc.setTRANSEQ(atmReq.getTRANSEQ());
				atm_faa_cc1b2pc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_cc1b2pc.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”(不吃卡)

				// 組D0(OUTPUT-2)畫面顯示(Display message)
				// 組 D0(004)
				atm_faa_cc1b2pc.setDATATYPE("D0");
				atm_faa_cc1b2pc.setDATALEN("004");
				atm_faa_cc1b2pc.setACKNOW("0");
				// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
				String pageNo;
				if (StringUtils.isBlank(feptxn.getFeptxnCbsRc())) { //交易尚未送主機
					pageNo = "226";
				} else {
					pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM, this.getTxData().getLogContext());
					if (StringUtils.equals(pageNo, "2999")) {
						pageNo = "226";
					}
				}
				atm_faa_cc1b2pc.setPAGENO(pageNo);

				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				atm_faa_cc1b2pc.setPTYPE("S0");
				atm_faa_cc1b2pc.setPLEN("193");
				atm_faa_cc1b2pc.setPBMPNO("010000"); // FPC
				// 西元年轉民國年
				atm_faa_cc1b2pc.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
				atm_faa_cc1b2pc.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
				atm_faa_cc1b2pc.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				atm_faa_cc1b2pc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));

				atm_faa_cc1b2pc.setPATXBKNO(feptxn.getFeptxnBkno());
				atm_faa_cc1b2pc.setPSTAN(feptxn.getFeptxnStan());
				if (!"EAT".equals(feptxn.getFeptxnChannel())) { //EAT 通道，交易失敗時不給記帳日及轉入行
					//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
					atm_faa_cc1b2pc.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
					// 轉入行
					atm_faa_cc1b2pc.setPITXBKNO(feptxn.getFeptxnTrinBkno());
				}
				// ATM回應代碼(空白放 "000") // [20221216]
				atm_faa_cc1b2pc.setPRCCODE(feptxn.getFeptxnReplyCode());
				// 轉出行
				atm_faa_cc1b2pc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				// 取得原存行的促銷應用訊息
				atm_faa_cc1b2pc.setPARPC(feptxn.getFeptxnLuckyno());
				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					atm_faa_cc1b2pc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                    //轉出帳號(明細表顯示內容)，他行帳號:16位,第10~12位隱碼
                    String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
                    if(StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
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
                	}else {
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
                	}
					atm_faa_cc1b2pc.setPACCNO(FROMACT);
					//轉入帳號(明細表顯示內容)
					atm_faa_cc1b2pc.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
				}else {
					 //交易種類
					atm_faa_cc1b2pc.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
                    //轉出帳號(明細表顯示內容)atm_faa_cc1apc
                    String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
                    if(StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
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
                	}else {
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
                	}
                    atm_faa_cc1b2pc.setPACCNO(FROMACT);
					//轉入帳號(明細表顯示內容)
                    atm_faa_cc1b2pc.setPTRINACCT(feptxn.getFeptxnReconSeqno());
				}
				rfs.set("");
				rtnMessage = atm_faa_cc1b2pc.makeMessage();
				
				if (!"ATM".equals(feptxn.getFeptxnChannel())) {
					rtnMessage = atm_faa_cc1b2pc.getMSGCAT() + atm_faa_cc1b2pc.getMSGTYP()
							+ atm_faa_cc1b2pc.getTRANDATE() + atm_faa_cc1b2pc.getTRANTIME() + feptxn.getFeptxnStan()
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
					atm_faa_cc1b2pc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
							getATMtxData().getLogContext()));
					atm_faa_cc1b2pc.setMACCODE(""); /* 訊息押碼 */
				} else {
					atm_faa_cc1b2pc.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				getLogContext().setRemark("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(getLogContext());
				rtnMessage = atm_faa_cc1b2pc.makeMessage();
			} else {
				ATM_FAA_CC1B2PN atm_faa_cc1b2pn = new ATM_FAA_CC1B2PN();
				// 組Header(OUTPUT-1)
				atm_faa_cc1b2pn.setWSID(atmReq.getWSID());
				atm_faa_cc1b2pn.setRECFMT("1");
				atm_faa_cc1b2pn.setMSGCAT("F");
				atm_faa_cc1b2pn.setMSGTYP("PN"); // + response
				atm_faa_cc1b2pn.setTRANDATE(atmReq.getTRANDATE());
				atm_faa_cc1b2pn.setTRANTIME(atmReq.getTRANTIME());
				atm_faa_cc1b2pn.setTRANSEQ(atmReq.getTRANSEQ());
				atm_faa_cc1b2pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_cc1b2pn.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”(不吃卡)

				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				atm_faa_cc1b2pn.setPTYPE("S0");
				atm_faa_cc1b2pn.setPLEN("193");
				atm_faa_cc1b2pn.setPBMPNO("000010"); // FPN
				// 西元年轉民國年
				atm_faa_cc1b2pn.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
				atm_faa_cc1b2pn.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
				atm_faa_cc1b2pn.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				atm_faa_cc1b2pn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
				// 格式:$999.0 ex :$0.0
				atm_faa_cc1b2pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0.0"));
				//交易成功才顯示帳戶餘額
				//格式 :正值放$,負值放-,$99,999,999,999.00(共18位)右靠左補空白
                BigDecimal feptxnBalb = feptxn.getFeptxnBalb();
                if (feptxnBalb.compareTo(BigDecimal.ZERO) >= 0) {
                	atm_faa_cc1b2pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "$#,##0.00"),18," "));
                } else {
                	atm_faa_cc1b2pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "-#,##0.00"),18," "));
                }
				
				atm_faa_cc1b2pn.setPATXBKNO(feptxn.getFeptxnBkno());
				atm_faa_cc1b2pn.setPSTAN(feptxn.getFeptxnStan());
				//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
				atm_faa_cc1b2pn.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
				// 轉入行
				atm_faa_cc1b2pn.setPITXBKNO(feptxn.getFeptxnTrinBkno());
				// ATM回應代碼(空白放 "000") // [20221216]
				atm_faa_cc1b2pn.setPRCCODE(feptxn.getFeptxnReplyCode());
				// 轉出行
				atm_faa_cc1b2pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				// 取得原存行的促銷應用訊息
				atm_faa_cc1b2pn.setPARPC(feptxn.getFeptxnLuckyno());
				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					atm_faa_cc1b2pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                    //轉出帳號(明細表顯示內容)，他行帳號:16位,第10~12位隱碼
                    String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
                    if(StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
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
                	}else {
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
                	}
					atm_faa_cc1b2pn.setPACCNO(FROMACT);
					//轉入帳號(明細表顯示內容)
					atm_faa_cc1b2pn.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
				}else {
					 //交易種類
					atm_faa_cc1b2pn.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
                   //轉出帳號(明細表顯示內容)atm_faa_cc1apc
                   String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
                   if(StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
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
               	}else {
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
               	}
                   atm_faa_cc1b2pn.setPACCNO(FROMACT);
					//轉入帳號(明細表顯示內容)
                   atm_faa_cc1b2pn.setPTRINACCT(feptxn.getFeptxnReconSeqno());
				}
				rfs.set("");
				rtnMessage = atm_faa_cc1b2pn.makeMessage();
				
				if (!"ATM".equals(feptxn.getFeptxnChannel())) {
					rtnMessage = atm_faa_cc1b2pn.getMSGCAT() + atm_faa_cc1b2pn.getMSGTYP()
							+ atm_faa_cc1b2pn.getTRANDATE() + atm_faa_cc1b2pn.getTRANTIME() + feptxn.getFeptxnStan()
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
					atm_faa_cc1b2pn.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
							getATMtxData().getLogContext()));
					atm_faa_cc1b2pn.setMACCODE(""); /* 訊息押碼 */
				} else {
					atm_faa_cc1b2pn.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				getLogContext().setRemark("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(getLogContext());
				rtnMessage = atm_faa_cc1b2pn.makeMessage();
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
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
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode2.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
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
        // 電文被主機視為garbage時(所有電文)，只傳送HEAD 給前端
		String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
		String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());

		if ("XXXX".equals(IMSRC4_FISC) && "XXX".equals(IMSRC_TCB)) {
			isGarbageR = true;// GO TO 10 /*組GarbageResponse回覆ATM */
		}
	}

	private String eatmResponse(String outdata) throws Exception {
		String rtnMessage = "" ;
		try {
			/* 組 WEBATM Response OUT-TEXT */
			RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();

			if (atmrepfpc == 1) {
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
            
            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
            	rtnMessage =  atm_fsn_head2.getMSGCAT() + atm_fsn_head2.getMSGTYP() + atm_fsn_head2.getTRANDATE()
	    	 		+ atm_fsn_head2.getTRANTIME() + feptxn.getFeptxnStan()
	        		+ atmReq.getIPYDATA().substring(10, 18) +  atmReq.getTRANSEQ()
            		+ atmReq.getTDRSEG() + StringUtils.rightPad(feptxn.getFeptxnReplyCode(), 4, " ");
            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
            	this.logContext.setProgramName(StringUtils.join(this.aaName, ".garbageResponse"));
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
	
	private String eatmGarbageResponse(String outdata) throws Exception {
		String rtnMessage = "" ;
		try {
			/* 組 WEBATM Response OUT-TEXT */
			RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
			SEND_EATM_FSN_HEAD2 rs = new SEND_EATM_FSN_HEAD2();
			SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body rsbody = new SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body();
			SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_NS1MsgRs msgrs = new SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_NS1MsgRs();
			SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_Header header = new SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_Header();
			SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_SvcRs();
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
	 * 更新feptxn
	 *
	 * @return
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
}
