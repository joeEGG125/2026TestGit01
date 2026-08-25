package com.syscom.fep.notify.common.config;

import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fep.notify.sms001")
// @RefreshScope
@Component
@Getter
@Setter
public class Sms001Config {
    private String classname;
    private String domain;
    private String username;
    private String sscode;
    private boolean useConfigDomain = true;

    @PostConstruct
    public void postConstruct() {
        com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Sms001Config"));
    }
}
