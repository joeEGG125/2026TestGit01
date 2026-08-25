package com.syscom.fep.common.sms.mitake;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "spring.fep.sms.mitake", name = "enable", havingValue = "true")
@Lazy
public class MitakeSmsOperator {
    private static final String ProgramName = MitakeSmsOperator.class.getSimpleName();
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private MitakeSmsConfiguration configuration;
    private ExecutorService executor;
    private static final Map<String, String> replacementMap = new HashMap<>() {{
        // 若有換行的需求，請填入ASCII Code 6 代表換⾏。
        put("\n", String.valueOf((char) 6));
    }};

    @PostConstruct
    public void postConstruct() {
        executor = ThreadPoolFactory.newFixedThreadPool(
                this.configuration.getExecutorCorePoolSize(),
                this.configuration.getExecutorKeepAliveTime(),
                TimeUnit.MILLISECONDS,
                this.configuration.getExecutorQueueCapacity(),
                new SimpleThreadFactory("MitakeSmsOperatorExecutor"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @PreDestroy
    public void preDestroy() {
        ThreadPoolFactory.shutdown(executor, "MitakeSmsOperatorExecutor");
    }

    public void send(String dstaddr, String smsMsg) {
        send(dstaddr, smsMsg, true);
    }

    public void send(String dstaddr, String smsMsg, boolean async) {
        send(Collections.singletonList(dstaddr), smsMsg, async);
    }

    public void send(List<String> dstaddrs, String smsMsg) {
        send(dstaddrs, smsMsg, true);
    }

    public void send(List<String> dstaddrs, String smsMsg, boolean async) {
        if (async) {
            executor.execute(() -> {
                sendSms(dstaddrs, smsMsg);
            });
        } else {
            sendSms(dstaddrs, smsMsg);
        }
    }

    private void sendSms(List<String> dstaddrs, String smsMsg) {
        if (CollectionUtils.isNotEmpty(dstaddrs))
            dstaddrs.forEach(dstaddr -> sendSms(dstaddr, smsMsg));
    }

    private void sendSms(String dstaddr, String smsMsg) {
        if (StringUtils.isBlank(dstaddr) || StringUtils.isBlank(smsMsg))
            return;
        String urlProvider = configuration.getDomain();
        String charsetUrl = configuration.getCharsetUrl();
        String username = configuration.getUsername(); // 帳號
        String smsSscode = configuration.getSscode(); // 密碼
        String smbody = this.replaceBody(smsMsg); // 簡訊內容
        logger.debug("[", ProgramName, "][", urlProvider, "][", dstaddr, "]Send SMS--begin");
        try {
            // 加上參數
            URIBuilder uriBuilder = new URIBuilder(urlProvider);
            uriBuilder.addParameter("CharsetURL", charsetUrl);
            uriBuilder.addParameter("username", username);
            uriBuilder.addParameter("password", smsSscode);
            uriBuilder.addParameter("dstaddr", dstaddr); // 手機號碼
            uriBuilder.addParameter("smbody", smbody);
            logger.debug("[", ProgramName, "][", urlProvider, "][", dstaddr, "]", StringUtils.repeat('>', 10), smbody);
            URI uri = uriBuilder.build();
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (HttpClient.isHttps(urlProvider))
                ((HttpsURLConnection) connection).setSSLSocketFactory(HttpClientConfiguration.createTrustAnySSLSocketFactory(SslContextFactory.PROTOCOL));
            connection.setConnectTimeout(configuration.getHttpConnectTimeout());
            connection.setReadTimeout(configuration.getHttpReadTimeout());
            connection.setRequestProperty("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    String responseText = response.toString();
                    logger.debug("[", ProgramName, "][", urlProvider, "][", dstaddr, "][", responseCode, "]", StringUtils.repeat('<', 10), responseText);
                    // 判斷是否為成功
                    if (responseText.contains("statuscode=1")) {
                        logger.debug("[", ProgramName, "][", urlProvider, "][", dstaddr, "][", responseCode, "]SMS send successful!!");
                    } else {
                        logger.error("[", ProgramName, "][", urlProvider, "][", dstaddr, "][", responseCode, "]SMS send may have failed!!");
                    }
                }
            } else {
                logger.error("[", ProgramName, "][", urlProvider, "][", dstaddr, "]Failed to connect to SMS API, HTTP code:", responseCode);
            }
        } catch (Exception e) {
            logger.error(e, "[", ProgramName, "][", urlProvider, "][", dstaddr, "]Send SMS--failed");
        } finally {
            logger.debug("[", ProgramName, "][", urlProvider, "][", dstaddr, "]Send SMS--finished");
        }
    }

    /**
     * 對簡訊內容進行特殊字串的替換
     *
     * @param smbody
     * @return
     */
    private String replaceBody(String smbody) {
        for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
            smbody = StringUtils.replace(smbody, entry.getKey(), entry.getValue());
        }
        return smbody;
    }
}
