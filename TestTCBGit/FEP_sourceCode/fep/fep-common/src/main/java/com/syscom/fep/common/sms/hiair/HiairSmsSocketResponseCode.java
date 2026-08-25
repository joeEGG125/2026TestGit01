package com.syscom.fep.common.sms.hiair;

public enum HiairSmsSocketResponseCode {
    SUCCESSFUL(0, "Successful", "成功");

    private final int retCode;
    private final String retMsg;
    private final String description;

    HiairSmsSocketResponseCode(int retCode, String retMsg, String description) {
        this.retCode = retCode;
        this.retMsg = retMsg;
        this.description = description;
    }

    public int getRetCode() {
        return retCode;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public String getDescription() {
        return description;
    }
}
