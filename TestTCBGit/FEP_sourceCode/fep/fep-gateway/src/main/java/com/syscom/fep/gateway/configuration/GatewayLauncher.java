package com.syscom.fep.gateway.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GatewayLauncher {
    @Autowired
    private GatewayConfiguration configuration;
    @Autowired
    private GatewayManager manager;

    public void launch() {
        configuration.registerBean();
        manager.establishAllConnection();
    }
}
