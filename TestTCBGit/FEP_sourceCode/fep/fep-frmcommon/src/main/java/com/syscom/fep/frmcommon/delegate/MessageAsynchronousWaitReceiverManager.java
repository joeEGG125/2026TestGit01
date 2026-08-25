package com.syscom.fep.frmcommon.delegate;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MessageAsynchronousWaitReceiverManager {
    private static final LogHelper logger = new LogHelper();
    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap messageReceiverMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <K, T> void subscribe(Object caller, MessageAsynchronousWaitReceiver<K, T> receiver) {
        if (!messageReceiverMap.containsKey(receiver.getKey())) {
            messageReceiverMap.put(receiver.getKey(), receiver);
            logger.debug("[", caller, "]Subscribe MessageReceiver, key = [", receiver.getKey(), "], messageClassname = [", receiver.getMessageClass().getName(), "]");
        } else {
            throw ExceptionUtil.createUnsupportedOperationException("MessageReceiver already exists, key = [", receiver.getKey(), "]");
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, T> void subscribeRepeatedly(Object caller, MessageAsynchronousWaitReceiver<K, T> receiver) {
        messageReceiverMap.put(receiver.getKey(), receiver);
        logger.debug("[", caller, "]Subscribe repeatedly MessageReceiver, key = [", receiver.getKey(), "], messageClassname = [", receiver.getMessageClass().getName(), "]");
    }

    public static <K, T> MessageAsynchronousWaitReceiver<K, T> unsubscribe(Object caller, K key, Class<T> messageClass) {
        return unsubscribe(caller, key, messageClass.getName());
    }

    @SuppressWarnings("unchecked")
    public static <K, T> MessageAsynchronousWaitReceiver<K, T> unsubscribe(Object caller, K key, String messageClassname) {
        Object object = messageReceiverMap.get(key);
        if (object != null) {
            MessageAsynchronousWaitReceiver<?, ?> receiver = (MessageAsynchronousWaitReceiver<?, ?>) object;
            // 必須比對key和message的類型一致, 才可以remove並且返回對應的receiver
            if (receiver.getKeyClass().getName().equals(key.getClass().getName()) && receiver.getMessageClass().getName().equals(messageClassname)) {
                messageReceiverMap.remove(key);
                logger.debug("[", caller, "]Unsubscribe MessageReceiver, key = [", key, "], messageClassname = [", messageClassname, "]");
                return (MessageAsynchronousWaitReceiver<K, T>) receiver;
            } else {
                logger.warn("[", caller, "]Cannot unsubscribe MessageReceiver, Inconsistently key class and message class, key = [", key, "]");
            }
        } else {
            logger.warn("[", caller, "]Cannot unsubscribe MessageReceiver which is not exist, key = [", key, "]");
        }
        return null;
    }

    public static <K> boolean waitMessage(Object caller, K key, long timeout) {
        Object object = messageReceiverMap.get(key);
        if (object != null) {
            MessageAsynchronousWaitReceiver<?, ?> receiver = (MessageAsynchronousWaitReceiver<?, ?>) object;
            // 必須比對key和message的類型一致, 才可以remove並且返回對應的receiver
            if (receiver.getKeyClass().getName().equals(key.getClass().getName())) {
                logger.debug("[", caller, "]Find MessageReceiver and start to wait, key = [", key, "], messageClassname = [", receiver.getMessageClass().getName(), "]");
                return receiver.waitMessage(caller, timeout);
            } else {
                logger.warn("[", caller, "]Cannot Find MessageReceiver, Inconsistently key class, key = [", key, "]");
            }
        } else {
            logger.warn("[", caller, "]Cannot Find MessageReceiver which is not exist, key = [", key, "]");
        }
        return true;
    }

    public static <K, T> void messageArrived(Object caller, K key, T message) {
        MessageAsynchronousWaitReceiver<K, T> receiver = unsubscribe(caller, key, message.getClass().getName());
        if (receiver != null) {
            logger.debug("[", caller, "]Find MessageReceiver and message arrived, key = [", key, "], message = [", message, "], messageClassname = [", message.getClass().getName(), "]");
            receiver.messageArrived(caller, message);
        } else {
            logger.warn("[", caller, "]Cannot find MessageReceiver, key = [", key, "], message = [", message, "], messageClassname = [", message.getClass().getName(), "]");
        }
    }
}
