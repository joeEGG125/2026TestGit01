package com.syscom.fep.batch.service;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.ScheduleType;
import com.syscom.fep.batch.base.vo.restful.BatchScheduler;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.batch.job.BatchJobContext;
import com.syscom.fep.batch.job.BatchJobManager;
import com.syscom.fep.batch.job.impl.BatchJobInvoker;
import com.syscom.fep.batch.util.QuartzUtil;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.mail.MailSender;
import com.syscom.fep.common.sms.mitake.MitakeSmsOperator;
import com.syscom.fep.configuration.util.FEPNotifyUtil;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.mail.MailData;
import com.syscom.fep.frmcommon.mail.MailPriority;
import com.syscom.fep.frmcommon.scheduler.CronExpressionGenerator;
import com.syscom.fep.frmcommon.scheduler.enums.DaysOfTheWeek;
import com.syscom.fep.frmcommon.scheduler.enums.MonthsOfTheYear;
import com.syscom.fep.frmcommon.scheduler.enums.MonthsType;
import com.syscom.fep.frmcommon.scheduler.enums.WeeksOfTheMonth;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.model.Batch;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.DateBuilder.IntervalUnit;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class JobScheduler extends FEPBase {
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private BatchConfiguration configuration;
    @Autowired
    private BatchJobManager manager;
    @Autowired
    private JobHelper jobHelper;
    private ScheduledExecutorService updateBatchNextRunTimeTimer, checkAndNotifyAllMisfiredTimer;

    @PostConstruct
    public void postConstruct() {
        // 定時更新所有批次的BATCH.BATCH_NEXTRUNTIME欄位值
        updateNextRunTime();
        // 依據BATCH.BATCH_NEXTRUNTIME定時檢查所有批次是否有Misfired並發送mail
        checkAndNotifyMisfired();
    }

    /**
     * 定時更新所有批次的BATCH.BATCH_NEXTRUNTIME欄位值
     */
    private void updateNextRunTime() {
        // 是否需要啟動定時更新所有批次的BATCH.BATCH_NEXTRUNTIME欄位值
        if (configuration.isUpdateNextRunTimeTimerEnabled() && configuration.getUpdateNextRunTimeIntervalMilliseconds() > 0) {
            // 每隔1分鐘更新下次執行時間
            updateBatchNextRunTimeTimer = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory(StringUtils.join(ProgramName, "-updateNextRunTime")));
            updateBatchNextRunTimeTimer.scheduleAtFixedRate(() -> {
                LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
                LogData logData = new LogData();
                Map<String, Date> nextFireTimeMap = manager.getAllNextFireTimeMap();
                if (MapUtils.isNotEmpty(nextFireTimeMap)) {
                    for (Entry<String, Date> entry : nextFireTimeMap.entrySet()) {
                        String key = entry.getKey();
                        if (StringUtils.isBlank(key)) {
                            continue;
                        }
                        Date nextFireTime = entry.getValue();
                        try {
                            int batchId = Integer.parseInt(key);
                            jobHelper.updateBatchNextRunTime(logData, batchId, StringUtils.EMPTY, nextFireTime);
                            logger.info("[updateBatchNextRunTime]batchId:", batchId, ", nextFireTime:", FormatUtil.dateTimeFormat(nextFireTime), "]");
                        } catch (NumberFormatException e) {
                            logger.warn(e, "BatchId格式不正確:", key);
                        }
                    }
                }
            }, configuration.getUpdateNextRunTimeInitialDelayMilliseconds(), configuration.getUpdateNextRunTimeIntervalMilliseconds(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 2025-11-11 Richard add
     * 批次服務中增加一個定時的Timer(時間間隔定在參數,預設30分)
     * Timer時間到時, 根據以下邏輯判斷, 若有符合條件, 則進行發mail通知
     * 1.查出批次主檔有設定排程BATCH_SCHEDULE=1(有排程)的資料
     * 2.逐筆檢查 系統時間是否 > (應執行時間(BATCH_NEXTRUNTIME) + 1分鐘), 若大於代表已過了預計啟動的時間1分鐘但還未收到批次啟動的要求, 則發mail通知
     * 3.通知的mail address來自spring.fep.batch.maillist設定, 通知的body內容如下:
     * 批次平台仍未收到預計在{BATCH_NEXTRUNTIME}執行{批次名稱}的要求, 請確認批次執行歷程是否有啟動記錄!
     * by Ashiang
     */
    private void checkAndNotifyMisfired() {
        // 判斷是否有設定CheckAndNotifyAllMisfiredIntervalSeconds
        if (configuration.getCheckAndNotifyAllMisfiredIntervalSeconds() > 0) {
            // 判斷是否有設定mail sender
            MailSender mailSender = SpringBeanFactoryUtil.getBean(MailSender.class, false);
            // 判斷是否有設定sms operator
            MitakeSmsOperator smsOperator = SpringBeanFactoryUtil.getBean(MitakeSmsOperator.class, false);
            if (mailSender != null || smsOperator != null) {
                checkAndNotifyAllMisfiredTimer = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory(StringUtils.join(ProgramName, "-checkAndNotifyMisfired")));
                checkAndNotifyAllMisfiredTimer.scheduleAtFixedRate(() -> {
                    LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
                    LogData logData = new LogData();
                    // 1.查出批次主檔有設定排程BATCH_SCHEDULE=1(有排程)的資料
                    List<Batch> batches = jobHelper.getAllScheduledBatch(logData);
                    if (CollectionUtils.isNotEmpty(batches)) {
                        for (Batch batch : batches) {
                            // 檢查是否是運行在本機的批次程式
                            if (manager.isSchedulerExist(Integer.toString(batch.getBatchBatchid()))) {
                                // 取出BATCH.BATCH_NEXTRUNTIME
                                Date batchNextruntime = batch.getBatchNextruntime();
                                // 再取出Quartz的Next Fire Time
                                Date quartzNextFireTime = null;
                                BatchScheduler batchScheduler = manager.getBatchScheduler(Integer.toString(batch.getBatchBatchid()));
                                if (batchScheduler != null) {
                                    quartzNextFireTime = batchScheduler.getNextFireTime();
                                } else {
                                    logData.setSubSys(SubSystem.CMN);
                                    logData.setChannel(FEPChannel.BATCH);
                                    logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                                    logData.setRemark(StringUtils.join("Cannot get Quartz Next Fire time, batchId: ", batch.getBatchBatchid()));
                                    sendEMS(logData);
                                }
                                logData.setSubSys(SubSystem.CMN);
                                logData.setChannel(FEPChannel.BATCH);
                                logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                                // 如果db中沒有資料, 則嘗試透過quartz也无法取到, 则處理下一筆
                                if (batchNextruntime == null && quartzNextFireTime == null) {
                                    logData.setRemark(StringUtils.join("Both BatchNextruntime and QuartzNextFireTime is null, batchBatchid:", batch.getBatchBatchid(), ", batchExecuteHostName:", batch.getBatchExecuteHostName()));
                                    logMessage(Level.WARN, logData);
                                    continue;
                                } else if (batchNextruntime != null && quartzNextFireTime != null && batchNextruntime.before(quartzNextFireTime)) {
                                    logData.setRemark(StringUtils.join("BatchNextruntime is before than QuartzNextFireTime, batchBatchid:", batch.getBatchBatchid(), ", batchExecuteHostName:", batch.getBatchExecuteHostName(),
                                            ", BatchNextruntime:", FormatUtil.dateTimeFormat(batchNextruntime), ", QuartzNextFireTime:", FormatUtil.dateTimeFormat(quartzNextFireTime)));
                                    logMessage(Level.WARN, logData);
                                } else if (batchNextruntime == null) {
                                    logData.setRemark(StringUtils.join("Overwrite BatchNextruntime, cause BatchNextruntime is null, batchBatchid:", batch.getBatchBatchid(), ", batchExecuteHostName:", batch.getBatchExecuteHostName(),
                                            ", QuartzNextFireTime:", FormatUtil.dateTimeFormat(quartzNextFireTime)));
                                    logMessage(Level.WARN, logData);
                                    batchNextruntime = quartzNextFireTime;
                                }
                                // 2.逐筆檢查 系統時間是否 > (應執行時間(BATCH_NEXTRUNTIME) + 1分鐘), 若大於代表已過了預計啟動的時間1分鐘但還未收到批次啟動的要求, 則發mail通知
                                long now = System.currentTimeMillis();
                                if (now > batchNextruntime.getTime() + configuration.getCheckAndNotifyAllMisfiredToleranceSeconds() * 1000L) {
                                    String body = StringUtils.join(
                                            "批次平台 ",
                                            FEPConfig.getInstance().getHostName(),
                                            " 於 ",
                                            FormatUtil.dateTimeFormat(now),
                                            " 仍未收到預計在",
                                            FormatUtil.dateTimeFormat(batchNextruntime.getTime()),
                                            " 執行 ",
                                            batch.getBatchName(),
                                            " 的要求, 請確認批次執行歷程是否有啟動記錄!");
                                    boolean notified = false;
                                    // 寄送mail
                                    if (mailSender != null) {
                                        String notifyMail = batch.getBatchNotifymail();
                                        List<String> mailTo = FEPNotifyUtil.getNotifiyList(notifyMail);
                                        String mailList = configuration.getMailList();
                                        if (CollectionUtils.isNotEmpty(mailTo) || StringUtils.isNotBlank(mailList)) {
                                            String to = CollectionUtils.isNotEmpty(mailTo) ? StringUtils.join(mailTo, ',') : configuration.getMailList();
                                            logData.setSubSys(SubSystem.CMN);
                                            logData.setChannel(FEPChannel.BATCH);
                                            logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                                            logData.setRemark(StringUtils.join("Batch was misfired, begin to send mail, batchBatchid:", batch.getBatchBatchid(), ", batchExecuteHostName:", batch.getBatchExecuteHostName(), ", mail to:[", to, "], body:", body));
                                            logMessage(Level.WARN, logData);
                                            try {
                                                MailData mailData = new MailData();
                                                mailData.setFrom(configuration.getMailSender());
                                                mailData.setTo(to);
                                                mailData.setSubject("FEP批次管理系統通知");
                                                mailData.setBody(body);
                                                mailData.setPriority(MailPriority.High);
                                                mailSender.sendSimpleEmail(mailData);
                                                logData.setSubSys(SubSystem.CMN);
                                                logData.setChannel(FEPChannel.BATCH);
                                                logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                                                logData.setRemark(StringUtils.join("Batch was misfired, and send mail finished, batchBatchid:", batch.getBatchBatchid(), ", batchExecuteHostName:", batch.getBatchExecuteHostName(), ", mail to:[", to, "], body:", body));
                                                logMessage(Level.INFO, logData);
                                            } catch (Exception e) {
                                                logData.setSubSys(SubSystem.CMN);
                                                logData.setChannel(FEPChannel.BATCH);
                                                logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                                                logData.setRemark("send mail failed");
                                                logData.setProgramException(e);
                                                sendEMS(logData);
                                            } finally {
                                                notified = true;
                                            }
                                        }
                                    }
                                    // 送SMS
                                    if (smsOperator != null) {
                                        String notifyPhone = batch.getBatchNotifyphone();
                                        List<String> smsTelList = FEPNotifyUtil.getNotifiyList(notifyPhone);
                                        logData.setSubSys(SubSystem.CMN);
                                        logData.setChannel(FEPChannel.BATCH);
                                        logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                                        logData.setRemark(StringUtils.join("Batch was misfired, begin to send sms, batchBatchid:", batch.getBatchBatchid(), ", batchExecuteHostName:", batch.getBatchExecuteHostName(), ", sms list:[", StringUtils.join(smsTelList, ","), "], body:", body));
                                        logMessage(Level.WARN, logData);
                                        try {
                                            smsOperator.send(smsTelList, body);
                                            logData.setSubSys(SubSystem.CMN);
                                            logData.setChannel(FEPChannel.BATCH);
                                            logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                                            logData.setRemark(StringUtils.join("Batch was misfired, and send sms finished, batchBatchid:", batch.getBatchBatchid(), ", batchExecuteHostName:", batch.getBatchExecuteHostName(), ", sms list:[", StringUtils.join(smsTelList, ","), "], body:", body));
                                            logMessage(Level.INFO, logData);
                                        } catch (Exception e) {
                                            logData.setSubSys(SubSystem.CMN);
                                            logData.setChannel(FEPChannel.BATCH);
                                            logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                                            logData.setRemark("send sms failed");
                                            logData.setProgramException(e);
                                            sendEMS(logData);
                                        } finally {
                                            notified = true;
                                        }
                                    }
                                    // 如果mail和sms都無法送, 則這裡印log
                                    if (!notified) {
                                        logData.setSubSys(SubSystem.CMN);
                                        logData.setChannel(FEPChannel.BATCH);
                                        logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                                        logData.setRemark(body);
                                        logMessage(Level.WARN, logData);
                                    }
                                }
                            } else {
                                logData.setSubSys(SubSystem.CMN);
                                logData.setChannel(FEPChannel.BATCH);
                                logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                                logData.setRemark(StringUtils.join("scheduled batch not exist, batchBatchid:", batch.getBatchBatchid(), ", batchExecuteHostName:", batch.getBatchExecuteHostName()));
                                logMessage(Level.WARN, logData);
                            }
                        }
                    } else {
                        logData.setSubSys(SubSystem.CMN);
                        logData.setChannel(FEPChannel.BATCH);
                        logData.setProgramName(StringUtils.join(ProgramName, ".checkAndNotifyMisfired"));
                        logData.setRemark("empty scheduled batch list");
                        logMessage(Level.WARN, logData);
                    }
                }, configuration.getCheckAndNotifyAllMisfiredInitialDelaySeconds(), configuration.getCheckAndNotifyAllMisfiredIntervalSeconds(), TimeUnit.SECONDS);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        logger.trace(ProgramName, " start to destroy...");
        ThreadPoolFactory.shutdown(updateBatchNextRunTimeTimer, ProgramName, "-updateNextRunTime");
        ThreadPoolFactory.shutdown(checkAndNotifyAllMisfiredTimer, ProgramName, "-checkAndNotifyMisfired");
    }

    public void createDailyTask(LogData logData, String hostName, String batchId, String instanceId, String taskName, String taskDescription, String action, String actionArguments, String startTime, int daysInterval) throws SchedulerException {
        manager.unscheduledJob(batchId, false);
        Date triggerStartTime = this.parseDateTime(startTime);
        BatchJobContext batchJobContext = new BatchJobContext(ScheduleType.Daily, hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments);
        Set<Trigger> triggers = getDailyTrigger(logData, batchId, instanceId, triggerStartTime, daysInterval);
        manager.scheduleJob(
                batchJobContext,
                BatchJobInvoker.class,
                triggers,
                "batchId:", batchId,
                ",taskName:", taskName,
                ",startTime:", startTime,
                ",daysInterval:", daysInterval
        );
    }

    public void createDailyRepetitionTask(LogData logData, String hostName, String batchId, String instanceId, String taskName, String taskDescription, String action, String actionArguments, String startTime, int daysInterval, int minutesInterval, int hoursDuration) throws SchedulerException {
        manager.unscheduledJob(batchId, false);
        Date triggerStartTime = this.parseDateTime(startTime);
        BatchJobContext batchJobContext = new BatchJobContext(ScheduleType.DailyRepetition, hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments);
        Set<Trigger> triggers = getDailyRepetitionTrigger(logData, batchId, instanceId, triggerStartTime, daysInterval, minutesInterval, hoursDuration);
        manager.scheduleJob(
                batchJobContext,
                BatchJobInvoker.class,
                triggers,
                "batchId:", batchId,
                ",taskName:", taskName,
                ",startTime:", startTime,
                ",daysInterval:", daysInterval,
                ",minutesInterval:", minutesInterval,
                ",hoursDuration:", hoursDuration
        );
    }

    public void createWeeklyTask(LogData logData, String hostName, String batchId, String instanceId, String taskName, String taskDescription, String action, String actionArguments, String startTime, int daysOfWeek, int weeksInterval) throws SchedulerException {
        manager.unscheduledJob(batchId, false);
        Date triggerStartTime = this.parseDateTime(startTime);
        BatchJobContext batchJobContext = new BatchJobContext(ScheduleType.Weekly, hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments);
        Set<Trigger> triggers = getWeeklyTrigger(logData, batchId, instanceId, triggerStartTime, daysOfWeek, weeksInterval);
        manager.scheduleJob(
                batchJobContext,
                BatchJobInvoker.class,
                triggers,
                "batchId:", batchId,
                ",taskName:", taskName,
                ",startTime:", startTime,
                ",daysOfWeek:", daysOfWeek, StringUtils.join("(", StringUtils.join(MathUtil.splitByPow2(daysOfWeek).stream().map(DaysOfTheWeek::fromValue).map(Enum::name).toArray(), ","), ")"),
                ",weeksInterval:", weeksInterval
        );
    }

    public void createMonthlyTask(LogData logData, String hostName, String batchId, String instanceId, String taskName, String taskDescription, String action, String actionArguments, String startTime, String daysOfMonth, int monthsOfYear, boolean runOnLastDayOfMonth) throws SchedulerException {
        manager.unscheduledJob(batchId, false);
        Date triggerStartTime = this.parseDateTime(startTime);
        BatchJobContext batchJobContext = new BatchJobContext(ScheduleType.Monthly, hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments);
        Set<Trigger> triggers = getMonthlyTrigger(logData, batchId, instanceId, triggerStartTime, daysOfMonth, monthsOfYear, runOnLastDayOfMonth);
        manager.scheduleJob(
                batchJobContext,
                BatchJobInvoker.class,
                triggers,
                "batchId:", batchId,
                ",taskName:", taskName,
                ",startTime:", startTime,
                ",daysOfMonth:", daysOfMonth,
                ",monthsOfYear:", monthsOfYear, StringUtils.join("(", StringUtils.join(MathUtil.splitByPow2(monthsOfYear).stream().map(MonthsOfTheYear::fromValue).map(Enum::name).toArray(), ","), ")"),
                ",runOnLastDayOfMonth:", runOnLastDayOfMonth
        );
    }

    public void createMonthDayOfWeekTask(LogData logData, String hostName, String batchId, String instanceId, String taskName, String taskDescription, String action, String actionArguments, String startTime, int monthsOfYear, int weekOfMonth, int daysOfWeek, boolean runOnLastWeekOfMonth) throws SchedulerException {
        manager.unscheduledJob(batchId, false);
        Date triggerStartTime = this.parseDateTime(startTime);
        BatchJobContext batchJobContext = new BatchJobContext(ScheduleType.MonthDayOfWeek, hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments);
        Set<Trigger> triggers = getMonthDayOfWeekTrigger(logData, batchId, instanceId, triggerStartTime, monthsOfYear, weekOfMonth, daysOfWeek, runOnLastWeekOfMonth);
        manager.scheduleJob(
                batchJobContext,
                BatchJobInvoker.class,
                triggers,
                "batchId:", batchId,
                ",taskName:", taskName,
                ",startTime:", startTime,
                ",monthsOfYear:", monthsOfYear, StringUtils.join("(", StringUtils.join(MathUtil.splitByPow2(monthsOfYear).stream().map(MonthsOfTheYear::fromValue).map(Enum::name).toArray(), ","), ")"),
                ",weekOfMonth:", weekOfMonth, StringUtils.join("(", StringUtils.join(MathUtil.splitByPow2(weekOfMonth).stream().map(WeeksOfTheMonth::fromValue).map(Enum::name).toArray(), ","), ")"),
                ",daysOfWeek:", daysOfWeek, StringUtils.join("(", StringUtils.join(MathUtil.splitByPow2(daysOfWeek).stream().map(DaysOfTheWeek::fromValue).map(Enum::name).toArray(), ","), ")"),
                ",runOnLastWeekOfMonth:", (weekOfMonth == WeeksOfTheMonth.LastWeek.getValue()) || runOnLastWeekOfMonth
        );
    }

    private Date parseDateTime(String dateTime) {
        if (StringUtils.isNotBlank(dateTime)) {
            try {
                return FormatUtil.parseDataTime(dateTime, FormatUtil.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS);
            } catch (ParseException e) {
                logger.warn("Parse startTime:", dateTime, "] with ParseException occur, exception message:", e.getMessage());
            }
        }
        return Calendar.getInstance().getTime();
    }

    private Set<Trigger> getDailyTrigger(LogData logData, String batchId, String instanceId, Date triggerStartTime, int daysInterval) {
        jobHelper.log(logData, ProgramName + ".getDailyTrigger", instanceId, StringUtils.join("Try to create CalendarIntervalTrigger, ",
                "batchId:", batchId,
                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                ",daysInterval:", daysInterval,
                ",misfire:", configuration.getMisfirePolicy()));
        CalendarIntervalTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(QuartzUtil.getTriggerKey(batchId, batchId))
                .withDescription(batchId)
                .startAt(triggerStartTime)
                .withSchedule(getCalendarIntervalScheduleBuilder(daysInterval, IntervalUnit.DAY))
                .build();
        jobHelper.log(logData, ProgramName + ".getDailyTrigger", instanceId, StringUtils.join("Create CalendarIntervalTrigger succeed, ",
                "batchId:", batchId,
                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                ",daysInterval:", daysInterval,
                ",misfire:", trigger.getMisfireInstruction(),
                ",trigger:", trigger.toString()));
        return Sets.newHashSet(trigger);
    }

    private Set<Trigger> getDailyRepetitionTrigger(LogData logData, String batchId, String instanceId, Date triggerStartTime, int intervalInDays, int intervalInMinutes, int hoursDuration) {
        // 先檢核
        if (intervalInMinutes > 60 * 24) {
            throw ExceptionUtil.createIllegalArgumentException("intervalInMinutes = [", intervalInMinutes, "], which cannot be greater than 1440!!!");
        } else if (hoursDuration > 24) {
            throw ExceptionUtil.createIllegalArgumentException("hoursDuration = [", hoursDuration, "], which cannot be greater than 24!!!");
        }
        jobHelper.log(logData, ProgramName + ".getDailyRepetitionTrigger", instanceId, StringUtils.join("Try to create CalendarIntervalTrigger, ",
                "batchId:", batchId,
                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                ",intervalInDays:", intervalInDays,
                ",intervalInMinutes:", intervalInMinutes,
                ",hoursDuration:", hoursDuration,
                ",misfire:", configuration.getMisfirePolicy()));
        Set<Trigger> triggers = new LinkedHashSet<>();
        // 如果每隔分鐘小於等於0, 表示定點執行
        if (intervalInMinutes <= 0) {
            CalendarIntervalTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(QuartzUtil.getTriggerKey(batchId, batchId))
                    .withDescription(batchId)
                    .startAt(triggerStartTime)
                    .withSchedule(getCalendarIntervalScheduleBuilder(intervalInDays, IntervalUnit.DAY))
                    .build();
            triggers.add(trigger);
            jobHelper.log(logData, ProgramName + ".getDailyRepetitionTrigger", instanceId, StringUtils.join("Create CalendarIntervalTrigger succeed, ",
                    "batchId:", batchId,
                    ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                    ",intervalInDays:", intervalInDays,
                    ",misfire:", trigger.getMisfireInstruction(),
                    ",trigger:", trigger.toString()));
        }
        // 每天每隔分鐘數持續執行
        else if (intervalInDays <= 0 && hoursDuration <= 0) {
            CalendarIntervalTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(QuartzUtil.getTriggerKey(batchId, batchId))
                    .withDescription(batchId)
                    .startAt(triggerStartTime)
                    .withSchedule(getCalendarIntervalScheduleBuilder(intervalInMinutes, IntervalUnit.MINUTE))
                    .build();
            triggers.add(trigger);
            jobHelper.log(logData, ProgramName + ".getDailyRepetitionTrigger", instanceId, StringUtils.join("Create CalendarIntervalTrigger succeed, ",
                    "batchId:", batchId,
                    ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                    ",intervalInMinutes:", intervalInMinutes,
                    ",misfire:", trigger.getMisfireInstruction(),
                    ",trigger:", trigger.toString()));
        }
        // 每隔天幾天每天每隔分鐘數持續執行小時
        else {
            int minutes = 0;
            while (true) {
                Date startAt = CalendarUtil.add(triggerStartTime, Calendar.MINUTE, minutes).getTime();
                CalendarIntervalTrigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(QuartzUtil.getTriggerKey(StringUtils.join(batchId, "-", minutes), batchId)) // 注意這裡的trigger name必須要不同的命名, 否則會被覆蓋
                        .withDescription(StringUtils.join(batchId, "-", minutes))
                        .startAt(startAt)
                        .withSchedule(getCalendarIntervalScheduleBuilder(intervalInDays, IntervalUnit.DAY))
                        .build();
                triggers.add(trigger);
                jobHelper.log(logData, ProgramName + ".getDailyRepetitionTrigger", instanceId, StringUtils.join("Create CalendarIntervalTrigger succeed, ",
                        "batchId:", batchId,
                        ",startAt:", FormatUtil.dateTimeFormat(startAt),
                        ",intervalInDays:", intervalInDays,
                        ",misfire:", trigger.getMisfireInstruction(),
                        ",trigger:", trigger.toString()));
                // 累加分鐘
                minutes += intervalInMinutes;
                // 如果持續小時大於0, 並且累加的分鐘數已經超過持續的小時, 則跳出
                if (hoursDuration > 0 && minutes >= hoursDuration * 60) { // 2026-01-04 Richard modified 判斷改為>=, 如果大於等於則表示間隔時間剛好到達, 則不要再執行
                    break;
                }
                // 如果持續小時小於等於0, 並且累加的分鐘數已經超過24的小時, 則跳出
                else if (hoursDuration <= 0 && minutes >= 24 * 60) { // 2026-01-04 Richard modified 判斷改為>=, 如果大於等於則表示間隔時間剛好到達, 則不要再執行
                    break;
                }
            }
        }
        return triggers;
    }

    private Set<Trigger> getWeeklyTrigger(LogData logData, String batchId, String instanceId, Date triggerStartTime, int dayOfWeek, int intervalInWeeks) {
        List<Integer> daysOfWeekList = MathUtil.splitByPow2(dayOfWeek);
        List<DaysOfTheWeek> daysOfTheWeekList = new ArrayList<>();
        for (int daysOfWeek : daysOfWeekList) {
            daysOfTheWeekList.add(DaysOfTheWeek.fromValue(daysOfWeek));
        }
        jobHelper.log(logData, ProgramName + ".getWeeklyTrigger", instanceId, StringUtils.join("Try to create CalendarIntervalTrigger, ",
                "batchId:", batchId,
                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                ",dayOfWeek:", dayOfWeek, (CollectionUtils.isNotEmpty(daysOfTheWeekList) ? StringUtils.join("(", StringUtils.join(daysOfTheWeekList.stream().map(Enum::name).toArray(), ","), ")") : StringUtils.EMPTY),
                ",intervalInWeeks:", intervalInWeeks,
                ",misfire:", configuration.getMisfirePolicy()));
        Set<Trigger> triggers = new LinkedHashSet<>();
        // 如果有指定週
        if (CollectionUtils.isNotEmpty(daysOfTheWeekList)) {
            for (int i = 0; i < 7; i++) {
                Calendar startAt = CalendarUtil.add(triggerStartTime, Calendar.DATE, i);
                int dayOfTheWeek = CalendarUtil.getDayOfWeek(startAt);
                DaysOfTheWeek daysOfTheWeek = daysOfTheWeekList.stream().filter(t -> dayOfTheWeek == t.getDayOfWeek()).findFirst().orElse(null);
                if (daysOfTheWeek != null) {
                    CalendarIntervalTrigger trigger = TriggerBuilder.newTrigger()
                            .withIdentity(QuartzUtil.getTriggerKey(StringUtils.join(batchId, "-", daysOfTheWeek.getShortName()), batchId))
                            .withDescription(StringUtils.join(batchId, "-", daysOfTheWeek.getShortName()))
                            .startAt(startAt.getTime())
                            .withSchedule(getCalendarIntervalScheduleBuilder(intervalInWeeks, IntervalUnit.WEEK))
                            .build();
                    triggers.add(trigger);
                    jobHelper.log(logData, ProgramName + ".getWeeklyTrigger", instanceId, StringUtils.join("Create CalendarIntervalTrigger succeed, ",
                            "batchId:", batchId,
                            ",startAt:", FormatUtil.dateTimeFormat(startAt.getTime()),
                            ",daysOfTheWeek:", daysOfTheWeek,
                            ",intervalInWeeks:", intervalInWeeks,
                            ",misfire:", trigger.getMisfireInstruction(),
                            ",trigger:", trigger.toString()));
                }
            }
        }
        // 如果沒有指定週, 則每隔intervalInWeeks週運行一次
        else {
            CalendarIntervalTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(QuartzUtil.getTriggerKey(batchId, batchId))
                    .withDescription(batchId)
                    .startAt(triggerStartTime)
                    .withSchedule(getCalendarIntervalScheduleBuilder(intervalInWeeks, IntervalUnit.WEEK))
                    .build();
            triggers.add(trigger);
            jobHelper.log(logData, ProgramName + ".getWeeklyTrigger", instanceId, StringUtils.join("Create CalendarIntervalTrigger succeed, ",
                    "batchId:", batchId,
                    ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                    ",intervalInWeeks:", intervalInWeeks,
                    ",misfire:", trigger.getMisfireInstruction(),
                    ",trigger:", trigger.toString()));
        }
        return triggers;
    }

    private Set<Trigger> getMonthlyTrigger(LogData logData, String batchId, String instanceId, Date triggerStartTime, String daysOfMonth, int monthsOfYear, boolean runOnLastDayOfMonth) {
        // 日期
        List<Integer> daysOfTheMonthList = new ArrayList<>();
        if (StringUtils.isNotBlank(daysOfMonth)) {
            for (String day : daysOfMonth.split(",")) {
                daysOfTheMonthList.add(Integer.parseInt(day));
            }
        }
        // 月份
        List<MonthsOfTheYear> monthsOfTheYearList = new ArrayList<>();
        List<Integer> monthOfYearList = MathUtil.splitByPow2(monthsOfYear);
        for (int monthOfYear : monthOfYearList) {
            monthsOfTheYearList.add(MonthsOfTheYear.fromValue(monthOfYear));
        }
        jobHelper.log(logData, ProgramName + ".getMonthlyTrigger", instanceId, StringUtils.join("Try to create Trigger, ",
                "batchId:", batchId,
                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                ",monthsOfYear:", monthsOfYear, StringUtils.join("(", StringUtils.join(monthsOfTheYearList.stream().map(Enum::name).toArray(), ","), ")"),
                ",daysOfMonth:", daysOfMonth,
                ",misfire:", configuration.getMisfirePolicy()));
        Set<Trigger> triggers = new LinkedHashSet<>();
        // 如果每個月每天運行, 相當於建立DailyTrigger, 每日定時運行
        if (monthsOfYear == MonthsOfTheYear.AllMonths.getValue() && daysOfTheMonthList.size() == 31) {
            CalendarIntervalTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(QuartzUtil.getTriggerKey(batchId, batchId))
                    .withDescription(batchId)
                    .startAt(triggerStartTime)
                    .withSchedule(getCalendarIntervalScheduleBuilder(0, IntervalUnit.DAY))
                    .build();
            triggers.add(trigger);
            jobHelper.log(logData, ProgramName + ".getMonthlyTrigger", instanceId, StringUtils.join("Create CalendarIntervalTrigger succeed, ",
                    "batchId:", batchId,
                    ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                    ",misfire:", trigger.getMisfireInstruction(),
                    ",trigger:", trigger.toString()));
        }
        // 如果是指定月份指定天運行
        else {
            // 先設定沒有每月最後一天運行
            String cronExpression = CronExpressionGenerator.generateCronExpressionByMonthly(triggerStartTime, monthsOfYear, daysOfMonth, false);
            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(QuartzUtil.getTriggerKey(batchId, batchId))
                    .withDescription(batchId)
                    .startAt(triggerStartTime)
                    .withSchedule(getCronScheduleBuilder(cronExpression))
                    .build();
            triggers.add(cronTrigger);
            jobHelper.log(logData, ProgramName + ".getMonthlyTrigger", instanceId, StringUtils.join("Create CronTrigger succeed, ",
                    "batchId:", batchId,
                    ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                    ",monthsOfYear:", monthsOfYear, StringUtils.join("(", StringUtils.join(monthsOfTheYearList.stream().map(Enum::name).toArray(), ","), ")"),
                    ",daysOfMonth:", daysOfMonth,
                    ",runOnLastDayOfMonth:false",
                    ",misfire:", cronTrigger.getMisfireInstruction(),
                    ",cronExpression:", cronExpression,
                    ",trigger:", cronTrigger.toString()));
            // 設定每月最後一天運行
            if (runOnLastDayOfMonth) {
                // 判斷是否有2月份
                // 2月份比較特殊, 因為會區分閏年與非閏年, 所以2月的最後一天會是28日或者29日
                MonthsOfTheYear february = monthsOfTheYearList.stream().filter(t -> t == MonthsOfTheYear.February).findFirst().orElse(null);
                if (february != null) {
                    // 如果不含有28日和29日
                    if (!daysOfTheMonthList.contains(28) && !daysOfTheMonthList.contains(29)) {
                        // 建立2月最後一天運行的trigger
                        String cronExpressionFebruaryLastDay = CronExpressionGenerator.generateCronExpressionByMonthly(triggerStartTime, february.getValue(), null, true);
                        CronTrigger cronTriggerFebruaryLastDay = TriggerBuilder.newTrigger()
                                .withIdentity(QuartzUtil.getTriggerKey(StringUtils.join(batchId, "-", february.getShortName(), "-L"), batchId))
                                .withDescription(StringUtils.join(batchId, "-", february.getShortName(), "-L"))
                                .startAt(triggerStartTime)
                                .withSchedule(getCronScheduleBuilder(cronExpressionFebruaryLastDay))
                                .build();
                        triggers.add(cronTriggerFebruaryLastDay);
                        jobHelper.log(logData, ProgramName + ".getMonthlyTrigger", instanceId, StringUtils.join("Create CronTrigger succeed, ",
                                "batchId:", batchId,
                                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                                ",monthsOfYear:", february,
                                ",runOnLastDayOfMonth:", runOnLastDayOfMonth,
                                ",misfire:", cronTriggerFebruaryLastDay.getMisfireInstruction(),
                                ",cronExpression:", cronExpressionFebruaryLastDay,
                                ",trigger:", cronTriggerFebruaryLastDay.toString()));
                    }
                    // 如果有28日或者29日, 則需要重新建trigger
                    else {
                        // 1.先移除掉
                        triggers.remove(cronTrigger);
                        jobHelper.log(logData, Level.WARN, ProgramName + ".getMonthlyTrigger", instanceId, StringUtils.join("Remove CronTrigger succeed, ",
                                "batchId:", batchId,
                                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                                ",monthsOfYear:", monthsOfYear, StringUtils.join("(", StringUtils.join(monthsOfTheYearList.stream().map(Enum::name).toArray(), ","), ")"),
                                ",daysOfMonth:", daysOfMonth,
                                ",runOnLastDayOfMonth:false",
                                ",misfire:", cronTrigger.getMisfireInstruction(),
                                ",cronExpression:", cronExpression,
                                ",trigger:", cronTrigger.toString()));
                        // 移除掉2月份
                        monthsOfYear -= february.getValue();
                        monthsOfTheYearList.remove(february);
                        // 2.先設定沒有2月份每月指定天運行
                        String cronExpressionWithoutFebruary = CronExpressionGenerator.generateCronExpressionByMonthly(triggerStartTime, monthsOfYear, daysOfMonth, false);
                        CronTrigger cronTriggerWithoutFebruary = TriggerBuilder.newTrigger()
                                .withIdentity(QuartzUtil.getTriggerKey(batchId, batchId))
                                .withDescription(batchId)
                                .startAt(triggerStartTime)
                                .withSchedule(getCronScheduleBuilder(cronExpressionWithoutFebruary))
                                .build();
                        triggers.add(cronTriggerWithoutFebruary);
                        jobHelper.log(logData, ProgramName + ".getMonthlyTrigger", instanceId, StringUtils.join("Create CronTrigger succeed, ",
                                "batchId:", batchId,
                                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                                ",monthsOfYear:", monthsOfYear, StringUtils.join("(", StringUtils.join(monthsOfTheYearList.stream().map(Enum::name).toArray(), ","), ")"),
                                ",daysOfMonth:", daysOfMonth,
                                ",runOnLastDayOfMonth:false",
                                ",misfire:", cronTriggerWithoutFebruary.getMisfireInstruction(),
                                ",cronExpression:", cronExpressionWithoutFebruary,
                                ",trigger:", cronTriggerWithoutFebruary.toString()));
                        // 3.再設定2月份除28日和29日指定天運行
                        daysOfMonth = StringUtils.remove(daysOfMonth, ",28");
                        daysOfMonth = StringUtils.remove(daysOfMonth, ",29");
                        String cronExpressionWithoutFebruary2829 = CronExpressionGenerator.generateCronExpressionByMonthly(triggerStartTime, february.getValue(), daysOfMonth, false);
                        CronTrigger cronTriggerWithoutFebruary2829 = TriggerBuilder.newTrigger()
                                .withIdentity(QuartzUtil.getTriggerKey(StringUtils.join(batchId, "-", february.getShortName()), batchId))
                                .withDescription(StringUtils.join(batchId, "-", february.getShortName()))
                                .startAt(triggerStartTime)
                                .withSchedule(getCronScheduleBuilder(cronExpressionWithoutFebruary2829))
                                .build();
                        triggers.add(cronTriggerWithoutFebruary2829);
                        jobHelper.log(logData, ProgramName + ".getMonthlyTrigger", instanceId, StringUtils.join("Create CronTrigger succeed, ",
                                "batchId:", batchId,
                                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                                ",monthsOfYear:", february.getValue(), StringUtils.join("(", february, ")"),
                                ",daysOfMonth:", daysOfMonth,
                                ",runOnLastDayOfMonth:false",
                                ",misfire:", cronTriggerWithoutFebruary2829.getMisfireInstruction(),
                                ",cronExpression:", cronExpressionWithoutFebruary2829,
                                ",trigger:", cronTriggerWithoutFebruary2829.toString()));
                        // 4.建立2月最後一天運行的trigger
                        String cronExpressionFebruaryLastDay = CronExpressionGenerator.generateCronExpressionByMonthly(triggerStartTime, february.getValue(), null, true);
                        CronTrigger cronTriggerFebruaryLastDay = TriggerBuilder.newTrigger()
                                .withIdentity(QuartzUtil.getTriggerKey(StringUtils.join(batchId, "-", february.getShortName(), "-L"), batchId))
                                .withDescription(StringUtils.join(batchId, "-", february.getShortName(), "-L"))
                                .startAt(triggerStartTime)
                                .withSchedule(getCronScheduleBuilder(cronExpressionFebruaryLastDay))
                                .build();
                        triggers.add(cronTriggerFebruaryLastDay);
                        jobHelper.log(logData, ProgramName + ".getMonthlyTrigger", instanceId, StringUtils.join("Create CronTrigger succeed, ",
                                "batchId:", batchId,
                                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                                ",monthsOfYear:", february.getValue(), StringUtils.join("(", february, ")"),
                                ",runOnLastDayOfMonth:true",
                                ",misfire:", cronTriggerFebruaryLastDay.getMisfireInstruction(),
                                ",cronExpression:", cronExpressionFebruaryLastDay,
                                ",trigger:", cronTriggerFebruaryLastDay.toString()));
                    }
                }
                // 如果不含有30日
                if (!daysOfTheMonthList.contains(30)) {
                    // 取出除2月份所有的小月
                    for (MonthsOfTheYear monthsOfTheYear : monthsOfTheYearList) {
                        if (monthsOfTheYear == MonthsOfTheYear.February || monthsOfTheYear.getMonthsType() != MonthsType.LUNAR)
                            continue;
                        // 建立小月最後一天運行的trigger
                        String cronExpressionLunarLastDay = CronExpressionGenerator.generateCronExpressionByMonthly(triggerStartTime, monthsOfTheYear.getValue(), null, true);
                        CronTrigger cronTriggerLunarLastDay = TriggerBuilder.newTrigger()
                                .withIdentity(QuartzUtil.getTriggerKey(StringUtils.join(batchId, "-", monthsOfTheYear.getShortName(), "-L"), batchId))
                                .withDescription(StringUtils.join(batchId, "-", monthsOfTheYear.getShortName(), "-L"))
                                .startAt(triggerStartTime)
                                .withSchedule(getCronScheduleBuilder(cronExpressionLunarLastDay))
                                .build();
                        triggers.add(cronTriggerLunarLastDay);
                        jobHelper.log(logData, ProgramName + ".getMonthlyTrigger", instanceId, StringUtils.join("Create CronTrigger succeed, ",
                                "batchId:", batchId,
                                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                                ",monthsOfYear:", monthsOfTheYear.getValue(), StringUtils.join("(", monthsOfTheYear, ")"),
                                ",runOnLastDayOfMonth:true",
                                ",misfire:", cronTriggerLunarLastDay.getMisfireInstruction(),
                                ",cronExpression:", cronExpressionLunarLastDay,
                                ",trigger:", cronTriggerLunarLastDay.toString()));
                    }
                }
                // 如果不含有31日
                if (!daysOfTheMonthList.contains(31)) {
                    // 取出所有的大月
                    for (MonthsOfTheYear monthsOfTheYear : monthsOfTheYearList) {
                        if (monthsOfTheYear.getMonthsType() != MonthsType.SOLAR)
                            continue;
                        // 建立大月最後一天運行的trigger
                        String cronExpressionSolarLastDay = CronExpressionGenerator.generateCronExpressionByMonthly(triggerStartTime, monthsOfTheYear.getValue(), null, true);
                        CronTrigger cronTriggerSolarLastDay = TriggerBuilder.newTrigger()
                                .withIdentity(QuartzUtil.getTriggerKey(StringUtils.join(batchId, "-", monthsOfTheYear.getShortName(), "-L"), batchId))
                                .withDescription(StringUtils.join(batchId, "-", monthsOfTheYear.getShortName(), "-L"))
                                .startAt(triggerStartTime)
                                .withSchedule(getCronScheduleBuilder(cronExpressionSolarLastDay))
                                .build();
                        triggers.add(cronTriggerSolarLastDay);
                        jobHelper.log(logData, ProgramName + ".getMonthlyTrigger", instanceId, StringUtils.join("Create CronTrigger succeed, ",
                                "batchId:", batchId,
                                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                                ",monthsOfYear:", monthsOfTheYear.getValue(), StringUtils.join("(", monthsOfTheYear, ")"),
                                ",runOnLastDayOfMonth:true",
                                ",misfire:", cronTriggerSolarLastDay.getMisfireInstruction(),
                                ",cronExpression:", cronExpressionSolarLastDay,
                                ",trigger:", cronTriggerSolarLastDay.toString()));
                    }
                }
            }
        }
        return triggers;
    }

    private Set<Trigger> getMonthDayOfWeekTrigger(LogData logData, String batchId, String instanceId, Date triggerStartTime, int monthsOfYear, int weekOfMonth, int dayOfWeek, boolean runOnLastWeekOfMonth) {
        // 月份
        List<MonthsOfTheYear> monthsOfTheYearList = new ArrayList<>();
        List<Integer> monthOfYearList = MathUtil.splitByPow2(monthsOfYear);
        for (int monthOfYear : monthOfYearList) {
            monthsOfTheYearList.add(MonthsOfTheYear.fromValue(monthOfYear));
        }
        // 月份中的週
        List<WeeksOfTheMonth> weeksOfTheMonthList = new ArrayList<>();
        List<Integer> weeksOfMonthList = MathUtil.splitByPow2(weekOfMonth);
        for (int weeksOfMonth : weeksOfMonthList) {
            weeksOfTheMonthList.add(WeeksOfTheMonth.fromValue(weeksOfMonth));
        }
        // 週中的日
        List<DaysOfTheWeek> daysOfTheWeekList = new ArrayList<>();
        List<Integer> daysOfWeekList = MathUtil.splitByPow2(dayOfWeek);
        for (int daysOfWeek : daysOfWeekList) {
            daysOfTheWeekList.add(DaysOfTheWeek.fromValue(daysOfWeek));
        }
        jobHelper.log(logData, ProgramName + ".getMonthDayOfWeekTrigger", instanceId, StringUtils.join("Try to create Trigger, ",
                "batchId:", batchId,
                ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                ",monthsOfYear:", monthsOfYear, StringUtils.join("(", StringUtils.join(monthsOfTheYearList.stream().map(Enum::name).toArray(), ","), ")"),
                ",weekOfMonth:", weekOfMonth, StringUtils.join("(", StringUtils.join(weeksOfTheMonthList.stream().map(Enum::name).toArray(), ","), ")"),
                ",dayOfWeek:", dayOfWeek, StringUtils.join("(", StringUtils.join(daysOfTheWeekList.stream().map(Enum::name).toArray(), ","), ")"),
                ",runOnLastWeekOfMonth:", runOnLastWeekOfMonth,
                ",misfire:", configuration.getMisfirePolicy()));
        Set<Trigger> triggers = new LinkedHashSet<>();
        // 如果選擇所有月所有周所有天, 相當於建立DailyTrigger, 每日定時運行
        if (monthsOfYear == MonthsOfTheYear.AllMonths.getValue() && weekOfMonth == WeeksOfTheMonth.AllWeeks.getValue() && dayOfWeek == DaysOfTheWeek.AllDays.getValue()) {
            CalendarIntervalTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(QuartzUtil.getTriggerKey(batchId, batchId))
                    .withDescription(batchId)
                    .startAt(triggerStartTime)
                    .withSchedule(getCalendarIntervalScheduleBuilder(0, IntervalUnit.DAY))
                    .build();
            triggers.add(trigger);
            jobHelper.log(logData, ProgramName + ".getMonthDayOfWeekTrigger", instanceId, StringUtils.join("Create CalendarIntervalTrigger succeed, ",
                    "batchId:", batchId,
                    ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                    ",misfire:", trigger.getMisfireInstruction(),
                    ",trigger:", trigger.toString()));
        }
        // 如果週全選
        else if (weekOfMonth == WeeksOfTheMonth.AllWeeks.getValue()) {
            String cronExpression = CronExpressionGenerator.generateCronExpressionByMonthDayOfWeek(triggerStartTime, monthsOfYear, weekOfMonth, dayOfWeek, false);
            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(QuartzUtil.getTriggerKey(batchId, batchId))
                    .withDescription(batchId)
                    .startAt(triggerStartTime)
                    .withSchedule(getCronScheduleBuilder(cronExpression))
                    .build();
            triggers.add(cronTrigger);
            jobHelper.log(logData, ProgramName + ".getMonthDayOfWeekTrigger", instanceId, StringUtils.join("Create CronTrigger succeed, ",
                    "batchId:", batchId,
                    ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                    ",monthsOfYear:", monthsOfYear, StringUtils.join("(", StringUtils.join(monthsOfTheYearList.stream().map(Enum::name).toArray(), ","), ")"),
                    ",weekOfMonth:", weekOfMonth, StringUtils.join("(", StringUtils.join(weeksOfTheMonthList.stream().map(Enum::name).toArray(), ","), ")"),
                    ",dayOfWeek:", dayOfWeek, StringUtils.join("(", StringUtils.join(daysOfTheWeekList.stream().map(Enum::name).toArray(), ","), ")"),
                    ",runOnLastWeekOfMonth:false",
                    ",misfire:", cronTrigger.getMisfireInstruction(),
                    ",cronExpression:", cronExpression,
                    ",trigger:", cronTrigger.toString()));
        } else {
            // 如果有指定週, 注意此種情況下第四週和最後一週可能會出現trigger time重複的問題, 必須要手動控制
            for (WeeksOfTheMonth weeksOfTheMonth : weeksOfTheMonthList) {
                for (DaysOfTheWeek daysOfTheWeek : daysOfTheWeekList) {
                    String cronExpression = CronExpressionGenerator.generateCronExpressionByMonthDayOfWeek(triggerStartTime, monthsOfYear, weeksOfTheMonth.getValue(), daysOfTheWeek.getValue(), false);
                    CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                            .withIdentity(QuartzUtil.getTriggerKey(StringUtils.join(batchId, "-", weeksOfTheMonth.name(), "-", daysOfTheWeek.getShortName()), batchId))
                            .withDescription(StringUtils.join(batchId, "-", weeksOfTheMonth.name(), "-", daysOfTheWeek.getShortName()))
                            .startAt(triggerStartTime)
                            .withSchedule(getCronScheduleBuilder(cronExpression))
                            .build();
                    triggers.add(cronTrigger);
                    jobHelper.log(logData, ProgramName + ".getMonthDayOfWeekTrigger", instanceId, StringUtils.join("Create CronTrigger succeed, ",
                            "batchId:", batchId,
                            ",startAt:", FormatUtil.dateTimeFormat(triggerStartTime),
                            ",monthsOfYear:", monthsOfYear, StringUtils.join("(", StringUtils.join(monthsOfTheYearList.stream().map(Enum::name).toArray(), ","), ")"),
                            ",weekOfMonth:", weekOfMonth, StringUtils.join("(", weeksOfTheMonth, ")"),
                            ",dayOfWeek:", daysOfTheWeek.getValue(), StringUtils.join("(", daysOfTheWeek, ")"),
                            ",runOnLastWeekOfMonth:false",
                            ",misfire:", cronTrigger.getMisfireInstruction(),
                            ",cronExpression:", cronExpression,
                            ",trigger:", cronTrigger.toString()));
                }
            }
        }
        return triggers;
    }

    private CalendarIntervalScheduleBuilder getCalendarIntervalScheduleBuilder(int timeInterval, IntervalUnit unit) {
        // 設定MisfirePolicy, 先取出配置檔設定的值
        int misfirePolicy = configuration.getMisfirePolicy();
        CalendarIntervalScheduleBuilder builder = CalendarIntervalScheduleBuilder.calendarIntervalSchedule();
        // Misfire之後什麼事情都不做
        if (misfirePolicy == CalendarIntervalTrigger.MISFIRE_INSTRUCTION_DO_NOTHING) {
            builder = builder.withMisfireHandlingInstructionDoNothing();
        }
        // Misfire之後會做一次, 并且仅仅会做一次
        else if (misfirePolicy == CalendarIntervalTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW) {
            builder = builder.withMisfireHandlingInstructionFireAndProceed();
        }
        // 忽略Misfire, 即所有Misfire的事情全部做一次
        else if (misfirePolicy == CalendarIntervalTrigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) {
            builder = builder.withMisfireHandlingInstructionIgnoreMisfires();
        }
        // 間隔多少分/天/週
        if (timeInterval > 0 || unit == IntervalUnit.WEEK) {
            // 如果是天/週, 要給timeInterval加1
            if (unit == IntervalUnit.DAY || unit == IntervalUnit.WEEK) {
                timeInterval++;
            }
            builder.withInterval(timeInterval, unit);
        }
        return builder;
    }

    private CronScheduleBuilder getCronScheduleBuilder(String cronExpression) {
        // 設定MisfirePolicy, 先取出配置檔設定的值
        int misfirePolicy = configuration.getMisfirePolicy();
        CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule(cronExpression);
        // Misfire之後什麼事情都不做
        if (misfirePolicy == CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING) {
            builder = builder.withMisfireHandlingInstructionDoNothing();
        }
        // Misfire之後會做一次, 并且仅仅会做一次
        else if (misfirePolicy == CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW) {
            builder = builder.withMisfireHandlingInstructionFireAndProceed();
        }
        // 忽略Misfire, 即所有Misfire的事情全部做一次
        else if (misfirePolicy == CronTrigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) {
            builder = builder.withMisfireHandlingInstructionIgnoreMisfires();
        }
        return builder;
    }
}
