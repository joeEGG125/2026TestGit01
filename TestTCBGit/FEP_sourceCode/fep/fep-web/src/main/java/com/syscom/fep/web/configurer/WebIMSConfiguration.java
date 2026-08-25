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

@ConfigurationProperties(prefix = "spring.fep.web.ims")
// @RefreshScope
public class WebIMSConfiguration {
    @NestedConfigurationProperty
    private static final WebIMSCtrlUri ctrlUri = new WebIMSCtrlUri();
    private long waitInMillisecondsAfterChangeLineStatus = 1000L;
    @NestedConfigurationProperty
    private final List<WebIMSGateway> gw = new ArrayList<>();

    public WebIMSCtrlUri getCtrlUri() {
        return ctrlUri;
    }

    public long getWaitInMillisecondsAfterChangeLineStatus() {
        return waitInMillisecondsAfterChangeLineStatus;
    }

    public void setWaitInMillisecondsAfterChangeLineStatus(long waitInMillisecondsAfterChangeLineStatus) {
        this.waitInMillisecondsAfterChangeLineStatus = waitInMillisecondsAfterChangeLineStatus;
    }

    public List<WebIMSGateway> getGw() {
        return gw;
    }

    public static class WebIMSGateway {
        private String hostName;
        private String ip;
        private String port;
        private String contextPath;
        private WebIMSCtrlUri uri;

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

        public WebIMSCtrlUri getUri() {
            if (uri == null) {
                uri = new WebIMSCtrlUri();
            }
            if (StringUtils.isBlank(uri.getGetAllLineStatus())) {
                uri.setGetAllLineStatus(FormatUtil.messageFormat(ctrlUri.getGetAllLineStatus(), ip, StringUtils.join(port, contextPath)));
            }
            if (StringUtils.isBlank(uri.getChangeLineStatus())) {
                uri.setChangeLineStatus(FormatUtil.messageFormat(ctrlUri.getChangeLineStatus(), ip, StringUtils.join(port, contextPath)));
            }
            return uri;
        }

        public void setUri(WebIMSCtrlUri uri) {
            this.uri = uri;
        }
    }

    public static class WebIMSCtrlUri {
        private String getAllLineStatus;
        private String changeLineStatus;

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
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Web IMS Gateway Configuration"));
    }
}
