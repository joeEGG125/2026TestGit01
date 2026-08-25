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

@ConfigurationProperties(prefix = "spring.fep.web.queue")
// @RefreshScope
public class WebQueueConfiguration {
    @NestedConfigurationProperty
    private static final WebQueueCtrlUri ctrlUri = new WebQueueCtrlUri();
    @NestedConfigurationProperty
    private final List<WebQueueReceiver> receiver = new ArrayList<>();

    public WebQueueCtrlUri getCtrlUri() {
        return ctrlUri;
    }

    public List<WebQueueReceiver> getReceiver() {
        return receiver;
    }

    public static class WebQueueReceiver {
        private String name;
        private String programName;
        private String ip;
        private String port;
        private String contextPath;
        private WebQueueCtrlUri uri;

        public String getName() {
            if (StringUtils.isBlank(name))
                name = programName;
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

        public WebQueueCtrlUri getUri() {
            if (uri == null) {
                uri = new WebQueueCtrlUri();
            }
            if (StringUtils.isBlank(uri.getGetConcurrency())) {
                uri.setGetConcurrency(FormatUtil.messageFormat(ctrlUri.getGetConcurrency(), ip, StringUtils.join(port, contextPath), getProgramName()));
            }
            if (StringUtils.isBlank(uri.getSetConcurrency())) {
                uri.setSetConcurrency(FormatUtil.messageFormat(ctrlUri.getSetConcurrency(), ip, StringUtils.join(port, contextPath), getProgramName()));
            }
            return uri;
        }

        public void setUri(WebQueueCtrlUri uri) {
            this.uri = uri;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class WebQueueCtrlUri {
        private String getConcurrency;
        private String setConcurrency;

        public String getGetConcurrency() {
            return getConcurrency;
        }

        public void setGetConcurrency(String getConcurrency) {
            this.getConcurrency = getConcurrency;
        }

        public String getSetConcurrency() {
            return setConcurrency;
        }

        public void setSetConcurrency(String setConcurrency) {
            this.setConcurrency = setConcurrency;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Web Queue Configuration"));
    }
}
