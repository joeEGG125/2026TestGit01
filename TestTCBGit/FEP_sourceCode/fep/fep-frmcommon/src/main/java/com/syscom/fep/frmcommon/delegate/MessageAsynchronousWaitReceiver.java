package com.syscom.fep.frmcommon.delegate;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefBoolean;

public class MessageAsynchronousWaitReceiver<K, T> {
    private final LogHelper logger = new LogHelper();
    private final Class<K> keyClass;
    private final Class<T> messageClass;
    private final K key;
    private T message;
    private final RefBoolean lockWait = new RefBoolean(true);

    @SuppressWarnings("unchecked")
    public MessageAsynchronousWaitReceiver(K key, Class<T> messageClass) {
        this.key = key;
        this.keyClass = (Class<K>) key.getClass();
        this.messageClass = messageClass;
    }

    public Class<K> getKeyClass() {
        return keyClass;
    }

    public Class<T> getMessageClass() {
        return messageClass;
    }

    public K getKey() {
        return key;
    }

    public T getMessage() {
        return this.message;
    }

    public void messageArrived(Object caller, T message) {
        logger.debug("[", caller, "][", this.key, "][", this.messageClass.getName(), "]Message arrived and start to notify, message = [", message, "]");
        this.message = message;
        synchronized (this.lockWait) {
            this.lockWait.set(false); // 如果在waitMessage之前就messageArrived, 則就不用再waitMessage
            this.lockWait.notifyAll();
        }
    }

    public boolean waitMessage(Object caller, long timeout) {
        synchronized (this.lockWait) {
            // 如果lockWait為false, 表示不需要等待
            if (!this.lockWait.get()) {
                logger.warn("[", caller, "][", this.key, "][", this.messageClass.getName(), "]Message Already Arrived, no need to wait, timeout = [", timeout, "]");
                return true;
            }
            if (timeout <= 0) {
                timeout = 1000L;
            }
            long currentTimeMillis = System.currentTimeMillis();
            logger.debug("[", caller, "][", this.key, "][", this.messageClass.getName(), "]Start to wait, timeout = [", timeout, "]");
            try {
                this.lockWait.wait(timeout);
            } catch (Exception e) {
                logger.warn(e, e.getMessage());
            }
            long elapsed = System.currentTimeMillis() - currentTimeMillis;
            if (elapsed > timeout) {
                logger.warn("[", caller, "][", this.key, "]Wait timeout, timeout = [", timeout, "], elapsed = [", elapsed, "]");
                return false;
            }
            logger.debug("[", caller, "][", this.key, "]Wait succeed, timeout = [", timeout, "], elapsed = [", elapsed, "]");
            return true;
        }
    }

    public boolean isMessageArrived() {
        synchronized (this.lockWait) {
            return !this.lockWait.get();
        }
    }
}