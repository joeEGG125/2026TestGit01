package com.syscom.fep.server.aa.atmp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.UcdidExtMapper;
import com.syscom.fep.mybatis.ext.model.CashTOTARequestExt;
import com.syscom.fep.mybatis.mapper.UcdidMapper;
import com.syscom.fep.mybatis.model.CashTOTA;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Ucdid;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
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

/**
 * @author vincent
 */
public class WDSelfIssueC extends ATMPAABase {
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode1 = FEPReturnCode.Normal;
    private boolean isGoResponse = false;
    private String AATxTYPE = "";
    private String atmno;
    private String tita;
    private CashTOTA cashTOTA = new CashTOTA();
    private UcdidMapper ucdidMapper = SpringBeanFactoryUtil.getBean(UcdidMapper.class);
    private UcdidExtMapper ucdidExtMapper = SpringBeanFactoryUtil.getBean(UcdidExtMapper.class);

    public WDSelfIssueC(ATMData txnData) throws Exception {
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
            tita = EbcdicConverter.fromHex(CCSID.English, this.getTxData().getTxRequestMessage());
            getLogContext().setProgramFlowType(ProgramFlow.AAIn);
            getLogContext().setMessageFlowType(MessageFlow.Confirmation);
            getLogContext().setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            getLogContext().setMessage("ASCII TITA:" + tita);
            getLogContext().setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(getLogContext());

            // 1. CheckBusinessRule: 商業邏輯檢核
            this.checkBusinessRule();
            
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
            getLogContext().setRemark("after checkBusinessRule isGoResponse :" + isGoResponse + ",RC :" + rtnCode.toString());
            logMessage(getLogContext());

            //2. SendToCBS(if need):
            if(!isGoResponse){
            	// 提款確認電文, 如ATM送Con(-), 須組I002電文送往CBS主機
            	//ATM第二道為失敗電文且FEP紀錄CBS已記帳
				if ("SE".equals(getATMRequest().getMSGTYP()) && feptxn.getFeptxnAccType() == 1) {
					AATxTYPE = "2"; // 上CBS沖正
				}
				if (StringUtils.isNotBlank(AATxTYPE)) {
					String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid(); // AA = MSGCTL_TWCBSTXID的電文
					feptxn.setFeptxnCbsTxCode(AA);
					ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
					rtnCode1 = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
				}
            }

            if (!isGoResponse) {
                // 3. 全民普發身分驗證異常解除提領註記
                this.sendToCASH();
            }

            if (!isGoResponse) {
                // 4. UpdateTxData: 更新交易記錄(FEPTxn)
                this.updateTxData();
            }

            //5. 寫入傳送授權結果通知訊息初始資料 INBK2160 (if need)
            if ("Y".equals(feptxn.getFeptxnSend2160()) && "A".equals(feptxn.getFeptxnTxrust())) {
                /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
                rtnCode = getATMBusiness().prepareInbk2160();
            } else if ("A".equals(feptxn.getFeptxnSend2160())) {
                /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
                rtnCode = getATMBusiness().prepareInbk2160();
            }
            if (!rtnCode.equals(FEPReturnCode.Normal)) {
                getLogContext().setProgramName(ProgramName + ".prepareInbk2160");
                getLogContext().setRemark("PREPARE INBK2160 ERROR");
                sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            }

            // 6. Response:組ATM回應電文 & 回 ATMMsgHandler
            rtnMessage = this.response();

            // 7. 交易通知 (if need)
            getATMBusiness().sendToNotify();

            // 8. 交易結束通知主機(By PCODE)
            if (("A").equals(feptxn.getFeptxnTxrust())){ // 提款類交易，成功才發結束通知
                AATxTYPE = "";      //不需提供此值
                String AATxRs = "N";    //不需等待主機回應
                String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1(); // AA = MSGCTL_TWCBSTXID的電文
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
                rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE,AATxRs);
            }
        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        }

        try {
            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage("MessageToATM:"+rtnMessage);
            getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getTxData().getLogContext().setMessageFlowType(MessageFlow.ResponseConfirmation);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode,getLogContext()));
            logMessage(Level.DEBUG, getLogContext());
        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
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
        getTxData().setFeptxn(feptxn);
        if (getATMBusiness().getFeptxn() == null) {
            rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
            getLogContext().setRemark("Feptxn is null");
            sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            isGoResponse = true;
            return; // GO TO 6 /* 組 ATM 回應電文 */
        }
        
        Feptxntcb tempFeptxntcb = getATMBusiness().checkFeptxntcbData(feptxn);
        feptxntcb = tempFeptxntcb;
        getATMBusiness().setFeptxntcb(tempFeptxntcb);
        getTxData().setFeptxntcb(feptxntcb);
        if (getATMBusiness().getFeptxntcb() == null) {
            rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
            getLogContext().setRemark("Feptxntcb is null");
            sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            isGoResponse = true;
            return; // GO TO 6 /* 組 ATM 回應電文 */
        }

        // 1.2 將ATM確認電文, 準備寫入原交易 FEPTXN欄位
        rtnCode = getATMBusiness().prepareConFEPTXN();
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GO TO 2 /* SendToCBS */
        }

        // 1.3 交易確認電文檢核 MAC
		String ATM_TITA_PICCMACD = this.getATMRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError;
			return; // GO TO 2 /* SendToCBS */
		}
        String newMac = ATM_TITA_PICCMACD;
        getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
        getLogContext().setRemark("Begin checkAtmMac mac:" + newMac);
        logMessage(getLogContext());
        
        // CHANNEL = "EAT"，用ATMNO = "NEATM001"去押驗，在 CheckATMMACNew replace
        atmno = feptxn.getFeptxnAtmno();
        
        String wkMAC = this.getTxData().getTxRequestMessage().substring(36, 742); // EBCDIC(36,742)
		rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno, wkMAC, newMac);
		getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
        getLogContext().setRemark("after checkAtmMac RC:" + rtnCode.toString());
        logMessage(getLogContext());
    }

    /**
     * 3. 全民普發身分驗證異常解除提領註記
     */
    private void sendToCASH() throws Exception {
        getLogContext().setProgramName(ProgramName + ".sendToCASH");
        String URL = ATMPConfig.getInstance().getCashDistributionUrl();
        if (("WP").equals(feptxn.getFeptxnTxCode().trim())) {
            try {
                //2025-12-04修改 找該筆交易
                Ucdid ucdid = ucdidExtMapper.selectByIdnoAndLFStan(feptxn.getFeptxnIdno().trim(), feptxntcb.getFeptxntcbHealthcard(), feptxntcb.getFeptxntcb2566Stan());
                if (ucdid == null) {
                    rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
                    getLogContext().setProgramName(ProgramName + ".sendToCASH");
                    getLogContext().setRemark("UCDID is null");
                    sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                    logMessage(getLogContext());
                    isGoResponse = true;
                    return; // GO TO 4 /* 組 ATM 回應電文 */
                }

                //吐鈔失敗，主機已沖正，Call普發平台解除提款註記
                if (("SE").equals(this.getATMRequest().getMSGTYP()) && feptxn.getFeptxnAccType() == 2) {
                    try {
                        feptxntcb.setFeptxntcb2510Status("N"); //提領失敗
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
                            rtnCode1 = FEPReturnCode.UCDResponseTimeout; //API回覆逾時
                            feptxn.setFeptxnAaRc(rtnCode1.getValue()); //10210
                            this.logContext.setRemark("API回傳值為null");
                            logMessage(this.logContext);
                            return; // GO TO 4    /* 更新交易記錄 */
                        } else {
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
                                rtnCode1 = FEPReturnCode.custom(this.cashTOTA.getReturnCode());    //直接取FISC代碼
                                feptxn.setFeptxnAaRc(10221); //FEPReturnCode.UCDResponseError
                                this.logContext.setRemark("API回傳值不正確 ReturnCode: " + this.cashTOTA.getReturnCode());
                                logMessage(this.logContext);
                            }
                        }
                    }catch (Exception e){
                        //呼叫API失敗請給ErrCode：UCDAPIError(14041)
                        rtnCode1 = FEPReturnCode.UCDAPIError;
                        getLogContext().setProgramException(e);
                        getLogContext().setProgramName(ProgramName + ".sendToCASH");
                        sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                        this.logContext.setRemark("UCDAPIError: " + e.getMessage());
                        logMessage(this.logContext);
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
                } else if (feptxn.getFeptxnAccType() == 1) {  //全民普發交易狀態(核驗成功，提領成功)
                    feptxntcb.setFeptxntcb2510Status("Y"); //領現成功
                    ucdid.setUcdid2510Status("3"); //提領成功
                    ucdid.setUcdidLaststatus("A"); //領取(2510)
                }

                //更新全民普發個人紀錄
                try {
                    ucdid.setUpdateTime(new Date());
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
     * 4. UpdateTxData: 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
	        feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response); // (RESPONSE)
            if ( ("WP").equals(feptxn.getFeptxnTxCode().trim()) && this.rtnCode1 != FEPReturnCode.Normal) {
                if (rtnCode1 == FEPReturnCode.UpdateFail) {
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(FEPReturnCode.toString(rtnCode1), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
                } else if (StringUtils.isNotBlank(this.cashTOTA.getReturnCode()) && !("00000").equals(this.cashTOTA.getReturnCode())){
                    feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(FEPReturnCode.toString(rtnCode1), FEPChannel.FISC, FEPChannel.ATM, getTxData().getLogContext()));
                } else {
                    feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(FEPReturnCode.toString(rtnCode1), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
                }
                feptxn.setFeptxnAaRc(this.rtnCode1.getValue());
            }else {
                feptxn.setFeptxnAaRc(this.rtnCode.getValue());
                feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(this.rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
            }
	        /* 寫入處理結果 */
	        if (feptxn.getFeptxnAccType() == 1) {
				feptxn.setFeptxnTxrust("A"); /* 成功 */
			} else if (feptxn.getFeptxnAccType() == 2) {
                feptxn.setFeptxnTxrust("C"); /* 已沖正 */
            }

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
     * 6. Response:組ATM回應電文 & 回 ATMMsgHandler
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
            if (feptxn == null) {
                atm_fsn_head2.setRECFMT("0");
            }

            /* CALL ENC 取得MAC 資料 */
            ENCHelper atmEncHelper = new ENCHelper(this.getTxData());

            rfs.set("");
            rtnMessage = atm_fsn_head2.makeMessage();
			rtnCode = atmEncHelper.makeAtmMacP3(atmno,rtnMessage, rfs);
			if (rtnCode != FEPReturnCode.Normal) {
				atm_fsn_head2.setMACCODE(""); /* 訊息押碼 */
			} else {
				atm_fsn_head2.setMACCODE(rfs.get()); /* 訊息押碼 */
			}
			getLogContext().setProgramName(ProgramName + ".response");
            getLogContext().setRemark("after makeAtmMac RC:" + rtnCode.toString());
            logMessage(getLogContext());
            rtnMessage = atm_fsn_head2.makeMessage();
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

}
