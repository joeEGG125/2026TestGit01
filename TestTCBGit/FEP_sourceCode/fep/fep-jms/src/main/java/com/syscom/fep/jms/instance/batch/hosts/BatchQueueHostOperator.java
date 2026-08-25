package com.syscom.fep.jms.instance.batch.hosts;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.*;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import java.util.List;
import java.util.function.Predicate;

public class BatchQueueHostOperator extends JmsPayloadOperator {
    private static final LogHelper logger = LogHelperFactory.getJmsLogger();
    private JmsListenerEndpointRegistry registry;
    private JmsTemplate jmsTemplate;
    private MessageConverter messageConverter;

    public BatchQueueHostOperator(JmsListenerEndpointRegistry registry, JmsTemplate jmsTemplate, MessageConverter messageConverter) {
        this.registry = registry;
        this.jmsTemplate = jmsTemplate;
        this.messageConverter = messageConverter;
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
     * @param payload
     * @param property
     * @param handler
     * @throws Exception
     */
    @Override
    public void sendQueue(JmsPayload<?> payload, JmsProperty property, JmsHandler handler) throws Exception {
        this.logOut(logger, payload);
        try {
            JmsFactory.sendQueue(jmsTemplate, payload.getDestination(), payload, property, handler);
        } catch (Exception e) {
            String error = StringUtils.join("send queue failed, payload = [", payload, "]");
            logger.exceptionMsg(e, error);
            throw ExceptionUtil.createException(e, error);
        }
    }

    /**
     * 送出Queue訊息並接收
     *
     * @param payload
     * @param property
     * @param handler
     * @throws Exception
     */
    @Override
    public JmsPayload<?> sendAndReceiveQueue(JmsPayload<?> payload, JmsProperty property, JmsHandler handler) throws Exception {
        this.logOut(logger, payload);
        try {
            JmsPayload<?> messageIn = JmsFactory.sendAndReceiveQueue(jmsTemplate, messageConverter, payload.getDestination(), payload, property, handler);
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
    public JmsPayload<?> receiveQueue(String destination, JmsProperty property, JmsHandler handler) throws Exception {
        try {
            JmsPayload<?> payload = JmsFactory.receiveQueue(jmsTemplate, messageConverter, destination, property, handler);
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
    public List<JmsPayload<?>> browseQueue(String destination, Predicate<JmsPayload<?>> predicate, JmsProperty property, JmsHandler handler) throws Exception {
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
     * @param payload
     * @param property
     * @param handler
     * @throws Exception
     */
    @Override
    public void publishTopic(JmsPayload<?> payload, JmsProperty property, JmsHandler handler) throws Exception {
        this.logOut(logger, payload);
        try {
            JmsFactory.publishTopic(jmsTemplate, payload.getDestination(), payload, property, handler);
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
    public JmsPayload<?> receiveTopic(String destination, JmsProperty property, JmsHandler handler) throws Exception {
        try {
            JmsPayload<?> payload = JmsFactory.receiveTopic(jmsTemplate, messageConverter, destination, property, handler);
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
