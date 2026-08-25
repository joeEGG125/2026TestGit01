package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

public class UI_060621_FormMain extends BaseForm {
    private String errorcode;

    private String externalcode;

    private String exsubcode;

    private Short notify;

    private Long msgkbNo;

    public String getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(String errorcode) {
        this.errorcode = errorcode;
    }

    public String getExternalcode() {
        return externalcode;
    }

    public void setExternalcode(String externalcode) {
        this.externalcode = externalcode;
    }

    public String getExsubcode() {
        return exsubcode;
    }

    public void setExsubcode(String exsubcode) {
        this.exsubcode = exsubcode;
    }

    public Short getNotify() {
        return notify;
    }

    public void setNotify(Short notify) {
        this.notify = notify;
    }

    public Long getMsgkbNo() {return msgkbNo;}

    public void setMsgkbNo(Long msgkbNo) {this.msgkbNo = msgkbNo;}
}
