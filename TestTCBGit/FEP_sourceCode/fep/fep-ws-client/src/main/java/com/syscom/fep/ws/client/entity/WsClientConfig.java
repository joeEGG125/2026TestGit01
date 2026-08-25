package com.syscom.fep.ws.client.entity;

import org.apache.commons.lang3.StringUtils;

public class WsClientConfig {
    private WsClientType clientType;
    private String sslKeyPath;
    private String sslKeyStore;
    private String sslKeyType;
    private String uri;
    private boolean https;
    private int timeout;

    public WsClientType getClientType() {
        return clientType;
    }

    public void setClientType(WsClientType clientType) {
        this.clientType = clientType;
    }

    public String getSslKeyPath() {
        return sslKeyPath;
    }

    public void setSslKeyPath(String sslKeyPath) {
        this.sslKeyPath = sslKeyPath;
    }

    public String getSslKeyStore() {
        return sslKeyStore;
    }

    public void setSslKeyStore(String sslKeyStore) {
        this.sslKeyStore = sslKeyStore;
    }

    public String getSslKeyType() {
        return sslKeyType;
    }

    public void setSslKeyType(String sslKeyType) {
        this.sslKeyType = sslKeyType;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
        this.https = StringUtils.isNotBlank(uri) && uri.toLowerCase().startsWith("https");
    }

    public boolean isHttps() {
        return https;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
