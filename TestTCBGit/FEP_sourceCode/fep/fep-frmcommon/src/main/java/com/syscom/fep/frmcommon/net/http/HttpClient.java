package com.syscom.fep.frmcommon.net.http;

import com.google.gson.Gson;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpClient {
    private final LogHelper logger = new LogHelper();
    private static final String MESSAGE_IN = "<<<<<<<<<<";
    private static final String MESSAGE_OUT = ">>>>>>>>>>";
    private static final int DEFAULT_TIMEOUT = 30000;
    private boolean recordLog = true;
    private long responseLengthLimit;
    private boolean httpConnectionPooling = false;
    private boolean exceptionLogging;

    public HttpClient() {
        this(true);
    }

    public HttpClient(boolean recordLog) {
        this(recordLog, 0, false);
    }

    public HttpClient(long responseLengthLimit) {
        this(true, responseLengthLimit, false);
    }

    public HttpClient(boolean recordLog, boolean httpConnectionPooling) {
        this(recordLog, 0, httpConnectionPooling);
    }

    public HttpClient(boolean recordLog, long responseLengthLimit, boolean httpConnectionPooling) {
        this.recordLog = recordLog;
        this.responseLengthLimit = responseLengthLimit;
        this.httpConnectionPooling = httpConnectionPooling;
    }

    public void setRecordLog(boolean recordLog) {
        this.recordLog = recordLog;
    }

    public void setResponseLengthLimit(long responseLengthLimit) {
        this.responseLengthLimit = responseLengthLimit;
    }

    public void setHttpConnectionPooling(boolean httpConnectionPooling) {
        this.httpConnectionPooling = httpConnectionPooling;
    }

    public void setExceptionLogging(boolean exceptionLogging) {
        this.exceptionLogging = exceptionLogging;
    }

    public String doGet(String uri, boolean... exceptionLogging) throws Exception {
        return doGet(uri, null, exceptionLogging);
    }

    public String doGet(String uri, Map<String, Object> uriVariablesMap, boolean... exceptionLogging) throws Exception {
        return doGet(uri, (MediaType) null, uriVariablesMap, null, exceptionLogging);
    }

    public String doGet(String uri, Map<String, Object> uriVariablesMap, Charset defaultCharset, boolean... exceptionLogging) throws Exception {
        return doGet(uri, (MediaType) null, uriVariablesMap, defaultCharset, exceptionLogging);
    }

    public String doGet(String uri, MediaType mediaType, Map<String, Object> uriVariablesMap, Charset defaultCharset, boolean... exceptionLogging) throws Exception {
        return doGet(uri, mediaType, DEFAULT_TIMEOUT, uriVariablesMap, defaultCharset, exceptionLogging);
    }

    public String doGet(String uri, MediaType mediaType, int timeout, Map<String, Object> uriVariablesMap, boolean... exceptionLogging) throws Exception {
        return doGet(uri, mediaType, timeout, uriVariablesMap, null, exceptionLogging);
    }

    public String doGet(String uri, MediaType mediaType, int timeout, Map<String, Object> uriVariablesMap, Charset defaultCharset, boolean... exceptionLogging) throws Exception {
        String url = uri;
        if (MapUtils.isNotEmpty(uriVariablesMap)) {
            for (Map.Entry<String, Object> entry : uriVariablesMap.entrySet()) {
                url = StringUtils.replace(url, StringUtils.join("{", entry.getKey(), "}"), String.valueOf(entry.getValue()));
            }
        }
        try {
            // HttpHeaders
            HttpHeaders headers = new HttpHeaders();
            // ContentType
            headers.setContentType(mediaType);
            // requestEntity
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            // RestTemplate
            RestTemplate restTemplate = new RestTemplate();
            if (defaultCharset != null) {
                restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            }
            // SimpleClientHttpRequestFactory
            if (timeout > 0) {
                if (isHttps(uri)) {
                    restTemplate.setRequestFactory(HttpClientConfiguration.createTrustAnyHttpComponentsClientHttpRequestFactory(SslContextFactory.PROTOCOL, timeout, this.httpConnectionPooling));
                } else {
                    restTemplate.setRequestFactory(HttpClientConfiguration.createHttpComponentsClientHttpRequestFactory(timeout, this.httpConnectionPooling));
                }
            }
            debug("[", url, "]", MESSAGE_OUT);
            ResponseEntity<String> responseEntity = null;
            if (MapUtils.isEmpty(uriVariablesMap)) {
                responseEntity = restTemplate.exchange(toUriString(uri), HttpMethod.GET, requestEntity, String.class);
            } else {
                // 有可能uri中帶有{param}這樣的, 所以不要轉義
                responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class, uriVariablesMap);
            }
            String response = responseEntity.getBody();
            debug("[", url, "]", MESSAGE_IN, this.responseLengthLimit > 0 && StringUtils.isNotBlank(response) && response.length() > this.responseLengthLimit ? StringUtils.join("[", response.length(), " characters]") : response);
            return response;
        } catch (Exception e) {
            throw handleException(url, e, exceptionLogging);
        }
    }

    public <T> String doPost(String uri, T request, boolean... exceptionLogging) throws Exception {
        return doPost(uri, null, request, exceptionLogging);
    }

    public <T> String doPost(String uri, MediaType mediaType, boolean shortConnFlag, T request, boolean... exceptionLogging) throws Exception {
        return doPost(uri, mediaType, DEFAULT_TIMEOUT, shortConnFlag, request, StandardCharsets.UTF_8, exceptionLogging);
    }

    public <T> String doPost(String uri, MediaType mediaType, T request, boolean... exceptionLogging) throws Exception {
        return doPost(uri, mediaType, DEFAULT_TIMEOUT, false, request, StandardCharsets.UTF_8, exceptionLogging);
    }

    public <T> String doPost(String uri, MediaType mediaType, T request, Charset defaultCharset, boolean... exceptionLogging) throws Exception {
        return doPost(uri, mediaType, DEFAULT_TIMEOUT, false, request, defaultCharset, exceptionLogging);
    }

    public <T> String doPost(String uri, MediaType mediaType, int timeout, T request, boolean... exceptionLogging) throws Exception {
        return doPost(uri, mediaType, timeout, false, request, StandardCharsets.UTF_8, exceptionLogging);
    }

    public <T> String doPost(String uri, MediaType mediaType, int timeout, boolean shortConnFlag, T request, Charset defaultCharset, boolean... exceptionLogging) throws Exception {
        try {
            // HttpHeaders
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            // HTTP 1.1 預設為keep-alive
            if (shortConnFlag) {
                headers.setConnection("close");
            }
            // HttpEntity
            HttpEntity<?> requestEntity = null;
            if (request instanceof Map) {
                // MultiValueMap
                MultiValueMap<Object, Object> postParameters = new LinkedMultiValueMap<>();
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) request).entrySet()) {
                    postParameters.add(entry.getKey(), entry.getValue());
                }
                requestEntity = new HttpEntity<>(postParameters, headers);
            } else {
                requestEntity = new HttpEntity<>(request, headers);
            }
            // RestTemplate
            RestTemplate restTemplate = new RestTemplate();
            if (defaultCharset != null) {
                restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(defaultCharset));
            } else {
                restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            }
            // SimpleClientHttpRequestFactory
            if (timeout > 0) {
                if (isHttps(uri)) {
                    restTemplate.setRequestFactory(HttpClientConfiguration.createTrustAnyHttpComponentsClientHttpRequestFactory(SslContextFactory.PROTOCOL, timeout, this.httpConnectionPooling));
                } else {
                    restTemplate.setRequestFactory(HttpClientConfiguration.createHttpComponentsClientHttpRequestFactory(timeout, this.httpConnectionPooling));
                }
            }
            debug("[", uri, "]", MESSAGE_OUT, this.printRequest(mediaType, request));
            ResponseEntity<String> responseEntity = restTemplate.exchange(toUriString(uri), HttpMethod.POST, requestEntity, String.class);
            String response = responseEntity.getBody();
            debug("[", uri, "]", MESSAGE_IN, this.responseLengthLimit > 0 && StringUtils.isNotBlank(response) && response.length() > this.responseLengthLimit ? StringUtils.join("[", response.length(), " characters]") : response);
            return response;
        } catch (Exception e) {
            throw handleException(uri, e, exceptionLogging);
        }
    }

    private <T> String printRequest(MediaType mediaType, T request) {
        if (mediaType == MediaType.APPLICATION_JSON || mediaType == MediaType.APPLICATION_JSON_UTF8) {
            return new Gson().toJson(request);
        }
        return request.toString();
    }

    private Exception handleException(String uri, Exception e, boolean... exceptionLogging) {
        if (ArrayUtils.isEmpty(exceptionLogging) || exceptionLogging[0] || this.exceptionLogging) {
            logger.exceptionMsg(e, "do http request, uri = [", uri, "] with exception occur!!");
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

    public static boolean isHttps(String uri) {
        return uri.toLowerCase().startsWith("https");
    }

    public static String toUriString(String uri) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);
        return builder.build().encode().toUriString();
    }

    private void debug(Object... messages) {
        if (recordLog)
            logger.debug(messages);
    }

    public static HttpEntity<MultiValueMap<String, Object>> createHttpEntity(Map<String, String> args) {
        // HttpHeaders
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // MultiValueMap
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : args.entrySet()) {
            map.add(entry.getKey(), entry.getValue());
        }
        // HttpEntity
        return new HttpEntity<>(map, headers);
    }
}
