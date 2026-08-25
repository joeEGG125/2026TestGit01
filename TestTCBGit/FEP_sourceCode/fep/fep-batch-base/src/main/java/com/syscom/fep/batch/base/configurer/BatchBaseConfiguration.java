package com.syscom.fep.batch.base.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = BatchBaseConstant.CONFIGURATION_PROPERTIES_PREFIX)
@ConditionalOnProperty(prefix = BatchBaseConstant.CONFIGURATION_PROPERTIES_PREFIX, name = {BatchBaseConstant.CONFIGURATION_PROPERTIES_HOST_0_NAME})
@Lazy
// @RefreshScope
public class BatchBaseConfiguration {
    @NestedConfigurationProperty
    private final List<BatchBaseConfigurationHost> host = new ArrayList<>();
    @NestedConfigurationProperty
    private final BatchBaseConfigurationTask task = new BatchBaseConfigurationTask();
    /**
     * 是否給批次服務平台送Queue訊息
     */
    private boolean callBatchJob = true;

    public List<BatchBaseConfigurationHost> getHost() {
        return host;
    }

    public BatchBaseConfigurationTask getTask() {
        return task;
    }

    public boolean isCallBatchJob() {
        return callBatchJob;
    }

    public void setCallBatchJob(boolean callBatchJob) {
        this.callBatchJob = callBatchJob;
    }

    @PostConstruct
    public void print() {
        StringBuilder sb = new StringBuilder();
        sb.append("Batch Base Configuration:\r\n");
        for (int i = 0; i < host.size(); i++) {
            this.toString(sb, StringUtils.join("spring.fep.batch.base.host[", i, "]"), host.get(i));
        }
        this.toString(sb, "spring.fep.batch.base.task", task);
        LogHelperFactory.getGeneralLogger().info(sb.toString());
    }

    private void toString(StringBuilder sb, String configurationPropertiesPrefix, Object value) {
        if (value == null)
            return;
        Field[] fields = value.getClass().getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                        .append(configurationPropertiesPrefix).append(".")
                        .append(field.getName())
                        .append(" = ")
                        .append(ReflectionUtils.getField(field, value))
                        .append("\r\n");
            }
        }
    }
}
