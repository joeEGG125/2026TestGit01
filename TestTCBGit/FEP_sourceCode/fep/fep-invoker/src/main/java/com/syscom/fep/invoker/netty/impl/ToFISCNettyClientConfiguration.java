package com.syscom.fep.invoker.netty.impl;

import com.syscom.fep.invoker.netty.SimpleNettyClientConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.gateway.fisc")
// @RefreshScope
public class ToFISCNettyClientConfiguration extends SimpleNettyClientConfiguration {
    @Override
    public void print() {}

    public void printConfiguration() {
        super.print();
    }
}
