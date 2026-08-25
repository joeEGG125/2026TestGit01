package com.syscom.fep.batch.job.impl;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.job.BatchJob;
import org.quartz.JobExecutionContext;

import com.syscom.fep.batch.invoker.JobSchedulerInvoker;
import com.syscom.fep.batch.job.BatchJobContext;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;

public class BatchJobInvoker extends BatchJob {

    @Override
    protected void execute(LogData logData, JobExecutionContext context, BatchJobContext batchJobContext) throws Exception {
        JobSchedulerInvoker jobSchedulerInvoker = SpringBeanFactoryUtil.getBean(batchJobContext.getAction());
        jobSchedulerInvoker.invoke(logData, batchJobContext);
    }
}
