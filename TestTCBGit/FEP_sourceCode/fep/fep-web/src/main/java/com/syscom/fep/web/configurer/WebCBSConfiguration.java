package com.syscom.fep.web.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "spring.fep.web.cbs")
// @RefreshScope
public class WebCBSConfiguration {
    @NestedConfigurationProperty
    private static final WebCBSCtrlUri ctrlUri = new WebCBSCtrlUri();
    private long waitInMillisecondsAfterChangeLineStatus = 1000L;
    private boolean isPrimaryReadOnly = true, isAlternativeReadOnly = false;
    @NestedConfigurationProperty
    private final List<WebCBSGateway> gw = new ArrayList<>();

    public WebCBSCtrlUri getCtrlUri() {
        return ctrlUri;
    }

    public long getWaitInMillisecondsAfterChangeLineStatus() {
        return waitInMillisecondsAfterChangeLineStatus;
    }

    public void setWaitInMillisecondsAfterChangeLineStatus(long waitInMillisecondsAfterChangeLineStatus) {
        this.waitInMillisecondsAfterChangeLineStatus = waitInMillisecondsAfterChangeLineStatus;
    }

    public boolean isPrimaryReadOnly() {
        return isPrimaryReadOnly;
    }

    public void setPrimaryReadOnly(boolean primaryReadOnly) {
        isPrimaryReadOnly = primaryReadOnly;
    }

    public boolean isAlternativeReadOnly() {
        return isAlternativeReadOnly;
    }

    public void setAlternativeReadOnly(boolean alternativeReadOnly) {
        isAlternativeReadOnly = alternativeReadOnly;
    }

    public List<WebCBSGateway> getGw() {
        return gw;
    }

    public static class WebCBSGateway {
        private String hostName;
        private String ip;
        private String port;
        private String contextPath;
        private WebCBSCtrlUri uri;

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
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

        public WebCBSCtrlUri getUri() {
            if (uri == null) {
                uri = new WebCBSCtrlUri();
            }
            if (StringUtils.isBlank(uri.getGetAllLineStatus())) {
                uri.setGetAllLineStatus(FormatUtil.messageFormat(ctrlUri.getGetAllLineStatus(), ip, StringUtils.join(port, contextPath)));
            }
            if (StringUtils.isBlank(uri.getChangeLineStatus())) {
                uri.setChangeLineStatus(FormatUtil.messageFormat(ctrlUri.getChangeLineStatus(), ip, StringUtils.join(port, contextPath)));
            }
            if (StringUtils.isBlank(uri.getChangeServerStatus())) {
                uri.setChangeServerStatus(FormatUtil.messageFormat(ctrlUri.getChangeServerStatus(), ip, StringUtils.join(port, contextPath)));
            }
            return uri;
        }

        public void setUri(WebCBSCtrlUri uri) {
            this.uri = uri;
        }
    }

    public static class WebCBSCtrlUri {
        private String getAllLineStatus;
        private String changeLineStatus;
        private String changeServerStatus;

        public String getGetAllLineStatus() {
            return getAllLineStatus;
        }

        public void setGetAllLineStatus(String getAllLineStatus) {
            this.getAllLineStatus = getAllLineStatus;
        }

        public String getChangeLineStatus() {
            return changeLineStatus;
        }

        public void setChangeLineStatus(String changeLineStatus) {
            this.changeLineStatus = changeLineStatus;
        }

        public String getChangeServerStatus() {
            return changeServerStatus;
        }

        public void setChangeServerStatus(String changeServerStatus) {
            this.changeServerStatus = changeServerStatus;
        }
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Web CBS Gateway Configuration"));
    }
}
