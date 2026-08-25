package com.syscom.fep.frmcommon.jms;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.headers.CCSID;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQQueue;
import com.ibm.mq.jakarta.jms.MQTopic;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;
import com.ibm.msg.client.commonservices.trace.Trace;
import com.ibm.msg.client.jakarta.jms.JmsConstants;
import com.ibm.msg.client.jakarta.jms.JmsPropertyContext;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.*;
import jakarta.jms.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryFactory;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.converter.*;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

public class JmsFactory implements JmsConstant {
    private static final LogHelper logger = new LogHelper();
    private static final String CLASS_NAME = JmsFactory.class.getSimpleName();
    private static final String SYSTEM_KEY_MESSAGE_LOG_ENABLE = "spring.fep.jms.message.log.enable";

    // 避免IBM Queue底層在處理byte[]根據指定編碼轉換時出現Exception
    static {
        System.setProperty("com.ibm.mq.cfg.jmqi.UnmappableCharacterAction", "REPLACE");
        System.setProperty("com.ibm.mq.cfg.jmqi.UnmappableCharacterReplacement", "63");
        // 2025-10-20 Richard add 加入TCP連線超時設定
        int connectTimeout = EnvPropertiesUtil.getProperty("spring.fep.jms.com.ibm.mq.cfg.TCP.Connect_Timeout", 0);
        if (connectTimeout > 0) {
            System.setProperty("com.ibm.mq.cfg.TCP.Connect_Timeout", Integer.toString(connectTimeout));
        }
        Trace.setOn(EnvPropertiesUtil.getProperty("spring.fep.jms.com.ibm.msg.client.commonservices.trace.Trace", false));
    }

    private JmsFactory() {}

    public static MQQueueManager createMQQueueManager(String hostname, int port, String queueManagerName, String channel, String userID, String password) throws MQException {
        Hashtable<String, Object> mqht = new Hashtable<String, Object>();
        mqht.put(CMQC.CHANNEL_PROPERTY, channel);
        mqht.put(CMQC.HOST_NAME_PROPERTY, hostname);
        mqht.put(CMQC.PORT_PROPERTY, port);
        if (StringUtils.isNotEmpty(userID)) {
            mqht.put(CMQC.USER_ID_PROPERTY, userID);
        } else {
            logger.warn("no specific userID for hostname = [", hostname, "], port = [", port, "], queueManagerName = [", queueManagerName, "], channel = [", channel, "]");
        }
        if (StringUtils.isNotEmpty(password)) {
            mqht.put(CMQC.PASSWORD_PROPERTY, password);
        } else {
            logger.warn("no specific password for hostname = [", hostname, "], port = [", port, "], queueManagerName = [", queueManagerName, "], channel = [", channel, "], userID = [", userID, "]");
        }
        MQQueueManager queueManager = new MQQueueManager(queueManagerName, mqht);
        return queueManager;
    }

    public static ConnectionFactory createMQConnectionFactory(JmsConfigurationProperties properties) throws JMSException {
        MQConnectionFactory factory = new MQConnectionFactory();
        MQConnectionFactoryFactory.configureConnectionFactory(factory, properties);
        setJmsPropertyContextProperty(factory);
        return factory;
    }

    private static void setJmsPropertyContextProperty(JmsPropertyContext context) throws JMSException {
        context.setBooleanProperty(WMQConstants.WMQ_MQMD_READ_ENABLED, true);
        context.setBooleanProperty(WMQConstants.WMQ_MQMD_WRITE_ENABLED, true);
    }

    public static UserCredentialsConnectionFactoryAdapter createUserCredentialsConnectionFactoryAdapter(ConnectionFactory factory, JmsConfigurationProperties properties) {
        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
        logger.info("hostname:", properties.getHostName(), ";queueManager:", properties.getQueueManager(), ";channel:", properties.getChannel(), ";user:", properties.getUser(), ";password:", properties.getPassword());
        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(factory);
        userCredentialsConnectionFactoryAdapter.setUsername(properties.getUser());
        if (StringUtils.isNotEmpty(properties.getPassword())) {
            userCredentialsConnectionFactoryAdapter.setPassword(properties.getPassword());
        } else {
            logger.warn("no specific password for hostname = [", properties.getHostName(), "], queueManagerName = [", properties.getQueueManager(), "], channel = [", properties.getChannel(), "], userID = [", properties.getUser(), "]");
        }
        return userCredentialsConnectionFactoryAdapter;
    }

    public static CachingConnectionFactory createCachingConnectionFactory(ConnectionFactory connectionFactory, JmsConfigurationProperties properties) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(connectionFactory);
        cachingConnectionFactory.setSessionCacheSize(properties.getCache().getSessionCacheSize());
        cachingConnectionFactory.setReconnectOnException(properties.getCache().isReconnectOnException());
        return cachingConnectionFactory;
    }

    public static JmsPoolConnectionFactory createPoolConnectionFactory(ConnectionFactory connectionFactory, JmsConfigurationProperties properties) {
        return new JmsPoolConnectionFactoryFactory(properties.getPool()).createPooledConnectionFactory(connectionFactory);
    }

    public static void closeConnection(String instanceName, ConnectionFactory connectionFactory) {
        if (connectionFactory instanceof CachingConnectionFactory) {
            logger.info("[closeConnection] call CachingConnectionFactory destroy method for instance = [", instanceName, "]");
            ((CachingConnectionFactory) connectionFactory).destroy();
        } else if (connectionFactory instanceof JmsPoolConnectionFactory) {
            logger.info("[closeConnection] call JmsPoolConnectionFactory stop method for instance = [", instanceName, "]");
            ((JmsPoolConnectionFactory) connectionFactory).stop();
        }
    }

    public static MessageConverter createMappingJackson2MessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    public static MessageConverter createSimpleMessageConverter() {
        return new SimpleMessageConverter();
    }

    public static MessageConverter createSimpleStringInMessageConverter() {
        return new SimpleMessageConverter() {
            @Override
            public Object fromMessage(Message message) throws JMSException, MessageConversionException {
                Object messageIn = super.fromMessage(message);
                if (messageIn instanceof String) {
                    return messageIn;
                } else if (messageIn instanceof byte[]) {
                    byte[] bytes = (byte[]) messageIn;
                    String charsetName = getCharacterSet(message);
                    if (StringUtils.isNotBlank(charsetName)) {
                        try {
                            return new String(bytes, charsetName);
                        } catch (UnsupportedEncodingException e) {
                            logger.error(e, e.getMessage());
                        }
                    }
                    return new String(bytes, StandardCharsets.UTF_8);
                }
                // return super.fromMessage(message);
                return messageIn.toString();
            }
        };
    }

    public static MessageConverter createSimpleStringInMessageConverter950() {
        return new SimpleMessageConverter() {
            @Override
            public Object fromMessage(Message message) throws JMSException, MessageConversionException {
                Object messageIn = super.fromMessage(message);
                if (messageIn instanceof String) {
                    return messageIn;
                } else if (messageIn instanceof byte[]) {
                    byte[] bytes = (byte[]) messageIn;
                    String charsetName = "x-IBM950";
                    if (StringUtils.isNotBlank(charsetName)) {
                        try {
                            return new String(bytes, charsetName);
                        } catch (UnsupportedEncodingException e) {
                            logger.error(e, e.getMessage());
                        }
                    }
                    return new String(bytes, StandardCharsets.UTF_8);
                }
                return super.fromMessage(message);
            }
        };
    }

    public static PlatformTransactionManager createPlatformTransactionManager(ConnectionFactory connectionFactory) {
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(connectionFactory);
        return jmsTransactionManager;
    }

    public static DefaultJmsListenerContainerFactory createQueueListenerFactory(JmsConfigurationProperties properties, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(properties.isSessionTransacted());
        factory.setSessionAcknowledgeMode(properties.getSessionAcknowledgeMode());
        factory.setPubSubDomain(false);
        if (StringUtils.isNotBlank(properties.getConcurrency()))
            factory.setConcurrency(properties.getConcurrency());
        return factory;
    }

    public static DefaultJmsListenerContainerFactory createTopicListenerFactory(JmsConfigurationProperties properties, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(properties.isSessionTransacted());
        factory.setSessionAcknowledgeMode(properties.getSessionAcknowledgeMode());
        factory.setPubSubDomain(true);
        if (StringUtils.isNotBlank(properties.getConcurrency()))
            factory.setConcurrency(properties.getConcurrency());
        return factory;
    }

    // public static JmsTemplate createJmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
    //     return createJmsTemplate(connectionFactory, messageConverter, null);
    // }

    public static JmsTemplate createJmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter, JmsConfigurationQosSettingsProperties qosSettings) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setMessageConverter(messageConverter);
        if (qosSettings != null && qosSettings.isEnable()) {
            jmsTemplate.setQosSettings(qosSettings);
        }
        return jmsTemplate;
    }

    public static JmsTemplate clone(JmsTemplate source, JmsProperty property) {
        if (property != null) {
            JmsTemplate clone = copyProperty(source);
            return overwriteProperty(clone, property);
        }
        return source;
    }

    public static JmsTemplate copyProperty(JmsTemplate jmsTemplate) {
        JmsTemplate target = null;
        if (jmsTemplate != null) {
            target = new JmsTemplate();
            target.setConnectionFactory(jmsTemplate.getConnectionFactory());
            target.setMessageConverter(jmsTemplate.getMessageConverter());
            if (jmsTemplate.getDeliveryDelay() != -1)
                target.setDeliveryDelay(jmsTemplate.getDeliveryDelay());
            target.setMessageIdEnabled(jmsTemplate.isMessageIdEnabled());
            target.setMessageTimestampEnabled(jmsTemplate.isMessageTimestampEnabled());
            target.setPubSubDomain(jmsTemplate.isPubSubDomain());
            target.setPubSubNoLocal(jmsTemplate.isPubSubNoLocal());
            target.setReceiveTimeout(jmsTemplate.getReceiveTimeout());
            target.setSessionAcknowledgeMode(jmsTemplate.getSessionAcknowledgeMode());
            target.setSessionTransacted(jmsTemplate.isSessionTransacted());
            // QosSettings
            target.setExplicitQosEnabled(jmsTemplate.isExplicitQosEnabled());
            if (jmsTemplate.getDeliveryMode() == DeliveryMode.NON_PERSISTENT || jmsTemplate.getDeliveryMode() == DeliveryMode.PERSISTENT)
                target.setDeliveryMode(jmsTemplate.getDeliveryMode());
            target.setPriority(jmsTemplate.getPriority());
            target.setTimeToLive(jmsTemplate.getTimeToLive());
        }
        return target;
    }

    public static JmsTemplate overwriteProperty(JmsTemplate jmsTemplate, JmsProperty property) {
        if (jmsTemplate != null && property != null) {
            if (property.getDeliveryDelay() != -1)
                jmsTemplate.setDeliveryDelay(property.getDeliveryDelay());
            jmsTemplate.setMessageIdEnabled(property.isMessageIdEnabled());
            jmsTemplate.setMessageTimestampEnabled(property.isMessageTimestampEnabled());
            jmsTemplate.setPubSubDomain(property.isPubSubDomain());
            jmsTemplate.setPubSubNoLocal(property.isPubSubNoLocal());
            jmsTemplate.setReceiveTimeout(property.getReceiveTimeout());
            jmsTemplate.setSessionAcknowledgeMode(property.getSessionAcknowledgeMode());
            jmsTemplate.setSessionTransacted(property.isSessionTransacted());
            // QosSettings
            jmsTemplate.setExplicitQosEnabled(property.isExplicitQosEnabled());
            if (property.getDeliveryMode() == DeliveryMode.NON_PERSISTENT || property.getDeliveryMode() == DeliveryMode.PERSISTENT)
                jmsTemplate.setDeliveryMode(property.getDeliveryMode());
            jmsTemplate.setPriority(property.getPriority());
            jmsTemplate.setTimeToLive(property.getTimeToLive());
        }
        return jmsTemplate;
    }

    /**
     * 送出Queue訊息
     *
     * @param jmsTemplate
     * @param destination
     * @param messageOut
     * @param property
     * @param handler
     * @param <MessageOut>
     * @throws JMSException
     */
    public static <MessageOut extends Serializable> void sendQueue(JmsTemplate jmsTemplate, String destination, MessageOut messageOut, JmsProperty property, JmsHandler handler) throws JMSException {
        MQQueue mqQueue = new MQQueue(destination);
        setJmsPropertyContextProperty(mqQueue);
        if (handler != null) {
            handler.setPropertyOut(mqQueue);
        }
        JmsTemplate template = clone(jmsTemplate, property);
        template.send(mqQueue, session -> {
            Message message = template.getMessageConverter().toMessage(messageOut, session);
            if (handler != null) {
                handler.setPropertyOut(message);
            }
            logMessage(destination, "sendQueue", message, true);
            return message;
        });
    }

    /**
     * 送出Queue訊息並接收
     *
     * @param jmsTemplate
     * @param messageInConverter
     * @param destination
     * @param messageOut
     * @param property
     * @param handler
     * @param <MessageOut>
     * @param <MessageIn>
     * @return
     * @throws JMSException
     */
    @SuppressWarnings("unchecked")
    public static <MessageOut extends Serializable, MessageIn extends Serializable> MessageIn sendAndReceiveQueue(JmsTemplate jmsTemplate, MessageConverter messageInConverter, String destination, MessageOut messageOut, JmsProperty property, JmsHandler handler) throws JMSException {
        MQQueue mqQueue = new MQQueue(destination);
        setJmsPropertyContextProperty(mqQueue);
        if (handler != null) {
            handler.setPropertyOut(mqQueue);
        }
        JmsTemplate template = clone(jmsTemplate, property);
        Message messageIn = template.sendAndReceive(mqQueue, session ->
        {
            Message message = template.getMessageConverter().toMessage(messageOut, session);
            if (handler != null) {
                handler.setPropertyOut(message);
            }
            logMessage(destination, "sendAndReceiveQueue", message, true);
            return message;
        });
        logMessage(destination, "sendAndReceiveQueue", messageIn, false);
        if (handler != null) {
            handler.getPropertyIn(messageIn);
        }
        return messageIn == null ? null : (MessageIn) messageInConverter.fromMessage(messageIn);
    }

    /**
     * 接收Queue訊息
     *
     * @param jmsTemplate
     * @param messageInConverter
     * @param destination
     * @param property
     * @param handler
     * @param <MessageIn>
     * @return
     * @throws JMSException
     */
    @SuppressWarnings("unchecked")
    public static <MessageIn extends Serializable> MessageIn receiveQueue(JmsTemplate jmsTemplate, MessageConverter messageInConverter, String destination, JmsProperty property, JmsHandler handler) throws JMSException {
        MQQueue mqQueue = new MQQueue(destination);
        setJmsPropertyContextProperty(mqQueue);
        if (handler != null) {
            handler.setPropertyOut(mqQueue);
        }
        JmsTemplate template = clone(jmsTemplate, property);
        Message message = null;
        // Has MessageSelector
        if (property != null && StringUtils.isNotBlank(property.getMessageSelector())) {
            message = template.receiveSelected(mqQueue, property.getMessageSelector());
        } else {
            message = template.receive(mqQueue);
        }
        logMessage(destination, "receiveQueue", message, false);
        if (handler != null) {
            handler.getPropertyIn(message);
        }
        return message == null ? null : (MessageIn) messageInConverter.fromMessage(message);
    }

    /**
     * 瀏覽Queue消息
     *
     * @param jmsTemplate
     * @param messageConverter
     * @param destination
     * @param predicate
     * @param property
     * @param handler
     * @param <T>
     * @return
     * @throws JMSException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> List<T> browseQueue(JmsTemplate jmsTemplate, MessageConverter messageConverter, String destination, Predicate<? super T> predicate, JmsProperty property, JmsHandler handler) throws JMSException {
        MQQueue mqQueue = new MQQueue(destination);
        setJmsPropertyContextProperty(mqQueue);
        if (handler != null) {
            handler.setPropertyOut(mqQueue);
        }
        BrowserCallback<List<T>> action = (Session session, QueueBrowser browser) -> {
            List<T> result = new ArrayList<>();
            Enumeration<Message> messageEnum = browser.getEnumeration();
            while (messageEnum.hasMoreElements()) {
                Message message = messageEnum.nextElement();
                if (message == null)
                    continue;
                T t = (T) messageConverter.fromMessage(message);
                if (predicate != null && !predicate.test(t)) {
                    continue;
                }
                logMessage(destination, "browseQueue", message, false);
                if (handler != null) {
                    handler.getPropertyIn(message);
                }
                result.add(t);
            }
            return result;
        };
        JmsTemplate template = clone(jmsTemplate, property);
        // Has MessageSelector
        if (property != null && StringUtils.isNotBlank(property.getMessageSelector())) {
            return template.browseSelected(mqQueue, property.getMessageSelector(), action);
        }
        return template.browse(mqQueue, action);
    }

    /**
     * 送出Topic訊息
     *
     * @param jmsTemplate
     * @param destination
     * @param messageOut
     * @param property
     * @param handler
     * @param <MessageOut>
     * @throws JMSException
     */
    public static <MessageOut extends Serializable> void publishTopic(JmsTemplate jmsTemplate, String destination, MessageOut messageOut, JmsProperty property, JmsHandler handler) throws JMSException {
        MQTopic mqTopic = new MQTopic(destination);
        setJmsPropertyContextProperty(mqTopic);
        if (handler != null) {
            handler.setPropertyOut(mqTopic);
        }
        JmsTemplate template = clone(jmsTemplate, property);
        template.send(mqTopic, session -> {
            Message message = template.getMessageConverter().toMessage(messageOut, session);
            if (handler != null) {
                handler.setPropertyOut(message);
            }
            logMessage(destination, "publishTopic", message, true);
            return message;
        });
    }

    /**
     * 接收Topic訊息
     *
     * @param jmsTemplate
     * @param messageInConverter
     * @param destination
     * @param property
     * @param handler
     * @param <MessageIn>
     * @return
     * @throws JMSException
     */
    @SuppressWarnings("unchecked")
    public static <MessageIn extends Serializable> MessageIn receiveTopic(JmsTemplate jmsTemplate, MessageConverter messageInConverter, String destination, JmsProperty property, JmsHandler handler) throws JMSException {
        MQTopic mqTopic = new MQTopic(destination);
        setJmsPropertyContextProperty(mqTopic);
        if (handler != null) {
            handler.setPropertyOut(mqTopic);
        }
        JmsTemplate template = clone(jmsTemplate, property);
        Message message = null;
        // Has MessageSelector
        if (property != null && StringUtils.isNotBlank(property.getMessageSelector())) {
            message = template.receiveSelected(mqTopic, property.getMessageSelector());
        } else {
            message = template.receive(mqTopic);
        }
        logMessage(destination, "receiveTopic", message, false);
        if (handler != null) {
            handler.getPropertyIn(message);
        }
        return message == null ? null : (MessageIn) messageInConverter.fromMessage(message);
    }

    /**
     * 開始Queue/Topic接收
     *
     * @param registry
     * @param id
     * @return
     */
    public static boolean startReceive(JmsListenerEndpointRegistry registry, String id) {
        MessageListenerContainer container = registry.getListenerContainer(id);
        if (container != null && !container.isRunning()) {
            logger.info("Start to Receive Message for id = [", id, "]");
            container.start();
            return true;
        }
        return false;
    }

    /**
     * 停止Queue/Topic接收
     *
     * @param registry
     * @param id
     * @return
     */
    public static boolean stopReceive(JmsListenerEndpointRegistry registry, String id) {
        try {
            MessageListenerContainer container = registry.getListenerContainer(id);
            if (container != null && container.isRunning()) {
                logger.info("Stop to Receive Message for id = [", id, "]");
                container.stop();
                return true;
            }
        } catch (Exception e) {
            logger.error(e, "Stop to Receive Message for id = [", id, "] with exception occur, ", e.getMessage());
        }
        return false;
    }

    /**
     * 關閉線程池, 等待線程池積壓消息處理
     *
     * @param registry
     * @param id
     * @return
     */
    public static boolean shutdown(JmsListenerEndpointRegistry registry, String id) {
        try {
            AbstractMessageListenerContainer container = (AbstractMessageListenerContainer) registry.getListenerContainer(id);
            if (container != null && container.isActive()) {
                logger.info("Shutdown JMS Message Container for id = [", id, "]");
                container.shutdown();
                return true;
            }
        } catch (Exception e) {
            logger.error(e, "Shutdown JMS Message Container for id = [", id, "] with exception occur, ", e.getMessage());
        }
        return false;
    }

    /**
     * 註冊JmsListenerEndpoint
     *
     * @param registry
     * @param factory
     * @param jmsDefinition
     * @param notifier
     * @param <T>
     * @param <R>
     * @throws Exception
     */
    public static <T extends Serializable, R extends JmsReceiver<T>> void registerJmsListenerEndpoint(JmsListenerEndpointRegistry registry, DefaultJmsListenerContainerFactory factory, JmsDefinition jmsDefinition, JmsNotifier<T, R> notifier) throws Exception {
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId(jmsDefinition.getIdentity());
        endpoint.setDestination(jmsDefinition.getDestination());
        if (StringUtils.isNotBlank(jmsDefinition.getConcurrency())) {
            endpoint.setConcurrency(jmsDefinition.getConcurrency());
        }
        endpoint.setMessageListener(message -> {
            try {
                notifier.messageReceived(message);
            } catch (Exception e) {
                logger.error(e, "Receive Message from jmsDefinition = ", jmsDefinition.toString(), " with exception occur, ", e.getMessage());
            }
        });
        try {
            registry.registerListenerContainer(endpoint, factory, true);
        } catch (Exception e) {
            String errorMessage = StringUtils.join("Register Listener Container jmsDefinition = ", jmsDefinition.toString(), " with exception occur, ", e.getMessage());
            logger.error(e, errorMessage);
            throw ExceptionUtil.createException(e, errorMessage);
        }
    }

    /**
     * 註銷JmsListenerEndpoint
     *
     * @param registry
     * @param id
     * @throws Exception
     */
    public static void unregisterJmsListenerEndpoint(JmsListenerEndpointRegistry registry, String id) throws Exception {
        stopReceive(registry, id);
        shutdown(registry, id);
    }

    /**
     * 取ReplyToQueueName
     *
     * @param message
     * @return
     * @throws JMSException
     */
    public static String getReplyTo(Message message) throws JMSException {
        Destination destination = message.getJMSReplyTo();
        if (destination != null && destination instanceof MQQueue)
            return ((MQQueue) destination).getQueueURI();
        return null;
    }

    /**
     * 取ReplyToQueueManagerName
     *
     * @param message
     * @return
     * @throws JMSException
     */
    public static String getReplyToQueueManagerName(Message message) throws JMSException {
        Destination destination = message.getJMSReplyTo();
        if (destination != null && destination instanceof MQQueue)
            return ((MQQueue) destination).getBaseQueueManagerName();
        return null;
    }

    /**
     * 取ReplyToQueueName
     *
     * @param message
     * @return
     * @throws JMSException
     */
    public static String getReplyToQueueName(Message message) throws JMSException {
        Destination destination = message.getJMSReplyTo();
        if (destination != null && destination instanceof MQQueue)
            return ((MQQueue) destination).getBaseQueueName();
        return null;
    }

    /**
     * 取消息發送的日期時間
     *
     * @param message
     * @return
     * @throws JMSException
     */
    public static Calendar getTimestamp(Message message) throws JMSException {
        return CalendarUtil.clone(message.getJMSTimestamp());
    }

    /**
     * 設定CharacterSet, 發送消息時必須是數字編碼字符集標識
     *
     * @param message
     * @param characterSet
     * @throws JMSException
     */
    public static void setCharacterSet(Message message, int characterSet) throws JMSException {
        message.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, characterSet);
    }

    /**
     * 從收進來的Message中取CharacterSet, 接收消息時, JMS_IBM_CHARACTER_SET是Java Charset代碼頁名稱
     *
     * @param message
     * @return
     * @throws JMSException
     */
    public static String getCharacterSet(Message message) throws JMSException {
        String characterSet = message.getStringProperty(WMQConstants.JMS_IBM_CHARACTER_SET);
        // 正常情況下, 送出的Message設定CharacterSet應該要塞入CCSID int值, 所以如果是從送出的Message中取, 這裡要判斷轉換一下
        if (StringUtils.isNumeric(characterSet)) {
            try {
                characterSet = CCSID.getCodepage(Integer.parseInt(characterSet));
            } catch (NumberFormatException | UnsupportedEncodingException e) {
                logger.warn(e, "Unsupported Encoding = [", characterSet, "]");
                characterSet = StandardCharsets.UTF_8.name();
            }
        }
        return characterSet;
    }

    /**
     * 從收進來的Message中取CharacterSet的CCSID int值
     *
     * @param message
     * @return
     * @throws JMSException
     * @throws UnsupportedEncodingException
     */
    public static int getCharacterSetAsInt(Message message) throws JMSException, UnsupportedEncodingException {
        // codePage是Java Charset代碼頁名稱
        String codePage = getCharacterSet(message);
        return CCSID.getCCSID(codePage);
    }

    /**
     * 設定MessageId
     *
     * @param message
     * @param messageId
     * @throws JMSException
     */
    public static void setMessageId(Message message, String messageId) throws JMSException {
        setMessageId(message, messageId, null);
    }

    /**
     * 設定MessageId
     *
     * @param message
     * @param messageId
     * @param characterSet
     * @throws JMSException
     */
    public static void setMessageId(Message message, String messageId, String characterSet) throws JMSException {
        if (StringUtils.isBlank(characterSet)) {
            characterSet = getCharacterSet(message);
        }
        byte[] bytes = null;
        try {
            bytes = messageId.getBytes(characterSet);
        } catch (UnsupportedEncodingException e) {
            logger.warn(e, "Unsupported Encoding = [", characterSet, "]");
            bytes = messageId.getBytes();
        }
        setMessageId(message, bytes);
    }

    /**
     * 設定MessageId
     *
     * @param message
     * @param messageId
     * @throws JMSException
     */
    public static void setMessageId(Message message, byte[] messageId) throws JMSException {
        message.setObjectProperty(JmsConstants.JMS_IBM_MQMD_MSGID, messageId);
    }

    /**
     * 取MessageId的byte[]值
     *
     * @param message
     * @return
     * @throws JMSException
     */
    public static byte[] getMessageIdAsBytes(Message message) throws JMSException {
        String messageId = message.getJMSMessageID();
        if (StringUtils.isNotBlank(messageId)) {
            if (messageId.startsWith("ID:")) {
                return ConvertUtil.hexToBytes(messageId.substring(3).toUpperCase());
            }
            String characterSet = getCharacterSet(message);
            try {
                return messageId.getBytes(characterSet);
            } catch (UnsupportedEncodingException e) {
                logger.warn(e, "Unsupported Encoding = [", characterSet, "]");
            }
            return messageId.getBytes();
        }
        return new byte[0];
    }

    /**
     * 取MessageId
     *
     * @param message
     * @return
     * @throws JMSException
     * @throws DecoderException
     */
    public static String getMessageId(Message message) throws JMSException, DecoderException {
        return getMessageId(message, null);
    }

    /**
     * 根據指定的characterSet取MessageId
     *
     * @param message
     * @param characterSet
     * @return
     * @throws JMSException
     * @throws DecoderException
     */
    public static String getMessageId(Message message, String characterSet) throws JMSException, DecoderException {
        String messageId = message.getJMSMessageID();
        if (StringUtils.isNotBlank(messageId) && messageId.startsWith("ID:")) {
            if (StringUtils.isBlank(characterSet)) {
                characterSet = getCharacterSet(message);
            }
            return StringUtil.fromHex(messageId.substring(3), Charset.forName(characterSet), false);
        }
        return messageId;
    }

    /**
     * 設定CorrelationID
     *
     * @param message
     * @param correlationID
     * @throws JMSException
     */
    public static void setCorrelationID(Message message, String correlationID) throws JMSException {
        message.setJMSCorrelationID(correlationID);
    }

    /**
     * 設定CorrelationID
     *
     * @param message
     * @param correlationID
     * @throws JMSException
     */
    public static void setCorrelationID(Message message, byte[] correlationID) throws JMSException {
        message.setObjectProperty(JmsConstants.JMS_IBM_MQMD_CORRELID, correlationID);
    }

    /**
     * 取CorrelationID的byte[]值
     *
     * @param message
     * @return
     * @throws JMSException
     */
    public static byte[] getCorrelationIDAsBytes(Message message) throws JMSException {
        return message.getJMSCorrelationIDAsBytes();
    }

    /**
     * 取CorrelationID
     *
     * @param message
     * @return
     * @throws JMSException
     * @throws DecoderException
     */
    public static String getCorrelationID(Message message) throws JMSException, DecoderException {
        return getCorrelationID(message, null);
    }

    /**
     * 根據指定的characterSet取CorrelationID
     *
     * @param message
     * @param characterSet
     * @return
     * @throws JMSException
     * @throws DecoderException
     */
    public static String getCorrelationID(Message message, String characterSet) throws JMSException, DecoderException {
        String correlationID = message.getJMSCorrelationID();
        if (StringUtils.isNotBlank(correlationID) && correlationID.startsWith("ID:")) {
            if (StringUtils.isBlank(characterSet)) {
                characterSet = getCharacterSet(message);
            }
            return StringUtil.fromHex(correlationID.substring(3), Charset.forName(characterSet), false);
        }
        return correlationID;
    }

    /**
     * 根據MessageID獲取selector
     *
     * @param selectorValue
     * @param characterSet
     * @return
     */
    public static String getSelectorForMessageID(String selectorValue, String characterSet) {
        if (StringUtils.isNotBlank(selectorValue)) {
            StringBuilder sb = new StringBuilder();
            sb.append("JMSMessageID='").append(selectorValue).append("'")
                    .append(" or JMSMessageID='ID:").append(StringUtils.rightPad(StringUtil.toHex(selectorValue, Charset.forName(characterSet)), 48, '0')).append("'");
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 根據CorrelationID獲取selector
     *
     * @param selectorValue
     * @param characterSet
     * @return
     */
    public static String getSelectorForCorrelationID(String selectorValue, String characterSet) {
        if (StringUtils.isNotBlank(selectorValue)) {
            StringBuilder sb = new StringBuilder();
            sb.append("JMSCorrelationID='").append(selectorValue).append("'")
                    .append(" or JMSCorrelationID='ID:").append(StringUtils.rightPad(StringUtil.toHex(selectorValue, Charset.forName(characterSet)), 48, '0')).append("'");
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 列印Message
     *
     * @param destination
     * @param methodName
     * @param message
     */
    public static void logMessage(String destination, String methodName, Message message, boolean sendOut) {
        if (!messageLogEnable()) return;
        if (message == null) return;
        try {
            logger.debug("[", CLASS_NAME, "][", destination, "][", methodName, "]", sendOut ? ">>>>>>>>" : "<<<<<<<<", Message.class.getName(), " : ", message);
        } catch (Throwable t) {
            logger.warn(t, t.getMessage());
        }
    }

    /**
     * 是否列印Message
     *
     * @return
     */
    public static boolean messageLogEnable() {
        return Boolean.parseBoolean(EnvPropertiesUtil.getProperty(SYSTEM_KEY_MESSAGE_LOG_ENABLE, Boolean.FALSE.toString()));
    }

    /**
     * 塞入Encoding
     *
     * @param message
     * @param encoding
     * @throws JMSException
     */
    public static void setEncoding(Message message, int encoding) throws JMSException {
        message.setIntProperty(JmsConstants.JMS_IBM_ENCODING, encoding);
    }

    /**
     * 獲取Encoding
     *
     * @param message
     * @return
     * @throws JMSException
     */
    public static int getEncoding(Message message) throws JMSException {
        return message.getIntProperty(JmsConstants.JMS_IBM_ENCODING);
    }
}
