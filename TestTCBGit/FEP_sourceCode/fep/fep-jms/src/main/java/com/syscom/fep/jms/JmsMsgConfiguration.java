package com.syscom.fep.jms;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsConfigurationProperties;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

/**
 * JMS配置類
 * <p>
 * 將所有的Queue名稱和Topic名稱定義在配置檔application-jms.properties中
 *
 * @author Richard
 */
@Component
@EnableJms
// @Validated
@ConfigurationProperties(prefix = JmsMsgConstant.CONFIGURATION_PROPERTIES_PREFIX)
@ConditionalOnProperty(value = {
        JmsMsgConstant.CONFIGURATION_PROPERTIES_QUEUEMANAGER,
        JmsMsgConstant.CONFIGURATION_PROPERTIES_CHANNEL,
        JmsMsgConstant.CONFIGURATION_PROPERTIES_CONNNAME,
        JmsMsgConstant.CONFIGURATION_PROPERTIES_USER,
        JmsMsgConstant.CONFIGURATION_PROPERTIES_PASSWORD})
// @RefreshScope
public class JmsMsgConfiguration implements JmsMsgConstant {
    @NestedConfigurationProperty
    private final JmsConfigurationProperties prop = new JmsConfigurationProperties();
    /**
     * 所有的Queue名稱定義在這個內部類中
     */
    @NestedConfigurationProperty
    private JmsQueueNames queueNames = new JmsQueueNames();
    /**
     * 所有的Topic名稱定義在這個內部類中
     */
    @NestedConfigurationProperty
    private JmsTopicNames topicNames = new JmsTopicNames();

    private static ConnectionFactory connectionFactory;

    public JmsConfigurationProperties getProp() {
        return prop;
    }

    public JmsQueueNames getQueueNames() {
        return queueNames;
    }

    public JmsTopicNames getTopicNames() {
        return topicNames;
    }

    /**
     * Serialize message content to json using TextMessage
     *
     * @return
     */
    @Bean(name = MESSAGE_CONVERTER)
    public MessageConverter messageConverter() {
        return JmsFactory.createMappingJackson2MessageConverter();
    }

    /**
     * Serialize message content to Simple Message for different type
     *
     * @return
     */
    @Bean(name = SIMPLE_MESSAGE_CONVERTER)
    public MessageConverter simpleMessageConverter() {
        return JmsFactory.createSimpleMessageConverter();
    }

    /**
     * Serialize message content to Simple Message for different type, but fromMessage convert to String only
     *
     * @return
     */
    @Bean(name = SIMPLE_STRING_IN_MESSAGE_CONVERTER)
    public MessageConverter simpleStringInMessageConverter() {
        return JmsFactory.createSimpleStringInMessageConverter();
    }

    @Bean(name = JMS_TEMPLATE)
    public JmsTemplate jmsTemplate(@Qualifier(MESSAGE_CONVERTER) MessageConverter messageConverter) throws JMSException {
        return JmsFactory.createJmsTemplate(this.connectionFactory(), messageConverter, prop.getQos());
    }

    @Bean(name = SIMPLE_JMS_TEMPLATE)
    public JmsTemplate simpleJmsTemplate(@Qualifier(SIMPLE_MESSAGE_CONVERTER) MessageConverter messageConverter) throws JMSException {
        return JmsFactory.createJmsTemplate(this.connectionFactory(), messageConverter, prop.getQos());
    }

    @Bean(name = SIMPLE_STRING_IN_JMS_TEMPLATE)
    public JmsTemplate simpleStringInJmsTemplate(@Qualifier(SIMPLE_STRING_IN_MESSAGE_CONVERTER) MessageConverter messageConverter) throws JMSException {
        return JmsFactory.createJmsTemplate(this.connectionFactory(), messageConverter, prop.getQos());
    }

    @Bean(name = JMS_TRANSACTION_MANAGER)
    public PlatformTransactionManager jmsTransactionManager() throws JMSException {
        return JmsFactory.createPlatformTransactionManager(this.connectionFactory());
    }

    /**
     * Queue的工廠連線類
     *
     * @return
     * @throws JMSException
     */
    @Bean(name = QUEUE_LISTENER_FACTORY)
    public JmsListenerContainerFactory<?> queueListenerFactory() throws JMSException {
        return JmsFactory.createQueueListenerFactory(this.prop, this.connectionFactory());
    }

    /**
     * Topic的工廠連線類
     *
     * @return
     * @throws JMSException
     */
    @Bean(name = TOPIC_LISTENER_FACTORY)
    public JmsListenerContainerFactory<?> topicListenerFactory() throws JMSException {
        return JmsFactory.createTopicListenerFactory(this.prop, this.connectionFactory());
    }

    private ConnectionFactory connectionFactory() throws JMSException {
        if (connectionFactory == null) {
            if (prop.getCache() != null && prop.getCache().isEnabled()) {
                connectionFactory = cachingJmsConnectionFactory();
            } else {
                connectionFactory = jmsConnectionFactory();
            }
        }
        return connectionFactory;
    }

    private ConnectionFactory jmsConnectionFactory() throws JMSException {
        LogHelperFactory.getTraceLogger().trace("Creating ", INSTANCE_NAME.toUpperCase(), " single IBM MQ ConnectionFactory start...");
        try {
            ConnectionFactory connectionFactory = JmsFactory.createMQConnectionFactory(prop);
            return JmsFactory.createUserCredentialsConnectionFactoryAdapter(connectionFactory, prop);
        } finally {
            LogHelperFactory.getTraceLogger().trace("Creating ", INSTANCE_NAME.toUpperCase(), " single IBM MQ ConnectionFactory finish");
        }
    }

    private CachingConnectionFactory cachingJmsConnectionFactory() throws JMSException {
        LogHelperFactory.getTraceLogger().trace("Creating ", INSTANCE_NAME.toUpperCase(), " caching IBM MQ ConnectionFactory start...");
        try {
            ConnectionFactory connectionFactory = JmsFactory.createMQConnectionFactory(prop);
            UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = JmsFactory.createUserCredentialsConnectionFactoryAdapter(connectionFactory, prop);
            return JmsFactory.createCachingConnectionFactory(userCredentialsConnectionFactoryAdapter, prop);
        } finally {
            LogHelperFactory.getTraceLogger().trace("Creating ", INSTANCE_NAME.toUpperCase(), " caching IBM MQ ConnectionFactory finish");
        }
    }

    @PostConstruct
    public void print() {
        printConstant();
        LogHelperFactory.getGeneralLogger().info(prop.toString(INSTANCE_NAME, CONFIGURATION_PROPERTIES_PREFIX_PROP));
        StringBuilder sb = new StringBuilder();
        sb.append("JMS Msg Configuration:\r\n");
        if (queueNames != null)
            queueNames.toString(sb, StringUtils.join(CONFIGURATION_PROPERTIES_PREFIX, ".queue-names"));
        if (topicNames != null)
            topicNames.toString(sb, StringUtils.join(CONFIGURATION_PROPERTIES_PREFIX, ".topic-names"));
        LogHelperFactory.getGeneralLogger().info(sb.toString());
    }

    @PreDestroy
    public void destroy() {
        if (connectionFactory != null) {
            JmsFactory.closeConnection(INSTANCE_NAME.toUpperCase(), connectionFactory);
        }
    }
}
