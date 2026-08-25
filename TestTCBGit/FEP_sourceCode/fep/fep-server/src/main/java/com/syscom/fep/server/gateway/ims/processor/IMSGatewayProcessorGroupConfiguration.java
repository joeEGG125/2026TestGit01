package com.syscom.fep.server.gateway.ims.processor;

import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.server.gateway.ims.IMSGatewayConfiguration;
import com.syscom.fep.server.gateway.ims.IMSGatewayLineType;
import com.syscom.fep.server.gateway.ims.IMSGatewayMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IMSGatewayProcessorGroupConfiguration {
    private IMSGatewayMode mode;
    /**
     * sender
     */
    @NestedConfigurationProperty()
    private IMSGatewayProcessorConfiguration sender = new IMSGatewayProcessorConfiguration();
    /**
     * receiver
     */
    @NestedConfigurationProperty()
    private IMSGatewayProcessorConfiguration receiver = new IMSGatewayProcessorConfiguration();
    /**
     * alternative sender
     */
    @NestedConfigurationProperty()
    private IMSGatewayProcessorConfiguration alternativeSender = new IMSGatewayProcessorConfiguration();
    /**
     * alternative receiver
     */
    @NestedConfigurationProperty()
    private IMSGatewayProcessorConfiguration alternativeReceiver = new IMSGatewayProcessorConfiguration();

    private List<IMSGatewayProcessorConfiguration> senderConfigurations;
    private List<IMSGatewayProcessorConfiguration> receiverConfigurations;

    /**
     * @return the mode
     */
    public IMSGatewayMode getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(IMSGatewayMode mode) {
        this.mode = mode;
    }

    public IMSGatewayProcessorConfiguration getSender() {
        return sender;
    }

    public IMSGatewayProcessorConfiguration getReceiver() {
        return receiver;
    }

    public IMSGatewayProcessorConfiguration getAlternativeSender() {
        return alternativeSender;
    }

    public IMSGatewayProcessorConfiguration getAlternativeReceiver() {
        return alternativeReceiver;
    }

    public List<IMSGatewayProcessorConfiguration> getSenderConfigurations() {
        if (senderConfigurations == null) {
            senderConfigurations = new ArrayList<>();
            this.initConfigurations(senderConfigurations, sender, true, IMSGatewayProcessorType.SENDER, IMSGatewayLineType.Primary, "sender");
            this.initConfigurations(senderConfigurations, alternativeSender, false, IMSGatewayProcessorType.SENDER, IMSGatewayLineType.Alternative, "alternative-sender");
        }
        return senderConfigurations;
    }

    public List<IMSGatewayProcessorConfiguration> getReceiverConfigurations() {
        if (receiverConfigurations == null) {
            receiverConfigurations = new ArrayList<>();
            this.initConfigurations(receiverConfigurations, receiver, true, IMSGatewayProcessorType.RECEIVER, IMSGatewayLineType.Primary, "receiver");
            this.initConfigurations(receiverConfigurations, alternativeReceiver, false, IMSGatewayProcessorType.RECEIVER, IMSGatewayLineType.Alternative, "alternative-receiver");
        }
        return receiverConfigurations;
    }

    private void initConfigurations(List<IMSGatewayProcessorConfiguration> configurations, IMSGatewayProcessorConfiguration configuration, boolean enable, IMSGatewayProcessorType processorType, IMSGatewayLineType lineType, String configurationPropertiesKey) {
        ConfigurationProperties configurationProperties = IMSGatewayConfiguration.class.getAnnotation(ConfigurationProperties.class);
        // 根據拆開的ClientId, 一個ClientId對應一個IMS實例
        List<String> clientIds = StringUtil.split(configuration.getClientId(), ',');
        for (String clientId : clientIds) {
            IMSGatewayProcessorConfiguration newConfiguration = new IMSGatewayProcessorConfiguration();
            BeanUtils.copyProperties(configuration, newConfiguration);
            newConfiguration.setConfigurationPropertiesPrefix(StringUtils.join(configurationProperties.prefix(), ".", mode.name(), ".", configurationPropertiesKey, "[", configurations.size(), "]"));
            newConfiguration.setThreadName(StringUtils.join(Arrays.asList("IMSGateway", mode.name().toUpperCase(), processorType.name(), clientId, String.format("(%s)", lineType.name())), "-"));
            newConfiguration.setProcessorType(processorType);
            newConfiguration.setMode(mode);
            newConfiguration.setEnable(enable);
            newConfiguration.setLineType(lineType);
            newConfiguration.setClientId(clientId); // 塞入拆解的Client Id
            configurations.add(newConfiguration);
        }
    }
}
