package com.syscom.fep.frmcommon.jms;

import com.syscom.fep.frmcommon.jms.entity.JmsInfoConcurrency;
import com.syscom.fep.frmcommon.jms.entity.JmsMonitorConcurrencyRequest;
import com.syscom.fep.frmcommon.jms.entity.JmsMonitorResponseErrorCode;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringValueResolverUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMS訊息通知類, 用來實現接收到的訊息, 可以通知到多個接收者
 * <p>
 * JMS的監聽器JmsMsgListener的實作類必須要繼承此類
 *
 * @param <R>
 * @param <T>
 * @author Richard
 */
public abstract class JmsNotifier<T extends Serializable, R extends JmsReceiver<T>> extends MessageListenerAdapter implements MessageConverter, JmsConstant {
    private final LogHelper jmsLogger = new LogHelper(this.getClass().getName());
    private final String CLASS_NAME = this.getClass().getSimpleName();
    private final List<R> receiverList = new ArrayList<>();
    // private PayloadMessageNotifierThread payloadMessageNotifierThread = null;
    // private ThreadPoolExecutor executor = null;
    @Autowired
    private JmsListenerEndpointRegistry registry;
    /**
     * Please refer {@link JmsListener#id()}
     */
    protected String identity;
    /**
     * Please refer {@link JmsListener#destination()}
     */
    protected String destination;
    /**
     * Please refer {@link JmsListener#concurrency()}
     */
    private String concurrency;
    private String initConcurrency;

    @PostConstruct
    public void initialization() {
        Method method = ReflectionUtils.findMethod(this.getClass(), "onMessage", Message.class);
        if (method != null) {
            JmsListener jmsListener = method.getAnnotation(JmsListener.class);
            if (jmsListener != null) {
                this.identity = StringValueResolverUtil.resolve(jmsListener.id());
                this.destination = StringValueResolverUtil.resolve(jmsListener.destination());
                this.concurrency = StringValueResolverUtil.resolve(jmsListener.concurrency());
                this.initConcurrency = StringValueResolverUtil.resolve(jmsListener.concurrency());
                // 如果@JmsListener中的concurrency沒有在配置檔中設定, 則嘗試從DefaultJmsListenerContainerFactory取
                if (StringUtils.isBlank(this.initConcurrency) && StringUtils.isBlank(this.concurrency)) {
                    String containerFactory = StringValueResolverUtil.resolve(jmsListener.containerFactory());
                    DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory = SpringBeanFactoryUtil.getBean(containerFactory);
                    if (defaultJmsListenerContainerFactory != null) {
                        this.initConcurrency = ReflectUtil.getFieldValue(defaultJmsListenerContainerFactory, "concurrency", StringUtils.EMPTY);
                        this.concurrency = ReflectUtil.getFieldValue(defaultJmsListenerContainerFactory, "concurrency", StringUtils.EMPTY);
                    }
                }
            }
        }
        // this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(new SimpleThreadFactory(StringUtils.join(this.identity, "-notifier")));
        // this.payloadMessageNotifierThread = new PayloadMessageNotifierThread(this.getFlushIntervalInMilliseconds());
        jmsLogger.info("[", CLASS_NAME, "][", this.destination, "]initialization succeed");
    }

    @PreDestroy
    public void destroy() {
        jmsLogger.warn("[", CLASS_NAME, "][", this.destination, "]start to destroy...");
        this.shutdown(); // 注意這裡一定要先關閉, 否則會有遺留的JMS線程
        // this.payloadMessageNotifierThread.terminate();
        this.receiverList.forEach(this::unSubscribe);
        // ThreadPoolFactory.shutdown(this.executor, "[", CLASS_NAME, "][", this.destination, "]");
        SpringBeanFactoryUtil.unregisterBean(this.getClass());
    }

    /**
     * 註冊
     *
     * @param receiver
     */
    public JmsNotifier<T, R> subscribe(R receiver) {
        this.receiverList.add(receiver);
        jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][subscribe]receiver = [", receiver, "], size = [", receiverList.size(), "]");
        return this;
    }

    /**
     * 反註冊
     *
     * @param receiver
     */
    public JmsNotifier<T, R> unSubscribe(R receiver) {
        receiverList.remove(receiver);
        jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][unSubscribe]receiver = [", receiver, "], size = [", receiverList.size(), "]");
        return this;
    }

    /**
     * 開始接收
     */
    public JmsNotifier<T, R> start() {
        if (JmsFactory.startReceive(registry, identity)) {
            jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][start]size = [", receiverList.size(), "]");
        }
        return this;
    }

    /**
     * 停止接收
     */
    public JmsNotifier<T, R> stop() {
        if (JmsFactory.stopReceive(registry, identity)) {
            jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][stop]size = [", receiverList.size(), "]");
        }
        return this;
    }

    /**
     * 關閉線程池, 等待線程池積壓消息處理
     */
    public JmsNotifier<T, R> shutdown() {
        if (JmsFactory.shutdown(registry, identity)) {
            jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][shutdown]size = [", receiverList.size(), "]");
        }
        return this;
    }

    /**
     * 設置concurrency, Please refer {@link JmsListener#concurrency()}
     *
     * @param request
     * @return
     * @throws Exception
     */
    public boolean setConcurrency(JmsMonitorConcurrencyRequest request) throws Exception {
        String concurrency = request.getConcurrency();
        // 為空白則什麼都不做
        if (StringUtils.isBlank(concurrency)) {
            jmsLogger.warn("[", CLASS_NAME, "][", this.destination, "][setConcurrency]concurrency cannot be empty!!!");
            throw ExceptionUtil.createException("concurrency cannot be empty!!!");
        }
        // 設置為-1時, 則重置為預設值
        else if ("-1".equals(concurrency.trim())) {
            return request.isModified() ? this.resetConcurrency() : true;
        }
        // 先取出配置檔中的初始最大值
        int maxInitConcurrency = 1;
        try {
            if (StringUtils.isNotBlank(initConcurrency)) {
                int separatorIndex = initConcurrency.indexOf('-');
                if (separatorIndex != -1) {
                    maxInitConcurrency = Integer.parseInt(initConcurrency.substring(separatorIndex + 1));
                } else {
                    maxInitConcurrency = Integer.parseInt(initConcurrency);
                }
            }
        } catch (NumberFormatException ex) {
            jmsLogger.warn("[", CLASS_NAME, "][", this.destination, "][setConcurrency]invalid initConcurrency!!!");
        }
        // 再取出需要修改的最大值
        int maxConcurrency = 0;
        try {
            int separatorIndex = concurrency.indexOf('-');
            if (separatorIndex != -1) {
                maxConcurrency = Integer.parseInt(concurrency.substring(separatorIndex + 1));
            } else {
                maxConcurrency = Integer.parseInt(concurrency);
            }
        } catch (NumberFormatException ex) {
            // 這裡只列印log
            jmsLogger.warn("[", CLASS_NAME, "][", this.destination, "][setConcurrency]invalid concurrency!!!");
        }
        // 修改的線程數不能超過配置檔中預設的線程數的最大倍數
        int MAX_THREADS_RATIO = 2;
        if (maxConcurrency > maxInitConcurrency * MAX_THREADS_RATIO) {
            throw ExceptionUtil.createException(StringUtils.join(JmsMonitorResponseErrorCode.SET_CONCURRENCY_OVER_MAX_THREADS_RATIO, "您輸入的線程數", String.format("「%s」", concurrency), ", 己超過設定檔", String.format("「%s」", StringUtils.isBlank(initConcurrency) ? "1" : initConcurrency), "的", MAX_THREADS_RATIO, "倍, 請重新輸入"));
        }
        // 如果不用修改線程數, 則直接返回true
        if (!request.isModified())
            return true;
        AbstractMessageListenerContainer container = (AbstractMessageListenerContainer) registry.getListenerContainer(identity);
        if (container != null) {
            this.concurrency = concurrency;
            try {
                container.setConcurrency(concurrency);
                jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][setConcurrency]concurrency = [", this.concurrency, "]");
                return true;
            } catch (Exception e) {
                jmsLogger.error(e, "[", CLASS_NAME, "][", this.destination, "][setConcurrency]concurrency = [", concurrency, "] with exception occur, ", e.getMessage());
                throw ExceptionUtil.createException(e, "set concurrency with exception occur, ", e.getMessage());
            }
        }
        return false;
    }

    /**
     * 重置concurrency為初始值, Please refer {@link JmsListener#concurrency()}
     *
     * @return
     * @throws Exception
     */
    public boolean resetConcurrency() throws Exception {
        jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][resetConcurrency]concurrency = [", this.initConcurrency, "]");
        int initMaxConcurrency = 1;
        if (StringUtils.isNotBlank(this.initConcurrency)) {
            int separatorIndex = this.initConcurrency.indexOf('-');
            if (separatorIndex != -1) {
                initMaxConcurrency = Integer.parseInt(this.initConcurrency.substring(separatorIndex + 1));
            } else {
                initMaxConcurrency = Integer.parseInt(this.initConcurrency);
            }
        }
        // 若執行中的線程數>初始值, 則直接丟異常
        JmsInfoConcurrency jmsInfoConcurrency = this.getConcurrency();
        if (jmsInfoConcurrency.getActiveInvokerCount() > initMaxConcurrency) {
            throw ExceptionUtil.createException("[", JmsMonitorResponseErrorCode.CANNOT_REDUCE_CONCURRENCY, "]cannot reset cause Active Concurrency [", jmsInfoConcurrency.getActiveInvokerCount(), "] is greater than Init Max Concurrency [", initMaxConcurrency, "]");
        }
        // initConcurrency為空字串, 則default設為1
        return this.setConcurrency(new JmsMonitorConcurrencyRequest(StringUtils.isBlank(this.initConcurrency) ? "1" : this.initConcurrency, true));
    }

    /**
     * 獲取並發數相關信息
     *
     * @return
     */
    public JmsInfoConcurrency getConcurrency() {
        JmsInfoConcurrency jmsInfoConcurrency = new JmsInfoConcurrency();
        jmsInfoConcurrency.setIdentity(this.identity);
        jmsInfoConcurrency.setDestination(this.destination);
        jmsInfoConcurrency.setInitConcurrency(this.initConcurrency);
        jmsInfoConcurrency.setCurrentConcurrency(this.concurrency);
        DefaultMessageListenerContainer container = (DefaultMessageListenerContainer) registry.getListenerContainer(identity);
        if (container != null) {
            jmsInfoConcurrency.setConcurrentConsumers(container.getConcurrentConsumers());
            jmsInfoConcurrency.setMaxConcurrentConsumers(container.getMaxConcurrentConsumers());
            jmsInfoConcurrency.setActiveConsumerCount(container.getActiveConsumerCount());
            jmsInfoConcurrency.setPausedTaskCount(container.getPausedTaskCount());
            jmsInfoConcurrency.setScheduledConsumerCount(container.getScheduledConsumerCount());
            jmsInfoConcurrency.setIdleInvokerCount(ReflectUtil.envokeMethod(container, "getIdleInvokerCount", null, null, 0));
        }
        jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][getJmsConcurrency]concurrency = [", ReflectionToStringBuilder.toString(jmsInfoConcurrency, ToStringStyle.SHORT_PREFIX_STYLE), "]");
        return jmsInfoConcurrency;
    }

    /**
     * 設置messageSelector, Please refer {@link JmsListener#selector()}
     *
     * @param messageSelector
     */
    public void setMessageSelector(String messageSelector) {
        AbstractMessageListenerContainer container = (AbstractMessageListenerContainer) registry.getListenerContainer(identity);
        if (container != null) {
            container.setMessageSelector(messageSelector);
            jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][setMessageSelector]messageSelector = [", messageSelector, "]");
        }
    }

    /**
     * 接收訊息
     *
     * @param message
     */
    protected void messageReceived(Message message) {
        if (JmsFactory.messageLogEnable()) {
            try {
                jmsLogger.debug("[", CLASS_NAME, "][", this.destination, "][messageReceived]", Message.class.getName(), " : ", message);
            } catch (Throwable t) {
                jmsLogger.warn(t, t.getMessage());
            }
        }
        // try {
        //     NotifierData<T> notifierData = new NotifierData<>(message);
        //     this.logIn(jmsLogger, this.destination, notifierData.payload);
        //     this.messageReceived(notifierData);
        // } catch (Throwable e) {
        //     jmsLogger.error(e, "[", CLASS_NAME, "][", this.destination, "][messageReceived] exception occur ", e.getMessage());
        // }
        try {
            @SuppressWarnings("unchecked")
            T payload = (T) this.fromMessage(message);
            this.logIn(jmsLogger, this.destination, payload);
            receiverList.forEach(receiver -> {
                jmsLogger.info("start to notify to receiver = [", receiver.getClass().getName(), "], destination = [", destination, "], payload = [", payload, "]");
                receiver.messageReceived(destination, payload, message);
            });
        } catch (Throwable e) {
            jmsLogger.error(e, "[", CLASS_NAME, "][", this.destination, "][messageReceived] exception occur ", e.getMessage());
        }
    }

    /**
     * 是否將訊息蒐集一段時間後, 再進行notify, 由子類覆寫
     *
     * @return
     */
    protected long getFlushIntervalInMilliseconds() {
        return 0;
    }

    /**
     * 手動ack
     *
     * @param message
     */
    protected void acknowledge(Message message) {
        try {
            message.acknowledge();
            // jmsLogger.debug("[", CLASS_NAME, "][", this.destination, "][acknowledge] succeed");
        } catch (JMSException e) {
            jmsLogger.error(e, "[", CLASS_NAME, "][", this.destination, "][acknowledge] exception occur ", e.getMessage());
        }
    }

//    /**
//     * 接收訊息
//     *
//     * @param notifierData
//     */
//    private void messageReceived(NotifierData<T> notifierData) {
//        jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][messageReceived][", notifierData.payload, "]");
//        payloadMessageNotifierThread.messageReceived(notifierData);
//    }
//
//    /**
//     * 設定Thread Pool相關屬性
//     *
//     * @param corePoolSize
//     * @param maximumPoolSize
//     * @param keepAliveTime
//     * @param threadFactory
//     * @param handler
//     */
//    protected void setThreadPoolExecutorProperty(int corePoolSize, int maximumPoolSize, long keepAliveTime, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
//        jmsLogger.info("[", CLASS_NAME, "][", this.destination, "][setThreadPoolExecutorProperty]corePoolSize = [", corePoolSize, "], maximumPoolSize = [", maximumPoolSize, "], keepAliveTime = [", keepAliveTime, "], threadFactory = [", threadFactory, "], handler = [", handler, "]");
//        if (corePoolSize > 0)
//            executor.setCorePoolSize(corePoolSize);
//        if (maximumPoolSize > 0)
//            executor.setMaximumPoolSize(maximumPoolSize);
//        if (keepAliveTime > 0)
//            executor.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);
//        if (threadFactory != null)
//            executor.setThreadFactory(threadFactory);
//        if (handler != null)
//            executor.setRejectedExecutionHandler(handler);
//    }
//
//    private class PayloadMessageNotifierThread extends Thread {
//        private final List<NotifierData<T>> payloadList = Collections.synchronizedList(new ArrayList<>());
//        private final Object flushLock = new Object();
//        private boolean running = true;
//        private long flushIntervalInMilliseconds = 0;
//        private final AtomicBoolean wait = new AtomicBoolean(true);
//
//        public PayloadMessageNotifierThread(long flushIntervalInMilliseconds) {
//            super(StringUtils.join(CLASS_NAME, ".", destination, ".", flushIntervalInMilliseconds));
//            setPriority(Thread.NORM_PRIORITY);
//            this.flushIntervalInMilliseconds = flushIntervalInMilliseconds;
//            start();
//        }
//
//        public void terminate() {
//            this.running = false;
//            // payloadList要notifyAll一下
//            synchronized (this.payloadList) {
//                this.payloadList.notifyAll();
//            }
//            // flushLock要notifyAll一下
//            synchronized (this.flushLock) {
//                this.flushLock.notifyAll();
//            }
//        }
//
//        public void messageReceived(NotifierData<T> notifierData) {
//            this.payloadList.add(notifierData);
//            // if (this.payloadList.size() == 1) {
//            if (wait.get()) {
//                synchronized (this.payloadList) {
//                    this.payloadList.notifyAll();
//                }
//            }
//            // }
//        }
//
//        @Override
//        public void run() {
//            while (this.running) {
//                try {
//                    if (this.payloadList.isEmpty()) {
//                        wait.set(true);
//                        synchronized (this.payloadList) {
//                            this.payloadList.wait(600000);
//                        }
//                        wait.set(false);
//                    }
//                    // 如果有收到訊息, 並且flushIntervalInMilliseconds大於0, 則再wait上一段時間, 收更多的訊息進來一起notify
//                    if (!this.payloadList.isEmpty() && this.flushIntervalInMilliseconds > 0) {
//                        jmsLogger.debug("[", this.getName(), "][run]wait to collect more payloads in [", flushIntervalInMilliseconds, "] milliseconds...");
//                        synchronized (this.flushLock) {
//                            this.flushLock.wait(this.flushIntervalInMilliseconds);
//                        }
//                    }
//                    @SuppressWarnings("unchecked")
//                    NotifierData<T>[] payloads = new NotifierData[this.payloadList.size()];
//                    this.payloadList.toArray(payloads);
//                    this.payloadList.clear();
//                    if (ArrayUtils.isNotEmpty(payloads)) {
//                        if (this.flushIntervalInMilliseconds > 0) {
//                            jmsLogger.debug("[", this.getName(), "][run]start to notify all payloads after [", flushIntervalInMilliseconds, "] milliseconds, payloads.length = [", payloads.length, "]");
//                            final List<T> payloadList = Arrays.stream(payloads).map(e -> e.payload).collect(Collectors.toList());
//                            final List<Message> messageList = Arrays.stream(payloads).map(e -> e.message).collect(Collectors.toList());
//                            receiverList.forEach(receiver -> {
//                                executor.execute(() -> {
//                                    receiver.messageBatchReceived(destination, payloadList, messageList);
//                                });
//                            });
//                        } else {
//                            receiverList.forEach(receiver -> {
//                                if (receiver.isSynchronized()) {
//                                    for (final NotifierData<T> payload : payloads) {
//                                        receiver.messageReceived(destination, payload.payload, payload.message);
//                                    }
//                                } else {
//                                    executor.execute(() -> {
//                                        for (final NotifierData<T> payload : payloads) {
//                                            receiver.messageReceived(destination, payload.payload, payload.message);
//                                        }
//                                    });
//                                }
//                            });
//                        }
//                    }
//                } catch (Throwable e) {
//                    jmsLogger.warn(e, "[", this.getName(), "][run]exception occur, ", e.getMessage());
//                }
//            }
//        }
//    }
//
//    private class NotifierData<T> {
//        Message message;
//        T payload;
//
//        @SuppressWarnings("unchecked")
//        public NotifierData(Message message) throws JMSException {
//            this.message = message;
//            this.payload = (T) fromMessage(message);
//        }
//    }
}
