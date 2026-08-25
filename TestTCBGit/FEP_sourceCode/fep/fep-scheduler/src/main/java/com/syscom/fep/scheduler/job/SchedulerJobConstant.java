package com.syscom.fep.scheduler.job;

public interface SchedulerJobConstant {
    public static final String CONFIGURATION_PROPERTIES_PREFIX = "spring.fep.scheduler.job";
    public static final String CONFIGURATION_PROPERTIES_REGISTER_0_CLASSNAME = "register[0].className";
    public static final String CONFIGURATION_PROPERTIES_REGISTER_0_CONFIGCLASSNAME = "register[0].configClassName";
    public static final String SCHEDULER_JOB_FACTORY_NAME = "fepSchedulerJobFactory";
    public static final String SCHEDULER_NAME = "FEPScheduler";
    public static final String SCHEDULER_JOB_FACTORY_SCHEDULER_NAME = "fepSchedulerJobFactoryBean";
    public static final String GROUP_NAME = "FEPSchedulerGroup";
    public static final String JOB_DATA_MAP_KEY_IS_SIMPLE = "isSimple";
    public static final String JOB_DATA_MAP_KEY_JOB_CONFIG = "jobConfig";
}
