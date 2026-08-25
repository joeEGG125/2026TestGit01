package com.syscom.fep.notify.common.config;

import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fep.notify.push001")
// @RefreshScope
@Component
@Getter
@Setter
public class Push001Config {
    private String classname;
    private String hostname;
    private String port;
    private String channel;
    private String managerId;
    private String accessQueueId;
    private String toMqUserID;

    @PostConstruct
    public void postConstruct() {
        com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Email001Config"));
    }
}
