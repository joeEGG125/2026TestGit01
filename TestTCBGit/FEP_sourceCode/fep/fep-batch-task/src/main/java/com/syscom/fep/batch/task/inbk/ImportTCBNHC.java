package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ImportTCBNHC extends FEPBase implements Task {

    private String remotePath = StringUtils.EMPTY;//MFT 目錄
    private String filelocalPath = StringUtils.EMPTY;//MFT檔案抓到 local FEPAP的目錄
    private String fileTcbnhcPath = StringUtils.EMPTY;//FEP standAlone使用的資源檔目錄
    private String fileNames = StringUtils.EMPTY;
    private String[] fileNamesList;
    private String today = StringUtils.EMPTY;
    private String _BatchLogPath = StringUtils.EMPTY;
    private BatchReturnCode batchRC;

    private BatchJobLibrary job = null;

    @Override
    public BatchReturnCode execute(String[] args) {
        try {
            //初始化相關批次物件及拆解傳入參數
            this.initialBatch(args);
            job.writeLog("---------------------------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始!");
            //回報批次平台開始工作
            job.startTask();
            if (!this.checkConfig()) {
                job.abortTask();
                return BatchReturnCode.MissingArgument;
            }
            //批次主要處理流程
            if (doBusiness() == BatchReturnCode.Succeed) {
                job.writeLog(ProgramName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
                return batchRC;
            }
            return BatchReturnCode.Succeed;
        } catch (Exception ex) {
            //通知批次作業管理系統工作失敗,暫停後面流程
            try {
                job.writeLog(ex.toString());
                job.abortTask();
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog("------------------------------------------------------------------");
            } catch (Exception e) {
                logContext.setProgramException(e);
                logContext.setProgramName(ProgramName);
                sendEMS(logContext);
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

    private BatchReturnCode doBusiness() {
        FileInputStream fisList = null;
        today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        try {
            int filecnt = 1;
            Map<String, String> fileList = new HashMap<>();
            job.writeLog("批次將抓取下列字霸檔案:");
            fileNamesList = fileNames.split(";");
            for(String singlefileName : fileNamesList){
                fileList.put(Integer.toString(filecnt),singlefileName);
                job.writeLog(singlefileName);
                filecnt++;
            }
            job.writeLog("==================================================================");
            //3.MFT取得字霸檔 MFT to FEPAP
            for(int i = 1 ; i <= fileList.size() ; i++){
                String fileName = fileList.get(Integer.toString(i));
                if (!receiveMftFile(fileName)) {
                    job.writeLog("取MFT檔案失敗或不存在，停止批次執行。");
                    batchRC = BatchReturnCode.ProgramException;
                    return batchRC;
                } else {
                    //成功抓檔後，直接檢查檔案是否為空
                    job.writeLog("get MFT File:" + fileName + " 成功!!");
                    try {
                        Path singleFilePath = Paths.get(filelocalPath + fileName);
                        if (Files.exists(singleFilePath) && Files.size(singleFilePath) == 0) {
                            job.writeLog(fileName +" 為空檔，刪除檔案並停止批次執行。");
                            Files.delete(singleFilePath);
                            batchRC = BatchReturnCode.ProgramException;
                            return batchRC;
                        }else{
                            job.writeLog(fileName +" 下載成功。");
                        }
                    } catch (Exception e) {
                        job.writeLog("檢查檔案發生異常:" + e.getMessage());
                    }
                }
            }
            job.writeLog("==================================================================");
            //4.將檔案放到固定路徑並複製備份檔
            int i = 0;  //下載檔案數
            int j = 0; //下載檔案備份數
            int k = 0;  //下載檔案覆蓋數
            try {
                for (int x = 1; x <= fileList.size(); x++) {
                    String fileName = fileList.get(Integer.toString(x));
                    i++;
                    //備份檔案
                    Path sourcePath = Paths.get(filelocalPath + fileName); //最後需要delete
                    Path bkSourcePath = Paths.get(filelocalPath + fileName + ".bk" + today);
                    Path targetPath = Paths.get(fileTcbnhcPath + fileName);

                    //檢核下載後的檔案權限
                    if (!Files.exists(sourcePath)) {
                        job.writeLog(filelocalPath + fileName + " 檔案不存在!!");
                        return BatchReturnCode.ProgramException;
                    }
                    if (!Files.isReadable(sourcePath) || !Files.isWritable(sourcePath)) {
                        job.writeLog(filelocalPath + fileName + " 檔案無Read權限 or Write權限!!");
                        return BatchReturnCode.ProgramException;
                    }
                    //先檢核備份檔案權限，若無檔案，父目錄需具有Write權限
                    //再備份檔案
                    if (Files.exists(bkSourcePath)) {
                        if (Files.isReadable(bkSourcePath) && Files.isWritable(bkSourcePath)) {
                            Files.copy(sourcePath, bkSourcePath, StandardCopyOption.REPLACE_EXISTING);
                            j++;
                        }
                    }else if(Files.isWritable(bkSourcePath.getParent()) && Files.isReadable(bkSourcePath.getParent())) {
                        Files.copy(sourcePath, bkSourcePath, StandardCopyOption.REPLACE_EXISTING);
                        j++;
                    }else{
                        job.writeLog(filelocalPath + fileName + ".bk" + today + " 檔案之目錄無Read權限 or Write權限!!");
                        return BatchReturnCode.ProgramException;
                    }
                    //檔案覆蓋到AP使用檔案的固定路徑下的同檔名檔案 目的端使用者是自己 不檢核權限
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    k++;
                    job.writeLog(fileTcbnhcPath + fileName + " 檔案成功覆蓋!!");
                    //刪除importpath (收檔路徑)下的file
                    Files.delete(sourcePath);
                    job.writeLog("刪除收檔:"  + filelocalPath + fileName + " 成功!!");
                }
            }catch (Exception e) {
                job.writeLog("批次於覆蓋&備份過程有異常:" + e);
                batchRC = BatchReturnCode.ProgramException;
                return batchRC;
            }
            job.writeLog("==================================================================");
            //5.清除三個月前的備份檔
            String beforeThreeMonthsDate = LocalDate.now().minusMonths(3).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            if (!DeleteLocalFile(beforeThreeMonthsDate)) {
                return BatchReturnCode.ProgramException;
            }
            job.writeLog("==================================================================");
            //結果
            if(i != fileList.size()){
                job.writeLog("檔案備份數量:" + j + ", 檔案備份數量不符，待確認!!");
            }else if(k != fileList.size()) {
                job.writeLog("檔案覆蓋數量:" + k + ", 檔案覆蓋數量不符，待確認!!");
            }else{
                job.writeLog("檔案下載完成，檔案數:" + fileList.size() + ", 批次執行成功!!");
            }
            batchRC = BatchReturnCode.Succeed;
            return batchRC;
        } catch (Exception e) {
            job.writeLog("ImportTCBNHC批次發生異常:" + e);
            batchRC = BatchReturnCode.ProgramException;
            return batchRC;
        }
    }

    //MFT 下命令執行shellScript取檔
    //字霸MFT取檔(只有字霸這傳檔批次test跟Prod不同): 測試環境NODE:ithist 正式環境NODE:ITHIS02
    private boolean receiveMftFile(String singleFileName) {
        // Shell script
        String shellScript = "/mft/transferfile.sh";  //測試套是transferfile.sh
        String remoteFilePath = remotePath + singleFileName;
        String filePath = filelocalPath + singleFileName;
        String activation = "getbyOtherNode";

        String[] command = {"sudo", "-u", "mftfepap", shellScript, filePath, remoteFilePath, activation};
        Boolean getFile = true;
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
                if (line.contains("Error") || line.contains("Fail")) {
                    job.writeLog("get MFT file " + singleFileName + " may fail!!|" + line);
                    getFile = false;
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
            return getFile;
        } catch (Exception e) {
            getFile = false;
            e.printStackTrace();
            return getFile;
        }
    }

    private boolean DeleteLocalFile(String beforeThreeMonthsDate) {
        job.writeLog("即將刪除" + filelocalPath + "檔名中的.bkYYYYMMDD日期" + "小於" + beforeThreeMonthsDate + "的檔案");
        File directory = new File(filelocalPath);
        if (!directory.exists() || !directory.isDirectory()) {
            job.writeLog("目錄不存在或不是一個有效的目錄: " + filelocalPath);
            return true;
        }
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().contains(".bk"));

        if (files == null) {
            job.writeErrorLog(null, "讀取目錄時發生錯誤: " + filelocalPath);
            return false;
        }
        if (files.length == 0) {
            job.writeLog("在目錄 " + filelocalPath + " 中沒有找到任何 .bkYYYYMMDD 檔案。");
            return true;
        }

        // 1. 將截止日期轉換成 LocalDate
        LocalDate cutoffDate = LocalDate.parse(beforeThreeMonthsDate,DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate fileDate;
        int count = 0;
        for (File file : files) {
            // 2. 取得檔案日期, Ex: TB_NHC_UCS2.txt.bk20260101 -> 20260101
            try {
                fileDate = LocalDate.parse(file.getName().substring(file.getName().length() - 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (DateTimeParseException e) {
                job.writeErrorLog(e, "檔案名稱轉換為日期失敗: " + file.getName());
                return false; // 無法繼續，直接返回失敗
            }
            job.writeLog("開始比較" + file.getName() + "檔案日期: " + fileDate + " , 截止日期: " + cutoffDate);
            // 3. 如果檔案日期早於截止日期，則刪除
            if (fileDate.isBefore(cutoffDate)) {
                if (file.delete()) {
                    count++;
                    job.writeLog("刪除檔案成功: " + file.getName());
                } else {
                    job.writeLog("刪除檔案失敗: " + file.getName());
                    return false;
                }
            }else{
                job.writeLog("無須刪除!");

            }
            job.writeLog("--------------------------------------");
        }
        if (count == 0){
            job.writeLog("刪除檔案成功, 沒有檔案需要刪除");
        }
        return true;
    }

    /**
     * 初始化相關批次物件及拆解傳入參數
     *
     * @param args
     */
    private void initialBatch(String[] args) {
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);

        //2024-05-31更正
        // 檢查Batch Log目錄參數
        _BatchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(_BatchLogPath)) {
            LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
            return;
        }

        //初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, _BatchLogPath);
    }

    /**
     * 檢查是否為營業日
     *
     * @return
     * @throws Exception
     */
    private boolean checkConfig() throws Exception {

        //1-1. 檢查MFT 檔案目錄參數 (應為D:\astarfont\)
        remotePath = CMNConfig.getInstance().getTCBNHCMFTPath().trim();
        if (StringUtils.isBlank(remotePath)) {
            job.writeLog("MFT 檔案目錄未設定(應為D:\\astarfont\\)，請修正");
            return false;
        }
        job.writeLog("remotePath:" + remotePath);

        //1-2. 檢查MFT local檔案目錄參數 (應為/mftdata/mftfile/astar/)
        filelocalPath = CMNConfig.getInstance().getTCBNHCImportPath().trim();
        if (StringUtils.isBlank(filelocalPath)) {
            job.writeLog("MFT Local File 檔案目錄未設定(應為/mftdata/mftfile/astar/)，請修正");
            return false;
        }
        //1-3. 字霸 資源檔案放置目錄參數 (應為/fep/fep-app/lib/astar/)
        fileTcbnhcPath = CMNConfig.getInstance().getTCBNHCPath().trim();
        if (StringUtils.isBlank(fileTcbnhcPath)) {
            job.writeLog("字霸資源檔放置目錄未設定(應為/fep/fep-app/lib/astar/)，請修正");
            return false;
        }

        //1-4.
        //檢查FileName參數
        if (job.getArguments().containsKey("ImportFileName")) {
            fileNames = job.getArguments().get("ImportFileName");
        }
        return true;
    }

    public void safeClose(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException ex) {
                job.writeLog(ex.getMessage());
            }
        }
    }
}
