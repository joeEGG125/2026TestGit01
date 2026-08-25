package com.syscom.fep.frmcommon.jms;

import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class JmsConfigurationProperties extends MQConfigurationProperties {
    private String hostName;
    private long flushIntervalInMilliseconds = 5000;
    private String concurrency;
    private boolean sessionTransacted = true;
    private Integer sessionAcknowledgeMode;
    @NestedConfigurationProperty
    private final JmsConfigurationCacheProperties cache = new JmsConfigurationCacheProperties();
    private JmsConfigurationQosSettingsProperties qos;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public long getFlushIntervalInMilliseconds() {
        return flushIntervalInMilliseconds;
    }

    public void setFlushIntervalInMilliseconds(long flushIntervalInMilliseconds) {
        this.flushIntervalInMilliseconds = flushIntervalInMilliseconds;
    }

    public String getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(String concurrency) {
        this.concurrency = concurrency;
    }

    public boolean isSessionTransacted() {
        return sessionTransacted;
    }

    public void setSessionTransacted(boolean sessionTransacted) {
        this.sessionTransacted = sessionTransacted;
    }

    public Integer getSessionAcknowledgeMode() {
        return sessionAcknowledgeMode;
    }

    public void setSessionAcknowledgeMode(Integer sessionAcknowledgeMode) {
        this.sessionAcknowledgeMode = sessionAcknowledgeMode;
    }

    public String getHostNameFromConnName() {
        String connName = this.getConnName();
        if (StringUtils.isNoneBlank(connName)) {
            try {
                return connName.substring(0, connName.indexOf('('));
            } catch (Exception e) {}
        }
        return null;
    }

    public int getPortFromConnName() {
        String connName = this.getConnName();
        if (StringUtils.isNoneBlank(connName)) {
            try {
                return Integer.parseInt(connName.substring(connName.indexOf('(') + 1, connName.indexOf(')')));
            } catch (Exception e) {}
        }
        return -1;
    }

    public JmsConfigurationCacheProperties getCache() {
        return cache;
    }

    public JmsConfigurationQosSettingsProperties getQos() {
        return qos;
    }

    public void setQos(JmsConfigurationQosSettingsProperties qos) {
        this.qos = qos;
    }

    public String toString(String instanceName, String configurationPropertiesPrefix) {
        StringBuilder sb = new StringBuilder();
        List<Field> fieldList = ReflectUtil.getAllFields(this);
        if (CollectionUtils.isNotEmpty(fieldList)) {
            int repeat = 2;
            sb.append("JMS ").append(instanceName.toUpperCase()).append(" Configuration Properties:\r\n");
            for (Field field : fieldList) {
                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, this);
                if ("cache".equals(field.getName()) || "qos".equals(field.getName()) || "pool".equals(field.getName()) || "jndi".equals(field.getName())|| "jks".equals(field.getName())|| "trace".equals(field.getName())) {
                    this.toString(sb, StringUtils.join(Arrays.asList(configurationPropertiesPrefix, field.getName()), "."), value);
                } else if ("queueNames".equals(field.getName()) || "topicNames".equals(field.getName())) {
                } else {
                    sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                            .append(configurationPropertiesPrefix).append(".")
                            .append(field.getName())
                            .append(" = ");
                    if ("password".equals(field.getName())) {
                        if (value == null || StringUtils.isEmpty(value.toString()))
                            sb.append("null");
                        else
                            sb.append(StringUtils.repeat('*', value.toString().length()));
                    } else {
                        sb.append(value);
                    }
                    sb.append("\r\n");
                }
            }
        }
        return sb.toString();
    }

    private void toString(StringBuilder sb, String configurationPropertiesPrefix, Object value) {
        if (value == null)
            return;
        List<Field> fieldList = ReflectUtil.getAllFields(value);
        if (CollectionUtils.isNotEmpty(fieldList)) {
            int repeat = 2;
            for (Field field : fieldList) {
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
