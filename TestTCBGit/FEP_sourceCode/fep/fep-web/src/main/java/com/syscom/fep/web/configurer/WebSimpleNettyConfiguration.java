package com.syscom.fep.web.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import jakarta.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "spring.fep.web.simple.netty")
// @RefreshScope
public class WebSimpleNettyConfiguration {
    @NestedConfigurationProperty
    private static final WebSimpleNettyCtrlUri ctrlUri = new WebSimpleNettyCtrlUri();
    @NestedConfigurationProperty
    private final List<WebSimpleNettyServer> server = new ArrayList<>();

    public WebSimpleNettyCtrlUri getCtrlUri() {
        return ctrlUri;
    }

    public List<WebSimpleNettyServer> getServer() {
        return server;
    }

    public static class WebSimpleNettyServer {
        private String name;
        private String programName;
        private String ip;
        private String port;
        private String contextPath;
        private WebSimpleNettyCtrlUri uri;

        public String getName() {
            if (StringUtils.isBlank(name))
                name = StringUtils.replace(programName, "NettyServer", StringUtils.EMPTY);
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProgramName() {
            return programName;
        }

        public void setProgramName(String programName) {
            this.programName = programName;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getContextPath() {
            return contextPath;
        }

        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        public WebSimpleNettyCtrlUri getUri() {
            if (uri == null) {
                uri = new WebSimpleNettyCtrlUri();
            }
            String programIdentity = StringUtils.replace(programName, "NettyServer", StringUtils.EMPTY);
            if (StringUtils.isBlank(uri.getGetEventExecutorData())) {
                uri.setGetEventExecutorData(FormatUtil.messageFormat(ctrlUri.getGetEventExecutorData(), ip, StringUtils.join(port, contextPath), programIdentity));
            }
            if (StringUtils.isBlank(uri.getSetEventExecutorThreads())) {
                uri.setSetEventExecutorThreads(FormatUtil.messageFormat(ctrlUri.getSetEventExecutorThreads(), ip, StringUtils.join(port, contextPath), programIdentity));
            }
            return uri;
        }
    }

    public static class WebSimpleNettyCtrlUri {
        private String getEventExecutorData;
        private String setEventExecutorThreads;

        public String getGetEventExecutorData() {
            return getEventExecutorData;
        }

        public void setGetEventExecutorData(String getEventExecutorData) {
            this.getEventExecutorData = getEventExecutorData;
        }

        public String getSetEventExecutorThreads() {
            return setEventExecutorThreads;
        }

        public void setSetEventExecutorThreads(String setEventExecutorThreads) {
            this.setEventExecutorThreads = setEventExecutorThreads;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Web Simple Netty Configuration"));
    }
}
