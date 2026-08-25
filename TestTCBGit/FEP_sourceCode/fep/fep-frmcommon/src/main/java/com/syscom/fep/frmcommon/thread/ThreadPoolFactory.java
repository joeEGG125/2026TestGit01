package com.syscom.fep.frmcommon.thread;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

public class ThreadPoolFactory {
    private static final LogHelper logger = new LogHelper();

    private ThreadPoolFactory() {}

    public static ThreadPoolTaskExecutor createTaskExecutor(ThreadPoolConfiguration configuration, RejectedExecutionHandler rejectedExecutionHandler) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() {
            @Override
            public Thread newThread(Runnable runnable) {
                return createThread(ThreadWrapper.wrap(runnable));
            }
        };
        //設置核心線程數
        executor.setCorePoolSize(configuration.getCorePoolSize());
        // 設置最大線程數
        executor.setMaxPoolSize(configuration.getMaxPoolSize());
        // 設置隊列容量
        executor.setQueueCapacity(configuration.getQueueCapacity());
        // 設置允許的空閑時間（秒）
        executor.setKeepAliveSeconds(configuration.getKeepAlive());
        // 設置預設線程名稱
        executor.setThreadNamePrefix(configuration.getThreadNamePrefix());
        // 設置拒絕策略rejection-policy：當pool已經達到max size的時候，如何處理新任務
        executor.setRejectedExecutionHandler(rejectedExecutionHandler);
        // 等待所有任務結束後在關閉線程池
        executor.setWaitForTasksToCompleteOnShutdown(configuration.isWaitForJobsToCompleteOnShutdown());
        // 拒絕策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
        return executor;
    }

    public static ExecutorService newFixedThreadPool(int corePoolSize, String threadNamePrefix) {
        return newFixedThreadPool(corePoolSize, Integer.MAX_VALUE, new SimpleThreadFactory(threadNamePrefix), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ExecutorService newFixedThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        return newFixedThreadPool(corePoolSize, Integer.MAX_VALUE, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ExecutorService newFixedThreadPool(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return newFixedThreadPool(corePoolSize, Integer.MAX_VALUE, threadFactory, handler);
    }

    public static ExecutorService newFixedThreadPool(int corePoolSize, int queueCapacity, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return newFixedThreadPool(corePoolSize, 0, TimeUnit.MILLISECONDS, queueCapacity, threadFactory, handler);
    }

    public static ExecutorService newFixedThreadPool(int corePoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return newFixedThreadPool(corePoolSize, keepAliveTime, unit, Integer.MAX_VALUE, threadFactory, handler);
    }

    public static ExecutorService newFixedThreadPool(int corePoolSize, long keepAliveTime, TimeUnit unit, int queueCapacity, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return newThreadPool(corePoolSize, corePoolSize, keepAliveTime, unit, queueCapacity, threadFactory, handler);
    }

    public static ExecutorService newThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueCapacity, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(queueCapacity), threadFactory, handler);
    }

    public static void shutdown(ExecutorService executor, Object... logPrefixes) {
        if (executor != null) {
            String logPrefix = null;
            if (ArrayUtils.isEmpty(logPrefixes)) {
                if (executor instanceof ThreadPoolExecutor) {
                    ThreadFactory threadFactory = ((ThreadPoolExecutor) executor).getThreadFactory();
                    if (threadFactory instanceof SimpleThreadFactory) {
                        logPrefix = ((SimpleThreadFactory) threadFactory).getNamePrefix();
                    }
                }
            } else {
                logPrefix = StringUtils.join(logPrefixes);
            }
            if (StringUtils.isBlank(logPrefix)) {
                logPrefix = StringUtils.join("[", executor.getClass().getSimpleName(), "]");
            }
            try {
                executor.shutdown();
                if (executor.awaitTermination(60, TimeUnit.SECONDS))
                    logger.info(logPrefix, " executor terminate all runnable successful");
                else
                    logger.warn(logPrefix, " executor terminate all runnable timeout occur");
            } catch (Throwable e) {
                logger.warn(e, e.getMessage());
            }
        }
    }
}
