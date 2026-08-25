package com.syscom.fep.frmcommon.jms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class JmsDefinition {
    /**
     * Please refer {@link JmsListener#id()}
     */
    private String identity;
    /**
     * Please refer {@link JmsListener#destination()}
     */
    private String destination;
    /**
     * Please refer {@link JmsListener#concurrency()}
     */
    private String concurrency;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
        if (StringUtils.isBlank(getIdentity())) {
            setIdentity(destination);
        }
    }

    public String getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(String concurrency) {
        this.concurrency = concurrency;
    }

    public void toString(StringBuilder sb, String configurationPropertiesPrefix) {
        Field[] fields = this.getClass().getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                        .append(configurationPropertiesPrefix).append(".")
                        .append(field.getName())
                        .append(" = ")
                        .append(ReflectionUtils.getField(field, this))
                        .append("\r\n");
            }
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}
