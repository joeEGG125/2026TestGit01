package com.syscom.fep.common.http;

public interface HttpClientConfigureConstant {
    // for Monitor
    String TYPE_MONITOR = "monitor";
    String CONFIGURATION_PREFIX_MONITOR = "spring.fep.http-client." + TYPE_MONITOR;
    String BEAN_NAME_CONFIG_MONITOR = TYPE_MONITOR + "HttpClientConfig";
    String BEAN_NAME_MONITOR = TYPE_MONITOR + "HttpClient";
    // for Notify
    String TYPE_NOTIFY = "notify";
    String CONFIGURATION_PREFIX_NOTIFY = "spring.fep.http-client." + TYPE_NOTIFY;
    String BEAN_NAME_CONFIG_NOTIFY = TYPE_NOTIFY + "HttpClientConfig";
    String BEAN_NAME_NOTIFY = TYPE_NOTIFY + "HttpClient";
}
