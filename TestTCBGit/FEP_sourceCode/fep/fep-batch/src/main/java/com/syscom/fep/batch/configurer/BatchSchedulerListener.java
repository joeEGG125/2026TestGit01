package com.syscom.fep.batch.configurer;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.job.BatchJobContext;
import com.syscom.fep.batch.util.QuartzUtil;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.slf4j.event.Level;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public class BatchSchedulerListener extends FEPBase implements SchedulerListener {
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    private final SchedulerFactoryBean batchSchedulerFactoryBean;

    public BatchSchedulerListener(SchedulerFactoryBean batchSchedulerFactoryBean) {
        this.batchSchedulerFactoryBean = batchSchedulerFactoryBean;
    }

    @Override
    public void jobScheduled(Trigger trigger) {
        log("jobScheduled", getBatchJobContext(trigger.getJobKey().getName()), "trigger [", trigger, "] has scheduled");
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        log(Level.WARN, "jobUnscheduled", getBatchJobContext(triggerKey.getGroup()), "trigger [", getTriggerInfo(triggerKey), "] has unscheduled");
    }

    @Override
    public void triggerFinalized(Trigger trigger) {
        log(Level.WARN, "triggerFinalized", getBatchJobContext(trigger.getJobKey().getName()), "trigger [", trigger, "] has finalized");
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {
        log(Level.WARN, "triggerPaused", getBatchJobContext(triggerKey.getGroup()), "trigger [", getTriggerInfo(triggerKey), "] has paused");
    }

    @Override
    public void triggersPaused(String triggerGroup) {
        log(Level.WARN, "triggersPaused", getBatchJobContext(triggerGroup), "triggers [", triggerGroup, "] has paused");
    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {
        log("triggerResumed", getBatchJobContext(triggerKey.getGroup()), "trigger [", getTriggerInfo(triggerKey), "] has resumed");
    }

    @Override
    public void triggersResumed(String triggerGroup) {
        log("triggersResumed", getBatchJobContext(triggerGroup), "triggers [", triggerGroup, "] has resumed");
    }

    @Override
    public void jobAdded(JobDetail jobDetail) {
        BatchJobContext context = BatchJobContext.getJobDataMap(jobDetail.getJobDataMap());
        log("jobAdded", context, "job [", jobDetail.getKey(), "] has added");
    }

    @Override
    public void jobDeleted(JobKey jobKey) {
        log(Level.WARN, "jobDeleted", getBatchJobContext(jobKey.getName()), "job [", jobKey, "] has deleted");
    }

    @Override
    public void jobPaused(JobKey jobKey) {
        log(Level.WARN, "jobPaused", getBatchJobContext(jobKey.getName()), "job [", jobKey, "] has paused");
    }

    @Override
    public void jobsPaused(String jobGroup) {
        log(Level.WARN, "jobsPaused", getBatchJobContext(jobGroup), "jobs [", jobGroup, "] has paused");
    }

    @Override
    public void jobResumed(JobKey jobKey) {
        log("jobResumed", getBatchJobContext(jobKey.getName()), "job [", jobKey, "] has resumed");
    }

    @Override
    public void jobsResumed(String jobGroup) {
        log("jobsResumed", getBatchJobContext(jobGroup), "jobs [", jobGroup, "] has resumed");
    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {
        sendEMS("schedulerError", null, cause, "scheduler has error, ", msg);
    }

    @Override
    public void schedulerInStandbyMode() {
        log("schedulerInStandbyMode", null, "scheduler has been in stand by mode");
    }

    @Override
    public void schedulerStarted() {
        log("schedulerStarted", null, "scheduler has started");
    }

    @Override
    public void schedulerStarting() {
        log("schedulerStarting", null, "scheduler is starting");
    }

    @Override
    public void schedulerShutdown() {
        logger.warn("==========scheduler was shutdown==========");
    }

    @Override
    public void schedulerShuttingdown() {
        logger.warn("==========scheduler is shutting down==========");
    }

    @Override
    public void schedulingDataCleared() {
        logger.warn("==========scheduling data cleared==========");
    }

    private BatchJobContext getBatchJobContext(String batchId) {
        return QuartzUtil.getJobData(batchSchedulerFactoryBean, batchId, batchId, BatchJobContext::getJobDataMap);
    }

    private String getTriggerInfo(TriggerKey triggerKey) {
        return QuartzUtil.getTriggerInfo(batchSchedulerFactoryBean, triggerKey);
    }

    private void log(String methodName, BatchJobContext context, Object... messages) {
        log(Level.INFO, methodName, context, messages);
    }

    private void log(Level level, String methodName, BatchJobContext context, Object... messages) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.CMN);
        logData.setChannel(FEPChannel.BATCH);
        logData.setProgramName(StringUtils.join(ProgramName, ".", methodName));
        logData.setRemark(StringUtils.join(StringUtils.join(messages), context != null ? StringUtils.join(", ", context.getScheduleInfo()) : StringUtils.EMPTY));
        logMessage(level, logData);
    }

    private void sendEMS(String methodName, BatchJobContext context, Throwable t, Object... messages) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.CMN);
        logData.setChannel(FEPChannel.BATCH);
        logData.setProgramName(StringUtils.join(ProgramName, ".", methodName));
        logData.setRemark(StringUtils.join(StringUtils.join(messages), context != null ? StringUtils.join(", ", context.getScheduleInfo()) : StringUtils.EMPTY));
        logData.setProgramException(t);
        sendEMS(logData);
    }
}
