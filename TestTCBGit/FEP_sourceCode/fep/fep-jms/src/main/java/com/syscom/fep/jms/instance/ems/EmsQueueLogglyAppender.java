package com.syscom.fep.jms.instance.ems;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.ext.loggly.LogglyAppender;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.layout.ParameterJsonLayout;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.jms.instance.ems.sender.EmsQueueSender;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.JmsException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EmsQueueLogglyAppender<E> extends LogglyAppender<E> {
    private static final String PROGRAM_NAME = EmsQueueLogglyAppender.class.getSimpleName();
    private final LogHelper logger = LogHelperFactory.getTraceLogger();
    private boolean async = true;
    private int corePoolSize = 1;
    private int maximumPoolSize = 10;
    private long keepAliveTime = 0;
    private int queueCapacity = Integer.MAX_VALUE;
    private ExecutorService executor;
    private String excludeAppNames;
    private List<String> excludeAppNameList;

    @Override
    public void start() {
        this.endpointUrl = StringUtils.EMPTY;
        if (async) {
            this.executor = ThreadPoolFactory.newThreadPool(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, queueCapacity, new SimpleThreadFactory(PROGRAM_NAME), new ThreadPoolExecutor.CallerRunsPolicy());
        }
        super.start();
        // 以下兩行必須加入, 否則後面的stop方法不會被呼叫
        getContext().register(this);
        getContext().putObject(this.getClass().getName(), this);
    }

    @Override
    public void stop() {
        boolean isStarted = this.isStarted();
        logger.debug(PROGRAM_NAME, " Stop, isStarted:", isStarted);
        if (!isStarted)
            return;
        super.stop();
        ThreadPoolFactory.shutdown(executor, PROGRAM_NAME);
        EmsQueueSender.destroy();
    }

    @Override
    protected void append(E eventObject) {
        if (eventObject instanceof LoggingEvent) {
            Map<String, String> mdc = ((LoggingEvent) eventObject).getMDCPropertyMap();
            if (MapUtils.isNotEmpty(mdc)) {
                // 從MDC中取出app, 如果在excludeAppNameList中, 則不要送Queue
                if (CollectionUtils.isNotEmpty(this.excludeAppNameList)) {
                    String appName = mdc.get(Const.MDC_APP);
                    if (StringUtils.isNotBlank(appName) && this.excludeAppNameList.stream().anyMatch(appName::equalsIgnoreCase)) {
                        return;
                    }
                }
                // 從MDC中取出doNotSendEMS, 如果是true, 則不要送Queue
                if (Boolean.parseBoolean(mdc.get(Const.MDC_DO_NOT_SEND_EMS)))
                    return;
            }
        }
        if (this.executor != null) {
            this.executor.execute(() -> {
                String event = this.layout.doLayout(eventObject);
                postToLoggly(event);
            });
        } else {
            String event = this.layout.doLayout(eventObject);
            postToLoggly(event);
        }
    }

    private void postToLoggly(final String event) {
        EMSLogMessageType messageType = null;
        ParameterJsonLayout jsonLayout = (ParameterJsonLayout) this.layout;
        // 若送的訊息是丟alert失敗, 則要試著重送另一台
        if (jsonLayout.containsParameter(ParameterJsonLayout.MESSAGE_TYPE_LOG)) {
            messageType = EMSLogMessageType.log;
        } else if (jsonLayout.containsParameter(ParameterJsonLayout.MESSAGE_TYPE_ALERT)) {
            messageType = EMSLogMessageType.alert;
        }
        try {
            EmsQueueSender.sendEMS(event, messageType);
        } catch (JmsException e) {
            logger.error(e, PROGRAM_NAME, ".postToLoggly with exception occur, ", e.getMessage());
            addError(PROGRAM_NAME + " server-side exception: " + e.getErrorCode() + ": " + e.getMessage());
        } catch (Throwable t) {
            logger.error(t, PROGRAM_NAME, ".postToLoggly with exception occur, ", t.getMessage());
            addError(PROGRAM_NAME + " client-side exception", t);
        }
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String getExcludeAppNames() {
        return excludeAppNames;
    }

    public void setExcludeAppNames(String excludeAppNames) {
        this.excludeAppNames = excludeAppNames;
        this.excludeAppNameList = StringUtil.split(this.excludeAppNames, ',', ';');
        logger.debug("[", PROGRAM_NAME, "][setExcludeAppNames]excludeAppNameList:", excludeAppNameList);
    }
}
