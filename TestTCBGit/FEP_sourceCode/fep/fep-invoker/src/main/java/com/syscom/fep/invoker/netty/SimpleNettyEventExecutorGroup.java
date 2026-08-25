package com.syscom.fep.invoker.netty;

import com.syscom.fep.common.log.LogHelperFactory;
import io.netty.util.concurrent.*;
import io.netty.util.concurrent.EventExecutorChooserFactory.EventExecutorChooser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.util.internal.ObjectUtil.checkPositive;

public class SimpleNettyEventExecutorGroup extends AbstractEventExecutorGroup {
    private static final int MAX_LOOPS = Integer.MAX_VALUE; // 2025-04-09 Richard add for [Unchecked Input for Loop Condition]
    private final List<EventExecutor> children;
    private final AtomicInteger terminatedChildren = new AtomicInteger();
    private final Promise<?> terminationFuture = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
    private final SimpleNettyEventExecutorChooserFactory chooserFactory;
    private EventExecutorChooser chooser;
    private final Object[] args;
    private final Executor executor;
    private final FutureListener<Object> terminationListener;

    static final long DEFAULT_SHUTDOWN_QUIET_PERIOD = 2;
    static final long DEFAULT_SHUTDOWN_TIMEOUT = 15;
    private final String name;
    private int nThreads;
    private IdleEventExecutorChecker checker;

    public SimpleNettyEventExecutorGroup(String name, int nThreads, long idleCheckInterval) {
        this(name, nThreads, idleCheckInterval, null);
    }

    public SimpleNettyEventExecutorGroup(String name, int nThreads, long idleCheckInterval, ThreadFactory threadFactory) {
        this(name, nThreads, idleCheckInterval, threadFactory, SimpleNettyEventExecutor.DEFAULT_MAX_PENDING_EXECUTOR_TASKS, RejectedExecutionHandlers.reject());
    }

    public SimpleNettyEventExecutorGroup(String name, int nThreads, long idleCheckInterval, ThreadFactory threadFactory, int maxPendingTasks, RejectedExecutionHandler rejectedHandler) {
        this(name, nThreads, idleCheckInterval, threadFactory, new Object[] {maxPendingTasks, rejectedHandler});
    }

    protected SimpleNettyEventExecutorGroup(String name, int nThreads, long idleCheckInterval, ThreadFactory threadFactory, Object... args) {
        this(name, nThreads, idleCheckInterval, threadFactory == null ? null : new ThreadPerTaskExecutor(threadFactory), args);
    }

    protected SimpleNettyEventExecutorGroup(String name, int nThreads, long idleCheckInterval, Executor executor, Object... args) {
        this(name, nThreads, idleCheckInterval, executor, SimpleNettyEventExecutorChooserFactory.INSTANCE, args);
    }

    protected SimpleNettyEventExecutorGroup(String name, int nThreads, long idleCheckInterval, Executor executor, SimpleNettyEventExecutorChooserFactory chooserFactory, Object... args) {
        checkPositive(nThreads, "nThreads");
        this.name = name;
        this.args = args;
        this.nThreads = nThreads;
        if (executor == null) {
            this.executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
        } else {
            this.executor = executor;
        }
        this.children = new ArrayList<>(nThreads);
        this.addNewChildExecutor(nThreads);
        this.chooserFactory = chooserFactory;
        this.chooser = this.chooserFactory.newChooser(this.children);
        this.terminationListener = future -> {
            synchronized (this.children) {
                if (this.terminatedChildren.incrementAndGet() == this.children.size()) {
                    this.terminationFuture.setSuccess(null);
                }
            }
        };
        for (EventExecutor e : this.children) {
            e.terminationFuture().addListener(this.terminationListener);
        }
        if (idleCheckInterval > 0) {
            this.checker = new IdleEventExecutorChecker(idleCheckInterval);
            this.executor.execute(this.checker);
        }
    }

    private void addNewChildExecutor(int nThreads) {
        synchronized (this.children) {
            // 2025-04-09 Richard add start for [Unchecked Input for Loop Condition]
            if (nThreads > MAX_LOOPS) {
                nThreads = MAX_LOOPS;
            }
            // 2025-04-09 Richard add end for [Unchecked Input for Loop Condition]
            for (int i = 0; i < nThreads; i++) {
                boolean success = false;
                try {
                    this.children.add(newChild(this.executor, this.args));
                    success = true;
                } catch (Exception e) {
                    // TODO: Think about if this is a good exception type
                    throw new IllegalStateException("failed to create a child event loop", e);
                } finally {
                    if (!success) {
                        for (int j = 0; j < i; j++) {
                            this.children.get(j).shutdownGracefully();
                        }
                        for (int j = 0; j < i; j++) {
                            EventExecutor e = this.children.get(j);
                            try {
                                while (!e.isTerminated()) {
                                    e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                                }
                            } catch (InterruptedException interrupted) {
                                // Let the caller handle the interruption.
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    protected ThreadFactory newDefaultThreadFactory() {
        return new SimpleNettyThreadFactory(getClass());
    }

    @Override
    public EventExecutor next() {
        return this.chooser.next();
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        synchronized (this.children) {
            return this.children.iterator();
        }
    }

    public final int executorCount() {
        synchronized (this.children) {
            return this.children.size();
        }
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        if (this.checker != null) this.checker.destroy();
        synchronized (this.children) {
            for (EventExecutor l : this.children) {
                l.shutdownGracefully(quietPeriod, timeout, unit);
            }
        }
        return terminationFuture();
    }

    @Override
    public Future<?> terminationFuture() {
        return this.terminationFuture;
    }

    @Override
    @Deprecated
    public void shutdown() {
        if (this.checker != null) this.checker.destroy();
        synchronized (this.children) {
            for (EventExecutor l : this.children) {
                l.shutdown();
            }
        }
    }

    @Override
    public boolean isShuttingDown() {
        synchronized (this.children) {
            for (EventExecutor l : this.children) {
                if (!l.isShuttingDown()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isShutdown() {
        synchronized (this.children) {
            for (EventExecutor l : this.children) {
                if (!l.isShutdown()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isTerminated() {
        synchronized (this.children) {
            for (EventExecutor l : this.children) {
                if (!l.isTerminated()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (this.checker != null) this.checker.destroy();
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        loop:
        synchronized (this.children) {
            for (EventExecutor l : this.children) {
                for (; ; ) {
                    long timeLeft = deadline - System.nanoTime();
                    if (timeLeft <= 0) {
                        break loop;
                    }
                    if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
                        break;
                    }
                }
            }
        }
        return isTerminated();
    }

    protected EventExecutor newChild(Executor executor, Object... args) throws Exception {
        return new SimpleNettyEventExecutor(this.name, this, executor, (Integer) args[0], (RejectedExecutionHandler) args[1]);
    }

    /**
     * 重新設定線程數
     *
     * @param nThreads
     */
    public boolean setExecutorThreads(int nThreads) {
        synchronized (this.children) {
            this.nThreads = nThreads;
            int val = nThreads - this.children.size();
            // 如果設定的線程數比現有的多, 則需要新增
            if (val > 0) {
                this.addNewChildExecutor(val);
                for (int i = val; i < this.children.size(); i++) {
                    this.children.get(i).terminationFuture().addListener(this.terminationListener);
                }
                this.chooser = this.chooserFactory.newChooser(this.children);
            }
            // 減少線程數
            else if (val < 0) {
                // 先看一下哪些EventExecutor有綁定Channel, 也就是說已經有在使用中
                List<EventExecutor> kept = new ArrayList<>();
                for (EventExecutor l : this.children) {
                    // 有綁定Channel的不能shutdown, 並且要保留
                    if (((SimpleNettyEventExecutor) l).hasBindChannelId()) {
                        kept.add(l);
                        continue;
                    }
                    l.shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
                }
                this.children.clear();
                this.children.addAll(kept);
                // 看下還有沒有剩餘的
                int remaining = nThreads - this.children.size();
                // 如果線程數不夠, 則繼續新增
                if (remaining > 0) {
                    this.addNewChildExecutor(remaining);
                    for (int i = remaining; i < this.children.size(); i++) {
                        this.children.get(i).terminationFuture().addListener(this.terminationListener);
                    }
                }
                this.chooser = this.chooserFactory.newChooser(this.children);
            }
        }
        return true;
    }

    /**
     * 當前設定的線程數
     *
     * @return
     */
    public int executorSetupCount() {
        return this.nThreads;
    }

    /**
     * 活動中的線程數
     *
     * @return
     */
    public int executorActiveCount() {
        int activeCount = 0;
        synchronized (this.children) {
            for (EventExecutor l : this.children) {
                if (((SimpleNettyEventExecutor) l).getState() == SimpleNettyEventExecutor.ST_STARTED)
                    activeCount++;
            }
        }
        return activeCount;
    }

    /**
     * 空閒中的線程數
     *
     * @return
     */
    public int executorIdleCount() {
        return this.executorCount() - this.executorActiveCount();
    }

    /**
     * 定時監測是否有空閒的EventExecutor, 如果有的話移除掉直到數量和nThreads相等
     */
    private class IdleEventExecutorChecker implements Runnable {
        private boolean running = true;
        private final Object lock = new Object();
        private final long idleCheckInterval;

        public IdleEventExecutorChecker(long idleCheckInterval) {
            this.idleCheckInterval = idleCheckInterval;
        }

        public void destroy() {
            LogHelperFactory.getTraceLogger().info("[IdleEventExecutorChecker][destroy]destroy, idleCheckInterval = [", this.idleCheckInterval, "]");
            this.running = false;
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        /**
         * Runs this operation.
         */
        @Override
        public void run() {
            LogHelperFactory.getTraceLogger().info("[IdleEventExecutorChecker][run]start, idleCheckInterval = [", this.idleCheckInterval, "]");
            while (this.running) {
                synchronized (children) {
                    if (children.size() > nThreads) {
                        final Iterator<EventExecutor> it = children.iterator();
                        while (it.hasNext() && children.size() > nThreads) {
                            SimpleNettyEventExecutor executor = (SimpleNettyEventExecutor) it.next();
                            if (executor.hasNotBindChannelId()) {
                                executor.shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
                                children.remove(executor);
                            }
                        }
                        if (children.size() == nThreads) {
                            LogHelperFactory.getTraceLogger().info("[IdleEventExecutorChecker][run]Idle EventExecutor check finished, children.size()[", children.size(), "] == nThreads[", nThreads, "]");
                        }
                    }
                }
                synchronized (lock) {
                    try {
                        lock.wait(this.idleCheckInterval);
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                }
            }
        }
    }
}
