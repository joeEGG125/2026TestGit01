package com.syscom.fep.scheduler.job;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@DependsOn("schedulerJobConfiguration")
public class SchedulerJobManager implements SchedulerJobConstant {
    private static final LogHelper ScheduleLogger = LogHelperFactory.getSchedulerLogger();
    private final List<Job> scheduledJobList = new ArrayList<>();

    @Autowired
    @Qualifier(SCHEDULER_JOB_FACTORY_SCHEDULER_NAME)
    private SchedulerFactoryBean schedulerJobFactoryBean;

    protected void scheduleAllJobs() {
        Job[] jobs = null;
        synchronized (this.scheduledJobList) {
            jobs = new SchedulerJob[this.scheduledJobList.size()];
            this.scheduledJobList.toArray(jobs);
        }
        if (ArrayUtils.isNotEmpty(jobs)) {
            for (Job job : jobs) {
                if (job instanceof SchedulerJob) {
                    this.scheduleJob((SchedulerJob<?>) job);
                } else if (job instanceof SchedulerSimpleJob) {
                    this.scheduleJob((SchedulerSimpleJob<?>) job);
                }
            }
        }
    }

    public <JobConfig extends SchedulerJobConfig> void scheduleJob(SchedulerJob<JobConfig> job) {
        job.scheduleJob(schedulerJobFactoryBean.getScheduler());
    }

    public <JobConfig extends SchedulerJobConfig> void scheduleJob(SchedulerSimpleJob<JobConfig> job) {
        job.scheduleJob(schedulerJobFactoryBean.getScheduler());
    }

    protected void addJob(Job job) {
        synchronized (this.scheduledJobList) {
            this.scheduledJobList.add(job);
        }
    }

    protected void removeJob(Job job) {
        synchronized (this.scheduledJobList) {
            this.scheduledJobList.remove(job);
        }
    }
}
