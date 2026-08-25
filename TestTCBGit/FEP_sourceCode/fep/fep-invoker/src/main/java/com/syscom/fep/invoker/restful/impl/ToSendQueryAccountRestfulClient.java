package com.syscom.fep.invoker.restful.impl;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.invoker.restful.RestfulClient;
import com.syscom.fep.vo.communication.ToSendQueryAccountCommu;
import com.syscom.fep.vo.enums.ClientType;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Calendar;

/**
 * <p>發送Restful請求給SendQueryAccount</p>
 *
 * <ul>
 * <li>
 * 送出的是{@code SendQueryAccount}對應的XML字串
 * </li>
 * <li>
 * 收到的是{@code HEX String}
 * </li>
 * </ul>
 *
 * @author Jaime
 */
public class ToSendQueryAccountRestfulClient extends RestfulClient<ToSendQueryAccountCommu, String> {

    public ToSendQueryAccountRestfulClient(String uri) {
        super(uri);
    }

    @Override
    protected String doRequest(ToSendQueryAccountCommu request, int timeout) {
        // HttpHeaders
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("X-SourceId", "ATM");
        headers.set("X-TxnDttm", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        // MultiValueMap
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("Idno", request.getIdNo());
        postParameters.add("MobilePhone", request.getMp());
        postParameters.add("BankCode", request.getBankCode());
        postParameters.add("timeout", String.valueOf(request.getTimeout()));
        // HttpEntity
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(postParameters, headers);
        InputStream fis = null;
        BufferedInputStream bis = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);//Make an empty store
            //TODO 改作法
            fis = SslContextFactory.getInputStream("TCBCA_IndR.cer", KeyStore.getDefaultType());
            // InputStream fis = new FileInputStream("./TCBCA_IndR.cer");
            bis = new BufferedInputStream(fis);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            while (bis.available() > 0) {
                Certificate cert = cf.generateCertificate(bis);
                trustStore.setCertificateEntry("fiddler" + bis.available(), cert);
            }
            // 2024-10-17 Richard modified start for 【SSL Verification Bypass】
            // SSLContext sslcontext = SSLContexts.custom()
            //         .loadTrustMaterial(new TrustStrategy() {
            //             @Override
            //             public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            //                 return true;
            //             }
            //         })
            //         .loadKeyMaterial(trustStore, "changeit".toCharArray())
            //         .build();
            SSLContextBuilder builder = ReflectUtil.envokeStaticMethod(SSLContexts.class, "custom", SSLContexts.custom());
            builder = ReflectUtil.envokeMethod(builder, "loadTrustMaterial", new Class[] {org.apache.hc.core5.ssl.TrustStrategy.class}, new Object[] {(org.apache.hc.core5.ssl.TrustStrategy) (chain, authType) -> true}, builder);
            builder = ReflectUtil.envokeMethod(builder, "loadKeyMaterial", new Class[] {KeyStore.class, char[].class}, new Object[] {trustStore, "changeit".toCharArray()}, builder);
            SSLContext sslcontext = builder.build();
            // 2024-10-17 Richard modified end for 【SSL Verification Bypass】
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[] {"TLSv1.2"},
                    null,
                    NoopHostnameVerifier.INSTANCE);
            HttpClientConnectionManager clientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslConnectionSocketFactory).build();
            CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(clientConnectionManager).build();

            ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            // SimpleClientHttpRequestFactory
            if (timeout > 0) {
                restTemplate.setRequestFactory(this.getSimpleClientHttpRequestFactory(timeout));
            }
            String response = restTemplate.postForObject(this.getURI(), httpEntity, String.class);
            if (response.contains("\"")) {
                // 因返回為Json格式所以會在內容前後加雙引號, 你收到內容後看是要用replace或用Json Parse後再取出值即可
                response = StringUtils.replace(response, "\"", StringUtils.EMPTY);
            }
            return response;
        } catch (Exception e) {
            LogHelperFactory.getRestfulClientLogger().error(e.getMessage(), e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    LogHelperFactory.getRestfulClientLogger().warn(e.getMessage(), e);
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception e) {
                    LogHelperFactory.getRestfulClientLogger().warn(e.getMessage(), e);
                }
            }

        }
        return null;

    }

    @Override
    protected String getURI() {
        String uri = super.getURI();
        // 如果db中沒有設置，則返回預設的測試url
        return StringUtils.isBlank(uri) ? "http://192.168.0.30:8912/api/VFISC/SendReceive" : uri;
    }

    @Override
    protected String getName() {
        return ClientType.TO_FEP_ATM.name();
    }
}
