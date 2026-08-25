package com.syscom.fep.frmcommon.netty;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 修改線程數請求對象
 *
 * @author Richard
 */
public class NettyEventExecutorRequest implements Serializable {
    /**
     * 要修改的線程數
     */
    private int threads;
    /**
     * 是否確定要修改線程數(預設為true)
     */
    private boolean modified = true;

    public NettyEventExecutorRequest() {}

    public NettyEventExecutorRequest(int threads) {
        this.threads = threads;
    }

    public NettyEventExecutorRequest(int threads, boolean modified) {
        this.threads = threads;
        this.modified = modified;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
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
