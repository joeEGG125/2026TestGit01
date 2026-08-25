package com.syscom.fep.batch.job;

import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.batch.base.vo.restful.BatchScheduler;
import com.syscom.fep.batch.base.vo.restful.request.ListSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.request.OperateSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.response.ListSchedulerResponse;
import com.syscom.fep.batch.base.vo.restful.response.OperateSchedulerResponse;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.batch.util.QuartzUtil;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@DependsOn("batchConfiguration")
public class BatchJobManager {
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    @Qualifier(BatchConfiguration.SCHEDULER_JOB_FACTORY_SCHEDULER_NAME)
    private SchedulerFactoryBean batchSchedulerFactoryBean;
    @Autowired
    private FEPConfig fepConfig;

    /**
     * 將指定的批次加入排程中
     *
     * @param batchJobContext
     * @param jobClass
     * @param triggers
     * @param scheduleInfo
     * @throws SchedulerException
     */
    public void scheduleJob(BatchJobContext batchJobContext, Class<? extends Job> jobClass, Set<Trigger> triggers, Object... scheduleInfo) throws SchedulerException {
        batchJobContext.setScheduleInfo(StringUtils.join(scheduleInfo));
        logger.info(batchJobContext.getLogContent(), "start to schedule...");
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(QuartzUtil.getJobKey(batchJobContext.getBatchId(), batchJobContext.getGroup()))
                .withDescription(batchJobContext.getBatchId())
                .storeDurably(true)
                .build();
        batchJobContext.putJobDataMap(jobDetail.getJobDataMap());
        Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
        scheduler.scheduleJob(jobDetail, triggers, false);
        scheduler.start();
        logger.info(batchJobContext.getLogContent(), "schedule job successful, ", batchJobContext.getScheduleInfo());
    }

    /**
     * 從排程中移除批次
     *
     * @param batchId
     * @param forceDelete
     * @throws SchedulerException
     */
    public void unscheduledJob(String batchId, boolean forceDelete) throws SchedulerException {
        BatchJobContext batchJobContext = this.getBatchJobContext(batchId);
        if (batchJobContext == null && forceDelete) {
            batchJobContext = new BatchJobContext();
            batchJobContext.setBatchId(batchId);
            batchJobContext.setGroup(batchId);
        }
        if (batchJobContext == null) {
            logger.warn("Cannot unscheduled job cause job not exist, batchId = [", batchId, "]");
            return;
        }
        logger.info(batchJobContext.getLogContent(), "start to unscheduled and delete...");
        Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
        JobKey jobKey = QuartzUtil.getJobKey(batchJobContext.getBatchId(), batchJobContext.getGroup());
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
            logger.info(batchJobContext.getLogContent(), "pause job successful");
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            if (CollectionUtils.isNotEmpty(triggers)) {
                for (Trigger trigger : triggers) {
                    scheduler.unscheduleJob(trigger.getKey());
                    logger.info(batchJobContext.getLogContent(), "remove trigger successful, trigger:", trigger);
                }
            } else {
                logger.warn(batchJobContext.getLogContent(), "cannot remove trigger cause trigger not exist");
            }
            scheduler.deleteJob(jobKey);
            logger.info(batchJobContext.getLogContent(), "delete job successful");
        } else {
            logger.warn(batchJobContext.getLogContent(), "cannot unscheduled cause Quartz Job not exist");
        }
    }

    public boolean isSchedulerExist(String batchId) {
        return QuartzUtil.isSchedulerExist(batchSchedulerFactoryBean, batchId, batchId);
    }

    public Trigger getNextFireTrigger(String batchId) {
        return QuartzUtil.getNextFireTrigger(batchSchedulerFactoryBean, batchId, batchId);
    }

    public Map<String, Date> getAllNextFireTimeMap() {
        HashMap<String, Date> map = new HashMap<>();
        QuartzUtil.handleAllScheduler(batchSchedulerFactoryBean, (scheduler, jobKey) -> {
            String batchId = jobKey.getGroup(); // 這個group取出來的實際上是batchId
            BatchScheduler batchScheduler = getBatchScheduler(batchId);
            Date nextFireTime = batchScheduler != null ? batchScheduler.getNextFireTime() : null;
            if (nextFireTime == null) {
                logger.warn("cannot fetch nextFireTime, batchId:", batchId);
                return;
            }
            map.put(batchId, nextFireTime);
            logger.info("fetch nextFireTime succeed, batchId:", batchId, ", nextFireTime:", FormatUtil.dateTimeFormat(nextFireTime));
        });
        return map;
    }

    public ListSchedulerResponse listScheduler(ListSchedulerRequest request) {
        ListSchedulerResponse response = new ListSchedulerResponse();
        if (request == null || CollectionUtils.isEmpty(request.getBatchSchedulerList())) {
            response.setResult(false);
            response.setMessage("Request BatchId List cannot be empty!!!");
        } else {
            List<BatchScheduler> batchSchedulerRespList = new ArrayList<>();
            for (BatchScheduler batchSchedulerReq : request.getBatchSchedulerList()) {
                BatchScheduler batchSchedulerResp = this.getBatchScheduler(batchSchedulerReq.getBatchId());
                if (batchSchedulerResp != null) {
                    batchSchedulerRespList.add(batchSchedulerResp);
                }
            }
            response.setBatchSchedulerList(batchSchedulerRespList);
        }
        return response;
    }

    public OperateSchedulerResponse operateScheduler(OperateSchedulerRequest request) {
        OperateSchedulerResponse response = new OperateSchedulerResponse();
        if (request == null || CollectionUtils.isEmpty(request.getBatchSchedulerList())) {
            response.setResult(false);
            response.setMessage("Request BatchId List cannot be empty!!!");
        } else {
            ArrayList<BatchScheduler> batchSchedulerRespList = new ArrayList<>();
            Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
            for (BatchScheduler batchSchedulerReq : request.getBatchSchedulerList()) {
                // 如果不存在, 則跳出
                if (!this.isSchedulerExist(batchSchedulerReq.getBatchId())) {
                    continue;
                }
                GroupMatcher<JobKey> groupMatcher = QuartzUtil.getGroupMatcher(batchSchedulerReq.getBatchId());
                try {
                    switch (request.getAction()) {
                        case pause:
                            scheduler.pauseJobs(groupMatcher);
                            break;
                        case resume:
                            scheduler.resumeJobs(groupMatcher);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    logger.error(e, "operateScheduler failed, batchId = [", batchSchedulerReq.getBatchId(), "]");
                    response.setMessage(e.getMessage());
                    response.setResult(false);
                }
                BatchScheduler batchSchedulerResp = this.getBatchScheduler(batchSchedulerReq.getBatchId());
                if (batchSchedulerResp != null) {
                    batchSchedulerRespList.add(batchSchedulerResp);
                }
            }
            response.setBatchSchedulerList(batchSchedulerRespList);
        }
        return response;
    }

    public BatchScheduler getBatchScheduler(String batchId) {
        // 如果quartz資料不存在, 則直接返回傳入的request物件
        if (!this.isSchedulerExist(batchId)) {
            return null;
        }
        // 取出Quartz的next fire time
        Trigger nextFireTrigger = null;
        TriggerState triggerState = TriggerState.NONE;
        try {
            Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
            nextFireTrigger = this.getNextFireTrigger(batchId);
            if (nextFireTrigger != null) {
                triggerState = scheduler.getTriggerState(nextFireTrigger.getKey());
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
        }
        // 更新batchJobContext中的NextFireDateTime
        try {
            Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(QuartzUtil.getJobKey(batchId, batchId));
            if (jobDetail != null) {
                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                BatchJobContext batchJobContext = BatchJobContext.getJobDataMap(jobDataMap);
                if (batchJobContext != null && nextFireTrigger != null) {
                    batchJobContext.setNextFireDateTime(nextFireTrigger.getNextFireTime());
                    batchJobContext.putJobDataMap(jobDataMap);
                }
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
        }
        // BatchScheduler
        BatchScheduler batchScheduler = new BatchScheduler();
        batchScheduler.setHostName(fepConfig.getHostName());
        batchScheduler.setBatchId(batchId);
        batchScheduler.setTriggerState(triggerState != null ? triggerState : TriggerState.NONE);
        // trigger.getNextFireTime()需要判斷一下是否為null, 因為有些排程是有設定運行持續時間, 例如持續運行1個小時, 則過了1個小時後, quartz會自動刪除這個排程, 所以這裡取出的trigger.getNextFireTime()會為null
        batchScheduler.setNextFireTime(nextFireTrigger != null && nextFireTrigger.getNextFireTime() != null ? CalendarUtil.clone(nextFireTrigger.getNextFireTime()).getTime() : null);
        return batchScheduler;
    }

    /**
     * 根據batchId取出BatchJobContext
     *
     * @param batchId
     * @return
     */
    public BatchJobContext getBatchJobContext(String batchId) {
        return QuartzUtil.getJobData(batchSchedulerFactoryBean, batchId, batchId, BatchJobContext::getJobDataMap);
    }
}
