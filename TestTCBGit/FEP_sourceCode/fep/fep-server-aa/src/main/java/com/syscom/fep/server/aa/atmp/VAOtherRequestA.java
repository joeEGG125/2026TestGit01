package com.syscom.fep.server.aa.atmp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.mybatis.ext.mapper.UcdidExtMapper;
import com.syscom.fep.mybatis.ext.model.CashTOTARequestExt;
import com.syscom.fep.mybatis.mapper.UcdidMapper;
import com.syscom.fep.mybatis.model.CashTOTA;
import com.syscom.fep.mybatis.model.Ucdid;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B1PN;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
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
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.FIDOAdapter;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_FIDO_CC1B1PN;
import com.syscom.fep.vo.text.fido.request.FidoRequest;
import com.syscom.fep.vo.text.fido.response.FidoResponse;
import org.springframework.web.client.RestTemplate;

/**
 * @author 
 */
public class VAOtherRequestA extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode1 = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;
	private String atmno;
	private String fidoTota = StringUtils.EMPTY;
	private Vatxn vatxn = new Vatxn();
	private CashTOTA cashTOTA = new CashTOTA();
	private FidoResponse fidoResponse = null;
	private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
	private UcdidMapper ucdidMapper = SpringBeanFactoryUtil.getBean(UcdidMapper.class);
	private UcdidExtMapper ucdidExtMapper = SpringBeanFactoryUtil.getBean(UcdidExtMapper.class);
    private Integer fepfail = 0;		//預設FEP處理成功
    private Integer fiscfail = 0;		//預設FISC處理成功
	private Integer cashfail = 0;       //預設提領註記處理成功
	private Integer vacheck = 0;        //預設對方行核驗成功
	private Integer atmrepfpc = 1; 		//預設下送ATM FPC
	private String vatxnrc = StringUtils.EMPTY;

	public VAOtherRequestA(ATMData txnData) throws Exception {
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

			FISC fiscBusiness = getFiscBusiness();
			if (rtnCode == FEPReturnCode.Normal) {
				// 4. 組送往 FISC 之 Request 電文並等待財金之 Response 電文
				rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
				if (rtnCode != FEPReturnCode.Normal) {
					fepfail = 1;       
					 //GO TO  7    /* 更新交易紀錄 */
				}
			}

			boolean repRcEq4001 = false;
			if (rtnCode == FEPReturnCode.Normal) {
				// 5. CheckResponseFromFISC:檢核回應電文是否正確
				rtnCode = fiscBusiness.checkResponseMessage();
				getLogContext().setProgramName(ProgramName + ".checkResponseMessage");
				getLogContext().setRemark("after checkResponseMessage RepRc:" + feptxn.getFeptxnRepRc());
				logMessage(getLogContext());

				if (StringUtils.equals("4001", feptxn.getFeptxnRepRc())) {
					repRcEq4001 = true;
				}
				
				if (rtnCode != FEPReturnCode.Normal) {
					fepfail = 1;
					//GO TO  7    /* 更新交易紀錄 */
				}else if(!repRcEq4001) {
					fiscfail = 1;
					//GO TO  7    /* 更新交易紀錄 */
				}else{
					// 回覆財金+Con
					feptxn.setFeptxnConRc("4001");
					if (!DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlFisc2way())){
						rtnCode3 = getFiscBusiness().sendConfirmToFISC();
						if(rtnCode3 != FEPReturnCode.Normal) {
							feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode3.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
							getLogContext().setProgramName(ProgramName + ".sendConfirmToFISC");
							getLogContext().setRemark("SEND CONFIRM TO FISC ERROR.");
							sendEMS(getLogContext());
						}
					}else { //for 測試pending交易的控制
						feptxn.setFeptxnWay((short) 3); // 配合人工補confirm 條件
					}
					//成功：核驗結果= 00,帳號核驗結果= 00,開戶狀態= 01,02,05,10,11,12,13
					//除以上皆為核驗失敗，記錄在VATXN，FEPTXN紀錄財金回應的RC
					getLogContext().setMessage(feptxn.getFeptxnRemark());
					getLogContext().setRemark("R1: " + feptxn.getFeptxnRemark().substring(0,2) + ",R2: " + feptxn.getFeptxnRemark().substring(2,4) + ",R3: " + feptxn.getFeptxnRemark().substring(4,6));
					logMessage(getLogContext());
					vatxnrc = feptxn.getFeptxnRepRc(); //暫存，轉換後存VATXN
					if ("99".equals(feptxn.getFeptxnRemark().substring(0,2)) || "99".equals(feptxn.getFeptxnRemark().substring(2,4))||
						"03".equals(feptxn.getFeptxnRemark().substring(4,6)) || "99".equals(feptxn.getFeptxnRemark().substring(4,6))) {
						vatxnrc = "2999";
					} else if (!"00".equals(feptxn.getFeptxnRemark().substring(0,2)) ) {
						vatxnrc = "4516";
					} else if (!"00".equals(feptxn.getFeptxnRemark().substring(2,4)) ) {
						vatxnrc = "4501";
					}
					getLogContext().setRemark("after checkRemark RepRc: " + vatxnrc);
					logMessage(getLogContext());
					if (!vatxnrc.equals(feptxn.getFeptxnRepRc())){ //核驗失敗
						vacheck = 1;
					}
				}
			}

			if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
				// 6. 全民普發交易提領註記
				this.sendToCASH();
			}
			
			// 7. 判斷是否需組 CON 電文回財金
			this.sendToConfirm();
			
			// 8. Insert VATXN: 新增金融帳戶核驗紀錄檔
			this.insertVATXN();
			
			// 9. 核驗成功，送財金FIDO平台登錄
			this.sendToFIDO();

			// 10. 組ATM回應電文 & 回 ATMMsgHandler
			rtnMessage = this.response();
			
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
            // 新增交易記錄(FEPTxn) Returning FEPReturnCode
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
			return; // GOTO  7      /* 更新交易紀錄 */
		}
		
		// 3.2 檢核ATM電文訊息押碼(MAC)
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		String ATM_REQ_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_REQ_PICCMACD)) {
			fepfail = 1;       // FEP有誤
			rtnCode = FEPReturnCode.ENCCheckMACError;
			return; // GO TO 7      /* 更新交易紀錄 */
		}
		
	    getLogContext().setProgramName(ProgramName + ".updateTxData");
		getLogContext().setRemark("Begin checkAtmMacNew mac:" + ATM_REQ_PICCMACD);
        logMessage(getLogContext());
        
		rtnCode = new ENCHelper(getATMtxData()).checkAtmMacNew(atmno, StringUtils.substring(getATMtxData().getTxRequestMessage(), 36,742), ATM_REQ_PICCMACD); // EBCDIC(36,742)
		
		getLogContext().setProgramName(ProgramName + ".updateTxData");
		getLogContext().setRemark("after checkAtmMacNew RC:" + rtnCode.toString());
		logMessage(getLogContext());
		
		if (rtnCode != FEPReturnCode.Normal) {
			fepfail = 1; // FEP有誤
			return; // GOTO 7      /* 更新交易紀錄 */
		}	
	}

	/**
	 * 6. 全民普發交易提領註記
	 */
	private void sendToCASH() throws Exception {
		getLogContext().setProgramName(ProgramName + ".sendToCASH");
		ATMGeneralRequest atmReq = this.getATMRequest();
		String URL = ATMPConfig.getInstance().getCashDistributionUrl();
		if (("TT").equals(feptxn.getFeptxnActivityCode()) && "4001".equals(vatxnrc)) {
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
					requestBody.setType("3");
					requestBody.setTxnTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))); //發送請求時間
					requestBody.setSeqNo(StringUtils.right(requestBody.getTxnTime(),10) + feptxn.getFeptxnBkno() + feptxn.getFeptxnStan()); //交易序號(唯一)
					requestBody.setOrgSeqNo(feptxntcb.getFeptxntcbUcdApiseqno()); //被查詢交易之序號(查詢必填)
					requestEntity = new HttpEntity<>(requestBody, headers);
					this.logContext.setRemark("再呼叫一次API");
					this.logContext.setMessage("Request Body: " + requestBody.toString());
					logMessage(this.logContext);
					response = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, String.class);
					if (response.getBody() == null) {
						cashfail = 1;  //提領註記處理失敗
						rtnCode1 = FEPReturnCode.UCDResponseTimeout; //API回覆逾時
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode1.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
						feptxn.setFeptxnAaRc(rtnCode1.getValue()); //10210
						this.logContext.setRemark("API回傳值為null");
						logMessage(this.logContext);
						return; // GO TO 7    /* 更新交易記錄 */
					}else {
						String responseBody = response.getBody();
						this.logContext.setRemark("API回傳值為: " + responseBody);
						logMessage(this.logContext);
						ObjectMapper objectMapper = new ObjectMapper();
						this.cashTOTA = objectMapper.readValue(responseBody, CashTOTA.class);
						feptxntcb.setFeptxntcbUcdTxno(cashTOTA.getOrgTxnNo()); //普發平台回應的交易序號
						feptxntcb.setFeptxntcbUcdResptime(cashTOTA.getOrgRespTime()); //普發平台的回應時間
						feptxntcb.setFeptxntcbUcdRc(cashTOTA.getOrgReturnCode()); //普發平台的回應代碼
						if (!("00000").equals(this.cashTOTA.getOrgReturnCode())) { //交易失敗
							cashfail = 1;  //提領註記處理失敗
							rtnCode1 = FEPReturnCode.custom(this.cashTOTA.getOrgReturnCode());	//直接取FISC代碼
							feptxn.setFeptxnAaRc(10221); //FEPReturnCode.UCDResponseError
							this.logContext.setRemark("API回傳值不正確 ReturnCode: " + this.cashTOTA.getOrgReturnCode());
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
						cashfail = 1;  //提領註記處理失敗
						rtnCode1 = FEPReturnCode.custom(this.cashTOTA.getReturnCode());	//直接取FISC代碼
						feptxn.setFeptxnAaRc(10221); //FEPReturnCode.UCDResponseError
						this.logContext.setRemark("API回傳值不正確 ReturnCode: " + this.cashTOTA.getReturnCode());
						logMessage(this.logContext);
					}else {
						this.logContext.setRemark("ReturnCode: " + this.cashTOTA.getReturnCode());
						logMessage(this.logContext);
					}
				}

				//將此IDNO的UCDID 當前交易更新為歷史交易
				try{
					int count = ucdidExtMapper.updateHistory(feptxn.getFeptxnIdno().trim(),feptxntcb.getFeptxntcbHealthcard());
				}catch (Exception e){
					cashfail = 1;  //提領註記處理失敗
					rtnCode1 = FEPReturnCode.UpdateFail;
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode1.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
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
				ucdid.setUcdidHistory("N"); //當前交易
				ucdid.setUpdateTime(new Date()); //系統時間

				//新增全民普發個人紀錄
				try {
					int count = ucdidMapper.insert(ucdid);
					if (count <= 0) {
						throw new Exception();
					}
				}catch (Exception e){
					cashfail = 1;  //提領註記處理失敗
					rtnCode1 = FEPReturnCode.InsertFail;
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode1.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
					getLogContext().setProgramException(e);
					getLogContext().setProgramName(ProgramName + ".insertUCDID");
					sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				}
			} catch (Exception e) {
				//呼叫API失敗請給ErrCode：UCDAPIError(14041)
				cashfail = 1;  //提領註記處理失敗
				rtnCode1 = FEPReturnCode.UCDAPIError;
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode1.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
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
	 * 7. 判斷是否需組 CON 電文回財金
	 */
	private void sendToConfirm() {
		getLogContext().setProgramName(ProgramName + ".sendToConfirm");
		getLogContext().setRemark("fepfail:" + fepfail  + ", fiscfail:" + fiscfail + ", cashfail:" + cashfail);
    	logMessage(getLogContext());
        String feptxnRepRc = feptxn.getFeptxnRepRc();
		if (StringUtils.isNoneBlank(feptxnRepRc)) {
			feptxn.setFeptxnPending((short)2); /*解除 Pending*/
		}
        if (fepfail == 0 && fiscfail == 0 && rtnCode == FEPReturnCode.Normal) {
			if (vacheck == 1) { /* 對方行核驗失敗*/
				feptxn.setFeptxnTxrust("R"); /*Accept-Reverse*/
				feptxn.setFeptxnReplyCode(vatxnrc);
				atmrepfpc = 1; //下送FPC給ATM
			} else if ("TT".equals(feptxn.getFeptxnActivityCode()) && cashfail == 1) {  /* 普發提領註記失敗*/
				feptxn.setFeptxnTxrust("R"); /*Accept-Reverse*/
				if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())){
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(FEPReturnCode.toString(rtnCode1), FEPChannel.FISC, FEPChannel.ATM, getATMtxData().getLogContext()));
				}
				atmrepfpc = 1; //下送FPC給ATM
			} else {
				atmrepfpc = 0; // 下送FPN給ATM
				feptxn.setFeptxnReplyCode(feptxnRepRc); // 回覆 ATM正常
				feptxn.setFeptxnTxrust("A"); /* 交易成功 */
				if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) { //跨行清算統計
					rtnCode2 = getFiscBusiness().processAptot(false);
					if (rtnCode2 != FEPReturnCode.Normal) {
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode2.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
						getLogContext().setProgramName(ProgramName + ".processAptot");
						getLogContext().setRemark("process Aptot ERROR");
						sendEMS(getLogContext());
					}
				}
			}
        } else if (fiscfail == 1) { /* FISC失敗*/
        	feptxn.setFeptxnTxrust("R"); /* Reject-normal */
            feptxn.setFeptxnReplyCode(feptxn.getFeptxnRepRc());
        } else { /* 其他失敗 */
			if (feptxn.getFeptxnFiscTimeout() != null ) {
				feptxn.setFeptxnTxrust("R"); /* Reject-normal */
				// 回覆財金-Con
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
		}else if(rtnCode1 != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode1.getValue());
        }else if(rtnCode != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode.getValue());
		}else {
        	feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
        }
        feptxn.setFeptxnAaComplete((short)1);  /*AA Close*/

		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
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
		vatxn.setVatxnReqRc(feptxn.getFeptxnReqRc());
		vatxn.setVatxnRepRc(vatxnrc); /* 紀錄轉換後RC */
		vatxn.setVatxnConRc(feptxn.getFeptxnConRc());
		vatxn.setVatxnTroutBkno(feptxn.getFeptxnTroutBkno());
		vatxn.setVatxnTroutActno(feptxn.getFeptxnTroutActno());
		vatxn.setVatxnBrno(feptxn.getFeptxnBrno());
		vatxn.setVatxnCate("10"); /* 業務類別- FIDO金融卡核驗服務 */
		vatxn.setVatxnType("00"); /* 交易類別-晶片卡核驗 */
		vatxn.setVatxnItem("01"); /* 核驗項目-卡片及帳號 */
		vatxn.setVatxnUse("01"); /* 核驗用途-金融FIDO */
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
			getLogContext().setRemark("insert VATXN error.");
			sendEMS(getLogContext());
		}
	}
	
	/**
	 * 9. 核驗成功，送財金FIDO平台登錄
	 *
	 * @throws Exception
	 */
	private void sendToFIDO() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		if(StringUtils.equals(feptxn.getFeptxnTxrust(), "A") && StringUtils.equals(feptxn.getFeptxnActivityCode(), "FD")) {
			FIDOAdapter adapter;
			// API Request Body
			String fidoBodyStr = this.FIDOBodyJsonStr();
			
			try {
				adapter = new FIDOAdapter(this.getATMtxData());
				adapter.setMessageToFIDO(fidoBodyStr);
				rtnCode = adapter.sendReceive();
				if(rtnCode != FEPReturnCode.Normal) {
					atmrepfpc = 1; //下送ATM FPC
					feptxn.setFeptxnTxrust("R");
					feptxn.setFeptxnAaRc(rtnCode.getValue());
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
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
				        	atmrepfpc = 1; //下送ATM FPC
				        	feptxn.setFeptxnTxrust("R");
				        	feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(statusCode), FEPChannel.FID, FEPChannel.ATM, getTxData().getLogContext()));
			        	}else {
			        		statusCode = String.valueOf(code);
			        	}
			        	feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(statusCode), FEPChannel.FID, FEPChannel.FISC, getTxData().getLogContext()));
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
				}
			} catch (Exception e) {
				getLogContext().setProgramName(ProgramName + ".sendToFIDO");
	            getLogContext().setRemark("FIDOAdapter Exception.");
				sendEMS(getLogContext());
			}
		}
	}
	
	/**
	 * 組成body的Json字串
	 * @return
	 */
	private String FIDOBodyJsonStr() {
		try {
			// 取傳FIDO的Body參數
			String acqBank = feptxn.getFeptxnBkno() + feptxn.getFeptxnAtmno().substring(1,5); //代理行
			String issBank = StringUtils.rightPad(feptxn.getFeptxnTroutBkno(), 7, "0"); // 右補0滿7位 //發卡行
			String stan = feptxn.getFeptxnStan(); // 交易序號
			String txDateTime = feptxn.getFeptxnReqDatetime(); // Request日期時間
			String verifyType = "01"; // 核驗用途-金融FIDO
			String verifyResult = vatxn.getVatxnResult();
			String accVerifyResult = vatxn.getVatxnAcresult();
			String accOpenStatus = vatxn.getVatxnAcstat();
			String terminalType = feptxn.getFeptxnAtmType();
			String id = getATMBusiness().encryptString(feptxn.getFeptxnIdno(), issBank, stan, txDateTime); // 用AES-GCM加密
			String fidoApply = "1"; // 財金定義之公版格式
			String txAccount = StringUtils.leftPad(feptxn.getFeptxnTroutActno(), 16, "0"); // 右靠左補0
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
	 * 10. label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
	 *
	 * @throws Exception
	 */
	private String response() {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
			ATMGeneralRequest atmReq = this.getATMRequest();
			ENCHelper atmEncHelper = new ENCHelper(this.getATMtxData());
			RefString rfs = new RefString();
			if (atmrepfpc == 1) {
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
					pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM, this.getATMtxData().getLogContext());
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
					//轉出帳號(明細表顯示內容)，他行序號16位第10~12位隱碼
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
					response.setPACCNO(FROMACT);
				}else {
					//交易種類
					response.setPTXTYPE(feptxn.getFeptxnTxCode()); //20250805主機確認下送此值為FSCODE
					//轉出帳號(明細表顯示內容)，他行序號16位第10~12位隱碼
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
					response.setPACCNO(FROMACT);
				}
				
				rfs.set("");
				rtnMessage = response.makeMessage();
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				
				this.logContext.setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
				logMessage(this.logContext);
				
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				rtnMessage = response.makeMessage();
			}else if (feptxn.getFeptxnActivityCode().equals("FD")) { //FIDO 身分驗證
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
					//轉出帳號(明細表顯示內容)，他行序號16位第10~12位隱碼
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
					response.setPACCNO(FROMACT);
				}else {
					//交易種類
					response.setPTXTYPE(feptxn.getFeptxnTxCode()); //20250805主機確認下送此值為FSCODE
					//轉出帳號(明細表顯示內容)，他行序號16位第10~12位隱碼
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
					response.setPACCNO(FROMACT);
				}
				
				rfs.set("");
				rtnMessage = response.makeMessage();
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				
				this.logContext.setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
				logMessage(this.logContext);
				
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
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
					//轉出帳號(明細表顯示內容)，他行序號16位第10~12位隱碼
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
					response.setPACCNO(FROMACT);
				}else {
					//交易種類
					response.setPTXTYPE(feptxn.getFeptxnTxCode()); //20250805主機確認下送此值為FSCODE
					//轉出帳號(明細表顯示內容)，他行序號16位第10~12位隱碼
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
					response.setPACCNO(FROMACT);
				}

				rfs.set("");
				rtnMessage = response.makeMessage();
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);

				this.logContext.setRemark("after makeAtmMacP3 RC:" + rtnCode.toString());
				logMessage(this.logContext);

				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
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
