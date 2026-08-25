package com.syscom.fep.frmcommon.jms;

/**
 * JMS訊息發送
 *
 * @author Richard
 */
public abstract class JmsPayloadOperator implements JmsOperator<JmsPayload<?>, JmsPayload<?>> {

    /**
     * 送出Queue訊息
     *
     * @param payload
     * @param property
     * @param handler
     * @throws Exception
     */
    public abstract void sendQueue(JmsPayload<?> payload, JmsProperty property, JmsHandler handler) throws Exception;

    /**
     * 送出Queue訊息並接收
     *
     * @param payload
     * @param property
     * @param handler
     * @return
     * @throws Exception
     */
    public abstract JmsPayload<?> sendAndReceiveQueue(JmsPayload<?> payload, JmsProperty property, JmsHandler handler) throws Exception;

    /**
     * 送出Topic訊息
     *
     * @param payload
     * @param property
     * @param handler
     * @throws Exception
     */
    public abstract void publishTopic(JmsPayload<?> payload, JmsProperty property, JmsHandler handler) throws Exception;

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
    public void sendQueue(String destination, JmsPayload<?> payload, JmsProperty property, JmsHandler handler) throws Exception {
        payload.setKind(JmsKind.QUEUE);
        payload.setDestination(destination);
        this.sendQueue(payload, property, handler);
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
    public JmsPayload<?> sendAndReceiveQueue(String destination, JmsPayload<?> payload, JmsProperty property, JmsHandler handler) throws Exception {
        payload.setKind(JmsKind.QUEUE);
        payload.setDestination(destination);
        return this.sendAndReceiveQueue(payload, property, handler);
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
    public void publishTopic(String destination, JmsPayload<?> payload, JmsProperty property, JmsHandler handler) throws Exception {
        payload.setKind(JmsKind.TOPIC);
        payload.setDestination(destination);
        this.publishTopic(payload, property, handler);
    }
}
