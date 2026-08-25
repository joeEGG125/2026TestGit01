package com.syscom.fep.web.form.common;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;

public class UI_080060_Form extends BaseForm {

    private String logonId;
    private String oldSscod;
    private String newSscod;
    private String confimSscod;
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

    public String getLogonId() {
        return logonId;
    }

    public void setLogonId(String logonId) {
        this.logonId = logonId;
    }

    public String getOldSscod() {
        return oldSscod;
    }

    public void setOldSscod(String oldSscod) {
        this.oldSscod = oldSscod;
    }

    public String getNewSscod() {
        return newSscod;
    }

    public void setNewSscod(String newSscod) {
        this.newSscod = newSscod;
    }

    public String getConfimSscod() {
        return confimSscod;
    }

    public void setConfimSscod(String confimSscod) {
        this.confimSscod = confimSscod;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessage(MessageType messageType, String message) {
        this.setMessageType(messageType);
        this.setMessage(message);
    }
}
