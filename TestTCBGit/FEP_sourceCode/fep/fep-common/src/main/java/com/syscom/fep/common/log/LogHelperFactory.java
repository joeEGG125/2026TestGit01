package com.syscom.fep.common.log;

import ch.qos.logback.classic.ClassicConstants;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class LogHelperFactory {
    private static final LogHelper FEPLOGGER = new LogHelper("FEPMessageLogger");
    private static final LogHelper EMSLOGGER = new LogHelper("EMSLogger");
    private static final LogHelper ENCLOGGER = new LogHelper("ENCMessageLogger");
    private static final LogHelper FEPWEBMESSAGELOGGER = new LogHelper("FEPWebMessageLogger");

    private static final LogHelper GENERALLOGGER = new LogHelper("GeneralLogger");
    private static final LogHelper TRACELOGGER = new LogHelper("TraceLogger");
    private static final LogHelper UNITTESTLOGGER = new LogHelper("UnitTestLogger");
    private static final LogHelper WSCLIENTLOGGER = new LogHelper("WsClientLogger");
    private static final LogHelper RESTFULLOGGER = new LogHelper("RestfulClientLogger");
    private static final LogHelper SCHEDULERLOGGER = new LogHelper("SchedulerLogger");
    private static final LogHelper SERVICELOGGER = new LogHelper("ServiceLogger");
    private static final LogHelper GATEWAYLOGGER = new LogHelper("GatewayLogger");
    private static final LogHelper REPOSITORYLOGGER = new LogHelper("RepositoryLogger");
    private static final LogHelper BATCHLOGGER = new LogHelper("BatchLogger");
    private static final LogHelper JMSLOGGER = new LogHelper("JmsLogger");
    private static final LogHelper JDBCLOGGER = new LogHelper("JdbcLogger");

    private LogHelperFactory() {}

    public static LogHelper getFEPLogger() {
        return FEPLOGGER;
    }

    public static LogHelper getEMSLogger() {
        return EMSLOGGER;
    }

    public static LogHelper getGeneralLogger() {
        return GENERALLOGGER;
    }

    public static LogHelper getENCLogger() {
        return ENCLOGGER;
    }

    public static LogHelper getTraceLogger() {
        return TRACELOGGER;
    }

    public static LogHelper getUnitTestLogger() {
        return UNITTESTLOGGER;
    }

    public static LogHelper getWsClientLogger() {
        return WSCLIENTLOGGER;
    }

    public static LogHelper getRestfulClientLogger() {
        return RESTFULLOGGER;
    }

    public static LogHelper getSchedulerLogger() {
        return SCHEDULERLOGGER;
    }

    public static LogHelper getServiceLogger() {
        return SERVICELOGGER;
    }

    public static LogHelper getFEPWebMessageLogger() {
        return FEPWEBMESSAGELOGGER;
    }

    public static LogHelper getGatewayLogger() {
        return GATEWAYLOGGER;
    }

    public static LogHelper getRepositoryLogger() {
        return REPOSITORYLOGGER;
    }

    public static LogHelper getBatchLogger() {
        return BATCHLOGGER;
    }

    public static LogHelper getJmsLogger() {
        return JMSLOGGER;
    }

    public static LogHelper getJdbcLogger() {
        return JDBCLOGGER;
    }

    public static void setProperty(Map<String, String> property) {
        final Map<String, String> defaultMap = new HashMap<>() {{
            put("management.metrics.tags.application", "fep-app");
            put("spring.fep.application.name", "fep-app");
            put("spring.fep.log.path", "logs");
            put(ClassicConstants.CONFIG_FILE_PROPERTY, "logback-spring-ut.xml");
        }};
        for (Map.Entry<String, String> entry : defaultMap.entrySet()) {
            System.setProperty(entry.getKey(), MapUtils.getString(property, entry.getKey(), entry.getValue()));
        }
    }
}
