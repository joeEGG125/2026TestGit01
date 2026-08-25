package com.syscom.fep.jms.instance.ems.sender;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsDefinition;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.jms.JmsHandler;
import com.syscom.fep.frmcommon.jms.JmsProperty;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.JmsQueueNames;
import com.syscom.fep.jms.instance.ems.EmsQueueConfigurationProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class EmsQueueSenderOperator extends JmsMsgSimpleOperator {
    private static final LogHelper logger = LogHelperFactory.getJmsLogger();
    private final JmsListenerEndpointRegistry registry;
    private final JmsTemplate jmsTemplate;
    private final MessageConverter messageConverter;
    private final EmsQueueConfigurationProperties properties;

    public EmsQueueSenderOperator(EmsQueueConfigurationProperties properties, JmsListenerEndpointRegistry registry, JmsTemplate jmsTemplate, MessageConverter messageConverter) {
        this.registry = registry;
        this.jmsTemplate = jmsTemplate;
        this.messageConverter = messageConverter;
        this.properties = properties;
    }

    public EmsQueueConfigurationProperties getProperties() {
        return properties;
    }

    public String getEmsQueueName() {
        Function<JmsQueueNames, JmsDefinition> function = JmsQueueNames::getEms;
        if (properties != null && properties.getQueueNames() != null) {
            String queueName = JmsQueueNames.getQueueName(properties.getQueueNames(), function);
            if (StringUtils.isNotBlank(queueName)) {
                return queueName;
            }
        }
        JmsMsgConfiguration jmsMsgConfiguration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
        return JmsQueueNames.getQueueName(jmsMsgConfiguration.getQueueNames(), function);
    }

    /**
     * 獲取JmsTemplate實例
     *
     * @return
     */
    @Override
    public JmsTemplate getJmsTemplate() {
        return this.jmsTemplate;
    }

    /**
     * 送出Queue訊息
     *
     * @param destination
     * @param payload
     * @param handler
     * @throws Exception
     */
    @Override
    public void sendQueue(String destination, Serializable payload, JmsProperty property, JmsHandler handler) throws Exception {
        this.logOut(logger, destination, payload);
        try {
            JmsFactory.sendQueue(jmsTemplate, destination, payload, property, handler);
        } catch (Exception e) {
            String error = StringUtils.join("send queue failed, destination = [", destination, "], payload = [", payload, "]");
            logger.exceptionMsg(e, error);
            throw ExceptionUtil.createException(e, error);
        }
    }

    /**
     * 送出Queue訊息並接收
     *
     * @param destination
     * @param payload
     * @param property
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public Serializable sendAndReceiveQueue(String destination, Serializable payload, JmsProperty property, JmsHandler handler) throws Exception {
        this.logOut(logger, destination, payload);
        try {
            Serializable messageIn = JmsFactory.sendAndReceiveQueue(jmsTemplate, messageConverter, destination, payload, property, handler);
            this.logIn(logger, destination, messageIn);
            return messageIn;
        } catch (Exception e) {
            String error = StringUtils.join("send and receive queue failed, destination = [", destination, "], payload = [", payload, "]");
            logger.exceptionMsg(e, error);
            throw ExceptionUtil.createException(e, error);
        }
    }

    /**
     * 接收Queue訊息
     *
     * @param destination
     * @param property
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public Serializable receiveQueue(String destination, JmsProperty property, JmsHandler handler) throws Exception {
        try {
            Serializable payload = JmsFactory.receiveQueue(jmsTemplate, messageConverter, destination, property, handler);
            this.logIn(logger, destination, payload);
            return payload;
        } catch (Exception e) {
            String error = StringUtils.join("receive queue failed, destination = [", destination, "]");
            logger.exceptionMsg(e, error);
            throw ExceptionUtil.createException(e, error);
        }
    }

    /**
     * 瀏覽Queue消息
     *
     * @param destination
     * @param predicate
     * @param property
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public List<Serializable> browseQueue(String destination, Predicate<Serializable> predicate, JmsProperty property, JmsHandler handler) throws Exception {
        try {
            return JmsFactory.browseQueue(jmsTemplate, messageConverter, destination, predicate, property, handler);
        } catch (Exception e) {
            String error = StringUtils.join("browse queue failed, destination = [", destination, "]");
            logger.exceptionMsg(e, error);
            throw ExceptionUtil.createException(e, error);
        }
    }

    /**
     * 送出Topic訊息
     *
     * @param destination
     * @param payload
     * @param property
     * @param handler
     * @throws Exception
     */
    @Override
    public void publishTopic(String destination, Serializable payload, JmsProperty property, JmsHandler handler) throws Exception {
        this.logOut(logger, destination, payload);
        try {
            JmsFactory.publishTopic(jmsTemplate, destination, payload, property, handler);
        } catch (Exception e) {
            String error = StringUtils.join("publish topic failed, destination = [", destination, "], payload = [", payload, "]");
            logger.exceptionMsg(e, error);
            throw ExceptionUtil.createException(e, error);
        }
    }

    /**
     * 接收Topic訊息
     *
     * @param destination
     * @param property
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public Serializable receiveTopic(String destination, JmsProperty property, JmsHandler handler) throws Exception {
        try {
            Serializable payload = JmsFactory.receiveTopic(jmsTemplate, messageConverter, destination, property, handler);
            this.logIn(logger, destination, payload);
            return payload;
        } catch (Exception e) {
            String error = StringUtils.join("receive topic failed, destination = [", destination, "]");
            logger.exceptionMsg(e, error);
            throw ExceptionUtil.createException(e, error);
        }
    }

    /**
     * 開始Queue/Topic接收
     *
     * @param destination
     */
    @Override
    public void startReceive(String destination) {
        if (JmsFactory.startReceive(registry, destination)) {
            logger.info("[startReceive]destination = [", destination, "]");
        }
    }

    /**
     * 停止Queue/Topic接收
     *
     * @param destination
     */
    @Override
    public void stopReceive(String destination) {
        if (JmsFactory.stopReceive(registry, destination)) {
            logger.info("[stopReceive]destination = [", destination, "]");
        }
    }
}
