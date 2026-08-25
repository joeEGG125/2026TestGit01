package com.syscom.fep.frmcommon.jms.entity;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class JmsMonitorResponse<T extends Serializable> implements Serializable {
    private boolean result = true;
    private String error;
    private JmsMonitorResponseErrorCode errorCode;
    private T data;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        if (StringUtils.isNotBlank(error))
            this.setResult(false);
    }

    public JmsMonitorResponseErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(JmsMonitorResponseErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
