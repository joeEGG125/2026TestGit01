package com.syscom.fep.web.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.safeaa.configuration.DataSourceSafeaaConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
// @RefreshScope
public class SafeaaInitialConfiguration implements DataSourceSafeaaConstant {
    @Qualifier(DataSourceConstant.BEAN_NAME_DATASOURCE)
    @Autowired
    private DataSource fepdbDataSource;

    @Bean(name = BEAN_NAME_DATASOURCE)
    @ConditionalOnProperty(prefix = CONFIGURATION_PROPERTIES_PREFIX, name = "enable", havingValue = "false", matchIfMissing = true)
    public DataSource buildProperties() {
        LogHelperFactory.getTraceLogger().debug("********************[SAFEAA API]Use FEPDB DataSource SpringBean Object");
        return fepdbDataSource;
    }
}
