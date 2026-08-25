package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;

public class UI_015313_Form extends BaseForm {
    /**
     * 訊息類別
     */
    private MessageType messageType;
    /**
     * 訊息內容
     */
    private String message;

    private String result;

    private String fgSeqno;

    private String txDateTxt;

    private String fgAmt;

    private String stanTxt;

    private String selectedValue;

    private String tlridTxt;

    private String supIDTxt;

    private String queryFlagTxt;

    public String getFgSeqno() {
        return fgSeqno;
    }

    public void setFgSeqno(String fgSeqno) {
        this.fgSeqno = fgSeqno;
    }

    public String getTxDateTxt() {
        return txDateTxt;
    }

    public void setTxDateTxt(String txDateTxt) {
        this.txDateTxt = txDateTxt;
    }

    public String getFgAmt() {
        return fgAmt;
    }

    public void setFgAmt(String fgAmt) {
        this.fgAmt = fgAmt;
    }

    public String getStanTxt() {
        return stanTxt;
    }

    public void setStanTxt(String stanTxt) {
        this.stanTxt = stanTxt;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public String getTlridTxt() {
        return tlridTxt;
    }

    public void setTlridTxt(String tlridTxt) {
        this.tlridTxt = tlridTxt;
    }

    public String getSupIDTxt() {
        return supIDTxt;
    }

    public void setSupIDTxt(String supIDTxt) {
        this.supIDTxt = supIDTxt;
    }

    public String getQueryFlagTxt() {
        return queryFlagTxt;
    }

    public void setQueryFlagTxt(String queryFlagTxt) {
        this.queryFlagTxt = queryFlagTxt;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
