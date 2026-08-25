package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_WW1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_WW1APN;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Objects;

public class EMVIssueRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
    private FEPReturnCode _rtnCode3 = FEPReturnCode.Normal;
    private Intltxn intlTxn = new Intltxn(); // 國際卡檔
    private Intltxn oriintlTxn = new Intltxn(); // 國際卡檔
    private String atmno;
    private int fepfail = 0;  //預設FEP處理成功
	private int fiscfail = 0;  //預設FISC處理成功
	private int cbsfail = 0;  //預設CBS處理成功
	private int atmrepfpc = 1;  //預設下送ATM FPC
	private boolean toCbs = false;
	private boolean isGarbageR = false;

    public EMVIssueRequestA(ATMData txnData) throws Exception {
        super(txnData);
    }

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
        	
			getFiscBusiness().getFISCTxData().setFiscTeleType(FISCSubSystem.EMVIC);
            // 1. 交易記錄初始資料
            _rtnCode = getATMBusiness().PrepareFEPTXN_EMV();
			if (this._rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".PrepareFEPTXN_EMV");
				getLogContext().setRemark("FEPTXN_EMV PREPARE ERROR");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			}
			
			_rtnCode = getATMBusiness().prepareFEPTXNTCB();
			if (this._rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
				getLogContext().setRemark("FEPTXNTCB PREPARE ERROR");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			}
			
            // 2. 新增交易記錄(FEPTXN)
			this.addTxData();
			if (this._rtnCode != FEPReturnCode.Normal) {
				return rtnMessage;
			}

            // 3. 商業邏輯檢核
			_rtnCode = checkBusinessRule();


            // 4. 組送往 FISC 之 Request 電文並等待財金之 Response
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendEMVRequestToFISC(getATMRequest());
                getLogContext().setProgramName(ProgramName + "." + "sendEMVRequestToFISC");
    			getLogContext().setRemark("after send EMV Request To FISC, RC:" + _rtnCode.toString());
    	        logMessage(getLogContext());
                if (_rtnCode != FEPReturnCode.Normal) {
                	fepfail = 1;  // FEP傳送FISC電文有誤
					// GO TO  8   /* 更新交易紀錄 */
                }
            }

            // 5. CheckResponseFromFISC:檢核回應電文是否正確
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = getFiscBusiness().checkEMVResponseMessage();
				/* CIRRUS國際卡餘額查詢(2631) , RC:4007為正常交易*/
                getLogContext().setProgramName(ProgramName + "." + "checkEMVResponseMessage");
    			getLogContext().setRemark("after check EMV Response Message, RC:" + _rtnCode.toString());
    	        logMessage(getLogContext());
                if (_rtnCode != FEPReturnCode.Normal) {
                	fepfail = 1;// GO TO  8   /* 更新交易紀錄 */
                }else if(!StringUtils.equals(feptxn.getFeptxnRepRc().substring(0, 3), "400")) { /* -REP */
                	getLogContext().setProgramName(ProgramName + "." + "checkEMVResponseMessage");
        			getLogContext().setRemark("after check EMV Response Message, FeptxnRepRc:" + feptxn.getFeptxnRepRc());
        	        logMessage(getLogContext());
                	fiscfail = 1;
                	if(StringUtils.equals(feptxn.getFeptxnPcode().substring(3, 4), "1")) {
						//餘額查詢失敗的某些錯誤需要入手續費，由IMS決定
                		toCbs = true;
                		// GO TO  7  /* SendToCBS */
                	}else{
                		// GO TO  8  /* 更新交易紀錄 */
                	}
                }else {
                	toCbs = true;
                	//銀聯檢查BITMAP63
                	if(StringUtils.isBlank(getFiscEMVICRes().getIcCheckdata())) {
                		getLogContext().setProgramName(ProgramName + ". After check EMV Response Message");
                		getLogContext().setRemark("財金REP未包含BITMAP63(IC_CHECKDATA)!!");
                		logMessage(getLogContext());
                	}
					// VISA、MASTER檢查BITMAP60
					if (StringUtils.isBlank(getFiscEMVICRes().getIcCheckresult())) {
						getLogContext().setProgramName(ProgramName + ". After check EMV Response Message");
						getLogContext().setRemark("財金REP未包含BITMAP60(IC_CHECKRESULT)!!");
						logMessage(getLogContext());
					}
				}
            }

            // 6. Prepare國際卡交易(INTLTXN)記錄(if need)
            if(_rtnCode == FEPReturnCode.Normal && fepfail == 0 && fiscfail == 0) {
            	if(StringUtils.isNotBlank(getFiscEMVICRes().getOriData())) {
					RefBase<Intltxn> intltxnRefBase = new RefBase<>(intlTxn);
					RefBase<Intltxn> oriintltxnRefBase = new RefBase<>(oriintlTxn);
					_rtnCode = getFiscBusiness().prepareIntltxnEMV(intltxnRefBase, oriintltxnRefBase, MessageFlow.Response);
					getLogContext().setProgramName(ProgramName + ".prepareIntltxnEMV");
					getLogContext().setRemark(". After prepare Intltxn EMV, RC:" + _rtnCode.toString());
					logMessage(getLogContext());
					intlTxn = intltxnRefBase.get();
					oriintlTxn = oriintltxnRefBase.get();
					getATMtxData().setIntltxn(intlTxn);
					if (_rtnCode != FEPReturnCode.Normal) {
						fepfail = 1; // GO TO 8
					} else if (StringUtils.isNotBlank(getFiscEMVICRes().getOriData())) {
						_rtnCode2 = getFiscBusiness().insertINTLTxn(intlTxn);
					}
                 }
            }

            // 7. SendToCBS: 代理提款-進帳務主機掛現金帳
            if (_rtnCode == FEPReturnCode.Normal && toCbs) {
                this.sendToCBS();
            }

            // 8. 組回應電文回給 ATM&組 CON 電文回財金
            this.sendConfirm();
			//主機入扣帳逾時不回覆前端
			if(cbsfail == 1 && feptxn.getFeptxnCbsTimeout() == 1) {
				getLogContext().setProgramName(ProgramName + ".sendToConfirm");
				getLogContext().setRemark("CBS TIMEOUT.");
				sendEMS(getLogContext());
				return rtnMessage; // 回空字串，不回覆ATM
			}
            
            if(!isGarbageR) {
				// 9. 組ATM回應電文 & 回 ATMMsgHandler
	            rtnMessage = this.response();
			}else {
				//10. 	GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler 
				rtnMessage = this.garbageResponse();
			}

        } catch (Exception ex) {
            rtnMessage = "";
            _rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "." + "processRequestData");
            sendEMS(getLogContext());
            throw ex;
        } finally {
        	getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getATMtxData().getLogContext().setMessage("MessageToATM:"+rtnMessage);
			getATMtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getATMtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			getATMtxData().getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode));
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
			logMessage(Level.DEBUG, getATMtxData().getLogContext());
        }
        return rtnMessage;
    }

    /**
     * 商業邏輯檢核
     *
     * @return
     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtn = FEPReturnCode.Normal;
        try {
        	atmno = feptxn.getFeptxnAtmno();
        	//3.1 檢核ATM電文
            rtn = checkRequestFromATM(getATMtxData());
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
    		getLogContext().setRemark("after checkRequestFromATM RC:" + rtn.toString());
    		logMessage(getLogContext());
            if (rtn != FEPReturnCode.Normal) {
            	fepfail = 1;
                return rtn; /* 更新交易紀錄 */
            }
            
            //3.2 檢核不可使用EMV的交易
            // 檢核自行卡不能使用PLUS(2620)及Cirrus(2630) 提款以及部分BIN_NO 不能使用26xx交易
            // 不分行庫，銀聯卡交易需存在UPBIN中才能交易
            if (feptxn.getFeptxnTmoFlag() == 0) { //銀聯卡檢核
            	rtn = FEPReturnCode.CCardServiceNotAllowed;
            	fepfail = 1;
            	getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
            	if(StringUtils.equals(feptxn.getFeptxnPcode().substring(0, 3), "260")) {
            		getLogContext().setRemark("銀聯卡BIN不存在UPBIN：" + feptxn.getFeptxnTrk2().substring(0, 10));
            	}else {
            		getLogContext().setRemark(feptxn.getFeptxnTrk2().substring(0, 6) + "不能使用EMV國際卡交易：" + feptxn.getFeptxnPcode());
            	}
         		logMessage(getLogContext());
                return rtn; /* 更新交易紀錄 */
            }
            
            //3.3 檢核ATM電文訊息押碼(MAC)
            String ATM_REQ_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
            if (StringUtils.isBlank(ATM_REQ_PICCMACD)) {
            	getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
         		getLogContext().setRemark("ATM_REQ_PICCMACD is empty.");
         		logMessage(getLogContext());
            	fepfail = 1;
    			return FEPReturnCode.ENCCheckMACError; /* 更新交易紀錄 */
    		}

            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
    		getLogContext().setRemark("Begin checkAtmMacNew mac:" + ATM_REQ_PICCMACD);
            logMessage(getLogContext());
            
            rtn = new ENCHelper(getATMtxData()).checkAtmMacNew(atmno, StringUtils.substring(getATMtxData().getTxRequestMessage(), 36, 750), ATM_REQ_PICCMACD); // EBCDIC(36,750)
            
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
    		getLogContext().setRemark("after checkAtmMacNew RC:" + rtn.toString());
    		logMessage(getLogContext());
    		
            if (rtn != FEPReturnCode.Normal) {
            	fepfail = 1;
                return rtn;  /* 更新交易紀錄 */
            }
    		
            //3.4 轉換跨行預借現金密碼
            String ssCode = getATMRequest().getSSCODE();
            String trk2 = getATMRequest().getTRK2();
    		if(StringUtils.isBlank(ssCode)) {
    			//轉換PIN BLOCK失敗
    			getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
         		getLogContext().setRemark("ssCode is empty.");
         		logMessage(getLogContext());
    			fepfail = 1;
    			rtn = FEPReturnCode.ENCPINBlockConvertError;
    			return rtn; // GO TO  8   /* 更新交易紀錄 */
    		}
    		
    		ENCKeyType keytype;
    		String mode = "01";
    		String accno;
    		String pin = ssCode;
    		String atmSeqNo = StringUtils.rightPad(this.getATMRequest().getTRANSEQ(), 8, "0"); 
			atmSeqNo = EbcdicConverter.toHex(CCSID.English, atmSeqNo.length(), atmSeqNo);
    		RefString rfs = new RefString();
    		rfs.set("");
    		
    		if("3".equals(getATMRequest().getAPPLUSE())) {
    			keytype = ENCKeyType.T3;
    		}else {
    			keytype = ENCKeyType.S1;
    		}
    		
    		int eqInt = trk2.indexOf("D");
    		int eqInt2 = trk2.indexOf("=");
    		if((eqInt > -1 && eqInt <= 12) || (eqInt2 > -1 && eqInt2 <= 12)) {
    			if(eqInt > -1) {
    				accno = StringUtils.leftPad(StringUtils.substring(trk2, 0, eqInt - 1), 12, "0");
    			}else {
    				accno = StringUtils.leftPad(StringUtils.substring(trk2, 0, eqInt2 - 1), 12, "0");
    			}
    		}else if(eqInt > 12 || eqInt2 > 12) {
    			int start;
    			if(eqInt > 12) {
    				start = eqInt - 13;
                }else {
    				start = eqInt2 - 13;
                }
                accno = StringUtils.substring(trk2, start, start + 12);

            }else {
    			accno = StringUtils.substring(trk2, 3, 15);//ATM.TRK2[4 :12]
    		}
    		
			try {
				 // ATM PIN 轉換為IMS PIN
				rtn = new ENCHelper(getATMtxData()).ConvertATMPinToIMS(keytype, mode, atmno, atmSeqNo, accno, pin, rfs);
			} catch (Exception e) {
				getLogContext().setProgramException(e);
				sendEMS(getLogContext());
				rtn = ENCReturnCode.ENCPINBlockConvertError;
			}
			
			getLogContext().setProgramName(ProgramName + "." + "checkBusinessRule");
			getLogContext().setRemark("after Convert ATM Pin To IMS, RC:" + rtn.toString());
	        logMessage(getLogContext());
			
			if(rtn  == FEPReturnCode.Normal) {
				getLogContext().setProgramName(ProgramName + "." + "checkBusinessRule");
				getLogContext().setRemark("after Convert ATM Pin To IMS, new pin:" + rfs.get());
		        logMessage(getLogContext());
				feptxn.setFeptxnPinblock(rfs.get());
			}else {
				fepfail = 1;
				return rtn; // GO TO  8       /* 更新交易紀錄 */
			}

			rfs = new RefString();
			pin = feptxn.getFeptxnPinblock(); 
			try {
				rtn = new ENCHelper(getATMtxData().getFeptxn(), getATMtxData()).ConvertATMPinToFISC(pin, rfs);
			} catch (Exception e) {
				getLogContext().setProgramException(e);
				sendEMS(getLogContext());
				rtn = ENCReturnCode.ENCPINBlockConvertError;
			}
			
			getLogContext().setProgramName(ProgramName + "." + "checkBusinessRule");
			getLogContext().setRemark("after Convert ATM Pin To FISC, RC:" + rtn.toString());
	        logMessage(getLogContext());
			
			if (rtn == FEPReturnCode.Normal) {
				getLogContext().setProgramName(ProgramName + "." + "checkBusinessRule");
				getLogContext().setRemark("after Convert ATM Pin To FISC, new pin:" + rfs.get());
		        logMessage(getLogContext());
				feptxn.setFeptxnPinblock(rfs.get());
			}else {
				fepfail = 1;
				// GO TO  8       /* 更新交易紀錄 */
			}
			
            return rtn;
        } catch (Exception ex) {
            // 異常時要Return ProgramException
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "." + "checkBusinessRule");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 代理提款-進帳務主機掛現金帳
     *
     * @return
     */
    private void sendToCBS() {
        try {
            if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlCbsFlag())) {
                /* 進主機入扣帳/手續費 */
                String AATxTYPE = "1"; // 上CBS入扣帳
                String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid();
                feptxn.setFeptxnCbsTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
                _rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
                getLogContext().setProgramName(ProgramName + ".sendToCBS");
				getLogContext().setRemark(". After send To CBS, RC:" + _rtnCode.toString());
				logMessage(getLogContext());
                tota = hostAA.getTota();
                if (_rtnCode != FEPReturnCode.Normal) {
    				cbsfail = 1;    // CBS有誤
    				if(feptxn.getFeptxnCbsTimeout() == 0) {
						// HostResponse無Timeout，回前端主機的處理結果
    					feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
    				} else { // HostResponseTimeout
    					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
    				}
    			}
            }
        } catch (Exception ex) {
        	cbsfail = 1; 
        	_rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName);
            sendEMS(getLogContext());
        }
    }

    /**
     * label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
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
                ATM_FAA_WW1APC atm_faa_ww1apc = new ATM_FAA_WW1APC();
                // 組Header(OUTPUT-1)
                atm_faa_ww1apc.setWSID(atmReq.getWSID());
                atm_faa_ww1apc.setRECFMT("1");
                atm_faa_ww1apc.setMSGCAT("F");
                atm_faa_ww1apc.setMSGTYP("PC"); // - response
                atm_faa_ww1apc.setTRANDATE(atmReq.getTRANDATE());  // 西元後兩碼+系統月日共六碼
                atm_faa_ww1apc.setTRANTIME(atmReq.getTRANTIME()); // 系統時間
                atm_faa_ww1apc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                atm_faa_ww1apc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
                // 財金回應代碼: 4305,4403,4412,4413 要留置卡片
                String repRc = feptxn.getFeptxnRepRc();
                if ("4305".equals(repRc) || "4403".equals(repRc) || "4412".equals(repRc) || "4413".equals(repRc)) {
                    atm_faa_ww1apc.setPRCRDACT("2"); // 留置卡片
                } else {
                    atm_faa_ww1apc.setPRCRDACT("4"); // 不處理
                }

                // 組D0(OUTPUT-2)畫面顯示(Display message)
                // 組 D0(004)
                atm_faa_ww1apc.setDATATYPE("D0");
                atm_faa_ww1apc.setDATALEN("004");
                atm_faa_ww1apc.setACKNOW("0");
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
                atm_faa_ww1apc.setPAGENO(pageNo);

                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                atm_faa_ww1apc.setPTYPE("S0");
                atm_faa_ww1apc.setPLEN("161");
                atm_faa_ww1apc.setPBMPNO("010000"); // FPC
                // 日期格式 :YYYYMMDD(不用轉換)
                atm_faa_ww1apc.setPDATE(feptxn.getFeptxnTxDateAtm());
                // 時間格式：HHMMSS 轉為HH:MM:SS
                atm_faa_ww1apc.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
                //交易種類
                if(StringUtils.equals(feptxn.getFeptxnPcode().substring(3, 4), "1")  ) {
                	atm_faa_ww1apc.setPTXTYPE("09"); //查詢交易
                }else {
                	atm_faa_ww1apc.setPTXTYPE("01");
                }
                atm_faa_ww1apc.setPTID(feptxn.getFeptxnAtmno());
                // 交易金額(格式：$$$,$$$,$$9)
                atm_faa_ww1apc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));

                //國際卡卡號(明細表顯示內容) ，前6及後4不隱碼，中間給*
                String EMVCARD = feptxntcb.getFeptxntcbEmvcard();
                if(StringUtils.isNotBlank(EMVCARD)) {
                	EMVCARD = EMVCARD.trim();
					int len_F = EMVCARD.length();
					if(len_F <= 10) {
					}else {
						int sub = len_F - 4;
						EMVCARD = EMVCARD.substring(0, 6) + StringUtils.repeat("*", (sub - 6)) + EMVCARD.substring(sub);
					}
				}
                atm_faa_ww1apc.setPACCNO(EMVCARD);
                // 代理銀行別
                atm_faa_ww1apc.setPATXBKNO(feptxn.getFeptxnBkno());
                // 跨行交易序號
                atm_faa_ww1apc.setPSTAN(feptxn.getFeptxnStan());
                //授權碼
                atm_faa_ww1apc.setEMVAUTH(feptxn.getFeptxnAuthcd());
                //ATM回應代碼(CBSProcess已依主機下送規則處理)
                atm_faa_ww1apc.setPRCCODE(feptxn.getFeptxnReplyCode());
                //轉出行
                atm_faa_ww1apc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
                //國際組織回應代碼
                if(getFiscEMVICRes().getORIDATA() != null && StringUtils.isNotBlank(getFiscEMVICRes().getORIDATA().getRsCode())) {
                	atm_faa_ww1apc.setCARDCODE(getFiscEMVICRes().getORIDATA().getRsCode());
                }else {
                	atm_faa_ww1apc.setCARDCODE("00");
                }
                
                rfs.set("");
                rtnMessage = atm_faa_ww1apc.makeMessage();
                _rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
                
                getLogContext().setProgramName(ProgramName + ".Response");
				getLogContext().setRemark("after makeAtmMacP3 RC:" + _rtnCode.toString());
	            logMessage(getLogContext());
                
                if (_rtnCode != FEPReturnCode.Normal) {
                    atm_faa_ww1apc.setMACCODE(""); /* 訊息押碼 */
                } else {
                    atm_faa_ww1apc.setMACCODE(rfs.get()); /* 訊息押碼 */
                }
                
                //VISA 交易ARPC(bit #60 IC 卡驗證結果資料)
                //銀聯交易 (bit #63 IC 卡驗證資料)
                if(StringUtils.isNotBlank(getFiscEMVICRes().getIcCheckresult())){ //FISC 0210 bit 60
                	String w_ln =  StringUtil.convertFromAnyBaseString(String.valueOf((getFiscEMVICRes().getIcCheckresult().length() / 2) + 2), 10, 16, 4);
                	atm_faa_ww1apc.setARPC(w_ln + getFiscEMVICRes().getIcCheckresult());
                }else if(StringUtils.isNotBlank(getFiscEMVICRes().getIcCheckdata())){ //FISC 0210 bit 63
                	String w_ln =  StringUtil.convertFromAnyBaseString(String.valueOf((getFiscEMVICRes().getIcCheckdata().length() / 2) + 2), 10, 16, 4);
                	atm_faa_ww1apc.setARPC(w_ln + getFiscEMVICRes().getIcCheckdata());
                }
                rtnMessage = atm_faa_ww1apc.makeMessage();
                if(StringUtils.isBlank(getFiscEMVICRes().getIcCheckresult()) && StringUtils.isBlank(getFiscEMVICRes().getIcCheckdata())){
                	rtnMessage = rtnMessage.substring(0, rtnMessage.length() - (257 * 2));
                }
            } else {
                ATM_FAA_WW1APN atm_faa_ww1apn = new ATM_FAA_WW1APN();
                // 組Header(OUTPUT-1)
                atm_faa_ww1apn.setWSID(atmReq.getWSID());
                atm_faa_ww1apn.setRECFMT("1");
                atm_faa_ww1apn.setMSGCAT("F");
                atm_faa_ww1apn.setMSGTYP("PN"); // + response
                atm_faa_ww1apn.setTRANDATE(atmReq.getTRANDATE());  // 西元後兩碼+系統月日共六碼
                atm_faa_ww1apn.setTRANTIME(atmReq.getTRANTIME()); // 系統時間
                atm_faa_ww1apn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                atm_faa_ww1apn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
                // 財金回應代碼: 4305,4403,4412,4413 要留置卡片
                String repRc = feptxn.getFeptxnRepRc();
                if ("4305".equals(repRc) || "4403".equals(repRc) || "4412".equals(repRc) || "4413".equals(repRc)) {
                    atm_faa_ww1apn.setPRCRDACT("2"); // 留置卡片
                } else {
                    atm_faa_ww1apn.setPRCRDACT("4"); // 不處理
                }

                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                atm_faa_ww1apn.setPTYPE("S0");
                atm_faa_ww1apn.setPLEN("161");
                atm_faa_ww1apn.setPBMPNO("000010"); // FPN
                // 日期格式 :YYYYMMDD(不用轉換)
                atm_faa_ww1apn.setPDATE(feptxn.getFeptxnTxDateAtm());
                // 時間格式：HHMMSS 轉為HH:MM:SS
                atm_faa_ww1apn.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
                //交易種類
                if(StringUtils.equals(feptxn.getFeptxnPcode().substring(3, 4), "1")) {
                	atm_faa_ww1apn.setPTXTYPE("09"); //查詢交易
                }else {
                	atm_faa_ww1apn.setPTXTYPE("01");
                }
                atm_faa_ww1apn.setPTID(feptxn.getFeptxnAtmno());
                // 交易金額(格式：$$$,$$$,$$9)
                atm_faa_ww1apn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
                //交易手續費，格式:$999 ex :$0
                if(feptxn.getFeptxnFeeCustpay().compareTo(BigDecimal.ZERO) > 0) {
                	atm_faa_ww1apn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0")); 
                }
                
                //交易成功才顯示可用餘額(財金0210 BIT#7)
                //格式 :正值放$,負值放-,$99,999,999,999.00(共17位)右靠左補空白
                BigDecimal feptxnBalb = feptxn.getFeptxnBala();
                if (feptxnBalb.compareTo(BigDecimal.ZERO) > 0) {
                	atm_faa_ww1apn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "$#,##0.00"),17," "));
                } else if (feptxnBalb.compareTo(BigDecimal.ZERO) < 0) {
                	atm_faa_ww1apn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "-#,##0.00"),17," "));
                }
                //國際卡卡號(明細表顯示內容) ，前6及後4不隱碼，中間給*
                String EMVCARD = feptxntcb.getFeptxntcbEmvcard();
                if(StringUtils.isNotBlank(EMVCARD)) {
                	EMVCARD = EMVCARD.trim();
					int len_F = EMVCARD.length();
					if(len_F <= 10) {
					}else {
						int sub = len_F - 4;
						EMVCARD = EMVCARD.substring(0, 6) + StringUtils.repeat("*", (sub - 6)) + EMVCARD.substring(sub);
					}
				}
                atm_faa_ww1apn.setPACCNO(EMVCARD);
                // 代理銀行別
                atm_faa_ww1apn.setPATXBKNO(feptxn.getFeptxnBkno());
                // 跨行交易序號
                atm_faa_ww1apn.setPSTAN(feptxn.getFeptxnStan());
                //授權碼
                atm_faa_ww1apn.setEMVAUTH(feptxn.getFeptxnAuthcd());
                //ATM回應代碼(CBSProcess已依主機下送規則處理)
                atm_faa_ww1apn.setPRCCODE(feptxn.getFeptxnReplyCode());
                //轉出行
                atm_faa_ww1apn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
              //國際組織回應代碼
                if(getFiscEMVICRes().getORIDATA() != null && StringUtils.isNotBlank(getFiscEMVICRes().getORIDATA().getRsCode())) {
                	atm_faa_ww1apn.setCARDCODE(getFiscEMVICRes().getORIDATA().getRsCode());
                }else {
                	atm_faa_ww1apn.setCARDCODE("00");
                }
                
                rfs.set("");
                rtnMessage = atm_faa_ww1apn.makeMessage();
                _rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
                
                getLogContext().setProgramName(ProgramName + ".Response");
				getLogContext().setRemark("after makeAtmMacP3 RC:" + _rtnCode.toString());
	            logMessage(getLogContext());
                
                if (_rtnCode != FEPReturnCode.Normal) {
                    atm_faa_ww1apn.setMACCODE(""); /* 訊息押碼 */
                } else {
                    atm_faa_ww1apn.setMACCODE(rfs.get()); /* 訊息押碼 */
                }
                //VISA 交易ARPC(bit #60 IC 卡驗證結果資料)
                //銀聯交易 (bit #63 IC 卡驗證資料)
                if(StringUtils.isNotBlank(getFiscEMVICRes().getIcCheckresult())){ //FISC 0210 bit 60
                	String w_ln =  StringUtil.convertFromAnyBaseString(String.valueOf((getFiscEMVICRes().getIcCheckresult().length() / 2) + 2), 10, 16, 4);
                    atm_faa_ww1apn.setARPC(w_ln + getFiscEMVICRes().getIcCheckresult());
                }else if(StringUtils.isNotBlank(getFiscEMVICRes().getIcCheckdata())){ //FISC 0210 bit 63
                	String w_ln =  StringUtil.convertFromAnyBaseString(String.valueOf((getFiscEMVICRes().getIcCheckdata().length() / 2) + 2), 10, 16, 4);
                    atm_faa_ww1apn.setARPC(w_ln + getFiscEMVICRes().getIcCheckdata());
                }
                rtnMessage = atm_faa_ww1apn.makeMessage();
                if(StringUtils.isBlank(getFiscEMVICRes().getIcCheckresult()) && StringUtils.isBlank(getFiscEMVICRes().getIcCheckdata())){
                	rtnMessage = rtnMessage.substring(0, rtnMessage.length() - (257 * 2));
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * 組回應電文回給 ATM&組 CON 電文回財金
     *
     * @return
     */
    private void sendConfirm() {
       	getLogContext().setProgramName(StringUtils.join(this.aaName, ".updateTxData"));
    	getLogContext().setRemark("fepfail:" + fepfail + ", fiscfail:" + fiscfail+ ", cbsfail:" + cbsfail);
    	logMessage(getLogContext());
        String feptxnRepRc = feptxn.getFeptxnRepRc();
		if(StringUtils.isNoneBlank(feptxnRepRc)) {
			feptxn.setFeptxnPending((short)2); /*解除 Pending*/
		}
        if (fepfail == 0 && fiscfail == 0  &&  cbsfail == 0 && _rtnCode == FEPReturnCode.Normal) {
			/* +REP */
            feptxn.setFeptxnReplyCode(feptxnRepRc); // 回覆 ATM正常
            feptxn.setFeptxnTxrust("B"); /* PENDING */
			atmrepfpc = 0;  // 下送FPN給ATM
			//跨行清算統計
			if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
				_rtnCode2 = getFiscBusiness().processAptot(false);
				if (_rtnCode2 != FEPReturnCode.Normal) {
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode2.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
					getLogContext().setProgramName(ProgramName + ".processAptot");
					getLogContext().setRemark("process Aptot ERROR");
					sendEMS(getLogContext());
				}
            }
        } else if (cbsfail == 1) { /*CBS 失敗*/
			feptxn.setFeptxnTxrust("R"); /* Accept-Reverse */
			if(fiscfail == 0) {  /* FISC成功*/
				if(feptxn.getFeptxnCbsTimeout() == 1) { //主機逾時不回覆財金
					feptxn.setFeptxnTxrust("B"); /*Pending*/
				}else {
					feptxn.setFeptxnConRc(feptxn.getFeptxnCbsRc());
					feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
					_rtnCode3 = getFiscBusiness().sendConfirmToFISCEMV();
					if (_rtnCode3 != FEPReturnCode.Normal) {
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode3.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
						getLogContext().setProgramName(ProgramName + ".sendConfirmToFISC");
						getLogContext().setRemark("send Confirm To FISC ERROR");
						sendEMS(getLogContext());
					}
				}
			} else { /* FISC失敗*/
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
			}
        } else if (fiscfail == 1) { /* FISC失敗*/
        	feptxn.setFeptxnTxrust("R"); /* Reject-normal */
            feptxn.setFeptxnReplyCode(feptxn.getFeptxnRepRc());
        } else { /* 其他失敗 */
			feptxn.setFeptxnTxrust("S"); /*Reject-abnormal*/
        	if(feptxn.getFeptxnFiscTimeout() != null ) {
				feptxn.setFeptxnTxrust("R"); /* Reject-normal */
				if(feptxn.getFeptxnFiscTimeout() == 1) {
					if(getATMtxData().getMsgCtl().getMsgctlFisc2way() == 1) {
						//查詢交易回覆前端逾時
						feptxn.setFeptxnReplyCode("0601");//交易逾時
					}else {
						//提款交易逾時回覆財金-Con
						feptxn.setFeptxnConRc("0601");//交易逾時
						feptxn.setFeptxnReplyCode(feptxn.getFeptxnConRc());
						_rtnCode3 = getFiscBusiness().sendConfirmToFISCEMV();
						if (_rtnCode3 != FEPReturnCode.Normal) {
							feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode3.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
							getLogContext().setProgramName(ProgramName + ".sendConfirmToFISCEMV");
							getLogContext().setRemark("send Confirm To FISC EMV ERROR");
							sendEMS(getLogContext());
						}
					}
				} else if (getATMtxData().getMsgCtl().getMsgctlFisc2way() == 0) { //財金0210電文檢核錯誤，回覆財金-Con
					feptxn.setFeptxnConRc("0101");
					_rtnCode3 = getFiscBusiness().sendConfirmToFISCEMV();
					if (_rtnCode3 != FEPReturnCode.Normal) {
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode3.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
						getLogContext().setProgramName(ProgramName + ".sendConfirmToFISCEMV");
						getLogContext().setRemark("send Confirm To FISC EMV ERROR");
						sendEMS(getLogContext());
					}
				}
        	}

        	if(StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
        		feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
        	}
        }
        feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
		if(_rtnCode3 != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(_rtnCode3.getValue());
		}else if(_rtnCode2 != FEPReturnCode.Normal) {
        	feptxn.setFeptxnAaRc(_rtnCode2.getValue());
        }else if(_rtnCode != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(_rtnCode.getValue());
		}else {
        	feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
        }
        feptxn.setFeptxnAaComplete((short)1); /*AA Close*/

		FEPReturnCode rtnCode4 = this.updateFeptxn(); 
		if (rtnCode4 != FEPReturnCode.Normal) {
			// 回寫檔案 (FEPTxn) 發生錯誤
			if(feptxn.getFeptxnAaRc() == FEPReturnCode.Normal.getValue()) {
        		this.feptxn.setFeptxnReplyCode("T452");//FEPTXNUpdateError
        		_rtnCode = rtnCode4;
        	}
			// ERROR MSG 送 EVENT MONITOR SYSTEM
			getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            getLogContext().setRemark("FEPTXN UPDATE ERROR");
			sendEMS(getLogContext());
		} 
        
		String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
		String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());

		if ("XXXX".equals(IMSRC4_FISC) && "XXX".equals(IMSRC_TCB)) {
			isGarbageR = true;// GO TO 10 /*組GarbageResponse回覆ATM */
		}
    }

    /**
     * AddTxData: 新增交易記錄(FEPTxn)
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
        	_rtnCode = FEPReturnCode.FEPTXNInsertError;
        	transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".addTxData");
            sendEMS(getLogContext());
        }
    }

	//10. 	GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler 
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
			_rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
			
			getLogContext().setProgramName(ProgramName + ".garbageResponse");
            getLogContext().setRemark("after makeAtmMacP3 RC:" + _rtnCode.toString());
            logMessage(getLogContext());
            
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
     * 時間格式：HHmmss 轉為HH:mm:ss
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
