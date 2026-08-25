package com.syscom.fep.jms.instance.batch.hosts;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsConfigurationProperties;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@EnableJms
// @Validated
@ConfigurationProperties(prefix = BatchQueueHostConstant.CONFIGURATION_PROPERTIES_PREFIX)
@ConditionalOnProperty(value = {
        BatchQueueHostConstant.CONFIGURATION_PROPERTIES_HOST_NAME,
        BatchQueueHostConstant.CONFIGURATION_PROPERTIES_QUEUEMANAGER,
        BatchQueueHostConstant.CONFIGURATION_PROPERTIES_CHANNEL,
        BatchQueueHostConstant.CONFIGURATION_PROPERTIES_CONNNAME,
        BatchQueueHostConstant.CONFIGURATION_PROPERTIES_USER,
        BatchQueueHostConstant.CONFIGURATION_PROPERTIES_PASSWORD})
@Lazy
// @RefreshScope
public class BatchQueueHostConfiguration implements BatchQueueHostConstant {
    @Autowired
    private JmsListenerEndpointRegistry registry;
    private List<BatchQueueHostConfigurationProperties> prop;
    private final List<ConnectionFactory> connectionFactories = new ArrayList<>();

    public void setProp(List<BatchQueueHostConfigurationProperties> prop) {
        this.prop = prop;
    }

    @Bean(name = MESSAGE_CONVERTER)
    public MessageConverter messageConverter() {
        return JmsFactory.createMappingJackson2MessageConverter();
    }

    @Bean(name = JMS_PROPERTIES_MAP)
    public Map<String, BatchQueueHostConfigurationProperties> batchQueueHostProperiesMap() {
        Map<String, BatchQueueHostConfigurationProperties> map = new HashMap<>();
        for (BatchQueueHostConfigurationProperties properties : prop) {
            map.put(properties.getHostName(), properties);
        }
        return map;
    }

    @Bean(name = JMS_OPERATOR_MAP)
    public Map<String, BatchQueueHostOperator> batchQueueHostOperatorMap(@Qualifier(MESSAGE_CONVERTER) MessageConverter messageConverter) throws JMSException {
        Map<String, BatchQueueHostOperator> map = new HashMap<>();
        for (JmsConfigurationProperties properties : prop) {
            ConnectionFactory connectionFactory = this.connectionFactory(properties);
            JmsTemplate jmsTemplate = JmsFactory.createJmsTemplate(connectionFactory, messageConverter, properties.getQos());
            map.put(properties.getHostName(), new BatchQueueHostOperator(registry, jmsTemplate, messageConverter));
        }
        return map;
    }

    private ConnectionFactory connectionFactory(JmsConfigurationProperties prop) throws JMSException {
        ConnectionFactory connectionFactory;
        if (prop.getCache() != null && prop.getCache().isEnabled()) {
            connectionFactory = cachingJmsConnectionFactory(prop);
        } else {
            connectionFactory = jmsConnectionFactory(prop);
        }
        this.connectionFactories.add(connectionFactory);
        return connectionFactory;
    }

    private ConnectionFactory jmsConnectionFactory(JmsConfigurationProperties prop) throws JMSException {
        LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, INSTANCE_NAME.toUpperCase(), " single IBM MQ ConnectionFactory start...");
        try {
            ConnectionFactory connectionFactory = JmsFactory.createMQConnectionFactory(prop);
            return JmsFactory.createUserCredentialsConnectionFactoryAdapter(connectionFactory, prop);
        } finally {
            LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, INSTANCE_NAME.toUpperCase(), " single IBM MQ ConnectionFactory finish");
        }
    }

    private CachingConnectionFactory cachingJmsConnectionFactory(JmsConfigurationProperties prop) throws JMSException {
        LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, INSTANCE_NAME.toUpperCase(), " caching IBM MQ ConnectionFactory start...");
        try {
            ConnectionFactory connectionFactory = JmsFactory.createMQConnectionFactory(prop);
            UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = JmsFactory.createUserCredentialsConnectionFactoryAdapter(connectionFactory, prop);
            return JmsFactory.createCachingConnectionFactory(userCredentialsConnectionFactoryAdapter, prop);
        } finally {
            LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, INSTANCE_NAME.toUpperCase(), " caching IBM MQ ConnectionFactory finish");
        }
    }

    @PostConstruct
    public void print() {
        printConstant();
        int index = 0;
        for (JmsConfigurationProperties properties : prop) {
            LogHelperFactory.getGeneralLogger().info(properties.toString(INSTANCE_NAME + "[" + index + "]", CONFIGURATION_PROPERTIES_PREFIX + ".prop[" + index++ + "]"));
        }
    }

    @PreDestroy
    public void destroy() {
        if (!connectionFactories.isEmpty()) {
            int index = 0;
            for (ConnectionFactory connectionFactory : connectionFactories)
                JmsFactory.closeConnection(INSTANCE_NAME.toUpperCase() + "[" + index++ + "]", connectionFactory);
        }
    }
}
