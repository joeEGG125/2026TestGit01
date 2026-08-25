package com.syscom.fep.batch.task.inbk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.MerchantExtMapper;
import com.syscom.fep.server.common.batch.SckBatch;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.model.Merchant;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 */
public class ImportMERCHANT extends FEPBase implements Task {

    public static final String MS_950 = "MS950";
    private String remotePath = StringUtils.EMPTY;
    private String filelocalPath = StringUtils.EMPTY;
    private String fileName = StringUtils.EMPTY;
    private String _BatchInputPath = StringUtils.EMPTY;
    private String _batchInputFile = StringUtils.EMPTY;
    private String _BatchLogPath = StringUtils.EMPTY;
    private BatchReturnCode batchRC;

    private BatchJobLibrary job = null;
    private String effDate = "";
    private int countOK = 0;
    List<Merchant> merchantInsertList = new ArrayList<>();

    private MerchantExtMapper merchantMapper = SpringBeanFactoryUtil.getBean(MerchantExtMapper.class);
    private FEPConfig fepConfig = SpringBeanFactoryUtil.getBean(FEPConfig.class);

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
            if (StringUtils.isBlank(_BatchInputPath)) {
                LogHelperFactory.getGeneralLogger().error("參數BatchInputPath未設定");
                batchRC = BatchReturnCode.MissingArgument;
                return batchRC;
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
                job.abortTask();
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog(ex.toString());
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
        try {
            //取檔
//            SckBatch sckBatch = new SckBatch();
//            String remoteFilePath="D:\\TCBFTP\\AP1T\\FEP\\SL\\"+_batchInputFile;
//            sckBatch.SimpleFileReceiver("10.0.5.250", 46464, _BatchInputPath+_batchInputFile, remoteFilePath);
//            sckBatch.receiveSocketFile();

            // MFT取檔至FEP
            if (!receiveMftFile()) {
                job.writeLog("get MFT File Fail!!");
                batchRC = BatchReturnCode.ProgramException;
                return batchRC;
            } else {
                job.writeLog("get MFT File Success!!");
            }

            //FEP下取檔
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
            //檢合格式
            batchRC = checkMerchantData(brList);
            if (batchRC != BatchReturnCode.Succeed) {
                job.writeLog("資料格式檢核失敗!!");
                return batchRC;
            }

            job.writeLog("資料檢核完成!!");

            //清檔 + 寫檔
            batchRC = batchInsertMerchant();
            if (batchRC != BatchReturnCode.Succeed) {
                job.writeLog("資料匯入異常!!");
                return batchRC;
            }
            if (fisList != null) {
                safeClose(fisList);
            }
            //修改副檔名
            File a = new File(filelocalPath + fileName);
            File b = new File(filelocalPath + fileName.substring(0, fileName.length() - 3) + "BAK");

            a.renameTo(b);
            batchRC = BatchReturnCode.Succeed;
            return batchRC;
        } catch (Exception e) {
            job.writeLog(e);
            batchRC = BatchReturnCode.ProgramException;
            return batchRC;
        }
    }

    //MFT 下命令執行shellScript取檔
    private boolean receiveMftFile() {
        // Shell script
//        String shellScript = "/mft/recvfile.sh";        //開發套是recvfile.sh
        String shellScript = "/mft/transferfile.sh";  //測試套是transferfile.sh
//        String remotePath = "D:\\TCBFTP\\AP1T\\FEP\\SL\\";
//        String remotePath = INBKConfig.getInstance().getImportMERCHANTMFTPath();
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

    private BatchReturnCode checkMerchantData(BufferedReader brList) {
        try {
            List<MerchantLengthsRule> MerchantLengthsRules = new ArrayList<>();
            MerchantLengthsRules.add(new MerchantLengthsRule(3, "銀行別"));
            MerchantLengthsRules.add(new MerchantLengthsRule(16, "特店代碼"));
            MerchantLengthsRules.add(new MerchantLengthsRule(46, "特店名稱"));
            MerchantLengthsRules.add(new MerchantLengthsRule(12, "特店簡稱"));
            MerchantLengthsRules.add(new MerchantLengthsRule(4, "MCC"));
            boolean valid;
            Set<String> tempList = new HashSet<>();
            List<String> errorLines = new ArrayList<>();
            String singlelineList;
            int readline = 0;
            int errorLine = 0;
            while ((singlelineList = brList.readLine()) != null) {
                valid = true;
                readline++;
                String[] merchantSingleLine = singlelineList.split(";");
                if (!tempList.add(merchantSingleLine[1].trim())) {
                    errorLine++;
                    valid = false;
                    errorLines.add("第" + readline + "筆, 特店代碼有重複!! >>" + merchantSingleLine[1].trim() + "<<");
                    continue;
                }
                //依格式檢核每個欄位長度(含空白)是否正確, 以分號(;)為分隔符號, 若檢核有誤,顯示”第X筆欄位資料長度錯誤”
                for (int i = 0; i < merchantSingleLine.length; i++) {
                    if (MerchantLengthsRules.get(i).length < getWordCountRegex(merchantSingleLine[i])) {
                        valid = false;
                        errorLines.add("第" + readline + "筆,"
                                + MerchantLengthsRules.get(i).typeName + "(" + MerchantLengthsRules.get(i).length + ")" + "資料長度有誤!!"
                                + ">>" + merchantSingleLine[i] + "<<" + "長度為" + merchantSingleLine[i].length());
                    }
                }
                if (valid) {
                    Merchant merchantTmp = new Merchant();
                    merchantTmp.setMerchantBank(merchantSingleLine[0].trim());
                    merchantTmp.setMerchantId(merchantSingleLine[1].trim());
                    merchantTmp.setMerchantName(merchantSingleLine[2].trim());
                    merchantTmp.setMerchantAbbnm(merchantSingleLine[3].trim());
                    merchantTmp.setMerchantMcc(merchantSingleLine[4].trim());
                    merchantTmp.setUpdateUserid(0);
                    merchantTmp.setUpdateTime(new Date());

                    merchantInsertList.add(merchantTmp);
                }else{
                    errorLine++;
                }
            }
            if (!errorLines.isEmpty()) {
                for (String errMsg : errorLines) {
                    job.writeLog(errMsg);
                }
            }
            job.writeLog("總資料數:" + readline + "筆!!");
            job.writeLog("剔除資料:" + errorLine + "筆!!");
        } catch (Exception e) {
            job.writeLog("資料檢核發生例外:" + e);
            return BatchReturnCode.ProgramException;
        }
        return BatchReturnCode.Succeed;
    }

    private BatchReturnCode batchInsertMerchant() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            if (merchantInsertList.isEmpty()) {
                job.writeLog("無資料需要匯入!!批次直接結束!!");
                transactionManager.commit(txStatus);
                return BatchReturnCode.Succeed;
            }
            //清檔
            job.writeLog("Delete MERCHANT...");
            merchantMapper.deleteMERCHANT();

            //寫檔
            job.writeLog("begin write file...");

            List<Merchant> merchantBatchInsert = new ArrayList<>();
            int batchSize = 1000;

            for(Merchant merchantSingleLine : merchantInsertList){
                merchantBatchInsert.add(merchantSingleLine);
                // 滿1000 先insert掉後，再收下一批
                if (merchantBatchInsert.size() == batchSize) {
                    merchantMapper.batchInsertMERCHANT(merchantBatchInsert);
                    countOK += merchantBatchInsert.size();
                    merchantBatchInsert.clear(); // 清掉後再收下一批
                }
            }
            //最後再把剩下的insert一次
            if (!merchantBatchInsert.isEmpty()) {
                merchantMapper.batchInsertMERCHANT(merchantBatchInsert);
                countOK += merchantBatchInsert.size();
            }

            transactionManager.commit(txStatus);
            job.writeLog("MERCHANT Table一共新增:" + countOK + "筆數");
        } catch (Exception e) {
            job.writeLog(e);
            transactionManager.rollback(txStatus);
            batchRC = BatchReturnCode.ProgramException;
            return batchRC;
        }
        return BatchReturnCode.Succeed;
    }

    private boolean TranNPSUNIT(String singleline) {
        boolean rtn = true;//是否正常跑完
        try {
            String[] array = singleline.split(";");
            Merchant merchant = new Merchant();
            merchant.setMerchantBank(array[0]);
            merchant.setMerchantId(array[1]);
            merchant.setMerchantName(array[2].trim());
            merchant.setMerchantAbbnm(array[3].trim());
            merchant.setMerchantMcc(array[4].trim());
            merchant.setUpdateUserid(0);
            merchant.setUpdateTime(new Date());
//            if (merchantMapper.selectByPrimaryKey(merchant.getMerchantId()) == null) {
            if (merchantMapper.insert(merchant) < 1) {
                return false;
            } else {
                countOK += 1;
                return true;
            }
//            } else {
//                if (merchantMapper.updateByPrimaryKey(merchant) < 1) {
//                    return false;
//                } else {
//                    countOK += 1;
//                    return true;
//                }
//            }

        } catch (Exception ex) {
            job.writeLog(ex.getMessage());
            rtn = false;
        }
        return rtn;
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


        //DB 抓BatchInputPath參數
        _BatchInputPath = CMNConfig.getInstance().getBatchInputPath();
        if (StringUtils.isBlank(_BatchInputPath)) {
            job.writeLog("參數BatchInputPath未設定");
            return false;
        }

        //1-2. 檢查MFT檔案目錄參數
        remotePath = INBKConfig.getInstance().getImportMERCHANTMFTPath().trim();
        if (StringUtils.isBlank(remotePath)) {
            job.writeLog("MFT 檔案目錄未設定，請修正");
            return false;
        }
        //1-3. 檢查MERCHANT Local檔案放置目錄參數
        filelocalPath = INBKConfig.getInstance().getMERCHANTLocalPath().trim();
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

    public void safeClose(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException ex) {
                job.writeLog(ex.getMessage());
            }
        }
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
