package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.jms.JmsHandler;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.mybatis.ext.mapper.NpsbatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsdtlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Npsbatch;
import com.syscom.fep.mybatis.model.Npsdtl;
import com.syscom.fep.mybatis.model.Sysconf;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class NPAYResponseToChannel extends FEPBase implements Task {
    private BatchJobLibrary job = null;
    private String batchLogPath = StringUtils.EMPTY;
    private boolean batchResult = false;

    private String filename = StringUtils.EMPTY;
    private String txdate = StringUtils.EMPTY;
    private String batchno = StringUtils.EMPTY;

    private String w_fileName = StringUtils.EMPTY;
    private String w_Date = StringUtils.EMPTY;
    private String w_batchNo = StringUtils.EMPTY;
    private NpsbatchExtMapper npsbatchExtMapper = SpringBeanFactoryUtil.getBean(NpsbatchExtMapper.class);
    private NpsdtlExtMapper npsdtlExtMapper = SpringBeanFactoryUtil.getBean(NpsdtlExtMapper.class);
    private SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);


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
        //查詢尚未回覆的批號及交易明細
        // 主機查詢批號，依現狀回覆結果檔，未處理之交易視為失敗，更新 NPSDTL_RESULT
        // 批號狀態：00:交易發送完成，01:整批剔退，02:已回結果檔
        // 1. 檢核手動啟動批次的傳入參數
        boolean result = checkManualBatchParams();
        if (!result) {
            return true; // 批次結束
        }

        // 讀參數檔
        Sysconf sysconf = sysconfExtMapper.selectByPrimaryKey( (short) 3, "NPAYBatchResponseControl");
        if (sysconf == null) {
            job.writeLog("找不到外圍整批轉即時批次強迫回覆結果檔的時間參數!!");
            return true; // 批次結束
        }

        List<Npsbatch> batchList = npsbatchExtMapper.getNpsbatchForResponse(w_fileName, w_Date,w_batchNo);
        if (batchList == null || batchList.isEmpty()) {
            job.writeLog("無符合條件的NPSBATCH(整批轉即時批次檔)紀錄!!");
            return true; // 批次結束
        }

        String sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
        // 2. 取NPSBATCH(整批轉即時批次檔)
        int W_IMS_CNT = 0;// 計算IMS的批號回覆批數，主機NPAY，每次最多回覆兩批
        for (Npsbatch npsbatch : batchList) {
            int wStop = 0; // 停止交易註記

            if (!"02".equals(npsbatch.getNpsbatchResult())) {
                LocalDateTime now = LocalDateTime.now();
                String wTime = now.format(DateTimeFormatter.ofPattern("HHmmss"));
                // 晚上 22:30 強迫回覆
                if (wTime.substring(0, 4).compareTo("2230") >= 0) {
                    wStop = 1;
                }

                //批號收到?分鐘(參數設定，以分計)後強迫回覆結果檔
                if (npsbatch.getNpsbatchRcvTime() != null) {
                    LocalDateTime rcvTime = npsbatch.getNpsbatchRcvTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    long minutesBetween = ChronoUnit.MINUTES.between(rcvTime, now);

                    int configMinutes = 0;
                    if (sysconf.getSysconfValue() != null && !sysconf.getSysconfValue().trim().isEmpty()) {
                        configMinutes = Integer.parseInt(sysconf.getSysconfValue().trim());
                    }
                    if (minutesBetween > configMinutes) {
                        wStop = 1;
                    }
                }
            //主機NPAY，每次最多回覆兩批
            }else if (!"AMPAYFL".equals(npsbatch.getNpsbatchFileId().trim())) {
                W_IMS_CNT++;
                if(W_IMS_CNT > 2){
                    job.writeLog("主機已回覆兩批次，此批號待下次排程執行,<" + npsbatch.getNpsbatchFileId().trim() + "> <" + npsbatch.getNpsbatchBatchNo().trim() + ">");
                    continue;
                }
            }

            // 3. 讀取NPSDTL(整批轉即時扣款檔)，組結果檔回覆前端
            String batno = StringUtils.rightPad(StringUtils.trimToEmpty(npsbatch.getNpsbatchFileId()), 8," ") +
                    StringUtils.trimToEmpty(npsbatch.getNpsbatchTxDate()) +
                    StringUtils.trimToEmpty(npsbatch.getNpsbatchBatchNo());
            List<Npsdtl> dtlList = npsdtlExtMapper.GetNPSDTLByBATNOforAllforBatchJob(batno, wStop, npsbatch.getNpsbatchResult());
            if (dtlList == null || dtlList.isEmpty()) {
                job.writeLog("無符合條件的NPSDTL(整批轉即時扣款檔)紀錄：" + batno);
                continue;
            }else if (dtlList.size() != npsbatch.getNpsbatchTotCnt()) {
                job.writeLog("NPSDTL(整批轉即時扣款檔)已完成交易筆數與NPSBATCH(整批轉即時批次檔)總筆數不符，需等待所有交易完成後再回覆結果檔：" + batno);
                continue;
            }

            // 統計變數
            int wOkCnt = 0;
            BigDecimal wOkAmt = BigDecimal.ZERO;
            int wFailCnt = 0;
            BigDecimal wFailAmt = BigDecimal.ZERO;
            int wTotCnt = 0;
            BigDecimal wTotAmt = BigDecimal.ZERO;
            int wFeeCnt = 0;
            BigDecimal wFeeAmt = BigDecimal.ZERO;

            // 組 messageOut 檔名: 長度 29
            String fileId = StringUtils.rightPad(StringUtils.trimToEmpty(npsbatch.getNpsbatchFileId()), 8, ' ');
            String messageFile = fileId + npsbatch.getNpsbatchBatchNo() + "230" + StringUtils.leftPad(String.valueOf(npsbatch.getNpsbatchTotCnt()),5,"0");

            // 組首筆 (11)
            String tita01 = npsbatch.getNpsbatch01Tita().substring(2); //db撈出來長度就是180
            String message11 = StringUtils.rightPad("11" + tita01, 230, ' ');

            StringBuilder message12Builder = new StringBuilder();
            // 組明細錄
            for (Npsdtl dtl : dtlList) {
                //交易處理結果：'':單筆交易已發送，00:交易成功，01:交易失敗，11:停止交易
                if (wStop == 1 && StringUtils.isBlank(dtl.getNpsdtlResult())) {
                    //已送出XML但尚未執行的交易
                    dtl.setNpsdtlResult("11"); // 停止交易
                    dtl.setNpsdtlErrMsg("批次回覆結果檔，22:30後執行或批號收到兩小時後，強迫回覆結果檔，未處理之交易視為失敗交易!!"); // [cite: 5]
                    dtl.setNpsdtlCbsRc("XXX");
                    dtl.setNpsdtlReplyCode("XXXX");
                    npsdtlExtMapper.updateByPrimaryKeySelective(dtl); // UPDATE 此筆 NPSDTL
                }

                String wTxType = "AB3W";
                if (sysstatHbkno.equals(dtl.getNpsdtlTroutBkno()) && sysstatHbkno.equals(dtl.getNpsdtlTrinBkno())) {
                    wTxType = "CB2W"; // 自行
                }

                // 轉民國年
                String tbsdyRoc = CalendarUtil.adStringToROCString(dtl.getNpsdtlTbsdy());
                String tita02 = dtl.getNpsdtl02Tita().substring(2); //db撈出來長度就是180
                String chargeTwo = StringUtils.right(StringUtils.leftPad(String.valueOf(dtl.getNpsdtlHostCharge()), 2, '0'), 2); // 取後兩位

                // 組明細錄
                String detailStr = "12" + tita02 +
                        StringUtils.rightPad(StringUtils.trimToEmpty(dtl.getNpsdtlBkno()), 3, ' ') +
                        StringUtils.rightPad(StringUtils.trimToEmpty(dtl.getNpsdtlStan()), 7, ' ') +
                        StringUtils.rightPad(StringUtils.trimToEmpty(dtl.getNpsdtlPcode()), 4, ' ') +
                        StringUtils.rightPad(wTxType, 4, ' ') +
                        StringUtils.rightPad(tbsdyRoc, 7, ' ') +
                        StringUtils.rightPad(StringUtils.trimToEmpty(dtl.getNpsdtlHostBrch()), 4, ' ') +
                        StringUtils.rightPad(StringUtils.trimToEmpty(dtl.getNpsdtlCbsRc()), 3, ' ') +
                        StringUtils.rightPad(StringUtils.trimToEmpty(dtl.getNpsdtlReplyCode()), 4, ' ') +
                        chargeTwo +
                        StringUtils.rightPad(StringUtils.trimToEmpty(dtl.getNpsdtlHostChargeFlag()), 1, ' ');

                message12Builder.append(StringUtils.rightPad(detailStr, 230, ' '));

                // 累計交易總筆數及金額
                wTotCnt++;
                wTotAmt = wTotAmt.add(dtl.getNpsdtlTxAmt() != null ? dtl.getNpsdtlTxAmt() : BigDecimal.ZERO);

                if ("00".equals(dtl.getNpsdtlResult())) {
                    wOkCnt++;
                    wOkAmt = wOkAmt.add(dtl.getNpsdtlTxAmt() != null ? dtl.getNpsdtlTxAmt() : BigDecimal.ZERO);

                    if (dtl.getNpsdtlFee() != null && dtl.getNpsdtlFee().compareTo(BigDecimal.ZERO) != 0) {
                        wFeeCnt++;
                        wFeeAmt = wFeeAmt.add(dtl.getNpsdtlFee());
                    }
                } else { //累計失敗筆數及金額
                    wFailCnt++;
                    wFailAmt = wFailAmt.add(dtl.getNpsdtlTxAmt() != null ? dtl.getNpsdtlTxAmt() : BigDecimal.ZERO);
                }
            }

            // 更新 NPSBATCH
            npsbatch.setNpsbatchRspTime(new Date());
            npsbatch.setNpsbatchResult("02"); // 已回結果檔
            npsbatch.setNpsbatchOkCnt(wOkCnt);
            npsbatch.setNpsbatchOkAmt(wOkAmt);
            npsbatch.setNpsbatchFailCnt(wFailCnt);
            npsbatch.setNpsbatchFailAmt(wFailAmt);
            npsbatch.setNpsbatchFeeCnt(wFeeCnt);
            npsbatch.setNpsbatchFeeAmt(wFeeAmt);
            npsbatch.setNpsbatchDoTotCnt(wTotCnt);
            npsbatch.setNpsbatchDoTotAmt(wTotAmt);

            try {
                npsbatchExtMapper.updateByPrimaryKeySelective(npsbatch);
            } catch (Exception ex) {
                job.writeLog("組扣帳結果檔明細錄後 Update NPSBATCH Error：" + npsbatch.getNpsbatchBatchNo());
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                logContext.setProgramException(ex);
                logContext.setRemark("Update NPSBATCH Error：" + npsbatch.getNpsbatchBatchNo());
                sendEMS(logContext);
            }

            // 組尾筆
            String wtOkCnt = StringUtils.leftPad(String.valueOf(wOkCnt), 6, '0');
            String wtOkAmt = StringUtils.leftPad(wOkAmt.multiply(new BigDecimal("100")).setScale(0).toPlainString(), 14, '0');
            String wtFailCnt = StringUtils.leftPad(String.valueOf(wFailCnt), 6, '0');
            String wtFailAmt = StringUtils.leftPad(wFailAmt.multiply(new BigDecimal("100")).setScale(0).toPlainString(), 14, '0');

            String tita09 = safeSubstring(npsbatch.getNpsbatch09Tita(), 2, 40);
            String message19 = StringUtils.rightPad("19" + tita09 + wtOkCnt + wtOkAmt + wtFailCnt + wtFailAmt, 230, ' ');

            String messageOut = messageFile + message11 + message12Builder.toString() + message19;
            // 寫 Response Log
            logContext.setProgramFlowType(ProgramFlow.MFTGWOut);
            logContext.setMessageFlowType(MessageFlow.Response);
            logContext.setProgramName("MFTServerReceiver.processResponseData");
            logContext.setRemark("MFTService Reveive Response");
            logMessage(logContext);

            // 整批處理結果電文(messageout) 回傳給MQ
            sendResponse(messageOut, npsbatch, batno);
            Thread.sleep(5000); //2026-06-03 瑜芳說需要間隔5s
        }

        return true;
    }

    // 整批處理結果電文(messageout) 回傳給MQ
    public void sendResponse (String messageOut, Npsbatch npsbatch, String batno) {
        try {
            String replyTo = "";
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            if ("AMPAYFL".equals(StringUtils.trimToEmpty(npsbatch.getNpsbatchFileId()))) {
                // 回傳整批處理結果電文給 MQ-NPAY (NPAY.FEP.BATCH.RS)
                replyTo = npsbatch.getNpsbatchRtq().substring(npsbatch.getNpsbatchRtq().lastIndexOf("/") + 1);
                if (StringUtils.isBlank(replyTo))  replyTo = "NPAY1.FEP.BATCH.RS";
            } else {
                // 回傳 MessageOut To queue 給 MQ-ZMQ (ZMQ.FEP.ONLINE.RS)
                replyTo = configuration.getQueueNames().getMftAck().getDestination(); //主機 (MQ-ZMQ)
            }
            job.writeLog("NpsBatchFileId 檢查: " + StringUtils.trimToEmpty(npsbatch.getNpsbatchFileId()));

            if (StringUtils.isNotBlank(replyTo)) {
                job.writeLog("準備發送 Ack Queue , replyTo=" +  replyTo + ", 批號:" + batno);
                logContext.setProgramName(StringUtils.join(ProgramName, ".sendResponse"));
                logContext.setRemark(StringUtils.join("Ready to send Ack Queue, replyTo=", replyTo));
                logContext.setMessage(messageOut);
                this.logMessage(logContext);

                JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
                String correlationID = npsbatch.getNpsbatchCorrelId();
                sender.sendQueue(replyTo, messageOut,null, new JmsHandler() {
                            @Override
                            public void setPropertyOut(Message jmsMessage) throws JMSException {
                                try {
                                    JmsFactory.setCharacterSet(jmsMessage, 937);

//                                    JmsFactory.setCorrelationID(jmsMessage, correlationID);
                                } catch (Exception e) {
                                    throw new JMSException("設定 JMS 屬性發生異常: " + e.getMessage());
                                }
                            }
                        });

                job.writeLog("發送 Ack Queue 成功, replyTo=" +  replyTo + ", 批號:" + batno);
                logContext.setRemark(StringUtils.join("Send Ack Queue succeed, replyTo=", replyTo));
                this.logMessage(logContext);
            } else {
                // replyTo 為空值的異常
                job.writeLog("無法發送 MQ 訊息：Queue (replyTo) 為空值。");
                logContext.setProgramName(StringUtils.join(ProgramName, ".sendResponse"));
                logContext.setRemark("無法發送 MQ 訊息：Queue 名稱 (replyTo) 為空值。");
                sendEMS(logContext);
            }

        }  catch (Exception e) {
            job.writeLog("發送 整批處理結果電文 至 MQ 時發生異常: " + e.getMessage());
            logContext.setProgramName(StringUtils.join(ProgramName, ".sendResponse"));
            logContext.setRemark("發送 整批處理結果電文 至 MQ 時發生異常");
            logContext.setProgramException(e);
            sendEMS(logContext);
        }
    }

    /**
     * 檢核手動啟動批次的傳入參數
     */
    public boolean checkManualBatchParams() {
        if (filename.trim().isEmpty() && txdate.trim().isEmpty() && batchno.trim().isEmpty()) {
            w_Date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        } else {
            if (filename.trim().length() != 8) {
                w_fileName = String.format("%-8s", filename);
            } else {
                w_fileName = filename.trim(); // 補上長度正確時的賦值
            }

            if (txdate.trim().length() != 8) {
                job.writeLog("手動啟動批次：交易日期長度錯誤，應為8位!!");
                return false;
            } else {
                w_Date = txdate;
            }

            if (batchno.trim().length() != 13) {
                job.writeLog("手動啟動批次：批號長度錯誤，應為13位!!");
                return false;
            } else {
                w_batchNo = batchno.trim();
            }
        }
        return  true;
    }

    private void initialBatch(String[] args) {
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(batchLogPath)) {
            System.out.println("Batch Log目錄未設定，請修正");
            return;
        }
        job = new BatchJobLibrary(this, args, batchLogPath);
    }

    /**
     * 安全擷取字串，避免 IndexOutOfBoundsException
     */
    private String safeSubstring(String str, int start, int length) {
        if (StringUtils.isBlank(str)) return "";
        if (str.length() <= start) return "";
        int end = Math.min(start + length, str.length());
        return str.substring(start, end);
    }

    private boolean checkConfig() throws Exception {
        //檔名
        if (job.getArguments().containsKey("FILENAME")) {
            filename = job.getArguments().get("FILENAME");
        }
        //交易日期(yyyymmdd)
        if (job.getArguments().containsKey("TXDATE")) {
            txdate = job.getArguments().get("TXDATE");
        }
        //批號
        if (job.getArguments().containsKey("BATCHNO")) {
            batchno = job.getArguments().get("BATCHNO");
        }
        return true;
    }
}