package com.syscom.fep.frmcommon.log.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.ReconfigureOnChangeTask;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.scheduler.AbstractScheduledTask;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@ConfigurationProperties(prefix = "spring.fep.log.context.listener.change")
public class LoggerContextChangeListener extends ContextAwareBase implements LoggerContextListener, LifeCycle, ApplicationListener<ApplicationEvent> {
    private final LogHelper logger = new LogHelper();
    private final String SPLITTER = StringUtils.repeat('=', 20);
    private ConfigurableEnvironment environment;
    private AbstractScheduledTask propertiesChangeDetectorTask;
    private boolean isStarted = false;
    /**
     * 用來保存初始載入的自定義的變量值
     */
    private final Map<String, String> property = new HashMap<>();
    /**
     * 偵測配置檔更新時間間隔, 單位秒
     */
    private long detect = 60;
    /**
     * 開始偵測配置檔延遲時間, 單位秒
     */
    private long delay = 60;

    @PostConstruct
    public void initialization() {
        logger.debug(SPLITTER, "initialization", SPLITTER);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.addListener(this);
        this.initProperty(loggerContext);
    }

    @PreDestroy
    public void destory() {
        logger.debug(SPLITTER, "destory", SPLITTER);
        if (propertiesChangeDetectorTask != null)
            propertiesChangeDetectorTask.destroy();
    }

    @Override
    public boolean isResetResistant() {
        logger.debug(SPLITTER, "isResetResistant", SPLITTER);
        return true; // 注意, 必須是true, 否則在配置檔異動重新載入配置檔時, logContext在reset時, 會將這個listener移除掉
    }

    @Override
    public void onStart(LoggerContext loggerContext) {
        logger.debug(SPLITTER, "onStart", SPLITTER);
    }

    @Override
    public void onReset(LoggerContext loggerContext) {
        logger.debug(SPLITTER, "onReset", SPLITTER);
        // 當配置檔有異動時, 將初始的自定義變量值重新塞入, 確保log檔名+path不會跑掉
        this.putProperty(loggerContext);
    }

    @Override
    public void onStop(LoggerContext loggerContext) {
        logger.debug(SPLITTER, "onStop", SPLITTER);
    }

    @Override
    public void onLevelChange(Logger logger, Level level) {
        this.logger.debug(SPLITTER, "onLevelChange-", logger.getName(), SPLITTER);
    }

    @Override
    public void start() {
        logger.debug(SPLITTER, "start", SPLITTER);
        this.isStarted = true;
    }

    @Override
    public void stop() {
        logger.debug(SPLITTER, "stop", SPLITTER);
        this.isStarted = false;
    }

    @Override
    public boolean isStarted() {
        logger.debug(SPLITTER, "isStarted", SPLITTER);
        return this.isStarted;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (this.context == null)
            this.context = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (event instanceof ApplicationPreparedEvent) {
            this.environment = ((ApplicationPreparedEvent) event).getApplicationContext().getEnvironment();
            initPropertiesChangeDetectorTask();
        }
    }

    private void initProperty(LoggerContext loggerContext) {
        synchronized (property) {
            if (CollectionUtils.isEmpty(property)) {
                Map<String, String> contextCopyOfPropertyMap = loggerContext.getCopyOfPropertyMap();
                if (MapUtils.isNotEmpty(contextCopyOfPropertyMap)) {
                    property.putAll(contextCopyOfPropertyMap);
                }
            }
        }
    }

    private void initPropertiesChangeDetectorTask() {
        // 如果配置檔中沒有定義springProperty, 則建立task
        synchronized (property) {
            if (CollectionUtils.isEmpty(property))
                return;
        }
        propertiesChangeDetectorTask = new AbstractScheduledTask("LoggerContextChangeTask") {
            private final Map<String, Long> activeProfileToLastModifiedMap = new HashMap<>();

            @Override
            public void execute() {
                propertiesChangeDetect(activeProfileToLastModifiedMap);
            }
        };
        propertiesChangeDetectorTask.scheduleAtFixedRate(delay, detect, TimeUnit.SECONDS);
        logger.debug("propertiesChangeDetectorTask scheduleAtFixedRate, delay:", delay, ",period:", detect);
    }

    /**
     * 偵測配置檔異動
     */
    private void propertiesChangeDetect(final Map<String, Long> activeProfileToLastModifiedMap) {
        Properties properties = new Properties();
        String[] activeProfiles = this.environment.getActiveProfiles();
        if (ArrayUtils.isNotEmpty(activeProfiles)) {
            String profileName;
            boolean activeProfileChanged = false;
            // 先診測一下有異動的配置檔, 則重新抓取屬性值
            for (String activeProfile : activeProfiles) {
                profileName = StringUtils.join("application-", activeProfile, ".properties");
                try {
                    Resource resource = new PathMatchingResourcePatternResolver().getResource(StringUtils.join("classpath:", profileName));
                    if (resource.exists()) {
                        // 取出該配置檔上一次的最後異動日期時間
                        Long latestLastModified = activeProfileToLastModifiedMap.get(activeProfile);
                        // 取出該配置檔當前最後異動日期時間
                        long lastModified = resource.lastModified();
                        if (latestLastModified == null) {
                            activeProfileToLastModifiedMap.put(activeProfile, lastModified);
                        } else {
                            // 說明配置檔有異動, 則需要重新抓指定的屬性值
                            if (lastModified > latestLastModified) {
                                logger.debug(profileName, " changed");
                                activeProfileToLastModifiedMap.put(activeProfile, lastModified);
                                activeProfileChanged = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn(e, "load configuration file:", profileName, " with exception occur, ", e.getMessage());
                }
            }
            // 如果有異動的配置檔, 則重新抓取所有的配置檔的值, 以確保始終有吃到overwrite的屬性值
            if (activeProfileChanged) {
                for (String activeProfile : activeProfiles) {
                    profileName = StringUtils.join("application-", activeProfile, ".properties");
                    try {
                        Resource resource = new PathMatchingResourcePatternResolver().getResource(StringUtils.join("classpath:", profileName));
                        if (resource.exists()) {
                            logger.debug(profileName, " has been reloaded.");
                            Properties prop = new Properties();
                            prop.load(resource.getInputStream());
                            properties.putAll(prop);
                        }
                    } catch (Exception e) {
                        logger.warn(e, "load configuration file:", profileName, " with exception occur, ", e.getMessage());
                    }
                }
            }
        }
        if (MapUtils.isNotEmpty(properties)) {
            synchronized (property) {
                for (Map.Entry<String, String> entry : property.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    String newValue = EnvPropertiesUtil.replaceValue(properties, properties.getProperty(mappingKey(key)));
                    if (newValue != null && !newValue.equals(value)) {
                        logger.debug("the value of ", key, " changed from ", value, " to ", newValue);
                        property.put(key, newValue);
                    }
                }
            }
            this.performXMLConfiguration();
        }
    }

    private String mappingKey(String key) {
        if ("spring.fep.application.name".equals(key)) {
            return "management.metrics.tags.application";
        }
        return key;
    }

    private void performXMLConfiguration() {
        logger.debug("start to performXMLConfiguration");
        try {
            ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(this.context);
            if (configurationWatchList == null) {
                this.addWarn("Empty ConfigurationWatchList in context");
                logger.warn("Empty ConfigurationWatchList in context");
            } else {
                URL mainConfigurationURL = configurationWatchList.getMainURL();
                this.addInfo("Detected change in configuration files.");
                this.addInfo("Will reset and reconfigure context named [" + this.context.getName() + "]");
                logger.debug("Detected change in configuration files.");
                logger.debug("Will reset and reconfigure context named [", this.context.getName(), "]");
                LoggerContext lc = (LoggerContext) this.context;
                if (mainConfigurationURL.toString().endsWith("xml")) {
                    ReconfigureOnChangeTask reconfigureOnChangeTask = (ReconfigureOnChangeTask) context.getObject("RECONFIGURE_ON_CHANGE_TASK");
                    ReflectUtil.envokeMethod(reconfigureOnChangeTask, "performXMLConfiguration", new Class[] {LoggerContext.class, URL.class}, new Object[] {lc, mainConfigurationURL});
                }
                logger.debug("performXMLConfiguration succeed");
            }
        } catch (Exception e) {
            logger.warn(e, "performXMLConfiguration with exception occur, ", e.getMessage());
        } finally {
            logger.debug("finished to performXMLConfiguration");
        }
    }

    private void putProperty(LoggerContext loggerContext) {
        synchronized (property) {
            if (MapUtils.isNotEmpty(property)) {
                logger.debug("putProperty to loggerContext, property:", property);
                property.forEach(loggerContext::putProperty);
            }
        }
    }

    public long getDetect() {
        return detect;
    }

    public void setDetect(long detect) {
        this.detect = detect;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }
}