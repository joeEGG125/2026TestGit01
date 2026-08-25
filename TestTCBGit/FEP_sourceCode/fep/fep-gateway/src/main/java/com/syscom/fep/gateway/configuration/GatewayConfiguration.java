package com.syscom.fep.gateway.configuration;

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;

// @Validated
@Configuration
@ConfigurationProperties(prefix = "spring.fep.gateway")
// @RefreshScope
public class GatewayConfiguration {
    private List<GatewayRegister> register;

    public void registerBean() {
        if (CollectionUtils.isNotEmpty(register)) {
            // 註冊Gateway相關類到Spring容器中
            for (GatewayRegister gatewayRegister : register) {
                // Configuration, 必須
                SpringBeanFactoryUtil.registerBean(gatewayRegister.getCnfClassName());
                // Processor, Client必須, Server動態多實例化註冊
                if (StringUtils.isNotBlank(gatewayRegister.getPrcClassName())) {
                    SpringBeanFactoryUtil.registerBean(gatewayRegister.getPrcClassName());
                }
                // ProcessorManager, 可選
                if (StringUtils.isNotBlank(gatewayRegister.getMgrClassName())) {
                    SpringBeanFactoryUtil.registerBean(gatewayRegister.getMgrClassName());
                }
                // HandlerAdapter, 必須
                SpringBeanFactoryUtil.registerBean(gatewayRegister.getHdrClassName());
                // IP過濾規則, 可選
                if (StringUtils.isNotBlank(gatewayRegister.getIpfClassName())) {
                    SpringBeanFactoryUtil.registerBean(gatewayRegister.getIpfClassName());
                }
                SpringBeanFactoryUtil.registerBean(gatewayRegister.getGtwClassName());
            }
        }
    }

    public void setRegister(List<GatewayRegister> register) {
        this.register = register;
    }
}
