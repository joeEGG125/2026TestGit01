package com.syscom.fep.web.form.inbk;

import com.syscom.fep.mybatis.model.Fundlog;
import com.syscom.fep.web.entity.MessageType;

public class UI_019313_Send {
    /**
     * 訊息類別
     */
    private MessageType messageType;
    /**
     * 訊息內容
     */
    private String message;

    private Fundlog fundlog;

    private String result;

    private String tlrid;

    private String fgAmt;

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

    public Fundlog getFundlog() {
        return fundlog;
    }

    public void setFundlog(Fundlog fundlog) {
        this.fundlog = fundlog;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTlrid() {
        return tlrid;
    }

    public void setTlrid(String tlrid) {
        this.tlrid = tlrid;
    }

    public String getFgAmt() {
        return fgAmt;
    }

    public void setFgAmt(String fgAmt) {
        this.fgAmt = fgAmt;
    }
}
