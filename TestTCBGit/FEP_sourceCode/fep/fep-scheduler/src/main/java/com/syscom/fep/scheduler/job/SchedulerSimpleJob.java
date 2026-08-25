package com.syscom.fep.scheduler.job;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 非單例模式下的的任務
 *
 * @param <JobConfig>
 */
@DisallowConcurrentExecution
public abstract class SchedulerSimpleJob<JobConfig extends SchedulerJobConfig> extends FEPBase implements SchedulerJobConstant, Job {
    protected static final LogHelper ScheduleLogger = LogHelperFactory.getSchedulerLogger();
    private JobConfig config;

    /**
     * 執行任務
     *
     * @param context
     */
    protected abstract void executeJob(JobExecutionContext context, JobConfig config) throws Exception;

    public void setConfig(JobConfig config) {
        this.config = config;
    }

    protected void putMDC() {
        String mdcProfile = LogMDC.get(Const.MDC_PROFILE);
        LogMDC.put(Const.MDC_PROFILE, StringUtils.isBlank(mdcProfile) ? ProgramName : mdcProfile);
    }

    /**
     * 建立排程
     *
     * @param scheduler
     */
    protected void scheduleJob(Scheduler scheduler) {
        if (config == null) throw ExceptionUtil.createNullPointException("Please set config first!!!");
        JobDetail jobDetail = JobBuilder.newJob(this.getClass()).withIdentity(this.getJobKey(config)).build();
        jobDetail.getJobDataMap().put(JOB_DATA_MAP_KEY_IS_SIMPLE, true);
        jobDetail.getJobDataMap().put(JOB_DATA_MAP_KEY_JOB_CONFIG, config);
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(config.getCronExpression());
        CronTrigger cronTrigger = getCronTrigger(scheduleBuilder, config);
        ScheduleLogger.info(ProgramName, "-", config.getIdentity(), " start to schedule...");
        try {
            scheduler.scheduleJob(jobDetail, cronTrigger);
            ScheduleLogger.info(ProgramName, "-", config.getIdentity(), " has been scheduled by CronExpression = [", config.getCronExpression(), "]");
        } catch (SchedulerException e) {
            ScheduleLogger.exceptionMsg(e, ProgramName, "-", config.getIdentity(), " schedule failed with exception occur, ", e.getMessage());
        }
    }

    protected CronTrigger getCronTrigger(CronScheduleBuilder scheduleBuilder, JobConfig config) {
        return TriggerBuilder.newTrigger().withIdentity(this.getTriggerKey(config)).withSchedule(scheduleBuilder).build();
    }

    protected JobKey getJobKey(JobConfig config) {
        return JobKey.jobKey(config.getIdentity(), GROUP_NAME);
    }

    protected TriggerKey getTriggerKey(JobConfig config) {
        return TriggerKey.triggerKey(config.getIdentity(), GROUP_NAME);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime begin = LocalDateTime.now();
        putMDC();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        @SuppressWarnings("unchecked")
        JobConfig config = (JobConfig) jobDataMap.get(JOB_DATA_MAP_KEY_JOB_CONFIG);
        StackTraceElement element = Thread.currentThread().getStackTrace()[1];
        String methodName = StringUtils.join(ProgramName, ".", element.getMethodName(), "(", element.getLineNumber(), ")");
        ScheduleLogger.debug("[", methodName, "][", config.getIdentity(), "][timeOccupied][", DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(begin), "]begin at ", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(begin));
        try {
            this.executeJob(context, config);
        } catch (Exception e) {
            ScheduleLogger.exceptionMsg(e, ProgramName, "-", config.getIdentity(), " execute failed with exception occur, ", e.getMessage());
        } finally {
            LocalDateTime end = LocalDateTime.now();
            long duration = ChronoUnit.MILLIS.between(begin, end);
            ScheduleLogger.debug("[", methodName, "][", config.getIdentity(), "][timeOccupied][", DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(begin), "]finished at ", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(end), ", duration:", duration, " millisecond");
        }
    }
}
