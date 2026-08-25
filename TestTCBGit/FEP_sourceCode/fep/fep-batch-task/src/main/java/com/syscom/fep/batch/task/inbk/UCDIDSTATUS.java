package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.UcdidExtMapper;
import com.syscom.fep.mybatis.model.Ucdid;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UCDIDSTATUS extends FEPBase implements Task  {
    private BatchJobLibrary job = null;
    private BatchReturnCode batchRC;
    private String batchLogPath = StringUtils.EMPTY;
    private boolean batchResult = false;
    private String remotePath = StringUtils.EMPTY;
    private String filelocalPath = StringUtils.EMPTY;
    private String keepLogDays = StringUtils.EMPTY;
    private String fileName = StringUtils.EMPTY;
    private String wk_DATE = StringUtils.EMPTY;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    private UcdidExtMapper ucdidExtMapper = SpringBeanFactoryUtil.getBean(UcdidExtMapper.class);
    private boolean nolist = false; //判定有無解除異常名單

    @Override
    public BatchReturnCode execute(String[] args) {
        batchRC = BatchReturnCode.Succeed;
        try {
            // 1. 初始化相關批次物件及拆解傳入參數
            batchRC = initialBatch(args);
            if (batchRC != BatchReturnCode.Succeed) {
                return batchRC;
            }

            // 2. 檢核批次參數是否正確, 若正確則啟動批次工作
            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始");
            job.startTask();

            if (!checkConfig(args)) {
                job.abortTask();
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
        if (!updateUcdidStatus()) {
            job.writeLog("Create Excel File Fail!!");
            return false;
        } else if(nolist) { //無解除異常名單
            return true;
        } else {
            job.writeLog("Create Excel File Success!!");
        }
        job.writeLog("開始上傳檔案... ");
        if (!uploadFTPFile()) {
            job.writeLog("Put FTP Fail!!");
            return false;
        } else {
            job.writeLog("Put FTP Success!!");
        }
        job.writeLog("開始刪除Server上的檔案... ");
        return DeleteLocalFile();
    }

    private boolean updateUcdidStatus() {
        List<Ucdid> ucdidList;
        LocalDate yesterday = LocalDate.now().minusDays(1);
        wk_DATE = yesterday.format(dateFormatter); // 取前一日的普發交易
        job.writeLog("查詢日期(系統日-1): " + wk_DATE);

        try {
            // 1.讀取UCDID(全民普發ID紀錄檔)
            ucdidList = ucdidExtMapper.selectForStatusUpdate(wk_DATE);
        } catch (Exception e) {
            job.writeLog("讀取 UCDID 資料庫發生錯誤: " + e);
            return false;
        }

        // 2.判斷查詢結果
        if (ucdidList == null || ucdidList.isEmpty()) {
            nolist = true;
            job.writeLog("本日無解除異常名單");
            return true; // 批次正常結束
        } else {
            String currentTime = LocalDateTime.now().format(dateTimeFormatter);
            job.writeLog("批次開始：" + currentTime);

            // 3.產出結果檔(Excel)
            fileName = "Cash10K_Exceptions_" + wk_DATE + ".xls";
            String filePath = filelocalPath + fileName;
            job.writeLog("開始產出結果檔: " + filePath);

            try (Workbook workbook = new HSSFWorkbook(); // HSSFWorkbook 對應 .xls 格式
                 FileOutputStream fileOut = new FileOutputStream(filePath)) {

                Sheet sheet = workbook.createSheet("解除異常名單");

                // 1.建立標題列 (第一列)
                String[] headers = {
                        "身分證字號", "健保卡卡號", "身分驗證交易序號", "身分驗證交易時間",
                        "提領交易序號", "提領交易時間", "提領註記登錄序號", "財金回覆處理序號",
                        "登錄回應時間", "身分驗證交易狀態", "提領交易狀態", "最後狀態"
                };
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                // 2.第二列開始寫入資料列
                int rowNum = 1;
                for (Ucdid ucdid : ucdidList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(ucdid.getUcdidIdno());
                    row.createCell(1).setCellValue(ucdid.getUcdidHealthid());
                    row.createCell(2).setCellValue(ucdid.getUcdid2566Stan());
                    row.createCell(3).setCellValue(formatTimeHHMMSS(ucdid.getUcdid2566Txtime()));
                    row.createCell(4).setCellValue(ucdid.getUcdid2510Stan());
                    row.createCell(5).setCellValue(formatTimeHHMMSS(ucdid.getUcdid2510Txtime()));
                    row.createCell(6).setCellValue(ucdid.getUcdidUcdApiseqno());
                    row.createCell(7).setCellValue(ucdid.getUcdidUcdTxno());
                    row.createCell(8).setCellValue(formatDateTime(ucdid.getUcdidUcdResptime()));
                    // 身分驗證交易狀態
                    row.createCell(9).setCellValue(get2566StatusDesc(ucdid.getUcdid2566Apistatus()));
                    row.createCell(10).setCellValue(get2510StatusDesc(ucdid.getUcdid2510Status()));
                    row.createCell(11).setCellValue(getLastStatusDesc(ucdid.getUcdidLaststatus()));
                }
                // 自動調整欄位長度
                for (int i = 0; i < headers.length; i++) {
                    sheet.setColumnWidth(i, 21 * 256);
                }

                // 3. 將 Workbook 內容寫入檔案
                workbook.write(fileOut);
                job.writeLog("結果檔產出成功。");
            } catch (IOException e) {
                job.writeLog("UCDIDSTATUS 產檔失敗: " + e.getMessage());
                return false;
            }catch (Exception e) {
                job.writeLog("UCDIDSTATUS 未預期失敗: " + e.getMessage());
                return false;
            }

            // 4.記錄結束日誌
            String endTime = LocalDateTime.now().format(dateTimeFormatter);
            job.writeLog("批次結束：" + endTime + "，處理筆數：" + ucdidList.size());

            return true;
        }
    }

    private boolean uploadFTPFile() {
//        String shellScript = "/mft/recvfile.sh";      //開發套是recvfile.sh
        String shellScript = "/mft/transferfile.sh";  //測試套是transferfile.sh
        String remoteFilePath = remotePath + fileName;
        String filePath = filelocalPath + fileName;
        String activation = "put";

        job.writeLog("開始連接FTP Server...");
//        job.writeLog("FTP Host: " + ftpHost + ":" + ftpPort);
        job.writeLog("Remote Path: " + remoteFilePath);
        job.writeLog("Local Path: " + filePath);

        String[] command = {"sudo", "-u", "mftfepap", shellScript, filePath, remoteFilePath, activation}; //開發套 mftuser
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("Progress...")) {
                    job.writeLog(line);
                }
            }

            // 等待執行結果
//            int exitCode = process.waitFor();
//            job.writeLog("Exited with code: " + exitCode);
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (finished) {
                int exitCode = process.exitValue();
                job.writeLog("Exited with code: " + exitCode);
            } else {
                process.destroyForcibly();
                job.writeLog("MFT transfer process timeout, force stop.");
                return false;
            }
        } catch (Exception e) {
            job.writeLog("Error: " + e.getMessage());
        }
        return true;
    }

    private boolean DeleteLocalFile() {
        File directory = new File(filelocalPath);
        if (!directory.exists() || !directory.isDirectory()) {
            job.writeLog("目錄不存在或不是一個有效的目錄: " + filelocalPath);
            return false;
        }
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().startsWith("Cash".toLowerCase()) && name.toLowerCase().endsWith(".xls"));

        if (files == null) {
            job.writeErrorLog(null, "讀取目錄時發生錯誤: " + filelocalPath);
            return false;
        }
        if (files.length == 0) {
            job.writeLog("在目錄 " + filelocalPath + " 中沒有找到任何 .xls 檔案。");
            return true;
        }

        // 1. 將截止日期轉換成 LocalDate
        LocalDate cutoffDate;
        LocalDate fileDate;
        try {
            int daysToKeep = Integer.parseInt(keepLogDays);
            cutoffDate = LocalDate.now().minusDays(daysToKeep);
        } catch (NumberFormatException e) {
            job.writeErrorLog(e, "KeepLogDays 參數設定錯誤，不是一個有效的數字: " + keepLogDays);
            return false; // 無法繼續，直接返回失敗
        }

        for (File file : files) {
            // 2. 取得檔案日期, Ex: Cash10K_Exceptions_20250711.xls -> 20250711
            try {
            //  fileDate = LocalDate.parse(file.getName().substring(19, 27), DateTimeFormatter.ofPattern("yyyyMMdd"));
                fileDate = Instant.ofEpochMilli(file.lastModified())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } catch (DateTimeParseException e) {
                job.writeErrorLog(e, "檔案名稱轉換為日期失敗: " + file.getName());
                return false; // 無法繼續，直接返回失敗
            }
            job.writeLog("開始比較檔案日期: " + fileDate + " , 截止日期: " + cutoffDate);
            // 3. 如果檔案日期早於截止日期，則刪除
            if (fileDate.isBefore(cutoffDate)) {
                if (file.delete()) {
                    job.writeLog("刪除檔案成功: " + file.getName());
                } else {
                    job.writeLog("刪除檔案失敗: " + file.getName());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 轉換身分驗證交易狀態為描述文字
     */
    private String get2566StatusDesc(String status) {
        if (status == null) return "";
        switch (status) {
            case "1": return "1:已註記提領";
            case "A": return "A:註記提領異常(API未回應)";
            case "2": return "2:已解除提領";
            case "B": return "B:解除註記異常(API未回應)";
            default: return status;
        }
    }

    /**
     * 轉換提領交易狀態為描述文字
     */
    private String get2510StatusDesc(String status) {
        if (status == null) return "";
        switch (status) {
            case "0": return "0:已入帳未取鈔";
            case "3": return "3:提領成功";
            case "4": return "4:沖正解除提領";
            case "B": return "B:沖正解除異常(API未回應)";
            default: return status;
        }
    }

    /**
     * 轉換最後狀態為描述文字
     */
    private String getLastStatusDesc(String status) {
        if (status == null) return "";
        switch (status) {
            case "F": return "F:身分驗證(2566)";
            case "A": return "A:領取(2510)";
            case "C": return "C:沖正(API解除成功)";
            default: return status;
        }
    }

    /**
     * 將 HHMMSS 時間格式轉換為 HH:MM:SS
     */
    private String formatTimeHHMMSS(String hhmmss) {
        if (hhmmss == null || hhmmss.length() != 6) {
            return hhmmss;
        }

        try {
            String hh = hhmmss.substring(0, 2);
            String mm = hhmmss.substring(2, 4);
            String ss = hhmmss.substring(4, 6);
            // 組合回 HH:MM:SS 格式
            return hh + ":" + mm + ":" + ss;
        } catch (Exception e) {
            job.writeLog("時間格式轉換失敗，原始值: " + hhmmss);
            return hhmmss;
        }
    }

    /**
     * 將 yyyyMMddHHmmss 格式轉換為 YYYY/MM/DD HH:MM:SS
     */
    private String formatDateTime(String yyyyMMddHHmmss) {
        if (StringUtils.isBlank(yyyyMMddHHmmss)) {
            return "";
        }
        if (yyyyMMddHHmmss.length() != 14) {
            return yyyyMMddHHmmss;
        }

        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

            LocalDateTime dateTime = LocalDateTime.parse(yyyyMMddHHmmss, inputFormatter);
            return dateTime.format(outputFormatter);
        } catch (Exception e) {
            job.writeLog("日期時間格式轉換失敗，原始值: " + yyyyMMddHHmmss);
            return yyyyMMddHHmmss;
        }
    }

    /**
     * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
     *
     * @param args
     */
    private BatchReturnCode initialBatch(String[] args) {
        // 初始化logContext物件,傳入工作執行參數
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        // 檢查Batch Log目錄參數
        batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(batchLogPath)) {
            LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
            return BatchReturnCode.ProgramException;
        }

        // 初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, batchLogPath);
        if (ArrayUtils.isNotEmpty(args)) {
            for (String arg : args) {
                job.writeLog("接收的參數:", arg);
            }
        }
        return BatchReturnCode.Succeed;
    }

    private boolean checkConfig(String[] args) {
        if (args == null || args.length == 0 || "?".equals(args[0])) {
            return false;
        } else {
            //檢查Log檔保留天數
            keepLogDays = job.getArguments().getOrDefault("KeepLogDays", "7");

            //檢查MFT檔案目錄參數
            if (job.getArguments().containsKey("RemotePath")) {
                remotePath = job.getArguments().get("RemotePath");
            } else {
                job.writeLog("FTP 檔案目錄未設定，請修正");
                return false;
            }

            if (job.getArguments().containsKey("LocalPath")) {
                filelocalPath = job.getArguments().get("LocalPath");
            } else {
                job.writeLog("Local 檔案目錄未設定，請修正");
                return false;
            }
        }
        return true;
    }
}
