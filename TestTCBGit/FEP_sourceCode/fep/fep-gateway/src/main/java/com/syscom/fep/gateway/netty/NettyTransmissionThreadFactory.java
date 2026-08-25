package com.syscom.fep.gateway.netty;

import com.syscom.fep.frmcommon.thread.ThreadWrapper;
import io.netty.util.concurrent.DefaultThreadFactory;

public class NettyTransmissionThreadFactory extends DefaultThreadFactory {

    public NettyTransmissionThreadFactory(Class<?> poolType) {
        super(poolType);
    }

    public NettyTransmissionThreadFactory(String poolName) {
        super(poolName);
    }

    public NettyTransmissionThreadFactory(Class<?> poolType, boolean daemon) {
        super(poolType, daemon);
    }

    public NettyTransmissionThreadFactory(String poolName, boolean daemon) {
        super(poolName, daemon);
    }

    public NettyTransmissionThreadFactory(Class<?> poolType, int priority) {
        super(poolType, priority);
    }

    public NettyTransmissionThreadFactory(String poolName, int priority) {
        super(poolName, priority);
    }

    public NettyTransmissionThreadFactory(Class<?> poolType, boolean daemon, int priority) {
        super(poolType, daemon, priority);
    }

    public NettyTransmissionThreadFactory(String poolName, boolean daemon, int priority, ThreadGroup threadGroup) {
        super(poolName, daemon, priority, threadGroup);
    }

    public NettyTransmissionThreadFactory(String poolName, boolean daemon, int priority) {
        super(poolName, daemon, priority);
    }

    @Override
    public Thread newThread(Runnable r) {
        return super.newThread(ThreadWrapper.wrap(r, false));
    }

    @Override
    protected Thread newThread(Runnable r, String name) {
        return super.newThread(ThreadWrapper.wrap(r, false), name);
    }
}
