package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.GraylistExtMapper;
import com.syscom.fep.mybatis.model.Graylist;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ImportGrayList extends FEPBase implements Task {
    private String remotePath = StringUtils.EMPTY;
    private String filelocalPath = StringUtils.EMPTY;
    private String fileName = StringUtils.EMPTY;
    private String keepLogDays = StringUtils.EMPTY;
    private String _BatchLogPath = StringUtils.EMPTY;
    private BatchReturnCode batchRC;
    private BatchJobLibrary job = null;
    List<Graylist> graylistInsertList = new ArrayList<>();

    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" ImportGrayList Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /LocalPath Required, EX:/mftdata/mftfile/GRL/");
        System.out.println(" /RemotePath Required, EX:D:\\TCBFTP\\AP1T\\FEP\\");
//        System.out.println(" /FileName Required, EX:graylist20250711.txt");
    }

    @Override
    public BatchReturnCode execute(String[] args) {
        batchRC = BatchReturnCode.Succeed;
        try {
            // 1. 初始化相關批次物件及拆解傳入參數
            batchRC = initialBatch(args);
            if (batchRC != BatchReturnCode.Succeed)
                return batchRC;

            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始!");
            job.startTask();

            // 2. 檢核批次參數是否正確
            if (!checkConfig(args)) {
                job.writeLog(ProgramName + "配置錯誤");
                job.stopBatch();
                return BatchReturnCode.ProgramException;
            }

            // 3. 批次主要處理流程
            batchRC = mainProcess();

            // 4. 通知批次作業管理系統工作正常完成
            if (batchRC == BatchReturnCode.Succeed) {
                job.writeLog(ProgramName + "正常完成!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(ProgramName + "不正常完成，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
            }
            return batchRC;
        } catch (Exception e) {
            logContext.setProgramException(e);
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

    /**
     * 初始化相關批次物件及拆解傳入參數
     */
    private BatchReturnCode initialBatch(String[] args) {
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        //1. 	輸入參數
        // 檢查Batch Log目錄參數
        _BatchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(_BatchLogPath)) {
            LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
            return BatchReturnCode.ProgramException;
        }

        //初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, _BatchLogPath);
        if (ArrayUtils.isNotEmpty(args)) {
            for (String arg : args) {
                job.writeLog("接收的參數:", arg);
            }
        }
        return BatchReturnCode.Succeed;
    }

    private boolean checkConfig(String[] args) throws Exception {
        if (args == null || args.length == 0 || "?".equals(args[0])) {
            displayUsage();
            return false;
        } else {
            //檢查MFT檔案目錄參數
            if (job.getArguments().containsKey("RemotePath")) {
                remotePath = job.getArguments().get("RemotePath");
            } else {
                job.writeLog("FTP 檔案目錄未設定，請修正");
                return false;
            }

            //檢查Log檔保留天數
            keepLogDays = job.getArguments().getOrDefault("KeepLogDays", "7");

            //檢查GrayList Local檔案放置目錄參數
            filelocalPath = job.getArguments().getOrDefault("LocalPath", "/mftdata/mftfile/GRL/");

            //檢查FileName參數
            if (job.getArguments().containsKey("FileName")) {
                fileName = job.getArguments().get("FileName");
                if (fileName.contains("{ADDATE}")) {
                    fileName = fileName.replace("{ADDATE}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                }
            } else {
                fileName = "ATM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            }
        }
        return true;
    }

    private BatchReturnCode mainProcess() throws Exception {
        //3.FTP取檔至Local
        if (!DownloadFTPFile()) {
            job.writeLog("Get FTP File Fail!!");
            batchRC = BatchReturnCode.FileNotFound;
            return batchRC;
        } else {
            job.writeLog("Get FTP File Success!!");
        }

        //4.檢核下載檔案格式
        File baseDirectory = new File(filelocalPath);
        File targetFile = new File(baseDirectory, CleanPathUtil.cleanString(fileName));
        job.writeLog("開始檢核下載檔案... ");
        if (!targetFile.exists()) {
            job.writeLog("檔案不存在: " + targetFile.getAbsolutePath());
            batchRC = BatchReturnCode.Succeed;
            return batchRC;
        }
        if (!targetFile.canRead()) {
            job.writeLog("檔案無法讀取: " + targetFile.getAbsolutePath());
            batchRC = BatchReturnCode.FileNotFound;
            return batchRC;
        }

        //5.資料檢核, 將下載檔案轉入 GrayList
        FileInputStream fisList = new FileInputStream(targetFile);
        BufferedReader brList = new BufferedReader(new InputStreamReader(fisList, StandardCharsets.UTF_8));
        job.writeLog("開始依規則分割檔案內容... ");
        batchRC = checkGrayListData(brList);
        if (batchRC != BatchReturnCode.Succeed) {
            job.writeLog("資料格式檢核失敗!!");
            return batchRC;
        }

        //6.資料檢核正確, 將GrayList轉入資料庫
        job.writeLog("開始將資料轉入資料庫... ");
        batchRC = InsertGrayListData();
        if (batchRC != BatchReturnCode.Succeed) {
            job.writeLog("資料轉入資料庫失敗!!");
            return batchRC;
        }
        job.writeLog("資料轉入資料庫完成!!");

        //7.批次執行成功, 修改Local Server文字檔副檔名由'.TXT'改為'.BAK'
        File originalFile = new File(filelocalPath + fileName);
        File backupFile = new File(filelocalPath + fileName.substring(0, fileName.length() - 3) + "BAK");
        if (originalFile.renameTo(backupFile)) {
            job.writeLog("檔案重新命名成功: " + backupFile.getName());
        } else {
            job.writeLog("檔案重新命名失敗");
        }

        //8.刪除LocalPath中超過keeplogdays的.bak檔案
        if (!DeleteLocalFile()) {
            return BatchReturnCode.ProgramException;
        }

        //9.RETURN BatchRC
        return BatchReturnCode.Succeed;
    }

    private boolean DeleteLocalFile() {
        job.writeLog("開始刪除" + filelocalPath + "中超過" + keepLogDays + "天的.bak檔案... ");
        File directory = new File(filelocalPath);
        if (!directory.exists() || !directory.isDirectory()) {
            job.writeLog("目錄不存在或不是一個有效的目錄: " + filelocalPath);
            return true;
        }
        String findname;
        if (fileName.length() > 12) {
            findname = fileName.substring(0, fileName.length() - 12);
        } else {
            findname = "ATM";
        }
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().startsWith(findname.toLowerCase()) && name.toLowerCase().endsWith(".bak"));

        if (files == null) {
            job.writeErrorLog(null, "讀取目錄時發生錯誤: " + filelocalPath);
            return false;
        }
        if (files.length == 0) {
            job.writeLog("在目錄 " + filelocalPath + " 中沒有找到任何 .bak 檔案。");
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
        int count = 0;
        for (File file : files) {
            // 2. 取得檔案日期, Ex: ATM20250711.bak -> 20250711
            try {
                fileDate = LocalDate.parse(file.getName().substring(file.getName().length() - 12, file.getName().length() - 4), DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (DateTimeParseException e) {
                job.writeErrorLog(e, "檔案名稱轉換為日期失敗: " + file.getName());
                return false; // 無法繼續，直接返回失敗
            }
            job.writeLog("開始比較檔案日期: " + fileDate + " , 截止日期: " + cutoffDate);
            // 3. 如果檔案日期早於截止日期，則刪除
            if (fileDate.isBefore(cutoffDate)) {
                if (file.delete()) {
                    count++;
                    job.writeLog("刪除檔案成功: " + file.getName());
                } else {
                    job.writeLog("刪除檔案失敗: " + file.getName());
                    return false;
                }
            }
        }
        if (count == 0){
            job.writeLog("刪除檔案成功, 沒有檔案需要刪除");
        }
        return true;
    }

    /**
     * 從FTP Server下載檔案到本地目錄
     */
    private boolean DownloadFTPFile() {
//        String shellScript = "/mft/recvfile.sh";      //開發套是recvfile.sh
        String shellScript = "/mft/transferfile.sh";  //測試套是transferfile.sh
        String remoteFilePath = remotePath + fileName;
        String filePath = filelocalPath + fileName;
        String activation = "get";

        job.writeLog("開始連接FTP Server...");
        job.writeLog("Remote Path: " + remoteFilePath);
        job.writeLog("Local Path: " + filePath);

        String[] command = {"sudo", "-u", "mftfepap", shellScript, filePath, remoteFilePath, activation, remotePath, fileName}; //開發套 mftuser
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
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (finished) {
                int exitCode = process.exitValue();
                job.writeLog("Exited with code: " + exitCode);
                return exitCode == 0;
            } else {
                process.destroyForcibly();
                job.writeLog("MFT transfer process timeout, force stop.");
                return false;
            }
        } catch (Exception e) {
            job.writeLog("Error: " + e.getMessage());
            return false;
        }
    }

    public BatchReturnCode checkGrayListData(BufferedReader brList) {
        Set<String> tempList = new HashSet<>();
        String singlelineList;
        int readline = 0;
        int wrongSemicolonCount = 0;
        try {
            while ((singlelineList = brList.readLine()) != null) {
                Graylist graylist = new Graylist();
                readline++;
                singlelineList = singlelineList.trim();
                //檢核GrayList長度
                if (singlelineList.length() != 55) {
                    job.writeLog("第" + readline + "行長度檢核錯誤, 長度為:" + singlelineList.length());
                    wrongSemicolonCount++;
                    continue;
                }
                graylist.setAllbankBkno(singlelineList.substring(0, 3).trim()); //轉出銀行代號
                graylist.setAccountNo(StringUtils.leftPad(singlelineList.substring(3, 19).trim(), 16, "0")); //轉出帳號
                if (!tempList.add(graylist.getAllbankBkno() + graylist.getAccountNo())) {
                    wrongSemicolonCount++;
                    job.writeLog("第" + readline + "筆, 帳號有重複!! >>" + graylist.getAllbankBkno() + graylist.getAccountNo() + "<<");
                    continue;
                }
                graylist.setWatchStartDate(singlelineList.substring(19, 27).trim()); //監視起始日
                graylist.setWatchEndDate(singlelineList.substring(27, 35).trim()); //監視結束日
                graylist.setPhone1(singlelineList.substring(35, 45).trim()); //電話1
                graylist.setPhone2(singlelineList.substring(45, 55).trim()); //電話2
                graylist.setSourceSystem("警政署");

                graylistInsertList.add(graylist);
            }
            if (readline == 0) {
                job.writeLog("灰名單檔為空檔");
                return BatchReturnCode.FileNotFound;
            }
            job.writeLog("總資料筆數:" + readline + "筆");
            job.writeLog("檢核錯誤筆數:" + wrongSemicolonCount + "筆");
        } catch (Exception e) {
            job.writeErrorLog(e, "資料檢核失敗");
            return BatchReturnCode.ProgramException;
        }
        return BatchReturnCode.Succeed;
    }

    @Transactional
    public BatchReturnCode InsertGrayListData() {
        GraylistExtMapper graylistextMapper = SpringBeanFactoryUtil.getBean(GraylistExtMapper.class);
        try {
            if (graylistInsertList.isEmpty()) {
                job.writeLog("無資料需要匯入!!");
                return BatchReturnCode.Succeed;
            }
            int totalCount = 0;
            int UpdateCount = 0;
            int InsertCount = 0;
            int deletedCount = 0;

            // 先標記現有資料
            job.writeLog("開始標記待刪除資料..."); //更新PRE_PROCESS_NOTE='*'
            if (graylistextMapper.checkGraylist()) {
                graylistextMapper.updateGraylist();
            } else {
                job.writeLog("資料庫中無資料可標記");
            }

            job.writeLog("開始寫入資料庫, 應匯入筆數:" + graylistInsertList.size());

            for (Graylist graylistSingleLine : graylistInsertList) {
                graylistSingleLine.setPreProcessNote("");
                graylistSingleLine.setUpdateTime(new Date());
                totalCount++;
                try {
                    int affectedRows = graylistextMapper.updateByPrimaryKeySelective(graylistSingleLine); //更新PRE_PROCESS_NOTE = ''
                    if (affectedRows > 0) {
                        UpdateCount++;
                    } else { // 資料不存在，進行新增
                        graylistSingleLine.setImportTimestamp(new Date());
                        graylistextMapper.insert(graylistSingleLine); //新增PRE_PROCESS_NOTE = ''
                        InsertCount++;
                    }
                } catch (Exception e) {
                    job.writeLog("第" + totalCount + "筆寫入或更新資料庫錯誤:" + e.getMessage());
                }
            }
            try {
                job.writeLog("寫入檔案完成, 一共新增:" + InsertCount + "筆, 一共更新:" + UpdateCount + "筆");
                job.writeLog("開始刪除被標記資料...");
                deletedCount = graylistextMapper.deleteByPreProcessNote(); //刪除PRE_PROCESS_NOTE = '*'的資料
            } catch (Exception e) {
                job.writeLog("刪除被標記資料錯誤:" + e.getMessage());
            }
            job.writeLog("一共刪除:" + deletedCount + "筆");
        } catch (Exception e) {
            job.writeErrorLog(e, "資料庫操作失敗:" + e.getMessage());
            return BatchReturnCode.ProgramException;
        }
        return BatchReturnCode.Succeed;
    }
}
