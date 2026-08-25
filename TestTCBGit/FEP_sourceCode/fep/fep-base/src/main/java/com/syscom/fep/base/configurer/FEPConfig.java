package com.syscom.fep.base.configurer;

import com.syscom.fep.base.enums.FISCEncoding;
import com.syscom.fep.base.enums.Protocol;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;

@Configuration
// @RefreshScope
public class FEPConfig {
    @Value("${spring.fep.systemid:FEP10}")
    private String systemId;
    @Value("${spring.fep.hostip:}")
    private String hostIp;
    @Value("${spring.fep.hostname:}")
    private String hostName;
    @Value("${spring.fep.t24.method.comparetota.enable:false}")
    private boolean t24MethodCompareTOTAEnable;
    @Value("${spring.fep.server.url.atm:http://localhost:8100/recv/atm}")
    private String atmURL;
    @Value("${spring.fep.server.url.fisc:http://localhost:8101/recv/fisc}")
    private String fiscURL;
    @Value("${spring.fep.service.url.inqueue11x1:http://localhost:8201/inqueue/11X1}")
    private String inQueue11X1URL;
    @Value("${spring.fep.restful.timeout:30000}")
    private int restfulTimeout;
    @Value("${spring.fep.server.protocol.atm:socket}")
    private Protocol atmProtocol;
    @Value("${spring.fep.server.protocol.fisc:socket}")
    private Protocol fiscProtocol;
    @Value("${spring.fep.server.checkAtmMac:true}")
    private boolean checkAtmMac;
    @Value("${spring.fep.server.encoding.fisc:ascii}")
    private FISCEncoding fiscencoding;
    @Value("${spring.fep.batch.task.cmdStart:test}")
    private String taskCmdStart;
    @Value("${spring.fep.batch.task.checkExLog:false}")
    private Boolean checkExLog;
    /**
     * SpringSecurity跨域設定
     */
    @Value("#{'${spring.fep.security.cors.allowed.origins:*}'.split(',')}")
    private List<String> securityCorsAllowedOrigins;

    public static FEPConfig getInstance() {
        return SpringBeanFactoryUtil.getBean(FEPConfig.class);
    }

    public String getSystemId() {
        return systemId;
    }

    public String getHostIp() {
        return hostIp;
    }

    public String getHostName() {
        return hostName;
    }

    public boolean isT24MethodCompareTOTAEnable() {
        return t24MethodCompareTOTAEnable;
    }

    public boolean isCheckAtmMac() {
        return checkAtmMac;
    }

    public String getAtmURL() {
        return atmURL;
    }

    public String getFiscURL() {
        return fiscURL;
    }

    public String getInQueue11X1URL() {
        return inQueue11X1URL;
    }

    public int getRestfulTimeout() {
        return restfulTimeout;
    }

    public Protocol getAtmProtocol() {
        return atmProtocol;
    }

    public Protocol getFiscProtocol() {
        return fiscProtocol;
    }

    public String getApplicationName() {
        return EnvPropertiesUtil.getProperty("management.metrics.tags.application", StringUtils.EMPTY);
    }

    public FISCEncoding getFiscencoding() {
        return fiscencoding;
    }

    public String getTaskCmdStart() {
        return taskCmdStart;
    }

    public List<String> getSecurityCorsAllowedOrigins() {
        return securityCorsAllowedOrigins;
    }

    public Boolean getCheckExLog() {return checkExLog;}


    @PostConstruct
    public void print() {
        Field[] fields = FEPConfig.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            // 列印配置檔內容
            StringBuilder sb = new StringBuilder();
            sb.append("FEP Configuration:\r\n");
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                Value annotation = field.getAnnotation(Value.class);
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat));
                if (annotation != null) {
                    sb.append(annotation.value().substring(annotation.value().indexOf("${") + 2,
                            annotation.value().contains(":") ? annotation.value().indexOf(":")
                                    : annotation.value().length() - 1));
                } else {
                    ConfigurationProperties annotation2 = this.getClass().getAnnotation(ConfigurationProperties.class);
                    if (annotation2 != null) {
                        String prefix = annotation2.prefix();
                        if (StringUtils.isNotBlank(prefix)) {
                            sb.append(prefix).append(".");
                        }
                    }
                    sb.append(field.getName());
                }
                sb.append(" = ").append(ReflectionUtils.getField(field, this)).append("\r\n");
            }
            LogHelperFactory.getGeneralLogger().info(sb.toString());
        }
    }
}