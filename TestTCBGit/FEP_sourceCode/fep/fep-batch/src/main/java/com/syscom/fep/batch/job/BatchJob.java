package com.syscom.fep.batch.job;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.ScheduleType;
import com.syscom.fep.batch.util.QuartzUtil;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.slf4j.event.Level;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public abstract class BatchJob extends FEPBase implements Job {
    /**
     * 執行任務
     *
     * @param logData
     * @param context
     * @param batchJobContext
     * @throws Exception
     */
    protected abstract void execute(LogData logData, JobExecutionContext context, BatchJobContext batchJobContext) throws Exception;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime begin = LocalDateTime.now();
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.CMN);
        logData.setChannel(FEPChannel.BATCH);
        logData.setProgramName(StringUtils.join(ProgramName, ".execute"));
        BatchJobContext batchJobContext = null;
        try {
            batchJobContext = QuartzUtil.getJobData(context, BatchJobContext::getJobDataMap);
            logData.setMessage(batchJobContext.getInstanceId());
            logData.setRemark(StringUtils.join("[timeOccupied][", DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(begin), "]begin at ", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(begin), ", ", batchJobContext.getScheduleInfo(), ", trigger:", context.getTrigger()));
            logMessage(Level.DEBUG, logData);
            // 如果是Monthly類型, 必須判斷當天不能重複執行
            // 例如某月第4周和某月最後一周, 可能會是同一天會重複執行, 則必須要判斷
            if (ScheduleType.isMonthly(batchJobContext.getScheduleType()) && batchJobContext.hasTriggeredInDays()) {
                logData.setSubSys(SubSystem.CMN);
                logData.setChannel(FEPChannel.BATCH);
                logData.setProgramName(StringUtils.join(ProgramName, ".execute"));
                logData.setMessage(batchJobContext.getInstanceId());
                logData.setRemark(StringUtils.join("Duplicate Execute Day!!! Batch Job has already executed at same day",
                        batchJobContext.getLatestExecutedStartDateTime() != null ? StringUtils.join(", LatestExecutedStartDateTime:", FormatUtil.dateTimeFormat(batchJobContext.getLatestExecutedStartDateTime())) : StringUtils.EMPTY,
                        batchJobContext.getNextFireDateTime() != null ? StringUtils.join(", NextFireDateTime:", FormatUtil.dateTimeFormat(batchJobContext.getNextFireDateTime())) : StringUtils.EMPTY,
                        ", ", batchJobContext.getScheduleInfo(), ", trigger:", context.getTrigger()));
                logMessage(Level.WARN, logData);
                return;
            }
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName(StringUtils.join(ProgramName, ".execute"));
            logData.setMessage(batchJobContext.getInstanceId());
            logData.setRemark(StringUtils.join("Batch Job start to execute, ", batchJobContext.getScheduleInfo(), ", trigger:", context.getTrigger()));
            logMessage(Level.INFO, logData);
            // 先塞入下一次執行時間
            BatchJobManager manager = SpringBeanFactoryUtil.getBean(BatchJobManager.class, false);
            if (manager != null) {
                Trigger nextFireTrigger = manager.getNextFireTrigger(batchJobContext.getBatchId());
                batchJobContext.setNextFireDateTime(nextFireTrigger != null ? nextFireTrigger.getNextFireTime() : null);
            }
            // 塞入最近一次開始執行的時間
            batchJobContext.setLatestExecutedStartDateTime(Calendar.getInstance().getTime());
            try {
                this.execute(logData, context, batchJobContext);
            } catch (Exception e) {
                logData.setMessage(batchJobContext.getInstanceId());
                logData.setProgramException(e);
                logData.setProgramName(StringUtils.join(ProgramName, ".execute"));
                logData.setRemark(StringUtils.join("Batch Job execute with exception occurred, ", batchJobContext.getScheduleInfo(), ", trigger:", context.getTrigger()));
                sendEMS(logData);
            } finally {
                // 塞入最近一次結束執行的時間
                batchJobContext.setLatestExecutedEndDateTime(Calendar.getInstance().getTime());
                // 上面改變了param的值, 所以這裡一定要再put一次, 否則不會寫入db
                QuartzUtil.putJobData(context, batchJobContext, (jobDataMap, jobContext) -> jobContext.putJobDataMap(jobDataMap));
                logData.setSubSys(SubSystem.CMN);
                logData.setChannel(FEPChannel.BATCH);
                logData.setProgramName(StringUtils.join(ProgramName, ".execute"));
                logData.setRemark(StringUtils.join("Batch Job execute finished",
                        batchJobContext.getLatestExecutedStartDateTime() != null ? StringUtils.join(", LatestExecutedStartDateTime:", FormatUtil.dateTimeFormat(batchJobContext.getLatestExecutedStartDateTime())) : StringUtils.EMPTY,
                        batchJobContext.getNextFireDateTime() != null ? StringUtils.join(", NextFireDateTime:", FormatUtil.dateTimeFormat(batchJobContext.getNextFireDateTime())) : StringUtils.EMPTY,
                        ", ", batchJobContext.getScheduleInfo(), ", trigger:", context.getTrigger()));
                logData.setMessage(batchJobContext.getInstanceId());
                logMessage(Level.INFO, logData);
            }
        } finally {
            LocalDateTime end = LocalDateTime.now();
            long duration = ChronoUnit.MILLIS.between(begin, end);
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName(StringUtils.join(ProgramName, ".execute"));
            logData.setRemark(StringUtils.join("[timeOccupied][", DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(begin), "]finished at ", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(end), ", duration:", duration, " millisecond", batchJobContext != null ? StringUtils.join(", ", batchJobContext.getScheduleInfo()) : StringUtils.EMPTY, ", trigger:", context.getTrigger()));
            if (batchJobContext != null)
                logData.setMessage(batchJobContext.getInstanceId());
            logMessage(Level.DEBUG, logData);
        }
    }
}
