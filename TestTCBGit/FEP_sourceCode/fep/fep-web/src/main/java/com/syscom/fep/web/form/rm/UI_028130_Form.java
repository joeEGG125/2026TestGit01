package com.syscom.fep.web.form.rm;

import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;


public class UI_028130_Form extends BaseForm {


    private String kind;

    private String uiItem;

    private String owpriority;

    private Rmout rmout = new Rmout();


    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getUiItem() {
        return uiItem;
    }

    public void setUiItem(String uiItem) {
        this.uiItem = uiItem;
    }

    public String getOwpriority() {
        return owpriority;
    }

    public void setOwpriority(String owpriority) {
        this.owpriority = owpriority;
    }

    public Rmout getRmout() {
        return rmout;
    }

    public void setRmout(Rmout rmout) {
        this.rmout = rmout;
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
