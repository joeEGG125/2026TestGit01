package com.syscom.fep.jms.instance.map.eatm;

import com.syscom.fep.frmcommon.jms.JmsDefinition;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.jms.JmsQueueNames;
import com.syscom.fep.jms.instance.map.MapQueueConfiguration;
import com.syscom.fep.jms.instance.map.MapQueueOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.jms.JMSException;
import java.util.Map;

@Component
@EnableJms
// @Validated
@ConfigurationProperties(prefix = EATMQueueConstant.CONFIGURATION_PROPERTIES_PREFIX)
@ConditionalOnProperty(value = {
        EATMQueueConstant.CONFIGURATION_PROPERTIES_QUEUE_MANAGER,
        EATMQueueConstant.CONFIGURATION_PROPERTIES_CHANNEL,
        EATMQueueConstant.CONFIGURATION_PROPERTIES_CONNECTION_NAME,
        EATMQueueConstant.CONFIGURATION_PROPERTIES_USER,
        EATMQueueConstant.CONFIGURATION_PROPERTIES_PASSWORD})
// @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
public class EATMQueueConfiguration extends MapQueueConfiguration<String, EATMQueueConfigurationProperties> implements EATMQueueConstant {
    /**
     * 獲取實例名稱
     *
     * @return
     */
    @Override
    public String getInstanceName() {
        return INSTANCE_NAME;
    }

    /**
     * 獲取配置檔key前綴
     *
     * @return
     */
    @Override
    protected String getConfigurationPropertiesPrefix() {
        return CONFIGURATION_PROPERTIES_PREFIX;
    }

    /**
     * 列印配置檔key
     */
    @Override
    public void printConstant() {
        EATMQueueConstant.super.printConstant();
    }

    /**
     * 定義MessageConverter實例
     *
     * @return
     */
    @Override
    @Bean(name = MESSAGE_CONVERTER)
    public MessageConverter messageConverter() {
        return JmsFactory.createSimpleStringInMessageConverter();
    }

    /**
     * 根據Key創建JmsOperator的Map
     *
     * @param messageConverter
     * @return
     * @throws JMSException
     */
    @Override
    @Bean(name = JMS_OPERATOR_MAP)
    public Map<String, MapQueueOperator<String, EATMQueueConfigurationProperties>> mapQueueOperatorMap(@Qualifier(MESSAGE_CONVERTER) MessageConverter messageConverter) throws JMSException {
        return super.mapQueueOperatorMap(messageConverter);
    }

    /**
     * 獲取FidoAck Queue
     *
     * @param replyTo
     * @return
     */
    public JmsDefinition getEatmAck(String replyTo) {
        return this.getQueueDefinition(replyTo, JmsQueueNames::getEatmAck);
    }

    /**
     * 依據Key從Map中獲取Operator, 子類可以根據實際情況覆寫這個方法
     *
     * @param mapQueueOperatorMap
     * @param key
     * @return
     */
    @Override
    protected MapQueueOperator<String, EATMQueueConfigurationProperties> getMapQueueOperator(Map<String, MapQueueOperator<String, EATMQueueConfigurationProperties>> mapQueueOperatorMap, String key) {
        return super.getMapQueueOperator(mapQueueOperatorMap, key, key::contains);
    }
}