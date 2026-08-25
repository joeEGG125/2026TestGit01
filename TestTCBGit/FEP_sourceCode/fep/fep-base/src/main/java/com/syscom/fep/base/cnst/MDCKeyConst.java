package com.syscom.fep.base.cnst;

public interface MDCKeyConst {
    /**
     * 以下這幾個屬於Kernel的MDC Key, 也就是在clear之前要先將值取出來, 然後clear之後再put回去
     * 如果有新增的key記得要修改{@link MDCKeyConst#MDC_KEPT}
     */
    public static final String MDC_LOGENABLE = "logEnable";
    public static final String MDC_PROFILE = "profile";
    public static final String MDC_GATEWAY = "gateway";
    public static final String MDC_GATEWAY_SOCKET_TYPE = "socketType";
    public static final String MDC_BATCHJOB = "batchjob";
    public static final String MDC_BATCHJOB_FILENAME = "batchjob-filename";
    public static final String[] MDC_KEPT = new String[] {
            MDC_LOGENABLE, MDC_PROFILE, MDC_GATEWAY, MDC_GATEWAY_SOCKET_TYPE, MDC_BATCHJOB, MDC_BATCHJOB_FILENAME
    };
    /**
     * 以上這幾個屬於Kernel的MDC Key, 也就是在clear之前要先將值取出來, 然後clear之後再put回去
     * 如果有新增的key記得要修改{@link MDCKeyConst#MDC_KEPT}
     */

    public static final String MDC_LOGDATE = "LogDate";
    public static final String MDC_SUBSYS = "Subsys";
    public static final String MDC_EJ = "Ej";
    public static final String MDC_CHANNEL = "Channel";
    public static final String MDC_MESSAGEFLOW = "MessageFlow";
    public static final String MDC_PROGRAMFLOW = "ProgramFlow";
    public static final String MDC_PROGRAMNAME = "ProgramName";
    public static final String MDC_MESSAGEID = "MessageID";
    public static final String MDC_STAN = "Stan";
    public static final String MDC_ATMSEQ = "ATMSeq";
    public static final String MDC_ATMNO = "ATMNo";
    public static final String MDC_TRINBANK = "TrinBank";
    public static final String MDC_TROUTBANK = "TroutBank";
    public static final String MDC_TRINACTNO = "TrinActno";
    public static final String MDC_TROUTACTNO = "TroutActno";
    public static final String MDC_TXDATE = "TxDate";
    public static final String MDC_TXMESSAGE = "TxMessage";
    public static final String MDC_STEP = "Step";
    public static final String MDC_BKNO = "Bkno";
    public static final String MDC_MESSAGEGROUP = "MessageGroup";
    public static final String MDC_CLASSNAME = "ClassName";
    public static final String MDC_PGFILE = "pgFile";
    public static final String MDC_LINENUMBER = "linenumber";
    public static final String MDC_APP = "app";
    public static final String MDC_ERRCODE = "errcode";
    public static final String MDC_ERRDESCRIPTION = "errdescription";
    public static final String MDC_HOSTNAME = "hostname";
    public static final String MDC_HOSTIP = "hostip";
    public static final String MDC_LEVEL = "level";
    public static final String MDC_SYS = "Sys";
    public static final String MDC_TXRQUID = "TxRquid";
    public static final String MDC_FUNCNO = "FuncNo";
    public static final String MDC_KEYID = "KeyId";
    public static final String MDC_INPUT1 = "Input1";
    public static final String MDC_INPUT2 = "Input2";
    public static final String MDC_OUTPUT1 = "Output1";
    public static final String MDC_OUTPUT2 = "Output2";
    public static final String MDC_SUIPCOMMAND = "SuipCommand";
    public static final String MDC_RC = "RC";
    public static final String MDC_CALLOBJ = "CallObj";
    public static final String MDC_WEB_LOGINID = "loginId";
    public static final String MDC_WEB_USERID = "userId";
    public static final String MDC_WEB_REMOTE_IP = "remoteIp";
    public static final String MDC_WEB_MENU_NAME = "menuName";
    public static final String MDC_WEB_MENU_VIEW = "menuView";
    public static final String MDC_WEB_MENU_URL = "menuUrl";
    public static final String MDC_GATEWAY_REMOTE_IP = "remoteIp";
    public static final String MDC_GATEWAY_REMOTE_PORT = "remotePort";
    public static final String MDC_GATEWAY_REMOTE_HOSTNAME = "remoteHostName";
    public static final String MDC_GATEWAY_LOCAL_IP = "localIp";
    public static final String MDC_GATEWAY_LOCAL_PORT = "localPort";
    public static final String MDC_GATEWAY_LOCAL_HOSTNAME = "localHostName";
    public static final String MDC_GATEWAY_HANDLER_ADAPTER_NAME = "handlerAdapterName";
    public static final String MDC_GATEWAY_CHANNEL_ID = "channelId";
    public static final String MDC_SERVICE_SVR_START_TO_RUN_TIME = "svrStartToRunTime";
    public static final String MDC_DO_NOT_SEND_EMS = "doNotSendEMS";
}
