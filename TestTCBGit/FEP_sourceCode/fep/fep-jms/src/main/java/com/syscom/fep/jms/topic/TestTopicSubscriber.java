package com.syscom.fep.jms.topic;

import com.syscom.fep.frmcommon.jms.JmsNotifier;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.jms.JmsMsgConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;

/**
 * 監聽TestTopic訊息
 *
 * @author Richard
 */
public class TestTopicSubscriber extends JmsNotifier<PlainTextMessage, JmsReceiver<PlainTextMessage>> implements JmsMsgConstant {
    @Qualifier(MESSAGE_CONVERTER)
    @Autowired
    private MessageConverter messageConverter;

    @Override
    @JmsListener(
            id = "#{@" + TOPIC_NAME_TEST + CONFIGURATION_PROPERTIES_SUFFIX_IDENTITY + "}",
            destination = "#{@" + TOPIC_NAME_TEST + CONFIGURATION_PROPERTIES_SUFFIX_DESTINATION + "}",
            concurrency = "#{@" + TOPIC_NAME_TEST + CONFIGURATION_PROPERTIES_SUFFIX_CONCURRENCY + "}",
            containerFactory = TOPIC_LISTENER_FACTORY)
    public void onMessage(Message message) {
        super.messageReceived(message);
    }

    /**
     * @param object  the object to convert
     * @param session the Session to use for creating a JMS Message
     * @return
     * @throws JMSException
     * @throws MessageConversionException
     */
    @Override
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        return messageConverter.toMessage(object, session);
    }

    /**
     * @param message the message to convert
     * @return
     * @throws JMSException
     * @throws MessageConversionException
     */
    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        return messageConverter.fromMessage(message);
    }
}