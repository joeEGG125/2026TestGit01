package com.syscom.fep.service.svr;

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(prefix = SvrProcessorConstant.CONFIGURATION_PROPERTIES_PREFIX, name = {SvrProcessorConstant.CONFIGURATION_PROPERTIES_REGISTER_0_MAINCLASSNAME})
public class SvrProcessorLauncher {

    @PostConstruct
    public void launch() {
        SpringBeanFactoryUtil.registerBean(SvrProcessorManager.class);
        SpringBeanFactoryUtil.registerBean(SvrProcessorConfiguration.class).registerBean();
    }
}
