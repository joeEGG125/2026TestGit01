package com.syscom.fep.base.enums;
public enum AdapterError {
    None(0),
    Error(-1),
    SendErrorWithRetry(-2),
    ReceiveError(-3),
    NetworkCardError(-4),
    NoDataError(-5),
    ServiceUnavailable(-6),
    ResumeOnline(-7),
    BindError(-8),
    ConnectError(-9),
    Timeout(99);
    private int value;

    private AdapterError(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
