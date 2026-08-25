package com.syscom.fep.jms.instance.map.vo;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.jms.JmsMsgInstanceName;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * 定義常量
 *
 * @author Richard
 */
public interface VOQueueConstant {
    /**
     * Instance Name with lower case, please refer {@link JmsMsgInstanceName#VO}
     */
    String INSTANCE_NAME = "vo";
    String CONFIGURATION_PROPERTIES_PREFIX = "spring.fep.jms." + INSTANCE_NAME;
    String CONFIGURATION_PROPERTIES_PREFIX_PROP = CONFIGURATION_PROPERTIES_PREFIX + ".prop[0]";
    String CONFIGURATION_PROPERTIES_HOST_NAME = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".host-name";
    String CONFIGURATION_PROPERTIES_QUEUE_MANAGER = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".queueManager";
    String CONFIGURATION_PROPERTIES_CHANNEL = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".channel";
    String CONFIGURATION_PROPERTIES_CONNECTION_NAME = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".connName";
    String CONFIGURATION_PROPERTIES_USER = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".user";
    String CONFIGURATION_PROPERTIES_PASSWORD = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".password";
    String BEAN_NAME_PROPERTIES = INSTANCE_NAME + "JmsConfigurationProperties";
    String MQ_CONNECTION_FACTORY = INSTANCE_NAME + "MQConnectionFactory";
    String QUEUE_LISTENER_FACTORY = INSTANCE_NAME + "QueueListenerFactory";
    String TOPIC_LISTENER_FACTORY = INSTANCE_NAME + "TopicListenerFactory";
    String USER_CREDENTIALS_CONNECTION_FACTORY = INSTANCE_NAME + "UserCredentialsConnectionFactory";
    String CACHING_CONNECTION_FACTORY = INSTANCE_NAME + "CachingConnectionFactory";
    String MESSAGE_CONVERTER = INSTANCE_NAME + "MessageConverter";
    String JMS_TEMPLATE = INSTANCE_NAME + "JmsTemplate";
    String JMS_TRANSACTION_MANAGER = INSTANCE_NAME + "JmsTransactionManager";
    String JMS_PROPERTIES_MAP = INSTANCE_NAME + "JmsPropertiesMap";
    String JMS_OPERATOR_MAP = INSTANCE_NAME + "JmsOperatorMap";

    default void printConstant() {
        Field[] fields = VOQueueConstant.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            StringBuilder sb = new StringBuilder();
            sb.append("JMS ").append(INSTANCE_NAME.toUpperCase()).append(" Constant:\r\n");
            for (Field field : fields) {
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                        .append(field.getName())
                        .append(" = ").append(ReflectionUtils.getField(field, this)).append("\r\n");
            }
            LogHelperFactory.getGeneralLogger().info(sb.toString());
        }
    }
}
