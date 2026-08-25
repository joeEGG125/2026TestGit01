package com.syscom.fep.gateway.agent.atm;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.gateway.agent.atm.job.ATMGatewayServerAgentProcessCommandJobConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.List;

// @Validated
@ConfigurationProperties(prefix = "spring.fep.gateway.agent.atm")
// @RefreshScope
public class ATMGatewayServerAgentConfiguration {
    @NotNull
    private String cmdStart;
    private List<ATMGatewayServerAgentProcessCommandJobConfig> cmdJobConfig;
    private boolean printInputStream = false;
    private boolean recordHttpLog = false;
    private String httpChangeFEPAP;
    private String httpCheckFEPAP;

    public String getCmdStart() {
        return cmdStart;
    }

    public void setCmdStart(String cmdStart) {
        this.cmdStart = cmdStart;
    }

    public List<ATMGatewayServerAgentProcessCommandJobConfig> getCmdJobConfig() {
        return cmdJobConfig;
    }

    public void setCmdJobConfig(List<ATMGatewayServerAgentProcessCommandJobConfig> cmdJobConfig) {
        this.cmdJobConfig = cmdJobConfig;
    }

    public boolean isPrintInputStream() {
        return printInputStream;
    }

    public void setPrintInputStream(boolean printInputStream) {
        this.printInputStream = printInputStream;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }

    public String getHttpChangeFEPAP() {
        return httpChangeFEPAP;
    }

    public void setHttpChangeFEPAP(String httpChangeFEPAP) {
        this.httpChangeFEPAP = httpChangeFEPAP;
    }

    public String getHttpCheckFEPAP() {
        return httpCheckFEPAP;
    }

    public void setHttpCheckFEPAP(String httpCheckFEPAP) {
        this.httpCheckFEPAP = httpCheckFEPAP;
    }

    @PostConstruct
    public void print() {
        Field[] fields = ATMGatewayServerAgentConfiguration.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            ConfigurationProperties annotation2 = this.getClass().getAnnotation(ConfigurationProperties.class);
            String prefix = annotation2.prefix();
            int repeat = 2;
            // 列印配置檔內容
            StringBuilder sb = new StringBuilder();
            sb.append("ATMGateway Agent Configuration:\r\n");
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                Object object = ReflectionUtils.getField(field, this);
                String prefix2 = prefix == null ? StringUtils.EMPTY : StringUtils.join(prefix, ".");
                if (object instanceof List) {
                    prefix2 = StringUtils.join(prefix2, field.getName());
                    List<?> list = (List<?>) object;
                    int i = 0;
                    for (Object e : list) {
                        print(e, StringUtils.join(prefix2, "[", i++, "]"), sb);
                    }
                } else {
                    sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                            .append(prefix2)
                            .append(field.getName())
                            .append(" = ").append(object).append("\r\n");
                }
            }
            LogHelperFactory.getGeneralLogger().info(sb.toString());
        }
    }

    private void print(Object object, String prefix, StringBuilder sb) {
        int repeat = 2;
        if ("java.lang".equals(object.getClass().getPackage().getName())) {
            sb.append(StringUtils.repeat(StringUtils.SPACE, 2))
                    .append(prefix)
                    .append(" = ")
                    .append(object)
                    .append("\r\n");
        } else {
            List<Field> fields = ReflectUtil.getAllFields(object);
            if (CollectionUtils.isNotEmpty(fields)) {
                for (Field field : fields) {
                    ReflectionUtils.makeAccessible(field);
                    sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                            .append(prefix).append(".")
                            .append(field.getName()).append(" = ")
                            .append(ReflectionUtils.getField(field, object))
                            .append("\r\n");
                }
            }
        }
    }
}
