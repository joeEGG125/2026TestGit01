package com.syscom.fep.gateway;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil.BeanOperationListener;
import com.syscom.fep.gateway.configuration.GatewayLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.syscom.fep"})
public class SyscomFepGatewayApplication implements ApplicationListener<ApplicationEvent>, BeanOperationListener {
    private static final String CLASS_NAME = SyscomFepGatewayApplication.class.getSimpleName();
    private static LogHelper logger = new LogHelper();
    private static long start;
    @Autowired
    private GatewayLauncher gatewayLauncher;
    private static boolean launchFailed = false;

    static {
        Jasypt.loadEncryptorKey(null);
        start = System.currentTimeMillis();
    }

    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplication(SyscomFepGatewayApplication.class);
            application.setLogStartupInfo(false); // 關閉列印Started SyscomFepGatewayApplication in XXX的日誌
            application.run(args);
        } catch (Exception e) {
            if ("org.springframework.boot.devtools.restart.SilentExitExceptionHandler$SilentExitException".equals(e.getClass().getName())) {
                // ignore
            } else {
                logger.exceptionMsg(e, CLASS_NAME, " start failed!!!");
                System.exit(6);
            }
        }
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        SpringBeanFactoryUtil.addBeanOperationListener(this);
        if (event instanceof AvailabilityChangeEvent) {
            AvailabilityChangeEvent availabilityChangeEvent = (AvailabilityChangeEvent) event;
            if (ReadinessState.ACCEPTING_TRAFFIC == availabilityChangeEvent.getState()) {
                if (!launchFailed)
                    logger.info("Started ", CLASS_NAME, " in ", CalendarUtil.getDiffMilliseconds(System.currentTimeMillis() - start));
            }
        } else if (event instanceof ApplicationStartedEvent) {
            try {
                gatewayLauncher.launch();
            } catch (Throwable t) {
                logger.exceptionMsg(t, CLASS_NAME, " start failed!!!");
                System.exit(6);
            } finally {
                SpringBeanFactoryUtil.removeBeanOperationListener(this);
            }
        } else if (event instanceof ApplicationFailedEvent) {
            logger.exceptionMsg(((ApplicationFailedEvent) event).getException(), CLASS_NAME, " start failed!!!");
            System.exit(6);
        }
    }

    @Override
    public void registerOnException(String beanName, Throwable t) {
        logger.exceptionMsg(t, CLASS_NAME, " start failed!!!");
        launchFailed = true;
        System.exit(6);
    }
}
