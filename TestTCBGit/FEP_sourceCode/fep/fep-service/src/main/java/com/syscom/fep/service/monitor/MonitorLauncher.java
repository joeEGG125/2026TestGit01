package com.syscom.fep.service.monitor;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.scheduler.job.SchedulerJob;
import com.syscom.fep.scheduler.job.SchedulerJobManager;
import com.syscom.fep.service.monitor.controller.*;
import com.syscom.fep.service.monitor.job.*;
import com.syscom.fep.service.monitor.svr.MonitorChecker;
import com.syscom.fep.service.monitor.svr.MonitorCheckerConfiguration;
import com.syscom.fep.service.monitor.svr.MonitorSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;

public class MonitorLauncher {
    @Autowired
    private SchedulerJobManager schedulerJobManager;

    @PostConstruct
    public void start() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
        // 註冊MonitorChecker
        SpringBeanFactoryUtil.registerBean(MonitorCheckerConfiguration.class);
        SpringBeanFactoryUtil.registerBean(MonitorChecker.class);
        // 註冊Config
        SpringBeanFactoryUtil.registerBean(MonitorSchedulerJobConfig.class);
        SpringBeanFactoryUtil.registerBean(MonitorDumpJobConfig.class);
        SpringBeanFactoryUtil.registerBean(MonitorHeartbeatJobConfig.class);
        // 註冊Service
        SpringBeanFactoryUtil.registerBean(MonitorSchedulerService.class);
        // 註冊Job
        List<SchedulerJob<?>> schedulerJobList = Arrays.asList(
                SpringBeanFactoryUtil.registerBean(MonitorSchedulerJob.class),
                SpringBeanFactoryUtil.registerBean(MonitorDumpJob.class),
                SpringBeanFactoryUtil.registerBean(MonitorHeartbeatJob.class)
        );
        for (SchedulerJob<?> schedulerJob : schedulerJobList) {
            schedulerJobManager.scheduleJob(schedulerJob);
        }
        // 註冊Controller
        SpringBeanFactoryUtil.registerController(MonitorController.class);
        SpringBeanFactoryUtil.registerController(MonitorNetworkController.class);
        SpringBeanFactoryUtil.registerController(MonitorSystemController.class);
        SpringBeanFactoryUtil.registerController(MonitorHeartbeatController.class);
    }

    @PreDestroy
    public void terminate() {
        SpringBeanFactoryUtil.unregisterController(MonitorController.class);
        SpringBeanFactoryUtil.unregisterController(MonitorNetworkController.class);
        SpringBeanFactoryUtil.unregisterController(MonitorSystemController.class);
        SpringBeanFactoryUtil.unregisterController(MonitorHeartbeatController.class);
    }
}
