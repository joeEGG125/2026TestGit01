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
import com.syscom.fep.mybatis.ext.mapper.UpbinExtMapper;
import com.syscom.fep.mybatis.model.Upbin;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author vincent
 */
public class ImportUPBIN extends FEPBase implements Task {
    public static final String BIG_5 = "BIG5";
    private String fileName = StringUtils.EMPTY;
    private String remotePath = StringUtils.EMPTY;
    private String filelocalPath = StringUtils.EMPTY;
    private int reserveDays = 7;
    private String _programName = ImportUPBIN.class.getSimpleName(); // 程式名稱
    private String batchLogPath = StringUtils.EMPTY;
    private BatchJobLibrary job = null;
    private LogData _logData;
    private BatchReturnCode batchRC;
    private String _batchInputPath;


    private UpbinExtMapper upbinExtMapper = SpringBeanFactoryUtil.getBean(UpbinExtMapper.class);

    private FEPConfig fepConfig = SpringBeanFactoryUtil.getBean(FEPConfig.class);

    @Override
    public BatchReturnCode execute(String[] args) {
        // 自動生成的方法存根
        try {

            // 0. 初始化相關批次物件及拆解傳入參數
            initialBatch(args);

            // 開始工作內容
            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始");
            job.startTask();

            if (!this.checkConfig()) {
                job.stopBatch();
                return BatchReturnCode.ProgramException;
            }

            // 設定公用變數
            _batchInputPath = CMNConfig.getInstance().getBatchInputPath().trim();
//			_batchInputPath ="D:/TXTT/";

            // 檢查公用變數值
            if (StringUtils.isBlank(_batchInputPath)) {
                _logData.setRemark("參數BatchInputPath未設定");
                job.writeLog(_logData.getRemark());
                BatchJobLibrary.sendEMS(_logData);
                job.stopBatch();
                return BatchReturnCode.TableNotFound;
            }

            // 主流程
            // 3. @FTP 取得財金公司下載之銀聯卡BIN檔轉入 UPBIN
            // 如下載銀聯卡BIN檔為空檔, 則正常結束批次, 不往下執行
            if (doBusinessNew() == BatchReturnCode.Succeed) {
                job.writeLog(_programName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(_programName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
                return batchRC;
            }

            // 通知批次作業管理系統工作正常結束
            // job.WriteLog("BT010010 end!")
            // job.EndTask()
            return BatchReturnCode.Succeed;
        } catch (Exception e) {
            logContext.setProgramException(e);
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            if (job != null) {
                // Send to System Event
                job.writeLog("Batch Log目錄未設定，請修正");
                System.out.println("Batch Log目錄未設定，請修正");
            } else {
                job.writeErrorLog(e, e.getMessage());
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog("------------------------------------------------------------------");
                // 通知批次作業管理系統工作失敗,暫停後面流程
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
        job.writeLog("ImportUPBIN start!");
    }

    /**
     * 主流程
     *
     * @return
     */
    private BatchReturnCode doBusinessNew() {
        try {
            PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
            TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {

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
//				SckBatch sckBatch = new SckBatch();
//				String remoteFilePath="D:\\TCBFTP\\AP1T\\FEP\\UPB\\"+_batchInputFile;
//				sckBatch.SimpleFileReceiver("10.0.5.250", 46464, _batchInputPath+_batchInputFile, remoteFilePath);
//				sckBatch.receiveSocketFile(BIG_5);

                //預掃檔案內格式
                job.writeLog("檢查格式......");
                if (this.validCheckProcess() != BatchReturnCode.Succeed) {
                    transactionManager.rollback(txStatus);
                    return batchRC;
                } else {
                    job.writeLog("格式無誤。");
                }

                job.writeLog("清檔...");

                // 轉入之前需清檔: DELETE<UPBIN>
                upbinExtMapper.deleteAll();
                if (this.mainProcess() != BatchReturnCode.Succeed) {
                    transactionManager.rollback(txStatus);
                    return batchRC;
                }
                //5. @修改FTP Server文字檔副檔名由’.TXT’改為’.BAK’
                File file = new File(filelocalPath, fileName);
                File newFile = new File(filelocalPath + fileName.substring(0, fileName.length() - 3) + "BAK");

                file.renameTo(newFile);
                transactionManager.commit(txStatus);

                //刪除reserveDays天前的資料
                try {
                    if (!DeleteLocalFile()) {
                        job.writeLog("刪除" + filelocalPath + "下舊檔案失敗!!");
                        return BatchReturnCode.ProgramException;
                    }
                } catch (Exception e) {
                    job.writeLog("刪除" + filelocalPath + "發生例外!!" + e.getMessage());
                }

            } catch (Exception ex) {
                transactionManager.rollback(txStatus);
                job.writeLog(ex);
                System.out.println(ex);
                batchRC = BatchReturnCode.ProgramException;
                return batchRC;
            }
        } catch (Exception e) {
            job.writeLog(e);
            System.out.println(e);
            batchRC = BatchReturnCode.ProgramException;
            return batchRC;
        }
        batchRC = BatchReturnCode.Succeed;
        return batchRC;
    }

    private BatchReturnCode validCheckProcess() throws Exception {
        FileInputStream fisCheck = null;
        BufferedReader brCheck = null;
        int txtRecordTotalCount = 0;
        String singleline = "";
        int txtReadCount = 0;
        Boolean valid = true;

        Set<String> tempList = new HashSet<>();

        try {
            File baseDirectory = new File(CleanPathUtil.cleanString(filelocalPath));
            fisCheck = new FileInputStream(new File(baseDirectory, CleanPathUtil.cleanString(fileName)));
            if (fisCheck == null) {
                batchRC = BatchReturnCode.FileNotFound;
                return batchRC;
            }
            brCheck = new BufferedReader(new InputStreamReader(fisCheck, BIG_5));
            while (brCheck.ready()) {
                singleline = brCheck.readLine();
                txtReadCount++;
                // IFOBIN.TXT第一筆及最後一筆資料不需轉入UPBIN
                boolean isLastLine = (StringUtils.trim(singleline).length() == 14);
                if (txtReadCount == 1 || isLastLine) {
                    if (isLastLine) {
                        txtRecordTotalCount = NumberUtils
                                .toInt(StringUtils.trim(StringUtils.replace(singleline, "TTTT", "")));
                        if (txtRecordTotalCount != (txtReadCount - 2)) {
                            job.writeLog("資料筆數有誤，資料顯示總筆數為:" + txtRecordTotalCount + ", 實際上為:" + txtReadCount);
                            valid = false;
                        }
                    }
                    continue;
                }

                Short UPBIN_BIN_LENGTH = NumberUtils.toShort(StringUtils.substring(singleline, 111, 113)); // BIN 長度
                String UPBIN_BIN = StringUtils.trim(StringUtils.substring(singleline, 113, 125)); // 銀聯卡 BIN

                if (!tempList.add(UPBIN_BIN_LENGTH + UPBIN_BIN)) {
                    job.writeLog("資料有重複!! " + singleline);
                    valid = false;
                }

                // 檢核BIN 長度與銀聯卡BIN (去掉空白) 資料實際長度是否相符?如不相符則將錯誤原因及當筆資料顯示於 LOG
                if (UPBIN_BIN_LENGTH != (short) UPBIN_BIN.length()) {
                    job.writeLog("第 " + txtReadCount + " 行資料有誤，BIN長度(" + UPBIN_BIN_LENGTH + ") 與 銀聯卡BIN(去掉空白)資料實際長度("
                            + (short) UPBIN_BIN.length() + ") 不相符: " + singleline);
                    valid = false;
                }
                // 檢核銀聯卡BIN是否為數字? 如BIN內容含有文字,則將錯誤原因及當筆資料顯示於 LOG
                if (!NumberUtils.isParsable(UPBIN_BIN)) {
                    job.writeLog("第 " + txtReadCount + " 行資料有誤，銀聯卡BIN(" + UPBIN_BIN + ")不為數字: " + singleline);
                    valid = false;
                }
            }
            if (!valid) {
                batchRC = BatchReturnCode.DBIOError;
                return batchRC;
            }
        } catch (Exception e) {
            job.writeLog(e.getMessage());
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (brCheck != null) {
                try {
                    brCheck.close();
                } catch (IOException ex) {
                    job.writeLog(ex.getMessage());
                }
            }
            if (fisCheck != null) {
                try {
                    fisCheck.close();
                } catch (IOException ex) {
                    job.writeLog(ex.getMessage());
                }
            }
        }
        batchRC = BatchReturnCode.Succeed;
        return batchRC;
    }

    /**
     * 批次主要處理流程
     *
     * @return
     * @throws Exception
     */
    private BatchReturnCode mainProcess() throws Exception {
        FileInputStream fis = null;
        BufferedReader br = null;
        String singleline = "";
        int txtRecordTotalCount = 0;
        int txtReadCount = 0;
        int insertCount = 0;
        try {
            File baseDirectory = new File(CleanPathUtil.cleanString(filelocalPath));
            fis = new FileInputStream(new File(baseDirectory, CleanPathUtil.cleanString(fileName)));
//			if(fis == null){
//				batchRC = BatchReturnCode.FileNotFound;
//				return batchRC;
//			}
            br = new BufferedReader(new InputStreamReader(fis, BIG_5));
            while (br.ready()) {
                txtReadCount++;
                singleline = br.readLine();
                // IFOBIN.TXT第一筆及最後一筆資料不需轉入UPBIN
                boolean isLastLine = (StringUtils.trim(singleline).length() == 14);
                if (txtReadCount == 1 || isLastLine) {
                    if (isLastLine) {
                        txtRecordTotalCount = NumberUtils
                                .toInt(StringUtils.trim(StringUtils.replace(singleline, "TTTT", "")));
                    }
                    continue;
                }

                Upbin upbin = new Upbin();
                Short UPBIN_BIN_LENGTH = NumberUtils.toShort(StringUtils.substring(singleline, 111, 113)); // BIN 長度
                String UPBIN_BIN = StringUtils.trim(StringUtils.substring(singleline, 113, 125)); // 銀聯卡 BIN
//				// 檢核BIN 長度與銀聯卡BIN (去掉空白) 資料實際長度是否相符?如不相符則將錯誤原因及當筆資料顯示於 LOG
//				if (UPBIN_BIN_LENGTH != (short) UPBIN_BIN.length()) {
//					job.writeLog("第 " + txtReadCount + " 筆資料有誤，BIN長度(" + UPBIN_BIN_LENGTH + ") 與 銀聯卡BIN(去掉空白)資料實際長度("
//							+ (short) UPBIN_BIN.length() + ") 不相符: " + singleline);
//					batchRC = BatchReturnCode.DBIOError;
//					return batchRC;
//				}
//				// 檢核銀聯卡BIN是否為數字? 如BIN內容含有文字,則將錯誤原因及當筆資料顯示於 LOG
//				if (!NumberUtils.isParsable(UPBIN_BIN)) {
//					job.writeLog("第 " + txtReadCount + " 筆資料有誤，銀聯卡BIN(" + UPBIN_BIN + ")不為數字: " + singleline);
//					batchRC = BatchReturnCode.DBIOError;
//					return batchRC;
//				}
                upbin.setUpbinBinLength(UPBIN_BIN_LENGTH);
                upbin.setUpbinBin(UPBIN_BIN);
                String UPBIN_ISSUER = StringUtils.trim(StringUtils.substring(singleline, 0, 11)); // 銀行代號
                upbin.setUpbinIssuer(UPBIN_ISSUER);
                String UPBIN_ISSUER_NAME = StringUtils.substring(singleline, 11, 71); // 銀行名稱
                upbin.setUpbinIssuerName(UPBIN_ISSUER_NAME);
                String UPBIN_CARD_NAME = StringUtils.substring(singleline, 71, 111); // 銀聯卡名稱
                upbin.setUpbinCardName(UPBIN_CARD_NAME);
                Short UPBIN_PAN_LENGTH = NumberUtils.toShort(StringUtils.substring(singleline, 125, 127)); // PAN 長度
                upbin.setUpbinPanLength(UPBIN_PAN_LENGTH);
                String UPBIN_CARD_TYPE = StringUtils.substring(singleline, 127, 128); // 銀聯卡類別
                upbin.setUpbinCardType(UPBIN_CARD_TYPE);
                upbin.setUpdateUserid(0);
                upbin.setUpdateTime(new Date());
                if (upbinExtMapper.selectByPrimaryKey(upbin.getUpbinBinLength(), upbin.getUpbinBin()) == null) {
                    upbinExtMapper.insert(upbin);
                } else {
                    upbinExtMapper.updateByPrimaryKey(upbin);
                }
                insertCount++;
            }
            // 4. 核對筆數
            job.writeLog("新增UPBIN之總筆數: " + insertCount + "筆");
            job.writeLog(fileName + "最後一筆資料顯示總筆數: " + txtRecordTotalCount + "筆");
            if (insertCount != txtRecordTotalCount) {
                job.writeLog("新增UPBIN之總筆數(" + insertCount + ")與" + fileName + "最後一筆資料之總筆數(" + txtRecordTotalCount
                        + ")不相符");
                batchRC = BatchReturnCode.DBIOError;
                return batchRC;
            }
        } catch (Exception e) {
            if (txtReadCount != 0) {
                job.writeLog("成功執行 " + (txtReadCount - 1) + "筆，第 " + txtReadCount + "筆資料有誤: " + singleline);
            }
            job.writeLog(e.getMessage());
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    job.writeLog(ex.getMessage());
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    job.writeLog(ex.getMessage());
                }
            }
        }
        batchRC = BatchReturnCode.Succeed;
        return batchRC;
    }

    private boolean checkConfig() throws Exception {
        //1-2. 檢查MFT檔案目錄參數
        remotePath = INBKConfig.getInstance().getImportUPBINMFTPath().trim();
        if (StringUtils.isBlank(remotePath)) {
            job.writeLog("MFT 檔案目錄未設定，請修正");
            return false;
        }
        //1-3. 檢查UPBIN Local檔案放置目錄參數
        filelocalPath = INBKConfig.getInstance().getUPBINLocalPath().trim();
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
        if (job.getArguments().containsKey("ReserveDays")) {
            reserveDays = Integer.parseInt(job.getArguments().get("ReserveDays"));
        }

//		_batchInputPath ="D:/TXTT/";
        return true;
    }


    //MFT 下命令執行shellScript取檔
    private boolean receiveMftFile() {
        // Shell script
//        String shellScript = "/mft/recvfile.sh";        //開發套是recvfile.sh
		String shellScript = "/mft/transferfile.sh";  //測試套是transferfile.sh
//		String remotePath = "D:\\TCBFTP\\AP1T\\FEP\\UPB\\";
//		String remotePath = INBKConfig.getInstance().getImportUPBINMFTPath();
        String remoteFilePath = remotePath + fileName;
        String filePath = filelocalPath + fileName;
        String activation = "get";

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
        }
        return true;
    }

    private boolean DeleteLocalFile() {
        File directory = new File(filelocalPath);
        if (!directory.exists() || !directory.isDirectory()) {
            job.writeLog("目錄不存在或不是一個有效的目錄: " + filelocalPath);
            return true;
        }
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().startsWith("upcbin") && name.endsWith(".BAK"));

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
