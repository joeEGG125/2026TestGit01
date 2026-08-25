package com.syscom.fep.mybatis.deslog.util;

import com.syscom.fep.base.FEPBaseMethod;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.BatchExecutorException;

import java.io.Reader;
import java.sql.BatchUpdateException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.List;

public class DB2DESLOGUtil {

    private DB2DESLOGUtil() {}

    public static String getClobValue(Object value, String... defaultOfNull) throws Exception {
        if (value != null) {
            Reader reader = ReflectUtil.envokeMethod(value, "w", null);
            if (reader != null) {
                List<String> lines = IOUtils.readLines(reader);
                if (CollectionUtils.isNotEmpty(lines)) {
                    return StringUtils.join(lines, "\r\n");
                }
            }
        }
        return ArrayUtils.isNotEmpty(defaultOfNull) ? defaultOfNull[0] : null;
    }

    public static String readClobData(Object value, String... defaultOfNull) throws Exception {
        if (value instanceof Clob) {
            Clob clob = (Clob) value;
            return readClobData(clob, defaultOfNull);
        }
        return defaultOfNull.length > 0 ? defaultOfNull[0] : null;
    }

    public static String readClobData(Clob clob, String... defaultOfNull) throws Exception {
        try (Reader reader = clob.getCharacterStream()) {
            List<String> lines = IOUtils.readLines(reader);
            if (CollectionUtils.isNotEmpty(lines)) {
                return StringUtils.join(lines, "\r\n");
            }
        }
        return ArrayUtils.isNotEmpty(defaultOfNull) ? defaultOfNull[0] : null;
    }

    public static void handleBatchExecutorException(Exception e) {
        handleBatchExecutorException(e, null);
    }

    public static void handleBatchExecutorException(Exception e, LogData logData) {
        BatchExecutorException be = (BatchExecutorException) ExceptionUtil.find(e, t -> t instanceof BatchExecutorException);
        if (be != null) {
            LogHelper logger = LogHelperFactory.getTraceLogger();
            StringBuilder sb = new StringBuilder();
            sb.append("[DB2Util.handleBatchExecutorException]")
                    .append("FailingSqlStatement:").append(be.getFailingSqlStatement()).append(",")
                    .append("FailingStatementId:").append(be.getFailingStatementId());
            if (logData == null) {
                logger.error(be, sb.toString());
            } else {
                logData.setProgramException(be);
                FEPBaseMethod.sendEMS(logData);
            }
            BatchUpdateException bue = be.getBatchUpdateException();
            if (bue != null) {
                SQLException sqlException = bue.getNextException();
                while (sqlException != null) {
                    if (logData == null) {
                        logger.error(sqlException, sb.toString());
                    } else {
                        logData.setProgramException(be);
                        FEPBaseMethod.sendEMS(logData);
                    }
                    sqlException = sqlException.getNextException();
                }
            }
        }
    }
}
