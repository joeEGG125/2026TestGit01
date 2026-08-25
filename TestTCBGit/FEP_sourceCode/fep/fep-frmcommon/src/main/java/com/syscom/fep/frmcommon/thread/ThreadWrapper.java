package com.syscom.fep.frmcommon.thread;

import com.syscom.fep.frmcommon.annotation.util.StackTracePointCutUtil;
import com.syscom.fep.frmcommon.ref.RefBase;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

public class ThreadWrapper {
    private ThreadWrapper() {}

    public static Runnable wrap(Runnable runnable) {
        return wrap(runnable, true);
    }

    public static Runnable wrap(Runnable runnable, boolean wrapStackTracePoint) {
        // MDC
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        // StackTracePointCut
        final RefBase<List<String>> callerList = new RefBase<>(null);
        if (wrapStackTracePoint)
            callerList.set(StackTracePointCutUtil.getCaller(runnable, 2));
        return () -> {
            try {
                if (contextMap != null)
                    MDC.setContextMap(contextMap);
                if (CollectionUtils.isNotEmpty(callerList.get()))
                    StackTracePointCutUtil.setCaller(callerList.get());
                runnable.run();
            } finally {
                MDC.clear();
                if (wrapStackTracePoint)
                    StackTracePointCutUtil.remove();
            }
        };
    }

    public static <T> Callable<T> wrap(Callable<T> callable) {
        return wrap(callable, true);
    }

    public static <T> Callable<T> wrap(Callable<T> callable, boolean wrapStackTracePoint) {
        // MDC
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        // StackTracePointCut
        final RefBase<List<String>> callerList = new RefBase<>(null);
        if (wrapStackTracePoint)
            callerList.set(StackTracePointCutUtil.getCaller(callable, 2));
        return () -> {
            try {
                if (contextMap != null)
                    MDC.setContextMap(contextMap);
                if (CollectionUtils.isNotEmpty(callerList.get()))
                    StackTracePointCutUtil.setCaller(callerList.get());
                return callable.call();
            } finally {
                MDC.clear();
                if (wrapStackTracePoint)
                    StackTracePointCutUtil.remove();
            }
        };
    }

    public static Executor wrap(Executor executor) {
        return wrap(executor, true);
    }

    public static Executor wrap(Executor executor, boolean wrapStackTracePoint) {
        return command -> executor.execute(wrap(command, wrapStackTracePoint));
    }
}
