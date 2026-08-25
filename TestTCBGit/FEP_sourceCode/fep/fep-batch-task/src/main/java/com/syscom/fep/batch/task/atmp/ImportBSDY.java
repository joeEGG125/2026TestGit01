package com.syscom.fep.batch.task.atmp;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class ImportBSDY extends FEPBase implements Task {
    private boolean batchResult = false;
    private String _batchLogPath = StringUtils.EMPTY;
    private BatchJobLibrary job = null;
    private BsdaysExtMapper bsdaysMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
    private String remotePath = StringUtils.EMPTY;
    private String filelocalPath = StringUtils.EMPTY;
    private String importFileName = StringUtils.EMPTY;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private int fileYeartoADYear = 0;
    private int fileYear = 0; //批次即將執行的年份
    private String holidayData = ""; //主機下傳之NYNY參數列
    private String fileLine1 = "";
    private String fileLine2 = "";

    @Override
    public BatchReturnCode execute(String[] args) {
        try {
            //初始化相關批次物件及拆解傳入參數
            this.initialBatch(args);
            job.writeLog("---------------------------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始!");
            job.startTask();

            if (!this.checkConfig(args)) {
                job.abortTask();
                return BatchReturnCode.MissingArgument;
            }

            //批次主要處理流程
            batchResult = this.mainProcess();
            //通知批次作業管理系統工作正常結束
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
        //檢查Batch Log目錄參數
        _batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(_batchLogPath)) {
            LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
            return;
        }
        //初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, _batchLogPath);
        if (ArrayUtils.isNotEmpty(args)) {
            for (String arg : args) {
                job.writeLog("接收的參數:", arg);
            }
        }
        //初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, _batchLogPath);
    }

    /**
     * 檢查是否為營業日
     *
     * @param args
     */
    private boolean checkConfig(String[] args) {
        if (args == null || args.length == 0 || "?".equals(args[0])) {
            return false;
        }

        if (!job.getArguments().containsKey("ImportFileName")) {
//            job.writeLog("請設定ImportFileName!!");
//            return false;
        } else {
            importFileName = job.getArguments().get("ImportFileName");
        }

        //檢查MFT檔案目錄參數
        remotePath = INBKConfig.getInstance().getImportBSDYMFTPath().trim();
        if (StringUtils.isBlank(remotePath)) {
            job.writeLog("MFT 檔案目錄未設定，請修正");
            return false;
        }
        //檢查BSDY Local檔案放置目錄參數
        filelocalPath = INBKConfig.getInstance().getBSDYLocalPath().trim();
        if (StringUtils.isBlank(filelocalPath)) {
            job.writeLog("Local File 放置目錄未設定，請修正");
            return false;
        }
        return true;
    }


    /**
     * 批次主要處理流程
     */
    @Transactional
    public boolean mainProcess() throws IOException {
        boolean rtn = true; //是否正常跑完
        boolean autoname = false; //判斷是否有手動輸入檔名
        BufferedReader br = null;
        //本年度  系統西元年度轉民國年
        int sYear = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))) - 1911;
        //下年度  SYear + 1
        int nYear = sYear + 1;
        //若ImportFileName帶入的值為空 檔名預設抓BSDY.TXT 年度預設會是下年度
        if (StringUtils.isBlank(importFileName)) {
            importFileName = "BSDY.txt";
            fileYear = nYear; //不帶檔名，默認為下一年度
            autoname = true;
        } else {
            try {
                fileYear = Integer.parseInt(importFileName.substring(importFileName.length() - 7, importFileName.length() - 4));
            } catch (NumberFormatException e) {
                job.writeLog("手動啟動批次參數：檔名規則為BSDY_民國年(三碼).txt，ex：BSDY_115.txt");
                return false;
            }
        }

        try {
            // MFT取檔至FEP
            if (!receiveMftFile()) {
                job.writeLog("get MFT File Fail!!");
                return false;
            } else {
                job.writeLog("get MFT File Success!!");
            }

            FileReader fr = new FileReader(filelocalPath + importFileName);
            br = new BufferedReader(fr);
            fileYeartoADYear = fileYear + 1911;
            String readline;
            int readlineCount = 0;
            boolean datepass = false;

            //逐筆檢核資料年度
            while ((readline = br.readLine()) != null) {
                readlineCount++;

                //營業日檔應只有2行資料，若超過直接踢掉
                if (readlineCount > 2) {
                    job.writeLog("檔案內容多於2行，與規格不符，停止批次執行。" + readline);
                    return false;
                }
                if (StringUtils.isNotBlank(readline)) {
                    if (fileYear == Integer.parseInt(readline.substring(2, 5))) {
                        //組合holiday字串
                        holidayData += readline.substring(6).trim();
                        if ("1".equals(readline.substring(5, 6))) {
                            job.writeLog("行數" + readlineCount + ", " + fileYear + "上半年度資料:" + readline.trim());
                            fileLine1 = readline;
                        } else if ("2".equals(readline.substring(5, 6))) {
                            job.writeLog("行數" + readlineCount + ", " + fileYear + "下半年度資料:" + readline.trim());
                            fileLine2 = readline;
                        } else {
                            datepass = false;
                            job.writeLog("資料格式有誤。" + readline);
                            return false;
                        }
                        datepass = true;
                    } else {
                        job.writeLog("比對年度不符，檔案內應為" + fileYear + "年度，實際為" + readline);
                        return false;
                    }
                }
            }

            if (holidayData.length() != 365 && holidayData.length() != 366) {
                job.writeLog("營業日檔案資料總長度有誤，上下年度資料總共應有 365筆 or 366筆。");
                datepass = false;
                return false;
            }

            if (datepass) {
                job.writeLog(importFileName + "檔案檢核成功!");

                boolean findNextBsday;
                Boolean findFirstBsdayOfThisYear = false;

                //開始讀holiday檔案 寫入BSDAYS
                for (int i = 1; i <= holidayData.length(); i++) {
                    findNextBsday = false;  //是否找到下營業日
                    LocalDate date = LocalDate.ofYearDay(fileYeartoADYear, i); //當年度的第幾天
                    char day = holidayData.charAt(i - 1);  //Y營業日  N非營業日

                    Bsdays bsday = bsdaysMapper.selectByPrimaryKey("TWN", date.format(formatter));

                    //當找到當年度第1個營業日後，往回判斷前年度的年尾"工作日"的下營業日是否正確
                    if (!findFirstBsdayOfThisYear && 'Y' == day) {
                        findFirstBsdayOfThisYear = true;

                        if(!checkLastDaysOfPreviousYear(date)){
                            return false;
                        }else{
                            job.writeLog("開始處理" + fileYear + "年度營業日...");
                        }
                    }

                    if (bsday != null) { //更新
                        //營業日
                        if ('Y' == day) {
                            bsday.setBsdaysWorkday((short) 1); //是營業日
                        } else {
                            bsday.setBsdaysWorkday((short) 0); //非營業日
                        }

                        //下一個營業日
                        for (int x = i; x < holidayData.length(); x++) {
                            char nextBSDay = holidayData.charAt(x);
                            if ('Y' == nextBSDay) {
                                findNextBsday = true;
                                LocalDate nextBSDaydate = LocalDate.ofYearDay(fileYeartoADYear, x + 1);
                                bsday.setBsdaysNbsdy(nextBSDaydate.format(formatter));
                                break;
                            }
                        }
                        //1228 ~ 1231的下營業日有可能是下一年度
                        if (!findNextBsday && Integer.parseInt(bsday.getBsdaysDate().substring(4, 8)) >= 1228) {
                            int dayOfNewYear = 2;
                            while (!findNextBsday) {
                                LocalDate newYeardate = LocalDate.ofYearDay(fileYeartoADYear + 1, dayOfNewYear);
                                int newYeardateOfweek = newYeardate.getDayOfWeek().getValue();
                                //星期一 ~ 星期五
                                if (newYeardateOfweek >= 1 && newYeardateOfweek <= 5) {
                                    bsday.setBsdaysNbsdy(newYeardate.format(formatter));
                                    findNextBsday = true;
                                    break;
                                }
                                dayOfNewYear++;
                            }
                        }
                        bsday.setBsdaysJday(LocalDate.parse(bsday.getBsdaysDate(), formatter).getDayOfYear());
                        bsday.setBsdaysStDateAtm(bsday.getBsdaysNbsdy());
                        bsday.setUpdateTime(new Date());
                        bsdaysMapper.updateByPrimaryKey(bsday);
                    } else { //新增
                        Bsdays newBsday = new Bsdays();
                        newBsday.setBsdaysZoneCode("TWN");
                        newBsday.setBsdaysDate(date.format(formatter));
                        //本年第幾日
                        newBsday.setBsdaysJday(i);
                        //營業日
                        if ('Y' == day) {
                            newBsday.setBsdaysWorkday((short) 1); //是營業日
                        } else {
                            newBsday.setBsdaysWorkday((short) 0); //非營業日
                        }

                        //星期幾 週一 -> 1
                        newBsday.setBsdaysWeekno((short) date.getDayOfWeek().getValue());

                        //下一個營業日
                        for (int x = i; x < holidayData.length(); x++) {
                            char nextBSDay = holidayData.charAt(x);
                            if ('Y' == nextBSDay) {
                                findNextBsday = true;
                                LocalDate nextBSDaydate = LocalDate.ofYearDay(fileYeartoADYear, x + 1);
                                newBsday.setBsdaysNbsdy(nextBSDaydate.format(formatter));
                                break;
                            }
                        }
                        //1228 ~ 1231的下營業日有可能是下一年度
                        if (!findNextBsday && Integer.parseInt(newBsday.getBsdaysDate().substring(4, 8)) >= 1228) {
                            int dayOfNewYear = 2; //從1/2開始找是否平日
                            while (!findNextBsday) {
                                LocalDate newYeardate = LocalDate.ofYearDay(fileYeartoADYear + 1, dayOfNewYear);
                                int newYeardateOfweek = newYeardate.getDayOfWeek().getValue();
                                //星期一 ~ 星期五
                                if (newYeardateOfweek >= 1 && newYeardateOfweek <= 5) {
                                    newBsday.setBsdaysNbsdy(newYeardate.format(formatter));
                                    findNextBsday = true;
                                    break;
                                }
                                dayOfNewYear++;
                            }
                        }
                        newBsday.setBsdaysStDateAtm(newBsday.getBsdaysNbsdy());
                        newBsday.setBsdaysStDateRm(newBsday.getBsdaysDate());
                        newBsday.setBsdaysStFlag((short) 0);
                        newBsday.setUpdateUserid(0);
                        newBsday.setUpdateTime(new Date());

                        bsdaysMapper.insert(newBsday);
                    }
                }
                job.writeLog(fileYear + "年度BSDY 處理 ok!");
                job.writeLog("==================================================================");

                //與原資料進行核對
                if (!checkBSDAY()) {
                    job.writeLog("檢核資料有誤!");
                    return false;
                } else {
                    job.writeLog("檢核資料完成!");
                }

                rtn = true;
            } else {
                job.writeLog("日曆文字檔內容有誤");
                rtn = false;
            }
        } catch (Exception ex) {
            job.writeLog(ex.getMessage());
            rtn = false;
        } finally {
            if (br != null) {
                safeClose(br);
            }
        }
        return rtn;
    }

    public boolean checkBSDAY() {
        try {
            //與原資料進行核對
            List<Bsdays> checkbsdaysList = bsdaysMapper.queryBSDAYSOfYearAndZone(Integer.toString(fileYeartoADYear), "TWN");

            if (checkbsdaysList.isEmpty()) {
                job.writeLog("BSDAYS找不到 " + fileYeartoADYear + " 年度的資料!!");
                return false;
            } else if (fileYear != Integer.parseInt(fileLine1.substring(2, 5))) {
                job.writeLog("BSDAYS年度與原檔案資料不符!!");
                return false;
            } else {
                //逐筆檢核資料
                int totcnt = 0;
                int successcnt = 0;
                int failcnt = 0;
                StringBuilder OutData = new StringBuilder();

                for (Bsdays checkSingleBSDay : checkbsdaysList) {
                    totcnt++;
                    if (Integer.parseInt(checkSingleBSDay.getBsdaysDate().substring(4, 8)) >= 1228) {
                        job.writeLog("年尾日期請人工比對,日期(BsdaysDate):" + checkSingleBSDay.getBsdaysDate()
                                + ", 下營業日(BsdaysNbsdy):" + checkSingleBSDay.getBsdaysNbsdy()
                                + ", 工作日(BsdaysWorkday):" + checkSingleBSDay.getBsdaysWorkday()
                                + ", 當年度第幾天(BsdaysJday):" + checkSingleBSDay.getBsdaysJday());
                        if (1 == checkSingleBSDay.getBsdaysWorkday()) {
                            OutData.append("Y");
                        } else {
                            OutData.append("N");
                        }
                        continue;
                    }

                    //檢核該日為該年度第幾天
                    LocalDate checkDate = LocalDate.parse(checkSingleBSDay.getBsdaysDate(), formatter);
                    int daycnt = checkDate.getDayOfYear();
                    if (daycnt != checkSingleBSDay.getBsdaysJday()) {
                        job.writeLog(checkSingleBSDay.getBsdaysDate() + "應為本年度第" + daycnt + "天!!");
                        OutData.append("-"); //此日註記異常符號
                        failcnt++;
                        continue;
                    }
                    //檢核下一營業日是否為工作日及與原資料比對
                    checkDate = LocalDate.parse(checkSingleBSDay.getBsdaysNbsdy(), formatter);
                    int nbsdaycnt = checkDate.getDayOfYear();
                    if ('Y' != holidayData.charAt(nbsdaycnt - 1)) {
                        job.writeLog(checkSingleBSDay.getBsdaysDate() + "的下營業日非工作日!! BsdaysNbsdy:" + checkSingleBSDay.getBsdaysNbsdy());
                        OutData.append("-"); //此日註記異常符號
                        failcnt++;
                        continue;
                    } else if (('Y' == holidayData.charAt(daycnt - 1) && 1 != checkSingleBSDay.getBsdaysWorkday()) //該日根據資料位置是Y是營業日，但工作日註記非1
                            || ('N' == holidayData.charAt(daycnt - 1) && 0 != checkSingleBSDay.getBsdaysWorkday())) { //該日根據資料位置是N是營業日，但工作日註記非0
                        job.writeLog(checkSingleBSDay.getBsdaysDate() + "工作日註記與原資料不符!! BsdaysWorkday:" + checkSingleBSDay.getBsdaysWorkday());
                        failcnt++;
                        continue;
                    }

                    if (1 == checkSingleBSDay.getBsdaysWorkday()) {
                        OutData.append("Y");
                        successcnt++;
                    } else {
                        OutData.append("N");
                        successcnt++;
                    }
                }

                job.writeLog("檢核前資料:" + holidayData);
                job.writeLog("檢核後資料:" + OutData);
                job.writeLog("檢核總天數:" + totcnt + "檢核成功:" + successcnt + "檢核失敗:" + failcnt);

                if (failcnt != 0) {
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            job.writeLog("檢核資料發生異常: " + e.getMessage());
            return false;
        }
    }

    public boolean checkLastDaysOfPreviousYear(LocalDate firstWorkDateOfNewYear){
        try {
            LocalDate lastDaysOfLastYear = firstWorkDateOfNewYear.minusYears(1).with(TemporalAdjusters.lastDayOfYear());//找到前年度的最後一天12/31
            int minusDay = 0;
            job.writeLog("==================================================================");
            job.writeLog("開始檢核" + (lastDaysOfLastYear.getYear() - 1911) + "年度最後營業日的下營業日是否正確...");
            while (true) {
                Bsdays lastYearBsday = bsdaysMapper.selectByPrimaryKey("TWN", lastDaysOfLastYear.minusDays(minusDay).format(formatter));
                //若非營業日，下營業日應為新年度第一個工作日
                if (lastYearBsday != null) {
                    if (!lastYearBsday.getBsdaysNbsdy().equals(firstWorkDateOfNewYear.format(formatter))) {
                        job.writeLog(lastYearBsday.getBsdaysDate() + "的下營業日有誤, 原為"
                                + lastYearBsday.getBsdaysNbsdy() + ", 已更新為" + firstWorkDateOfNewYear.format(formatter));
                        lastYearBsday.setBsdaysNbsdy(firstWorkDateOfNewYear.format(formatter));
                        lastYearBsday.setBsdaysStDateAtm(lastYearBsday.getBsdaysNbsdy());
                        lastYearBsday.setUpdateTime(new Date());
                        bsdaysMapper.updateByPrimaryKey(lastYearBsday);
                    } else {
                        job.writeLog(lastYearBsday.getBsdaysDate() + "的下營業日為" + lastYearBsday.getBsdaysNbsdy() + ". 檢核正確!");
                    }
                    if (lastYearBsday.getBsdaysWorkday() == 1) {
                        job.writeLog("==================================================================");
                        return true;
                    }
                    minusDay++;
                } else {
                    job.writeLog("查無去年度" + lastDaysOfLastYear.minusDays(minusDay).format(formatter) + "的營業日資料!");
                    return false;
                }
            }
        }catch (Exception e) {
            job.writeLog("檢核去年年尾營業日之下營業日發生異常: " + e.getMessage());
            return false;
        }
    }


    public void safeClose(BufferedReader br) {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                job.writeLog(e.getMessage());
            }
        }
    }

    private boolean receiveMftFile() {
        // Shell script
//        String shellScript = "/mft/recvfile.sh";        //開發套是recvfile.sh
        String shellScript = "/mft/transferfile.sh";  //測試套是transferfile.sh
//        String remotePath = "D:\\TCBFTP\\AP1T\\FEP\\";
        String remoteFilePath = remotePath + importFileName;
        String filePath = filelocalPath + importFileName;
        String activation = "get";

        String[] command = {"sudo", "-u", "mftfepap", shellScript, filePath, remoteFilePath, activation};
        try {
            boolean mftGetFile = true;
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                job.writeLog(line);
                if (line.contains("Error") || line.contains("Fail")) {
                    mftGetFile = false;
                }
            }

            // 等待執行結果
            int exitCode = process.waitFor();
            job.writeLog("Exited with code: " + exitCode);

            if (exitCode != 0 || !mftGetFile) {
                job.writeLog("MFT 取檔失敗!!");
                return false;
            }
        } catch (Exception e) {
            job.writeLog(e.getMessage());
            return false;
        }
        return true;
    }
}
