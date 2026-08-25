package com.syscom.fep.common.http;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.frmcommon.net.http.HttpClient2Config;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy
public class HttpClientConfigure implements HttpClientConfigureConstant {
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();

    @Bean(BEAN_NAME_CONFIG_MONITOR)
    @ConfigurationProperties(prefix = CONFIGURATION_PREFIX_MONITOR)
    @Lazy
    public HttpClient2Config monitorHttpClientConfig() {
        return new HttpClient2Config();
    }

    @Bean(BEAN_NAME_MONITOR)
    @Lazy
    public HttpClient2 monitorHttpClient(@Qualifier(BEAN_NAME_CONFIG_MONITOR) HttpClient2Config config) {
        logger.info(ConfigurationPropertiesUtil.info(config, "FEP HTTP Client Monitor Configuration", CONFIGURATION_PREFIX_MONITOR, false));
        return new HttpClient2(BEAN_NAME_MONITOR, config);
    }

    @Bean(BEAN_NAME_CONFIG_NOTIFY)
    @ConfigurationProperties(prefix = CONFIGURATION_PREFIX_NOTIFY)
    @Lazy
    public HttpClient2Config notifyHttpClientConfig() {
        return new HttpClient2Config();
    }

    @Bean(BEAN_NAME_NOTIFY)
    @Lazy
    public HttpClient2 notifyHttpClient(@Qualifier(BEAN_NAME_CONFIG_NOTIFY) HttpClient2Config config) {
        config.setExceptionLogging(true); // 預設要列印出異常訊息
        logger.info(ConfigurationPropertiesUtil.info(config, "FEP HTTP Client Notify Configuration", CONFIGURATION_PREFIX_NOTIFY, false));
        return new HttpClient2(BEAN_NAME_NOTIFY, config);
    }
}