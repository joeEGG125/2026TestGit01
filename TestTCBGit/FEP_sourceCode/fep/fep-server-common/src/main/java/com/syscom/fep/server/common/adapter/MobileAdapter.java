package com.syscom.fep.server.common.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.vo.communication.ToSendQueryAccountCommu;
import com.syscom.fep.vo.enums.RestfulResultCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Calendar;

/**
 * 負責傳送至手機轉帳API
 *
 * @author Jaime
 */
public class MobileAdapter extends AdapterBase {

    public MobileAdapter(MessageBase txData) {
        this.txData = txData;
    }

    /**
     * 送給手機的data
     */
    private ToSendQueryAccountCommu toSendQueryAccountCommu;

    private JsonNode responseJson;

    private MessageBase txData;

    /**
     * 財金STAN
     */
    private String stan;

    @Override
    public FEPReturnCode sendReceive() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        InputStream fis = null;
        BufferedInputStream bis = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try {

            this.txData.getLogContext().setMessage(toSendQueryAccountCommu.toString());
            this.txData.getLogContext().setStan(this.stan);
            this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
            this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, "sendMsgToFISC"));
            this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
            this.txData.getLogContext().setRemark(StringUtils.join("MobileAdapter before Send TO Mobile Url:", toSendQueryAccountCommu.getRestfulUrl(), ",Timeout:", this.timeout));
            logMessage(this.txData.getLogContext());

            String url = CMNConfig.getInstance().getMobileQueryUrl() + "/QueryAccountInfo";
            toSendQueryAccountCommu.setRestfulUrl(url);
            toSendQueryAccountCommu.setTimeout((60 + 5) * 1000);

//        	ToSendQueryAccountRestfulClient toFiscClient = new ToSendQueryAccountRestfulClient(toSendQueryAccountCommu.getRestfulUrl());
//            String resultMessage = toFiscClient.sendReceive((ToSendQueryAccountCommu) toSendQueryAccountCommu, this.timeout);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);//Make an empty store
            fis = SslContextFactory.getInputStream("TCBCA_IndR.cer", KeyStore.getDefaultType());
//	        InputStream fis = new FileInputStream("./TCBCA_IndR.cer");
            bis = new BufferedInputStream(fis);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            while (bis.available() > 0) {
                Certificate cert = cf.generateCertificate(bis);
                trustStore.setCertificateEntry("fiddler" + bis.available(), cert);
            }
            // 2024-11-05 Richard modified start for 【SSL Verification Bypass】
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
            // 2024-11-05 Richard modified end for 【SSL Verification Bypass】
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[] {"TLSv1.2"},
                    null,
                    NoopHostnameVerifier.INSTANCE);
            HttpClientConnectionManager clientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslConnectionSocketFactory).build();
            client = HttpClients.custom()
                    .setConnectionManager(clientConnectionManager)
                    .build();

            HttpPost httpPost = new HttpPost(url);
//			    HttpPost httpPost = new HttpPost("http://www.example.com");

//		    String json = "{\"Idno\":\"" + toSendQueryAccountCommu.getIdNo() + "\",\"MobilePhone\":\""+ toSendQueryAccountCommu.getMobilePhone() + "\",\"BankCode\":\""+ toSendQueryAccountCommu.getBankCode().trim() + "\"}";
            StringEntity entity = new StringEntity("{\"Idno\":\"" + toSendQueryAccountCommu.getIdNo() + "\",\"MobilePhone\":\"" + toSendQueryAccountCommu.getMp() + "\",\"BankCode\":\"" + toSendQueryAccountCommu.getBankCode().trim() + "\"}");
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("X-SourceId", "ATM");
            httpPost.setHeader("X-TxnDttm", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));

            response = client.execute(httpPost);

            HttpEntity httpentity = response.getEntity();
//                System.out.println(response.getStatusLine());
//                System.out.println("response ContentType:"+httpentity.getContentType());
//                System.out.println("response Content:"+IOUtils.toString(httpentity.getContent()));
//                System.out.println("response AllHeaders:"+response.getAllHeaders());
            String responseContent = IOUtils.toString(httpentity.getContent(), "UTF-8");
            int httpStatus = response.getCode();

            // Parse the JSON response to get ReturnCode
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseContent);
                if (httpStatus == 200) {
                    // 正常流程:讀取回應欄位
                    if (jsonNode.has("ReturnCode")) {
                        String returnCode = jsonNode.get("ReturnCode").asText();
                        this.txData.getLogContext().setRemark(jsonNode + ",ReturnCode:" + returnCode);
                        logMessage(this.txData.getLogContext());
                        if (!"4001".equals(returnCode)) {
                            this.txData.getLogContext().setRemark("MobileAdapter Custom ReturnCode:" + returnCode + ",Custom ReturnCodeToString:" + FEPReturnCode.toString(returnCode));
                            this.txData.getLogContext().setReturnCode(rtnCode);
                            logMessage(this.txData.getLogContext());
                        }
                        this.setResponseJson(jsonNode);
                    }
                } else {
                    // 錯誤處理:讀取錯誤訊息
                    String error = jsonNode.has("error") ? jsonNode.get("error").asText() : "Unknown error";
                    String message = jsonNode.has("message") ? jsonNode.get("message").asText() : "No message";
                    rtnCode = FEPReturnCode.MobileAPIError;
                    this.txData.getLogContext().setProgramException(new Exception(error + "," + message));
                    this.txData.getLogContext().setRemark("Bad HttpStatus:" + httpStatus);
                    this.txData.getLogContext().setReturnCode(rtnCode);
                    sendEMS(this.txData.getLogContext());
                    return rtnCode;
                }
            } catch (Exception e) {
                rtnCode = FEPReturnCode.MobileAPIError;
                this.txData.getLogContext().setProgramException(e);
                this.txData.getLogContext().setRemark("Error parsing JSON response: " + e.getMessage());
                this.txData.getLogContext().setReturnCode(rtnCode);
                sendEMS(this.txData.getLogContext());
                return rtnCode;
            }

            this.txData.getLogContext().setMessage(toSendQueryAccountCommu.getRestfulUrl() + ",response AllHeaders:" + StringUtils.join(response.getHeaders(), "|"));
            this.txData.getLogContext().setStan(this.stan);
            this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, "sendMsgToFISC"));
            this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterOut);
            this.txData.getLogContext().setMessageFlowType(MessageFlow.Response);
            this.txData.getLogContext().setReturnCode(rtnCode);
            this.txData.getLogContext().setRemark(StringUtils.join("MobileAdapter Receive Msg :", responseContent));
            logMessage(this.txData.getLogContext());

            return rtnCode;
        } catch (Exception e) {
            if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
                rtnCode = FEPReturnCode.FISCGWATMSendError;
            } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
                rtnCode = FEPReturnCode.FISCTimeout;
            } else {
                rtnCode = CommonReturnCode.ProgramException;
            }
            this.txData.getLogContext().setProgramException(e);
            this.txData.getLogContext().setReturnCode(rtnCode);
            sendEMS(this.txData.getLogContext());
            return rtnCode;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    this.txData.getLogContext().setProgramException(e);
                    this.txData.getLogContext().setReturnCode(rtnCode);
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    this.txData.getLogContext().setProgramException(e);
                    this.txData.getLogContext().setReturnCode(rtnCode);
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    this.txData.getLogContext().setProgramException(e);
                    this.txData.getLogContext().setReturnCode(rtnCode);
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception e) {
                    this.txData.getLogContext().setProgramException(e);
                    this.txData.getLogContext().setReturnCode(rtnCode);
                }
            }
        }
    }

    public ToSendQueryAccountCommu getToSendQueryAccountCommu() {
        return toSendQueryAccountCommu;
    }

    public void setToSendQueryAccountCommu(ToSendQueryAccountCommu toSendQueryAccountCommu) {
        this.toSendQueryAccountCommu = toSendQueryAccountCommu;
    }

    public MessageBase getTxData() {
        return txData;
    }

    public void setTxData(MessageBase txData) {
        this.txData = txData;
    }

    public JsonNode getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(JsonNode responseJson) {
        this.responseJson = responseJson;
    }
}
