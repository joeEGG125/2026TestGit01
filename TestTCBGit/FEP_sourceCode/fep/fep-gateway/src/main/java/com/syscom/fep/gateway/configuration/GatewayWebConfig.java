package com.syscom.fep.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConfigurationProperties(prefix = "spring.fep.gateway.web")
// @RefreshScope
public class GatewayWebConfig implements WebMvcConfigurer {
    @Value("${spring.fep.gateway.web.http.request.aysnc.timeout:600000}")
    private long httpRequestAsyncTimeout;
    @Value("${spring.fep.gateway.web.get.ap.log.total.size.limit:10}")
    private int getApLogTotalSizeLimit; // 下載檔案的最大限制, 預設10GB

    public int getGetApLogTotalSizeLimit() {
        return getApLogTotalSizeLimit;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(httpRequestAsyncTimeout);
    }
}
