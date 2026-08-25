package com.syscom.fep.base;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.exception.FEPBaseException;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.EnumUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;

public class FEPBaseMethod {
    private static final LogHelper FEPLogger = LogHelperFactory.getFEPLogger();
    private static final LogHelper EMSLogger = LogHelperFactory.getEMSLogger();
    private static final LogHelper GeneralLogger = LogHelperFactory.getGeneralLogger();

    protected final String ProgramName = this.getClass().getSimpleName();

    // 2024-10-25 Richard add
    // 程序初始化時, 先為LogData物件建置XStream物件, 以便後續使用
    static {
        XmlUtil.initXStream(LogData.class);
    }

    protected void logMessage(LogData data) {
        logMessage(Level.INFO, data);
    }

    public static void logMessage(Level level, LogData aaLog) {
        aaLog.setStep(aaLog.getStep() + 1);
        setLogContextField(aaLog);
        switch (level) {
            case ERROR:
                FEPLogger.error(aaLog.getRemark());
                sendEMS(aaLog);
                break;
            case INFO:
                FEPLogger.info(aaLog.getRemark());
                break;
            case DEBUG:
                FEPLogger.debug(aaLog.getRemark());
                break;
            case WARN:
                FEPLogger.warn(aaLog.getRemark());
                break;
            case TRACE:
                FEPLogger.trace(aaLog.getRemark());
                break;
        }
        clearLogContext(aaLog);
    }

    private static void clearLogContext(LogData aaLog) {
        clearMDC();
        aaLog.setRemark(StringUtils.EMPTY);
        aaLog.setMessage(StringUtils.EMPTY);
        aaLog.setProgramFlowType(ProgramFlow.None);
        aaLog.setMessageFlowType(MessageFlow.None);
        aaLog.setNotification(false);
        aaLog.setProgramException(null);
        aaLog.setDoNotSendEMSQueue(false);
    }

    private static void setLogContextField(LogData aaLog) {
        LogMDC.put(Const.MDC_APP, FEPConfig.getInstance().getApplicationName());
        LogMDC.put(Const.MDC_LOGDATE, FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
        LogMDC.put(Const.MDC_SUBSYS, EnumUtil.getEnumName(aaLog.getSubSys()));
        LogMDC.put(Const.MDC_EJ, String.valueOf(aaLog.getEj()));
        LogMDC.put(Const.MDC_CHANNEL, EnumUtil.getEnumName(aaLog.getChannel()));
        LogMDC.put(Const.MDC_MESSAGEFLOW, EnumUtil.getEnumName(aaLog.getMessageFlowType()));
        LogMDC.put(Const.MDC_PROGRAMFLOW, EnumUtil.getEnumName(aaLog.getProgramFlowType()));
        LogMDC.put(Const.MDC_PROGRAMNAME, aaLog.getProgramName());
        LogMDC.put(Const.MDC_MESSAGEID, aaLog.getMessageId());
        LogMDC.put(Const.MDC_STAN, aaLog.getStan());
        LogMDC.put(Const.MDC_ATMSEQ, aaLog.getAtmSeq());
        LogMDC.put(Const.MDC_ATMNO, aaLog.getAtmNo());
        LogMDC.put(Const.MDC_TRINBANK, aaLog.getTrinBank());
        LogMDC.put(Const.MDC_TROUTBANK, aaLog.getTroutBank());
        LogMDC.put(Const.MDC_TRINACTNO, aaLog.getTrinActno());
        LogMDC.put(Const.MDC_TROUTACTNO, aaLog.getTroutActno());
        LogMDC.put(Const.MDC_TXDATE, aaLog.getTxDate());
        LogMDC.put(Const.MDC_TXMESSAGE, aaLog.getMessage());
        LogMDC.put(Const.MDC_STEP, String.valueOf(aaLog.getStep()));
        LogMDC.put(Const.MDC_BKNO, aaLog.getBkno());
        LogMDC.put(Const.MDC_MESSAGEGROUP, aaLog.getMessageGroup());
        LogMDC.put(Const.MDC_TXRQUID, aaLog.getTxRquid());
        LogMDC.put(Const.MDC_HOSTNAME, FEPConfig.getInstance().getHostName());
        LogMDC.put(Const.MDC_HOSTIP, FEPConfig.getInstance().getHostIp());
        LogMDC.put(Const.MDC_DO_NOT_SEND_EMS, Boolean.toString(aaLog.isDoNotSendEMSQueue()));
    }

    protected void infoMessage(Object... msgs) {
        StackTraceElement st = (new Throwable()).getStackTrace()[1];
        LogMDC.put(Const.MDC_CLASSNAME, st.getClassName());
        LogMDC.put(Const.MDC_PROGRAMNAME, st.getMethodName());
        GeneralLogger.info(msgs);
        clearMDC();
    }

    protected void debugMessage(Object... msgs) {
        StackTraceElement st = (new Throwable()).getStackTrace()[1];
        LogMDC.put(Const.MDC_CLASSNAME, st.getClassName());
        LogMDC.put(Const.MDC_PROGRAMNAME, st.getMethodName());
        GeneralLogger.debug(msgs);
        clearMDC();
    }

    protected void warnMessage(Object... msgs) {
        StackTraceElement st = (new Throwable()).getStackTrace()[1];
        LogMDC.put(Const.MDC_CLASSNAME, st.getClassName());
        LogMDC.put(Const.MDC_PROGRAMNAME, st.getMethodName());
        GeneralLogger.warn(msgs);
        clearMDC();
    }

    protected void warnMessage(Throwable t, Object... msgs) {
        StackTraceElement st = (new Throwable()).getStackTrace()[1];
        LogMDC.put(Const.MDC_CLASSNAME, st.getClassName());
        LogMDC.put(Const.MDC_PROGRAMNAME, st.getMethodName());
        GeneralLogger.warn(t, msgs);
        clearMDC();
    }

    protected void errorMessage(Throwable t, Object... msgs) {
        StackTraceElement st = (new Throwable()).getStackTrace()[1];
        LogMDC.put(Const.MDC_CLASSNAME, st.getClassName());
        LogMDC.put(Const.MDC_PROGRAMNAME, st.getMethodName());
        GeneralLogger.error(t, msgs);
        clearMDC();
    }

    /**
     * 這裡設置預設的MDC key
     * 如果有新增的MDC key記得這裡也要增加預設值
     */
    static {
        LogMDC.setDefaultValue(
                new String[] {Const.MDC_LOGENABLE, Const.MDC_PROFILE, Const.MDC_BATCHJOB, Const.MDC_BATCHJOB_FILENAME, Const.MDC_GATEWAY},
                new String[] {Boolean.TRUE.toString(), "fep", "batchjob", "batchjob", "fep"});
    }

    /**
     * 注意, 這裡clear一定要小心, 以下幾個MDC_決定了log檔的檔名, 所以clear之前一定要先取出來, clear之後再塞回去, 否則會導致log記錄的檔名亂掉
     * <p>
     * 日後如果有新的, 一定要增加在{@link Const#MDC_KEPT}
     */
    public static void clearMDC() {
        LogMDC.clear(Const.MDC_KEPT);
    }

    public static Map<String, String> getMDCKeptValue() {
        return LogMDC.getAll(Const.MDC_KEPT);
    }

    public static void sendEMS(LogData aaLog, boolean... notPrintExceptionStack) {
        EMSLogger.accumulateStackFrameLevel();
        sendEMS(Level.ERROR, aaLog, notPrintExceptionStack);
    }

    public static void sendEMS(Level level, LogData aaLog, boolean... notPrintExceptionStack) {
        EMSLogger.accumulateStackFrameLevel();
        setLogContextField(aaLog);
        sendToEMS(level, aaLog, notPrintExceptionStack);
        clearLogContext(aaLog);
    }

    private static void sendToEMS(Level level, LogData aaLog, boolean... notPrintExceptionStack) {
        EMSLogger.accumulateStackFrameLevel();
        try {
            String exMessage = StringUtils.EMPTY;
            String exStack = StringUtils.EMPTY;
            String exCode = StringUtils.EMPTY;
            String exSource = StringUtils.EMPTY;
            StringBuilder sb = new StringBuilder();
            if (aaLog != null) {
                String logXml = serializeToXml(aaLog);
                sb.append(logXml);
                if (aaLog.getProgramException() != null) {
                    // 為了配合EMS Alert日誌記錄, 這裡加上自定義的訊息
                    String encExLog = "";
                    if (FEPConfig.getInstance().getCheckExLog()) {
                        encExLog = Const.MESSAGE_ERR_EXCEPTION_OCCUR + ",";
                    }
                    exMessage = StringUtils.join(
                            StringUtils.isNotBlank(aaLog.getProgramException().getMessage()) ?
                                    StringUtils.join(encExLog, aaLog.getProgramException().getMessage()) : StringUtils.EMPTY);
                    if (ArrayUtils.isEmpty(notPrintExceptionStack) || !notPrintExceptionStack[0]) {
                        GeneralLogger.exceptionMsg(aaLog.getProgramException(), exMessage);
                    }
                    exCode = aaLog.getProgramException().getClass().getSimpleName();
                    // 如果是FEPBaseException, 並且FEPReturnCode不是null, 則改變exCode
                    if (aaLog.getProgramException() instanceof FEPBaseException) {
                        FEPReturnCode rtnCode = ((FEPBaseException) aaLog.getProgramException()).getRtnCode();
                        if (rtnCode != null) {
                            exCode = rtnCode.name();
                        }
                    }
                    exSource = aaLog.getProgramException().getCause() == null ? StringUtils.EMPTY : aaLog.getProgramException().getCause().getClass().getSimpleName();
                    String customMsg = StringUtils.EMPTY;
                    // BadSqlGrammarException
                    // 2023-07-24 Richard add因為BadSqlGrammarException的message太長, 並且帶有sql語法, 所以這裡需要重新塞一次exMessage
                    if (aaLog.getProgramException() instanceof BadSqlGrammarException) {
                        BadSqlGrammarException badSqlGrammarException = (BadSqlGrammarException) aaLog.getProgramException();
                        SQLException sqlEx = badSqlGrammarException.getSQLException();
                        // 為了配合EMS Alert日誌記錄, 這裡加上自定義的訊息
                        exMessage = StringUtils.join(
                                "BadSqlGrammarException occur",
                                StringUtils.isNotBlank(sqlEx.getMessage()) ?
                                        StringUtils.join(", ", sqlEx.getMessage()) : StringUtils.EMPTY);
                        sb.append("<ExSubCode>").append(sqlEx.getErrorCode()).append("</ExSubCode>");
                    }
                    // DataAccessException
                    else if (aaLog.getProgramException() instanceof DataAccessException) {
                        DataAccessException dae = (DataAccessException) aaLog.getProgramException();
                        Throwable cause = dae.getCause();
                        if (cause instanceof SQLException) {
                            SQLException sqlEx = (SQLException) cause;
                            customMsg = StringUtils.join(",CustomMessage:", sqlEx.getMessage());
                            sb.append("<ExSubCode>").append(sqlEx.getErrorCode()).append("</ExSubCode>");
                        }
                    }
                    // SQLException
                    else if (aaLog.getProgramException() instanceof SQLException) {
                        SQLException sqlEx = (SQLException) aaLog.getProgramException();
                        sb.append("<ExSubCode>").append(sqlEx.getErrorCode()).append("</ExSubCode>");
                    } else {
                        sb.append("<ExSubCode/>");
                    }
                    exStack = ExceptionUtil.getStackTrace(aaLog.getProgramException());
                    sb.append("<TxErrDesc>").append(exMessage).append(customMsg).append("</TxErrDesc>");
                    sb.append("<ExSource>").append(exSource).append("</ExSource>");
                    sb.append("<ExStack><![CDATA[").append(exStack).append("]]></ExStack>");
                    if (StringUtils.isBlank(aaLog.getExternalCode())) {
                        sb.append("<TxExternalCode>ProgramException</TxExternalCode>");
                    } else {
                        sb.append("<TxExternalCode>").append(aaLog.getExternalCode()).append("</TxExternalCode>");
                    }
                } else {
                    exCode = aaLog.getReturnCode() != null ? aaLog.getReturnCode().name() : StringUtils.EMPTY;
                    exMessage = aaLog.getRemark();
                    sb.append("<TxErrDesc>").append(exMessage).append("</TxErrDesc>");
                    sb.append("<ExSource/>");
                    sb.append("<ExStack/>");
                    if (StringUtils.isBlank(aaLog.getExternalCode())) {
                        sb.append("<TxExternalCode>AbNormal</TxExternalCode>");
                    } else {
                        sb.append("<TxExternalCode>").append(aaLog.getExternalCode()).append("</TxExternalCode>");
                    }
                    sb.append("<ExSubCode/>");
                }
            } else {
                sb.append("<LogData/>");
            }
            sb.append("<TxPK/>");
            sb.append("<TxSource>").append(LogMDC.get(Const.MDC_PROFILE, StringUtils.EMPTY)).append("</TxSource>");
            StackTraceElement[] elements = (new Throwable()).getStackTrace();
            int stackFrameLevel = EMSLogger.getStackFrameLevel();
            stackFrameLevel = stackFrameLevel < elements.length ? stackFrameLevel : elements.length - 1;
            StackTraceElement st = (new Throwable()).getStackTrace()[stackFrameLevel];
            LogMDC.put(Const.MDC_PGFILE, st.getFileName());
            LogMDC.put(Const.MDC_LINENUMBER, String.valueOf(st.getLineNumber()));
            LogMDC.put(Const.MDC_APP, FEPConfig.getInstance().getApplicationName());
            LogMDC.put(Const.MDC_ERRCODE, exCode);
            LogMDC.put(Const.MDC_ERRDESCRIPTION, exMessage);
            // LogMDC.put(Const.MDC_HOSTNAME, InetAddress.getLocalHost().getHostName());
            // LogMDC.put(Const.MDC_HOSTIP, InetAddress.getLocalHost().getHostAddress());
            LogMDC.put(Const.MDC_HOSTNAME, FEPConfig.getInstance().getHostName());
            LogMDC.put(Const.MDC_HOSTIP, FEPConfig.getInstance().getHostIp());
            LogMDC.put(Const.MDC_LEVEL, level.name());
            LogMDC.put(Const.MDC_SYS, FEPConfig.getInstance().getSystemId());
            LogMDC.put(Const.MDC_SUBSYS, aaLog != null && aaLog.getSubSys() != null ? aaLog.getSubSys().name() : StringUtils.EMPTY);
            LogMDC.put(Const.MDC_TXRQUID, aaLog.getTxRquid());
            switch (level) {
                case ERROR:
                    EMSLogger.error(sb.toString());
                    break;
                case INFO:
                    EMSLogger.info(sb.toString());
                    break;
                case DEBUG:
                    EMSLogger.debug(sb.toString());
                    break;
                case WARN:
                    EMSLogger.warn(sb.toString());
                    break;
                case TRACE:
                    EMSLogger.trace(sb.toString());
                    break;
            }
        } catch (Exception e) {
            FEPLogger.exceptionMsg(e, "SendEMS Fail!EX:", e.getMessage());
        } finally {
            if (aaLog != null) {
                aaLog.setResponseMessage(StringUtils.EMPTY);
                aaLog.setResponsible(StringUtils.EMPTY);
            }
        }
    }

    protected static String serializeToXml(Object object) {
        return XmlUtil.toXML(object);
    }

    protected static <T> T deserializeFromXml(String xml, Class<T> clazz) {
        return XmlUtil.fromXML(xml, clazz);
    }

    public static void logMessage(Level level, Throwable t, Object... messages) {
        StackTraceElement st = (new Throwable()).getStackTrace()[1];
        LogMDC.put(Const.MDC_CLASSNAME, st.getClassName());
        LogMDC.put(Const.MDC_PROGRAMNAME, st.getMethodName());
        switch (level) {
            case ERROR:
                GeneralLogger.error(t, messages);
                break;
            case INFO:
                GeneralLogger.info(messages);
                break;
            case DEBUG:
                GeneralLogger.debug(messages);
                break;
            case WARN:
                GeneralLogger.warn(messages);
                break;
            case TRACE:
                GeneralLogger.trace(messages);
                break;
        }
        clearMDC();
    }
}
