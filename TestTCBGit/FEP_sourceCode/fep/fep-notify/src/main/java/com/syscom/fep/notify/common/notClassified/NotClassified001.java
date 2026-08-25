package com.syscom.fep.notify.common.notClassified;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.http.HttpClientConfigureConstant;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.NotClassified001Config;
import com.syscom.fep.notify.exception.NotifyException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.syscom.fep.notify.cnst.NotifyConstant.*;

@Component
public class NotClassified001<T> extends SenderBase<T> {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private NotClassified001Config notclassifiedConfig;
    @Autowired
    @Qualifier(HttpClientConfigureConstant.BEAN_NAME_NOTIFY)
    private HttpClient2 httpClient2;

    private static final Map<String, String> replacementMap = new HashMap<String, String>() {{
        // 若有換行的需求，請填入ASCII Code 6 代表換⾏。
        put("\n", String.valueOf((char) 6));
    }};


    @Override
    public void send(LogData logData, Map<String, T> content) throws NotifyException, Exception {
        Map<String, String> params = new HashMap<>();
        params.put("customerId", (String) content.get("customerId"));               // 客戶Id
        params.put("mobilePhone", (String) content.get("mobilePhone"));                // 訊息主旨
        params.put("messageTitle", (String) content.get(NOTIFY_MESSAGE_CONTENT_SUBJECT));                // 訊息主旨
        params.put("strContent", this.replaceBody((String) content.get(NOTIFY_MESSAGE_CONTENT_BODY)));   // 訊息內容
        params.put("appIds", notclassifiedConfig.getAppIds());                      // 可多組，以逗點區隔
        params.put("expiredDate", (String) content.get("expiredDate"));             // 過期日期(yyyy/MM/dd) 9999/12/31
        params.put("expiredMsg", (String) content.get("expiredMsg"));               // 過期訊息內容
        params.put("scheduleDateTime", (String) content.get("scheduleDateTime"));   // 預約時間:空白為即時 or YYYY/MM/DD h24:mi:ss
        params.put("salesKind", (String) content.get("salesKind"));                 // 業務別 (IBS,DCS,CardLink)
        params.put("messageType", (String) content.get("messageType"));             // P:個人訊息 G:一般訊息
        params.put("routeSMS", (String) content.get("routeSMS"));                   // Y:送簡訊 N:不送簡訊 B:備援
        params.put("routePUSH", (String) content.get("routePUSH"));                 // Y:推播 N:不送推播
        params.put("status", (String) content.get("status"));                       // 0:未處理 1:已處理 2:失敗 3:customerId not existed
        params.put("billingTo", (String) content.get("billingTo"));                 // 計帳
        params.put("priority", (String) content.get("priority"));                   // 5:立即傳送   1:Scheduling
        params.put("force", (String) content.get("force"));                         // Y:依後台Business Rules(default)  N:依App使用者

        String resultStr = httpClient2.postForObject(notclassifiedConfig.getUrl(), MediaType.APPLICATION_FORM_URLENCODED, params, String.class);
        // rc => 0000 成功Push；0001 成功SMS; 0002成功預約  9999失敗
        // rm => 回傳訊息
        // data => 任務代碼(taskId)
        ObjectMapper mapper = new ObjectMapper();
        Map result = mapper.readValue(resultStr, Map.class);
        if (Objects.isNull(result)) {
            // logger.warn("Response result is null! ");
            // logger.warn("request: ", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(params));
            logData.setRemark(StringUtils.join("Response result is null!"));
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logData.setMessage(mapper.writeValueAsString(params));
            logMessage(Level.WARN, logData);
            throw new NotifyException("Response result is null!");
        }
        if ("9999".equals(result.get("rc"))) {
            // logger.warn("SMS send Failure! ");
            // logger.warn("request: ", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(params));
            // logger.warn("response: ", replaceBody(resultStr));
            logData.setRemark(StringUtils.join("SMS send Failure! request:", mapper.writeValueAsString(params)));
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logData.setMessage(replaceBody(resultStr));
            logMessage(Level.WARN, logData);
            throw new NotifyException("SMS send Failure! ");
        }
    }


    /**
     * 對內容進行特殊字串的替換
     *
     * @param body
     * @return
     * @throws UnsupportedEncodingException
     */
    private String replaceBody(String body) throws UnsupportedEncodingException {
        for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
            body = StringUtils.replace(body, entry.getKey(), entry.getValue());
        }
        return body;
    }
}
