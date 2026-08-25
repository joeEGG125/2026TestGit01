package com.syscom.fep.frmcommon.scheduler;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;

import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractScheduledTask {
    protected String taskName;
    private final LogHelper logger = new LogHelper();
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> scheduledFuture = null;

    public AbstractScheduledTask(String taskName) {
        this.taskName = taskName;
        this.executor = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory(taskName));
    }

    /**
     * Execute Task
     */
    public abstract void execute();

    public void schedule(long delay, TimeUnit unit) {
        try {
            this.cancel();
            logger.debug("[", this.taskName, "]ScheduledTask is scheduled, delay = [", delay, "], unit = [", unit, "]");
            scheduledFuture = executor.schedule(this::execute, delay, unit);
        } catch (Throwable t) {
            logger.error(t, "[", this.taskName, "]ScheduledTask schedule failed, delay = [", delay, "], unit = [", unit, "]");
        }
    }

    public void scheduleAtFixedRate(long initialDelay, long period, TimeUnit unit) {
        try {
            this.cancel();
            logger.debug("[", this.taskName, "]ScheduledTask is scheduled at fixed rate, initialDelay = [", initialDelay, "], period = [", period, "], unit = [", unit, "]");
            scheduledFuture = executor.scheduleAtFixedRate(this::execute, initialDelay, period, unit);
        } catch (Throwable t) {
            logger.error(t, "[", this.taskName, "]ScheduledTask scheduleAtFixedRate failed, initialDelay = [", initialDelay, "], period = [", period, "], unit = [", unit, "]");
        }
    }

    public void scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit) {
        try {
            this.cancel();
            logger.debug("[", this.taskName, "]ScheduledTask is scheduled with fixed rate, initialDelay = [", initialDelay, "], delay = [", delay, "], unit = [", unit, "]");
            scheduledFuture = executor.scheduleWithFixedDelay(this::execute, initialDelay, delay, unit);
        } catch (Throwable t) {
            logger.error(t, "[", this.taskName, "]ScheduledTask scheduleWithFixedDelay failed, initialDelay = [", initialDelay, "], delay = [", delay, "], unit = [", unit, "]");
        }
    }

    public void cancel() {
        if (scheduledFuture != null) {
            try {
                scheduledFuture.cancel(false);
            } catch (Exception e) {
                logger.warn(e, e.getMessage());
            } finally {
                scheduledFuture = null;
                logger.debug("[", this.taskName, "]ScheduledTask is cancelled");
            }
        }
    }

    public boolean isCancelled() {
        return scheduledFuture == null;
    }

    @PreDestroy
    public void destroy() {
        logger.trace(this.taskName, " start to destroy...");
        ThreadPoolFactory.shutdown(this.executor, this.taskName);
    }
}
