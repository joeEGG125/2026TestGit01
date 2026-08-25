package com.syscom.fep.batch.task.rm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.CMNConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class runSHbyBatch extends FEPBase implements Task {
    private BatchJobLibrary job = null;
    private String batchLogPath = StringUtils.EMPTY;
    private boolean batchResult = false;
    private String COMMANDFILES = StringUtils.EMPTY;
    private int RESTARTPCNT = 90;

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
                job.writeLog(ProgramName + "配置錯誤");
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

    private void initialBatch(String[] args) {
        // 初始化logContext物件,傳入工作執行參數
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        // 檢查Batch Log目錄參數
        batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        // 初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, batchLogPath);
    }

    private boolean checkConfig() {
        //抓檔名
        if (job.getArguments().containsKey("COMMANDFILES")) {
            COMMANDFILES = job.getArguments().get("COMMANDFILES");
            job.writeLog("COMMANDFILES : " + COMMANDFILES);
        }
        if (job.getArguments().containsKey("RESTARTPCNT")) {
                String RESTARTPCNTString = job.getArguments().get("RESTARTPCNT");
            try {
                RESTARTPCNT = Integer.parseInt(RESTARTPCNTString);
            }catch (NumberFormatException ne){
                job.writeLog("Exception occur : " + RESTARTPCNTString);
                return false;
            }
        }
        return true;
    }

    private boolean mainProcess() {
        try {
            String[] COMMANDFILE = COMMANDFILES.split(";"); //預設放/fep/fep-app/bin/下
            String[] command = {"vmstat", "-v"};
            double memPercent = 0;
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                        job.writeLog(line);
                    if(line.contains("percentage of memory used for computational pages")){
                        memPercent = Double.parseDouble(line.trim().split("\\s+")[0]);
                    }
                }
                boolean finished = process.waitFor(60, TimeUnit.SECONDS);
                if (finished) {
                    int exitCode = process.exitValue();
                    job.writeLog("Exited with code: " + exitCode);
                } else {
                    process.destroyForcibly();
                    job.writeLog("process timeout, force stop.");
                    return false;
                }
                job.writeLog("end vmstat");
            } catch (Exception e) {
                job.writeLog(e);
                return false;
            }

            if(memPercent >= RESTARTPCNT){
                job.writeLog("memory:" + memPercent + ">=" + RESTARTPCNT + ", Begin to restart StandAlones...");
                job.writeLog(", Begin to run:" + COMMANDFILE[0]);
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(COMMANDFILE[0]);
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();

//                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        if(line.contains("is starting on host") || line.contains("started successfully")){
//                            job.writeLog(line);
//                        }
//                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            job.writeLog("指令執行完畢!");

            return true;
        } catch (Exception e) {
            job.writeLog("測試失敗, Error:" + e.getMessage());
        }
        return true;
    }

}
