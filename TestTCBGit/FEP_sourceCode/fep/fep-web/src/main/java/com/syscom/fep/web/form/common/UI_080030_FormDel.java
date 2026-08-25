package com.syscom.fep.web.form.common;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;

public class UI_080030_FormDel extends BaseForm {
    private Long pid;
    private Long id;
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

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
