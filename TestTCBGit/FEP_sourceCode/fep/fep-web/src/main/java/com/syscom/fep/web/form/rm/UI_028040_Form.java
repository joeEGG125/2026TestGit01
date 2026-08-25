package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;


public class UI_028040_Form extends BaseForm {


    private String kind;

    private String txKind;

    private String senderSeq = "";

    private String receiverSeq = "";

    private String newSenderSeq = "";

    private String newReceiverSeq = "";

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

    public String getTxKind() {
        return txKind;
    }

    public void setTxKind(String txkind) {
        this.txKind = txkind;
    }

    public String getSenderSeq() {
        return senderSeq;
    }

    public void setSenderSeq(String senderSeq) {
        this.senderSeq = senderSeq;
    }

    public String getReceiverSeq() {
        return receiverSeq;
    }

    public void setReceiverSeq(String receiverSeq) {
        this.receiverSeq = receiverSeq;
    }

    public String getNewSenderSeq() {
        return newSenderSeq;
    }

    public void setNewSenderSeq(String newSenderSeq) {
        this.newSenderSeq = newSenderSeq;
    }

    public String getNewReceiverSeq() {
        return newReceiverSeq;
    }

    public void setNewReceiverSeq(String newReceiverSeq) {
        this.newReceiverSeq = newReceiverSeq;
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
