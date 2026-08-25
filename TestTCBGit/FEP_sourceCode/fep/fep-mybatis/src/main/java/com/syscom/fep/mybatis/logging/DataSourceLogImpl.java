package com.syscom.fep.mybatis.logging;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.ibatis.logging.Log;

public class DataSourceLogImpl implements Log {
    private final LogHelper logger = LogHelperFactory.getRepositoryLogger();
    private final String clazz;

    public DataSourceLogImpl(String clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void error(String s, Throwable e) {
        logger.error(e, "[", clazz, "]", s);
    }

    @Override
    public void error(String s) {
        logger.error("[", clazz, "]", s);
    }

    @Override
    public void debug(String s) {
        logger.debug("[", clazz, "]", s);
    }

    @Override
    public void trace(String s) {
        logger.trace("[", clazz, "]", s);
    }

    @Override
    public void warn(String s) {
        logger.warn("[", clazz, "]", s);
    }
}
