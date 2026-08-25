package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Han
 */
public class ImportNPSUNIT extends FEPBase implements Task {

    public static final String MS_950 = "MS950";
    private String _programName = ImportNPSUNIT.class.getSimpleName(); //程式名稱
    private String batchLogPath = StringUtils.EMPTY;
    private String remotePath = StringUtils.EMPTY;
    private String filelocalPath = StringUtils.EMPTY;
    private String fileName = StringUtils.EMPTY;
    private String batchInputFile = StringUtils.EMPTY;
    private int reserveDays = 7;
    private BatchJobLibrary job = null;
    private LogData _logData;
    private String _BatchInputFilePath;   //全名
    private String _batchInputPath;
    private BatchReturnCode batchRC;
    private String effDate;
    private int countAll = 0;
    private int countOK = 0;
    private int countUpdateOK = 0;
    private int countInsertOK = 0;
    private NpsunitExtMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
    private FEPConfig fepConfig = SpringBeanFactoryUtil.getBean(FEPConfig.class);
    List<Npsunit> npsunitList = new ArrayList<>();

    @Override
    public BatchReturnCode execute(String[] args) {
        // 自動生成的方法存根
        try {
            // 1. 初始化相關批次物件及拆解傳入參數
            initialBatch(args);

            // 2. 檢核批次參數是否正確, 若正確則啟動批次工作
            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始");
            job.startTask();
            // 檢查公用變數值
            if (!this.checkConfig()) {
                job.stopBatch();
                return BatchReturnCode.ProgramException;
            }

            // 主流程
            if (doBusiness() == BatchReturnCode.Succeed) {
                job.writeLog(_programName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(_programName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
                return batchRC;
            }
            return BatchReturnCode.Succeed;
        } catch (Exception e) {
            logContext.setProgramException(e);
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            if (job != null) {
                //Send to System Event
                job.writeLog("Batch 發生例外 或 Batch Log目錄未設定，請修正");
            } else {
                job.writeErrorLog(e, e.getMessage());
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog("------------------------------------------------------------------");
                // 通知批w作業管理系統工作失敗,暫停後面流程
                try {
                    job.abortTask();
                    logContext.setProgramException(e);
                    logContext.setProgramName(ProgramName);
                    sendEMS(logContext);
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


    private BatchReturnCode doBusiness() {
        FileInputStream fis = null;
        FileInputStream fisList = null;
        try {
            //取檔
//            SckBatch sckBatch = new SckBatch();
//            String remoteFilePath="D:\\TCBFTP\\AP1T\\FEP\\payunt\\"+fileName;
//            sckBatch.SimpleFileReceiver("10.0.5.250", 46464, _BatchInputFilePath, remoteFilePath);
//            sckBatch.receiveSocketFile(MS_950);

            // MFT取檔至FEP
            if (!receiveMftFile()) {
                job.writeLog("get MFT File Fail!!");
                batchRC = BatchReturnCode.ProgramException;
                return batchRC;
            } else {
                job.writeLog("get MFT File Success!!");
            }

            //取檔
            String test = fepConfig.getTaskCmdStart();
            ProcessBuilder processBuilder = new ProcessBuilder(test, fileName);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);

            if (finished) {
                job.writeLog("Process finished within 30 seconds.");
            } else {
                job.writeLog("Process did not finish within 30 seconds.");
            }

            //預先掃所有資料長度是否符合 + 是否重複
            job.writeLog("Check file content...");
            File baseDirectory = new File(CleanPathUtil.cleanString(filelocalPath));
            fisList = new FileInputStream(new File(baseDirectory, CleanPathUtil.cleanString(fileName)));
            if (fisList == null) {
                batchRC = BatchReturnCode.FileNotFound;
                return batchRC;
            }
            BufferedReader brList = new BufferedReader(new InputStreamReader(fisList, MS_950));
            //檢核資料
            if (!checkNPSUNIT(brList)) {
                job.writeLog("格式檢核失敗......");
//                transactionManager.rollback(txStatus);
                batchRC = BatchReturnCode.DBIOError;
                return batchRC;
            }
            if (fisList != null) {
                safeClose(fisList);
            }
            job.writeLog("資料檢核完成!!");

            if (!TranNPSUNIT()) {
                job.writeLog("資料匯入異常!!");
                return BatchReturnCode.ProgramException;
            }
            job.writeLog("insert NPSUNIT End!!");
            //修改副檔名
            File a = new File(filelocalPath + fileName);
            File b = new File(filelocalPath + fileName.substring(0, fileName.length() - 3) + "BAK");
            job.writeLog("修改副檔名成功!!");
            a.renameTo(b);

            //刪除reserveDays天前的資料
            try {
                if (!DeleteLocalFile()) {
                    job.writeLog("刪除" + filelocalPath + "下舊檔案失敗!!");
                    return BatchReturnCode.ProgramException;
                }
            } catch (Exception e) {
                job.writeLog("刪除" + filelocalPath + "發生例外!!" + e.getMessage());
            }

            batchRC = BatchReturnCode.Succeed;
            return batchRC;
        } catch (Exception e) {
            job.writeLog(e);
            batchRC = BatchReturnCode.ProgramException;
            return batchRC;
        }
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

    //MFT 下命令執行shellScript取檔
    private boolean receiveMftFile() {
        // Shell script
//        String shellScript = "/mft/recvfile.sh";        //開發套是recvfile.sh
        String shellScript = "/mft/transferfile.sh";  //測試套是transferfile.sh
//        String remotePath = "D:\\TCBFTP\\AP1T\\FEP\\payunt\\";
        String activation = "get";
        String remoteFilePath = remotePath + fileName;
        String filePath = filelocalPath + fileName;

        String[] command = {"sudo", "-u", "mftfepap", shellScript, filePath, remoteFilePath, activation};
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
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean checkNPSUNIT(BufferedReader brList) {
        boolean valid = true;
        //存在List
        Map<String, String[]> dataNPSUNITMap = new HashMap<>();
        String singlelineList;
        int readline = 0;
        int duplicateKeyCount = 0;
        int fullyDuplicateCount = 0;
        try {
            while ((singlelineList = brList.readLine()) != null) {
                readline++;
                if (readline == 1) {
                    if (singlelineList.trim().length() == 6) {
                        job.writeLog("日期: " + singlelineList);
                        continue;
                    } else {
                        job.writeLog("首行日期格式有誤:" + singlelineList);
                        valid = false;
                    }
                }
                if ((getWordCountRegex(singlelineList) > 292) && (singlelineList.trim().length() != 6)) {
                    job.writeLog("資料長度錯誤(" + getWordCountRegex(singlelineList) + "), >>" + singlelineList + "<< 長度應為292");
                    continue;
                }
                if (readline > 1 && singlelineList.trim().length() == 6) {
                    if ((readline - 2) != Integer.parseInt(singlelineList.trim())) {
                        job.writeLog("資料筆數不符!! 總筆數: " + Integer.parseInt(singlelineList.trim()) + "筆.  實際筆數: " + (readline - 2) + "筆!!");
                        valid = false;
                    }
                } else {
                    String[] tempListArray = singlelineList.split("	");
                    //dataNPSUNITMap  HashMap<>()
                    String key = tempListArray[0].trim() + tempListArray[3].trim() + tempListArray[4].trim();
                    if (dataNPSUNITMap.containsKey(key)) {
                        if (tempListArray[8].equals(dataNPSUNITMap.get(key)[8])) {
                            job.writeLog("重複資料--" + tempListArray[0].trim() + "-"
                                    + tempListArray[3].trim() + "-"
                                    + tempListArray[4].trim() + "-"
                                    + tempListArray[8].trim());
                            fullyDuplicateCount++;
                            continue;
                        }
                        duplicateKeyCount++;
                    }
                    dataNPSUNITMap.put(key, tempListArray);
                }
            }
            if (!valid) {
                return valid;
            }

            for (String[] npsunitSingleData : dataNPSUNITMap.values()) {
                Npsunit npsunit = new Npsunit();
                npsunit.setNpsunitNo(npsunitSingleData[0].trim());
                npsunit.setNpsunitPaytype(npsunitSingleData[3].trim());
                npsunit.setNpsunitFeeno(npsunitSingleData[4].trim());
                npsunit.setNpsunitName(npsunitSingleData[1].trim());
                npsunit.setNpsunitNameS(npsunitSingleData[2].trim());
                npsunit.setNpsunitPayName(npsunitSingleData[5].trim());
                npsunit.setNpsunitBkno(npsunitSingleData[6].trim());
                npsunit.setNpsunitMonthlyFg(npsunitSingleData[7].trim());
                npsunit.setNpsunitEffectdate(npsunitSingleData[8].trim());
                npsunit.setNpsunitFee(new BigDecimal(npsunitSingleData[9].trim()));
                npsunit.setNpsunitRecvFee1(new BigDecimal(npsunitSingleData[10].trim()));
                npsunit.setNpsunitRecvFee2(new BigDecimal(npsunitSingleData[11].trim()));
                npsunit.setNpsunitRecvFee3(new BigDecimal(npsunitSingleData[12].trim()));
                npsunit.setNpsunitRecvFee4(new BigDecimal(npsunitSingleData[13].trim()));
                npsunit.setNpsunitOtherFee1(new BigDecimal(npsunitSingleData[14].trim()));
                npsunit.setNpsunitOtherFee2(new BigDecimal(npsunitSingleData[15].trim()));
                npsunit.setNpsunitOtherFee3(new BigDecimal(npsunitSingleData[16].trim()));
                npsunit.setNpsunitOtherFee4(new BigDecimal(npsunitSingleData[17].trim()));
                npsunit.setNpsunitOtherFee5(new BigDecimal(npsunitSingleData[18].trim()));
                npsunit.setNpsunitTrinBkno(npsunitSingleData[19].trim());
                npsunit.setNpsunitPaytypeName(npsunitSingleData[20].trim());
                npsunit.setNpsunitReFg(npsunitSingleData[21].trim());
                npsunit.setNpsunitUnitNo(Short.valueOf(npsunitSingleData[22].trim()));
                npsunit.setNpsunitPayKind(Short.valueOf(npsunitSingleData[23].trim()));
                npsunit.setNpsunitUnit(Short.valueOf(npsunitSingleData[24].trim()));
                npsunit.setUpdateUserid(0);
                npsunit.setUpdateTime(new Date());
                npsunitList.add(npsunit);
            }
            job.writeLog("全部重複筆數: " + fullyDuplicateCount);
            job.writeLog("8+5+4重複筆數: " + duplicateKeyCount);
            job.writeLog("準備匯入筆數: " + npsunitList.size());
            return true;
        } catch (Exception ex) {
            job.writeLog(ex.getMessage());
            return false;
        }
    }

    private boolean TranNPSUNIT() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Npsunit> npsunitBatchInsertList = new ArrayList<>();
        int batchSize = 1000;
        int countOK = 0;
        try {
            if (npsunitList.isEmpty()) {
                job.writeLog("無資料需要匯入!!批次直接結束!!");
                transactionManager.commit(txStatus);
                return true;
            }
            //清檔
            job.writeLog("Delete NPSUNIT");
            npsunitExtMapper.deleteNPSUNIT();

            //寫檔
            job.writeLog("begin write file...");


            for (Npsunit npsunit : npsunitList) {
                npsunitBatchInsertList.add(npsunit);
                if (npsunitBatchInsertList.size() == batchSize) {
                    npsunitExtMapper.batchInsertNPSUNIT(npsunitBatchInsertList);
                    countOK += npsunitBatchInsertList.size();
                    npsunitBatchInsertList.clear();// 清掉後再收下一批
                }
            }
            //最後再把剩下的insert一次
            if (!npsunitBatchInsertList.isEmpty()) {
                npsunitExtMapper.batchInsertNPSUNIT(npsunitBatchInsertList);
                countOK += npsunitBatchInsertList.size();
            }
            transactionManager.commit(txStatus);
            job.writeLog("NPSUNIT Table一共新增:" + countOK + "筆");
            return true;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            job.writeLog(ex.getMessage());
            return false;
        }
    }

    public static int getWordCountRegex(String s) {
        s = s.replaceAll("[^\\x00-\\xff]", "**");
        int length = s.length();
        return length;
    }

    /**
     * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
     *
     * @param args
     */
    private void initialBatch(String[] args) {
        // 0. 初始化logContext物件,傳入工作執行參數
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        // 1-1. 檢查Batch Log目錄參數
        batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(batchLogPath)) {
            job.writeLog("Batch Log目錄未設定，請修正");
            return;
        }
        // 2. 初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, batchLogPath);
        job.writeLog("ImportNPSUNIT start!");

        //設定公用變數
        _batchInputPath = CMNConfig.getInstance().getBatchInputPath().trim();
        if (StringUtils.isBlank(_batchInputPath)) {
            job.writeLog("參數BatchInputPath未設定");
        }
//		_batchInputPath ="D:/TXTT/";


    }


    private boolean checkConfig() throws Exception {
        //1-2. 檢查MFT檔案目錄參數
        remotePath = INBKConfig.getInstance().getImportNPSUNITMFTPath().trim();
        if (StringUtils.isBlank(remotePath)) {
            job.writeLog("MFT 檔案目錄未設定，請修正");
            return false;
        }
        //1-3. 檢查NPSUNIT Local檔案放置目錄參數
        filelocalPath = INBKConfig.getInstance().getNPSUNITLocalPath().trim();
        if (StringUtils.isBlank(filelocalPath)) {
            job.writeLog("Local File 放置目錄未設定，請修正");
            return false;
        }

        //抓檔名
        if (job.getArguments().containsKey("FILEID")) {
            fileName = job.getArguments().get("FILEID");
        }
        if (StringUtils.isBlank(fileName)) {
            job.writeLog("fileNanme未設定，請修正");
            return false;
        }

//		_batchInputPath ="D:/TXTT/";
        return true;
    }

    private boolean DeleteLocalFile() {
        File directory = new File(filelocalPath);
        if (!directory.exists() || !directory.isDirectory()) {
            job.writeLog("目錄不存在或不是一個有效的目錄: " + filelocalPath);
            return true;
        }
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().startsWith("payunt") && name.endsWith(".BAK"));

        if (files == null) {
            job.writeErrorLog(null, "讀取目錄時發生錯誤: " + filelocalPath);
            return false;
        }
        if (files.length == 0) {
            job.writeLog("在目錄 " + filelocalPath + " 中沒有找到任何 .BAK 檔案。");
            return true;
        }

        //取得reserveDays天前的日期
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        LocalDateTime archiveDate = LocalDateTime.now().minusDays(reserveDays);
        String deleteBeforeDate = DateTimeFormatter.ofPattern("yyyyMMdd").format(archiveDate);
        job.writeLog("開始刪除" + filelocalPath + "中超過" + reserveDays + "天(" + deleteBeforeDate + "以前)的.BAK檔案... ");

        for (File file : files) {
            String fileLastModifiedDate = simpleDateFormat.format(new Date(file.lastModified()));
            //fileLastModifiedDate 早於 deleteBeforeDate 小於 0
            if (fileLastModifiedDate.compareTo(deleteBeforeDate) < 0) {
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
}
