package com.syscom.fep.gateway.netty;

public class NettyTransmissionWriteAndFlushResult {
    private boolean succeed;
    private Throwable error;

    public NettyTransmissionWriteAndFlushResult() {}

    public NettyTransmissionWriteAndFlushResult(boolean succeed) {
        this(succeed, null);
    }

    public NettyTransmissionWriteAndFlushResult(boolean succeed, Throwable error) {
        this.succeed = succeed;
        this.error = error;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
