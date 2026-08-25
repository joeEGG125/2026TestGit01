package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.entity.atmmon.MsgkbNotify;
import com.syscom.fep.web.form.BaseForm;


public class UI_060620_B_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private String index;

    private String chbNotify;

    private String description;

    private String remark;

    private String responsible;

    private String notifyMail;

    private String action;
    private String msgPattern;
    private MsgkbNotify msgkbNotify;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getChbNotify() {
        return chbNotify;
    }

    public void setChbNotify(String chbNotify) {
        this.chbNotify = chbNotify;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getNotifyMail() {
        return notifyMail;
    }

    public void setNotifyMail(String notifyMail) {
        this.notifyMail = notifyMail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMsgPattern() {return msgPattern;}

    public void setMsgPattern(String msgPattern) {this.msgPattern = msgPattern;}

    public MsgkbNotify getMsgkbNotify() {
        return msgkbNotify;
    }

    public void setMsgkbNotify(MsgkbNotify msgkbNotify) {
        this.msgkbNotify = msgkbNotify;
    }
}
