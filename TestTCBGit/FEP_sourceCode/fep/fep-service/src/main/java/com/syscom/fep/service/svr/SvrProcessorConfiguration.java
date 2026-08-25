package com.syscom.fep.service.svr;

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = SvrProcessorConstant.CONFIGURATION_PROPERTIES_PREFIX)
// @RefreshScope
public class SvrProcessorConfiguration {
    @Autowired
    private SvrProcessorManager manager;
    @NestedConfigurationProperty
    private List<SvrProcessorRegister> register = new ArrayList<>();

    public List<SvrProcessorRegister> getRegister() {
        return register;
    }

    void registerBean() {
        if (CollectionUtils.isNotEmpty(register)) {
            for (SvrProcessorRegister processorRegister : register) {
                manager.addProcessor(SpringBeanFactoryUtil.registerBean(processorRegister.getMainClassName()));
            }
            manager.runAllProcessors();
        }
    }
}
