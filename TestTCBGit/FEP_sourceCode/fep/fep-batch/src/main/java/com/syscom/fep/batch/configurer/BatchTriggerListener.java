package com.syscom.fep.batch.configurer;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.job.BatchJobContext;
import com.syscom.fep.batch.util.QuartzUtil;
import com.syscom.fep.frmcommon.log.LogMDC;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.slf4j.event.Level;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public class BatchTriggerListener extends FEPBase implements TriggerListener {
    private final SchedulerFactoryBean batchSchedulerFactoryBean;

    public BatchTriggerListener(SchedulerFactoryBean batchSchedulerFactoryBean) {
        this.batchSchedulerFactoryBean = batchSchedulerFactoryBean;
    }

    @Override
    public String getName() {
        return ProgramName;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        BatchJobContext batchJobContext = getBatchJobContext(context);
        if (batchJobContext != null) {
            // 這裡要將上一次的InstanceId清掉
            batchJobContext.setInstanceId(StringUtils.EMPTY);
            // 上面改變了param的值, 所以這裡一定要再put一次, 否則不會寫入db
            batchJobContext.putJobDataMap(context.getJobDetail().getJobDataMap());
        }
        log("triggerFired", batchJobContext, "trigger [", trigger, "] has fired");
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        log(Level.WARN, "triggerMisfired", getBatchJobContext(trigger.getJobKey().getName()), "trigger [", trigger, "] has misfired");
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {
        log("triggerComplete", getBatchJobContext(context), "trigger [", trigger, "] has completed");
    }

    private BatchJobContext getBatchJobContext(JobExecutionContext context) {
        return QuartzUtil.getJobData(context, BatchJobContext::getJobDataMap);
    }

    private BatchJobContext getBatchJobContext(String batchId) {
        return QuartzUtil.getJobData(batchSchedulerFactoryBean, batchId, batchId, BatchJobContext::getJobDataMap);
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
        if (!"triggerMisfired".equals(methodName))
            logData.setMessage(context != null ? context.getInstanceId() : StringUtils.EMPTY);
        logMessage(level, logData);
    }
}
