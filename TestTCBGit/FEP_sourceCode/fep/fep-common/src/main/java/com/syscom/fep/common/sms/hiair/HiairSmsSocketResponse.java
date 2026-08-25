package com.syscom.fep.common.sms.hiair;

public class HiairSmsSocketResponse extends HiairSmsBaseResponse {
    private int retCode;
    private String retMsg;

    public HiairSmsSocketResponse(Throwable error) {
        super(error);
    }

    public HiairSmsSocketResponse(String mobile, int retCode, String retMsg) {
        this.setMobile(mobile);
        this.setResult(retCode == HiairSmsSocketResponseCode.SUCCESSFUL.getRetCode());
        this.retCode = retCode;
        this.retMsg = retMsg;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }
}
