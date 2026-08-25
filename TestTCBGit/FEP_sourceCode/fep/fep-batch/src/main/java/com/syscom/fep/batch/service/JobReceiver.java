package com.syscom.fep.batch.service;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.ScheduleType;
import com.syscom.fep.batch.base.enums.WeeksOfMonth;
import com.syscom.fep.batch.base.vo.FEPBatch;
import com.syscom.fep.batch.job.BatchJobManager;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.instance.batch.BatchQueueConsumers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.jms.Message;

@Component
public class JobReceiver extends FEPBase implements JmsReceiver<PlainTextMessage> {
    @Autowired
    private JobHelper jobHelper;
    @Autowired
    private JobScheduler jobScheduler;
    @Autowired
    private BatchJobManager jobManager;
    @Autowired
    private JobEngine jobEngine;
    @Autowired
    private FEPConfig fepConfig;

    @PostConstruct
    public void initialization() {
        SpringBeanFactoryUtil.registerBean(BatchQueueConsumers.class).subscribe(this);
    }

    /**
     * 是否同步化處理, 及一筆一筆訊息按照接收的順序處理
     *
     * @return
     */
    @Override
    public boolean isSynchronized() {
        // 要設置同步化接收, 也就是收到一條訊息, 處理一條訊息
        return true;
    }

    @Override
    public void messageReceived(String destination, PlainTextMessage payload, Message message) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
        LogData logData = new LogData();
        String messageIn = payload.getPayload();
        FEPBatch jobStartInfo;
        try {
            jobStartInfo = deserializeFromXml(messageIn, FEPBatch.class);
        } catch (Exception e) {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName(StringUtils.join(ProgramName, ".messageReceived"));
            logData.setRemark("parse message failed");
            logData.setMessage(messageIn);
            logData.setProgramException(e);
            sendEMS(logData);
            return;
        }
        jobHelper.log(logData, StringUtils.join(ProgramName, ".messageReceived"), jobStartInfo.getTaskParameters().getInstanceId(), StringUtils.join("Receive Job Definition:", messageIn));
        try {
            if (StringUtils.isNotBlank(jobStartInfo.getScheduleTask().getTaskName())) {
                if (Boolean.parseBoolean(jobStartInfo.getScheduleTask().getDelete())) {
                    jobHelper.log(logData, Level.WARN, StringUtils.join(ProgramName, ".messageReceived"), jobStartInfo.getTaskParameters().getInstanceId(), StringUtils.join("BatchJobService Begin to Delete Job",
                            ", BatchId:", jobStartInfo.getTaskParameters().getBatchId(),
                            ", TaskName:", jobStartInfo.getScheduleTask().getTaskName()));
                    jobManager.unscheduledJob(jobStartInfo.getTaskParameters().getBatchId(), Boolean.parseBoolean(jobStartInfo.getScheduleTask().getForceDelete()));
                    jobHelper.log(logData, Level.WARN, StringUtils.join(ProgramName, ".messageReceived"), jobStartInfo.getTaskParameters().getInstanceId(), StringUtils.join("BatchJobService Delete Job finished",
                            ", BatchId:", jobStartInfo.getTaskParameters().getBatchId(),
                            ", TaskName:", jobStartInfo.getScheduleTask().getTaskName()));
                    return;
                }
                ScheduleType sch = Enum.valueOf(ScheduleType.class, jobStartInfo.getScheduleTask().getScheduleType());
                switch (sch) {
                    case Daily:
                        jobScheduler.createDailyTask(
                                logData,
                                fepConfig.getHostName(),
                                jobStartInfo.getTaskParameters().getBatchId(),
                                jobStartInfo.getTaskParameters().getInstanceId(),
                                jobStartInfo.getScheduleTask().getTaskName(),
                                jobStartInfo.getScheduleTask().getTaskDescription(),
                                jobStartInfo.getScheduleTask().getAction(),
                                jobStartInfo.getScheduleTask().getActionArguments(),
                                jobStartInfo.getScheduleTask().getStartTime(),
                                Integer.parseInt(jobStartInfo.getScheduleTask().getDailyTrigger().getDaysInterval()));
                        break;
                    case DailyRepetition:
                        jobScheduler.createDailyRepetitionTask(
                                logData,
                                fepConfig.getHostName(),
                                jobStartInfo.getTaskParameters().getBatchId(),
                                jobStartInfo.getTaskParameters().getInstanceId(),
                                jobStartInfo.getScheduleTask().getTaskName(),
                                jobStartInfo.getScheduleTask().getTaskDescription(),
                                jobStartInfo.getScheduleTask().getAction(),
                                jobStartInfo.getScheduleTask().getActionArguments(),
                                jobStartInfo.getScheduleTask().getStartTime(),
                                Integer.parseInt(jobStartInfo.getScheduleTask().getDailyTrigger().getDaysInterval()),
                                Integer.parseInt(jobStartInfo.getScheduleTask().getDailyTrigger().getRepetitionInterval()),
                                Integer.parseInt(jobStartInfo.getScheduleTask().getDailyTrigger().getRepetitionDuration()));
                        break;
                    case Weekly:
                        jobScheduler.createWeeklyTask(
                                logData,
                                fepConfig.getHostName(),
                                jobStartInfo.getTaskParameters().getBatchId(),
                                jobStartInfo.getTaskParameters().getInstanceId(),
                                jobStartInfo.getScheduleTask().getTaskName(),
                                jobStartInfo.getScheduleTask().getTaskDescription(),
                                jobStartInfo.getScheduleTask().getAction(),
                                jobStartInfo.getScheduleTask().getActionArguments(),
                                jobStartInfo.getScheduleTask().getStartTime(),
                                Integer.parseInt(jobStartInfo.getScheduleTask().getWeeklyTrigger().getDaysOfWeek()),
                                Integer.parseInt(jobStartInfo.getScheduleTask().getWeeklyTrigger().getWeeksInterval()));
                        break;
                    case Monthly:
                        jobScheduler.createMonthlyTask(
                                logData,
                                fepConfig.getHostName(),
                                jobStartInfo.getTaskParameters().getBatchId(),
                                jobStartInfo.getTaskParameters().getInstanceId(),
                                jobStartInfo.getScheduleTask().getTaskName(),
                                jobStartInfo.getScheduleTask().getTaskDescription(),
                                jobStartInfo.getScheduleTask().getAction(),
                                jobStartInfo.getScheduleTask().getActionArguments(),
                                jobStartInfo.getScheduleTask().getStartTime(),
                                jobStartInfo.getScheduleTask().getMonthlyTrigger().getDaysOfMonth(),
                                Integer.parseInt(jobStartInfo.getScheduleTask().getMonthlyTrigger().getMonthsOfYear()),
                                Boolean.parseBoolean(jobStartInfo.getScheduleTask().getMonthlyTrigger().getRunOnLastDayOfMonth()));
                        break;
                    case MonthDayOfWeek:
                        int iWeeksOfMonth = Integer.parseInt(jobStartInfo.getScheduleTask().getMonthlyDayOfWeekTrigger().getWeeksOfMonth());
                        boolean bRunOnLastWeek = Boolean.parseBoolean(jobStartInfo.getScheduleTask().getMonthlyDayOfWeekTrigger().getRunOnLastWeekOfMonth());
                        if (bRunOnLastWeek) {
                            iWeeksOfMonth = WeeksOfMonth.LastWeek.getValue();
                        }
                        jobScheduler.createMonthDayOfWeekTask(
                                logData,
                                fepConfig.getHostName(),
                                jobStartInfo.getTaskParameters().getBatchId(),
                                jobStartInfo.getTaskParameters().getInstanceId(),
                                jobStartInfo.getScheduleTask().getTaskName(),
                                jobStartInfo.getScheduleTask().getTaskDescription(),
                                jobStartInfo.getScheduleTask().getAction(),
                                jobStartInfo.getScheduleTask().getActionArguments(),
                                jobStartInfo.getScheduleTask().getStartTime(),
                                Integer.parseInt(jobStartInfo.getScheduleTask().getMonthlyDayOfWeekTrigger().getMonthsOfYear()),
                                iWeeksOfMonth,
                                Integer.parseInt(jobStartInfo.getScheduleTask().getMonthlyDayOfWeekTrigger().getDaysOfWeek()), bRunOnLastWeek);
                        break;
                    default:
                        break;
                }
            } else {
                jobHelper.log(logData, StringUtils.join(ProgramName, ".messageReceived"),
                        jobStartInfo.getTaskParameters().getInstanceId(),
                        StringUtils.join("parse job Context, ",
                                "HostName:", fepConfig.getHostName(),
                                ",BatchId:", jobStartInfo.getTaskParameters().getBatchId(),
                                ",InstanceId:", jobStartInfo.getTaskParameters().getInstanceId(),
                                ",JobId:", jobStartInfo.getTaskParameters().getJobId(),
                                ",TaskId:", jobStartInfo.getTaskParameters().getTaskId(),
                                ",JobState:", jobStartInfo.getTaskParameters().getState(),
                                ",CustomParameter:", jobStartInfo.getTaskParameters().getCustomParameters()));
                jobEngine.runBatch(logData, jobStartInfo);
            }
        } catch (Exception e) {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName(StringUtils.join(ProgramName, ".messageReceived"));
            logData.setRemark(StringUtils.join("process message failed, messageIn:", messageIn));
            logData.setProgramException(e);
            logData.setMessage(jobStartInfo.getTaskParameters().getInstanceId());
            sendEMS(logData);
        }
    }
}
