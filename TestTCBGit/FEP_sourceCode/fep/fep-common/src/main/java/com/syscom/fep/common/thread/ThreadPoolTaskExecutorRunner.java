package com.syscom.fep.common.thread;

import com.syscom.fep.frmcommon.thread.ThreadWrapper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ThreadPoolTaskExecutorRunner {

    public static void execute(Runnable runnable) {
        execute(ThreadPoolTaskExecutorConstant.BEAN_NAME_FEP_THREAD_POOL, runnable);
    }

    public static void execute(String executorName, Runnable runnable) {
        ThreadPoolTaskExecutor executor = SpringBeanFactoryUtil.getBean(executorName);
        if (executor != null)
            executor.execute(runnable);
        else
            ThreadWrapper.wrap(runnable).run();
    }
}
