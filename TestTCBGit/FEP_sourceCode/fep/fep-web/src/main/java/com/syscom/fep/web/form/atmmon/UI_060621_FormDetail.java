package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.entity.atmmon.MsgkbNotify;
import com.syscom.fep.web.form.BaseForm;

public class UI_060621_FormDetail extends BaseForm {
    private Long msgkbNo;

    private String errorcode;

    private String externalcode;

    private String exsubcode;

    private String severity;

    private boolean notify;

    private String responsible;

    private String action;

    private String msgpattern;

    private MsgkbNotify msgkbNotify;

    public Long getMsgkbNo() {return msgkbNo;}

    public void setMsgkbNo(Long msgkbNo) {this.msgkbNo = msgkbNo;}

    public String getErrorcode() {return errorcode;}

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

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public boolean getNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMsgpattern() {
        return msgpattern;
    }

    public void setMsgpattern(String msgpattern) {
        this.msgpattern = msgpattern;
    }

    public MsgkbNotify getMsgkbNotify() {
        return msgkbNotify;
    }

    public void setMsgkbNotify(MsgkbNotify msgkbNotify) {
        this.msgkbNotify = msgkbNotify;
    }
}