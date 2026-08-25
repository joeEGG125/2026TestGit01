package com.syscom.fep.frmcommon.os.netstat;

public interface NetstatConstant {
    public static final int FIELD_UNIX_PROTOCOL = 0;
    public static final int FIELD_UNIX_RECEIVEQUEUE = 1;
    public static final int FIELD_UNIX_SENDQUEUE = 2;
    public static final int FIELD_UNIX_LOCALADDRESS = 3;
    public static final int FIELD_UNIX_FOREIGNADDRESS = 4;
    public static final int FIELD_UNIX_STATE = 5;
    public static final int FIELD_UNIX_PID_PROGRAM = 6;

    public static final int FIELD_WIN_PROTOCOL = 0;
    public static final int FIELD_WIN_LOCALADDRESS = 1;
    public static final int FIELD_WIN_FOREIGNADDRESS = 2;
    public static final int FIELD_WIN_STATE = 3;
    public static final int FIELD_WIN_PID = 4;
}
