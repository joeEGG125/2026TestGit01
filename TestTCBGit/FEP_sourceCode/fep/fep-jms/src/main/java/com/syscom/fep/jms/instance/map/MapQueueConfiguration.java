package com.syscom.fep.jms.instance.map;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsConfigurationProperties;
import com.syscom.fep.frmcommon.jms.JmsDefinition;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.GenericTypeUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsQueueNames;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class MapQueueConfiguration<Key, ConfigurationProperties extends MapQueueConfigurationProperties<Key>> {
    @Autowired
    private JmsListenerEndpointRegistry registry;
    @Autowired
    private JmsMsgConfiguration jmsMsgConfiguration;
    private List<ConfigurationProperties> prop;
    private int debug = -1; // 如果是-1, 則根據Key取, >=0則取prop[debug]
    private final List<ConnectionFactory> connectionFactories = new ArrayList<>();
    private final Map<Key, MapQueueOperator<Key, ConfigurationProperties>> mapQueueOperatorMap = new HashMap<>();
    private final Class<Key> keyGenericClass = GenericTypeUtil.getGenericSuperClass(this.getClass(), 0);

    public List<ConfigurationProperties> getProp() {
        return prop;
    }

    public void setProp(List<ConfigurationProperties> prop) {
        this.prop = prop;
    }

    public int getDebug() {
        return debug;
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }

    /**
     * 獲取實例名稱
     *
     * @return
     */
    protected abstract String getInstanceName();

    /**
     * 獲取配置檔key前綴
     *
     * @return
     */
    protected abstract String getConfigurationPropertiesPrefix();

    /**
     * 列印配置檔key
     */
    protected abstract void printConstant();

    /**
     * 根據Key獲取JmsOperator實例
     *
     * @param key
     * @return
     */
    public final MapQueueOperator<Key, ConfigurationProperties> getMapQueueOperator(final Key key) {
        // 如果配置檔中只設定了一筆, 則預設就取第一筆, 不用map取值
        if (this.prop.size() == 1)
            return mapQueueOperatorMap.values().iterator().next();
        else if (this.debug < 0 || this.debug >= this.prop.size())
            return this.getMapQueueOperator(this.mapQueueOperatorMap, key);
        ConfigurationProperties properties = this.prop.get(this.debug);
        return this.mapQueueOperatorMap.get(properties.getKey());
    }

    /**
     * 依據Key從Map中獲取Operator, 子類可以根據實際情況覆寫這個方法
     *
     * @param mapQueueOperatorMap
     * @param key
     * @return
     */
    protected MapQueueOperator<Key, ConfigurationProperties> getMapQueueOperator(final Map<Key, MapQueueOperator<Key, ConfigurationProperties>> mapQueueOperatorMap, final Key key) {
        return this.getMapQueueOperator(mapQueueOperatorMap, key, t -> t.equals(key));
    }

    /**
     * 依據Key從Map中獲取Operator
     *
     * @param mapQueueOperatorMap
     * @param key
     * @return
     */
    protected final MapQueueOperator<Key, ConfigurationProperties> getMapQueueOperator(final Map<Key, MapQueueOperator<Key, ConfigurationProperties>> mapQueueOperatorMap, final Key key, final Predicate<Key> predicate) {
        // 如果配置檔中只設定了一筆, 則預設就取第一筆, 不用map取值
        if (this.prop.size() == 1)
            return mapQueueOperatorMap.values().iterator().next();
        else if (key != null) {
            if (predicate != null) {
                Key found = mapQueueOperatorMap.keySet().stream().filter(predicate).findFirst().orElse(null);
                return found == null ? null : mapQueueOperatorMap.get(found);
            }
            return mapQueueOperatorMap.get(key);
        }
        return null;
    }

    /**
     * 根據Key依據function獲取JmsDefinition
     *
     * @param key
     * @param function
     * @return
     */
    protected final JmsDefinition getQueueDefinition(final Key key, final Function<JmsQueueNames, JmsDefinition> function) {
        JmsDefinition definition = null;
        if (this.prop.size() == 1) {
            String queueName = disassemble(key);
            if (StringUtils.isNotBlank(queueName)) {
                definition = new JmsDefinition();
                definition.setDestination(queueName);
                return definition;
            }
        }
        MapQueueOperator<Key, ConfigurationProperties> operator = this.getMapQueueOperator(key);
        if (operator != null) {
            ConfigurationProperties configurationProperties = operator.getConfigurationProperties();
            if (configurationProperties != null) {
                JmsQueueNames queueNames = configurationProperties.getQueueNames();
                if (queueNames != null) {
                    definition = function.apply(queueNames);
                }
            }
        }
        if (definition == null) {
            definition = function.apply(jmsMsgConfiguration.getQueueNames());
        }
        // 2025-08-26 Richard modified 如果getDestination是空白, 則返回null
        return StringUtils.isBlank(definition.getDestination()) ? null : definition;
    }

    /**
     * 拆解出QueueName,
     * queue://QMAAIT01/FIDO1.FEP.ONLINE.RS?targetClient=1
     *
     * @param key
     * @return
     */
    protected String disassemble(Key key) {
        if (key != null) {
            String str = key.toString();
            String prefix = "queue://";
            if (str.startsWith(prefix)) {
                int beginIndex = prefix.length();
                beginIndex = str.indexOf("/", beginIndex) + 1;
                int endIndex = str.indexOf("?", beginIndex);
                endIndex = endIndex == -1 ? str.length() : endIndex;
                try {
                    return str.substring(beginIndex, endIndex);
                } catch (IndexOutOfBoundsException e) {
                    LogHelperFactory.getGeneralLogger().warn(e, "disassemble failed, Key:", str, ", ", e.getMessage());
                }
            }
            return str;
        }
        return null;
    }

    /**
     * 定義MessageConverter實例
     *
     * @return
     */
    protected MessageConverter messageConverter() {
        return JmsFactory.createSimpleMessageConverter();
    }

    /**
     * 根據Key創建JmsOperator的Map
     *
     * @param messageConverter
     * @return
     * @throws JMSException
     */
    protected Map<Key, MapQueueOperator<Key, ConfigurationProperties>> mapQueueOperatorMap(MessageConverter messageConverter) throws JMSException {
        if (!this.mapQueueOperatorMap.isEmpty())
            return this.mapQueueOperatorMap;
        for (ConfigurationProperties properties : this.prop) {
            ConnectionFactory connectionFactory = this.connectionFactory(properties);
            JmsTemplate jmsTemplate = JmsFactory.createJmsTemplate(connectionFactory, messageConverter, properties.getQos());
            PlatformTransactionManager transactionManager = JmsFactory.createPlatformTransactionManager(connectionFactory);
            Key key = properties.getKey();
            // 如果key為null, 則new一個塞進去
            if (key == null)
                key = ReflectUtil.instance(keyGenericClass);
            this.mapQueueOperatorMap.put(key, new MapQueueOperator<>(registry, properties, jmsTemplate, messageConverter, transactionManager));
        }
        return this.mapQueueOperatorMap;
    }

    /**
     * 創建JMS工長類
     *
     * @param prop
     * @return
     * @throws JMSException
     */
    private ConnectionFactory connectionFactory(JmsConfigurationProperties prop) throws JMSException {
        ConnectionFactory connectionFactory;
        if (prop.getCache() != null && prop.getCache().isEnabled()) {
            connectionFactory = cachingJmsConnectionFactory(prop);
        } else {
            connectionFactory = jmsConnectionFactory(prop);
        }
        this.connectionFactories.add(connectionFactory);
        return connectionFactory;
    }

    private ConnectionFactory jmsConnectionFactory(JmsConfigurationProperties prop) throws JMSException {
        String instanceName = this.getInstanceName();
        LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, instanceName.toUpperCase(), " single IBM MQ ConnectionFactory start...");
        try {
            ConnectionFactory connectionFactory = JmsFactory.createMQConnectionFactory(prop);
            return JmsFactory.createUserCredentialsConnectionFactoryAdapter(connectionFactory, prop);
        } finally {
            LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, instanceName.toUpperCase(), " single IBM MQ ConnectionFactory finish");
        }
    }

    private CachingConnectionFactory cachingJmsConnectionFactory(JmsConfigurationProperties prop) throws JMSException {
        String instanceName = this.getInstanceName();
        LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, instanceName.toUpperCase(), " caching IBM MQ ConnectionFactory start...");
        try {
            ConnectionFactory connectionFactory = JmsFactory.createMQConnectionFactory(prop);
            UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = JmsFactory.createUserCredentialsConnectionFactoryAdapter(connectionFactory, prop);
            return JmsFactory.createCachingConnectionFactory(userCredentialsConnectionFactoryAdapter, prop);
        } finally {
            LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, instanceName.toUpperCase(), " caching IBM MQ ConnectionFactory finish");
        }
    }

    @PostConstruct
    public void print() {
        String instanceName = this.getInstanceName();
        printConstant();
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, StringUtils.join(instanceName.toUpperCase(), " Queue Configuration"), true,
                "registry", "jmsMsgConfiguration", "prop", "connectionFactories", "mapQueueOperatorMap", "keyGenericClass"));
        int index = 0;
        for (JmsConfigurationProperties properties : this.prop) {
            LogHelperFactory.getGeneralLogger().info(properties.toString(instanceName + "[" + index + "]", this.getConfigurationPropertiesPrefix() + ".prop[" + index++ + "]"));
        }
    }

    @PreDestroy
    public void destroy() {
        String instanceName = this.getInstanceName();
        if (!this.connectionFactories.isEmpty()) {
            int index = 0;
            for (ConnectionFactory connectionFactory : this.connectionFactories)
                JmsFactory.closeConnection(instanceName.toUpperCase() + "[" + index++ + "]", connectionFactory);
        }
    }

//    public static void main(String[] args) {
//        MapQueueConfiguration<String, MapQueueConfigurationProperties<String>> configuration = new MapQueueConfiguration<>() {
//            @Override
//            protected String getInstanceName() {
//                return "";
//            }
//
//            @Override
//            protected String getConfigurationPropertiesPrefix() {
//                return "";
//            }
//
//            @Override
//            protected void printConstant() {}
//        };
//        System.out.println(configuration.disassemble("queue://QMAAIT01/FIDO1.FEP.ONLINE.RS?targetClient=1"));
//        System.out.println(configuration.disassemble("queue://QMAAIT01/FIDO1.FEP.ONLINE.RS"));
//        System.out.println(configuration.disassemble("FIDO1.FEP.ONLINE.RS"));
//    }
}
