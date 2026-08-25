package com.syscom.fep.common.sms.hiair;

import java.io.Serializable;

public class HiairSmsBaseResponse implements Serializable {
    private String mobile;
    private boolean result = true;
    private transient Throwable error;

    public HiairSmsBaseResponse() {}

    public HiairSmsBaseResponse(Throwable error) {
        this.result = false;
        this.error = error;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
