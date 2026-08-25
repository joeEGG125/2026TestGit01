package com.syscom.fep.common.sms.hiair;

import com.google.gson.Gson;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.io.IOUtils;
import org.owasp.esapi.ESAPI;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HiairSmsHttpApi {
    private final Gson gson = new Gson();
    private static final String ProgramName = HiairSmsHttpApi.class.getSimpleName();
    private static final LogHelper logger = LogHelperFactory.getGeneralLogger();

    public HiairSmsHttpResponse httpPost(HiairSmsConfiguration hiairSmsConfiguration, String targetURL, HiairSmsBaseRequest request) {
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            URL url;
            try {
                String cURL = ESAPI.encoder().canonicalize(targetURL, false, false);
                if (ESAPI.validator().isValidInput("URLContext", cURL, "URL", cURL.length(), false)) {
                    url = new URL(cURL);
                } else {
                    return null;
                }
            } catch (MalformedURLException e) {
                logger.error(e, "[", ProgramName, "]", e.getMessage());
                return new HiairSmsHttpResponse(e);
            }
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            if (HttpClient.isHttps(targetURL))
                ((HttpsURLConnection) conn).setSSLSocketFactory(HttpClientConfiguration.createTrustAnySSLSocketFactory(SslContextFactory.PROTOCOL));
            conn.setConnectTimeout(hiairSmsConfiguration.getHttpConnectTimeout());
            conn.setReadTimeout(hiairSmsConfiguration.getHttpReadTimeout());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            os = conn.getOutputStream();
            String requestBody = gson.toJson(request);
            logger.info("[", ProgramName, "][", targetURL, "]>>>>>>>>>>", requestBody);
            // 2025-05-22 Richard modified for [Missing HSTS Header]
            // os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            ReflectUtil.envokeMethod(os, "write", new Class[] {byte[].class}, new Object[] {requestBody.getBytes(StandardCharsets.UTF_8)});
            try {
                is = conn.getInputStream();
            } catch (IOException e) {
                is = conn.getErrorStream();
                if (is == null)
                    throw e;
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            String responseBody = sb.toString();
            int httpStatusCode = conn.getResponseCode();
            logger.info("[", ProgramName, "][", targetURL, "][", httpStatusCode, "]<<<<<<<<<<", responseBody);
            HiairSmsHttpResponseContent content = gson.fromJson(responseBody, HiairSmsHttpResponseContent.class);
            return new HiairSmsHttpResponse(httpStatusCode, content);
        } catch (Exception e) {
            logger.error(e, "doPost failed, ", e.getMessage());
            return new HiairSmsHttpResponse(e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
