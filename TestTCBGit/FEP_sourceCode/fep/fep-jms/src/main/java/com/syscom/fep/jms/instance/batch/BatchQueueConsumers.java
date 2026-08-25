package com.syscom.fep.jms.instance.batch;

import com.syscom.fep.frmcommon.jms.JmsNotifier;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;

/**
 * 監聽EmsQueue訊息
 *
 * @author Richard
 */
public class BatchQueueConsumers extends JmsNotifier<PlainTextMessage, JmsReceiver<PlainTextMessage>> implements BatchQueueConstant {
    @Qualifier(MESSAGE_CONVERTER)
    @Autowired
    private MessageConverter messageConverter;

    @Override
    @JmsListener(
            id = "#{@" + QUEUE_INSTANCE_NAME + CONFIGURATION_PROPERTIES_SUFFIX_IDENTITY + "}",
            destination = "#{@" + QUEUE_INSTANCE_NAME + CONFIGURATION_PROPERTIES_SUFFIX_DESTINATION + "}",
            concurrency = "#{@" + QUEUE_INSTANCE_NAME + CONFIGURATION_PROPERTIES_SUFFIX_CONCURRENCY + "}",
            containerFactory = QUEUE_LISTENER_FACTORY)
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
