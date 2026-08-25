package com.syscom.fep.notify.common.sms;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.Sms001Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.syscom.fep.notify.cnst.NotifyConstant.*;

@Component
public class Sms001<T> extends SenderBase<T> {

    @Autowired
    private Sms001Config smsConfig;

    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    private static final Map<String, String> replacementMap = new HashMap<String, String>() {{
        // 若有換行的需求，請填入ASCII Code 6 代表換⾏。
        put("\n", String.valueOf((char) 6));
    }};


    @Override
    public void send(LogData logData, Map<String, T> content) throws Exception {
//        HttpClient httpClient = new HttpClient();
//        Map<String, String> params = new HashMap<>();
//        params.put("username", (String) content.get(NOTIFY_MESSAGE_ACCOUNT));   //帳號
//        params.put("password", (String) content.get(NOTIFY_MESSAGE_SSCODE));    //密碼
//        params.put("dstaddr", (String) content.get(NOTIFY_PHONE_PARM_NAME));    //手機號碼
//        params.put("destname", (String) content.get("##destname##"));           //系統名稱
//        params.put("dlvtime", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN)); //簡訊預約時間，立即發送
//        params.put("vldtime", (String) content.get("##vldtime##"));             //簡訊有效時間
//        params.put("smbody", this.replaceBody((String) content.get(NOTIFY_MESSAGE_CONTENT_BODY)));

        String urlProvider = smsConfig.isUseConfigDomain() ? smsConfig.getDomain() : (String) content.get(NOTIFY_MESSAGE_DOMAIN);
        URIBuilder uriBuilder = new URIBuilder(urlProvider);
        String charsetUrl = "UTF-8";
        String username = (String) content.get(NOTIFY_MESSAGE_ACCOUNT); //帳號
        String smsSscode = (String) content.get(NOTIFY_MESSAGE_SSCODE); //密碼
        String dstaddr = (String) content.get(NOTIFY_PHONE_PARM_NAME);  //手機號碼
        String smbody = this.replaceBody((String) content.get(NOTIFY_MESSAGE_CONTENT_BODY));//簡訊內容
        String apSysKey = null; //for交易:各系統用的KEY

        //加上參數
        uriBuilder.addParameter("CharsetURL", charsetUrl);
        uriBuilder.addParameter("username", username);
        uriBuilder.addParameter("password", smsSscode);
        uriBuilder.addParameter("dstaddr", dstaddr);
        uriBuilder.addParameter("smbody", smbody);

        if (content.containsKey(NOTIFY_MESSAGE_APSYSKEY)) {
            apSysKey = (String) content.get(NOTIFY_MESSAGE_APSYSKEY);
            uriBuilder.addParameter("destname", apSysKey);
        }

        try {
            logData.setRemark("Sms001--Ready to Send SMS|" + uriBuilder);
            logData.setMessage("username:" + username + ",password:" + smsSscode + ",dstaddr:" + dstaddr + ",smbody:" + smbody);
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
            URI uri = uriBuilder.build();
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    String responseText = response.toString();
                    // 判斷是否為成功
                    if (responseText.contains("statuscode=1")) {
                        logData.setRemark("Sms001--SMS send successful!! HTTP code:" + responseCode + ",API Response:" + responseText);
                        logData.setProgramName(StringUtils.join(ProgramName, ".send"));
                        logMessage(logData);
                    } else {
                        logData.setRemark("Sms001--SMS send may have failed!!");
                        logData.setMessage("API Response:" + responseText);
                        logData.setProgramName(StringUtils.join(ProgramName, ".send"));
                        logMessage(logData);
                    }
                }
            } else {
                logData.setRemark("Failed to connect to SMS API. HTTP code: " + responseCode);
                logData.setProgramName(StringUtils.join(ProgramName, ".send"));
                logMessage(logData);
            }
            logData.setRemark("Sms001--Send SMS Finish !!");
            logData.setMessage("");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
        } catch (Exception e) {
            // logger.error("Sms001--Send SMS Fail !!-----------Response: " + e);
            logData.setRemark("Sms001--Send SMS Fail !!");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logData.setProgramException(e);
            sendEMS(logData);
        }
    }

    /**
     * 對簡訊內容進行特殊字串的替換
     *
     * @param smbody
     * @return
     * @throws UnsupportedEncodingException
     */
    private String replaceBody(String smbody) throws UnsupportedEncodingException {
        for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
            smbody = StringUtils.replace(smbody, entry.getKey(), entry.getValue());
        }
        return smbody;
    }
}
