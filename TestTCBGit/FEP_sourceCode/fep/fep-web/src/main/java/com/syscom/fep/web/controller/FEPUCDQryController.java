package com.syscom.fep.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.UcdidExtMapper;
import com.syscom.fep.mybatis.ext.model.*;
import com.syscom.fep.mybatis.mapper.MsgfileMapper;
import com.syscom.fep.mybatis.mapper.UcdidMapper;
import com.syscom.fep.mybatis.model.CashTOTA;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.mybatis.model.Ucdid;
import com.syscom.fep.web.configurer.WebConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class FEPUCDQryController extends BaseController {

    private final LogHelper log = new LogHelper();
    private final String[] keys = WebConfiguration.getInstance().getUcdapikey().split(",");
    private CashTOTA cashTOTA = new CashTOTA();
    private UcdidMapper ucdidMapper = SpringBeanFactoryUtil.getBean(UcdidMapper.class);
    private UcdidExtMapper ucdidExtMapper = SpringBeanFactoryUtil.getBean(UcdidExtMapper.class);
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);

    @RequestMapping(value = "/WebUtils/FEPUCDQry", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<UcdidMsgRsExt> fepucdQry (@RequestBody UcdidMsgRqExt request) throws Exception {
        String idNo = request.getI_IDNo();
        String healthID = request.getI_HealthID();
        String systemID = request.getI_SystemID();
        String secretKey = request.getI_SecretKey();

        // 1. 驗證輸入
        try {
            validateSecretKey(secretKey);
            validateSystemID(systemID);
        } catch (ValidationException e) {
            return createErrorResponse(idNo, healthID, e.getErrorCode(), e.getMessage());
        }

        // 2. 獲取資料
        try {
            return getFeptxnData(idNo, healthID);
        } catch (DataAccessException e) {
            return createErrorResponse(idNo, healthID, e.getErrorCode(), "Data access error: " + e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(idNo, healthID, "失敗", "Unexpected error: " + e.getMessage());
        }
    }

    private void validateSystemID(String systemID) throws ValidationException {
        if (!"branchweb".equalsIgnoreCase(systemID)) {
            throw new ValidationException("金鑰驗證失敗", "Invalid SystemID: " + systemID);
        }
    }

    private void validateSecretKey(String secretKey) throws ValidationException {
        if (Arrays.stream(keys).noneMatch(key -> key.equals(secretKey))) {
            throw new ValidationException("金鑰驗證失敗", "Invalid SecretKey");
        }
    }

    private ResponseEntity<UcdidMsgRsExt> getFeptxnData(String idNo, String healthID) throws Exception {
        log.info("Input: idNo: " + idNo + ", healthID: " + healthID);

        MsgfileMapper msgfileMapper = SpringBeanFactoryUtil.getBean(MsgfileMapper.class);
        Msgfile msgfile = null;
        String MSGDSCPT = "";

        // 查詢主要資料
        Ucdid ucdid = ucdidExtMapper.selectTopByIdno(idNo, healthID);
        if (ucdid == null) {
            log.info("未註記提領，查詢是否有其他當前交易");
            ucdid = ucdidExtMapper.selectByIdno(idNo, healthID);
            if (ucdid == null) {
                throw new DataAccessException("查無資料", "No Data found");
            }
        }

        LocalDateTime systemTimeMinus5mins = LocalDateTime.now().minusMinutes(5); //系統時間 -5Mins
        String ucdidRecordDateTimeStr = ucdid.getUcdid2566Txdate() + ucdid.getUcdid2566Txtime();
        LocalDateTime ucdidRecordTime = LocalDateTime.parse(ucdidRecordDateTimeStr, formatter); //UCDID 2566交易時間

        // 普發現金解除提領註記 2025-12-04 加條件: UCDID_2566_TXDATE + UCDID_2566_TXTIME < 系統時間 – 5 分鐘
        if (ucdidRecordTime.isBefore(systemTimeMinus5mins) &&
                !"C".equals(ucdid.getUcdidLaststatus()) &&
                ((!"0".equals(ucdid.getUcdid2510Status()) && !"3".equals(ucdid.getUcdid2510Status())) || ucdid.getUcdid2510Status() == null)){
            ucdid = this.sendToCASH(ucdid);
        }

        // 1. 填入 Header 欄位
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        UcdidRsHeadersExt ucdidRsHeadersExt = new UcdidRsHeadersExt();
        ucdidRsHeadersExt.setQryrc("成功");
        ucdidRsHeadersExt.setIdNo(Objects.toString(ucdid.getUcdidIdno(), ""));
        ucdidRsHeadersExt.setHealthId(Objects.toString(ucdid.getUcdidHealthid(), ""));
        if ("3".equals(ucdid.getUcdid2510Status())){
            ucdidRsHeadersExt.setStatus2510("提領成功");
        } else if ("0".equals(ucdid.getUcdid2510Status())) {
            ucdidRsHeadersExt.setStatus2510("提領未明，請人工確認");
        } else {
            ucdidRsHeadersExt.setStatus2510("提領失敗");
        }
        if ("C".equals(ucdid.getUcdidLaststatus())) {
            ucdidRsHeadersExt.setUcdrStatus("解除成功，請客戶重新提領");
        } else if (!"A".equals(ucdid.getUcdidLaststatus())) {
            ucdidRsHeadersExt.setUcdrStatus("解除失敗，請見下方沖正訊息");
        } else {
            ucdidRsHeadersExt.setUcdrStatus("");
        }
        ucdidRsHeadersExt.setActNo(Objects.toString(ucdid.getUcdidTroutActno(), ""));
        ucdidRsHeadersExt.setBkNo(Objects.toString(ucdid.getUcdidTroutBkno(), ""));
        ucdidRsHeadersExt.setAtmNo(Objects.toString(ucdid.getUcdidAtmno(), ""));
        ucdidRsHeadersExt.setAtmBkNo(Objects.toString(ucdid.getUcdidBkno(), ""));
        //日期格式轉換
        ucdidRsHeadersExt.setDateTime2510(StringUtils.isNotBlank(ucdid.getUcdid2510Txdate()) && StringUtils.isNotBlank(ucdid.getUcdid2510Txtime())
                ? LocalDateTime.parse((ucdid.getUcdid2510Txdate() + ucdid.getUcdid2510Txtime()).substring(0, 14), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) : "");

        if (StringUtils.isNotBlank(ucdid.getUcdidUcdRc())) {
            msgfile = msgfileMapper.selectByPrimaryKey(2, ucdid.getUcdidUcdRc());
            MSGDSCPT = msgfile.getMsgfileMsgdscpt();
            ucdidRsHeadersExt.setUcdrc(Objects.toString(ucdid.getUcdidUcdRc() + " " + MSGDSCPT, "")); // 領取訊息
        }else {
            ucdidRsHeadersExt.setUcdrrc("");
        }

        //日期格式轉換
        ucdidRsHeadersExt.setUcdrDateTime(StringUtils.isNotBlank(ucdid.getUcdidUcdrResptime())
                ? LocalDateTime.parse(ucdid.getUcdidUcdrResptime().substring(0, 14), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) : "");
        if (StringUtils.isNotBlank(ucdid.getUcdidUcdrRc())) {
            msgfile = msgfileMapper.selectByPrimaryKey(2, ucdid.getUcdidUcdrRc());
            MSGDSCPT = msgfile.getMsgfileMsgdscpt();
            ucdidRsHeadersExt.setUcdrrc(Objects.toString(ucdid.getUcdidUcdrRc() + " " + MSGDSCPT, "")); // 沖正訊息
        }else {
            ucdidRsHeadersExt.setUcdrrc("");
        }

        ucdidRsHeadersExt.setUcdrChannel(Objects.toString(ucdid.getUcdidUcdrChannel(), ""));
        if ("F".equals(ucdid.getUcdidLaststatus())) {
            ucdidRsHeadersExt.setLastStatus("身分驗證");
        } else if ("A".equals(ucdid.getUcdidLaststatus())) {
            ucdidRsHeadersExt.setLastStatus("領取");
        } else if ("C".equals(ucdid.getUcdidLaststatus())) {
            ucdidRsHeadersExt.setLastStatus("沖正");
        }

        List<UcdidRsBodyExt> responseBodyList = new ArrayList<>();
        String[] txTypes = {"2566", "2510", "API1", "API2"};
        for (String txType : txTypes){
            UcdidRsBodyExt bodyItem;
            // 2. 填入 Body 欄位
            switch (txType) {
                case "2566":
                    bodyItem = new UcdidRsBodyExt();
                    bodyItem.setTxType("身分驗證");
                    bodyItem.setTxStan(Objects.toString(ucdid.getUcdid2566Stan(), ""));
                    if ("1".equals(ucdid.getUcdid2566Apistatus())) bodyItem.setTxRust("Y");
                    else if ("2".equals(ucdid.getUcdid2566Apistatus())) bodyItem.setTxRust("R");
                    else bodyItem.setTxRust("X");
                    bodyItem.setBkTbsdy(StringUtils.isNotBlank(ucdid.getUcdid2566Tbsdy())
                            ? LocalDate.parse(ucdid.getUcdid2566Tbsdy().substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"))
                            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) : "");
                    bodyItem.setUcdapiSeqNo("");
                    bodyItem.setUcdTxnNo("");
                    bodyItem.setUcdRespTime("");
                    bodyItem.setUcdrcCode("");
                    responseBodyList.add(bodyItem);
                    break;
                case "2510":
                    bodyItem = new UcdidRsBodyExt();
                    bodyItem.setTxType("提領");
                    bodyItem.setTxStan(Objects.toString(ucdid.getUcdid2510Stan(), ""));
                    if ("3".equals(ucdid.getUcdid2510Status())) bodyItem.setTxRust("Y");
                    else if ("4".equals(ucdid.getUcdid2510Status())) bodyItem.setTxRust("R");
                    else bodyItem.setTxRust("X");
                    bodyItem.setBkTbsdy(StringUtils.isNotBlank(ucdid.getUcdid2510Tbsdy())
                            ? LocalDate.parse(ucdid.getUcdid2510Tbsdy().substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"))
                            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) : "");
                    bodyItem.setUcdapiSeqNo("");
                    bodyItem.setUcdTxnNo("");
                    bodyItem.setUcdRespTime("");
                    bodyItem.setUcdrcCode("");
                    responseBodyList.add(bodyItem);
                    break;
                case "API1":
                    bodyItem = new UcdidRsBodyExt();
                    bodyItem.setTxType("提領登錄");
                    bodyItem.setTxStan("");
                    bodyItem.setTxRust("");
                    bodyItem.setBkTbsdy("");
                    bodyItem.setUcdapiSeqNo(Objects.toString(ucdid.getUcdidUcdApiseqno(), ""));
                    bodyItem.setUcdTxnNo(Objects.toString(ucdid.getUcdidUcdTxno(), ""));
                    bodyItem.setUcdRespTime(StringUtils.isNotBlank(ucdid.getUcdidUcdResptime())
                            ? LocalDateTime.parse(ucdid.getUcdidUcdResptime().substring(0, 14), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) : "");
                    bodyItem.setUcdrcCode(Objects.toString(ucdid.getUcdidUcdRc(), "")); // 財金回覆代碼
                    responseBodyList.add(bodyItem);
                    break;
                case "API2":
                    bodyItem = new UcdidRsBodyExt();
                    bodyItem.setTxType("解除提領");
                    bodyItem.setTxStan("");
                    bodyItem.setTxRust("");
                    bodyItem.setBkTbsdy("");
                    bodyItem.setUcdapiSeqNo(Objects.toString(ucdid.getUcdidUcdrApiseqno(), ""));
                    bodyItem.setUcdTxnNo(Objects.toString(ucdid.getUcdidUcdrTxno(), ""));
                    bodyItem.setUcdRespTime(StringUtils.isNotBlank(ucdid.getUcdidUcdrResptime())
                            ? LocalDateTime.parse(ucdid.getUcdidUcdrResptime().substring(0, 14), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) : "");
                    bodyItem.setUcdrcCode(Objects.toString(ucdid.getUcdidUcdrRc(), "")); // 財金回覆代碼
                    responseBodyList.add(bodyItem);
                    break;
            }
        }

        // 3. 返回 ResponseEntity
        UcdidMsgRsExt ucdidMsgRsExt = new UcdidMsgRsExt();
        ucdidMsgRsExt.setBody(responseBodyList);
        ucdidMsgRsExt.setHeaders(ucdidRsHeadersExt);
        log.info("API 查詢成功, 輸出結果 Headers: " + ucdidRsHeadersExt.toString() + "Body: " + responseBodyList);
        return new ResponseEntity<>(ucdidMsgRsExt, headers, HttpStatus.OK);
    }

    /**
     * 全民普發身分驗證異常解除提領註記
     */
    private Ucdid sendToCASH(Ucdid ucdid) throws Exception {
        getLogContext().setProgramName(ProgramName + ".sendToCASH");
        log.info("開始解除提領註記...");
        Ucdid tempucdid = ucdid;
        String URL = ATMPConfig.getInstance().getCashDistributionUrl();
        try {
            // 1. 建立請求主體物件
            CashTOTARequestExt requestBody = new CashTOTARequestExt();
            requestBody.setType("2"); //解除提領
            requestBody.setAgentBank(SysStatus.getPropertyValue().getSysstatHbkno()); //發動行
            requestBody.setTxnTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))); //發送請求時間
            requestBody.setSeqNo(StringUtils.right(requestBody.getTxnTime(),10) + StringUtils.right(ucdid.getUcdidUcdApiseqno(),10)); //交易序號(唯一)
            requestBody.setIdn(ucdid.getUcdidIdno()); //身分證號
            requestBody.setHealthId(ucdid.getUcdidHealthid()); //健保卡卡號
            requestBody.setChannelType("10"); //ATM提領

            //解除時填入提領註記的回應資料
            requestBody.setOrgSeqNo(ucdid.getUcdidUcdApiseqno());
            requestBody.setOrgTxnNo(ucdid.getUcdidUcdTxno());
            requestBody.setOrgRespTime(ucdid.getUcdidUcdResptime());
            tempucdid.setUcdidUcdrApiseqno(requestBody.getSeqNo()); //Call全民普發平台解除交易序號

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
                this.logContext.setRemark("全民普發平台回應逾時，ID：" + ucdid.getUcdidIdno() + "HEALTHID：" + ucdid.getUcdidHealthid());
                logMessage(this.logContext);
            }else {
                String responseBody = response.getBody();
                this.logContext.setMessage("API Response: " + responseBody);
                this.logContext.setRemark("Call API Success");
                logMessage(this.logContext);
                //紀錄解除提領註記的結果
                ObjectMapper objectMapper = new ObjectMapper();
                this.cashTOTA = objectMapper.readValue(responseBody, CashTOTA.class);
                if (!("00000").equals(this.cashTOTA.getReturnCode())) { //交易失敗
                    this.logContext.setRemark("API回傳值不正確 ReturnCode: " + this.cashTOTA.getReturnCode());
                    logMessage(this.logContext);
                }else {
                    this.logContext.setRemark("ReturnCode: " + this.cashTOTA.getReturnCode());
                    logMessage(this.logContext);
                    ucdid.setUcdidLaststatus("C"); //沖正(API解除成功)
                }
                ucdid.setUcdidUcdrTxno(cashTOTA.getTxnNo());  //普發平台回應的交易序號
                ucdid.setUcdidUcdrResptime(cashTOTA.getRespTime()); //普發平台的回應時間
                ucdid.setUcdidUcdrRc(cashTOTA.getReturnCode()); //普發平台的回應代碼
            }
            ucdid.setUcdidUcdrChannel("WEB");
            ucdid.setUpdateTime(new Date()); //系統時間
            //更新全民普發個人紀錄
            try {
                int count = ucdidMapper.updateByPrimaryKey(ucdid);
                if (count <= 0) {
                    throw new Exception();
                }else {
                    log.info("UCDID更新成功" + count + "筆紀錄，ID：" + ucdid.getUcdidIdno()
                            + "HEALTHID：" + ucdid.getUcdidHealthid() + "RC：" + ucdid.getUcdidUcdrRc());
                    return ucdid;
                }
            }catch (Exception e){
                getLogContext().setProgramException(e);
                getLogContext().setProgramName("sendToCASH" + ".updateUCDID");
                sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                log.error("UCDID更新失敗，ID：" + ucdid.getUcdidIdno() + "HEALTHID：" + ucdid.getUcdidHealthid());
            }
            return tempucdid;
        } catch (Exception e) {
            getLogContext().setProgramException(e);
            getLogContext().setProgramName(ProgramName + ".sendToCASH");
            sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            log.error("呼叫全民普發平台失敗，ID：" + ucdid.getUcdidIdno() + "HEALTHID：" + ucdid.getUcdidHealthid());
            return tempucdid;
        }
    }

    private ResponseEntity<UcdidMsgRsExt> createErrorResponse(String idNo, String healthId, String errorCode, String logMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("logMessage", logMessage);

        UcdidRsHeadersExt ucdidRsHeadersExt = new UcdidRsHeadersExt();
        ucdidRsHeadersExt.setQryrc(errorCode);
        ucdidRsHeadersExt.setIdNo(idNo);
        ucdidRsHeadersExt.setHealthId(healthId);
        ucdidRsHeadersExt.setStatus2510("");
        ucdidRsHeadersExt.setUcdrStatus("");
        ucdidRsHeadersExt.setActNo("");
        ucdidRsHeadersExt.setBkNo("");
        ucdidRsHeadersExt.setAtmNo("");
        ucdidRsHeadersExt.setAtmBkNo("");
        ucdidRsHeadersExt.setDateTime2510("");
        ucdidRsHeadersExt.setUcdrc("");
        ucdidRsHeadersExt.setUcdrDateTime("");
        ucdidRsHeadersExt.setUcdrrc("");
        ucdidRsHeadersExt.setUcdrChannel("");
        ucdidRsHeadersExt.setLastStatus("");
        log.error(logMessage);

        UcdidMsgRsExt ucdidMsgRsExt = new UcdidMsgRsExt();
        ucdidMsgRsExt.setHeaders(ucdidRsHeadersExt);
        return new ResponseEntity<>(ucdidMsgRsExt, headers, HttpStatus.BAD_REQUEST);
    }

    // 自定義例外類別，用於封裝驗證錯誤
    private static class ValidationException extends Exception {
        private final String errorCode;

        public ValidationException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    // 自定義例外類別，用於封裝資料存取錯誤
    private static class DataAccessException extends Exception {
        private final String errorCode;

        public DataAccessException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
