package com.syscom.fep.jms;

import com.syscom.fep.frmcommon.jms.JmsDefinition;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * 所有的Topic名稱定義在這個類中
 *
 * @author Richard
 */
public class JmsTopicNames {
    /**
     * TestTopic
     */
    @Lazy
    private JmsDefinition test;

    public JmsDefinition getTest() {
        return test;
    }

    public void setTest(JmsDefinition test) {
        this.test = test;
    }

    public void toString(StringBuilder sb, String configurationPropertiesPrefix) {
        Field[] fields = this.getClass().getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                JmsDefinition jmsDefinition = (JmsDefinition) ReflectionUtils.getField(field, this);
                if (jmsDefinition != null) {
                    jmsDefinition.toString(sb, StringUtils.join(configurationPropertiesPrefix, ".", field.getName()));
                }
            }
        }
    }

    public static JmsDefinition getDefinition(JmsTopicNames topicNames, Function<JmsTopicNames, JmsDefinition> function) {
        if (topicNames != null && function != null) {
            return function.apply(topicNames);
        }
        return null;
    }

    public static String getTopicName(JmsTopicNames topicNames, Function<JmsTopicNames, JmsDefinition> function) {
        JmsDefinition definition = getDefinition(topicNames, function);
        return definition == null ? null : definition.getDestination();
    }
}
