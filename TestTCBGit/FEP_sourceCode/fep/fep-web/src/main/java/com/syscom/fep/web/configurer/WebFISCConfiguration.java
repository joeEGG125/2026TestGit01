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

@ConfigurationProperties(prefix = "spring.fep.web.fisc")
// @RefreshScope
public class WebFISCConfiguration {
    @NestedConfigurationProperty
    private static final WebFISCCtrlUri ctrlUri = new WebFISCCtrlUri();
    private long waitInMillisecondsAfterStartAndStop = 10000L;
    @NestedConfigurationProperty
    private final List<WebFISCAgent> agent = new ArrayList<>();

    public WebFISCCtrlUri getCtrlUri() {
        return ctrlUri;
    }

    public long getWaitInMillisecondsAfterStartAndStop() {
        return waitInMillisecondsAfterStartAndStop;
    }

    public void setWaitInMillisecondsAfterStartAndStop(long waitInMillisecondsAfterStartAndStop) {
        this.waitInMillisecondsAfterStartAndStop = waitInMillisecondsAfterStartAndStop;
    }

    public List<WebFISCAgent> getAgent() {
        return agent;
    }

    public static class WebFISCAgent {
        /**
         * FISCGW的名字, 注意不能重複
         */
        private String name;
        private String ip;
        private String port;
        private String contextPath;
        private String primaryNameSuffix = StringUtils.EMPTY;
        private String secondaryNameSuffix = StringUtils.EMPTY;
        private WebFISCCtrlUri uri;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getPrimaryNameSuffix() {
            return primaryNameSuffix;
        }

        public void setPrimaryNameSuffix(String primaryNameSuffix) {
            this.primaryNameSuffix = primaryNameSuffix;
        }

        public String getSecondaryNameSuffix() {
            return secondaryNameSuffix;
        }

        public void setSecondaryNameSuffix(String secondaryNameSuffix) {
            this.secondaryNameSuffix = secondaryNameSuffix;
        }

        public WebFISCCtrlUri getUri() {
            if (uri == null) {
                uri = new WebFISCCtrlUri();
            }
            if (StringUtils.isBlank(uri.getStart())) {
                uri.setStart(FormatUtil.messageFormat(ctrlUri.getStart(), ip, StringUtils.join(port, contextPath)));
            }
            if (StringUtils.isBlank(uri.getStop())) {
                uri.setStop(FormatUtil.messageFormat(ctrlUri.getStop(), ip, StringUtils.join(port, contextPath)));
            }
            if (StringUtils.isBlank(uri.getChannel())) {
                uri.setChannel(FormatUtil.messageFormat(ctrlUri.getChannel(), ip, StringUtils.join(port, contextPath)));
            }
            if (StringUtils.isBlank(uri.getCheck())) {
                uri.setCheck(FormatUtil.messageFormat(ctrlUri.getCheck(), ip, StringUtils.join(port, contextPath)));
            }
            return uri;
        }

        public void setUri(WebFISCCtrlUri uri) {
            this.uri = uri;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class WebFISCCtrlUri {
        private String start;
        private String stop;
        private String channel;
        private String check;

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getStop() {
            return stop;
        }

        public void setStop(String stop) {
            this.stop = stop;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getCheck() {
            return check;
        }

        public void setCheck(String check) {
            this.check = check;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Web FISC Gateway Configuration"));
    }
}
