package com.syscom.fep.server.aa.atmp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.transaction.TransactionWrapper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.UcdidExtMapper;
import com.syscom.fep.mybatis.ext.model.CashTOTARequestExt;
import com.syscom.fep.mybatis.mapper.UcdidMapper;
import com.syscom.fep.mybatis.model.CashTOTA;
import com.syscom.fep.mybatis.model.Ucdid;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.http.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * @author vincent
 */
public class WDSelfIssue extends ATMPAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private String atmno;
	private String tita ;
	private boolean isGarbageR = false;
	private CashTOTA cashTOTA = new CashTOTA();
	private UcdidMapper ucdidMapper = SpringBeanFactoryUtil.getBean(UcdidMapper.class);
	private UcdidExtMapper ucdidExtMapper = SpringBeanFactoryUtil.getBean(UcdidExtMapper.class);

	public WDSelfIssue(ATMData txnData) throws Exception {
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
			tita =EbcdicConverter.fromHex(CCSID.English, this.getTxData().getTxRequestMessage());
			this.logContext.setProgramFlowType(ProgramFlow.AAIn);
			this.logContext.setMessageFlowType(MessageFlow.Request);
			this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
			this.logContext.setMessage("ASCII TITA:"+tita);
			this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
			logMessage(this.logContext);

			// 1. Prepare():記錄MessageText & 準備回覆電文資料
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
				// 2. AddTxData: 新增交易記錄(FEPTxn)
				this.addTxData();
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

			//4. FEP檢核錯誤處理
			if(rtnCode != FEPReturnCode.Normal){
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP,
						FEPChannel.ATM, getTxData().getLogContext()));
				feptxn.setFeptxnAaRc(rtnCode.getValue());
				//GO TO  7    /* 更新交易記錄 */
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 5. SendToCBS:送往CBS主機處理
				this.sendToCBS();
			}

//			if (rtnCode == FEPReturnCode.Normal) {
				// 6. 更新全民普發ID紀錄已入帳(UCDID)
				this.sendToCASH();
//			}

			// 7. UpdateTxData: 更新交易記錄(FEPTxn)
			this.updateTxData();
			if (feptxn.getFeptxnCbsTimeout() != null && feptxn.getFeptxnCbsTimeout() == 1) { // HostResponseTimeout
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
			
			if(!isGarbageR) {
				// 8. Response:組ATM回應電文 & 回 ATMMsgHandler
				if (StringUtils.isBlank(getTxData().getTxResponseMessage())) {
					rtnMessage = this.response();
				} else {
					rtnMessage = getTxData().getTxResponseMessage();
				}
			}else {
				//9. GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler
				rtnMessage = this.garbageResponse();
			}
			
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage("MessageToATM:"+rtnMessage);
			getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode,getLogContext()));
			logMessage(Level.DEBUG, this.logContext);

			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			String ASCIIMessage = EbcdicConverter.fromHex(CCSID.English,rtnMessage.substring(0,rtnMessage.length()-8)) + rtnMessage.substring(rtnMessage.length()-8,rtnMessage.length());
			getTxData().getLogContext().setMessage("ASCII MessageToATM:"+ASCIIMessage);
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
			rtnCode = FEPReturnCode.FEPTXNInsertError;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".addTxData");
			sendEMS(getLogContext());
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
			return;
		}

		// 3.2 檢核單筆限額
		/* 自行台幣提款:FEP檢核單筆限額(3萬)，外幣未設定檢核 */
		if(DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlCheckLimit())) {
			rtnCode = getATMBusiness().checkTransLimit(getTxData().getMsgCtl());
			if (rtnCode != FEPReturnCode.Normal) {
				return; // GO TO 4 /* FEP檢核錯誤處理 */
			}
		}
		
		// 3.3 檢核ATM電文訊息押碼(MAC)
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		String ATM_TITA_PICCMACD = getTxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError; /* MAC Error */
			return; // GO TO 4 /* FEP檢核錯誤處理 */
		}
		String newMac = ATM_TITA_PICCMACD;
		this.logContext.setRemark("Begin checkAtmMac mac:" + newMac);
		logMessage(this.logContext);

		// CHANNEL = "EAT"，用ATMNO = "NEATM001"去押驗，在 CheckATMMACNew replace
		rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno,
				StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 36, 742),
				newMac);
		this.logContext.setRemark("after checkAtmMac RC:" + rtnCode.toString());
		logMessage(this.logContext);
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO 4 /* FEP檢核錯誤處理 */
		}
		
		//3.4	轉換PINBLOCK
		if("W2".equals(feptxn.getFeptxnTxCode())) {
			RefString rfs = new RefString();
			ENCKeyType keyType = ENCKeyType.T3;
			String mode = "00";
			String pin = this.getATMRequest().getIPYDATA().substring(2, 18);
			String atmSeqNo = this.getATMRequest().getTRANSEQ();
			String accno = "";
			rfs.set("");
	
			try {
				this.rtnCode = new ENCHelper(getTxData()).ConvertATMPinToIMS(keyType, mode, atmno, atmSeqNo, accno, pin, rfs);
			} catch (Exception e) {
				getLogContext().setProgramException(e);
				sendEMS(getLogContext());
				this.rtnCode = ENCReturnCode.ENCPINBlockConvertError;
			}
			if (this.rtnCode == FEPReturnCode.Normal) {
				this.logContext.setRemark("new pin:" + rfs.get());
		        logMessage(this.logContext);
				feptxn.setFeptxnPinblock(rfs.get());
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
			feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
		} else {
			// 回前端主機的處理結果
        	feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
		}
        feptxn.setFeptxnAaRc(rtnCode.getValue());
	}

	/**
	 * 6. 更新全民普發ID紀錄已入帳(UCDID)
	 */
	private void sendToCASH() {
		getLogContext().setProgramName(ProgramName + ".sendToCASH");
		String URL = ATMPConfig.getInstance().getCashDistributionUrl();
		if (("WP").equals(feptxn.getFeptxnTxCode().trim())) {
			try {
				//2025-12-04修改 找該筆交易
				Ucdid ucdid = ucdidExtMapper.selectByIdnoAndLFStan(feptxn.getFeptxnIdno().trim(), feptxntcb.getFeptxntcbHealthcard(), feptxntcb.getFeptxntcb2566Stan());
				if (ucdid == null) {
					getLogContext().setProgramName(ProgramName + ".sendToCASH");
					getLogContext().setRemark("UCDID is null");
					sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
					return; //GO TO  7    /* 更新交易記錄 */
				}
				//更新UCDID紀錄
				ucdid.setUcdid2510Txdate(feptxn.getFeptxnTxDate());
				ucdid.setUcdid2510Stan(feptxn.getFeptxnStan());
				ucdid.setUcdid2510Txtime(feptxn.getFeptxnTxTime());
				ucdid.setUcdid2510TbsdyFisc(feptxn.getFeptxnTbsdyFisc());
				ucdid.setUcdid2510Tbsdy(feptxn.getFeptxnTbsdy());
				ucdid.setUpdateTime(new Date());
				if (rtnCode == FEPReturnCode.Normal && feptxn.getFeptxnAccType() == 1) {
					ucdid.setUcdid2510Status("0"); //已入帳
				} else {
					//主機記帳失敗，解除提領，Call普發平台解除提款註記
					feptxntcb.setFeptxntcb2510Status("N"); //核驗成功，提領失敗
					// 1. 建立請求主體物件
					CashTOTARequestExt requestBody = new CashTOTARequestExt();
					requestBody.setType("2"); //解除提領
					requestBody.setAgentBank(SysStatus.getPropertyValue().getSysstatHbkno()); //發動行
					requestBody.setTxnTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))); //發送請求時間
					requestBody.setSeqNo(StringUtils.right(requestBody.getTxnTime(), 10) + feptxn.getFeptxnBkno() + feptxn.getFeptxnStan()); //交易序號(唯一)
					requestBody.setIdn(feptxn.getFeptxnIdno().trim()); //身分證號
					requestBody.setHealthId(feptxntcb.getFeptxntcbHealthcard()); //健保卡卡號
					requestBody.setChannelType("10"); //ATM提領

					//解除時填入提領註記的回應資料
					requestBody.setOrgSeqNo(ucdid.getUcdidUcdApiseqno());
					requestBody.setOrgTxnNo(ucdid.getUcdidUcdTxno());
					requestBody.setOrgRespTime(ucdid.getUcdidUcdResptime());

					feptxntcb.setFeptxntcbUcdrApiseqno(requestBody.getSeqNo()); //Call全民普發平台解除交易序號

					// 2. 設定請求標頭 (指定內容類型為 JSON)
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);

					// 3. 建立 HttpEntity，包含請求主體和標頭
					HttpEntity<CashTOTARequestExt> requestEntity = new HttpEntity<>(requestBody, headers);
					this.logContext.setMessage("Request Body: " + requestBody.toString() + ", Request Headers: " + headers);
					this.logContext.setRemark("Calling URL: " + URL);
					logMessage(this.logContext);

					// 4. 發送 POST 請求並取得回應
					RestTemplate restTemplate = new RestTemplate();
					int timeout = 50;
					// 如果是 Https 設定 TLS1.2
					if (HttpClient.isHttps(URL)) {
						restTemplate.setRequestFactory(HttpClientConfiguration.createTrustAnyHttpComponentsClientHttpRequestFactory("TLSv1.2", timeout * 1000));
					} else {
						restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(timeout * 1000));
					}
					ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, String.class);
					if (response.getBody() == null) {
						this.logContext.setRemark("API回傳值為null");
						logMessage(this.logContext);
					}else {
						String responseBody = response.getBody();
						this.logContext.setMessage("API回傳值為: " + responseBody);
						this.logContext.setRemark("Call API Success");
						logMessage(this.logContext);
						//紀錄解除提領註記的結果
						ObjectMapper objectMapper = new ObjectMapper();
						this.cashTOTA = objectMapper.readValue(responseBody, CashTOTA.class);
						feptxntcb.setFeptxntcbUcdrTxno(cashTOTA.getTxnNo()); //普發平台回應的交易序號(解除)
						feptxntcb.setFeptxntcbUcdrResptime(cashTOTA.getRespTime()); //普發平台的回應時間(解除)
						feptxntcb.setFeptxntcbUcdrRc(cashTOTA.getReturnCode()); //普發平台的回應代碼(解除)
						if (!("00000").equals(this.cashTOTA.getReturnCode())) { //解除提領失敗
							feptxn.setFeptxnAaRc(10221); //FEPReturnCode.UCDResponseError
							this.logContext.setRemark("API回傳值不正確 ReturnCode: " + this.cashTOTA.getReturnCode());
							logMessage(this.logContext);
						}
					}
					//全民普發個人紀錄更新
					ucdid.setUcdidUcdrApiseqno(feptxntcb.getFeptxntcbUcdrApiseqno());
					ucdid.setUcdidUcdrTxno(feptxntcb.getFeptxntcbUcdrTxno());
					ucdid.setUcdidUcdrResptime(feptxntcb.getFeptxntcbUcdrResptime());
					ucdid.setUcdidUcdrRc(feptxntcb.getFeptxntcbUcdrRc());
					ucdid.setUcdidUcdrChannel("ATM");
					if (StringUtils.isBlank(ucdid.getUcdidUcdrRc()) || "E0000".equals(ucdid.getUcdidUcdrRc())){
						ucdid.setUcdid2510Status("B"); //沖正解除異常(未收到API RC)
					}else if ("00000".equals(ucdid.getUcdidUcdrRc())){
						ucdid.setUcdid2510Status("4"); //沖正解除提領
						ucdid.setUcdidLaststatus("C"); //沖正(API解除成功)
					}
				}

				//更新全民普發個人紀錄
				int count = ucdidMapper.updateByPrimaryKey(ucdid);
				if (count <= 0) {
					throw new Exception();
				}else {
					this.logContext.setRemark("Update UCDID Success");
					logMessage(this.logContext);
				}
			} catch (Exception e) {
				getLogContext().setProgramException(e);
				getLogContext().setProgramName(ProgramName + ".sendToCASH");
				sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
			}
		}
	}

	/**
	 * 7. UpdateTxData: 更新交易記錄(FEPTxn)
	 */
	private void updateTxData() {
		feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); // (RESPONSE)
		feptxn.setFeptxnAaComplete((short) 1); /* AA Complete */
		/* For報表, 寫入處理結果 */
		if (rtnCode == FEPReturnCode.Normal) {
			feptxn.setFeptxnTxrust("B"); /* 處理結果=入帳成功 */
		} else {
			feptxn.setFeptxnTxrust("R"); /* 處理結果=Reject */
		}

		// 先透過指定的BeanName取得DataSource Transaction Manager SpringBean Object
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		// 將上面取到的transactionManager包進TransactionWrapper, 注意TransactionWrapper必須寫進try(...)中
		try (TransactionWrapper wrapper = new TransactionWrapper(transactionManager)) {
			// 呼叫doTransaction方法開始事務
			wrapper.beginTransaction(() -> {
				// 更新FEPTXN
				int updateCount = feptxnDao.updateByPrimaryKeySelective(this.feptxn);
				if (updateCount <= 0) {
					wrapper.rollback();
					return; // TransactionWrapper預設會自動回滾
				}
				// 更新FEPTXNTCB
				updateCount = feptxnDao.updateByPrimaryKeySelective(this.feptxntcb);
				if (updateCount <= 0) {
					wrapper.rollback();
					return; // TransactionWrapper預設會自動回滾
				}
				// commit by manual
				wrapper.commit();
			});
		} catch (Exception e) {
			// TODO 處理異常, 並且call sendEMS(logData)
			this.feptxn.setFeptxnReplyCode("T452");//FEPTXNUpdateError
			rtnCode = FEPReturnCode.FEPTXNUpdateError;
			getLogContext().setProgramName(ProgramName + ".updateTxData");
			getLogContext().setRemark("FEPTXN UPDATE ERROR");
			sendEMS(getLogContext());
		}
		
		// 電文被主機視為garbage時(所有電文)，只傳送HEAD 給ATM
		String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
		String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());
		
		if("XXXX".equals(IMSRC4_FISC) && "XXX".equals(IMSRC_TCB)) {
			isGarbageR = true;// GO TO  9     /*組GarbageResponse回覆ATM */
		}
	}

	/**
	 * 8. Response:組ATM回應電文 & 回 ATMMsgHandler
	 *
	 * @throws Exception
	 */
	private String response() {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
			ATMGeneralRequest atmReq = this.getATMRequest();
			String feptxnTxCode = feptxn.getFeptxnTxCode();
			ENCHelper atmEncHelper = new ENCHelper(this.getTxData());
			RefString rfs = new RefString();
			if (rtnCode != FEPReturnCode.Normal) {
				switch (feptxnTxCode) {
				case "US": // 外幣提款交易
				case "JP": // 外幣提款交易
					ATM_FAA_CC1B3PC atm_faa_cc1b3pc = new ATM_FAA_CC1B3PC();
					// 組 Header
					atm_faa_cc1b3pc.setWSID(atmReq.getWSID());
					atm_faa_cc1b3pc.setRECFMT("1");
					atm_faa_cc1b3pc.setMSGCAT("F");
					atm_faa_cc1b3pc.setMSGTYP("PC"); // - response
					atm_faa_cc1b3pc.setTRANDATE(atmReq.getTRANDATE());
					atm_faa_cc1b3pc.setTRANTIME(atmReq.getTRANTIME());
					atm_faa_cc1b3pc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1b3pc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b3pc.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

					// 組D0(004)畫面顯示(Display message)
					atm_faa_cc1b3pc.setDATATYPE("D0");
					atm_faa_cc1b3pc.setDATALEN("004");
					atm_faa_cc1b3pc.setACKNOW("0");
					// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
					String pageNo;
					// 此欄給主機回應的代碼，尚未走到主機就給空值
					if (StringUtils.isBlank(feptxn.getFeptxnCbsRc())) { //交易尚未送主機
						pageNo = "226";
					} else {
						pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM, this.getTxData().getLogContext());
						if (StringUtils.equals(pageNo, "2999")) {
							pageNo = "226";
						}
					}

					atm_faa_cc1b3pc.setPAGENO(pageNo);

					// 組S0明細表內容(PRINT message),依交易下送欄位,電文總長度也不同
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
					atm_faa_cc1b3pc.setPRCCODE(feptxn.getFeptxnReplyCode());
					// 轉出行
					atm_faa_cc1b3pc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 提領外幣
					atm_faa_cc1b3pc.setPEXRATE(Objects.toString(feptxn.getFeptxnExrate()));
					atm_faa_cc1b3pc.setPAMT(Objects.toString(feptxn.getFeptxnTxAmt()));
					// 處理有收到CBS Response的欄位值
					if (tota != null) {
						//交易種類
						atm_faa_cc1b3pc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
						//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼，ex：123456***0
						String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
						if (StringUtils.isNotBlank(FROMACT)) {
							FROMACT = FROMACT.trim();
							int len_F = FROMACT.length();
							if (len_F <= 6) {
							} else {
								if (len_F > 9) {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
								} else {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
								}
							}
						}
						atm_faa_cc1b3pc.setPACCNO(FROMACT);
						//轉出行
						atm_faa_cc1b3pc.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
						atm_faa_cc1b3pc.setPTMEXNO(this.getImsPropertiesValue(tota,ImsMethodName.FWDTMEX_NO.getValue()));
					}else {
						//交易種類
						atm_faa_cc1b3pc.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
						//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼，ex：123456***0
						String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
						if (StringUtils.isNotBlank(FROMACT)) {
							FROMACT = FROMACT.trim();
							int len_F = FROMACT.length();
							if (len_F <= 6) {
							} else {
								if (len_F > 9) {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
								} else {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
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
								getTxData().getLogContext()));
						atm_faa_cc1b3pc.setMACCODE(""); /* 訊息押碼 */
					} else {
						atm_faa_cc1b3pc.setMACCODE(rfs.get()); /* 訊息押碼 */
					}
					this.logContext.setRemark("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
					rtnMessage = atm_faa_cc1b3pc.makeMessage();
					break;
				default: // 其他提款交易
					ATM_FAA_CC1APC atm_faa_cc1apc = new ATM_FAA_CC1APC();
					// 組 Header
					atm_faa_cc1apc.setWSID(atmReq.getWSID());
					atm_faa_cc1apc.setRECFMT("1");
					atm_faa_cc1apc.setMSGCAT("F");
					atm_faa_cc1apc.setMSGTYP("PC"); // - response
					atm_faa_cc1apc.setTRANDATE(atmReq.getTRANDATE());
					atm_faa_cc1apc.setTRANTIME(atmReq.getTRANTIME());
					atm_faa_cc1apc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1apc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1apc.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

					// 組D0(004)畫面顯示(Display message)
					atm_faa_cc1apc.setDATATYPE("D0");
					atm_faa_cc1apc.setDATALEN("004");
					atm_faa_cc1apc.setACKNOW("0");
					// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
					
					// 此欄給主機回應的代碼，尚未走到主機就給空值
					if (StringUtils.isBlank(feptxn.getFeptxnCbsRc())) { //交易尚未送主機
						pageNo = "226";
					} else {
						pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM, this.getTxData().getLogContext());
						if (StringUtils.equals(pageNo, "2999")) {
							pageNo = "226";
						}
					}

					atm_faa_cc1apc.setPAGENO(pageNo);

					// 組S0明細表內容(PRINT message),依交易下送欄位,電文總長度也不同
					atm_faa_cc1apc.setPTYPE("S0");
					atm_faa_cc1apc.setPLEN("191");
					atm_faa_cc1apc.setPBMPNO("010000"); // FPC
					// 西元年轉民國年
					atm_faa_cc1apc.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1apc.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_cc1apc.setPTID(feptxn.getFeptxnAtmno());
					// 交易金額，格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1apc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));

					atm_faa_cc1apc.setPATXBKNO(feptxn.getFeptxnBkno()); // 代理行
					atm_faa_cc1apc.setPSTAN(feptxn.getFeptxnStan());    // 交易序號
					//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
					atm_faa_cc1apc.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
					// ATM回應代碼(CBSProcess已依主機下送規則處理)
					atm_faa_cc1apc.setPRCCODE(feptxn.getFeptxnReplyCode());
					// 轉出行
					atm_faa_cc1apc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 全民普發WP，轉入帳號放健保卡號
					if ("WP".equals(feptxnTxCode)) {
						atm_faa_cc1apc.setPTRINACCT(feptxntcb.getFeptxntcbHealthcard());
					}
					// 處理有收到CBS Response的欄位值
					if (tota != null) {
						//交易種類
						atm_faa_cc1apc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
						//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼，ex：123456***0
						String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
						if (StringUtils.isNotBlank(FROMACT)) {
							FROMACT = FROMACT.trim();
							int len_F = FROMACT.length();
							if (len_F <= 6) {
							} else {
								if (len_F > 9) {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
								} else {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
								}
							}
						}
						atm_faa_cc1apc.setPACCNO(FROMACT);
						//轉出行
						atm_faa_cc1apc.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
					}else {
						//交易種類
						atm_faa_cc1apc.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
						//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼，ex：123456***0
						String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
						if (StringUtils.isNotBlank(FROMACT)) {
							FROMACT = FROMACT.trim();
							int len_F = FROMACT.length();
							if (len_F <= 6) {
							} else {
								if (len_F > 9) {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
								} else {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
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
								getTxData().getLogContext()));
						atm_faa_cc1apc.setMACCODE(""); /* 訊息押碼 */
					} else {
						atm_faa_cc1apc.setMACCODE(rfs.get()); /* 訊息押碼 */
					}
					this.logContext.setRemark("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
					rtnMessage = atm_faa_cc1apc.makeMessage();
					break;
				}
			} else {
				switch (feptxn.getFeptxnTxCode()) {
				case "US": // 外幣提款交易
				case "JP": // 外幣提款交易
					ATM_FAA_CC1B3PN atm_faa_cc1b3pn = new ATM_FAA_CC1B3PN();
					// 組 Header
					atm_faa_cc1b3pn.setWSID(atmReq.getWSID());
					atm_faa_cc1b3pn.setRECFMT("1");
					atm_faa_cc1b3pn.setMSGCAT("F");
					atm_faa_cc1b3pn.setMSGTYP("PN"); // - response
					atm_faa_cc1b3pn.setTRANDATE(atmReq.getTRANDATE());
					atm_faa_cc1b3pn.setTRANTIME(atmReq.getTRANTIME());
					atm_faa_cc1b3pn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1b3pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b3pn.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

					// 組S0明細表內容(PRINT message),依交易下送欄位,電文總長度也不同
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
					atm_faa_cc1b3pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
					// 帳戶餘額，格式: $$$,$$$,$$$,$$9.99 ex :$11,282,203.00
					atm_faa_cc1b3pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnBalb(), "$#,##0.00"),18," "));
					atm_faa_cc1b3pn.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1b3pn.setPSTAN(feptxn.getFeptxnStan());
					//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
					atm_faa_cc1b3pn.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
					atm_faa_cc1b3pn.setPRCCODE(feptxn.getFeptxnReplyCode());
					// 轉出行
					atm_faa_cc1b3pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 提領外幣
					atm_faa_cc1b3pn.setPEXRATE(Objects.toString(feptxn.getFeptxnExrate()));
					atm_faa_cc1b3pn.setPAMT(Objects.toString(feptxn.getFeptxnTxAmt()));
					// 處理有收到CBS Response的欄位值
					if (tota != null) {
						//交易種類
						atm_faa_cc1b3pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
						//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼，ex：123456***0
						String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
						if (StringUtils.isNotBlank(FROMACT)) {
							FROMACT = FROMACT.trim();
							int len_F = FROMACT.length();
							if (len_F <= 6) {
							} else {
								if (len_F > 9) {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
								} else {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
								}
							}
						}
						atm_faa_cc1b3pn.setPACCNO(FROMACT);
						//轉出行
						atm_faa_cc1b3pn.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
						atm_faa_cc1b3pn.setPTMEXNO(this.getImsPropertiesValue(tota,ImsMethodName.FWDTMEX_NO.getValue()));
					}else {
						//交易種類
						atm_faa_cc1b3pn.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
						//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼，ex：123456***0
						String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
						if (StringUtils.isNotBlank(FROMACT)) {
							FROMACT = FROMACT.trim();
							int len_F = FROMACT.length();
							if (len_F <= 6) {
							} else {
								if (len_F > 9) {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
								} else {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
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
								getTxData().getLogContext()));
						atm_faa_cc1b3pn.setMACCODE(""); /* 訊息押碼 */
					} else {
						atm_faa_cc1b3pn.setMACCODE(rfs.get()); /* 訊息押碼 */
					}
					this.logContext.setRemark("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
					rtnMessage = atm_faa_cc1b3pn.makeMessage();
					break;
				default: // 其他提款交易
					ATM_FAA_CC1B1PN atm_faa_cc1b1pn = new ATM_FAA_CC1B1PN();
					// 組 Header
					atm_faa_cc1b1pn.setWSID(atmReq.getWSID());
					atm_faa_cc1b1pn.setRECFMT("1");
					atm_faa_cc1b1pn.setMSGCAT("F");
					atm_faa_cc1b1pn.setMSGTYP("PN"); // + response
					atm_faa_cc1b1pn.setTRANDATE(atmReq.getTRANDATE());
					atm_faa_cc1b1pn.setTRANTIME(atmReq.getTRANTIME());
					atm_faa_cc1b1pn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1b1pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b1pn.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

					// 組S0明細表內容(PRINT message),依交易下送欄位,電文總長度也不同
					atm_faa_cc1b1pn.setPTYPE("S0");
					atm_faa_cc1b1pn.setPLEN("191");
					atm_faa_cc1b1pn.setPBMPNO("000010"); // FPN
					// 西元年轉民國年
					atm_faa_cc1b1pn.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1b1pn.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_cc1b1pn.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1b1pn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					// 交易成功才顯示手續費，格式:$999 ex :$0
					atm_faa_cc1b1pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
					// 帳戶餘額，格式: $$$,$$$,$$$,$$9.99 ex :$11,282,203.00
					atm_faa_cc1b1pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxn.getFeptxnBalb(), "$#,##0.00"),18," "));
					atm_faa_cc1b1pn.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1b1pn.setPSTAN(feptxn.getFeptxnStan());
					//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
					atm_faa_cc1b1pn.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
					atm_faa_cc1b1pn.setPRCCODE(feptxn.getFeptxnReplyCode());
					// 轉出行
					atm_faa_cc1b1pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					if ("WP".equals(feptxnTxCode)) {
						atm_faa_cc1b1pn.setPTRINACCT(feptxntcb.getFeptxntcbHealthcard());
					}
					// 處理有收到CBS Response的欄位值
					if (tota != null) {
						//交易種類
						atm_faa_cc1b1pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
						//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼，ex：123456***0
						String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
						if (StringUtils.isNotBlank(FROMACT)) {
							FROMACT = FROMACT.trim();
							int len_F = FROMACT.length();
							if (len_F <= 6) {
							} else {
								if (len_F > 9) {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
								} else {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
								}
							}
						}
						atm_faa_cc1b1pn.setPACCNO(FROMACT);
						//轉出行
						atm_faa_cc1b1pn.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
					}else {
						//交易種類
						atm_faa_cc1b1pn.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
						//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼，ex：123456***0
						String FROMACT = StringUtils.trim(feptxn.getFeptxnTroutActno());
						if (StringUtils.isNotBlank(FROMACT)) {
							FROMACT = FROMACT.trim();
							int len_F = FROMACT.length();
							if (len_F <= 6) {
							} else {
								if (len_F > 9) {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3) + FROMACT.substring(9);
								} else {
									FROMACT = FROMACT.substring(0, 6) + StringUtils.repeat("*", 3);
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
								getTxData().getLogContext()));
						atm_faa_cc1b1pn.setMACCODE(""); /* 訊息押碼 */
					} else {
						atm_faa_cc1b1pn.setMACCODE(rfs.get()); /* 訊息押碼 */
					}
					this.logContext.setRemark("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
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
