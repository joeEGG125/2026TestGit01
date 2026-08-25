package com.syscom.fep.common.sms.hiair;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.delegate.ActionListener;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "spring.fep.sms.hiair", name = "enable", havingValue = "true")
@Lazy
public class HiairSmsOperator {
    private static final String ProgramName = HiairSmsOperator.class.getSimpleName();
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private HiairSmsConfiguration hiairSmsConfiguration;
    private ExecutorService executor;

    @PostConstruct
    public void postConstruct() {
        executor = ThreadPoolFactory.newFixedThreadPool(
                this.hiairSmsConfiguration.getExecutorCorePoolSize(),
                this.hiairSmsConfiguration.getExecutorKeepAliveTime(),
                TimeUnit.MILLISECONDS,
                this.hiairSmsConfiguration.getExecutorQueueCapacity(),
                new SimpleThreadFactory("HiairSmsOperatorExecutor"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @PreDestroy
    public void preDestroy() {
        ThreadPoolFactory.shutdown(executor, "HiairSmsOperatorExecutor");
    }

    /**
     * 發送簡訊
     *
     * @param mobiles
     * @param message
     * @return
     */
    public List<HiairSmsBaseResponse> send(String[] mobiles, String message) {
        return send(mobiles, message, false, null);
    }

    /**
     * 發送簡訊
     *
     * @param mobiles
     * @param message
     * @param async
     * @return
     */
    public List<HiairSmsBaseResponse> send(String[] mobiles, String message, boolean async) {
        return send(mobiles, message, null, async, null);
    }

    /**
     * 發送簡訊
     *
     * @param mobiles
     * @param message
     * @param async
     * @param callback
     * @return
     */
    public List<HiairSmsBaseResponse> send(String[] mobiles, String message, boolean async, ActionListener<HiairSmsBaseResponse> callback) {
        return send(mobiles, message, null, async, callback);
    }

    /**
     * 發生簡訊並指定預約的時間
     *
     * @param mobiles
     * @param message
     * @param orderTime
     * @return
     */
    public List<HiairSmsBaseResponse> send(String[] mobiles, String message, Calendar orderTime, boolean async, ActionListener<HiairSmsBaseResponse> callback) {
        switch (hiairSmsConfiguration.getProtocol()) {
            case HTTP:
                return sendViaHttp(mobiles, message, orderTime, async, callback);
            case SOCKET:
                return sendViaSocket(mobiles, message, orderTime, async, callback);
            default:
                throw ExceptionUtil.createIllegalArgumentException("Unsupported protocol: ", hiairSmsConfiguration.getProtocol());
        }
    }

    private List<HiairSmsBaseResponse> sendViaSocket(String[] mobiles, String message, Calendar orderTime, boolean async, ActionListener<HiairSmsBaseResponse> callback) {
        logger.debug("[", ProgramName, "][", StringUtils.join(hiairSmsConfiguration.getSocketHost(), ":", hiairSmsConfiguration.getSocketPort()), "]mobiles:[", StringUtils.join(mobiles, ","), "], default mobile:", hiairSmsConfiguration.getMobile());
        HiairSmsSocketApi socketApi = new HiairSmsSocketApi();
        try {
            // 建立連線 and 檢查帳號密碼是否錯誤
            int ret_code = socketApi.create_conn(hiairSmsConfiguration.getSocketHost(), hiairSmsConfiguration.getSocketPort(), hiairSmsConfiguration.getUsername(), hiairSmsConfiguration.getSscode(), hiairSmsConfiguration.getSocketSoTimeout());
            if (ret_code == 0) {
                logger.debug("[", ProgramName, "][", socketApi.getRemoteAddress(), "][", socketApi.getLocalAddress(), "][", ret_code, "]帳號密碼Login OK!");
            } else {
                logger.error("[", ProgramName, "][", ret_code, "]帳號密碼Login Fail! ", socketApi.get_message());
                return Collections.singletonList(new HiairSmsSocketResponse(null, ret_code, socketApi.get_message()));
            }
            // String smsMsg = new String(message.getBytes(), "Big5"); // 簡訊內容必須要轉成Big5
            String smsMsg = message;
            smsMsg = StringUtil.substring(smsMsg, hiairSmsConfiguration.getSocketMessageBytesLengthLimit(), "Big5"); // 避免超過最大長度限制
            if (ArrayUtils.isNotEmpty(mobiles) && Arrays.stream(mobiles).anyMatch(StringUtils::isNotBlank)) {
                logger.debug("[", ProgramName, "][", StringUtils.join(hiairSmsConfiguration.getSocketHost(), ":", hiairSmsConfiguration.getSocketPort()), "]Send SMS using mobile:[", StringUtils.join(mobiles, ","), "]");
                List<HiairSmsBaseResponse> list = new ArrayList<>();
                for (String mobile : mobiles) {
                    if (StringUtils.isNotBlank(mobile))
                        list.add(sendReceiveViaSocket(socketApi, mobile, smsMsg, orderTime, async, callback));
                }
                return list;
            }
            // 如果沒有傳入mobiles, 則使用預設的手機門號
            logger.debug("[", ProgramName, "][", StringUtils.join(hiairSmsConfiguration.getSocketHost(), ":", hiairSmsConfiguration.getSocketPort()), "]Send SMS using default mobile:", hiairSmsConfiguration.getMobile());
            return Collections.singletonList(sendReceiveViaSocket(socketApi, hiairSmsConfiguration.getMobile(), smsMsg, orderTime, async, callback));
        } catch (Exception e) {
            logger.error(e, "sendViaSocket failed, ", e.getMessage());
            return Collections.singletonList(new HiairSmsSocketResponse(e));
        } finally {
            //結束連線
            socketApi.close_conn();
        }
    }

    private HiairSmsSocketResponse sendReceiveViaSocket(HiairSmsSocketApi socketApi, String mobile, String smsMsg, Calendar orderTime, boolean async, ActionListener<HiairSmsBaseResponse> callback) {
        if (async) {
            executor.execute(() -> {
                HiairSmsSocketResponse response = sendReceiveViaSocket(socketApi, mobile, smsMsg, orderTime, false, null);
                if (callback != null)
                    callback.actionPerformed(response);
            });
            return null;
        }
        int ret_code;
        // 傳送文字簡訊
        if (orderTime == null) {
            ret_code = socketApi.send_text_message(mobile, smsMsg);
        }
        // 傳送預約文字簡訊
        else {
            ret_code = socketApi.send_text_message(mobile, smsMsg, convertToOrderTime(orderTime));
        }
        if (ret_code == 0) {
            logger.debug("[", ProgramName, "][", socketApi.getRemoteAddress(), "][", socketApi.getLocalAddress(), "][", mobile, "][", ret_code, "]簡訊已送到簡訊中心!, MessageID=", socketApi.get_message()); // 取得MessageID
        } else {
            logger.error("[", ProgramName, "][", socketApi.getRemoteAddress(), "][", socketApi.getLocalAddress(), "][", mobile, "][", ret_code, "]簡訊傳送發生錯誤! ", socketApi.get_message()); // 取得錯誤的訊息
        }
        return new HiairSmsSocketResponse(mobile, ret_code, socketApi.get_message() != null ? new String(socketApi.get_message().toCharArray()) : StringUtils.EMPTY);
    }

    private List<HiairSmsBaseResponse> sendViaHttp(String[] mobiles, String smsMsg, Calendar orderTime, boolean async, ActionListener<HiairSmsBaseResponse> callback) {
        HiairSmsHttpApi httpApi = new HiairSmsHttpApi();
        if (ArrayUtils.isNotEmpty(mobiles)) {
            List<HiairSmsBaseResponse> list = new ArrayList<>();
            for (String mobile : mobiles) {
                list.add(sendReceiveViaHttp(httpApi, mobile, smsMsg, orderTime, async, callback));
            }
            return list;
        }
        // 如果沒有傳入mobiles, 則使用預設的手機門號
        return Collections.singletonList(sendReceiveViaHttp(httpApi, hiairSmsConfiguration.getMobile(), smsMsg, orderTime, async, callback));
    }

    private HiairSmsHttpResponse sendReceiveViaHttp(HiairSmsHttpApi httpApi, String mobile, String message, Calendar orderTime, boolean async, ActionListener<HiairSmsBaseResponse> callback) {
        if (async) {
            executor.execute(() -> {
                HiairSmsHttpResponse response = sendReceiveViaHttp(httpApi, mobile, message, orderTime, false, null);
                if (callback != null)
                    callback.actionPerformed(response);
            });
            return null;
        }
        HiairSmsHttpSendRequest request = new HiairSmsHttpSendRequest();
        request.setUserName(hiairSmsConfiguration.getUsername());
        request.setSscode(hiairSmsConfiguration.getSscode());
        request.setLimitTime(hiairSmsConfiguration.getLimitTime());
        request.setMobile(mobile);
        // 如果含有中文, 判斷長度不能超過最大值, 如果超過要截斷避免後面post收到錯誤的回應訊息
        if (StringUtil.containsChinese(message) && message.length() > hiairSmsConfiguration.getHttpMessageLengthLimit()) {
            request.setMessage(message.substring(0, hiairSmsConfiguration.getHttpMessageLengthLimit()));
        }
        // 如果不含有中文, 判斷長度不能超過最大值, 如果超過要截斷避免後面post收到錯誤的回應訊息
        else if (message.length() > hiairSmsConfiguration.getHttpMessageAlphaNumericLengthLimit()) {
            request.setMessage(message.substring(0, hiairSmsConfiguration.getHttpMessageAlphaNumericLengthLimit()));
        } else {
            request.setMessage(message);
        }
        if (orderTime != null)
            request.setOrderTime(convertToOrderTime(orderTime));
        return httpApi.httpPost(hiairSmsConfiguration, hiairSmsConfiguration.getHttpUrlSendSMS(), request);
    }

    private String convertToOrderTime(Calendar orderTime) {
        return orderTime == null ? null : FormatUtil.dateTimeFormat(orderTime, "yyMMddHHmmss");
    }
}
