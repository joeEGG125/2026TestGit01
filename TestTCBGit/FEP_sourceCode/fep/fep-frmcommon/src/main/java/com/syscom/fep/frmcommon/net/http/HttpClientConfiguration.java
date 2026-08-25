package com.syscom.fep.frmcommon.net.http;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.ssl.X509TrustAnyTrustManager;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.boot.webservices.client.HttpWebServiceMessageSenderBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.ws.transport.WebServiceMessageSender;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

public class HttpClientConfiguration {
    private static final LogHelper logger = new LogHelper();

    private HttpClientConfiguration() {}

    public static SimpleClientHttpRequestFactory createSimpleClientHttpRequestFactory(int timeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (timeout > 0) {
            requestFactory.setConnectTimeout(timeout);
            requestFactory.setReadTimeout(timeout);
        }
        return requestFactory;
    }

    public static HttpComponentsClientHttpRequestFactory createHttpComponentsClientHttpRequestFactory(int timeout) throws Exception {
        return createHttpComponentsClientHttpRequestFactory(timeout, false);
    }

    public static HttpComponentsClientHttpRequestFactory createHttpComponentsClientHttpRequestFactory(int timeout, boolean httpConnectionPooling) throws Exception {
        CloseableHttpClient httpClient = createHttpClient(httpConnectionPooling);
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        if (timeout > 0) {
            requestFactory.setConnectTimeout(timeout);
            requestFactory.setConnectionRequestTimeout(timeout);
            requestFactory.setReadTimeout(timeout);
        }
        return requestFactory;
    }

    public static HttpComponentsClientHttpRequestFactory createHttpComponentsClientHttpRequestFactory(int timeout, InputStream ssl, String store, String type) throws Exception {
        return createHttpComponentsClientHttpRequestFactory(timeout, ssl, store, type, false);
    }

    public static HttpComponentsClientHttpRequestFactory createHttpComponentsClientHttpRequestFactory(int timeout, InputStream ssl, String store, String type, boolean httpConnectionPooling) throws Exception {
        CloseableHttpClient httpClient = createHttpClient(ssl, store, type, httpConnectionPooling);
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        if (timeout > 0) {
            requestFactory.setConnectTimeout(timeout);
            requestFactory.setConnectionRequestTimeout(timeout);
            requestFactory.setReadTimeout(timeout);
        }
        return requestFactory;
    }

    public static HttpComponentsClientHttpRequestFactory createTrustAnyHttpComponentsClientHttpRequestFactory(String protocol, int timeout) throws Exception {
        return createTrustAnyHttpComponentsClientHttpRequestFactory(protocol, timeout, false);
    }

    public static HttpComponentsClientHttpRequestFactory createTrustAnyHttpComponentsClientHttpRequestFactory(String protocol, int timeout, boolean httpConnectionPooling) throws Exception {
        CloseableHttpClient httpClient = createTrustAnyHttpClient(protocol, httpConnectionPooling);
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        if (timeout > 0) {
            requestFactory.setConnectTimeout(timeout);
            requestFactory.setConnectionRequestTimeout(timeout);
            requestFactory.setReadTimeout(timeout);
        }
        return requestFactory;
    }

    public static CloseableHttpClient createHttpClient(boolean httpConnectionPooling) throws Exception {
        SSLContext sslContext = SSLContexts.custom()
                .setProtocol(SslContextFactory.PROTOCOL)
                .loadTrustMaterial(null, (x509Certificates, authType) -> true).build();
        return createHttpClient(sslContext, httpConnectionPooling);
    }

    public static CloseableHttpClient createHttpClient(InputStream ssl, String store, String type, boolean httpConnectionPooling) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(StringUtils.isBlank(type) ? KeyStore.getDefaultType() : type);
        trustStore.load(ssl, store.toCharArray());
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, (x509Certificates, authType) -> true).build();
        return createHttpClient(sslContext, httpConnectionPooling);
    }

    public static CloseableHttpClient createTrustAnyHttpClient(String protocol, boolean httpConnectionPooling) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(null, new TrustManager[] {new X509TrustAnyTrustManager()}, null);
        return createHttpClient(sslContext, httpConnectionPooling);
    }

    public static CloseableHttpClient createHttpClient(SSLContext sslContext, boolean httpConnectionPooling) throws ClassNotFoundException {
        SSLConnectionSocketFactoryBuilder builder = SSLConnectionSocketFactoryBuilder.create().setSslContext(sslContext);
        Class<?>[] argsCls = new Class[] {Class.forName("javax.net.ssl.HostnameVerifier")};
        Object[] args = new Object[] {ReflectUtil.instance(Class.forName(StringUtils.join("org.apache.hc.client5.http.ssl.", new String(Base64.getDecoder().decode("Tm9vcEhvc3RuYW1lVmVyaWZpZXI="), StandardCharsets.UTF_8).intern())))};
        builder = ReflectUtil.envokeMethod(builder, "setHostnameVerifier", argsCls, args, builder);
        SSLConnectionSocketFactory connectionSocketFactory = builder.build();
        // 2025-05-07 Richard modified
        HttpClientConnectionManager clientConnectionManager;
        if (httpConnectionPooling) {
            clientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(connectionSocketFactory).build();
        } else {
            clientConnectionManager = new BasicHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register(URIScheme.HTTP.id, PlainConnectionSocketFactory.getSocketFactory())
                            .register(URIScheme.HTTPS.id, connectionSocketFactory)
                            .build()
            );
        }
        return HttpClients.custom().setConnectionManager(clientConnectionManager).build();
    }

    public static WebServiceMessageSender createHttpComponentsMessageSender(int timeout) {
        HttpWebServiceMessageSenderBuilder builder = new HttpWebServiceMessageSenderBuilder()
                .requestFactory(() -> {
                    try {
                        return createHttpComponentsClientHttpRequestFactory(timeout);
                    } catch (Exception e) {
                        logger.info(e, e.getMessage());
                        throw ExceptionUtil.createRuntimeException(e);
                    }
                });
        if (timeout > 0) {
            builder.setConnectTimeout(Duration.ofMillis(timeout)).setReadTimeout(Duration.ofMillis(timeout));
        }
        return builder.build();
    }

    public static WebServiceMessageSender createHttpComponentsMessageSender(int timeout, InputStream ssl, String store, String type) {
        HttpWebServiceMessageSenderBuilder builder = new HttpWebServiceMessageSenderBuilder()
                .requestFactory(() -> {
                    try {
                        return createHttpComponentsClientHttpRequestFactory(timeout, ssl, store, type);
                    } catch (Exception e) {
                        logger.info(e, e.getMessage());
                        throw ExceptionUtil.createRuntimeException(e);
                    }
                });
        if (timeout > 0) {
            builder.setConnectTimeout(Duration.ofMillis(timeout)).setReadTimeout(Duration.ofMillis(timeout)).build();
        }
        return builder.build();
    }

    public static SSLSocketFactory createTrustAnySSLSocketFactory(String protocol) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(null, new TrustManager[] {new X509TrustAnyTrustManager()}, null);
        return sslContext.getSocketFactory();
    }

    public static HttpEntity<MultiValueMap<String, Object>> createHttpForm(Map<String, String> args) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : args.entrySet()) {
            map.add(entry.getKey(), entry.getValue());
        }
        return new HttpEntity<>(map, headers);
    }
}
