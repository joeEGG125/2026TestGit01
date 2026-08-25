package com.syscom.fep.notify.common.config;

import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fep.notify.email001")
// @RefreshScope
@Component
@Getter
@Setter
public class Email001Config {
    private String smtp;
    private int port;
    private String account;
    private String sscode;
    private String from;
    private String classname;
    private boolean sslOnConnect = false;
    private boolean useConfigSmtp = true;
    private boolean useConfigSmtpPort = true;

    @PostConstruct
    public void postConstruct() {
        com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Email001Config"));
    }
}
