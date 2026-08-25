package com.syscom.fep.vo.monitor;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.delegate.ExecuteListener;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public interface MonitorConstant {
    public static final String SERVICE_NAME_SYSTEM = "SYSTEM";
    public static final String SERVICE_NAME_DB = "DB";
    public static final String SERVICE_NAME_MQ = "MQ";
    public static final String SERVICE_NAME_NET_SERVER = "NET-SERVER";
    public static final String SERVICE_NAME_NET_CLIENT = "NET-CLIENT";
    public static final String SERVICE_NAME_PROCESS = "PROCESS";
    public static final String DB_NAME_FEP = "FEPDB";
    public static final String DB_NAME_EMS = "EMSDB";
    public static final String DB_NAME_ENC = "ENCDB";
    public static final String DB_NAME_ENCLOG = "ENCLOGDB";
    public static final String DB_NAME_FEPHIS = "FEPHIS";
    public static final String STATUS_UP = "UP";
    public static final String JSON_FIELD_COMPONENTS = "components";
    public static final String JSON_FIELD_STATUS = "status";
    public static final String JSON_FIELD_HOSTNAME = "hostname";
    public static final String JSON_FIELD_NAME = "name";
    public static final String JSON_FIELD_IP = "ip";
    public static final String JSON_FIELD_USED = "used";
    public static final String JSON_FIELD_TOTAL = "total";
    public static final String JSON_FIELD_DISK = "disk";
    public static final short SYSCONF_VALUE_CMN = 9;
    public static final String SYSCONF_NAME_STOPNOTIFICATION = "StopNotification";
    public static final String SYSCONF_NAME_ENABLEAUTORESTART = "EnableAutoRestart";
    public static final String STATUS_NORMAL = "正常";
    public static final String STATUS_STOPPED = "停止";
    public static final String STATUS_UNKNOWN = "未知";
    public static final String NET_CLIENT_STATE_DISCONNECT = "DisConnect";
    public static final String NET_CLIENT_STATE_CONNECT = "Connect";
    public static final String NET_CLIENT_STATE_UNKNOWN = "Unknown";
    public static final String SUIP_RESP_FIELD_ID = "ID=";
    public static final String SUIP_RESP_FIELD_IPADDR = "IpADDR=";
    public static final String SUIP_RESP_FIELD_PORT = "port=";
    public static final String SUIP_RESP_FIELD_STATUS = "Status=";
    public static final String MONITOR_TYPE_SUIP_NET_CLIENT = "SUIP_NET_CLIENT";
    public static final String MONITOR_TYPE_FISCGW_NET_CLIENT = "FISCGW_NET_CLIENT";
    public static final String MONITOR_TYPE_CBSGW_NET_CLIENT = "CBSGW_NET_CLIENT";
    public static final String MONITOR_TYPE_IMSGW_NET_CLIENT = "IMSGW_NET_CLIENT";
    public static final String STATUS_CODE_STOPPED = "0";
    public static final String STATUS_CODE_NORMAL = "1";
    public static final String STATUS_CODE_UNKNOWN = "2";
    public static final String NOTIFY_SUBJECT = "FEP監控系統通知";
    public static final String REPLY_COMPLETE = "Complete";
    public static final String REPLY_SUCCESS = "Success";
    public static final String REPLY_EXCEPTION_OCCUR = "ExceptionOccur";

    default <T> T timeOccupied(ExecuteListener<T> listener, T defaultReturn, Object... args) {
        LocalDateTime begin = LocalDateTime.now();
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        String methodName = StringUtils.join(ClassUtils.getShortClassName(element.getClassName()), ".", element.getMethodName(), "(", element.getLineNumber(), ")");
        LogHelperFactory.getTraceLogger().debug("[", methodName, "][timeOccupied][", DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(begin), "]begin at ", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(begin));
        try {
            return listener.execute(args);
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().error(e, e.getMessage());
        } finally {
            LocalDateTime end = LocalDateTime.now();
            long duration = ChronoUnit.MILLIS.between(begin, end);
            LogHelperFactory.getTraceLogger().debug("[", methodName, "][timeOccupied][", DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(begin), "]finished at ", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(end), ", duration:", duration, " millisecond");
        }
        return defaultReturn;
    }
}

