package com.syscom.fep.invoker.netty;

import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;

public class SimpleNettyServerConfiguration extends SimpleNettyConfiguration {
    /**
     * BOSS線程數, 一般是有幾個protocol port設定幾個
     */
    private int bossThreadNum = 1;
    /**
     * WROKER線程數, 預設CPU核心數*2, 一般不需要特別設定
     */
    private int serverWorkerThreadNum = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
    /**
     * 業務線程數, 預設0, 根據業務需要設定
     */
    private int serverBisThreadNum = 0;
    private long readerIdleTime = 120000L;
    private long writerIdleTime = 0L;
    private int backlog;
    private String acceptIp;

    public int getBossThreadNum() {
        return bossThreadNum;
    }

    public void setBossThreadNum(int bossThreadNum) {
        this.bossThreadNum = bossThreadNum;
    }

    public int getServerWorkerThreadNum() {
        return serverWorkerThreadNum;
    }

    public void setServerWorkerThreadNum(int serverWorkerThreadNum) {
        this.serverWorkerThreadNum = serverWorkerThreadNum;
    }

    public int getServerBisThreadNum() {
        return serverBisThreadNum;
    }

    public void setServerBisThreadNum(int serverBisThreadNum) {
        this.serverBisThreadNum = serverBisThreadNum;
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

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public String getAcceptIp() {
        return acceptIp;
    }

    public void setAcceptIp(String acceptIp) {
        this.acceptIp = acceptIp;
    }
}
