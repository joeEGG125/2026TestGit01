package com.syscom.fep.frmcommon.thread;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadFactory implements ThreadFactory {
    protected final String ProgramName = this.getClass().getSimpleName();
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;
    private final int priority;

    public SimpleThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }

    public SimpleThreadFactory(String namePrefix, boolean daemon) {
        this(namePrefix, daemon, Thread.NORM_PRIORITY);
    }

    public SimpleThreadFactory(String namePrefix, boolean daemon, int priority) {
        this.namePrefix = StringUtils.join(namePrefix, "-");
        this.daemon = daemon;
        this.priority = priority;
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, ThreadWrapper.wrap(r), StringUtils.join(namePrefix, threadNumber.getAndIncrement()), 0);
        if (t.isDaemon() != daemon) {
            t.setDaemon(daemon);
        }
        if (t.getPriority() != priority) {
            t.setPriority(priority);
        }
        return t;
    }

    public String getNamePrefix() {
        return namePrefix;
    }
}
