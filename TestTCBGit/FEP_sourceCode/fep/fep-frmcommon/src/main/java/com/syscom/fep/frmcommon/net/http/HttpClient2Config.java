package com.syscom.fep.frmcommon.net.http;

import lombok.Data;

import java.nio.charset.StandardCharsets;

@Data
public class HttpClient2Config {
    private int responseTimeout = 30000;
    private int connectionKeepAlive = 30000;
    private int connectTimeout = 30000;
    private int socketTimeout = 30000;
    private int timeToLive = 600000;
    private int validateAfterInactivity = 60000;
    private int readTimeout = 30000;
    private int connectionRequestTimeout = 30000;
    private int maxConnTotal = 200;
    private int maxConnPerRoute = 50;
    private boolean evictExpiredConnections = true;
    private long maxIdleTime = 600000L;
    private String charset = StandardCharsets.UTF_8.name();
    private boolean recordLog = true;
    private long responseLengthLimit;
    private boolean exceptionLogging;
    private boolean sslIgnore;
    private boolean sslTrustAny = true;
    private String sslPath;
    private String sslStore;
    private String sslType;
    private String sslProtocol = "TLSv1.2";
}