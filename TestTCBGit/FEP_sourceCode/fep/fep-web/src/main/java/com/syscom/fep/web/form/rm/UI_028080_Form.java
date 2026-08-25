package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

public class UI_028080_Form extends BaseForm {
    private String kind;
    private String tradingDate;
    private String senderBankTxt;
    private String receiverBankTxt;
    private String msgPcode;
    private String msgflow;
    private String uiStan;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(String tradingDate) {
        this.tradingDate = tradingDate;
    }

    public String getSenderBankTxt() {
        return senderBankTxt;
    }

    public void setSenderBankTxt(String senderBankTxt) {
        this.senderBankTxt = senderBankTxt;
    }

    public String getReceiverBankTxt() {
        return receiverBankTxt;
    }

    public void setReceiverBankTxt(String receiverBankTxt) {
        this.receiverBankTxt = receiverBankTxt;
    }

    public String getMsgPcode() {
        return msgPcode;
    }

    public void setMsgPcode(String msgPcode) {
        this.msgPcode = msgPcode;
    }

    public String getMsgflow() {
        return msgflow;
    }

    public void setMsgflow(String msgflow) {
        this.msgflow = msgflow;
    }

    public String getUiStan() {
        return uiStan;
    }

    public void setUiStan(String uiStan) {
        this.uiStan = uiStan;
    }
}
