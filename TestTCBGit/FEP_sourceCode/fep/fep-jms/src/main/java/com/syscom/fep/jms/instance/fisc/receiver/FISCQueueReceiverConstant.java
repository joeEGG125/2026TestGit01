package com.syscom.fep.jms.instance.fisc.receiver;

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
public interface FISCQueueReceiverConstant {
    /**
     * Instance Name with lower case, please refer {@link JmsMsgInstanceName#FISC}
     */
    public static final String INSTANCE_NAME = "fisc.receiver";
    public static final String CONFIGURATION_PROPERTIES_PREFIX = "spring.fep.jms." + INSTANCE_NAME;
    public static final String CONFIGURATION_PROPERTIES_PREFIX_PROP = CONFIGURATION_PROPERTIES_PREFIX + ".prop";
    public static final String CONFIGURATION_PROPERTIES_QUEUEMANAGER = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".queueManager";
    public static final String CONFIGURATION_PROPERTIES_CHANNEL = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".channel";
    public static final String CONFIGURATION_PROPERTIES_CONNNAME = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".connName";
    public static final String CONFIGURATION_PROPERTIES_USER = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".user";
    public static final String CONFIGURATION_PROPERTIES_PASSWORD = CONFIGURATION_PROPERTIES_PREFIX_PROP + ".password";
    public static final String CONFIGURATION_PROPERTIES_SUFFIX_IDENTITY = ".identity";
    public static final String CONFIGURATION_PROPERTIES_SUFFIX_DESTINATION = ".destination";
    public static final String CONFIGURATION_PROPERTIES_SUFFIX_CONCURRENCY = ".concurrency";
    public static final String BEAN_NAME_PROPERTIES = INSTANCE_NAME + "JmsConfigurationProperties";
    public static final String MQ_CONNECTION_FACTORY = INSTANCE_NAME + "MQConnectionFactory";
    public static final String QUEUE_LISTENER_FACTORY = INSTANCE_NAME + "QueueListenerFactory";
    public static final String TOPIC_LISTENER_FACTORY = INSTANCE_NAME + "TopicListenerFactory";
    public static final String USER_CREDENTIALS_CONNECTION_FACTORY = INSTANCE_NAME + "UserCredentialsConnectionFactory";
    public static final String CACHING_CONNECTION_FACTORY = INSTANCE_NAME + "CachingConnectionFactory";
    public static final String MESSAGE_CONVERTER = INSTANCE_NAME + "MessageConverter";
    public static final String JMS_TEMPLATE = INSTANCE_NAME + "JmsTemplate";
    public static final String JMS_TRANSACTION_MANAGER = INSTANCE_NAME + "JmsTransactionManager";
    public static final String QUEUE_INSTANCE_NAME = "jmsMsgConfiguration.queueNames." + INSTANCE_NAME;

    default void printConstant() {
        Field[] fields = FISCQueueReceiverConstant.class.getDeclaredFields();
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
