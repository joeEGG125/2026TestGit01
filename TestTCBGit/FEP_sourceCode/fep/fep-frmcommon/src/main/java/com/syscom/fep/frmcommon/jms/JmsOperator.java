package com.syscom.fep.frmcommon.jms;

import org.springframework.jms.core.JmsTemplate;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

/**
 * JMS訊息發送
 *
 * @author Richard
 */
public interface JmsOperator<PayloadOut extends Serializable, PayloadIn extends Serializable> extends JmsConstant {

    /**
     * 獲取JmsTemplate實例
     *
     * @return
     */
    JmsTemplate getJmsTemplate();

    /**
     * 送出Queue訊息
     *
     * @param destination
     * @param payloadOut
     * @param property
     * @param handler
     * @throws Exception
     */
    void sendQueue(String destination, PayloadOut payloadOut, JmsProperty property, JmsHandler handler) throws Exception;

    /**
     * 送出Queue訊息並接收
     *
     * @param destination
     * @param payloadOut
     * @param property
     * @param handler
     * @return
     * @throws Exception
     */
    PayloadIn sendAndReceiveQueue(String destination, PayloadOut payloadOut, JmsProperty property, JmsHandler handler) throws Exception;

    /**
     * 接收Queue訊息
     *
     * @param destination
     * @param property
     * @param handler
     * @return
     * @throws Exception
     */
    PayloadIn receiveQueue(String destination, JmsProperty property, JmsHandler handler) throws Exception;

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
    List<PayloadIn> browseQueue(String destination, Predicate<PayloadOut> predicate, JmsProperty property, JmsHandler handler) throws Exception;

    /**
     * 送出Topic訊息
     *
     * @param destination
     * @param payloadOut
     * @param property
     * @param handler
     * @throws Exception
     */
    void publishTopic(String destination, PayloadOut payloadOut, JmsProperty property, JmsHandler handler) throws Exception;

    /**
     * 接收Topic訊息
     *
     * @param destination
     * @param property
     * @param handler
     * @return
     * @throws Exception
     */
    PayloadIn receiveTopic(String destination, JmsProperty property, JmsHandler handler) throws Exception;

    /**
     * 開始Queue/Topic接收
     *
     * @param destination
     */
    void startReceive(String destination);

    /**
     * 停止Queue/Topic接收
     *
     * @param destination
     */
    void stopReceive(String destination);

}
