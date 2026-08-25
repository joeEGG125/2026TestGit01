package com.syscom.fep.base.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * 系統監控配置
 */
@ConfigurationProperties(prefix = "spring.fep.monitor")
// @RefreshScope
public class FEPMonitorConfig {
    private Map<String, String> serviceNameToAliasMap;

    public void setServiceNameToAliasMap(Map<String, String> serviceNameToAliasMap) {
        this.serviceNameToAliasMap = serviceNameToAliasMap;
    }

    public String getAliasByServiceName(String serviceName) {
        if (MapUtils.isNotEmpty(this.serviceNameToAliasMap)) {
            String alias = this.serviceNameToAliasMap.get(serviceName);
            if (StringUtils.isNotBlank(alias)) {
                return alias;
            }
        }
        return serviceName;
    }

    @PostConstruct
    public void print() {
        Field[] fields = FEPMonitorConfig.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            // 列印配置檔內容
            StringBuilder sb = new StringBuilder();
            sb.append("FEP Monitor Configuration:\r\n");
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                Value annotation = field.getAnnotation(Value.class);
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat));
                if (annotation != null) {
                    sb.append(annotation.value().substring(annotation.value().indexOf("${") + 2, annotation.value().contains(":") ? annotation.value().indexOf(":") : annotation.value().length() - 1));
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
