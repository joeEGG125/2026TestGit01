package com.syscom.fep.server.aa.atmp;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.mybatis.ext.mapper.UcdidExtMapper;
import com.syscom.fep.mybatis.ext.model.CashTOTARequestExt;
import com.syscom.fep.mybatis.mapper.UcdidMapper;
import com.syscom.fep.mybatis.model.CashTOTA;
import com.syscom.fep.mybatis.model.Ucdid;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B1PN;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.FIDOAdapter;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_FIDO_CC1B1PN;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
import com.syscom.fep.vo.text.fido.request.FidoRequest;
import com.syscom.fep.vo.text.fido.response.FidoResponse;
import org.springframework.web.client.RestTemplate;


public class VASelfIssue extends ATMPAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode1 = FEPReturnCode.Normal;
	private String atmno;
	private String fidoTota = StringUtils.EMPTY;
	private boolean isGarbageR = false;
	private Vatxn vatxn = new Vatxn();
	private CashTOTA cashTOTA = new CashTOTA();
	private FidoResponse fidoResponse = null;
	private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
	private UcdidMapper ucdidMapper = SpringBeanFactoryUtil.getBean(UcdidMapper.class);
	private UcdidExtMapper ucdidExtMapper = SpringBeanFactoryUtil.getBean(UcdidExtMapper.class);

	public VASelfIssue(ATMData txnData) throws Exception {
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
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
				feptxn.setFeptxnAaRc(rtnCode.getValue());
				// GO TO 7    /* 更新交易記錄 */
			}
			
			if (rtnCode == FEPReturnCode.Normal) {
				// 5. SendToCBS:送往CBS主機處理
				this.sendToCBS();
			}

			if (rtnCode == FEPReturnCode.Normal) {
			// 6. 全民普發交易提領註記
			this.sendToCASH();
			}

			// 7. UpdateTxData: 更新交易記錄(FEPTxn)
			this.updateTxData();
			if (feptxn.getFeptxnCbsTimeout() == 1) {  // HostResponseTimeout
				getLogContext().setProgramName(ProgramName + ".updateTxData");
				getLogContext().setRemark("HostResponseTimeout");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			}

			// 8. Insert VATXN: 新增金融帳戶核驗紀錄檔
			this.insertVATXN();
			
			// 9. 核驗成功，送財金FIDO平台登錄
			this.sendToFIDO();
			
			if(!isGarbageR) {
				// 10. Response:組ATM回應電文 & 回 ATMMsgHandler
				if (StringUtils.isBlank(getTxData().getTxResponseMessage())) {
					rtnMessage = this.response();
				} else {
					rtnMessage = getTxData().getTxResponseMessage();
				}
			}else {
				// 11. GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler
				rtnMessage = this.garbageResponse();
			}
			
		} catch (Exception ex) {
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage("MessageToATM:" + rtnMessage);
			getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			getTxData().getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode,getLogContext()));
			logMessage(getTxData().getLogContext());
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
		
		rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno, StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 36, 742), ATMMAC);
        
		this.logContext.setRemark("after checkAtmMac RC:" + rtnCode.toString());
        logMessage(this.logContext);
        
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO 4    /* FEP檢核錯誤處理*/
		}
	}

	/**
	 * 5. SendToCBS:送往CBS主機處理
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		String IMS_RC4_FISC = null;
		/* 交易記帳處理 */
		String AATxTYPE = "0"; //上CBS查詢
		String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
		feptxn.setFeptxnCbsTxCode(AA);
		ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
		rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
		tota = hostAA.getTota();
		IMS_RC4_FISC = feptxntcb.getFeptxntcbImsrc4Fisc();
        if(feptxn.getFeptxnCbsTimeout() == 1) { // HostResponse Timeout
			feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
		} else {
			if ( IMS_RC4_FISC != null && !("4001").equals(IMS_RC4_FISC) ) {
				feptxn.setFeptxnCbsRc(IMS_RC4_FISC);
			}
        	feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
		}
        feptxn.setFeptxnAaRc(rtnCode.getValue());
	}

	/**
	 * 6. 全民普發交易提領註記
	 */
	private void sendToCASH() throws Exception {
		getLogContext().setProgramName(ProgramName + ".sendToCASH");
//		ATMGeneralRequest atmReq = getTxData().getTxObject().getRequest();
//		ATMGeneralRequest atmReq = this.getATMRequest();
		String URL = ATMPConfig.getInstance().getCashDistributionUrl();
		if (("TT").equals(feptxn.getFeptxnActivityCode())) {
			// 1. 建立請求主體物件
			CashTOTARequestExt requestBody = new CashTOTARequestExt();
			requestBody.setType("1"); //提領註記
			requestBody.setAgentBank(SysStatus.getPropertyValue().getSysstatHbkno()); //發動行
			requestBody.setTxnTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))); //發送請求時間
			requestBody.setSeqNo(StringUtils.right(requestBody.getTxnTime(),10) + feptxn.getFeptxnBkno() + feptxn.getFeptxnStan()); //交易序號(唯一)
			requestBody.setIdn(feptxn.getFeptxnIdno().trim());	//身分證號
			requestBody.setHealthId(feptxntcb.getFeptxntcbHealthcard()); //健保卡卡號
			requestBody.setChannelType("10"); //ATM提領
			feptxntcb.setFeptxntcbUcdApiseqno(requestBody.getSeqNo()); //Call全民普發平台交易序號

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
			try {
				// 如果是 Https 設定 TLS1.2
				if (HttpClient.isHttps(URL)) {
					restTemplate.setRequestFactory(HttpClientConfiguration.createTrustAnyHttpComponentsClientHttpRequestFactory("TLSv1.2", timeout * 1000));
				} else {
					restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(timeout * 1000));
				}
				ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, String.class);

				if (response.getBody() == null) {
					// 修改以下四項參數，再呼叫一次API
					requestBody.setType("3"); //提領註記查詢
					requestBody.setTxnTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))); //發送請求時間
					requestBody.setSeqNo(StringUtils.right(requestBody.getTxnTime(),10) + feptxn.getFeptxnBkno() + feptxn.getFeptxnStan()); //交易序號(唯一)
					requestBody.setOrgSeqNo(feptxntcb.getFeptxntcbUcdApiseqno()); //被查詢交易之序號(查詢必填)
					requestEntity = new HttpEntity<>(requestBody, headers);
					this.logContext.setRemark("再呼叫一次API");
					this.logContext.setMessage("Request Body: " + requestBody.toString());
					logMessage(this.logContext);
					response = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, String.class);
					if (response.getBody() == null) {
						rtnCode1 = FEPReturnCode.UCDResponseTimeout; //API回覆逾時
						feptxn.setFeptxnAaRc(rtnCode1.getValue()); //10210
						this.logContext.setRemark("API回傳值為null");
						logMessage(this.logContext);
						return; // GO TO 7    /* 更新交易記錄 */
					}else{
						String responseBody = response.getBody();
						this.logContext.setMessage("API回傳值為: " + responseBody);
						this.logContext.setRemark("Call API Success");
						logMessage(this.logContext);
						ObjectMapper objectMapper = new ObjectMapper();
						this.cashTOTA = objectMapper.readValue(responseBody, CashTOTA.class);
						feptxntcb.setFeptxntcbUcdTxno(cashTOTA.getOrgTxnNo()); //普發平台回應的交易序號
						feptxntcb.setFeptxntcbUcdResptime(cashTOTA.getOrgRespTime()); //普發平台的回應時間
						feptxntcb.setFeptxntcbUcdRc(cashTOTA.getOrgReturnCode()); //普發平台的回應代碼
						if (!("00000").equals(this.cashTOTA.getOrgReturnCode())) { //交易失敗
							rtnCode1 = FEPReturnCode.custom(this.cashTOTA.getOrgReturnCode());	//直接取FISC代碼
							feptxn.setFeptxnAaRc(10221); //FEPReturnCode.UCDResponseError
							this.logContext.setRemark("API回傳值不正確 ReturnCode: " + this.cashTOTA.getOrgReturnCode());
							logMessage(this.logContext);
						}else {
							this.logContext.setRemark("ReturnCode: " + this.cashTOTA.getReturnCode());
							logMessage(this.logContext);
						}
					}
				}else {
					String responseBody = response.getBody();
					this.logContext.setMessage("API回傳值為: " + responseBody);
					this.logContext.setRemark("Call API Success");
					logMessage(this.logContext);
					ObjectMapper objectMapper = new ObjectMapper();
					this.cashTOTA = objectMapper.readValue(responseBody, CashTOTA.class);
					feptxntcb.setFeptxntcbUcdTxno(cashTOTA.getTxnNo()); //普發平台回應的交易序號
					feptxntcb.setFeptxntcbUcdResptime(cashTOTA.getRespTime()); //普發平台的回應時間
					feptxntcb.setFeptxntcbUcdRc(cashTOTA.getReturnCode()); //普發平台的回應代碼
					if (!("00000").equals(this.cashTOTA.getReturnCode())) { //交易失敗
						rtnCode1 = FEPReturnCode.custom(this.cashTOTA.getReturnCode());	//直接取FISC代碼
						feptxn.setFeptxnAaRc(10221); //FEPReturnCode.UCDResponseError
						this.logContext.setRemark("API回傳值不正確 ReturnCode: " + this.cashTOTA.getReturnCode());
						logMessage(this.logContext);
					}
				}

				//將此IDNO的UCDID 當前交易更新為歷史交易
				try{
					int count = ucdidExtMapper.updateHistory(feptxn.getFeptxnIdno().trim(),feptxntcb.getFeptxntcbHealthcard());
				}catch (Exception e){
					rtnCode1 = FEPReturnCode.UpdateFail;
					getLogContext().setProgramException(e);
					getLogContext().setProgramName(ProgramName + ".updateUCDID");
					sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				}

				//新增全民普發個人紀錄
				Ucdid ucdid = new Ucdid();
				ucdid.setUcdidIdno(feptxn.getFeptxnIdno().trim());
				ucdid.setUcdidHealthid(feptxntcb.getFeptxntcbHealthcard());
				ucdid.setUcdid2566Txdate(feptxn.getFeptxnTxDate());
				ucdid.setUcdid2566Stan(feptxn.getFeptxnStan());
				ucdid.setUcdid2566Txtime(feptxn.getFeptxnTxTime());
				ucdid.setUcdid2566TbsdyFisc(feptxn.getFeptxnTbsdyFisc());
				ucdid.setUcdid2566Tbsdy(feptxn.getFeptxnTbsdy());
				if(StringUtils.isBlank(feptxntcb.getFeptxntcbUcdRc())){
					ucdid.setUcdid2566Apistatus("A"); //提領註記異常(未收到API RC)
				} else if (("00000").equals(feptxntcb.getFeptxntcbUcdRc())) {
					ucdid.setUcdid2566Apistatus("1"); //已註記提領
				}
				ucdid.setUcdidTroutBkno(feptxn.getFeptxnTroutBkno());
				ucdid.setUcdidTroutActno(feptxn.getFeptxnTroutActno());
				ucdid.setUcdidBkno(feptxn.getFeptxnBkno());
				ucdid.setUcdidAtmno(feptxn.getFeptxnAtmno());
				ucdid.setUcdidUcdApiseqno(feptxntcb.getFeptxntcbUcdApiseqno());
				ucdid.setUcdidUcdTxno(feptxntcb.getFeptxntcbUcdTxno());
				ucdid.setUcdidUcdResptime(feptxntcb.getFeptxntcbUcdResptime());
				ucdid.setUcdidUcdRc(feptxntcb.getFeptxntcbUcdRc());
				ucdid.setUcdidLaststatus("F"); //身分驗證
				ucdid.setUcdidHistory("N");
				ucdid.setUpdateTime(new Date());

				//更新全民普發個人紀錄
				try {
					int count = ucdidMapper.insert(ucdid);
					if (count <= 0) {
						throw new Exception();
					}
				}catch (Exception e){
					rtnCode1 = FEPReturnCode.InsertFail;
					getLogContext().setProgramException(e);
					getLogContext().setProgramName(ProgramName + ".insertUCDID");
					sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				}
			} catch (Exception e) {
				//呼叫API失敗請給ErrCode：UCDAPIError(14041)
				rtnCode1 = FEPReturnCode.UCDAPIError;
				getLogContext().setProgramException(e);
				getLogContext().setProgramName(ProgramName + ".sendToCASH");
				sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				this.logContext.setRemark("UCDAPIError: " + e.getMessage());
				logMessage(this.logContext);
				// GO TO 7    /* 更新交易記錄 */
			}
		}
	}

	/**
	 * 7. UpdateTxData: 更新交易記錄(FEPTxn)
	 */
	private void updateTxData() {
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); // (RESPONSE)
		feptxn.setFeptxnAaComplete((short) 1); /* AA Complete */
		/* For報表, 寫入處理結果 */
		if (rtnCode == FEPReturnCode.Normal) {
			feptxn.setFeptxnTxrust("A"); /* 處理結果=成功 */
			if (feptxn.getFeptxnActivityCode().equals("TT")) {
				if (rtnCode1 != FEPReturnCode.Normal) {
					feptxn.setFeptxnTxrust("R"); /* 處理結果=Reject */
					if (rtnCode1 == FEPReturnCode.UpdateFail || rtnCode1 == FEPReturnCode.InsertFail){
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(FEPReturnCode.toString(rtnCode1), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					}else if (StringUtils.isNotBlank(this.cashTOTA.getReturnCode()) && !("00000").equals(this.cashTOTA.getReturnCode())){
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(FEPReturnCode.toString(rtnCode1), FEPChannel.FISC, FEPChannel.ATM, getTxData().getLogContext()));
					}else{
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(FEPReturnCode.toString(rtnCode1), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					}
					this.logContext.setRemark("ReplyCode: " + feptxn.getFeptxnReplyCode() + ", rtnCode: " + FEPReturnCode.toString(rtnCode1));
					logMessage(this.logContext);
				}
				rtnCode = rtnCode1;
			}
		} else {
			feptxn.setFeptxnTxrust("R"); /* 處理結果=Reject */
		}

		// 兩個Table 同步更新，如有任何錯誤，請ROLLBACK & 寫EMS
		try {
			int updateCount = feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
			int updateCount2 = feptxnDao.updateByPrimaryKeySelective(this.feptxntcb);
			if (updateCount <= 0 || updateCount2 <= 0) { // 更新失敗
				throw new Exception(); //2025.07.15 Transaction call review 調整
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			this.feptxn.setFeptxnReplyCode("T452"); //FEPTXNUpdateError
			this.rtnCode = FEPReturnCode.FEPTXNUpdateError;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateTxData");
			getLogContext().setRemark("FEPTXN UPDATE ERROR");
			sendEMS(getLogContext());
		}
		
		// 電文被主機視為garbage時(所有電文)，只傳送HEAD 給ATM
		String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
		String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());

		if ("XXXX".equals(IMSRC4_FISC) && "XXX".equals(IMSRC_TCB)) {
			isGarbageR = true; // GO TO 10 /*組GarbageResponse回覆ATM */
		}
	}

	/**
	 * 8.	Insert VATXN: 新增金融帳戶核驗紀錄檔
	 */
	private void insertVATXN() {
		vatxn.setVatxnTxDate(feptxn.getFeptxnTxDate());
		vatxn.setVatxnEjfno(feptxn.getFeptxnEjfno());
		vatxn.setVatxnBkno(feptxn.getFeptxnBkno());
		vatxn.setVatxnStan(feptxn.getFeptxnStan());
		vatxn.setVatxnPcode(feptxn.getFeptxnPcode());
		vatxn.setVatxnTxTime(feptxn.getFeptxnTxTime());
		vatxn.setVatxnTbsdyFisc(feptxn.getFeptxnTbsdyFisc());
		vatxn.setVatxnTxrust(feptxn.getFeptxnTxrust());
		vatxn.setVatxnTroutBkno(feptxn.getFeptxnTroutBkno());
		vatxn.setVatxnTroutActno(feptxn.getFeptxnTroutActno());
		vatxn.setVatxnBrno(feptxn.getFeptxnBrno());
		vatxn.setVatxnCate("10"); /* 業務類別- FIDO金融卡核驗服務 */
		vatxn.setVatxnType("00"); /* 交易類別-晶片卡核驗 */
		vatxn.setVatxnItem("01"); /* 核驗項目-卡片及帳號 */
		vatxn.setVatxnUse("01");  /* 核驗用途-金融FIDO */
		vatxn.setVatxnIdno(feptxn.getFeptxnIdno()); /*身分證號 */
		if(StringUtils.isNotBlank(feptxn.getFeptxnRemark())) {
			vatxn.setVatxnResult(feptxn.getFeptxnRemark().substring(0, 2));
			vatxn.setVatxnAcresult(feptxn.getFeptxnRemark().substring(2, 4));
			vatxn.setVatxnAcstat(feptxn.getFeptxnRemark().substring(4, 6));
		}
		try {
			vatxnMapper.insertSelective(vatxn);
		} catch (Exception e) {
			getLogContext().setProgramException(e);
			getLogContext().setProgramName(ProgramName + ".insertVATXN");
			getLogContext().setRemark("Insert VATXN Error");
			sendEMS(getLogContext());
		}
		
	}

	/**
	 * 9. 核驗成功，送財金FIDO平台登錄
	 *
	 * @throws Exception
	 */
	private FEPReturnCode sendToFIDO() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		if(StringUtils.equals(feptxn.getFeptxnTxrust(), "A") && StringUtils.equals(feptxn.getFeptxnActivityCode(), "FD")) {
			FIDOAdapter adapter;
			// API Request Body
			String fidoBodyStr = this.FIDOBodyJsonStr();
			
			try {
				adapter = new FIDOAdapter(this.getTxData());
				adapter.setMessageToFIDO(fidoBodyStr);
				rtnCode = adapter.sendReceive();
				if(rtnCode != FEPReturnCode.Normal) {
					feptxn.setFeptxnTxrust("R");
					feptxn.setFeptxnAaRc(rtnCode.getValue());
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					try {
						feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
					} catch (Exception ex) {
						getLogContext().setProgramName(ProgramName + ".sendToFIDO");
			            getLogContext().setRemark("FEPTXN UPDATE ERROR");
						sendEMS(getLogContext());
					}
				}else {
					fidoTota = adapter.getMessageFromFIDO();
					this.logContext.setRemark("MessageFromFIDO");
					this.logContext.setMessage("MessageFromFIDO:" + fidoTota);
			        logMessage(this.logContext);
			        fidoResponse = new FidoResponse(fidoTota);
			        Integer code = fidoResponse.getCode();
			        String statusCode;
			        if(code != null) {
			        	if(code != 0) {
				        	if(code == 1 && StringUtils.isNoneBlank(fidoResponse.getStatusCode())) {
				        		statusCode = fidoResponse.getStatusCode();
				        	}else {
				        		statusCode = String.valueOf(code);
				        	}
				        	feptxn.setFeptxnTxrust("R");
				        	feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(statusCode), FEPChannel.FID, FEPChannel.ATM, getTxData().getLogContext()));
				        	
			        	}
			        }
					// FIDO平台回傳的QRCODE有效時間
			        if(StringUtils.isNotBlank(fidoResponse.getExpireDateTime())) {
			        	feptxntcb.setFeptxntcbQrexptime(fidoResponse.getExpireDateTime());
			        	try {
							feptxnDao.updateByPrimaryKeySelective(this.feptxntcb); // 更新資料
						} catch (Exception ex) {
							getLogContext().setProgramName(ProgramName + ".sendToFIDO");
				            getLogContext().setRemark("FEPTXNTCB UPDATE ERROR");
							sendEMS(getLogContext());
						}
			        }
			        try {
						feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
					} catch (Exception ex) {
						getLogContext().setProgramName(ProgramName + ".sendToFIDO");
			            getLogContext().setRemark("FEPTXN Reply Code UPDATE ERROR");
						sendEMS(getLogContext());
					}
				}
			} catch (Exception e) {
				getLogContext().setProgramName(ProgramName + ".sendToFIDO");
	            getLogContext().setRemark("sendToFIDO Error.");
				sendEMS(getLogContext());
			}
		}
		
		return rtnCode;
	}
	
	/**
	 * 組成body的Json字串
	 * @return
	 */
	private String FIDOBodyJsonStr() {
		try {
			// 取傳FIDO的Body參數
			String acqBank = feptxn.getFeptxnBkno() + feptxn.getFeptxnAtmno().substring(1,5); //代理行
			String issBank = feptxn.getFeptxnTroutBkno() + feptxn.getFeptxnBrno(); //發卡行+分行
			String stan = feptxn.getFeptxnStan(); // 交易序號
			String txDateTime = feptxn.getFeptxnReqDatetime(); // Request日期時間
			String verifyType = "01"; // 核驗用途-金融FIDO
			String verifyResult = vatxn.getVatxnResult();
			String accVerifyResult = vatxn.getVatxnAcresult();
			String accOpenStatus = vatxn.getVatxnAcstat();
			String terminalType = feptxn.getFeptxnAtmType();
			String id = getATMBusiness().encryptString(feptxn.getFeptxnIdno(), issBank, stan, txDateTime); // 用AES-GCM加密
			String fidoApply = "1"; // 財金定義之公版格式
			String txAccount = StringUtils.leftPad(feptxntcb.getFeptxntcbFromact(), 16, "0"); // 右靠左補0
			String machine = feptxn.getFeptxnAtmno();

			FidoRequest fr = new FidoRequest();
			fr.setAcqBank(acqBank);
			fr.setIssBank(issBank);
			fr.setStan(stan);
			fr.setTxDateTime(txDateTime);
			fr.setVerifyType(verifyType);
			fr.setVerifyResult(verifyResult);
			fr.setAccVerifyResult(accVerifyResult);
			fr.setAccOpenStatus(accOpenStatus);
			fr.setTerminalType(terminalType);
			fr.setId(id);
			fr.setFidoApply(fidoApply);
			fr.setTxAccount(txAccount);
			fr.setMachine(machine);
			return fr.toJsonString();
		} catch (Exception e) {
			getLogContext().setProgramException(e);
			sendEMS(getLogContext());
			return StringUtils.EMPTY;
		}
	}

	/**
	 * 10. Response:組ATM回應電文 & 回 ATMMsgHandler
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
			if (rtnCode != FEPReturnCode.Normal || StringUtils.equals(feptxn.getFeptxnTxrust(), "R")) {
				ATM_FAA_CC1APC response = new ATM_FAA_CC1APC();
				// 組Header
				response.setWSID(atmReq.getWSID());
				response.setRECFMT("1");
				response.setMSGCAT("F");
				response.setMSGTYP("PC"); // - response
				response.setTRANDATE(atmReq.getTRANDATE());
				response.setTRANTIME(atmReq.getTRANTIME());
				response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				response.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				response.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

				// 組 D0(004) 畫面顯示(Display message)
				response.setDATATYPE("D0");
				response.setDATALEN("004");
				response.setACKNOW("0");
				
                String pageNo;
                //此欄給主機回應的代碼，尚未走到主機就給空值
                if (StringUtils.isBlank(feptxn.getFeptxnCbsRc())) { //交易尚未送主機
					pageNo = "226"; //主機代碼
				} else {
					pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM, this.getTxData().getLogContext());
					if (StringUtils.equals(pageNo, "2999")) {
						pageNo = "226";
					}
				}
                
                response.setPAGENO(pageNo);

				// 組S0 明細表內容(PRINT message),依交易下送欄位, 電文總長度也不同
				response.setPTYPE("S0");
				response.setPLEN("191");
				response.setPBMPNO("010000"); // FPC
				// 西元年轉民國年
				response.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
				response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
				response.setPTID(feptxn.getFeptxnAtmno());
				//交易金額
				response.setPTXAMT(StringUtils.leftPad("", 11, " "));
				//交易手續費
				response.setPFEE(StringUtils.leftPad("", 4, " "));
				//帳戶餘額
				response.setPBAL(StringUtils.leftPad("", 18, " "));
				//代理行
				response.setPATXBKNO(feptxn.getFeptxnBkno());
				// 交易序號
				response.setPSTAN(feptxn.getFeptxnStan());
				//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
				response.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
				//ATM回應代碼(CBSProcess已依主機下送規則處理)
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
				// 轉出行
				response.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				
				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼
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
					response.setPACCNO(FROMACT);
				}else {
					//交易種類
					response.setPTXTYPE(feptxn.getFeptxnTxCode()); //20250805主機確認下送此值為FSCODE
					//轉出帳號(明細表顯示內容)，合庫帳號10位第7~9位隱碼 
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
					response.setPACCNO(FROMACT);
				}
				
				rfs.set("");
				rtnMessage = response.makeMessage();
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				
				this.logContext.setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
				logMessage(this.logContext);
				
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				rtnMessage = response.makeMessage();
			} else if (feptxn.getFeptxnActivityCode().equals("FD")) { //FIDO 身分驗證
				ATM_FAA_FIDO_CC1B1PN response = new ATM_FAA_FIDO_CC1B1PN();
				// 組Header(OUTPUT-1)
				response.setWSID(atmReq.getWSID());
				response.setRECFMT("1");
				response.setMSGCAT("F");
				response.setMSGTYP("PN"); // + response
				response.setTRANDATE(atmReq.getTRANDATE());
				response.setTRANTIME(atmReq.getTRANTIME());
				response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				response.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				response.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

				// 組S0 明細表內容(PRINT message),依交易下送欄位, 電文總長度也不同
				response.setPTYPE("S0");
				response.setPLEN("191");
				response.setPBMPNO("000010"); // FPN
				// 西元年轉民國年
				response.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
				response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
				response.setPTID(feptxn.getFeptxnAtmno());
				//交易金額
				response.setPTXAMT(StringUtils.leftPad("", 11, " "));
				//交易手續費
				response.setPFEE(StringUtils.leftPad("", 4, " "));
				//帳戶餘額
				response.setPBAL(StringUtils.leftPad("", 18, " "));
				//代理行
				response.setPATXBKNO(feptxn.getFeptxnBkno());
				// 交易序號
				response.setPSTAN(feptxn.getFeptxnStan());
				//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
				response.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
				//ATM回應代碼(CBSProcess已依主機下送規則處理)
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
				// 轉出行
				response.setPOTXBKNO(feptxn.getFeptxnTroutBkno());

				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼
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
					response.setPACCNO(FROMACT);
				}else {
					//交易種類
					response.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
					//轉出帳號(明細表顯示內容)，合庫帳號10位第7~9位隱碼 
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
					response.setPACCNO(FROMACT);
				}
				
				rfs.set("");
				rtnMessage = response.makeMessage();
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				
				this.logContext.setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
				logMessage(this.logContext);
				
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				//FIDO 回覆成功，需多組兩個欄位(QRCODE有效日期及QRCODE)
				if(StringUtils.equals(feptxn.getFeptxnTxrust(), "A") && StringUtils.equals(feptxn.getFeptxnActivityCode(), "FD")) {
					if(fidoResponse != null) {
						response.setEXPIREDATE(fidoResponse.getExpireDateTime());
						response.setQRCODE(fidoResponse.getQrContent());
						//QRCODE String為變動長度，需取實際長度值下送ATM，待下送電文轉EBCDIC完成，需再作如下處理
						int w_len = (500 - fidoResponse.getQrContent().trim().length()) * 2;
						int w_totlen = response.makeMessage().length() - w_len;
						rtnMessage = response.makeMessage().substring(0,w_totlen);
					}
				}else{
					rtnMessage = response.makeMessage();
				}
			}else {
				ATM_FAA_CC1B1PN response = new ATM_FAA_CC1B1PN();
				// 組Header(OUTPUT-1)
				response.setWSID(atmReq.getWSID());
				response.setRECFMT("1");
				response.setMSGCAT("F");
				response.setMSGTYP("PN"); // + response
				response.setTRANDATE(atmReq.getTRANDATE());
				response.setTRANTIME(atmReq.getTRANTIME());
				response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				response.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				response.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

				// 組S0 明細表內容(PRINT message),依交易下送欄位, 電文總長度也不同
				response.setPTYPE("S0");
				response.setPLEN("191");
				response.setPBMPNO("000010"); // FPN
				// 西元年轉民國年
				response.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
				response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
				response.setPTID(feptxn.getFeptxnAtmno());
				//交易金額
				response.setPTXAMT(StringUtils.leftPad("", 11, " "));
				//交易手續費
				response.setPFEE(StringUtils.leftPad("", 4, " "));
				//帳戶餘額
				response.setPBAL(StringUtils.leftPad("", 18, " "));
				//代理行
				response.setPATXBKNO(feptxn.getFeptxnBkno());
				// 交易序號
				response.setPSTAN(feptxn.getFeptxnStan());
				//CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
				response.setPBUSINESSDATE(this.formatDate(feptxn.getFeptxnTbsdy()).substring(1));
				//ATM回應代碼(CBSProcess已依主機下送規則處理)
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
				// 轉出行
				response.setPOTXBKNO(feptxn.getFeptxnTroutBkno());

				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)，合庫序號10位第7~9位隱碼
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
					response.setPACCNO(FROMACT);
				}else {
					//交易種類
					response.setPTXTYPE(feptxntcb.getFeptxntcbTxntypeCode());
					//轉出帳號(明細表顯示內容)，合庫帳號10位第7~9位隱碼
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
					response.setPACCNO(FROMACT);
				}

				rfs.set("");
				rtnMessage = response.makeMessage();
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);

				this.logContext.setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
				logMessage(this.logContext);

				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}

				rtnMessage = response.makeMessage();
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}
	
	// 10. GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler 
	private String garbageResponse() {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
            // 組 Header
            ATMGeneralRequest atmReq = this.getATMRequest();
            
            ATM_FSN_HEAD2 response = new ATM_FSN_HEAD2();
            response.setWSID(atmReq.getWSID());
            response.setRECFMT("1");
            response.setMSGCAT("F");
            response.setMSGTYP("PC");
            response.setTRANDATE(atmReq.getTRANDATE());
            response.setTRANTIME(atmReq.getTRANTIME());
            response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
            response.setTDRSEG(atmReq.getTDRSEG()); // 回覆FSN或FSE
            // PRCRDACT = 0 或4都是未留置卡片, 2 是吃卡, 只有磁條密碼變更交易
            // (FC1、P1)主機才有可能依據狀況要求吃卡
            response.setPRCRDACT("0");

            /* CALL ENC 取得MAC 資料 */
            ENCHelper atmEncHelper = new ENCHelper(this.getTxData());
            RefString rfs = new RefString();
            
            rfs.set("");
            rtnMessage = response.makeMessage();
            
			rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
			
            this.logContext.setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
            logMessage(this.logContext);
			
			if (rtnCode != FEPReturnCode.Normal) {
				response.setMACCODE(""); /* 訊息押碼 */
			} else {
				response.setMACCODE(rfs.get()); /* 訊息押碼 */
			}
			
            rtnMessage = response.makeMessage();
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
