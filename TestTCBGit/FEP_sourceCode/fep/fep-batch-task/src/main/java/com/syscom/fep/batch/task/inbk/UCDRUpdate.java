package com.syscom.fep.batch.task.inbk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.TaskExtMapper;
import com.syscom.fep.mybatis.ext.mapper.UcdidExtMapper;
import com.syscom.fep.mybatis.model.Ucdid;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class UCDRUpdate extends FEPBase implements Task {
    private BatchJobLibrary job = null;
    private String batchLogPath = StringUtils.EMPTY;
    private boolean batchResult = false;

    private String idno = StringUtils.EMPTY;
    private String healthid = StringUtils.EMPTY;
    private String wk_channel = StringUtils.EMPTY;
    private String nocondition = StringUtils.EMPTY; //無條件
    private String w_commandargs = StringUtils.EMPTY;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);

    private UcdidExtMapper ucdidExtMapper = SpringBeanFactoryUtil.getBean(UcdidExtMapper.class);
    private TaskExtMapper taskExtMapper = SpringBeanFactoryUtil.getBean(TaskExtMapper.class);

    @Override
    public BatchReturnCode execute(String[] args) {
        try {
            // 1. 初始化相關批次物件及拆解傳入參數
            initialBatch(args);

            // 2. 檢核批次參數是否正確, 若正確則啟動批次工作
            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始");
            job.startTask();

            if (!this.checkConfig()) {
                job.stopBatch();
                return BatchReturnCode.ProgramException;
            }

            // 3. 批次主要處理流程
            batchResult = mainProcess();

            // 4. 通知批次作業管理系統工作正常結束
            if (batchResult) {
                job.writeLog(ProgramName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
            }
            return BatchReturnCode.Succeed;
        } catch (Exception e) {
            logContext.setProgramException(e);
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            if (job != null) {
                job.writeErrorLog(e, e.getMessage());
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog("------------------------------------------------------------------");
                // 通知批次作業管理系統工作失敗,暫停後面流程
                try {
                    job.abortTask();
                } catch (Exception ex) {
                    logContext.setProgramException(ex);
                    logContext.setProgramName(ProgramName);
                    sendEMS(logContext);
                }
            }
            return BatchReturnCode.ProgramException;
        } finally {
            if (job != null) {
                job.writeLog(ProgramName + "結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.dispose();
                job = null;
            }
            if (logContext != null) {
                logContext = null;
            }
        }
    }

    private boolean mainProcess() throws Exception {
        List<Ucdid> ucdidList;
        List<com.syscom.fep.mybatis.model.Task> taskList;
        String URL = ATMPConfig.getInstance().getCashDistributionUrl();

        // 0.先檢查是否手動啟動批次(人工判斷)
        if(StringUtils.isNotBlank(idno) && StringUtils.isNotBlank(healthid)) {
            if("".equals(nocondition) || (!"Y".equals(nocondition) && !"N".equals(nocondition))) { //未輸入或不是Y/N
                job.writeLog("手動啟動批次：需指定是否無條件限制(Y/N)");
                return false;
            } else if ("N".equals(nocondition)) {
                wk_channel = "NOCOND_N"; //人工處理，有條件手動啟動批次
            }else{
                wk_channel = "NOCOND_Y"; //人工處理，無條件啟動批次
            }
            job.writeLog("手動啟動批次參數：身分證號：" + idno + "，健保卡號：" + healthid + "，無條件：" + nocondition);

        }else {
            wk_channel = "BATCH"; //整批執行
        }

        try {
            // 1.讀取UCDID(全民普發ID紀錄檔)
            ucdidList = ucdidExtMapper.queryByIdnoAndHealthId(idno, healthid, nocondition);
        } catch (Exception e) {
            job.writeErrorLog(e, "讀取 UCDID 資料庫發生錯誤: " + e.getMessage());
            return false;
        }

        if (ucdidList == null || ucdidList.isEmpty()) {
            job.writeLog("無符合條件的普發交易!!");
            return true; // 批次結束
        }

        // 初始化計數器
        int totCnt = 0;      // 總筆數
        int skipCnt = 0;     // 未處理筆數
        int ucdrScnt = 0;    // 解除成功筆數
        int ucdrFcnt = 0;    // 解除異常筆數
        int updateScnt = 0;  // 更新成功筆數
        int updateFcnt = 0;  // 更新失敗筆數
        String startTime = LocalDateTime.now().format(formatter);
        LocalDateTime executeTime = LocalDateTime.now().minusMinutes(10);
        job.writeLog("批次開始：" + startTime);

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        int timeout = 50;

        // 如果是 Https 設定 TLS1.2
        if (HttpClient.isHttps(URL)) {
            restTemplate.setRequestFactory(HttpClientConfiguration.createTrustAnyHttpComponentsClientHttpRequestFactory("TLSv1.2", timeout * 1000));
        } else {
            restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(timeout * 1000));
        }
        // 2.設定請求標頭 (指定內容類型為 JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // For i = 1 To UCDID.count
        for (Ucdid recordA : ucdidList) {
            totCnt++;

            //只抓取批次執行時間10分鐘以前的資料(改抓LF)
            String recordDateTimeStr = recordA.getUcdid2566Txdate() + recordA.getUcdid2566Txtime();
            LocalDateTime recordTime = LocalDateTime.parse(recordDateTimeStr, formatter);
            if (recordTime.isAfter(executeTime)) {
                skipCnt++;
                job.writeLog("10分鐘內交易不處理，ID: " + recordA.getUcdidIdno() + ", HEALTHID: " + recordA.getUcdidHealthid() + ", 交易序號: " + recordA.getUcdid2566Stan() + ", 交易時間: " + recordDateTimeStr);
                continue;
            }

            ResponseEntity<String> response = null;
            CashApiRequest request = new CashApiRequest();
            String requestJson = "";
            String responseBody = "";

            try{
                try {
                    String txnTime = LocalDateTime.now().format(formatter);
                    //交易序號(唯一)：最近一次解除異常的APISEQNO
                    String seqNo = txnTime.substring(txnTime.length() - 10) +
                            StringUtils.right(recordA.getUcdidUcdApiseqno(), 10);

                    request.setType("2"); //解除提領
                    request.setAgentBank(SysStatus.getPropertyValue().getSysstatHbkno()); //發動行
                    request.setTxnTime(txnTime);
                    request.setSeqNo(seqNo);
                    request.setIdn(recordA.getUcdidIdno()); //身分證號
                    request.setHealthId(recordA.getUcdidHealthid()); //健保卡卡號
                    request.setChannelType("10"); //ATM提領

                    //解除時填入提領註記的回應資料
                    request.setOrgSeqNo(recordA.getUcdidUcdApiseqno());
                    request.setOrgTxnNo(recordA.getUcdidUcdTxno());
                    request.setOrgRespTime(recordA.getUcdidUcdResptime());
                    recordA.setUcdidUcdrApiseqno(seqNo); //Call全民普發平台解除交易序號

                    requestJson = objectMapper.writeValueAsString(request);

                    // 3. 建立 HttpEntity，包含請求主體和標頭
                    HttpEntity<CashApiRequest> requestEntity = new HttpEntity<>(request, headers);
                    response = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, String.class);

                    //4.response 最終檢查
                    if (response == null || response.getStatusCodeValue() < 200 || response.getStatusCodeValue() >= 300 || StringUtils.isBlank(response.getBody())) {
                        skipCnt++;
                        job.writeLog("response:" + response);
                        job.writeLog("呼叫全民普發平台失敗，ID：" + recordA.getUcdidIdno() + "，HEALTHID：" + recordA.getUcdidHealthid() + "，Request：" + requestJson);
                        job.writeLog("---------------------------------------------");
                        continue; // 繼續處理下一筆
                    }

                    // 處理 API 回應
                    responseBody = response.getBody();
                    CashApiResponse cashTOTA= objectMapper.readValue(responseBody, CashApiResponse.class);
                    //紀錄解除提領註記的結果
                    recordA.setUcdidUcdrTxno(cashTOTA.txnNo); // 普發平台回應的交易序號
                    recordA.setUcdidUcdrResptime(cashTOTA.respTime); // 普發平台的回應時間
                    recordA.setUcdidUcdrRc(cashTOTA.returnCode); // 普發平台的回應代碼

                }catch (RestClientException e){
                    job.writeLog("response:" + response + ", Error:" + e.getMessage());
                    job.writeLog("全民普發平台回應逾時，ID：" + recordA.getUcdidIdno() + "，HEALTHID：" + recordA.getUcdidHealthid() + "，Request：" + requestJson);
                    job.writeLog("---------------------------------------------");
                    skipCnt++;
                    continue; // Next
                }

                // Channel
                recordA.setUcdidUcdrChannel(wk_channel);

                //List<String> list = Arrays.asList("00000","???");
                if (StringUtils.isBlank(recordA.getUcdidUcdrRc()) || "E0000".equals(recordA.getUcdidUcdrRc())) {//財金無回應
                    ucdrFcnt++;
                } else {
                    recordA.setUcdidLaststatus("C"); // 沖正(API解除成功)
                    ucdrScnt++;
                }

                recordA.setUpdateTime(new Date());
                // 5.更新UCDID
                if (ucdidExtMapper.updateByPrimaryKeySelective(recordA) > 0) {
                    updateScnt++;
                    job.writeLog("UCDID更新成功，ID：" + recordA.getUcdidIdno() + "，HEALTHID：" + recordA.getUcdidHealthid() + "，RC：" + recordA.getUcdidUcdrRc()
                            + "，Request：" + requestJson + "，Response：" + responseBody);
                    job.writeLog("---------------------------------------------");
                } else {
                    updateFcnt++;
                    job.writeLog("UCDID更新失敗，ID：" + recordA.getUcdidIdno() + "，HEALTHID：" + recordA.getUcdidHealthid()
                            + "，Request：" + requestJson + "，Response：" + responseBody);
                    job.writeLog("---------------------------------------------");
                }

            } catch (Exception e) {
                job.writeLog("response:" + response + ", Error:" + e.getMessage());
                job.writeLog( "處理資料發生異常, ID: " + recordA.getUcdidIdno() + ", HEALTHID: " + recordA.getUcdidHealthid() + ", Request: " + requestJson, e);
                job.writeLog("---------------------------------------------");
                skipCnt++;
                continue;
            }
        }

        // 6.批次結束，記錄總結 Log
        String endTime = LocalDateTime.now().format(formatter);
        job.writeLog("批次結束：" + endTime +
                "，處理筆數：" + totCnt +
                "，未處理筆數：" + skipCnt +
                "，解除成功筆數：" + ucdrScnt +
                "，解除失敗筆數：" + ucdrFcnt +
                "，更新成功筆數：" + updateScnt +
                "，更新失敗筆數：" + updateFcnt);

        // 7.更新傳入參數值NOCONDITION為N
        taskList = taskExtMapper.getUCDTaskByName("UCDRUpdate");

        if (!taskList.isEmpty()) {
            for(com.syscom.fep.mybatis.model.Task recordB : taskList) {
                // 檢查 recordB 和它的 commandargs 是否為 null 或空
                if (recordB == null || StringUtils.isBlank(recordB.getTaskCommandargs())) {
                    continue; // 跳過
                }

                w_commandargs = recordB.getTaskCommandargs().trim();
                if(w_commandargs.endsWith("NOCONDITION:Y")) { //無條件解除
                    w_commandargs = w_commandargs.substring(0, w_commandargs.length()-1) + "N";
                    recordB.setTaskCommandargs(w_commandargs);

                    if(taskExtMapper.updateByPrimaryKeySelective(recordB) > 0) {
                        job.writeLog("TASK 參數更新成功: " + recordB.getTaskName() + " (PK: " + recordB.getTaskId() + ")");
                    } else {
                        job.writeLog("TASK 參數更新失敗: " + recordB.getTaskName() + " (PK: " + recordB.getTaskId() + ")");
                    }
                }
            }
        }

        return true;
    }

     /**
     * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
     *
     * @param args
     */
    private void initialBatch(String[] args) {
        // 初始化logContext物件,傳入工作執行參數
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        // 檢查Batch Log目錄參數
        batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(batchLogPath)) {
            System.out.println("Batch Log目錄未設定，請修正");
            return;
        }

        // 初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, batchLogPath);
        job.writeLog("UCDRUpdate start!");
    }


    //api設定
    @Getter
    @Setter
    private static class CashApiRequest {
        private String type;
        private String agentBank;
        private String txnTime;
        private String seqNo;
        private String idn;
        private String healthId;
        private String channelType;
        private String orgSeqNo;
        private String orgTxnNo;
        private String orgRespTime;
    }

    @Getter
    @Setter
    private static class CashApiResponse {
        String type, agentBank, seqNo, txnTime, txnNo, respTime, returnCode,
                orgIdn, orgHealthId, orgTxnNo, orgChannelType, orgRespTime, orgReturnCode;
    }

    private boolean checkConfig() throws Exception {
        //抓檔名
        if (job.getArguments().containsKey("IDNO")) {
            idno = job.getArguments().get("IDNO");
        }
        if (job.getArguments().containsKey("HEALTHID")) {
            healthid = job.getArguments().get("HEALTHID");
        }
        if (job.getArguments().containsKey("NOCONDITION")) {
            nocondition = job.getArguments().get("NOCONDITION");
        }
        return true;
    }
}
