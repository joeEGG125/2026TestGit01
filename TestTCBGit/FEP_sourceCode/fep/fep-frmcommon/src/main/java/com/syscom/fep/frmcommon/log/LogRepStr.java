package com.syscom.fep.frmcommon.log;

public enum LogRepStr {
    CR("[9ae6f2c259dd441590a941c656679c3b]", "\r"),
    LF("[10e61c13951544c087048d88dfbe9428]", "\n"),
    CRLF("[6bfd270930c448a093f70b5cda5dde03]", "\r\n");

    private String replace;
    private String real;

    private LogRepStr(String replace, String real) {
        this.replace = replace;
        this.real = real;
    }

    public String getReplace() {
        return replace;
    }

    public String getReal() {
        return real;
    }
}
