package com.syscom.fep.frmcommon.netty;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class NettyEventExecutorData implements Serializable {
    /**
     * 設定的線程數
     */
    private int executorSetupCount;
    /**
     * 活動中的線程數
     */
    private int executorActiveCount;
    /**
     * 空閒中的線程數
     */
    private int executorIdleCount;
    /**
     * 設定檔中的線程數(預設值)
     */
    private int executorConfigurationCount;

    public int getExecutorSetupCount() {
        return executorSetupCount;
    }

    public void setExecutorSetupCount(int executorSetupCount) {
        this.executorSetupCount = executorSetupCount;
    }

    public int getExecutorActiveCount() {
        return executorActiveCount;
    }

    public void setExecutorActiveCount(int executorActiveCount) {
        this.executorActiveCount = executorActiveCount;
    }

    public int getExecutorIdleCount() {
        return executorIdleCount;
    }

    public void setExecutorIdleCount(int executorIdleCount) {
        this.executorIdleCount = executorIdleCount;
    }

    public int getExecutorConfigurationCount() {
        return executorConfigurationCount;
    }

    public void setExecutorConfigurationCount(int executorConfigurationCount) {
        this.executorConfigurationCount = executorConfigurationCount;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
