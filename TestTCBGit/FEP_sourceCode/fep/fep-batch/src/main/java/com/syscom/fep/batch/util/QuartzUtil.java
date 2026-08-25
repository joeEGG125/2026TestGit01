package com.syscom.fep.batch.util;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.delegate.ActionListener2;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class QuartzUtil {
    private static final LogHelper logger = LogHelperFactory.getGeneralLogger();

    private QuartzUtil() {}

    public static JobKey getJobKey(String name, String group) {
        return JobKey.jobKey(name, group);
    }

    public static TriggerKey getTriggerKey(String name, String group) {
        return TriggerKey.triggerKey(name, group);
    }

    public static GroupMatcher<JobKey> getGroupMatcher(String group) {
        return GroupMatcher.jobGroupEquals(group);
    }

    public static boolean isSchedulerExist(SchedulerFactoryBean batchSchedulerFactoryBean, String name, String group) {
        Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
        try {
            JobDetail jobDetail = scheduler.getJobDetail(getJobKey(name, group));
            return jobDetail != null;
        } catch (SchedulerException e) {
            logger.error(e, "isSchedulerExist failed!!");
            return false;
        }
    }

    public static Trigger getNextFireTrigger(SchedulerFactoryBean batchSchedulerFactoryBean, String name, String group) {
        try {
            Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(getJobKey(name, group));
            if (CollectionUtils.isNotEmpty(triggers)) {
                // 如果只有一個trigger, 則預設返回這一個就好
                if (triggers.size() == 1) {
                    return triggers.get(0);
                }
                // 如果所有的trigger中的nextFireTime都是null, 則預設返回第一個就好
                else if (triggers.stream().filter(t -> t.getNextFireTime() == null).count() == triggers.size()) {
                    return triggers.get(0);
                }
                // 取出nextFireTime最小的並且大於當前系統日期時間的那一筆trigger
                else {
                    return triggers.stream().sorted(Comparator.comparing(Trigger::getNextFireTime))
                            .filter(t -> t.getNextFireTime() != null && t.getNextFireTime().compareTo(Calendar.getInstance().getTime()) > 0)
                            .findFirst().orElse(null);
                }
            }
        } catch (SchedulerException e) {
            logger.error(e, "getNextFireTrigger failed!!");
        }
        return null;
    }

    public static void handleAllScheduler(SchedulerFactoryBean batchSchedulerFactoryBean, ActionListener2<Scheduler, JobKey> listener) {
        try {
            Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(getGroupMatcher(groupName))) {
                    listener.actionPerformed(scheduler, jobKey);
                }
            }
        } catch (SchedulerException e) {
            logger.error(e, "handleAllScheduler failed!!");
        }
    }

    public static <T> T getJobData(SchedulerFactoryBean batchSchedulerFactoryBean, String name, String group, Function<JobDataMap, T> function) {
        try {
            Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(QuartzUtil.getJobKey(name, group));
            if (jobDetail != null) {
                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                return function.apply(jobDataMap);
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, "getJobData failed!!");
        }
        return null;
    }

    public static <T> T getJobData(JobExecutionContext context, Function<JobDataMap, T> function) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        return function.apply(jobDataMap);
    }

    public static <T> void putJobData(JobExecutionContext context, T t, ActionListener2<JobDataMap, T> listener) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        listener.actionPerformed(jobDataMap, t);
    }

    public static String getTriggerInfo(SchedulerFactoryBean batchSchedulerFactoryBean, TriggerKey triggerKey) {
        Trigger trigger = null;
        try {
            Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
            trigger = scheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            logger.exceptionMsg(e, e.getMessage());
        }
        return trigger != null ? trigger.toString() : triggerKey.toString();
    }
}
