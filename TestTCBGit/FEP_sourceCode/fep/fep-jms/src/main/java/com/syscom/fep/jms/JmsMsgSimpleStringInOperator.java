package com.syscom.fep.jms;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.jms.JmsHandler;
import com.syscom.fep.frmcommon.jms.JmsOperator;
import com.syscom.fep.frmcommon.jms.JmsProperty;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

/**
 * JMS訊息發送, 注意發的訊息物件是Serializable, 收的訊息物件是String
 *
 * @author Richard
 */
@Component
@ConditionalOnProperty(value = {
        JmsMsgConstant.CONFIGURATION_PROPERTIES_QUEUEMANAGER,
        JmsMsgConstant.CONFIGURATION_PROPERTIES_CHANNEL,
        JmsMsgConstant.CONFIGURATION_PROPERTIES_CONNNAME,
        JmsMsgConstant.CONFIGURATION_PROPERTIES_USER,
        JmsMsgConstant.CONFIGURATION_PROPERTIES_PASSWORD})
@Lazy
public class JmsMsgSimpleStringInOperator implements JmsOperator<Serializable, String>, JmsMsgConstant {
    private static final LogHelper logger = LogHelperFactory.getJmsLogger();
    @Autowired
    private JmsListenerEndpointRegistry registry;
    @Qualifier(SIMPLE_STRING_IN_JMS_TEMPLATE)
    @Autowired
    private JmsTemplate jmsTemplate;
    @Qualifier(SIMPLE_STRING_IN_MESSAGE_CONVERTER)
    @Autowired
    private MessageConverter messageConverter;

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
     * @param property
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
    public String sendAndReceiveQueue(String destination, Serializable payload, JmsProperty property, JmsHandler handler) throws Exception {
        this.logOut(logger, destination, payload);
        try {
            String messageIn = JmsFactory.sendAndReceiveQueue(jmsTemplate, messageConverter, destination, payload, property, handler);
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
    public String receiveQueue(String destination, JmsProperty property, JmsHandler handler) throws Exception {
        try {
            String payload = JmsFactory.receiveQueue(jmsTemplate, messageConverter, destination, property, handler);
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
    public List<String> browseQueue(String destination, Predicate<Serializable> predicate, JmsProperty property, JmsHandler handler) throws Exception {
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
    public String receiveTopic(String destination, JmsProperty property, JmsHandler handler) throws Exception {
        try {
            String payload = JmsFactory.receiveTopic(jmsTemplate, messageConverter, destination, property, handler);
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
