package com.syscom.fep.server.aa.atmp;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * @author 
 */
public class VAOtherConfirmA extends INBKAABase {
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode1 = FEPReturnCode.Normal;
    private boolean isGoResponse = false;
    private String atmno;
    private CashTOTA cashTOTA = new CashTOTA();
    private UcdidMapper ucdidMapper = SpringBeanFactoryUtil.getBean(UcdidMapper.class);
    private UcdidExtMapper ucdidExtMapper = SpringBeanFactoryUtil.getBean(UcdidExtMapper.class);

    public VAOtherConfirmA(ATMData txnData) throws Exception {
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
            getLogContext().setMessageFlowType(MessageFlow.Confirmation);
            getLogContext().setProgramName(StringUtils.join(this.getATMtxData().getAaName(), ".processRequestData"));
            getLogContext().setMessage("ASCII TITA:" + EbcdicConverter.fromHex(CCSID.English, this.getATMtxData().getTxRequestMessage()));
            getLogContext().setRemark(StringUtils.join("Enter ", this.getATMtxData().getAaName()));
            logMessage(getLogContext());
            
            // 1. CheckBusinessRule: 商業邏輯檢核
            this.checkBusinessRule();

            // 2. 全民普發身分驗證異常解除提領註記
            if (rtnCode == FEPReturnCode.Normal) {
                this.sendToCASH();
            }

            if (!isGoResponse) {
                // 3. UpdateTxData: 更新交易記錄(FEPTxn)
                this.updateTxData();
            }

            // 4. Response:組ATM回應電文 & 回 ATMMsgHandler
            rtnMessage = this.response();
            
            // 5. 交易通知 (if need)
            getATMBusiness().sendToNotify();

        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
        	getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getATMtxData().getLogContext().setMessage("MessageToATM:" + rtnMessage);
            getATMtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getATMtxData().getLogContext().setMessageFlowType(MessageFlow.ResponseConfirmation);
            getATMtxData().getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode,getLogContext()));
            logMessage(getATMtxData().getLogContext());
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
        getATMBusiness().setFeptxn(tempFeptxn);
        getATMtxData().setFeptxn(feptxn);
        getFiscBusiness().setFeptxn(feptxn);
        if (getATMBusiness().getFeptxn() == null) {
            rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
            getLogContext().setRemark("Feptxn is null");
            sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            isGoResponse = true;
            return; // GO TO 4 /* 組 ATM 回應電文 */
        }else {
            feptxntcb = getATMBusiness().checkFeptxntcbData(feptxn);
            getATMBusiness().setFeptxntcb(feptxntcb);
            getATMtxData().setFeptxntcb(feptxntcb);
            getFiscBusiness().setFeptxntcb(feptxntcb);
            if (getATMBusiness().getFeptxntcb() == null) {
            	rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
                getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
                getLogContext().setRemark("Feptxntcb is null");
                sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                isGoResponse = true;
                return; // GO TO 4 /* 組 ATM 回應電文 */
            }
        }

        // 1.2  更新 FEPTXN
        rtnCode = getATMBusiness().prepareConFEPTXN();
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GO TO 3 /* 更新交易記錄 */
        }

        // 1.3 交易確認電文檢核 MAC
        /*因Confirm MAC error 需繼續執行其他步驟,故存入不同 RC*/
		String ATM_TITA_PICCMACD = this.getATMRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError;
			return; // GO TO 3 /* 更新交易記錄 */
		}
			
		atmno = feptxn.getFeptxnAtmno();
		String ATMMAC = ATM_TITA_PICCMACD;
		String wkMAC = getATMBusiness().getAtmTxData().getTxRequestMessage().substring(36, 742); 
		getLogContext().setRemark("Begin checkAtmMacNew mac:" + ATMMAC);
		logMessage(getLogContext());
		rtnCode = new ENCHelper(getATMtxData()).checkAtmMacNew(atmno, wkMAC, ATM_TITA_PICCMACD);
		getLogContext().setRemark("after checkAtmMacNew RC:" + rtnCode.toString());
		logMessage(getLogContext());
    }

    /**
     * 2. 全民普發身分驗證異常解除提領註記
     */
    private void sendToCASH() throws Exception {
        getLogContext().setProgramName(ProgramName + ".sendToCASH");
        String URL = ATMPConfig.getInstance().getCashDistributionUrl();
        if ( ("SE").equals(this.getATMRequest().getMSGTYP()) && ("TT").equals(feptxn.getFeptxnActivityCode())) {
            try {
                Ucdid ucdid = ucdidExtMapper.selectByIdno(feptxn.getFeptxnIdno().trim(), feptxntcb.getFeptxntcbHealthcard());
                if (ucdid == null) {
                    rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
                    getLogContext().setProgramName(ProgramName + ".sendToCASH");
                    getLogContext().setRemark("UCDID is null");
                    sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                    isGoResponse = true;
                    return; // GO TO 4 /* 組 ATM 回應電文 */
                }
                // 1. 建立請求主體物件
                CashTOTARequestExt requestBody = new CashTOTARequestExt();
                requestBody.setType("2"); //解除提領
                requestBody.setAgentBank(SysStatus.getPropertyValue().getSysstatHbkno()); //發動行
                requestBody.setTxnTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))); //發送請求時間
                requestBody.setSeqNo(StringUtils.right(requestBody.getTxnTime(),10) + feptxn.getFeptxnBkno() + feptxn.getFeptxnStan()); //交易序號(唯一)
                requestBody.setIdn(feptxn.getFeptxnIdno().trim());	//身分證號
                requestBody.setHealthId(feptxntcb.getFeptxntcbHealthcard()); //健保卡卡號
                requestBody.setChannelType("10"); //ATM提領
                //解除時填入提領註記的回應資料
                requestBody.setOrgSeqNo(feptxntcb.getFeptxntcbUcdApiseqno());
                requestBody.setOrgTxnNo(feptxntcb.getFeptxntcbUcdTxno());
                requestBody.setOrgRespTime(feptxntcb.getFeptxntcbUcdResptime());

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
                    rtnCode1 = FEPReturnCode.UCDResponseTimeout; //API回覆逾時
                    feptxn.setFeptxnAaRc(rtnCode1.getValue()); //10210
                    this.logContext.setRemark("API回傳值為null");
                    logMessage(this.logContext);
                    return; // GO TO 3 /* 更新交易記錄 */
                }else {
                    String responseBody = response.getBody();
                    this.logContext.setMessage("API回傳值為: " + responseBody);
                    this.logContext.setRemark("Call API Success");
                    logMessage(this.logContext);
                    //紀錄解除提領註記的結果
                    ObjectMapper objectMapper = new ObjectMapper();
                    this.cashTOTA = objectMapper.readValue(responseBody, CashTOTA.class);
                    feptxntcb.setFeptxntcbUcdrTxno(cashTOTA.getTxnNo()); //普發平台回應的交易序號
                    feptxntcb.setFeptxntcbUcdrResptime(cashTOTA.getRespTime()); //普發平台的回應時間
                    feptxntcb.setFeptxntcbUcdrRc(cashTOTA.getReturnCode()); //普發平台的回應代碼
                    if (!("00000").equals(this.cashTOTA.getReturnCode())) { //交易失敗
                        rtnCode1 = FEPReturnCode.custom(this.cashTOTA.getReturnCode());	//直接取FISC代碼
                        feptxn.setFeptxnAaRc(10221); //FEPReturnCode.UCDResponseError
                        this.logContext.setRemark("API回傳值不正確 ReturnCode: " + this.cashTOTA.getReturnCode());
                        logMessage(this.logContext);
                    }else {
                        this.logContext.setRemark("ReturnCode: " + this.cashTOTA.getReturnCode());
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
                    ucdid.setUcdid2510Status("2"); //已解除提領
                    ucdid.setUcdidLaststatus("C"); //沖正(API解除成功)
                }
                ucdid.setUpdateTime(new Date());

                //更新全民普發個人紀錄
                try {
                    int count = ucdidMapper.updateByPrimaryKey(ucdid);
                    if (count <= 0) {
                        throw new Exception();
                    }else {
                        this.logContext.setRemark("Update UCDID Success");
                        logMessage(this.logContext);
                    }
                }catch (Exception e){
                    rtnCode1 = FEPReturnCode.UpdateFail;
                    getLogContext().setProgramException(e);
                    getLogContext().setProgramName(ProgramName + ".updateUCDID");
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
            }
        }
    }

    /**
     * 3. UpdateTxData: 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
	        feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response); // (RESPONSE)
            if ( ("TT").equals(feptxn.getFeptxnActivityCode()) && this.rtnCode1 != FEPReturnCode.Normal) {
                feptxn.setFeptxnAaRc(this.rtnCode1.getValue());
                if (StringUtils.isNotBlank(this.cashTOTA.getReturnCode()) && !("00000").equals(this.cashTOTA.getReturnCode())){
                    feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(FEPReturnCode.toString(rtnCode1), FEPChannel.FISC, FEPChannel.ATM, getTxData().getLogContext()));
                }else{
                    feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(FEPReturnCode.toString(rtnCode1), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
                }
                feptxn.setFeptxnTxrust("R");
            }else{
                feptxn.setFeptxnAaRc(this.rtnCode.getValue());
                feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(FEPReturnCode.toString(rtnCode), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
            }

	        feptxn.setFeptxnPending((short) 2);
	        feptxn.setFeptxnAaComplete((short) 1);
			
			int updateCount = feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
			int updateCount2 = feptxnDao.updateByPrimaryKeySelective(this.feptxntcb);
	        if (updateCount <= 0 || updateCount2 <= 0) { // 更新失敗
	        	throw new Exception(); //2025.07.15 Transaction call review 調整
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
            this.rtnCode = FEPReturnCode.FEPTXNUpdateError;
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateTxData");
			sendEMS(getLogContext());
		}
    }

    /**
     * 4. Response:組ATM回應電文 & 回 ATMMsgHandler
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
            if (feptxn == null) {
                response.setRECFMT("0");
            }

            /* CALL ENC 取得MAC 資料 */
            ENCHelper atmEncHelper = new ENCHelper(this.getATMtxData());

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

}
