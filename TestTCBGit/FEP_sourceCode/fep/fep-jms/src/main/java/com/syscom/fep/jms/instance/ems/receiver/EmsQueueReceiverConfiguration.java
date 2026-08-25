package com.syscom.fep.jms.instance.ems.receiver;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsDefinition;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.instance.ems.EmsQueueConfigurationProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
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
import jakarta.jms.Session;

@Component
@EnableJms
// @Validated
@ConfigurationProperties(prefix = EmsQueueReceiverConstant.CONFIGURATION_PROPERTIES_PREFIX)
@ConditionalOnProperty(value = {
        EmsQueueReceiverConstant.CONFIGURATION_PROPERTIES_QUEUEMANAGER,
        EmsQueueReceiverConstant.CONFIGURATION_PROPERTIES_CHANNEL,
        EmsQueueReceiverConstant.CONFIGURATION_PROPERTIES_CONNNAME,
        EmsQueueReceiverConstant.CONFIGURATION_PROPERTIES_USER,
        EmsQueueReceiverConstant.CONFIGURATION_PROPERTIES_PASSWORD})
@Lazy
// @RefreshScope
public class EmsQueueReceiverConfiguration implements EmsQueueReceiverConstant {
    @NestedConfigurationProperty
    private final EmsQueueConfigurationProperties prop = new EmsQueueConfigurationProperties();

    private static ConnectionFactory connectionFactory;

    @PostConstruct
    public void postConstruct() {
        // 預設不要事務, 由Client手動回應ack
        this.prop.setSessionTransacted(false);
        this.prop.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
    }

    public EmsQueueConfigurationProperties getProp() {
        return prop;
    }

    @Bean(name = MESSAGE_CONVERTER)
    public MessageConverter messageConverter() {
        return JmsFactory.createSimpleMessageConverter();
    }

    @Bean(name = JMS_TEMPLATE)
    public JmsTemplate jmsTemplate(@Qualifier(MESSAGE_CONVERTER) MessageConverter messageConverter) throws JMSException {
        return JmsFactory.createJmsTemplate(this.connectionFactory(), messageConverter, prop.getQos());
    }

    @Bean(name = JMS_TRANSACTION_MANAGER)
    public PlatformTransactionManager jmsTransactionManager() throws JMSException {
        return JmsFactory.createPlatformTransactionManager(this.connectionFactory());
    }

    @Bean(name = QUEUE_LISTENER_FACTORY)
    public JmsListenerContainerFactory<?> queueListenerFactory() throws JMSException {
        return JmsFactory.createQueueListenerFactory(this.prop, this.connectionFactory());
    }

    /**
     * 取出EMS Queue Name, 如果spring.fep.jms.ems.receiver.prop.queue-names.ems沒有設定, 則取spring.fep.jms.queue-names.ems
     *
     * @return
     */
    public JmsDefinition getEmsQueueName() {
        if (this.prop.getQueueNames() != null && this.prop.getQueueNames().getEms() != null && StringUtils.isNotBlank(this.prop.getQueueNames().getEms().getDestination())) {
            return this.prop.getQueueNames().getEms();
        }
        JmsMsgConfiguration jmsMsgConfiguration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
        return jmsMsgConfiguration.getQueueNames().getEms();
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
    }

    @PreDestroy
    public void destroy() {
        if (connectionFactory != null) {
            JmsFactory.closeConnection(INSTANCE_NAME.toUpperCase(), connectionFactory);
        }
    }
}
