package com.syscom.fep.jms.instance.ems.receiver;

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

import java.io.Serializable;

/**
 * 監聽EmsQueue訊息
 *
 * @author Richard
 */
public class EmsQueueReceiverConsumers extends JmsNotifier<Serializable, JmsReceiver<Serializable>> implements EmsQueueReceiverConstant {
    @Qualifier(MESSAGE_CONVERTER)
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private EmsQueueReceiverConfiguration emsQueueReceiverConfiguration;

    @Override
    @JmsListener(
            id = "#{@" + QUEUE_INSTANCE_NAME + CONFIGURATION_PROPERTIES_SUFFIX_IDENTITY + "}",
            destination = "#{@" + QUEUE_INSTANCE_NAME + CONFIGURATION_PROPERTIES_SUFFIX_DESTINATION + "}",
            concurrency = "#{@" + QUEUE_INSTANCE_NAME + CONFIGURATION_PROPERTIES_SUFFIX_CONCURRENCY + "}",
            containerFactory = QUEUE_LISTENER_FACTORY)
    public void onMessage(Message message) {
        this.acknowledge(message); // 收到訊息後手動回應ack, 避免處理時間過長, 導致MQ Server發生rollback
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

    @Override
    protected long getFlushIntervalInMilliseconds() {
        return emsQueueReceiverConfiguration.getProp().getFlushIntervalInMilliseconds();
    }
}
