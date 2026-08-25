package com.syscom.fep.frmcommon.net.http;

import com.google.gson.Gson;
import com.syscom.fep.frmcommon.delegate.ExecuteListener2;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ssl.X509TrustAnyTrustManager;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.IOUtil;
import jakarta.annotation.PreDestroy;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class HttpClient2 {
    private static final String MESSAGE_IN = "<<<<<<<<<<";
    private static final String MESSAGE_OUT = ">>>>>>>>>>";
    private final Gson gson = new Gson();
    private final LogHelper logger = new LogHelper(HttpClient2.class);
    private final HttpClient2Config config;
    private final HttpClientConnectionManager httpClientConnectionManager;
    private final HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory;
    private final RestTemplate restTemplate;
    private final String beanName;

    public HttpClient2() {
        this(new HttpClient2Config());
    }

    public HttpClient2(HttpClient2Config config) {
        this(null, config);
    }

    public HttpClient2(String beanName, HttpClient2Config config) {
        this.beanName = StringUtils.isBlank(beanName) ? Integer.toString(Objects.hashCode(this)) : beanName;
        this.config = config;
        // SSLContext
        SSLContext sslContext = null;
        try {
            // SSLContextBuilder
            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create().setProtocol(config.getSslProtocol());
            if (config.isSslTrustAny()) {
                sslContext = sslContextBuilder.build();
                sslContext.init(null, new TrustManager[] {new X509TrustAnyTrustManager()}, null);
            } else if (config.isSslIgnore()) {
                sslContext = sslContextBuilder
                        .loadTrustMaterial(null, (x509Certificates, authType) -> true)
                        .build();
            } else {
                KeyStore trustStore = KeyStore.getInstance(StringUtils.isBlank(config.getSslType()) ? KeyStore.getDefaultType() : config.getSslType());
                trustStore.load(IOUtil.openInputStream(config.getSslPath()), config.getSslStore().toCharArray());
                sslContext = sslContextBuilder
                        .loadTrustMaterial(trustStore, (x509Certificates, authType) -> true)
                        .build();
            }
        } catch (Exception e) {
            logger.error(e, "[", beanName, "]SSL initialization failed!!");
        }
        // DefaultClientTlsStrategy
        DefaultClientTlsStrategy tlsStrategy = null;
        if (sslContext != null)
            tlsStrategy = new DefaultClientTlsStrategy(sslContext, HostnameVerificationPolicy.BOTH, (hostname, session) -> true);
        // ConnectionConfig.Builder
        ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom();
        if (config.getConnectTimeout() > 0)
            connectionConfigBuilder.setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()));
        if (config.getSocketTimeout() > 0)
            connectionConfigBuilder.setSocketTimeout(Timeout.ofMilliseconds(config.getSocketTimeout()));
        if (config.getTimeToLive() > 0)
            connectionConfigBuilder.setTimeToLive(Timeout.ofMilliseconds(config.getTimeToLive()));
        if (config.getValidateAfterInactivity() > 0)
            connectionConfigBuilder.setValidateAfterInactivity(Timeout.ofMilliseconds(config.getValidateAfterInactivity()));
        // ConnectionConfig
        ConnectionConfig connectionConfig = connectionConfigBuilder.build();
        // PoolingHttpClientConnectionManagerBuilder
        PoolingHttpClientConnectionManagerBuilder poolingHttpClientConnectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig);
        if (config.getMaxConnTotal() > 0)
            poolingHttpClientConnectionManagerBuilder.setMaxConnTotal(config.getMaxConnTotal());
        if (config.getMaxConnPerRoute() > 0)
            poolingHttpClientConnectionManagerBuilder.setMaxConnPerRoute(config.getMaxConnPerRoute());
        if (tlsStrategy != null)
            poolingHttpClientConnectionManagerBuilder.setTlsSocketStrategy(tlsStrategy);
        // HttpClientConnectionManager
        this.httpClientConnectionManager = poolingHttpClientConnectionManagerBuilder.build();
        // RequestConfig.Builder
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        if (config.getConnectionRequestTimeout() > 0)
            requestConfigBuilder.setConnectionRequestTimeout(Timeout.ofMilliseconds(config.getConnectionRequestTimeout()));
        if (config.getResponseTimeout() > 0)
            requestConfigBuilder.setResponseTimeout(Timeout.ofMilliseconds(config.getResponseTimeout()));
        if (config.getConnectionKeepAlive() > 0)
            requestConfigBuilder.setConnectionKeepAlive(Timeout.ofMilliseconds(config.getConnectionKeepAlive()));
        // RequestConfig
        RequestConfig requestConfig = requestConfigBuilder.build();
        // HttpClientBuilder
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .setConnectionManager(this.httpClientConnectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableCookieManagement();
        if (config.isEvictExpiredConnections())
            httpClientBuilder.evictExpiredConnections();
        if (config.getMaxIdleTime() > 0)
            httpClientBuilder.evictIdleConnections(TimeValue.of(config.getMaxIdleTime(), TimeUnit.MILLISECONDS));
        // CloseableHttpClient
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        // HttpComponentsClientHttpRequestFactory
        this.httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
        if (config.getConnectTimeout() > 0)
            this.httpComponentsClientHttpRequestFactory.setConnectTimeout(config.getConnectTimeout());
        if (config.getConnectionRequestTimeout() > 0)
            this.httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(config.getConnectionRequestTimeout());
        if (config.getReadTimeout() > 0)
            this.httpComponentsClientHttpRequestFactory.setReadTimeout(config.getReadTimeout());
        // RestTemplate
        this.restTemplate = new RestTemplate(this.httpComponentsClientHttpRequestFactory);
        this.getMessageConverters().set(1, new StringHttpMessageConverter(Charset.forName(StringUtils.isBlank(config.getCharset()) ? StandardCharsets.UTF_8.name() : config.getCharset())));
    }

    public HttpClient2Config getConfig() {
        return config;
    }

    public List<HttpMessageConverter<?>> getMessageConverters() {
        return this.restTemplate.getMessageConverters();
    }

    public <Response> Response getForObject(String url, Class<Response> responseType) throws Exception {
        return execute(url, null, u -> restTemplate.getForObject(u, responseType));
    }

    public <Response> Response getForObject(String url, Class<Response> responseType, Map<String, ?> uriVariables) throws Exception {
        return execute(url, uriVariables, u -> restTemplate.getForObject(u, responseType, uriVariables));
    }

    public <Response> ResponseEntity<Response> getForEntity(String url, Class<Response> responseType) throws Exception {
        return execute(url, null, u -> restTemplate.getForEntity(u, responseType));
    }

    public <Response> ResponseEntity<Response> getForEntity(String url, Class<Response> responseType, Map<String, ?> uriVariables) throws Exception {
        return execute(url, uriVariables, u -> restTemplate.getForEntity(u, responseType, uriVariables));
    }

    public <Response> ResponseEntity<Response> getForEntity(String url, MediaType mediaType, Class<Response> responseType, Map<String, ?> uriVariables, Map<String, Object> headerMap) throws Exception {
        return exchange(url, HttpMethod.GET, mediaType, responseType, uriVariables, headerMap);
    }

    public <Response> ResponseEntity<Response> exchange(String url, HttpMethod method, MediaType mediaType, Class<Response> responseType, Map<String, ?> uriVariables, Map<String, Object> headerMap) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        if (mediaType != null)
            headers.setContentType(mediaType);
        if (MapUtils.isNotEmpty(headerMap))
            headerMap.forEach((k, v) -> headers.add(k, v.toString()));
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        return execute(url, uriVariables, u -> restTemplate.exchange(u, method, requestEntity, responseType, uriVariables));
    }

    private <Response> Response execute(String url, Map<String, ?> uriVariables, Function<String, Response> function) throws Exception {
        String requestURL = url;
        if (MapUtils.isNotEmpty(uriVariables)) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet()) {
                requestURL = StringUtils.replace(requestURL, StringUtils.join("{", entry.getKey(), "}"), String.valueOf(entry.getValue()));
            }
        }
        messageOut(requestURL, null, uriVariables);
        try {
            Response entity = function.apply(MapUtils.isEmpty(uriVariables) ? toUriString(url) : url);
            messageIn(requestURL, entity);
            return entity;
        } catch (Exception e) {
            throw handleException(requestURL, e);
        }
    }

    public <Request, Response> Response postForObject(String url, MediaType mediaType, Request requestBody, Class<Response> responseType) throws Exception {
        return postForObject(url, mediaType, requestBody, null, responseType);
    }

    public <Request, Response> Response postForObject(String url, MediaType mediaType, Request requestBody, Map<String, Object> headerMap, Class<Response> responseType) throws Exception {
        return execute(url, mediaType, requestBody, headerMap, (u, h) -> restTemplate.postForObject(u, h, responseType));
    }

    public <Request, Response> ResponseEntity<Response> postForEntity(String url, MediaType mediaType, Request requestBody, Class<Response> responseType) throws Exception {
        return postForEntity(url, mediaType, requestBody, null, responseType);
    }

    public <Request, Response> ResponseEntity<Response> postForEntity(String url, MediaType mediaType, Request requestBody, Map<String, Object> headerMap, Class<Response> responseType) throws Exception {
        return execute(url, mediaType, requestBody, headerMap, (u, h) -> restTemplate.postForEntity(u, h, responseType));
    }

    public <Request, Response> ResponseEntity<Response> postForEntity(String url, MediaType mediaType, Request requestBody, Map<String, Object> headerMap, ParameterizedTypeReference<Response> responseTypeRef) throws Exception {
        return exchange(url, HttpMethod.POST, mediaType, requestBody, headerMap, responseTypeRef);
    }

    public <Response> ResponseEntity<Response> postForEntity(String url, MediaType mediaType, HttpEntity<?> requestEntity, ParameterizedTypeReference<Response> responseTypeRef) throws Exception {
        return exchange(url, HttpMethod.POST, mediaType, requestEntity, null, responseTypeRef);
    }

    public <Request, Response> ResponseEntity<Response> exchange(String url, HttpMethod method, MediaType mediaType, Request requestBody, Map<String, Object> headerMap, Class<Response> responseTypeRef) throws Exception {
        return execute(url, mediaType, requestBody, headerMap, (u, h) -> restTemplate.exchange(u, method, h, responseTypeRef));
    }

    public <Response> ResponseEntity<Response> exchange(String url, HttpMethod method, MediaType mediaType, HttpEntity<?> requestEntity, Class<Response> responseTypeRef) throws Exception {
        return execute(url, mediaType, requestEntity, null, (u, h) -> restTemplate.exchange(u, method, h, responseTypeRef));
    }

    public <Request, Response> ResponseEntity<Response> exchange(String url, HttpMethod method, MediaType mediaType, Request requestBody, Map<String, Object> headerMap, ParameterizedTypeReference<Response> responseTypeRef) throws Exception {
        return execute(url, mediaType, requestBody, headerMap, (u, h) -> restTemplate.exchange(u, method, h, responseTypeRef));
    }

    public <Response> ResponseEntity<Response> exchange(String url, HttpMethod method, MediaType mediaType, HttpEntity<?> requestEntity, ParameterizedTypeReference<Response> responseTypeRef) throws Exception {
        return execute(url, mediaType, requestEntity, null, (u, h) -> restTemplate.exchange(u, method, h, responseTypeRef));
    }

    private <Request, Response> Response execute(String url, MediaType mediaType, Request requestBody, Map<String, Object> headerMap, ExecuteListener2<Response, String, HttpEntity<?>> listener) throws Exception {
        HttpEntity<?> requestEntity;
        if (requestBody instanceof HttpEntity) {
            requestEntity = (HttpEntity<?>) requestBody;
        } else {
            HttpHeaders headers = new HttpHeaders();
            if (mediaType != null)
                headers.setContentType(mediaType);
            if (MapUtils.isNotEmpty(headerMap))
                headerMap.forEach((k, v) -> headers.add(k, v.toString()));
            if (requestBody instanceof Map) {
                MultiValueMap<Object, Object> postParameters = new LinkedMultiValueMap<>();
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) requestBody).entrySet()) {
                    postParameters.add(entry.getKey(), entry.getValue());
                }
                requestEntity = new HttpEntity<>(postParameters, headers);
            } else {
                requestEntity = new HttpEntity<>(requestBody, headers);
            }
        }
        messageOut(url, mediaType, requestBody);
        try {
            Response entity = listener.execute(toUriString(url), requestEntity);
            messageIn(url, entity);
            return entity;
        } catch (Exception e) {
            throw handleException(url, e);
        }
    }

    private Exception handleException(String uri, Exception e) {
        if (this.config.isExceptionLogging()) {
            logger.exceptionMsg(e, "[", beanName, "]do http request, uri = [", uri, "] with exception occur!!");
        }
        Throwable t = e;
        if (e.getCause() instanceof RestClientException) {
            t = e.getCause();
        }
        if (t instanceof RestClientException) {
            if (t.getCause() instanceof ConnectException) {
                return ExceptionUtil.createException(t, HttpResultCode.CONNECTION_REFUSED);
            } else if (t.getCause() instanceof SocketTimeoutException) {
                return ExceptionUtil.createException(t, HttpResultCode.READ_TIMED_OUT);
            }
            return ExceptionUtil.createException(t, t.getMessage());
        } else {
            return e;
        }
    }

    private String toUriString(String url) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        return builder.build().encode().toUriString();
    }

    private <Request> void messageOut(String url, MediaType mediaType, Request entity) {
        String request = StringUtils.EMPTY;
        if (entity != null) {
            if (entity instanceof HttpEntity) {
                Object body = ((HttpEntity<?>) entity).getBody();
                if (body != null) {
                    request = body.toString();
                }
            } else if (mediaType == MediaType.APPLICATION_JSON) {
                request = gson.toJson(entity);
            } else {
                request = entity.toString();
            }
        }
        debug("[", beanName, "][", url, "]", MESSAGE_OUT, request);
    }

    private <Response> void messageIn(String url, Response entity) {
        String response = StringUtils.EMPTY;
        if (entity != null) {
            if (entity instanceof ResponseEntity) {
                Object body = ((ResponseEntity<?>) entity).getBody();
                if (body != null) {
                    response = body.toString();
                }
            } else {
                response = entity.toString();
            }
        }
        debug("[", beanName, "][", url, "]", MESSAGE_IN,
                this.config.getResponseLengthLimit() > 0 && StringUtils.isNotBlank(response) && response.length() > this.config.getResponseLengthLimit() ?
                        StringUtils.join("[", response.length(), " characters]") : response);
    }

    private void debug(Object... messages) {
        if (this.config.isRecordLog()) {
            logger.debug(messages);
        }
    }

    @PreDestroy
    public void destroy() {
        if (httpClientConnectionManager != null) {
            try {
                httpClientConnectionManager.close();
            } catch (IOException e) {
                logger.warn(e, "[", beanName, "]httpClientConnectionManager close exception occur");
            }
        }
        if (httpComponentsClientHttpRequestFactory != null) {
            try {
                httpComponentsClientHttpRequestFactory.destroy();
            } catch (Exception e) {
                logger.warn(e, "[", beanName, "]httpComponentsClientHttpRequestFactory destroy exception occur");
            }
        }
    }
}
