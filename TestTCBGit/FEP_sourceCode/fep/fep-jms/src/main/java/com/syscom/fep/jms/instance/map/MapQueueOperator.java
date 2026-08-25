package com.syscom.fep.jms.instance.map;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.jms.JmsHandler;
import com.syscom.fep.frmcommon.jms.JmsOperator;
import com.syscom.fep.frmcommon.jms.JmsProperty;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.jms.JMSException;
import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

public class MapQueueOperator<Key, ConfigurationProperties extends MapQueueConfigurationProperties<Key>> implements JmsOperator<Serializable, Serializable> {
    private static final LogHelper logger = LogHelperFactory.getJmsLogger();
    private final JmsListenerEndpointRegistry registry;
    private final ConfigurationProperties configurationProperties;
    private final JmsTemplate jmsTemplate;
    private final MessageConverter messageConverter;
    private final PlatformTransactionManager transactionManager;

    public MapQueueOperator(JmsListenerEndpointRegistry registry, ConfigurationProperties configurationProperties, JmsTemplate jmsTemplate, MessageConverter messageConverter, PlatformTransactionManager transactionManager) {
        this.registry = registry;
        this.configurationProperties = configurationProperties;
        this.jmsTemplate = jmsTemplate;
        this.messageConverter = messageConverter;
        this.transactionManager = transactionManager;
    }

    /**
     * 獲取配置項
     *
     * @return
     */
    public ConfigurationProperties getConfigurationProperties() {
        return configurationProperties;
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
     * 獲取PlatformTransactionManager實例
     *
     * @return
     */
    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * 獲取MessageConverter實例
     *
     * @return
     */
    public MessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * 送出Queue訊息
     *
     * @param destination
     * @param payload
     * @param property
     * @param handler
     * @throws Exception
     */
    @Override
    public void sendQueue(String destination, Serializable payload, JmsProperty property, JmsHandler handler) throws Exception {
        this.logOut(logger, payload);
        try {
            JmsFactory.sendQueue(jmsTemplate, destination, payload, property, handler);
        } catch (Exception e) {
            String error = StringUtils.join("send queue failed, payload = [", payload, "]");
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
     * @throws Exception
     */
    @Override
    public Serializable sendAndReceiveQueue(String destination, Serializable payload, JmsProperty property, JmsHandler handler) throws Exception {
        this.logOut(logger, payload);
        try {
            Serializable messageIn = JmsFactory.sendAndReceiveQueue(jmsTemplate, messageConverter, destination, payload, property, handler);
            this.logIn(logger, messageIn);
            return messageIn;
        } catch (Exception e) {
            String error = StringUtils.join("send and receive queue failed, payload = [", payload, "]");
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
            this.logIn(logger, payload);
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
        this.logOut(logger, payload);
        try {
            JmsFactory.publishTopic(jmsTemplate, destination, payload, property, handler);
        } catch (Exception e) {
            String error = StringUtils.join("publish topic failed, payload = [", payload, "]");
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
            this.logIn(logger, payload);
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
