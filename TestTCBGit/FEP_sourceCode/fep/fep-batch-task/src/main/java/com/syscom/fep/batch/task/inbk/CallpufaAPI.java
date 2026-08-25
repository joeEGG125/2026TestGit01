package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.util.FormatUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CallpufaAPI extends FEPBase implements Task {
    private BatchJobLibrary job = null;
    private String batchLogPath = StringUtils.EMPTY;
    private boolean batchResult = false;
    private long durationMinutes = 10; // 預設執行時間：10 分鐘
    private long intervalMilliseconds = 50; // 預設發送間隔：50 毫秒
    private int maxThreads = 20; //執行序數量限制 預設20

    private int idno = 170000019;
    private int healthid = 11000009;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);

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
        String URL = ATMPConfig.getInstance().getCashDistributionUrl();
        AtomicInteger totCnt = new AtomicInteger(0);      // 總筆數
        AtomicInteger resCnt = new AtomicInteger(0);      // 回應筆數
        AtomicInteger unansCnt = new AtomicInteger(0);    // 未回應筆數
        AtomicInteger exCnt = new AtomicInteger(0);    // 未預期錯誤筆數

        String startTime = LocalDateTime.now().format(formatter);
        job.writeLog("批次開始：" + startTime);
        job.writeLog("預計執行時間：" + durationMinutes + " 分鐘");
        job.writeLog("發送間隔：" + intervalMilliseconds + " 毫秒");
        job.writeLog("最大執行緒數：" + maxThreads);

        // 1. 建立固定數量的執行緒池
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        RestTemplate restTemplate = new RestTemplate();
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

        // 計算批次結束時間
        long startTimeMillis = System.currentTimeMillis();
        long endTimeMillis = startTimeMillis + (durationMinutes * 60 * 1000);

        while (System.currentTimeMillis() < endTimeMillis) {
            //先遞增
            final int currentIdno = idno++;
            final int currentHealthid = healthid++;
            totCnt.incrementAndGet();

            Runnable apiCallTask = () -> {
                CashApiRequest request = new CashApiRequest();
                try {
                    String txnTime = LocalDateTime.now().format(formatter);
                    String seqNo = txnTime.substring(txnTime.length() - 10) + "006E05B313";

                    request.setType("1");
                    request.setAgentBank("006");
                    request.setTxnTime(txnTime);
                    request.setSeqNo(seqNo);
                    request.setIdn("B" + currentIdno); //身分證號
                    request.setHealthId("0061" + currentHealthid); //健保卡卡號
                    request.setChannelType("10");

                    request.setOrgSeqNo("");
                    request.setOrgTxnNo("");
                    request.setOrgRespTime("");

                    // 3.建立 HttpEntity，包含請求主體和標頭
                    HttpEntity<CashApiRequest> requestEntity = new HttpEntity<>(request, headers);
                    ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, String.class);

                    resCnt.incrementAndGet();
                } catch (RestClientException e) { //發生timeout
                    unansCnt.incrementAndGet();
                } catch (Exception e) {
                    // 捕捉所有其他預期之外的錯誤, 新增計數器
                    exCnt.incrementAndGet();
                }
            };

            executor.submit(apiCallTask);

            // 4.主執行緒等待的間隔時間
            try {
                Thread.sleep(intervalMilliseconds);
            } catch (InterruptedException e) {
                job.writeLog("主執行緒中斷，停止提交新任務...");
                Thread.currentThread().interrupt(); // 重新設定中斷狀態
                break; // 跳出迴圈
            }
        }

        // 5.執行時間結束,關閉執行序池
        job.writeLog("任務提交階段結束，共提交 " + totCnt.get() + " 個任務。現在開始等待所有任務執行完畢...");
        executor.shutdown();
        try {
            // 等待最多 1 分鐘讓所有現有任務結束
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                job.writeLog("等待逾時，強制關閉執行緒池...");
                executor.shutdownNow(); // 強制中斷
            }
        } catch (InterruptedException e) {
            job.writeLog("等待任務完成時發生錯誤，強制關閉...");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }


        // 6.批次結束，記錄總結 Log
        String endTime = LocalDateTime.now().format(formatter);
        job.writeLog("批次結束：" + endTime +
                "，總發送筆數：" + totCnt.get() +
                "，回應筆數：" + resCnt.get() +
                "，未回應筆數：" + unansCnt.get() + "未預期錯誤筆數: " + exCnt.get());

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
        job.writeLog("CallpufaAPI start!");
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
        // 讀取執行時間 (分鐘)
        if (job.getArguments().containsKey("DURATION")) {
            String durationStr = job.getArguments().get("DURATION");
            try {
                durationMinutes = Long.parseLong(durationStr);
            } catch (NumberFormatException e) {
                job.writeLog("執行時間參數 (DURATION) 格式錯誤 '" + durationStr + "'，將使用預設值：" + durationMinutes + " 分鐘");
            }
        }

        // 讀取發送間隔 (毫秒)
        if (job.getArguments().containsKey("INTERVAL")) {
            String intervalStr = job.getArguments().get("INTERVAL");
            try {
                intervalMilliseconds = Long.parseLong(intervalStr);
            } catch (NumberFormatException e) {
                job.writeLog("發送間隔參數 (INTERVAL) 格式錯誤 '" + intervalStr + "'，將使用預設值：" + intervalMilliseconds + " 毫秒");
            }
        }

        // 讀取最大執行緒(thread)數
        if (job.getArguments().containsKey("MAX_THREADS")) {
            String threadsStr = job.getArguments().get("MAX_THREADS");
            try {
                maxThreads = Integer.parseInt(threadsStr);
            } catch (NumberFormatException e) {
                job.writeLog("最大執行緒數參數 (MAX_THREADS) 格式錯誤 '" + threadsStr + "'，將使用預設值：" + maxThreads);
            }
        }

        return true;
    }
}
