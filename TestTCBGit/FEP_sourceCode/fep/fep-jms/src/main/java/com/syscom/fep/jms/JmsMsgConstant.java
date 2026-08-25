package com.syscom.fep.jms;

import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * 定義常量
 *
 * @author Richard
 */
public interface JmsMsgConstant {
    /**
     * Instance Name with lower case, please refer {@link JmsMsgInstanceName#FEP}
     */
    public static final String INSTANCE_NAME = "fep";
    public static final String CONFIGURATION_PROPERTIES_PREFIX = "spring." + INSTANCE_NAME + ".jms";
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
    public static final String POOLED_CONNECTION_FACTORY = INSTANCE_NAME + "PooledJmsConnectionFactory";
    public static final String MESSAGE_CONVERTER = INSTANCE_NAME + "MessageConverter";
    public static final String SIMPLE_MESSAGE_CONVERTER = INSTANCE_NAME + "SimpleMessageConverter";
    public static final String SIMPLE_STRING_IN_MESSAGE_CONVERTER = INSTANCE_NAME + "SimpleStringInMessageConverter";
    public static final String JMS_TEMPLATE = INSTANCE_NAME + "JmsTemplate";
    public static final String SIMPLE_JMS_TEMPLATE = INSTANCE_NAME + "SimpleJmsTemplate";
    public static final String SIMPLE_STRING_IN_JMS_TEMPLATE = INSTANCE_NAME + "SimpleStringInJmsTemplate";
    public static final String JMS_TRANSACTION_MANAGER = INSTANCE_NAME + "JmsTransactionManager";
    public static final String QUEUE_NAME_ATMMON = "jmsMsgConfiguration.queueNames.atmMon";
    public static final String QUEUE_NAME_DEAD = "jmsMsgConfiguration.queueNames.dead";
    public static final String QUEUE_NAME_PYBATCH = "jmsMsgConfiguration.queueNames.pyBatch";
    public static final String QUEUE_NAME_PYBATCHACK = "jmsMsgConfiguration.queueNames.pyBatchAck";
    public static final String QUEUE_NAME_NB = "jmsMsgConfiguration.queueNames.nb";
    public static final String QUEUE_NAME_MB = "jmsMsgConfiguration.queueNames.mb";
    public static final String QUEUE_NAME_MCH = "jmsMsgConfiguration.queueNames.mch";
    public static final String QUEUE_NAME_VO = "jmsMsgConfiguration.queueNames.vo";
    public static final String QUEUE_NAME_HCE = "jmsMsgConfiguration.queueNames.hce";
    public static final String QUEUE_NAME_EIP = "jmsMsgConfiguration.queueNames.eip";
    public static final String QUEUE_NAME_VIP = "jmsMsgConfiguration.queueNames.vip";
    public static final String QUEUE_NAME_NONVIP = "jmsMsgConfiguration.queueNames.nonvip";
    public static final String QUEUE_NAME_EOI = "jmsMsgConfiguration.queueNames.eoi";
    public static final String QUEUE_NAME_EATM = "jmsMsgConfiguration.queueNames.eatm";
    public static final String QUEUE_NAME_TEST = "jmsMsgConfiguration.queueNames.test";
    public static final String TOPIC_NAME_TEST = "jmsMsgConfiguration.topicNames.test";
    public static final String QUEUE_NAME_MFT = "jmsMsgConfiguration.queueNames.mft";
    public static final String QUEUE_NAME_TWMP = "jmsMsgConfiguration.queueNames.twmp";
    public static final String QUEUE_NAME_CBSPEND = "jmsMsgConfiguration.queueNames.cbspend";
    public static final String QUEUE_NAME_SSO = "jmsMsgConfiguration.queueNames.sso";
    public static final String QUEUE_NAME_ONL = "jmsMsgConfiguration.queueNames.onl";
    public static final String QUEUE_NAME_FIDO = "jmsMsgConfiguration.queueNames.fido";
    public static final String QUEUE_NAME_DIGITAL = "jmsMsgConfiguration.queueNames.digital";
    public static final String QUEUE_NAME_BIZLOAN = "jmsMsgConfiguration.queueNames.bizloan";

    default void printConstant() {
        Field[] fields = JmsMsgConstant.class.getDeclaredFields();
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
