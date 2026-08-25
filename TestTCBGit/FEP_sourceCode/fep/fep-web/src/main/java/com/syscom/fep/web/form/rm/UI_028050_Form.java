package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;


public class UI_028050_Form extends BaseForm {


    private String kind;

    private String bkno;

    private String senderSeqRv = "";

    private String receiverSeqRv = "";

    private String newSenderSeqRv = "";

    private String newReceiverSeqRv = "";

    /**
     * 執行結果
     */
    private boolean result = true;
    /**
     * 訊息類別
     */
    private MessageType messageType;
    /**
     * 訊息內容
     */
    private String message = "";

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getBkno() {
        return bkno;
    }

    public void setBkno(String bkno) {
        this.bkno = bkno;
    }

    public String getSenderSeqRv() {
        return senderSeqRv;
    }

    public void setSenderSeqRv(String senderSeqRv) {
        this.senderSeqRv = senderSeqRv;
    }

    public String getReceiverSeqRv() {
        return receiverSeqRv;
    }

    public void setReceiverSeqRv(String receiverSeqRv) {
        this.receiverSeqRv = receiverSeqRv;
    }

    public String getNewSenderSeqRv() {
        return newSenderSeqRv;
    }

    public void setNewSenderSeqRv(String newSenderSeqRv) {
        this.newSenderSeqRv = newSenderSeqRv;
    }

    public String getNewReceiverSeqRv() {
        return newReceiverSeqRv;
    }

    public void setNewReceiverSeqRv(String newReceiverSeqRv) {
        this.newReceiverSeqRv = newReceiverSeqRv;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
        if (messageType == MessageType.DANGER) {
            this.result = false;
        }
    }
    public void setMessage(MessageType messageType, String message) {
        this.setMessageType(messageType);
        this.setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
