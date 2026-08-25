package com.syscom.fep.invoker.netty;

import com.syscom.fep.frmcommon.thread.ThreadWrapper;
import io.netty.util.concurrent.DefaultThreadFactory;

public class SimpleNettyThreadFactory extends DefaultThreadFactory {

    public SimpleNettyThreadFactory(Class<?> poolType) {
        super(poolType);
    }

    public SimpleNettyThreadFactory(String poolName) {
        super(poolName);
    }

    public SimpleNettyThreadFactory(Class<?> poolType, boolean daemon) {
        super(poolType, daemon);
    }

    public SimpleNettyThreadFactory(String poolName, boolean daemon) {
        super(poolName, daemon);
    }

    public SimpleNettyThreadFactory(Class<?> poolType, int priority) {
        super(poolType, priority);
    }

    public SimpleNettyThreadFactory(String poolName, int priority) {
        super(poolName, priority);
    }

    public SimpleNettyThreadFactory(Class<?> poolType, boolean daemon, int priority) {
        super(poolType, daemon, priority);
    }

    public SimpleNettyThreadFactory(String poolName, boolean daemon, int priority, ThreadGroup threadGroup) {
        super(poolName, daemon, priority, threadGroup);
    }

    public SimpleNettyThreadFactory(String poolName, boolean daemon, int priority) {
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
