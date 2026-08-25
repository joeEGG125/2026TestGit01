package com.syscom.fep.jms.instance.batch.hosts;

import com.syscom.fep.frmcommon.jms.JmsConfigurationProperties;
import com.syscom.fep.jms.JmsQueueNames;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class BatchQueueHostConfigurationProperties extends JmsConfigurationProperties {
    /**
     * 所有的Queue名稱定義在這個內部類中
     */
    private JmsQueueNames queueNames;

    public void setQueueNames(JmsQueueNames queueNames) {
        this.queueNames = queueNames;
    }

    public JmsQueueNames getQueueNames() {
        return queueNames;
    }

    @Override
    public String toString(String instanceName, String configurationPropertiesPrefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString(instanceName, configurationPropertiesPrefix));
        if (queueNames != null)
            queueNames.toString(sb, StringUtils.join(configurationPropertiesPrefix, ".queue-names"));
        return sb.toString();
    }

    private void toString(StringBuilder sb, String configurationPropertiesPrefix, Object value) {
        if (value == null)
            return;
        Field[] fields = value.getClass().getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                Object v = ReflectionUtils.getField(field, value);
                if (v != null) {
                    sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                            .append(configurationPropertiesPrefix).append(".")
                            .append(field.getName())
                            .append(" = ")
                            .append(v)
                            .append("\r\n");
                }
            }
        }
    }
}
