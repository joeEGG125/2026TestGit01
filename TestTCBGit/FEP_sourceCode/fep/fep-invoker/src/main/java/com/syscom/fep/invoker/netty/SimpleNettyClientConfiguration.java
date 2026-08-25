package com.syscom.fep.invoker.netty;

import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;

public class SimpleNettyClientConfiguration extends SimpleNettyConfiguration {
    /**
     * WROKER線程數, 預設CPU核心數*2, 一般不需要特別設定
     */
    private int clientWorkerThreadNum = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
    /**
     * 業務線程數, 預設0, 根據業務需要設定
     */
    private int clientBisThreadNum = 0;
    private long readerIdleTime = 0L;
    private long writerIdleTime = 120000L;

    public int getClientWorkerThreadNum() {
        return clientWorkerThreadNum;
    }

    public void setClientWorkerThreadNum(int clientWorkerThreadNum) {
        this.clientWorkerThreadNum = clientWorkerThreadNum;
    }

    public int getClientBisThreadNum() {
        return clientBisThreadNum;
    }

    public void setClientBisThreadNum(int clientBisThreadNum) {
        this.clientBisThreadNum = clientBisThreadNum;
    }

    public long getReaderIdleTime() {
        return readerIdleTime;
    }

    public void setReaderIdleTime(long readerIdleTime) {
        this.readerIdleTime = readerIdleTime;
    }

    public long getWriterIdleTime() {
        return writerIdleTime;
    }

    public void setWriterIdleTime(long writerIdleTime) {
        this.writerIdleTime = writerIdleTime;
    }
}
