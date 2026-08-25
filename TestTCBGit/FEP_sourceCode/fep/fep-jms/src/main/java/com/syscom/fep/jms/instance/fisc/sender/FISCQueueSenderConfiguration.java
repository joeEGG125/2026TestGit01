package com.syscom.fep.jms.instance.fisc.sender;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsConfigurationProperties;
import com.syscom.fep.frmcommon.jms.JmsFactory;
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

@Component
@EnableJms
// @Validated
@ConfigurationProperties(prefix = FISCQueueSenderConstant.CONFIGURATION_PROPERTIES_PREFIX)
@ConditionalOnProperty(value = {
        FISCQueueSenderConstant.CONFIGURATION_PROPERTIES_QUEUEMANAGER,
        FISCQueueSenderConstant.CONFIGURATION_PROPERTIES_CHANNEL,
        FISCQueueSenderConstant.CONFIGURATION_PROPERTIES_CONNNAME,
        FISCQueueSenderConstant.CONFIGURATION_PROPERTIES_USER,
        FISCQueueSenderConstant.CONFIGURATION_PROPERTIES_PASSWORD})
@Lazy
// @RefreshScope
public class FISCQueueSenderConfiguration implements FISCQueueSenderConstant {
    @NestedConfigurationProperty
    private final JmsConfigurationProperties prop = new JmsConfigurationProperties();

    private static ConnectionFactory connectionFactory;

    public JmsConfigurationProperties getProp() {
        return prop;
    }

    @Bean(name = MESSAGE_CONVERTER)
    public MessageConverter messageConverter() {
        return JmsFactory.createMappingJackson2MessageConverter();
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
