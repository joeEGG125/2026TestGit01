package com.syscom.fep.scheduler.job;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import jakarta.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.List;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = SchedulerJobConstant.CONFIGURATION_PROPERTIES_PREFIX)
// @RefreshScope
public class SchedulerJobConfiguration implements SchedulerJobConstant {
    private final LogHelper logger = LogHelperFactory.getSchedulerLogger();
    private List<SchedulerJobRegister> register;
    private String threadPoolClass = "org.quartz.simpl.SimpleThreadPool";
    private int threadPoolCount = 10;
    private int threadPoolPriority = 5;
    private boolean sendEMS = true;

    public void setRegister(List<SchedulerJobRegister> register) {
        this.register = register;
    }

    public void setThreadPoolClass(String threadPoolClass) {
        this.threadPoolClass = threadPoolClass;
    }

    public void setThreadPoolCount(int threadPoolCount) {
        this.threadPoolCount = threadPoolCount;
    }

    public void setThreadPoolPriority(int threadPoolPriority) {
        this.threadPoolPriority = threadPoolPriority;
    }

    public boolean isSendEMS() {
        return sendEMS;
    }

    public void setSendEMS(boolean sendEMS) {
        this.sendEMS = sendEMS;
    }

    /**
     * 判斷job是否enable, 預設是true
     *
     * @param jobClassName
     * @return
     */
    public boolean isSchedulerJobEnabled(String jobClassName) {
        return EnvPropertiesUtil.getProperty(StringUtils.join(CONFIGURATION_PROPERTIES_PREFIX, ".", jobClassName), true);
    }

    protected void registerBean() {
        if (CollectionUtils.isNotEmpty(register)) {
            for (SchedulerJobRegister jobRegister : register) {
                // 先檢查是否有特別設定enabled為false, 否則預設按true註冊
                if (!isSchedulerJobEnabled(jobRegister.getClassName())) {
                    logger.warn("Don't register bean = [", jobRegister, "], cause it is not enabled");
                    continue;
                }
                try {
                    SpringBeanFactoryUtil.registerBean(jobRegister.getConfigClassName());
                    SpringBeanFactoryUtil.registerBean(jobRegister.getClassName());
                } catch (Exception e) {
                    logger.warn("Cannot register bean = [", jobRegister, "], maybe it is needless for this application");
                }
            }
        }
    }

    @Bean(name = SCHEDULER_JOB_FACTORY_NAME)
    @Primary
    public JobFactory schedulerJobFactory() {
        return new AdaptableJobFactory() {
            @Override
            protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
                JobDetail jobDetail = bundle.getJobDetail();
                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                if (jobDataMap.getBooleanValue(JOB_DATA_MAP_KEY_IS_SIMPLE)) {
                    SchedulerSimpleJob job = (SchedulerSimpleJob) super.createJobInstance(bundle);
                    SchedulerJobConfig config = (SchedulerJobConfig) jobDataMap.get(JOB_DATA_MAP_KEY_JOB_CONFIG);
                    job.setConfig(config);
                    return job;
                }
                return SpringBeanFactoryUtil.getBean(jobDetail.getJobClass());
            }
        };
    }

    @Bean(name = SCHEDULER_JOB_FACTORY_SCHEDULER_NAME)
    @Primary
    public SchedulerFactoryBean schedulerJobFactoryBean(@Qualifier(SCHEDULER_JOB_FACTORY_NAME) JobFactory factory) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setBeanName(SCHEDULER_JOB_FACTORY_SCHEDULER_NAME);
        schedulerFactoryBean.setJobFactory(factory);
        schedulerFactoryBean.setSchedulerName(SCHEDULER_NAME);
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceName", SCHEDULER_NAME);
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        prop.put("org.quartz.threadPool.class", threadPoolClass);
        prop.put("org.quartz.threadPool.threadCount", Integer.toString(threadPoolCount));
        prop.put("org.quartz.threadPool.threadPriority", Integer.toString(threadPoolPriority));
        schedulerFactoryBean.setQuartzProperties(prop);
        return schedulerFactoryBean;
    }

    @PostConstruct
    public void print() {
        StringBuilder sb = new StringBuilder(ConfigurationPropertiesUtil.info(this, "Scheduler Job Configuration", "logger"));
        if (CollectionUtils.isNotEmpty(register)) {
            String key = null;
            for (SchedulerJobRegister jobRegister : register) {
                key = StringUtils.join(CONFIGURATION_PROPERTIES_PREFIX, ".", jobRegister.getClassName());
                String enabled = EnvPropertiesUtil.getProperty(key, null);
                if (enabled != null) {
                    sb.append(StringUtils.repeat(StringUtils.SPACE, 2)).append(key).append(" = ").append(enabled).append("\r\n");
                }
            }
        }
        logger.info(sb.toString());
    }
}
