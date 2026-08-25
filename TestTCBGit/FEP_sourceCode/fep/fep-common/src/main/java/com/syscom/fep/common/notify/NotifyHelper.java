package com.syscom.fep.common.notify;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.delegate.AsynchronousListener;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.frmcommon.net.http.HttpResultCode;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.notify.dto.request.NotifyMiddlePlatformRequestForm;
import com.syscom.fep.notify.dto.request.NotifyRequestContent;
import com.syscom.fep.notify.dto.request.NotifyRequestForm;
import com.syscom.fep.notify.dto.response.MiddlePlatFormResponse;
import com.syscom.fep.notify.dto.response.NotifyResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = NotifyHelperConstant.CONFIGURATION_PROPERTIES_PREFIX, name = {NotifyHelperConstant.CONFIGURATION_PROPERTIES_URL_SEND_NOTIFY, NotifyHelperConstant.CONFIGURATION_PROPERTIES_URL_LOG_NOTIFY})
@Lazy
public class NotifyHelper extends NotifyHelperConstant {
    @Autowired
    private NotifyHelperConfiguration configuration;
    @Autowired
    private ObjectMapper objectMapper;
    private HttpClient2 httpClient2;
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    private ExecutorService executor;

    @PostConstruct
    public void initialization() {
        httpClient2 = new HttpClient2(configuration.getHttp());
        executor = ThreadPoolFactory.newFixedThreadPool(
                this.configuration.getExecutorCorePoolSize(),
                this.configuration.getExecutorKeepAliveTime(),
                TimeUnit.MILLISECONDS,
                this.configuration.getExecutorQueueCapacity(),
                new SimpleThreadFactory(this.getClass().getName()),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @PreDestroy
    public void destroy() {
        logger.trace("NotifyHelper start to destroy...");
        ThreadPoolFactory.shutdown(executor, this.getClass().getName());
        httpClient2.destroy();
    }

    /**
     * 依據指定的templateId送mail
     * <p>
     * 注意NotifyTemplate檔中Template欄位要有##Body##
     *
     * @param templateId 模板樣式編號
     * @param to         收件人地址
     * @param body       郵件內容
     * @param async      是否異步發送
     * @return
     * @throws Exception
     */
    public NotifyResponse sendSimpleMail(String templateId, String to, String body, boolean async) throws Exception {
        // parameter
        Map<String, String> paramVars = new HashMap<>();
        paramVars.put(NOTIFY_MESSAGE_CONTENT_BODY, body); // Mail內容
        return this.sendSimpleMail(templateId, to, paramVars, async);
    }

    /**
     * 依據指定的templateId送mail
     * <p>
     * 注意NotifyTemplate檔中Template欄位要有##Body##
     *
     * @param templateId 模板樣式編號
     * @param to         收件人地址
     * @param paramVars  郵件內容中需要替代的變數
     * @param async      是否異步發送
     * @return
     * @throws Exception
     */
    public NotifyResponse sendSimpleMail(String templateId, String to, Map<String, String> paramVars, boolean async) throws Exception {
        logger.info("start to sendSimpleMail,templateId:", templateId, ",to:", to, ",paramVars:", paramVars, ",async:", async);
        // parameter
        Map<String, String> parameter = new HashMap<>();
        parameter.put(NOTIFY_EMAIL_PARM_NAME, to); // 固定變數名稱, 表示接收通知的email address
        if (MapUtils.isNotEmpty(paramVars)) parameter.putAll(paramVars); // 加入自定義的變數
        // NotifyRequestContent
        NotifyRequestContent content = new NotifyRequestContent();
        content.setTemplateId(templateId); // 必填, 模板樣式編號
        content.setContentIndex(UUIDUtil.randomUUID()); // 必填, 訊息處理序列號, 為UUID, 作為辨別查詢處理的發送通知
        content.setParmVars(parameter); // 必填, 參數群組, 裡面為自定義參數(模板和規則詳細參數要用的)
        // NotifyRequestForm
        NotifyRequestForm form = new NotifyRequestForm();
        form.setContents(Collections.singletonList(content)); // 必填
        return this.sendNotify(form, async);
    }

    /**
     * 依據指定的templateId送mail, for BusinessBase呼叫 有帶入Ej
     * <p>
     * 注意NotifyTemplate檔中Template欄位要有##Body##
     *
     * @param templateId 模板樣式編號
     * @param to         收件人地址
     * @param paramVars  郵件內容中需要替代的變數
     * @param async      是否異步發送
     * @param feplogEj   logEj
     * @return
     * @throws Exception
     */
    public NotifyResponse sendATMSimpleMail(String templateId, String to, Map<String, String> paramVars, boolean async, int feplogEj) throws Exception {
        logger.info("start to sendATMSimpleMail,templateId:", templateId, ",to:", to, ",paramVars:", paramVars, ",async:", async);
        // parameter
        Map<String, String> parameter = new HashMap<>();
        parameter.put(NOTIFY_EMAIL_PARM_NAME, to); // 固定變數名稱, 表示接收通知的email address
        if (MapUtils.isNotEmpty(paramVars)) parameter.putAll(paramVars); // 加入自定義的變數
        // NotifyRequestContent
        NotifyRequestContent content = new NotifyRequestContent();
        content.setTemplateId(templateId); // 必填, 模板樣式編號
        content.setContentIndex(UUIDUtil.randomUUID()); // 必填, 訊息處理序列號, 為UUID, 作為辨別查詢處理的發送通知
        content.setParmVars(parameter); // 必填, 參數群組, 裡面為自定義參數(模板和規則詳細參數要用的)
        // NotifyRequestForm
        NotifyRequestForm form = new NotifyRequestForm();
        form.setContents(Collections.singletonList(content)); // 必填
        form.setEJNo(Integer.toString(feplogEj));
        return this.sendNotify(form, async);
    }

    /**
     * 依據指定的templateId送簡訊
     *
     * @param templateId 模板樣式編號
     * @param mp         手機門號
     * @param paramVars  簡訊內容中需要替代的變數
     * @param async      是否異步發送
     * @param feplogEj   logEj
     * @return
     * @throws Exception
     */
    public NotifyResponse sendSimpleSMS(String templateId, String mp, Map<String, String> paramVars, boolean async, int feplogEj) throws Exception {
        logger.info("start to sendSimpleSMS,templateId:", templateId, ",phone:", mp, ",paramVars:", paramVars, ",async:", async);
        // parameter
        Map<String, String> parameter = new HashMap<>();
        parameter.put(NOTIFY_PHONE_PARM_NAME, mp); // 固定變數名稱, 表示接收通知的手機門號
        if (MapUtils.isNotEmpty(paramVars)) parameter.putAll(paramVars); // 加入自定義的變數
        // NotifyRequestContent
        NotifyRequestContent content = new NotifyRequestContent();
        content.setTemplateId(templateId); // 必填, 模板樣式編號
        content.setContentIndex(UUIDUtil.randomUUID()); // 必填, 訊息處理序列號, 為UUID, 作為辨別查詢處理的發送通知
        content.setParmVars(parameter); // 必填, 參數群組, 裡面為自定義參數(模板和規則詳細參數要用的)
        // NotifyRequestForm
        NotifyRequestForm form = new NotifyRequestForm();
        form.setContents(Collections.singletonList(content)); // 必填
        form.setEJNo(Integer.toString(feplogEj));
        return this.sendNotify(form, async);
    }

    /**
     * 依據指定的templateId送簡訊
     *
     * @param templateId 模板樣式編號
     * @param paramVars  簡訊內容中需要替代的變數
     * @param async      是否異步發送
     * @param feplogEj   logEj
     * @return
     * @throws Exception
     */
    //2024-05-02新增Push
    public NotifyResponse sendSimplePush(String templateId, String personId, Map<String, String> paramVars, boolean async, int feplogEj) throws Exception {
        logger.info("start to sendSimplePush, templateId:", templateId, ",paramVars:", paramVars, ",async:", async);
        // parameter
        Map<String, String> parameter = new HashMap<>();
        parameter.put(NOTIFY_PERSONID_PARM_NAME, personId);
        if (MapUtils.isNotEmpty(paramVars)) parameter.putAll(paramVars); // 加入自定義的變數
        // NotifyRequestContent
        NotifyRequestContent content = new NotifyRequestContent();
        content.setTemplateId(templateId); // 必填, 模板樣式編號
        content.setContentIndex(UUIDUtil.randomUUID()); // 必填, 訊息處理序列號, 為UUID, 作為辨別查詢處理的發送通知
        content.setParmVars(parameter); // 必填, 參數群組, 裡面為自定義參數(模板和規則詳細參數要用的)
        // NotifyRequestForm
        NotifyRequestForm form = new NotifyRequestForm();
        form.setContents(Collections.singletonList(content)); // 必填
        form.setEJNo(Integer.toString(feplogEj));
        return this.sendNotify(form, async);
    }

    /**
     * @param clientId 由交易序號+交易日期組成, 如沒有交易序號或交易日期則由訊息通知中心產生UUID
     * @param ej       交易序號, 非必填
     * @param txDate   交易日期, 非必填
     * @return NotifyResponse
     * @throws Exception
     */
    public NotifyResponse logNotify(String clientId, Integer ej, String txDate) throws Exception {
        NotifyRequestForm form = new NotifyRequestForm();
        form.setClientId(clientId);
        if (ej != null)
            form.setEJNo(Integer.toString(ej));
        if (StringUtils.isNotBlank(txDate))
            form.setTXDate(txDate);
        return this.logNotify(form);
    }

    /**
     * 請求傳送訊息通知
     *
     * @param form
     * @param async
     * @return NotifyResponse
     * @throws Exception
     */
    public NotifyResponse sendNotify(final NotifyRequestForm form, final boolean async) throws Exception {
        return this.sendNotify(form, async, null);
    }

    /**
     * 請求非同步傳送訊息通知
     *
     * @param form
     * @return NotifyResponse
     * @throws Exception
     */
    public NotifyResponse sendAsyncNotify(final NotifyRequestForm form) throws Exception {
        return this.sendNotify(form, true, null);
    }

    /**
     * 請求傳送訊息通知
     *
     * @param form
     * @param async
     * @param listener
     * @return
     * @throws Exception
     */
    public NotifyResponse sendNotify(final NotifyRequestForm form, final boolean async, final AsynchronousListener<NotifyResponse> listener) throws Exception {
        if (async) {
            executor.execute(() -> {
                try {
                    NotifyResponse response = this.sendNotify(form);
                    if (listener != null) listener.callback(response);
                } catch (Exception e) {
                    throw ExceptionUtil.createRuntimeException(e);
                }
            });
            return null;
        } else {
            return this.sendNotify(form);
        }
    }

    /**
     * 請求傳送訊息通知
     *
     * @param form
     * @return NotifyResponse
     * @throws Exception
     */
    public NotifyResponse sendNotify(NotifyRequestForm form) throws Exception {
        try {
            String request = objectMapper.writeValueAsString(form);
            String resultStr = httpClient2.postForObject(configuration.getUrl().getSendNotify(), MediaType.APPLICATION_JSON, request, String.class);
            return objectMapper.readValue(resultStr, NotifyResponse.class);
        } catch (Exception e) {
            throw handleException(configuration.getUrl().getSendNotify(), e);
        }
    }

    /**
     * 請求查詢訊息通知
     *
     * @param form
     * @param async
     * @return
     * @throws Exception
     */
    public NotifyResponse logNotify(final NotifyRequestForm form, final boolean async) throws Exception {
        return this.logNotify(form, async, null);
    }

    /**
     * 請求查詢訊息通知
     *
     * @param form
     * @param async
     * @param listener
     * @return
     * @throws Exception
     */
    public NotifyResponse logNotify(final NotifyRequestForm form, final boolean async, final AsynchronousListener<NotifyResponse> listener) throws Exception {
        if (async) {
            executor.execute(() -> {
                try {
                    NotifyResponse response = this.logNotify(form);
                    if (listener != null) listener.callback(response);
                } catch (Exception e) {
                    throw ExceptionUtil.createRuntimeException(e);
                }
            });
            return null;
        } else {
            return this.logNotify(form);
        }
    }

    /**
     * 請求查詢訊息通知
     *
     * @param form
     * @return
     * @throws Exception
     */
    public NotifyResponse logNotify(NotifyRequestForm form) throws Exception {
        try {
            String request = objectMapper.writeValueAsString(form);
            String resultStr = httpClient2.postForObject(configuration.getUrl().getLogNotify(), MediaType.APPLICATION_JSON, request, String.class);
            return objectMapper.readValue(resultStr, NotifyResponse.class);
        } catch (Exception e) {
            throw handleException(configuration.getUrl().getLogNotify(), e);
        }
    }

    /**
     * 處理異常
     *
     * @param url
     * @param e
     * @return
     */
    private Exception handleException(String url, Exception e) {
        if (e instanceof JacksonException) {
            return ExceptionUtil.createException(e, logger.error(e, "訪問中心服務「", url, "」解讀請求/回應訊息出現異常"));
        } else if (HttpResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
            return ExceptionUtil.createException(e, logger.error(e, "訪問通知中心服務「", url, "」被拒絕"));
        } else if (HttpResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
            return ExceptionUtil.createSocketTimeoutException(e, logger.error(e, "等待通知中心服務「", url, "」回應超時"));
        }
        return ExceptionUtil.createException(e, logger.error(e, "訪問通知中心服務「", url, "」發生異常"));
    }

    /**
     * 送中台通知(不同步)
     *
     * @param templateId
     * @param feptxnEjfNo
     * @param feptxnTxDate
     * @param ntfAcctNo
     * @param defMail
     * @param defPhone
     * @param tellerId
     * @param defSms
     * @param defPush
     * @param defEmail
     * @param param1
     * @param param2
     * @param param3
     * @param param4
     * @param param5
     * @return
     * @throws Exception
     */
    public MiddlePlatFormResponse sendSimpleMiddlePlatForm(String templateId, String feptxnEjfNo, String feptxnTxDate, String ntfAcctNo,
                                                           String defMail, String defPhone, String tellerId, String defSms, String defPush, String defEmail,
                                                           String param1, String param2, String param3, String param4, String param5, String param6, String param7) throws Exception {
        return sendSimpleMiddlePlatForm(templateId, feptxnEjfNo, feptxnTxDate, ntfAcctNo, defMail, defPhone, tellerId, defSms, defPush, defEmail, param1, param2, param3, param4, param5, param6, param7, false);
    }

    /**
     * otp送中台通知(同步)
     *
     * @param templateId
     * @param feptxnEjfNo
     * @param feptxnTxDate
     * @param ntfAcctNo
     * @param defMail
     * @param defPhone
     * @param tellerId
     * @param defSms
     * @param defPush
     * @param defEmail
     * @param param1
     * @param param2
     * @param param3
     * @param param4
     * @param param5
     * @return
     * @throws Exception
     */
    public MiddlePlatFormResponse sendSimpleMiddlePlatFormOtp(String templateId, String feptxnEjfNo, String feptxnTxDate, String ntfAcctNo,
                                                              String defMail, String defPhone, String tellerId, String defSms, String defPush, String defEmail,
                                                              String param1, String param2, String param3, String param4, String param5, String param6, String param7) throws Exception {
        return sendSimpleMiddlePlatForm(templateId, feptxnEjfNo, feptxnTxDate, ntfAcctNo, defMail, defPhone, tellerId, defSms, defPush, defEmail, param1, param2, param3, param4, param5, param6, param7, true);
    }

    public MiddlePlatFormResponse sendSimpleMiddlePlatForm(String templateId, String feptxnEjfNo, String feptxnTxDate, String ntfAcctNo,
                                                           String defMail, String defPhone, String tellerId, String defSms, String defPush, String defEmail,
                                                           String param1, String param2, String param3, String param4, String param5, String param6, String param7, boolean async) throws Exception {
        logger.debug("templateId:", templateId, ",phone:", feptxnEjfNo, ",paramVars:", param1, param2, param3, param4, param5, param6, param7);
        // parameter
        NotifyMiddlePlatformRequestForm form = new NotifyMiddlePlatformRequestForm();
        form.setTemplateId(templateId);
        form.setEJNo(feptxnEjfNo);
        form.setTXDate(feptxnTxDate);
        form.setNtfAcctNo(ntfAcctNo);
        form.setDefMail(defMail);
        form.setDefphne_number(defPhone);
        form.setTellerId(tellerId);
        form.setDefSms(defSms);
        form.setDefPush(defPush);
        form.setDefEmail(defEmail);

        form.setParam1(param1);
        form.setParam2(param2);
        form.setParam3(param3);
        form.setParam4(param4);
        form.setParam5(param5);
        form.setParam6(param6);
        form.setParam7(param7);

        try {
            return this.sendSimpleMiddlePlatForm(form, async, null);
        } catch (SocketTimeoutException e) {
            // Handle the timeout exception
            logger.warn("sendSimpleMiddlePlatForm Request timed out", e);
            throw new Exception(e);
        } catch (Exception e) {
            logger.warn("sendSimpleMiddlePlatForm Request failed", e);
            throw new Exception(e);
        }
    }

    public MiddlePlatFormResponse sendSimpleMiddlePlatForm(NotifyMiddlePlatformRequestForm form, boolean async, final AsynchronousListener<MiddlePlatFormResponse> listener) throws Exception {
        if (async) {
            executor.execute(() -> {
                try {
                    MiddlePlatFormResponse response = this.sendMiddlePlatform(form);
                    if (listener != null) listener.callback(response);
                } catch (Exception e) {
                    throw ExceptionUtil.createRuntimeException(e);
                }
            });
            return null;
        } else {
            return this.sendMiddlePlatform(form);
        }
    }

    /**
     * 請求傳送中台通知
     *
     * @param form
     * @return NotifyResponse
     * @throws Exception
     */
    public MiddlePlatFormResponse sendMiddlePlatform(NotifyMiddlePlatformRequestForm form) throws Exception {
        try {
            // Parse request to JSON
            String request = objectMapper.writeValueAsString(form);
            String response = httpClient2.postForObject(configuration.getUrl().getSendMiddlePlatForm(), MediaType.APPLICATION_JSON, request, String.class);
            return objectMapper.readValue(response, MiddlePlatFormResponse.class);
        } catch (Exception e) {
            throw handleException(configuration.getUrl().getSendMiddlePlatForm(), e);
        }
    }

}
