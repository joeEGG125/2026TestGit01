package com.syscom.fep.batch.task.atmp;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.deslog.ext.mapper.DeslogExtMapper;

/**
 * 清除次系統日的DESLOG
 */
public class CleanDESLog extends FEPBase implements Task {
    private boolean batchResult = false;
    private String _batchLogPath = StringUtils.EMPTY;
    private BatchJobLibrary job = null;
    private DeslogExtMapper deslogextmapper = SpringBeanFactoryUtil.getBean(DeslogExtMapper.class);

    @Override
    public BatchReturnCode execute(String[] args) {
        try {
            // 初始化相關批次物件及拆解傳入參數
            this.initialBatch(args);
            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始!");
            // 回報批次平台開始工作
            job.startTask();

            // 顯示help說明
            if (job.getArguments().containsKey("?")) {
                DisplayUsage();
                return BatchReturnCode.Succeed;
            }

            // 批次主要處理流程
            batchResult = this.mainProcess();

            // 通知批次作業管理系統工作正常結束
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
            // 通知批次作業管理系統工作失敗,暫停後面流程
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
        // 檢查Batch Log目錄參數
        _batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();

        if (StringUtils.isBlank(_batchLogPath)) {
            LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
            return;
        }

        // 初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, _batchLogPath);
    }

    /**
     * 批次主要處理流程
     *
     * @return
     */
    private boolean mainProcess() {
        boolean rtn = true; // 是否正常跑完
        try {
            String businessDate = job.getArguments().get("TBSDY");
            SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat formatDayInWeek = new SimpleDateFormat("u"); // 僅取得星期，用於DESLOG patation

            // 若沒輸入參數則使用系統日期
            if (StringUtils.isBlank(businessDate) && StringUtils.length(businessDate) != 8) {
                businessDate = formatDay.format(new Date());
            }
            job.writeLog(String.format("清除日期:%s的次系統日的DESLOG資料", businessDate));

            // 準備所需參數
            // 起啟Date
            Date startDate = formatDay.parse(businessDate);
//			Calendar startCalendar = Calendar.getInstance();
//			startCalendar.setTime(startDate);
//			startCalendar.add(Calendar.DATE, 1);
//			startCalendar.set(Calendar.HOUR, 0);
//			startCalendar.set(Calendar.MINUTE, 0);
//			startCalendar.set(Calendar.SECOND, 0);
//			startDate = startCalendar.getTime();
            // 星期幾，用於區分DESLOG partition
            String dayInWeek = formatDayInWeek.format(startDate);
            if (dayInWeek.equals("7")){
                dayInWeek="1";
            }
            else{
                dayInWeek= String.valueOf(Integer.parseInt(dayInWeek)+1);
            }
            // 結束Date
//			Calendar endCalendar = Calendar.getInstance();
//			endCalendar.setTime(startDate);
//			endCalendar.set(Calendar.HOUR, 23);
//			endCalendar.set(Calendar.MINUTE, 59);
//			endCalendar.set(Calendar.SECOND, 59);
//			Date endDate = endCalendar.getTime();

            int count = this.deslogextmapper.TruncateByLogDate(dayInWeek);
//			int count = this.feplogextmapper.deleteByLogDate(dayInWeek, startDate, endDate);
//            job.writeLog("delete DESLOG By LogDate count : " + count);
            job.writeLog("truncate>>DESLOG" + dayInWeek + "<<成功. return code:" + count);
            job.writeLog("CleanDESLOG 執行正常!");
        } catch (Exception ex) {
            job.writeLog("CleanDESLOG執行失敗");
            job.writeLog(ex.getMessage());
            sendEMS(logContext); // 不得直接呼叫FEPBase.SendEMS
            rtn = false;
        }

        return rtn;
    }

    /**
     * 顯示help說明
     */
    private void DisplayUsage() {
        System.out.println("USAGE:");
        System.out.println("  CleanDESLOG Options");
        System.out.println();
        System.out.println("  Options:");
        System.out.println("      /?             Display this help message.");
        System.out.println("      /TBSDY         Optional, specifies execution day(yyyymmdd).");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  execute system day        CleanDESLOG");
        System.out.println("  execute specifies day     CleanDESLOG /TBSDY:20100101");
    }
}
