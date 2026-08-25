package com.syscom.fep.mybatis.util;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.ref.RefInt;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.mapper.SyscomserialMapper;
import com.syscom.fep.mybatis.model.Syscomserial;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * 流水序號取號器的類別
 *
 * @author Richard
 */
public class SerialUtil implements DataSourceConstant {
    private static final JdbcTemplate jdbcTemplate = SpringBeanFactoryUtil.getBean(BEAN_NAME_JDBC_TEMPLATE);
    private static final SyscomserialMapper syscomserialMapper = SpringBeanFactoryUtil.getBean(SyscomserialMapper.class);

    private SerialUtil() {}

    /**
     * 以序號名稱重置序號
     *
     * @param serialName
     */
    public static void resetId(String serialName) {
        Syscomserial syscomserial = new Syscomserial();
        syscomserial.setSerialname(serialName);
        syscomserial.setNextid(0L);
        syscomserialMapper.updateByPrimaryKeySelective(syscomserial);
    }

    /**
     * 改用Sequence不透過SP版
     *
     * @param logData
     * @param serialName
     * @param numberFormat
     * @param resetValue
     * @param interval
     * @return
     */
    private static long getNextId(LogData logData, String serialName, RefString numberFormat, String resetValue, RefInt interval) {
        if (logData == null)
            logData = new LogData();
        final String sql = String.format("SELECT NEXTVAL FOR %s_SEQUENCE FROM SYSIBM.SYSDUMMY1", serialName);
        try {
            Long nextId = jdbcTemplate.queryForObject(sql, Long.class);
            if (nextId != null) {
                Syscomserial syscomserial = syscomserialMapper.selectByPrimaryKey(serialName);
                if (syscomserial != null) {
                    long maxId = syscomserial.getMaxvalue() != null ? syscomserial.getMaxvalue() : 0;
                    String resetType = syscomserial.getResettype();
                    String resetDate = syscomserial.getSerialdate() != null ? FormatUtil.dateTimeFormat(CalendarUtil.clone(syscomserial.getSerialdate()), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) : "";
                    short resetYear = syscomserial.getSerialyear() != null ? syscomserial.getSerialyear() : 0;
                    short resetMonth = syscomserial.getSerialmonth() != null ? syscomserial.getSerialmonth() : 0;
                    String resetField = syscomserial.getResetfield();
                    if ("0".equals(resetType) && nextId >= maxId) {
                        // 最大值重設已由Sequence MaxValue控制, 所以這邊不處理
                    } else if ("1".equals(resetType) && !FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN).equals(resetDate)) {
                        // by換日時重設
                        final String sql3 = String.format("ALTER SEQUENCE %s_SEQUENCE RESTART WITH 1", serialName);
                        final String sql4 = String.format("UPDATE SYSCOMSERIAL SET SERIALDATE = SYSDATE WHERE SERIALNAME = '%s'", serialName);
                        jdbcTemplate.batchUpdate(sql3, sql4);
                        logData.setProgramName("SerialUtil.getNextId");
                        logData.setRemark(String.format("Reset and Get Next Id from %s_SEQUENCE by Date succeed, nextId:%d", serialName, nextId));
                        FEPBase.logMessage(Level.INFO, logData);
                    } else if ("2".equals(resetType) && resetYear != Calendar.getInstance().get(Calendar.YEAR)) {
                        // by換年時重設
                        final String sql3 = String.format("ALTER SEQUENCE %s_SEQUENCE RESTART WITH 1", serialName);
                        final String sql4 = String.format("UPDATE SYSCOMSERIAL SET SERIALYEAR = YEAR(SYSDATE) WHERE SERIALNAME = '%s'", serialName);
                        jdbcTemplate.batchUpdate(sql3, sql4);
                        logData.setProgramName("SerialUtil.getNextId");
                        logData.setRemark(String.format("Reset and Get Next Id from %s_SEQUENCE by Year succeed, nextId:%d", serialName, nextId));
                        FEPBase.logMessage(Level.INFO, logData);
                    } else if ("3".equals(resetType) && resetMonth != Calendar.getInstance().get(Calendar.MONTH) + 1) {
                        // by換月時重設
                        final String sql3 = String.format("ALTER SEQUENCE %s_SEQUENCE RESTART WITH 1", serialName);
                        final String sql4 = String.format("UPDATE SYSCOMSERIAL SET SERIALMONTH = MONTH(SYSDATE) WHERE SERIALNAME = '%s'", serialName);
                        jdbcTemplate.batchUpdate(sql3, sql4);
                        logData.setProgramName("SerialUtil.getNextId");
                        logData.setRemark(String.format("Reset and Get Next Id from %s_SEQUENCE by Month succeed, nextId:%d", serialName, nextId));
                        FEPBase.logMessage(Level.INFO, logData);
                    } else if ("4".equals(resetType) && StringUtils.isNotBlank(resetValue) && !resetValue.equals(resetField)) {
                        // by到指定值時重設
                        final String sql3 = String.format("ALTER SEQUENCE %s_SEQUENCE RESTART WITH 1", serialName);
                        final String sql4 = String.format("UPDATE SYSCOMSERIAL SET RESETFIELD = '%s' WHERE SERIALNAME = '%s'", resetValue, serialName);
                        jdbcTemplate.batchUpdate(sql3, sql4);
                        logData.setProgramName("SerialUtil.getNextId");
                        logData.setRemark(String.format("Reset and Get Next Id from %s_SEQUENCE by Field:%s succeed, nextId:%d", serialName, resetValue, nextId));
                        FEPBase.logMessage(Level.INFO, logData);
                    }
                }
                logData.setProgramName("SerialUtil.getNextId");
                logData.setRemark(String.format("Get Next Id from %s_SEQUENCE succeed, nextId:%d", serialName, nextId));
                FEPBase.logMessage(Level.INFO, logData);
                return nextId;
            } else {
                logData.setProgramName("SerialUtil.getNextId");
                logData.setRemark(String.format("Cannot Get Next Id from %s_SEQUENCE", serialName));
                FEPBase.logMessage(Level.WARN, logData);
            }
        } catch (DataAccessException e) {
            logData.setProgramException(e);
            logData.setProgramName("SerialUtil.getNextId");
            logData.setRemark("GetNextId Error");
            FEPBase.sendEMS(logData);
            throw e;
        }
        return -1L;
    }

    public static long getNextId(LogData logData, String serialName, String resetValue) {
        return getNextId(logData, serialName, new RefString(StringUtils.EMPTY), resetValue, new RefInt(0));
    }

    public static long getNextId(LogData logData, String serialName, String resetValue, RefInt interval) {
        return getNextId(logData, serialName, new RefString(StringUtils.EMPTY), resetValue, interval);
    }

    /**
     * 以序號名稱取得下一序號，回傳一整數
     *
     * @param logData
     * @param serialName
     * @return
     */
    public static long getNextId(LogData logData, String serialName) {
        return getNextId(logData, serialName, new RefString(StringUtils.EMPTY), StringUtils.EMPTY, new RefInt(0));
    }

    public static long getNextId(LogData logData, String serialName, RefInt interval) {
        return getNextId(logData, serialName, new RefString(StringUtils.EMPTY), StringUtils.EMPTY, interval);
    }

    /**
     * 以序號名稱取得下一序號，回傳格式化序號字串
     *
     * @param logData
     * @param serialName
     * @return
     */
    public static String getNextIdWithFormat(LogData logData, String serialName) {
        RefString numberFormat = new RefString(StringUtils.EMPTY);
        int nextId = Math.toIntExact(getNextId(logData, serialName, numberFormat, StringUtils.EMPTY, new RefInt(0)));
        return PolyfillUtil.toString(nextId, numberFormat.get());
    }
}
