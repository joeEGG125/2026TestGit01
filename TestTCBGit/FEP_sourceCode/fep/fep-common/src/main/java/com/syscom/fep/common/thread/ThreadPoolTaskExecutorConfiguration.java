package com.syscom.fep.common.thread;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolConfiguration;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Lazy
// @RefreshScope
public class ThreadPoolTaskExecutorConfiguration implements ThreadPoolTaskExecutorConstant {

    @Bean(name = BEAN_NAME_FEP_CONFIG)
    @ConfigurationProperties(prefix = CONFIGURATION_FEP_PROPERTIES_PREFIX)
    public ThreadPoolConfiguration threadPoolConfiguration() {
        return new ThreadPoolConfiguration();
    }

    @Bean(name = BEAN_NAME_FEP_THREAD_POOL)
    // @RefreshScope
    public ThreadPoolTaskExecutor taskExecutor(@Qualifier(BEAN_NAME_FEP_CONFIG) ThreadPoolConfiguration configuration) {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(configuration, "FEP Thread Pool Configuration", CONFIGURATION_FEP_PROPERTIES_PREFIX, false));
        return ThreadPoolFactory.createTaskExecutor(configuration, new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
