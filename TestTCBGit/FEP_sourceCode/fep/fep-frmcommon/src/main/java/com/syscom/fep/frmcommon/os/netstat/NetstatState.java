package com.syscom.fep.frmcommon.os.netstat;

public enum NetstatState {
    UNKNOWN,
    CLOSED,
    LISTEN,
    SYN_SENT,
    SYN_RCVD,
    ESTABLISHED,
    FIN_WAIT_1,
    FIN_WAIT_2,
    CLOSE_WAIT,
    CLOSING,
    LAST_ACK,
    TIME_WAIT,
    NONE,
    SYN_RECEIVED,
    SYN_SEND,
    TIMED_WAIT,
    LISTENING;
}
