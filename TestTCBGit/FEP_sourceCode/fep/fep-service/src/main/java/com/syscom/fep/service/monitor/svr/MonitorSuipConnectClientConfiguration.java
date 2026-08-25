package com.syscom.fep.service.monitor.svr;

import com.syscom.fep.invoker.netty.SimpleNettyClientConfiguration;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.service.monitor.suip-connect-client")
// @RefreshScope
public class MonitorSuipConnectClientConfiguration extends SimpleNettyClientConfiguration {

    @Override
    public void print() {
        setClientWorkerThreadNum(5); // 這裡預設WorkThread數為5
        super.print();
    }
}
