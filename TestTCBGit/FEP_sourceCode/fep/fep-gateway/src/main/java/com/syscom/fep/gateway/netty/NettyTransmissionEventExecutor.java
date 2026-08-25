package com.syscom.fep.gateway.netty;

import com.syscom.fep.frmcommon.util.ReflectUtil;
import io.netty.channel.Channel;
import io.netty.util.concurrent.*;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class NettyTransmissionEventExecutor extends SingleThreadEventExecutor {
    static final int DEFAULT_MAX_PENDING_EXECUTOR_TASKS = Math.max(16, SystemPropertyUtil.getInt("io.netty.eventexecutor.maxPendingTasks", Integer.MAX_VALUE));
    static final int ST_NOT_STARTED = 1;
    static final int ST_STARTED = 2;
    static final int ST_SHUTTING_DOWN = 3;
    static final int ST_SHUTDOWN = 4;
    static final int ST_TERMINATED = 5;

    private final Set<String> bindChannelIds = Collections.synchronizedSet(new HashSet<>());

    public NettyTransmissionEventExecutor() {
        this((EventExecutorGroup) null);
    }

    public NettyTransmissionEventExecutor(ThreadFactory threadFactory) {
        this(null, threadFactory);
    }

    public NettyTransmissionEventExecutor(Executor executor) {
        this(null, executor);
    }

    public NettyTransmissionEventExecutor(EventExecutorGroup parent) {
        this(parent, new NettyTransmissionThreadFactory(DefaultEventExecutor.class));
    }

    public NettyTransmissionEventExecutor(EventExecutorGroup parent, ThreadFactory threadFactory) {
        super(parent, threadFactory, true);
    }

    public NettyTransmissionEventExecutor(EventExecutorGroup parent, Executor executor) {
        super(parent, executor, true);
    }

    public NettyTransmissionEventExecutor(EventExecutorGroup parent, ThreadFactory threadFactory, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, threadFactory, true, maxPendingTasks, rejectedExecutionHandler);
    }

    public NettyTransmissionEventExecutor(EventExecutorGroup parent, Executor executor, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, true, maxPendingTasks, rejectedExecutionHandler);
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
                NettyTransmissionUtil.infoMessage(channel, "Bind Channel ID to Executor succeed, channelId = [", channelId, "]");
            } else {
                NettyTransmissionUtil.warnMessage(channel, "Duplicate Channel ID, cannot bind to Executor again, channelId = [", channelId, "]");
            }
        }
    }

    public void unbindChannelId(Channel channel) {
        if (channel != null) {
            String channelId = channel.id().asLongText();
            if (this.bindChannelIds.remove(channelId)) {
                NettyTransmissionUtil.infoMessage(channel, "Unbind Channel ID from Executor succeed, channelId = [", channelId, "]");
            } else {
                NettyTransmissionUtil.warnMessage(channel, "Channel ID not exist, cannot unbind from Executor, channelId = [", channelId, "]");
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
