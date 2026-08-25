package com.syscom.fep.scheduler.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(prefix = SchedulerJobConstant.CONFIGURATION_PROPERTIES_PREFIX, name = {SchedulerJobConstant.CONFIGURATION_PROPERTIES_REGISTER_0_CLASSNAME, SchedulerJobConstant.CONFIGURATION_PROPERTIES_REGISTER_0_CONFIGCLASSNAME})
public class SchedulerJobLauncher {
    @Autowired
    private SchedulerJobConfiguration configuration;
    @Autowired
    private SchedulerJobManager manager;

    @PostConstruct
    public void launch() {
        configuration.registerBean();
        manager.scheduleAllJobs();
    }
}
