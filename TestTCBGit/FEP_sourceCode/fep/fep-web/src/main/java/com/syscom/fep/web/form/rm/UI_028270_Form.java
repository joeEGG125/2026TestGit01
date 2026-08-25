package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;


public class UI_028270_Form extends BaseForm {


    private String amlStatus;

    private String qryAmlFlag;

    private String amlFlag;

    private String executeBtnEnabled;

    public String getAmlStatus() {
        return amlStatus;
    }

    public void setAmlStatus(String amlStatus) {
        this.amlStatus = amlStatus;
    }

    public String getQryAmlFlag() {
        return qryAmlFlag;
    }

    public void setQryAmlFlag(String qryAmlFlag) {
        this.qryAmlFlag = qryAmlFlag;
    }

    public String getAmlFlag() {
        return amlFlag;
    }

    public void setAmlFlag(String amlFlag) {
        this.amlFlag = amlFlag;
    }

    public String getExecuteBtnEnabled() {
        return executeBtnEnabled;
    }

    public void setExecuteBtnEnabled(String executeBtnEnabled) {
        this.executeBtnEnabled = executeBtnEnabled;
    }

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
