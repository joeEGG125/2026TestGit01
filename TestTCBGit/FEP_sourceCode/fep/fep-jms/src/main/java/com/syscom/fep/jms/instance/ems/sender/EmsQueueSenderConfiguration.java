package com.syscom.fep.jms.instance.ems.sender;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsConfigurationProperties;
import com.syscom.fep.frmcommon.jms.JmsConfigurationQosSettingsProperties;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.instance.ems.EmsQueueConfigurationProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@EnableJms
// @Validated
@ConfigurationProperties(prefix = EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_PREFIX)
@ConditionalOnProperty(value = {
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_QUEUEMANAGER,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_CHANNEL,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_CONNNAME,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_USER,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_PASSWORD})
@Lazy
// @RefreshScope
public class EmsQueueSenderConfiguration implements EmsQueueSenderConstant {
    private List<EmsQueueConfigurationProperties> log;
    private List<EmsQueueConfigurationProperties> alert;
    private final List<ConnectionFactory> logConnectionFactories = new ArrayList<>();
    private final List<ConnectionFactory> alertConnectionFactories = new ArrayList<>();
    private DeliveryMode deliveryMode = DeliveryMode.Static; // 傳送模式

    public void setLog(List<EmsQueueConfigurationProperties> log) {
        this.log = log;
    }

    public List<EmsQueueConfigurationProperties> getLog() {
        return log;
    }

    public List<EmsQueueConfigurationProperties> getAlert() {
        return alert;
    }

    public void setAlert(List<EmsQueueConfigurationProperties> alert) {
        this.alert = alert;
    }

    public DeliveryMode getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(DeliveryMode deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    RoundRobin<EmsQueueSenderOperator> emsQueueSenderLogOperatorList() throws JMSException {
        RoundRobin<EmsQueueSenderOperator> list = this.emsQueueSenderOperatorList(this.log, this.logConnectionFactories);
        // 2026-02-24 Richard modified persistent設定放入properties檔中
        // 2025-09-15 Richard add 根據客戶的要求, 這裡特別設定DeliveryMode為NON_PERSISTENT
        // for (int i = 0; i < list.size(); i++) {
        //     JmsTemplate jmsTemplate = list.get(i).getJmsTemplate();
        //     jmsTemplate.setExplicitQosEnabled(true); // 注意這個設定為true, 下面這個設定才起作用
        //     jmsTemplate.setDeliveryMode(jakarta.jms.DeliveryMode.NON_PERSISTENT);
        // }
        return list;
    }

    RoundRobin<EmsQueueSenderOperator> emsQueueSenderAlertOperatorList() throws JMSException {
        return this.emsQueueSenderOperatorList(this.alert, this.alertConnectionFactories);
    }

    private RoundRobin<EmsQueueSenderOperator> emsQueueSenderOperatorList(List<EmsQueueConfigurationProperties> prop, List<ConnectionFactory> connectionFactories) throws JMSException {
        JmsListenerEndpointRegistry registry = SpringBeanFactoryUtil.getBean(JmsListenerEndpointRegistry.class, false);
        if (registry == null)
            registry = new JmsListenerEndpointRegistry();
        MessageConverter messageConverter = JmsFactory.createSimpleMessageConverter();
        RoundRobin<EmsQueueSenderOperator> roundRobin = new RoundRobin<>();
        for (EmsQueueConfigurationProperties properties : prop) {
            ConnectionFactory connectionFactory = this.connectionFactory(connectionFactories, properties);
            JmsTemplate jmsTemplate = JmsFactory.createJmsTemplate(connectionFactory, messageConverter, properties.getQos());
            roundRobin.add(new EmsQueueSenderOperator(properties, registry, jmsTemplate, messageConverter));
        }
        return roundRobin;
    }

    private ConnectionFactory connectionFactory(List<ConnectionFactory> connectionFactories, JmsConfigurationProperties prop) throws JMSException {
        ConnectionFactory connectionFactory;
        if (prop.getCache() != null && prop.getCache().isEnabled()) {
            connectionFactory = cachingJmsConnectionFactory(prop);
        } else {
            connectionFactory = jmsConnectionFactory(prop);
        }
        connectionFactories.add(connectionFactory);
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
        for (JmsConfigurationProperties properties : log) {
            // 如果沒有設定Qos, 則根據客戶的要求, 這裡預設DeliveryMode為NON_PERSISTENT
            if (properties.getQos() == null) {
                properties.setQos(new JmsConfigurationQosSettingsProperties());
                properties.getQos().setDeliveryMode(jakarta.jms.DeliveryMode.NON_PERSISTENT);
            }
            LogHelperFactory.getGeneralLogger().info(properties.toString(INSTANCE_NAME + ".log[" + index + "]", CONFIGURATION_PROPERTIES_PREFIX + ".log[" + index++ + "]"));
        }
        index = 0;
        for (JmsConfigurationProperties properties : alert) {
            LogHelperFactory.getGeneralLogger().info(properties.toString(INSTANCE_NAME + ".alert[" + index + "]", CONFIGURATION_PROPERTIES_PREFIX + ".alert[" + index++ + "]"));
        }
    }

    @PreDestroy
    public void destroy() {
        synchronized (logConnectionFactories) {
            if (!logConnectionFactories.isEmpty()) {
                int index = 0;
                for (ConnectionFactory connectionFactory : logConnectionFactories)
                    JmsFactory.closeConnection(INSTANCE_NAME.toUpperCase() + "log[" + index++ + "]", connectionFactory);
                logConnectionFactories.clear();
            }
        }
        synchronized (alertConnectionFactories) {
            if (!alertConnectionFactories.isEmpty()) {
                int index = 0;
                for (ConnectionFactory connectionFactory : alertConnectionFactories)
                    JmsFactory.closeConnection(INSTANCE_NAME.toUpperCase() + "alert[" + index++ + "]", connectionFactory);
                alertConnectionFactories.clear();
            }
        }
    }

    public enum DeliveryMode {
        Static, RoundRobin;
    }
}
