package com.syscom.fep.notify.exception;

import com.syscom.fep.notify.enums.NotifyStatusCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Map;

import static com.syscom.fep.notify.cnst.NotifyConstant.NOTIFY_MESSAGE_DESC;

public class NotifyException extends Exception {
    @Getter
    private String code;
    @Getter
    private String ejNo;
    @Getter
    private String txDate;
    @Getter
    @Setter
    private String clientId;
    @Getter
    private Map<String, Object> msg;

    public NotifyException() {
        super();
    }

    public NotifyException(String message) {
        super(message);
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, message);
        = Collections.singletonMap(NOTIFY_MESSAGE_DESC, message);

    }


    public NotifyException(NotifyStatusCode notifyCode, String clientId, String errorMsg) {
        this.clientId = clientId;
        this.code = notifyCode.getCode();
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, errorMsg);
        = Collections.singletonMap(NOTIFY_MESSAGE_DESC, errorMsg);
    }

    public NotifyException(NotifyStatusCode notifyCode, String errorMsg) {
        this.code = notifyCode.getCode();
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, errorMsg);
                = Collections.singletonMap(NOTIFY_MESSAGE_DESC, errorMsg);
    }

    public NotifyException(NotifyStatusCode notifyCode, String clientId, Map<String, Object> msg) {
        this.code = notifyCode.getCode();
        this.clientId = clientId;
        this.msg = msg;
    }

    public NotifyException(RuntimeException e, NotifyStatusCode notifyCode, String clientId) {
        this.code = notifyCode.getCode();
        this.clientId = clientId;
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, e.fillInStackTrace().toString());
        = Collections.singletonMap(NOTIFY_MESSAGE_DESC, e.fillInStackTrace().toString());
    }

    public NotifyException(Throwable e, NotifyStatusCode notifyCode, String clientId) {
        this.code = notifyCode.getCode();
        this.clientId = clientId;
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, e.fillInStackTrace().toString());
        = Collections.singletonMap(NOTIFY_MESSAGE_DESC, ((NotifyException) e).msg.get(NOTIFY_MESSAGE_DESC).toString());
    }

    public NotifyException(Throwable e, NotifyStatusCode notifyCode, String ejNo, String txDate) {
        this.code = notifyCode.getCode();
        this.ejNo = ejNo;
        this.txDate = txDate;
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, e.fillInStackTrace().toString());
                = Collections.singletonMap(NOTIFY_MESSAGE_DESC, ((NotifyException) e).msg.get(NOTIFY_MESSAGE_DESC).toString());
    }
}
