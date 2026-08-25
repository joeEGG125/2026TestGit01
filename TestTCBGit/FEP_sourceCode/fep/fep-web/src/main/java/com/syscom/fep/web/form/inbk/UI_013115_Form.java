package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;

public class UI_013115_Form extends BaseForm {

	private static final long serialVersionUID = 1L;
	private String bankRbl;
    private String bankNoTxt;
    private String aTMNoTxt;
    private String aTMWorkStatus;
    private String aTMServiceStatus;
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

    public String getBankRbl() {
        return bankRbl;
    }

    public void setBankRbl(String bankRbl) {
        this.bankRbl = bankRbl;
    }

    public String getBankNoTxt() {
        return bankNoTxt;
    }

    public void setBankNoTxt(String bankNoTxt) {
        this.bankNoTxt = bankNoTxt;
    }

    public String getaTMNoTxt() {
        return aTMNoTxt;
    }

    public void setaTMNoTxt(String aTMNoTxt) {
        this.aTMNoTxt = aTMNoTxt;
    }

    public String getaTMWorkStatus() {
        return aTMWorkStatus;
    }

    public void setaTMWorkStatus(String aTMWorkStatus) {
        this.aTMWorkStatus = aTMWorkStatus;
    }

    public String getaTMServiceStatus() {
        return aTMServiceStatus;
    }

    public void setaTMServiceStatus(String aTMServiceStatus) {
        this.aTMServiceStatus = aTMServiceStatus;
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

    public void setMessage(MessageType messageType, String message) {
        this.setMessageType(messageType);
        this.setMessage(message);
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
}
