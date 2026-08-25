package com.syscom.fep.invoker.netty;

import com.syscom.fep.frmcommon.util.ReflectUtil;
import io.netty.channel.Channel;
import io.netty.util.concurrent.*;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.event.Level;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class SimpleNettyEventExecutor extends SingleThreadEventExecutor {
    static final int DEFAULT_MAX_PENDING_EXECUTOR_TASKS = Math.max(16, SystemPropertyUtil.getInt("io.netty.eventexecutor.maxPendingTasks", Integer.MAX_VALUE));
    static final int ST_NOT_STARTED = 1;
    static final int ST_STARTED = 2;
    static final int ST_SHUTTING_DOWN = 3;
    static final int ST_SHUTDOWN = 4;
    static final int ST_TERMINATED = 5;

    private final Set<String> bindChannelIds = Collections.synchronizedSet(new HashSet<>());
    private final String name;

    public SimpleNettyEventExecutor(String name) {
        this(name, (EventExecutorGroup) null);
    }

    public SimpleNettyEventExecutor(String name, ThreadFactory threadFactory) {
        this(name, null, threadFactory);
    }

    public SimpleNettyEventExecutor(String name, Executor executor) {
        this(name, null, executor);
    }

    public SimpleNettyEventExecutor(String name, EventExecutorGroup parent) {
        this(name, parent, new SimpleNettyThreadFactory(DefaultEventExecutor.class));
    }

    public SimpleNettyEventExecutor(String name, EventExecutorGroup parent, ThreadFactory threadFactory) {
        super(parent, threadFactory, true);
        this.name = name;
    }

    public SimpleNettyEventExecutor(String name, EventExecutorGroup parent, Executor executor) {
        super(parent, executor, true);
        this.name = name;
    }

    public SimpleNettyEventExecutor(String name, EventExecutorGroup parent, ThreadFactory threadFactory, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, threadFactory, true, maxPendingTasks, rejectedExecutionHandler);
        this.name = name;
    }

    public SimpleNettyEventExecutor(String name, EventExecutorGroup parent, Executor executor, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, true, maxPendingTasks, rejectedExecutionHandler);
        this.name = name;
    }

    @Override
    protected void run() {
        for (; ; ) {
            Runnable task = takeTask();
            if (task != null) {
                task.run();
                updateLastExecutionTime();
            }

            if (confirmShutdown()) {
                break;
            }
        }
    }

    int getState() {
        return ReflectUtil.getFieldValue(this, "state", ST_NOT_STARTED);
    }

    public void bindChannelId(Channel channel) {
        if (channel != null) {
            String channelId = channel.id().asLongText();
            if (this.bindChannelIds.add(channelId)) {
                SimpleNettyBaseMethod.logMessage(Level.INFO, name, channel, "Bind Channel ID to Executor succeed, channelId = [", channelId, "]");
            } else {
                SimpleNettyBaseMethod.logMessage(Level.WARN, name, channel, "Duplicate Channel ID, cannot bind to Executor again, channelId = [", channelId, "]");
            }
        }
    }

    public void unbindChannelId(Channel channel) {
        if (channel != null) {
            String channelId = channel.id().asLongText();
            if (this.bindChannelIds.remove(channelId)) {
                SimpleNettyBaseMethod.logMessage(Level.INFO, name, channel, "Unbind Channel ID from Executor succeed, channelId = [", channelId, "]");
            } else {
                SimpleNettyBaseMethod.logMessage(Level.WARN, name, channel, "Channel ID not exist, cannot unbind from Executor, channelId = [", channelId, "]");
            }
        }
    }

    public boolean hasBindChannelId() {
        return !this.hasNotBindChannelId();
    }

    public boolean hasNotBindChannelId() {
        return this.bindChannelIds.isEmpty();
    }
}
