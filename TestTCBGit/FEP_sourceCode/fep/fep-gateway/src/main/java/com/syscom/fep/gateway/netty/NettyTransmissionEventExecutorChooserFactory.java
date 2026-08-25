package com.syscom.fep.gateway.netty;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorChooserFactory.EventExecutorChooser;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NettyTransmissionEventExecutorChooserFactory {

    public static final NettyTransmissionEventExecutorChooserFactory INSTANCE = new NettyTransmissionEventExecutorChooserFactory();

    private NettyTransmissionEventExecutorChooserFactory() {}

    public EventExecutorChooser newChooser(final List<EventExecutor> executors) {
        if (isPowerOfTwo(executors.size())) {
            return new PowerOfTwoEventExecutorChooser(executors);
        } else {
            return new GenericEventExecutorChooser(executors);
        }
    }

    private static boolean isPowerOfTwo(final int val) {
        return (val & -val) == val;
    }

    private static final class PowerOfTwoEventExecutorChooser implements EventExecutorChooser {
        private final AtomicInteger idx = new AtomicInteger();
        private final List<EventExecutor> executors;

        PowerOfTwoEventExecutorChooser(final List<EventExecutor> executors) {
            this.executors = executors;
        }

        @Override
        public EventExecutor next() {
            synchronized (executors) {
                return executors.get(idx.getAndIncrement() & executors.size() - 1);
            }
        }
    }

    private static final class GenericEventExecutorChooser implements EventExecutorChooser {
        private final AtomicLong idx = new AtomicLong();
        private final List<EventExecutor> executors;

        GenericEventExecutorChooser(final List<EventExecutor> executors) {
            this.executors = executors;
        }

        @Override
        public EventExecutor next() {
            synchronized (executors) {
                return executors.get((int) Math.abs(idx.getAndIncrement() % executors.size()));
            }
        }
    }
}
