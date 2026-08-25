package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.QRMerchantExtMapper;
import com.syscom.fep.mybatis.model.QRMerchant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImportQRMERCHANT extends FEPBase implements Task {
    public static final String MS_950 = "MS950";
    private String remotePath = StringUtils.EMPTY;
    private String filelocalPath = StringUtils.EMPTY;
    private String fileName = StringUtils.EMPTY;
    private int countOK = 0;
    private String _BatchInputPath = StringUtils.EMPTY;
    private String _batchInputFile = StringUtils.EMPTY;
    private String _BatchLogPath = StringUtils.EMPTY;
    private BatchReturnCode batchRC;
    private BatchJobLibrary job = null;
    List<QRMerchant> merchantInsertList = new ArrayList<>();
    private QRMerchantExtMapper qrMerchantMapper = SpringBeanFactoryUtil.getBean(QRMerchantExtMapper.class);
    private FEPConfig fepConfig = SpringBeanFactoryUtil.getBean(FEPConfig.class);

    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" ImportQRMERCHANT Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /FILEID Required, EX:QRCodeStoreListYYYYMMDD.TXT");
        System.out.println();
        System.out.println(" EXAMPLES:");
        System.out.println(" /FILEID:QRCodeStoreListYYYYMMDD.TXT");
    }

    @Override
    public BatchReturnCode execute(String[] args) {
        batchRC = BatchReturnCode.Succeed;
        try {
            // 1. 初始化相關批次物件及拆解傳入參數
            batchRC = initialBatch(args);
            if (batchRC != BatchReturnCode.Succeed)
                return batchRC;
            // 2. 檢核批次參數是否正確, 若正確則啟動批次工作
            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始");
            job.startTask();
            if (!this.checkConfig(args)) {
                job.abortTask();
                return BatchReturnCode.MissingArgument;
            }
            // 3. 批次主要處理流程
            batchRC = mainProcess();
            // 4. 通知批次作業管理系統工作正常結束
            if (batchRC == BatchReturnCode.Succeed) {
                job.writeLog(ProgramName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
            }
            return batchRC;
        } catch (Exception e) {
            logContext.setProgramException(e);
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            if (job != null) {
                job.writeErrorLog(e, e.getMessage());
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog("------------------------------------------------------------------");
                // 通知批w作業管理系統工作失敗,暫停後面流程
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

    /**
     * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
     *
     * @param args
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
            return null;
        }

        //初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, _BatchLogPath);
        return BatchReturnCode.Succeed;
    }

    private boolean checkConfig(String[] args) throws Exception {
        if (args == null || args.length == 0 || "?".equals(args[0])) {
            displayUsage();
            return false;
        } else {
            //1.輸入參數 && 2.讀取檔案路徑
            //檢查MFT檔案目錄參數
            remotePath = INBKConfig.getInstance().getImportQRMERCHANTMFTPath().trim();
            if (StringUtils.isBlank(remotePath)) {
                job.writeLog("MFT 檔案目錄未設定，請修正");
                return false;
            }
            //檢查MERCHANT Local檔案放置目錄參數
            filelocalPath = INBKConfig.getInstance().getQRMERCHANTLocalPath().trim();
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
        }
        return true;
    }

    private BatchReturnCode mainProcess() throws Exception {
        FileInputStream fisList = null;

        //3.取得財金公司下載之消費扣款特約商店檔檔轉入 QRMERCHANT
        // MFT取檔至FEP
        if (!receiveMftFile()) {
            job.writeLog("get MFT File Fail!!");
            batchRC = BatchReturnCode.ProgramException;
            return batchRC;
        } else {
            job.writeLog("get MFT File Success!!");
        }


        //4.檢核下載檔案格式
        File baseDirectory = new File(CleanPathUtil.cleanString(filelocalPath));
        fisList = new FileInputStream(new File(baseDirectory, CleanPathUtil.cleanString(fileName)));
        if (fisList == null) {
            batchRC = BatchReturnCode.FileNotFound;
            return batchRC;
        }
        //5.資料檢核正確, 將下載檔案轉入 QRMERCHANT
        BufferedReader brList = new BufferedReader(new InputStreamReader(fisList, MS_950));
        //檢核
        batchRC = checkMerchantData(brList);
        if (batchRC != BatchReturnCode.Succeed) {
            job.writeLog("資料格式檢核失敗!!");
            return batchRC;
        }
        job.writeLog("資料檢核完成!!");
        //資料檢核正確, 將下載檔案剔除不必要資料, 再轉入 QRMERCHANT
        batchRC = batchInsertMerchant();
        if (batchRC != BatchReturnCode.Succeed) {
            job.writeLog("資料insert失敗!!");
            return batchRC;
        }

        //7.批次執行成功, 修改FTP Server文字檔副檔名由’.TXT’改為’.BAK’
        File a = new File(filelocalPath + fileName);
        File b = new File(filelocalPath + fileName.substring(0, fileName.length() - 3) + "BAK");
        a.renameTo(b);

        //8.RETURN BatchRC
        return BatchReturnCode.Succeed;
    }

    //MFT 下命令執行shellScript取檔
    private boolean receiveMftFile() {
        // Shell script
//        String shellScript = "/mft/recvfile.sh";        //開發套是recvfile.sh
        String shellScript = "/mft/transferfile.sh";  //測試套是transferfile.sh
//        String remotePath = "D:\\TCBFTP\\AP1T\\FEP\\SL\\";
        String remoteFilePath = remotePath + fileName;
        String filePath = filelocalPath + fileName;
        String activation = "get";

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
            e.printStackTrace();
        }
        return true;
    }

    public BatchReturnCode checkMerchantData(BufferedReader brList) {
        List<MerchantLengthsRule> MerchantLengthsRules = new ArrayList<>();
        MerchantLengthsRules.add(new MerchantLengthsRule(3, "銀行別"));
        MerchantLengthsRules.add(new MerchantLengthsRule(16, "特店代碼"));
        MerchantLengthsRules.add(new MerchantLengthsRule(46, "特店名稱"));
        MerchantLengthsRules.add(new MerchantLengthsRule(20, "特店簡稱"));
        MerchantLengthsRules.add(new MerchantLengthsRule(4, "MCC"));
        Set<String> tempList = new HashSet<>();
        String singlelineList;
        int readline = 0;
        int wrongSemicolonCount = 0;
        int wrongMccType = 0;
        int duplicateMerchantId = 0;
        int MerchantBank943 = 0;
        int MerchantTypeWrong = 0;
        boolean valid;
        try {
            while ((singlelineList = brList.readLine()) != null) {
                valid = true;
                readline++;
                //檢核是否有多餘分號
                String[] merchantSingleLine = singlelineList.split(";");
                if (merchantSingleLine.length != 5) {
                    wrongSemicolonCount++;
                    continue;
                }
                //檢核 剔除首欄銀行別為943
                if("943".equals(merchantSingleLine[0])){
                    MerchantBank943++;
                    continue;
                }
                //檢核是否有重複特店代碼
                if (!tempList.add(merchantSingleLine[1].trim())) {
                    duplicateMerchantId++;
                    continue;
                }
                //依格式檢核每個欄位長度(含空白)是否正確, 以分號(;)為分隔符號, 若檢核有誤,顯示”第X筆欄位資料長度錯誤”
                for (int i = 0; i < merchantSingleLine.length; i++) {
                    if (MerchantLengthsRules.get(i).length < getWordCountRegex(merchantSingleLine[i])) {
                        valid = false;
                    }
                }
                if (valid) {
                    QRMerchant merchantTmp = new QRMerchant();
                    merchantTmp.setQrMerchantBank(merchantSingleLine[0].trim());
                    merchantTmp.setQrMerchantId(merchantSingleLine[1].trim());
                    merchantTmp.setQrMerchantName(merchantSingleLine[2].trim());
                    merchantTmp.setQrMerchantAbbnm(merchantSingleLine[3].trim());
                    merchantTmp.setQrMerchantMcc(merchantSingleLine[4].trim());
                    merchantTmp.setUpdateUserid(0);
                    merchantTmp.setUpdateTime(new Date());

                    merchantInsertList.add(merchantTmp);
                } else {
                    MerchantTypeWrong++;
                }
            }

            job.writeLog("總資料數:" + readline + "筆!!");
            job.writeLog("剔除-多餘分號筆數:" + wrongSemicolonCount + "筆!!");
            job.writeLog("剔除-特店代號重複筆數:" + duplicateMerchantId + "筆!!");
            job.writeLog("剔除-MCC不為數值筆數:" + wrongMccType + "筆!!");
            job.writeLog("剔除-首欄銀行別為943筆數:" + MerchantBank943 + "筆!!");
            job.writeLog("剔除-資料欄位長度異常數:" + MerchantTypeWrong + "筆!!");
        } catch (Exception e) {
            job.writeLog(e);
            return BatchReturnCode.ProgramException;
        }
        return BatchReturnCode.Succeed;
    }

    //---------------------------
    public BatchReturnCode batchInsertMerchant() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            if (merchantInsertList.isEmpty()) {
                job.writeLog("無資料需要匯入!!批次直接結束!!");
                transactionManager.commit(txStatus);
                return BatchReturnCode.Succeed;
            }


            //清檔
            job.writeLog("Delete QRMERCHANT...");
            qrMerchantMapper.deleteQRMERCHANT();
            //寫檔
            job.writeLog("begin write file...");

            job.writeLog("應匯入筆數:" + merchantInsertList.size());

            List<QRMerchant> tBatchList = new ArrayList<>();
            int BatchSize = 500;

            for (QRMerchant merchantSingleLine : merchantInsertList) {
                tBatchList.add(merchantSingleLine);
                // 滿500 先存入 List<List<Merchant>> tBatchList
                if (tBatchList.size() == BatchSize) {
                    qrMerchantMapper.batchInsertQRMERCHANT(tBatchList);
                    countOK += tBatchList.size();
                    tBatchList.clear(); // 清掉後再收下一批
                }
            }
            //最後再把剩下的insert一次
            if (!tBatchList.isEmpty()) {
                qrMerchantMapper.batchInsertQRMERCHANT(tBatchList);
                countOK += tBatchList.size();
            }
            transactionManager.commit(txStatus);

            job.writeLog("QRMERCHANT一共新增:" + countOK + "筆!!");
        } catch (Exception e) {
            job.writeLog(e);
            transactionManager.rollback(txStatus);
            return BatchReturnCode.ProgramException;
        }
        return BatchReturnCode.Succeed;
    }

    public static int getWordCountRegex(String s) {
        s = s.replaceAll("[^\\x00-\\xff]", "**");
        int length = s.length();
        return length;
    }

    public static class MerchantLengthsRule {
        int length;
        String typeName;

        public MerchantLengthsRule(int length, String typeName) {
            this.length = length;
            this.typeName = typeName;
        }
    }
}
