package com.syscom.fep.frmcommon.jms.entity;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 請求修改線程數
 *
 * @author Richard
 */
public class JmsMonitorConcurrencyRequest implements Serializable {
    /**
     * 要修改的線程數
     */
    private String concurrency;
    /**
     * 是否確定要修改線程數(預設為true)
     */
    private boolean modified = true;

    public JmsMonitorConcurrencyRequest() {}

    public JmsMonitorConcurrencyRequest(String concurrency) {
        this.concurrency = concurrency;
    }

    public JmsMonitorConcurrencyRequest(String concurrency, boolean modified) {
        this.concurrency = concurrency;
        this.modified = modified;
    }

    public String getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(String concurrency) {
        this.concurrency = concurrency;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}