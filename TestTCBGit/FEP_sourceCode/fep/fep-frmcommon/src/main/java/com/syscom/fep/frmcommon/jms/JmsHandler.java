package com.syscom.fep.frmcommon.jms;

import com.ibm.mq.jakarta.jms.MQQueue;
import com.ibm.mq.jakarta.jms.MQTopic;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

public interface JmsHandler {

    default void setPropertyOut(Message message) throws JMSException {}

    default void getPropertyIn(Message message) throws JMSException {}

    default void setPropertyOut(MQQueue destination) throws JMSException {}

    default void setPropertyOut(MQTopic destination) throws JMSException {}

}
