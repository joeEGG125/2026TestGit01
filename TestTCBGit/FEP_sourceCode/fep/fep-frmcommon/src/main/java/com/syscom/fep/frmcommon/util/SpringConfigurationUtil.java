package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Lazy
public class SpringConfigurationUtil {
    private final LogHelper logger = new LogHelper();
    private ExecutorService executors;

    @EventListener
    public void envListener(EnvironmentChangeEvent event) {
        logger.info("Configuration has been changed, source is [", event.getSource(), "], key is [", StringUtils.join(event.getKeys(), ","), "]");
    }

    @PreDestroy
    public void preDestory(){
        ThreadPoolFactory.shutdown(executors, this.getClass().getSimpleName());
    }

    public synchronized void refreshManually() {
        ContextRefresher contextRefresher = SpringBeanFactoryUtil.getBean(ContextRefresher.class, false);
        if (contextRefresher != null) {
            if (this.executors == null)
                this.executors = Executors.newSingleThreadExecutor(new SimpleThreadFactory(this.getClass().getSimpleName()));
            this.executors.execute(contextRefresher::refresh);
        }
    }
}
