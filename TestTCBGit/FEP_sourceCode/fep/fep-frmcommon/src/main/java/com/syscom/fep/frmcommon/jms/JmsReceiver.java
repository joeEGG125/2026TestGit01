package com.syscom.fep.frmcommon.jms;

import jakarta.jms.Message;

import java.io.Serializable;
import java.util.EventListener;
import java.util.List;

/**
 * JMS訊息接收處理者, 由JMS訊息監聽器將收到的訊息, 經JmsMsgNotifier通知轉發給JMS訊息接收處理者
 *
 * @param <T>
 * @author Richard
 */
public interface JmsReceiver<T extends Serializable> extends EventListener {
    /**
     * 接收訊息
     *
     * @param destination
     * @param payload
     * @param message
     */
    void messageReceived(String destination, T payload, Message message);

    /**
     * 批量接收訊息
     *
     * @param destination
     * @param payloadList
     * @param messageList
     */
    default void messageBatchReceived(String destination, List<T> payloadList, List<Message> messageList) {}

    /**
     * 是否同步化處理, 及一筆一筆訊息按照接收的順序處理
     *
     * @return
     */
    default boolean isSynchronized() {
        return false;
    }
}
